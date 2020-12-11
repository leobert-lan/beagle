package com.pandulapeter.beagle.core

import android.app.Application
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import com.pandulapeter.beagle.BeagleCore
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.Placement
import com.pandulapeter.beagle.common.configuration.Text
import com.pandulapeter.beagle.common.contracts.BeagleContract
import com.pandulapeter.beagle.common.contracts.module.Module
import com.pandulapeter.beagle.common.listeners.LogListener
import com.pandulapeter.beagle.common.listeners.NetworkLogListener
import com.pandulapeter.beagle.common.listeners.OverlayListener
import com.pandulapeter.beagle.common.listeners.UpdateListener
import com.pandulapeter.beagle.common.listeners.VisibilityListener
import com.pandulapeter.beagle.core.manager.BugReportManager
import com.pandulapeter.beagle.core.manager.CrashLogManager
import com.pandulapeter.beagle.core.manager.DebugMenuInjector
import com.pandulapeter.beagle.core.manager.LifecycleLogManager
import com.pandulapeter.beagle.core.manager.ListManager
import com.pandulapeter.beagle.core.manager.LocalStorageManager
import com.pandulapeter.beagle.core.manager.LogManager
import com.pandulapeter.beagle.core.manager.MemoryStorageManager
import com.pandulapeter.beagle.core.manager.NetworkLogManager
import com.pandulapeter.beagle.core.manager.ScreenCaptureManager
import com.pandulapeter.beagle.core.manager.ShakeDetector
import com.pandulapeter.beagle.core.manager.UiManagerContract
import com.pandulapeter.beagle.core.manager.listener.LogListenerManager
import com.pandulapeter.beagle.core.manager.listener.NetworkLogListenerManager
import com.pandulapeter.beagle.core.manager.listener.OverlayListenerManager
import com.pandulapeter.beagle.core.manager.listener.UpdateListenerManager
import com.pandulapeter.beagle.core.manager.listener.VisibilityListenerManager
import com.pandulapeter.beagle.core.util.model.CrashLogEntry
import com.pandulapeter.beagle.core.util.model.RestoreModel
import com.pandulapeter.beagle.core.view.gallery.MediaPreviewDialogFragment
import com.pandulapeter.beagle.core.view.logDetail.LogDetailDialogFragment
import com.pandulapeter.beagle.core.view.networkLogDetail.NetworkLogDetailDialogFragment
import com.pandulapeter.beagle.modules.LifecycleLogListModule
import com.pandulapeter.beagle.utils.extensions.hideKeyboard
import com.pandulapeter.beagle.utils.view.GestureBlockingRecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
import kotlin.reflect.KClass

class BeagleImplementation(val uiManager: UiManagerContract) : BeagleContract {

    override var isUiEnabled by Delegates.observable(true) { _, _, newValue ->
        if (!newValue) {
            hide()
        }
    }
    override val currentActivity get() = debugMenuInjector.currentActivity
    var appearance = Appearance()
        private set
    var behavior = Behavior()
        private set
    lateinit var videoThumbnailLoader: ImageLoader
    internal val hasPendingUpdates get() = listManager.hasPendingUpdates
    internal val memoryStorageManager by lazy { MemoryStorageManager() }
    internal lateinit var localStorageManager: LocalStorageManager
        private set
    private val shakeDetector by lazy { ShakeDetector() }
    private val debugMenuInjector by lazy { DebugMenuInjector(uiManager) }
    private val logListenerManager by lazy { LogListenerManager() }
    private val networkLogListenerManager by lazy { NetworkLogListenerManager() }
    private val overlayListenerManager by lazy { OverlayListenerManager() }
    private val updateListenerManager by lazy { UpdateListenerManager() }
    private val visibilityListenerManager by lazy { VisibilityListenerManager() }
    private val crashLogManager by lazy { CrashLogManager() }
    private val logManager by lazy { LogManager(logListenerManager, listManager, ::refresh) }
    private val lifecycleLogManager by lazy { LifecycleLogManager(listManager, ::refresh) }
    private val networkLogManager by lazy { NetworkLogManager(networkLogListenerManager, listManager, ::refresh) }
    private val listManager by lazy { ListManager() }
    private val screenCaptureManager by lazy { ScreenCaptureManager() }
    private val bugReportManager by lazy { BugReportManager() }
    internal var onScreenCaptureReady: ((Uri?) -> Unit)?
        get() = screenCaptureManager.onScreenCaptureReady
        set(value) {
            screenCaptureManager.onScreenCaptureReady = value
        }

    init {
        BeagleCore.implementation = this
    }

    override fun initialize(
        application: Application,
        appearance: Appearance,
        behavior: Behavior
    ) = (behavior.shakeDetectionBehavior.threshold == null || shakeDetector.initialize(application)).also {
        this.appearance = appearance
        this.behavior = behavior
        logManager.application = application
        crashLogManager.application = application
        this.localStorageManager = LocalStorageManager(application)
        behavior.bugReportingBehavior.crashLoggers.forEach { it.initialize(application) }
        debugMenuInjector.register(application)
        behavior.logBehavior.loggers.forEach { it.register(::log, ::clearLogs) }
        behavior.networkLogBehavior.networkLoggers.forEach { it.register(::logNetworkEvent, ::clearNetworkLogs) }
        videoThumbnailLoader = ImageLoader.Builder(application)
            .componentRegistry {
                add(VideoFrameFileFetcher(application))
            }
            .build()
    }

    override fun show() = (currentActivity?.let { currentActivity ->
        if (currentActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            && currentActivity.supportFragmentManager.findFragmentByTag(MediaPreviewDialogFragment.TAG) == null
        ) uiManager.show(currentActivity) else false
    } ?: false)

    override fun hide() = uiManager.hide(currentActivity)

    override fun set(vararg modules: Module<*>) = listManager.setModules(
        newModules = modules.toList(),
        onContentsChanged = updateListenerManager::notifyListenersOnContentsChanged
    )

    override fun add(
        vararg modules: Module<*>,
        placement: Placement,
        lifecycleOwner: LifecycleOwner?
    ) = listManager.addModules(
        newModules = modules.toList(),
        placement = placement,
        lifecycleOwner = lifecycleOwner,
        onContentsChanged = updateListenerManager::notifyListenersOnContentsChanged
    )

    override fun remove(vararg ids: String) = listManager.removeModules(
        ids = ids.toList(),
        onContentsChanged = updateListenerManager::notifyListenersOnContentsChanged
    )

    override fun contains(id: String) = listManager.contains(id)

    override fun <M : Module<M>> find(id: String) = listManager.findModule<M>(id)

    override fun <M : Module<M>> delegateFor(type: KClass<out M>) = listManager.findModuleDelegate(type)

    override fun addLogListener(
        listener: LogListener,
        lifecycleOwner: LifecycleOwner?
    ) = logListenerManager.addListener(
        listener = listener,
        lifecycleOwner = lifecycleOwner
    )

    override fun removeLogListener(listener: LogListener) = logListenerManager.removeListener(listener)

    override fun clearLogListeners() = logListenerManager.clearListeners()

    override fun addNetworkLogListener(
        listener: NetworkLogListener,
        lifecycleOwner: LifecycleOwner?
    ) = networkLogListenerManager.addListener(
        listener = listener,
        lifecycleOwner = lifecycleOwner
    )

    override fun removeNetworkLogListener(listener: NetworkLogListener) = networkLogListenerManager.removeListener(listener)

    override fun clearNetworkLogListeners() = networkLogListenerManager.clearListeners()

    internal fun addInternalOverlayListener(listener: OverlayListener) = overlayListenerManager.addInternalListener(listener)

    override fun addOverlayListener(
        listener: OverlayListener,
        lifecycleOwner: LifecycleOwner?
    ) = overlayListenerManager.addListener(
        listener = listener,
        lifecycleOwner = lifecycleOwner
    )

    override fun removeOverlayListener(listener: OverlayListener) = overlayListenerManager.removeListener(listener)

    fun addInternalUpdateListener(listener: UpdateListener) = updateListenerManager.addInternalListener(listener)

    override fun addUpdateListener(
        listener: UpdateListener,
        lifecycleOwner: LifecycleOwner?
    ) = updateListenerManager.addListener(
        listener = listener,
        lifecycleOwner = lifecycleOwner
    )

    override fun removeUpdateListener(listener: UpdateListener) = updateListenerManager.removeListener(listener)

    override fun clearUpdateListeners() = updateListenerManager.clearListeners()

    override fun clearOverlayListeners() = overlayListenerManager.clearListeners()

    private fun addInternalVisibilityListener(listener: VisibilityListener) = visibilityListenerManager.addInternalListener(listener)

    override fun addVisibilityListener(
        listener: VisibilityListener,
        lifecycleOwner: LifecycleOwner?
    ) = visibilityListenerManager.addListener(
        listener = listener,
        lifecycleOwner = lifecycleOwner
    )

    override fun removeVisibilityListener(listener: VisibilityListener) = visibilityListenerManager.removeListener(listener)

    override fun clearVisibilityListeners() = visibilityListenerManager.clearListeners()

    override fun log(
        message: String,
        label: String?,
        payload: String?,
        isPersisted: Boolean,
        timestamp: Long,
        id: String
    ) = logManager.log(
        label = label,
        message = message,
        payload = payload,
        isPersisted = isPersisted,
        timestamp = timestamp,
        id = id
    )

    override fun clearLogs(label: String?) = logManager.clearLogs(label)

    override fun logNetworkEvent(
        isOutgoing: Boolean,
        url: String,
        payload: String?,
        headers: List<String>?,
        duration: Long?,
        timestamp: Long,
        id: String
    ) = networkLogManager.log(
        isOutgoing = isOutgoing,
        url = url,
        payload = payload,
        headers = headers,
        duration = duration,
        timestamp = timestamp,
        id = id
    )

    override fun clearNetworkLogs() = networkLogManager.clearLogs()

    override fun clearLifecycleLogs() = lifecycleLogManager.clearLogs()

    override fun clearCrashLogs() = crashLogManager.clearLogs()

    override fun takeScreenshot() = screenCaptureManager.takeScreenshot()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun recordScreen() = screenCaptureManager.recordScreen()

    override fun openGallery() = screenCaptureManager.openGallery()

    override fun openBugReportingScreen() = bugReportManager.openBugReportingScreen()

    override fun refresh() = listManager.refreshCells(updateListenerManager::notifyListenersOnContentsChanged)

    override fun invalidateOverlay() = debugMenuInjector.invalidateOverlay()

    override fun showDialog(
        content: Text,
        isHorizontalScrollEnabled: Boolean,
        shouldShowShareButton: Boolean,
        timestamp: Long,
        id: String
    ) = showDialog(
        content = content,
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        shouldShowShareButton = shouldShowShareButton,
        timestamp = timestamp,
        id = id,
        fileName = ""
    )

    override fun showNetworkEventDialog(
        isOutgoing: Boolean,
        url: String,
        payload: String,
        headers: List<String>?,
        duration: Long?,
        timestamp: Long,
        id: String
    ) {
        (uiManager.findHostFragmentManager() ?: currentActivity?.supportFragmentManager)?.let { fragmentManager ->
            NetworkLogDetailDialogFragment.show(
                fragmentManager = fragmentManager,
                isOutgoing = isOutgoing,
                url = url,
                payload = payload,
                headers = headers,
                duration = duration,
                timestamp = timestamp,
                id = id
            )
        }
    }

    override fun performOnHide(action: () -> Any?) {
        val listener = object : VisibilityListener {
            override fun onHidden() {
                val reference = this
                action()
                GlobalScope.launch {
                    delay(100)
                    removeVisibilityListener(reference)
                }
            }
        }
        addInternalVisibilityListener(listener)
        if (!hide()) {
            removeVisibilityListener(listener)
            listener.onHidden()
        }
    }

    internal fun showDialog(
        content: Text,
        isHorizontalScrollEnabled: Boolean,
        shouldShowShareButton: Boolean,
        timestamp: Long,
        id: String,
        fileName: String
    ) {
        (uiManager.findHostFragmentManager() ?: currentActivity?.supportFragmentManager)?.let { fragmentManager ->
            LogDetailDialogFragment.show(
                fragmentManager = fragmentManager,
                content = content,
                isHorizontalScrollEnabled = isHorizontalScrollEnabled,
                shouldShowShareButton = shouldShowShareButton,
                timestamp = timestamp,
                id = id,
                fileName = fileName
            )
        }
    }

    internal fun applyPendingChanges() {
        listManager.applyPendingChanges()
        updateListenerManager.notifyListenersOnAllPendingChangesApplied()
    }

    internal fun resetPendingChanges() = listManager.resetPendingChanges()

    fun getLogEntries(label: String?) = logManager.getEntries(label)

    fun getLifecycleLogEntries(eventTypes: List<LifecycleLogListModule.EventType>?) = lifecycleLogManager.getEntries(eventTypes)

    internal fun restoreAfterCrash(restoreModel: RestoreModel) {
        logManager.restore(restoreModel.logs)
        networkLogManager.restore(restoreModel.networkLogs)
        lifecycleLogManager.restore(restoreModel.lifecycleLogs)
    }

    internal fun logCrash(crashLogEntry: CrashLogEntry) = crashLogManager.log(crashLogEntry)

    internal fun logLifecycle(
        classType: Class<*>,
        eventType: LifecycleLogListModule.EventType,
        hasSavedInstanceState: Boolean? = null
    ) = lifecycleLogManager.log(
        classType = classType,
        eventType = eventType,
        hasSavedInstanceState = hasSavedInstanceState
    )

    internal suspend fun getCrashLogEntries() = crashLogManager.getCrashLogEntries()

    fun getNetworkLogEntries() = networkLogManager.getEntries()

    internal fun createOverlayLayout(activity: FragmentActivity, overlayFragment: Fragment) = uiManager.createOverlayLayout(activity, overlayFragment)

    fun notifyVisibilityListenersOnShow() = visibilityListenerManager.notifyListenersOnShow()

    fun notifyVisibilityListenersOnHide() = visibilityListenerManager.notifyListenersOnHide()

    internal fun notifyOverlayListenersOnDrawOver(canvas: Canvas) = overlayListenerManager.notifyListeners(canvas)

    fun hideKeyboard() = currentActivity?.currentFocus?.hideKeyboard() ?: Unit

    internal fun setupRecyclerView(recyclerView: GestureBlockingRecyclerView) = listManager.setupRecyclerView(recyclerView)
}