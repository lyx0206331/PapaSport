package com.adrian.papasport.application

import android.app.Application
import android.content.MutableContextWrapper
import android.webkit.WebView

/**
 * author:RanQing
 * date:2019/6/18 0018 0:36
 * description:
 **/
class MyApplication: Application() {


    override fun onCreate() {
        super.onCreate()

        val webView = WebView(MutableContextWrapper(this))
    }

}