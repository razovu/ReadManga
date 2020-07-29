package com.sale.readmanga.tools

import android.view.LayoutInflater
import android.view.View
import com.github.piasy.biv.indicator.ProgressIndicator
import com.github.piasy.biv.view.BigImageView
import com.sale.readmanga.R


class ProgressIndicatorForBIV: ProgressIndicator {

    override fun onFinish() {}

    override fun getView(parent: BigImageView?): View {
        return LayoutInflater.from(parent?.context).inflate(R.layout.item_progress_bar, parent,false)
    }

    override fun onProgress(progress: Int) { }

    override fun onStart() { }
}