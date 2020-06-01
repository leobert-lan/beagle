package com.pandulapeter.beagle.modules

import androidx.annotation.ColorInt
import com.pandulapeter.beagle.common.contracts.Cell
import com.pandulapeter.beagle.common.contracts.modules.SwitchModuleContract
import com.pandulapeter.beagle.core.list.cells.SwitchCell
import java.util.UUID

/**
 * Displays a switch.
 *
 * @param id - A unique identifier for the module. Optional, auto-generated by default.
 * @param text - The text to display.
 * @param color - The resolved color for the text. Optional, color from theme is used by default.
 * @param initialValue - Whether or not the switch should be enabled initially. Optional, false by default.
 * @param onValueChanged - Callback triggered when the user toggles the switch.
 */
class SwitchModule(
    override val id: String = "switch_${UUID.randomUUID()}",
    override val text: CharSequence,
    @ColorInt override val color: Int? = null,
    override val initialValue: Boolean = false,
    override val onValueChanged: (Boolean) -> Unit
) : SwitchModuleContract {

    override fun createCells() = listOf<Cell<*>>(SwitchCell(id, text, color, initialValue, onValueChanged))
}