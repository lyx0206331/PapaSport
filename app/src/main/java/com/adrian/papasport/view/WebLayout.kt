package com.adrian.papasport.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import com.adrian.papasport.R
import com.just.agentweb.IWebLayout
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout

/**
 * TODO: document your custom view class.
 */
class WebLayout(activity: Activity) : IWebLayout<WebView, ViewGroup> {

    private var twinklingRefreshLayout: TwinklingRefreshLayout =
        LayoutInflater.from(activity).inflate(R.layout.web_layout, null) as TwinklingRefreshLayout
    private var webView: WebView

    init {
        twinklingRefreshLayout.setPureScrollModeOn()
        webView = twinklingRefreshLayout.findViewById(R.id.webView) as WebView
    }

    override fun getLayout(): ViewGroup {
        return twinklingRefreshLayout
    }

    override fun getWebView(): WebView? {
        return webView
    }


}
