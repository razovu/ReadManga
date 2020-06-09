package com.sale.readmanga.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.sale.readmanga.R
import kotlinx.android.synthetic.main.item_progress_bar.*

class ReadMangaAdapter : RecyclerView.Adapter<ReadMangaVH>() {

    private val linksImg = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadMangaVH {
        return ReadMangaVH(
            LayoutInflater.from(parent.context).inflate(R.layout.reader_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReadMangaVH, position: Int) {
        holder.bind(linksImg[position])
    }

    override fun getItemCount(): Int = linksImg.size

    fun set(list: MutableList<String>) {
        this.linksImg.clear()
        this.linksImg.addAll(list)
        this.notifyDataSetChanged()
    }
}