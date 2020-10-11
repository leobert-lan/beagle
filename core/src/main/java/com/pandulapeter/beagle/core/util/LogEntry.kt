package com.pandulapeter.beagle.core.util

import com.pandulapeter.beagle.common.configuration.Text
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.common.contracts.module.Module

internal data class LogEntry(
    val label: String?,
    val message: CharSequence,
    val payload: CharSequence?,
    val timestamp: Long = System.currentTimeMillis()
) : BeagleListItemContract {

    override val title = Text.CharSequence(message)
    override val id: String = Module.randomId
}