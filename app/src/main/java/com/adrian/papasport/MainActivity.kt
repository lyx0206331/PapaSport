package com.adrian.papasport

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import com.adrian.basemodule.LogUtils
import com.adrian.papasport.view.SmartRefreshWebLayout
import com.just.agentweb.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import kotlinx.android.synthetic.main.activity_base_web.*

class MainActivity : BaseWebActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener { alertDialog.show() }

//        toolbar.visibility = View.GONE
    }

    override fun addBGChild(frameLayout: FrameLayout) {
        frameLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_base_web
    }

    override fun getAgentWebSettings(): AbsAgentWebSettings {
        return AgentWebSettingsImpl.getInstance()
    }

    override fun getMiddleWareWebClient(): MiddlewareWebClientBase {
        return object : MiddlewareWebClientBase(){}
    }

    override fun getMiddleWareWebChrome(): MiddlewareWebChromeBase {
        return object : MiddlewareWebChromeBase(){}
    }

    override fun getErrorLayoutEntity(): ErrorLayoutEntity {
        return ErrorLayoutEntity()
    }

    override fun getWebChromeClient(): WebChromeClient? {
        return object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                tvTitle?.text = title
            }
        }
    }

    override fun getWebViewClient(): WebViewClient? {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                LogUtils.logE(TAG, "shouldOverrideUrlLoading")
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                LogUtils.logE(TAG, "onPageStarted. url: $url")
            }
        }
    }

    override fun getWebView(): WebView? {
        val wv = WebView(this)
        wv.settings.javaScriptEnabled = true
        wv.settings.useWideViewPort = true
        wv.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        wv.settings.loadWithOverviewMode = true
        wv.settings.setSupportZoom(true)
        return wv
//        return null
    }

    override fun getWebLayout(): IWebLayout<*, *>? {
        val smartRefreshWebLayout = SmartRefreshWebLayout(this)
        val smartRefreshLayout = smartRefreshWebLayout.layout as SmartRefreshLayout
        smartRefreshLayout.setOnRefreshListener {
            agentWeb.urlLoader.reload()
            smartRefreshLayout.postDelayed({
                smartRefreshLayout.finishRefresh()
            }, 2000)
        }
        return smartRefreshWebLayout
//        return null
    }

    override fun getPermissionInterceptor(): PermissionInterceptor? {
        return null
    }

    override fun getAgentWebUIController(): AgentWebUIControllerImplBase? {
        return null
    }

    override fun getOpenOtherAppWay(): DefaultWebClient.OpenOtherPageWays? {
        return DefaultWebClient.OpenOtherPageWays.ASK
    }

    override fun getAgentWebParent(): ViewGroup {
        return container
    }

    override fun getUrl(): String {
        //https://m.jd.com/
        return "http://pda.test.papasports.com.cn"
    }

}
