package com.adrian.papasport

import android.app.AlertDialog
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import com.adrian.basemodule.BaseActivity
import com.just.agentweb.*

/**
 * author:RanQing
 * date:2019/6/22 0022 20:47
 * description:网页加载界面
 **/
abstract class BaseWebActivity : BaseActivity() {

    companion object {
        const val TAG = "BaseWebActivity"
    }

    protected lateinit var agentWeb: AgentWeb


    protected val alertDialog by lazy {
        AlertDialog.Builder(this).setMessage(R.string.close_tips)
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog?.dismiss() }
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    dialog?.dismiss()
                    finish()
                }.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildAgentWeb()
    }

    private fun buildAgentWeb() {
        val errorLayoutEntity = getErrorLayoutEntity()
        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(getAgentWebParent(), ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            .useDefaultIndicator(getIndicatorColor(), getIndicatorHeight())
            .setWebChromeClient(getWebChromeClient())
            .setWebViewClient(getWebViewClient())
            .setWebView(getWebView())
            .setPermissionInterceptor(getPermissionInterceptor())
            .setWebLayout(getWebLayout())
            .setAgentWebUIController(getAgentWebUIController())
            .interceptUnkownUrl()
            .setOpenOtherPageWays(getOpenOtherAppWay())
            .useMiddlewareWebChrome(getMiddleWareWebChrome())
            .useMiddlewareWebClient(getMiddleWareWebClient())
            .setAgentWebWebSettings(getAgentWebSettings())
            .setMainFrameErrorView(errorLayoutEntity.layoutRes, errorLayoutEntity.reloadId)
            .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
            .createAgentWeb()
            .ready()
            .go(getUrl())

        // 得到 AgentWeb 最底层的控件
        addBGChild(agentWeb.webCreator.webParentLayout)

        agentWeb.jsInterfaceHolder.addJavaObject("android", getAndroidInterface())
    }

    abstract fun getAndroidInterface(): AndroidInterface

    protected abstract fun addBGChild(frameLayout: FrameLayout)

    abstract fun getUrl(): String

    override fun onBackPressed() {
        if (!agentWeb.back()) {
            alertDialog.show()
        }
    }

    override fun onResume() {
        agentWeb.webLifeCycle.onResume()
        super.onResume()
    }

    override fun onPause() {
        agentWeb.webLifeCycle.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        agentWeb.webLifeCycle.onDestroy()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (agentWeb.handleKeyEvent(keyCode, event)) true else super.onKeyDown(keyCode, event)
    }

    protected abstract fun getMiddleWareWebClient(): MiddlewareWebClientBase

    protected abstract fun getMiddleWareWebChrome(): MiddlewareWebChromeBase

    protected abstract fun getErrorLayoutEntity(): ErrorLayoutEntity

    protected abstract fun getAgentWebSettings(): AbsAgentWebSettings

    protected abstract fun getAgentWebParent(): ViewGroup

    protected abstract fun getWebChromeClient(): WebChromeClient?

    @ColorInt
    protected fun getIndicatorColor(): Int {
        return -1
    }

    protected fun getIndicatorHeight(): Int {
        return -1
    }

    protected abstract fun getWebViewClient(): WebViewClient?

    protected abstract fun getWebView(): WebView?

    protected abstract fun getWebLayout(): IWebLayout<*, *>?

    protected abstract fun getPermissionInterceptor(): PermissionInterceptor?

    protected abstract fun getAgentWebUIController(): AgentWebUIControllerImplBase?

    protected abstract fun getOpenOtherAppWay(): DefaultWebClient.OpenOtherPageWays?

    inner class ErrorLayoutEntity {
        var layoutRes = com.just.agentweb.R.layout.agentweb_error_page
            set(value) {
                field = if (value <= 0) -1 else value
            }
        var reloadId: Int = 0
            set(value) {
                field = if (value <= 0) -1 else value
            }

    }
}
