package com.sale.readmanga.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sale.readmanga.R
import com.sale.readmanga.data.MangaListForHistory
import kotlinx.android.synthetic.main.item_history.view.*


class HistoryRVAdapter : RecyclerView.Adapter<HistoryRVAdapter.HistoryAdapterVH>() {

    private val listOfManga = mutableListOf<MangaListForHistory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapterVH {
        return HistoryAdapterVH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        )
    }

    override fun getItemCount(): Int = listOfManga.size

    override fun onBindViewHolder(holder: HistoryAdapterVH, position: Int) {
        holder.bind(listOfManga[position])
    }

    fun set(list: MutableList<MangaListForHistory>) {
        this.listOfManga.clear()
        this.listOfManga.addAll(list)
        this.notifyDataSetChanged()
    }

    inner class HistoryAdapterVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var bundle = Bundle()

        init {
            itemView.setOnClickListener {
                it.findNavController().navigate(R.id.mangaDescFragment, bundle)
            }
        }

        fun bind(manga: MangaListForHistory) {
            val lastVolume = "Глава: ${manga.lastVol}"

            itemView.txt_name.text = manga.titleTxt
            Glide.with(itemView.context)
                .load(manga.linkImg)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemView.title_img)
            itemView.txt_time.text = manga.lastTime
            itemView.txt_last_vol.text = lastVolume

            bundle = bundleOf("link" to manga.linkOfManga, "title" to manga.titleTxt)
        }

    }
}