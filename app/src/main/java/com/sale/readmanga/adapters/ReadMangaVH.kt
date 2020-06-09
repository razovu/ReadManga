package com.sale.readmanga.adapters

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reader_item.view.*


class ReadMangaVH (itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(link: String) {
        itemView.mBigImage.showImage(Uri.parse(link))
    }

}