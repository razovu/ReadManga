package com.sale.readmanga.adapters

import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sale.readmanga.data.Manga
import com.sale.readmanga.fragments.ListOfMangaFragmentDirections
import kotlinx.android.synthetic.main.crd_view_item.view.*
import kotlinx.android.synthetic.main.crd_view_item2.view.*

class MangaListVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var link = ""

    //Передаем аргументы в MangaDescriptionFragment
    init {
        itemView.setOnClickListener {
            it.findNavController().navigate(
                ListOfMangaFragmentDirections.actionListOfMangaToMangaDescFragment().setLink(link)
            )
        }
    }

    fun bind(manga: Manga) {
        itemView.title_txt2.text = manga.titleTxt
        Glide.with(itemView.context)
            .load(manga.linkImg)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(itemView.title_img2)
        link = manga.linkOfManga
    }

}