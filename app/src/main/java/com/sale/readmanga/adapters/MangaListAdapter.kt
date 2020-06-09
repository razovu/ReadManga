package com.sale.readmanga.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sale.readmanga.R
import com.sale.readmanga.data.Manga
import com.sale.readmanga.fragments.ListOfMangaFragment


class MangaListAdapter : RecyclerView.Adapter<MangaListVH>() {

    private val listOfManga = mutableListOf<Manga>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaListVH {
        return MangaListVH(
            LayoutInflater.from(parent.context).inflate(R.layout.crd_view_item, parent, false)
        )
    }

    override fun getItemCount(): Int = listOfManga.size

    override fun onBindViewHolder(holder: MangaListVH, position: Int) {
        holder.bind(listOfManga[position])
    }


    fun set(list: MutableList<Manga>) {
        this.listOfManga.clear()
        this.listOfManga.addAll(list)
        this.notifyDataSetChanged()
    }
}