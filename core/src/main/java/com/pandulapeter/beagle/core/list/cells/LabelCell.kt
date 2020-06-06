package com.pandulapeter.beagle.core.list.cells

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import com.pandulapeter.beagle.common.contracts.module.Cell
import com.pandulapeter.beagle.common.contracts.module.ViewHolder
import com.pandulapeter.beagle.core.R
import com.pandulapeter.beagle.core.util.extension.dimension

internal data class LabelCell(
    override val id: String,
    private val text: CharSequence,
    @ColorInt private val color: Int?
) : Cell<LabelCell> {

    override fun createViewHolderDelegate() = object : ViewHolder.Delegate<LabelCell>() {

        override fun createViewHolder(parent: ViewGroup) = TextViewHolder(parent.context)
    }

    private class TextViewHolder(context: Context) : ViewHolder<LabelCell>(AppCompatTextView(context)) {

        init {
            itemView.context.dimension(R.dimen.beagle_content_padding).let { padding ->
                itemView.setPadding(padding * 2, padding, padding, padding)
            }
        }

        override fun bind(model: LabelCell) {
            (itemView as TextView).run {
                text = model.text
                model.color?.let { setTextColor(it) }
            }
        }
    }
}