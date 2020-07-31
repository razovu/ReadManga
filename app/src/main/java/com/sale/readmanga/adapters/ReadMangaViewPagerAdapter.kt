package com.sale.readmanga.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.view.BigImageView
import com.sale.readmanga.R
import com.sale.readmanga.tools.ProgressIndicatorForBIV

class ReadMangaViewPagerAdapter(private val context: Context) : PagerAdapter() {

    private val imgList = mutableListOf<String>()


    init {

        for (url in imgList)
            BigImageViewer.prefetch(Uri.parse(url))
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return imgList.size
    }

    fun set(list: MutableList<String>) {
        this.imgList.clear()
        this.imgList.addAll(list)
        this.notifyDataSetChanged()
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val imgView = BigImageView(context)
        with(imgView) {
            setFailureImage(resources.getDrawable(R.drawable.failure_icon))
            setOptimizeDisplay(true)
            setTapToRetry(true)
            setProgressIndicator(ProgressIndicatorForBIV())
            showImage(Uri.parse(imgList[position]))
        }

        container.addView(imgView)

        return imgView

    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as BigImageView)
    }
}