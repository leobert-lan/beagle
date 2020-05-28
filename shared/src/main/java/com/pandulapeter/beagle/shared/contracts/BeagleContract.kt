package com.pandulapeter.beagle.shared.contracts

import android.app.Application
import android.content.Context
import androidx.annotation.RestrictTo
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.pandulapeter.beagle.shared.configuration.Appearance
import com.pandulapeter.beagle.shared.configuration.Behavior

/**
 * This interface ensures that the real implementation and the noop variant have the same public API.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface BeagleContract {

    //region Core functionality
    /**
     * Can be used to enable or disable the library UI (but not its functionality) at runtime. Setting this to false has the side effect of calling the [hide] function.
     * Note: to completely disable the library UI as well as its functionality at compile-time, use the noop variant instead.
     *
     * @return Whether or not the library is currently enabled. Possible reasons for returning false:
     *  - The library has explicitly been disabled.
     *  - The application depends on the noop variant.
     */
    var isUiEnabled: Boolean
        get() = false
        set(_) = Unit

    /**
     * Initializes the library. No UI-related functionality will work before calling this function.
     *
     * @param application - Needed for hooking into the lifecycle.
     * @param appearance - Optional [Appearance] instance for customizing the appearance of the debug menu.
     * @param behavior - Optional [Behavior] instance for customizing the behavior of the debug menu.
     *
     * @return Whether or not the initialization was successful. Possible causes of failure:
     *  - The behavior specified the shake to open trigger gesture but the device does not have an accelerometer sensor.
     *  - The application depends on the noop variant.
     */
    fun initialize(application: Application, appearance: Appearance = Appearance(), behavior: Behavior = Behavior()): Boolean = false

    /**
     * Call this to show the debug menu.
     *
     * @return Whether or not the operation was successful. Possible causes of failure:
     *  - The library has not been initialized yet.
     *  - The library has explicitly been disabled.
     *  - The debug menu is already visible.
     *  - The application does not have any created activities.
     *  - The currently visible Activity is not a subclass of [FragmentActivity].
     *  - The currently visible Activity should not support a debug menu (social login overlay, in-app-purchase overlay, etc).
     *  - The application depends on the ui-view variant (in this case its your responsibility to show / hide the UI).
     *  - The application depends on the noop variant.
     */
    fun show(): Boolean = false

    /**
     * Call this to hide the debug menu.
     *
     * @return Whether or not the operation was successful. Possible causes of failure:
     *  - The library has not been initialized yet.
     *  - The debug menu is not currently visible.
     *  - The application depends on the noop variant.
     */
    fun hide(): Boolean = false
    //endregion

    //region Listeners
    /**
     * Adds a new [VisibilityListener] implementation to listen to the debug menu visibility changes.
     * The optional [Lifecycle] can be used to to automatically add / remove the listener when the lifecycle is started / stopped.
     *
     * @param listener - The [VisibilityListener] implementation to add.
     * @param lifecycle - The [Lifecycle] to use for automatically adding or removing the listener. Null by default.
     */
    fun addVisibilityListener(listener: VisibilityListener, lifecycle: Lifecycle? = null) = Unit

    /**
     * Removes the [VisibilityListener] implementation, if it was added to the list of listeners.
     *
     * @param listener - The [VisibilityListener] implementation to remove.
     */
    fun removeVisibilityListener(listener: VisibilityListener) = Unit

    /**
     * Removes all [VisibilityListener] implementations, from the list of listeners.
     */
    fun clearVisibilityListeners() = Unit
    //endregion

    //region Helpers
    /**
     * Convenience getter when a module callback implementation needs to perform UI-related operations or simply needs a [Context] instance.
     *
     * @return The nullable [FragmentActivity] instance on top of the back stack. Possible reasons for returning null:
     *  - The library has not been initialized yet.
     *  - The application does not have any created activities.
     *  - The currently visible Activity is not a subclass of [FragmentActivity].
     *  - The currently visible Activity should not support a debug menu (social login overlay, in-app-purchase overlay, etc).
     *  - The application depends on the noop variant.
     */
    val currentActivity: FragmentActivity? get() = null
    //endregion
}