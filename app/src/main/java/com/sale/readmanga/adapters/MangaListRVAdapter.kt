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
import com.sale.readmanga.data.MangaList
import kotlinx.android.synthetic.main.item_rv.view.*


class MangaListRVAdapter : RecyclerView.Adapter<MangaListRVAdapter.MangaListVH>() {

    private val listOfManga = mutableListOf<MangaList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaListVH {
        return MangaListVH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv, parent, false)
        )
    }

    override fun getItemCount(): Int = listOfManga.size

    override fun onBindViewHolder(holder: MangaListVH, position: Int) {
        holder.bind(listOfManga[position])
    }
      
    fun set(list: MutableList<MangaList>) {
        this.listOfManga.clear()
        this.listOfManga.addAll(list)
        this.notifyDataSetChanged()
    }

    inner class MangaListVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var bundle = Bundle()

        init {
            itemView.setOnClickListener {
                it.findNavController().navigate(R.id.mangaDescFragment, bundle)
            }
        }

        fun bind(manga: MangaList) {
            itemView.title_txt2.text = manga.titleTxt
            Glide.with(itemView.context)
                .load(manga.linkImg)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemView.title_img2)

            bundle = bundleOf("link" to manga.linkOfManga, "title" to manga.titleTxt)
        }

    }
}
