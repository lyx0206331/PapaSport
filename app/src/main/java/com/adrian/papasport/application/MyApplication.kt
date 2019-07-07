package com.adrian.papasport.application

import android.content.Context
import android.content.MutableContextWrapper
import android.support.multidex.MultiDex
import android.webkit.WebView
import com.adrian.basemodule.BaseApplication

/**
 * author:RanQing
 * date:2019/6/18 0018 0:36
 * description:
 **/
class MyApplication : BaseApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        val webView = WebView(MutableContextWrapper(this))
    }

}