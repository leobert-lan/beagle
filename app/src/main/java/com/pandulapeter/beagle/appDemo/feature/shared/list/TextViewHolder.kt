package com.pandulapeter.beagle.appDemo.feature.shared.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import com.pandulapeter.beagle.appDemo.R
import com.pandulapeter.beagle.appDemo.databinding.ItemTextBinding
import com.pandulapeter.beagle.appDemo.feature.main.inspiration.authentication.list.AuthenticationListItem
import com.pandulapeter.beagle.appDemo.feature.main.inspiration.basicSetup.list.BasicSetupListItem
import com.pandulapeter.beagle.appDemo.feature.main.inspiration.list.InspirationListItem

class TextViewHolder private constructor(
    binding: ItemTextBinding
) : BaseViewHolder<ItemTextBinding, TextViewHolder.UiModel>(binding) {

    data class UiModel(
        @StringRes val textResourceId: Int,
        override val id: String = "text_$textResourceId"
    ) : ListItem, InspirationListItem, BasicSetupListItem, AuthenticationListItem

    companion object {
        fun create(
            parent: ViewGroup
        ) = TextViewHolder(
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_text, parent, false)
        )
    }
}