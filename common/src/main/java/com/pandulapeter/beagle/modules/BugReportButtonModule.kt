package com.pandulapeter.beagle.modules

import androidx.annotation.DrawableRes
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.Text
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.module.Module
import com.pandulapeter.beagle.modules.AppInfoButtonModule.Companion.ID
import com.pandulapeter.beagle.modules.BugReportButtonModule.Companion.DEFAULT_ICON
import com.pandulapeter.beagle.modules.BugReportButtonModule.Companion.DEFAULT_IS_ENABLED
import com.pandulapeter.beagle.modules.BugReportButtonModule.Companion.DEFAULT_ON_BUTTON_PRESSED
import com.pandulapeter.beagle.modules.BugReportButtonModule.Companion.DEFAULT_TEXT
import com.pandulapeter.beagle.modules.BugReportButtonModule.Companion.DEFAULT_TYPE
import com.pandulapeter.beagle.modules.BugReportButtonModule.Companion.ID
import com.pandulapeter.beagle.modules.GalleryButtonModule.Companion.ID
import com.pandulapeter.beagle.modules.ScreenshotButtonModule.Companion.ID


/**
 * Displays a button that opens the bug reporting screen. Empty sections will not be displayed.
 * Check out the [Appearance] and [Behavior] classes for customization options.
 *
 * This module can only be added once. It uses the value of [ID] as id.
 *
 * @param text - The text that should be displayed on the button. [DEFAULT_TEXT] by default.
 * @param type - Specify a [TextModule.Type] to apply a specific appearance. [DEFAULT_TYPE] by default.
 * @param icon - A drawable resource ID that will be tinted and displayed before the text, or null to display no icon. [DEFAULT_ICON] by default.
 * @param isEnabled - Can be used to enable or disable all user interaction with the module. [DEFAULT_IS_ENABLED] by default.
 * @param onButtonPressed - Callback invoked when the user presses the button. [DEFAULT_ON_BUTTON_PRESSED] by default.
 */
@Suppress("unused")
data class BugReportButtonModule(
    val text: Text = DEFAULT_TEXT.toText(),
    val type: TextModule.Type = DEFAULT_TYPE,
    @DrawableRes val icon: Int? = DEFAULT_ICON,
    val isEnabled: Boolean = DEFAULT_IS_ENABLED,
    val onButtonPressed: () -> Unit = {}
) : Module<BugReportButtonModule> {

    override val id: String = ID

    companion object {
        const val ID = "bugReportButton"
        private const val DEFAULT_TEXT = "Report a bug"
        private const val DEFAULT_SHOULD_SHOW_GALLERY_SECTION = true
        private const val DEFAULT_SHOULD_SHOW_NETWORK_LOGS_SECTION = true
        private val DEFAULT_LABEL_SECTIONS_TO_SHOW = listOf<String?>(null)
        private const val DEFAULT_SHOULD_SHOW_METADATA_SECTION = true
        private val DEFAULT_TYPE = TextModule.Type.NORMAL
        private val DEFAULT_ICON: Int? = null
        private const val DEFAULT_IS_ENABLED = true
        private val DEFAULT_ON_BUTTON_PRESSED: () -> Unit = {}
    }
}