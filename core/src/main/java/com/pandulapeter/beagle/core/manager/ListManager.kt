package com.pandulapeter.beagle.core.manager

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.pandulapeter.beagle.BeagleCore
import com.pandulapeter.beagle.common.configuration.Placement
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.common.contracts.module.Module
import com.pandulapeter.beagle.common.contracts.module.ValueWrapperModule
import com.pandulapeter.beagle.core.list.CellAdapter
import com.pandulapeter.beagle.core.list.delegates.AnimationDurationSwitchDelegate
import com.pandulapeter.beagle.core.list.delegates.AppInfoButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.ButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.CheckBoxDelegate
import com.pandulapeter.beagle.core.list.delegates.DeveloperOptionsButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.DeviceInfoDelegate
import com.pandulapeter.beagle.core.list.delegates.DividerDelegate
import com.pandulapeter.beagle.core.list.delegates.ForceCrashButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.GalleryButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.HeaderDelegate
import com.pandulapeter.beagle.core.list.delegates.ItemListDelegate
import com.pandulapeter.beagle.core.list.delegates.KeyValueListDelegate
import com.pandulapeter.beagle.core.list.delegates.KeylineOverlaySwitchDelegate
import com.pandulapeter.beagle.core.list.delegates.LabelDelegate
import com.pandulapeter.beagle.core.list.delegates.LogListDelegate
import com.pandulapeter.beagle.core.list.delegates.LongTextDelegate
import com.pandulapeter.beagle.core.list.delegates.LoremIpsumGeneratorButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.MultipleSelectionListDelegate
import com.pandulapeter.beagle.core.list.delegates.NetworkLogListDelegate
import com.pandulapeter.beagle.core.list.delegates.PaddingDelegate
import com.pandulapeter.beagle.core.list.delegates.ScreenCaptureToolboxDelegate
import com.pandulapeter.beagle.core.list.delegates.ScreenRecordingButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.ScreenshotButtonDelegate
import com.pandulapeter.beagle.core.list.delegates.SingleSelectionListDelegate
import com.pandulapeter.beagle.core.list.delegates.SliderDelegate
import com.pandulapeter.beagle.core.list.delegates.SwitchDelegate
import com.pandulapeter.beagle.core.list.delegates.TextDelegate
import com.pandulapeter.beagle.core.list.delegates.TextInputDelegate
import com.pandulapeter.beagle.core.view.GestureBlockingRecyclerView
import com.pandulapeter.beagle.modules.AnimationDurationSwitchModule
import com.pandulapeter.beagle.modules.AppInfoButtonModule
import com.pandulapeter.beagle.modules.ButtonModule
import com.pandulapeter.beagle.modules.CheckBoxModule
import com.pandulapeter.beagle.modules.DeveloperOptionsButtonModule
import com.pandulapeter.beagle.modules.DeviceInfoModule
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.ForceCrashButtonModule
import com.pandulapeter.beagle.modules.GalleryButtonModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.ItemListModule
import com.pandulapeter.beagle.modules.KeyValueListModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import com.pandulapeter.beagle.modules.LabelModule
import com.pandulapeter.beagle.modules.LogListModule
import com.pandulapeter.beagle.modules.LongTextModule
import com.pandulapeter.beagle.modules.LoremIpsumGeneratorButtonModule
import com.pandulapeter.beagle.modules.MultipleSelectionListModule
import com.pandulapeter.beagle.modules.NetworkLogListModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.ScreenCaptureToolboxModule
import com.pandulapeter.beagle.modules.ScreenRecordingButtonModule
import com.pandulapeter.beagle.modules.ScreenshotButtonModule
import com.pandulapeter.beagle.modules.SingleSelectionListModule
import com.pandulapeter.beagle.modules.SliderModule
import com.pandulapeter.beagle.modules.SwitchModule
import com.pandulapeter.beagle.modules.TextInputModule
import com.pandulapeter.beagle.modules.TextModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.reflect.KClass

internal class ListManager {

    val hasPendingUpdates get() = persistableModules.any { it.hasPendingChanges(BeagleCore.implementation) }
    private var countDownJob: Job? = null
    private var shouldBlockGestures = true
    private val cellAdapter = CellAdapter()
    private val moduleManagerContext = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
    private val modules = mutableListOf<Module<*>>()
    private val moduleDelegates = mutableMapOf(
        AnimationDurationSwitchModule::class to AnimationDurationSwitchDelegate(),
        AppInfoButtonModule::class to AppInfoButtonDelegate(),
        ButtonModule::class to ButtonDelegate(),
        CheckBoxModule::class to CheckBoxDelegate(),
        DeveloperOptionsButtonModule::class to DeveloperOptionsButtonDelegate(),
        DeviceInfoModule::class to DeviceInfoDelegate(),
        DividerModule::class to DividerDelegate(),
        ForceCrashButtonModule::class to ForceCrashButtonDelegate(),
        GalleryButtonModule::class to GalleryButtonDelegate(),
        HeaderModule::class to HeaderDelegate(),
        LabelModule::class to LabelDelegate(),
        LogListModule::class to LogListDelegate(),
        LongTextModule::class to LongTextDelegate(),
        LoremIpsumGeneratorButtonModule::class to LoremIpsumGeneratorButtonDelegate(),
        ItemListModule::class to ItemListDelegate<BeagleListItemContract>(),
        KeylineOverlaySwitchModule::class to KeylineOverlaySwitchDelegate(),
        KeyValueListModule::class to KeyValueListDelegate(),
        MultipleSelectionListModule::class to MultipleSelectionListDelegate<BeagleListItemContract>(),
        NetworkLogListModule::class to NetworkLogListDelegate(),
        PaddingModule::class to PaddingDelegate(),
        ScreenCaptureToolboxModule::class to ScreenCaptureToolboxDelegate(),
        ScreenRecordingButtonModule::class to ScreenRecordingButtonDelegate(),
        ScreenshotButtonModule::class to ScreenshotButtonDelegate(),
        SingleSelectionListModule::class to SingleSelectionListDelegate<BeagleListItemContract>(),
        SliderModule::class to SliderDelegate(),
        SwitchModule::class to SwitchDelegate(),
        TextModule::class to TextDelegate(),
        TextInputModule::class to TextInputDelegate()
    )

    //TODO: This might cause concurrency issues. Consider making it a suspend function.
    private val persistableModules get() = modules.filterIsInstance<ValueWrapperModule<*, *>>()

    fun setupRecyclerView(recyclerView: GestureBlockingRecyclerView) = recyclerView.run {
        adapter = cellAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(recyclerView.context)
        shouldBlockGestures = { this@ListManager.shouldBlockGestures }
    }

    fun setModules(newModules: List<Module<*>>, onContentsChanged: () -> Unit) {
        GlobalScope.launch { setModulesInternal(newModules, onContentsChanged) }
    }

    fun addModules(newModules: List<Module<*>>, placement: Placement, lifecycleOwner: LifecycleOwner?, onContentsChanged: () -> Unit) {
        GlobalScope.launch { addModulesInternal(newModules, placement, lifecycleOwner, onContentsChanged) }
    }

    fun removeModules(ids: List<String>, onContentsChanged: () -> Unit) {
        GlobalScope.launch { removeModulesInternal(ids, onContentsChanged) }
    }

    fun refreshCells(onContentsChanged: () -> Unit) {
        GlobalScope.launch { refreshCellsInternal(onContentsChanged) }
    }

    fun contains(id: String) = modules.any { it.id == id }

    fun applyPendingChanges() {
        persistableModules.forEach { it.applyPendingChanges(BeagleCore.implementation) }
        BeagleCore.implementation.refresh()
    }

    fun resetPendingChanges() {
        persistableModules.forEach { it.resetPendingChanges(BeagleCore.implementation) }
        BeagleCore.implementation.refresh()
    }

    //TODO: This might cause concurrency issues. Consider making it a suspend function.
    @Suppress("UNCHECKED_CAST")
    fun <M : Module<*>> findModule(id: String): M? = modules.firstOrNull { it.id == id } as? M?

    //TODO: This might cause concurrency issues. Consider making it a suspend function.
    @Suppress("UNCHECKED_CAST")
    fun <M : Module<M>> findModuleDelegate(type: KClass<out M>) = moduleDelegates[type] as? Module.Delegate<M>?

    private suspend fun setModulesInternal(newModules: List<Module<*>>, onContentsChanged: () -> Unit) = withContext(moduleManagerContext) {
        modules.clear()
        modules.addAll(newModules.distinctBy { it.id })
        refreshCellsInternal(onContentsChanged)
    }

    @Suppress("unused")
    private suspend fun addModulesInternal(newModules: List<Module<*>>, placement: Placement, lifecycleOwner: LifecycleOwner?, onContentsChanged: () -> Unit) =
        lifecycleOwner?.lifecycle?.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                GlobalScope.launch { addModulesInternal(newModules, placement, onContentsChanged) }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                GlobalScope.launch { removeModules(newModules.map { it.id }, onContentsChanged) }
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        }) ?: addModulesInternal(newModules, placement, onContentsChanged)

    private suspend fun addModulesInternal(newModules: List<Module<*>>, placement: Placement, onContentsChanged: () -> Unit) = withContext(moduleManagerContext) {
        modules.apply {
            var newIndex = 0
            newModules.distinctBy { it.id }.forEach { module ->
                indexOfFirst { it.id == module.id }.also { currentIndex ->
                    if (currentIndex != -1) {
                        removeAt(currentIndex)
                        add(currentIndex, module)
                    } else {
                        when (placement) {
                            Placement.Bottom -> add(module)
                            Placement.Top -> add(newIndex++, module)
                            is Placement.Below -> {
                                indexOfFirst { it.id == placement.id }.also { referencePosition ->
                                    if (referencePosition == -1) {
                                        add(module)
                                    } else {
                                        add(referencePosition + 1 + newIndex++, module)
                                    }
                                }
                            }
                            is Placement.Above -> {
                                indexOfFirst { it.id == placement.id }.also { referencePosition ->
                                    if (referencePosition == -1) {
                                        add(newIndex++, module)
                                    } else {
                                        add(referencePosition, module)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        refreshCellsInternal(onContentsChanged)
    }

    private suspend fun removeModulesInternal(ids: List<String>, onContentsChanged: () -> Unit) = withContext(moduleManagerContext) {
        modules.removeAll { ids.contains(it.id) }
        refreshCellsInternal(onContentsChanged)
    }

    //TODO: Throw custom exception if no handler is found
    private suspend fun refreshCellsInternal(onContentsChanged: () -> Unit) = withContext(moduleManagerContext) {
        val newCells = modules.flatMap { module ->
            (moduleDelegates[module::class] ?: (module.createModuleDelegate().also {
                moduleDelegates[module::class] = it
            })).forceCreateCells(module)
        }
        if (cellAdapter.itemCount > 0 && cellAdapter.itemCount != newCells.size) {
            shouldBlockGestures = true
        }
        cellAdapter.submitList(newCells) {
            onContentsChanged()
            countDownJob?.cancel()
            countDownJob = GlobalScope.launch {
                delay(300)
                shouldBlockGestures = false
            }
        }
    }
}