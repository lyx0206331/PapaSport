package com.adrian.papasport.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import com.adrian.papasport.R
import com.just.agentweb.IWebLayout
import com.scwang.smartrefresh.layout.SmartRefreshLayout

/**
 * TODO: document your custom view class.
 */
class SmartRefreshWebLayout(activity: Activity) : IWebLayout<WebView, ViewGroup> {

    private var smartRefreshLayout: SmartRefreshLayout
    private var webView: WebView

    init {
        val v = LayoutInflater.from(activity).inflate(R.layout.smart_refresh_web_layout, null)
        smartRefreshLayout = v.findViewById(R.id.smartLayout) as SmartRefreshLayout
        webView = smartRefreshLayout.findViewById(R.id.webView) as WebView
    }

    override fun getLayout(): ViewGroup {
        return smartRefreshLayout
    }

    override fun getWebView(): WebView? {
        return webView
    }


}
