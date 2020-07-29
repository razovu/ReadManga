package com.sale.readmanga.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.sale.readmanga.R
import com.sale.readmanga.data.MangaVolume
import kotlinx.android.synthetic.main.item_volumes.view.*

class MangaVolumesRVAdapter: RecyclerView.Adapter<MangaVolumesRVAdapter.MangaVolumeVH>() {

    var listVol = mutableListOf<MangaVolume>()
    var historyList = mutableListOf<String>()
    var mangaTitle = ""
    var mangaLink = ""
    var mangaImg = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaVolumeVH {
        return MangaVolumeVH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_volumes, parent, false)
        )
    }

    override fun getItemCount(): Int = listVol.size

    override fun onBindViewHolder(holder: MangaVolumeVH, position: Int) {
        holder.bind(listVol[position], (position - listVol.size + 1).toString())
    }

    fun set(list: MutableList<MangaVolume>, title: String, img: String, link: String, history: MutableList<String>) {
        this.listVol.clear()
        this.listVol.addAll(list)
        this.notifyDataSetChanged()

        mangaTitle = title
        mangaImg = img
        mangaLink = link
        historyList = history
    }

    inner class MangaVolumeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pos = ""

        init {
            itemView.setOnClickListener {
                val bundle: Bundle = bundleOf(
                    "volList" to listVol.reversed().toTypedArray(),
                    "mangaLink" to mangaLink,
                    "mangaImg" to mangaImg,
                    "chapterNum" to pos,
                    "mangaTitle" to mangaTitle
                )
                it.findNavController().navigate(R.id.readThisFragment, bundle)
            }
        }

        fun bind(volume: MangaVolume, position: String){
            itemView.volumes_txt.text = volume.volName
            pos = position.replace("-", "")
            if (historyList.contains(pos)) {
                itemView.volumes_txt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, R.drawable.watched, 0
                )
            }
        }
    }
}
