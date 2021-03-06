package com.pandulapeter.beagle.appDemo.data.model

import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract

data class BeagleListItemContractImplementation(
    private val name: CharSequence
) : BeagleListItemContract {

    override val title = name.toText()
}