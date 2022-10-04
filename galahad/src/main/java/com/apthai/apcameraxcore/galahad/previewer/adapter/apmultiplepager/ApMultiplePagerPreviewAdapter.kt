package com.apthai.apcameraxcore.galahad.previewer.adapter.apmultiplepager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apthai.apcameraxcore.common.model.ApImageUriAdapter
import com.apthai.apcameraxcore.galahad.R

class ApMultiplePagerPreviewAdapter(private val imageUri: ArrayList<ApImageUriAdapter>) :
    RecyclerView.Adapter<ApMultiplePagerPreviewViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApMultiplePagerPreviewViewHolder {
        return ApMultiplePagerPreviewViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ap_previewer_pager_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ApMultiplePagerPreviewViewHolder, position: Int) {
        holder.initView(this.imageUri[position])
    }

    override fun getItemCount(): Int = this.imageUri.size

    fun setToggleCheckedByPosition(position: Int) {
        if (this.isTurePosition(position)) {
            val item = this.imageUri[position]
            item.isChecked = !item.isChecked
            this.imageUri[position] = item
            notifyItemChanged(position, this.imageUri[position])
        }
    }

    fun getItemByPosition(position: Int): ApImageUriAdapter? {
        if (this.isTurePosition(position)) {
            return this.imageUri[position]
        }
        return null
    }

    fun getItemSelected(): ArrayList<ApImageUriAdapter> {
        return this.imageUri.filter { item -> item.isChecked } as ArrayList<ApImageUriAdapter>
    }

    fun updatePathUriByPosition(uriStr: String, position: Int) {
        if (this.isTurePosition(position)) {
            if (uriStr.isNotEmpty()) {
                val item = this.imageUri[position]
                item.uriStr = uriStr
                this.imageUri[position] = item
                notifyItemChanged(position, this.imageUri[position])
            }
        }
    }

    private fun isTurePosition(position: Int): Boolean =
        position >= 0 && position + 1 <= imageUri.size
}