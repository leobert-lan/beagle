package com.pandulapeter.beagle.modules

import com.pandulapeter.beagle.common.contracts.ModuleCell
import com.pandulapeter.beagle.common.contracts.modules.TextModuleContract
import com.pandulapeter.beagle.core.list.moduleCells.TextModuleCell
import java.util.UUID

/**
 * See [TextModuleContract] for documentation.
 */
data class TextModule(
    override val id: String = "text_${UUID.randomUUID()}",
    override val text: CharSequence
) : TextModuleContract {

    override fun createCells() = listOf<ModuleCell>(
        TextModuleCell(
            id = id,
            text = text
        )
    )
}