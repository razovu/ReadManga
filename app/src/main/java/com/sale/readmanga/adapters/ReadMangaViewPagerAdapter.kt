package com.sale.readmanga.adapters

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.view.BigImageView
import com.sale.readmanga.R

class ReadMangaViewPagerAdapter(
    private val context: Context,
    private val imgList: MutableList<String>
    ) : PagerAdapter() {

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

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        synchronized(ReadMangaViewPagerAdapter::class) {

            val imgView = BigImageView(context)
            with(imgView) {
                setFailureImage(resources.getDrawable(R.drawable.squidward))
                setOptimizeDisplay(true)
                setTapToRetry(true)
                showImage(Uri.parse(imgList[position]))
            }

            container.addView(imgView)

            return imgView
        }
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as BigImageView)
    }
}