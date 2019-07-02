package com.adrian.papasport

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.posapi.PosApi
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import com.adrian.basemodule.LogUtils.logE
import com.adrian.basemodule.ToastUtils.showToastShort
import com.adrian.basemodule.orFalse
import com.adrian.nfcmodule.NFCUtils
import com.adrian.papasport.model.NFCTagInfo
import com.adrian.papasport.view.SmartRefreshWebLayout
import com.adrian.printmodule.PrintUtils
import com.adrian.rfidmodule.IDCardInfo
import com.adrian.rfidmodule.RFIDUtils
import com.just.agentweb.*
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import kotlinx.android.synthetic.main.activity_base_web.*
import org.json.JSONObject
import java.util.*

class MainActivity : BaseWebActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null
    private var ndefPushMessage: NdefMessage? = null
    private lateinit var nfcDialog: AlertDialog

    private lateinit var nfcUtils: NFCUtils
    private var curUrl: String? = null
    private var pageTag: String = "memberSearch"

    private lateinit var rfidUtils: RFIDUtils
    private val player by lazy { MediaPlayer.create(this, R.raw.success) }

    private lateinit var scanPrintUtils: PrintUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = ""
        toolbar.setBackgroundColor(Color.WHITE)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.mipmap.back)

//        toolbar.visibility = View.GONE

        initNFC()
        initRFID()
        initScanPrint()
    }

    /**
     * 初始化扫描打印
     */
    private fun initScanPrint() {
        scanPrintUtils = PrintUtils(this, object : PrintUtils.IPrintListener {
            override fun onFinish() {
                showToastShort("打印完成")
            }

            override fun onScanSuccess(msg: String?) {
                val ticketNum = NFCTagInfo(msg.orEmpty(), "").toJsonString()
                logE(TAG, ticketNum)
                agentWeb.jsAccessEntrace.quickCallJs(
                    "andriodGetCode", ticketNum
                )
                player.start()
            }

            override fun onFailed(state: Int) {
                showToastShort(
                    when (state) {
                        PosApi.ERR_POS_PRINT_NO_PAPER -> "打印缺纸"
                        PosApi.ERR_POS_PRINT_FAILED -> "打印失败"
                        PosApi.ERR_POS_PRINT_VOLTAGE_LOW -> "电压过低"
                        PosApi.ERR_POS_PRINT_VOLTAGE_HIGH -> "电压过高"
                        else -> "未知错误"
                    }
                )
            }

            override fun onPrinterSetting(state: Int) {
                showToastShort(
                    when (state) {
                        0 -> "持续有纸"
                        1 -> "缺纸"
                        2 -> "检测到黑标"
                        else -> "未知设置错误"
                    }
                )
            }

            override fun onGetState(p0: Int) {}
        })
    }

    /**
     * 初始化rfid
     */
    private fun initRFID() {
        rfidUtils = RFIDUtils(this, object : RFIDUtils.IRfidListener {
            override fun onReadSuccess(idCardInfo: IDCardInfo?, state: Int) {
                val jsonStr = idCardInfo?.toJsonString().orEmpty()
                logE(TAG, jsonStr)
                agentWeb.jsAccessEntrace.quickCallJs(
                    "andriodCallH5",
                    jsonStr
                )
                player.start()
            }

            override fun onError(state: Int) {
                showToastShort(
                    when (state) {
                        -2 -> "连接异常"
                        -3 -> "无卡或卡片已读过"
                        -4 -> "无卡或卡片已读过"
                        -5 -> "读卡失败"
                        -99 -> "操作异常"
                        -1000 -> "读取数据异常！"
                        else -> "未知错误"
                    }
                )
            }
        })

        rfidUtils.isOpen = true
    }

    /**
     * NFC初始化
     */
    private fun initNFC() {
        nfcUtils = NFCUtils(this, object : NFCUtils.INFCListener {

            override fun showNfcData(msgs: Array<NdefMessage>) {
            }

            override fun getIds(decTagId: Long, reversedId: Long) {
                val jsonStr = NFCTagInfo("$decTagId", "$reversedId").toJsonString()
                logE(TAG, jsonStr)
                agentWeb.jsAccessEntrace.quickCallJs(
                    "andriodCallH5",
                    jsonStr
                )
            }
        })

        nfcUtils.resolveIntent(intent)

        nfcDialog = AlertDialog.Builder(this).setNeutralButton("Ok", null)
            .create()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        //拦截系统级的NFC扫描，例如扫描蓝牙
        mPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(
                this,
                javaClass
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        ndefPushMessage = NdefMessage(
            arrayOf(
                nfcUtils.newTextRecord(
                    "",
                    Locale.ENGLISH, true
                )
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter == null) {
            if (!nfcAdapter?.isEnabled.orFalse()) {
                showWirelessSettingsDialog()
            }

            showMessage("error", " NO NFC")

            showToastShort("设备不支持NFC！")

            return
        }
        if (!nfcAdapter?.isEnabled.orFalse()) {
            showToastShort("请在系统设置中先启用NFC功能！")
            return
        }

        if (isTargetPage()) {
            bootNFC()
            rfidUtils.resume()
        }

        // 必须延迟一秒，否则将会出现第一次扫描和打印延迟的现象
        Handler().postDelayed({
            // 打开GPIO，给扫描头上电
            scanPrintUtils.openDevice()

        }, 1000)
    }

    /**
     * 启动NFC
     */
    private fun bootNFC() {
        nfcAdapter?.let {
            //隐式启动
            it.enableForegroundDispatch(this, mPendingIntent, null, null)
            it.enableForegroundNdefPush(this, ndefPushMessage)
        }
    }

    override fun onPause() {
        super.onPause()
        closeNFC()
        rfidUtils.closeRfidRead()
    }

    /**
     * 关闭NFC
     */
    private fun closeNFC() {
        nfcAdapter?.let {
            it.disableForegroundDispatch(this)
            it.disableForegroundNdefPush(this)
        }
    }

    override fun onDestroy() {
        rfidUtils.release()
        scanPrintUtils.release()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        nfcUtils.resolveIntent(intent)
    }

    private fun showMessage(title: String, message: String) {
        nfcDialog.setTitle(title)
        nfcDialog.setMessage(message)
        nfcDialog.show()
    }

    private fun showWirelessSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("nfc_disabled")
        builder.setPositiveButton(
            android.R.string.ok
        ) { dialogInterface, i ->
            val intent = Intent(
                Settings.ACTION_WIRELESS_SETTINGS
            )
            startActivity(intent)
        }
        builder.setNegativeButton(android.R.string.cancel,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    finish()
                }
            })
        builder.create().show()
    }

    override fun addBGChild(frameLayout: FrameLayout) {
        frameLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_base_web
    }

    override fun getAndroidInterface(): AndroidInterface {
        return AndroidInterface(this, agentWeb, object : AndroidInterface.IJsListener {

            override fun printMsg(msg: String) {
//                showToastShort(msg)
//                val content = "asdfasdf\nasdfasdfa\n12342134\n-------------------\n12343    132414  asdf\n" +
//                        "================="
                val jsonObj = JSONObject(msg)
                val type = jsonObj.optInt("type")
                val content = jsonObj.optString("content")
                when (type) {
                    0 -> showToastShort(content)
                    //门票
                    1 -> ""
                    //支付凭证
                    2 -> ""
                }
                scanPrintUtils.printText(content)
            }

            override fun startScan() {
                scanPrintUtils.scanDomn()
            }

            override fun turnOnNFC() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun turnOnRFID() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    override fun getAgentWebSettings(): AbsAgentWebSettings {
        return AgentWebSettingsImpl.getInstance()
    }

    override fun getMiddleWareWebClient(): MiddlewareWebClientBase {
        return object : MiddlewareWebClientBase() {}
    }

    override fun getMiddleWareWebChrome(): MiddlewareWebChromeBase {
        return object : MiddlewareWebChromeBase() {}
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
                logE(TAG, "shouldOverrideUrlLoading")
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                logE(TAG, "onPageStarted. url: $url")
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                logE(TAG, "shouldOverrideUrlLoading. url: $url")
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                logE(TAG, "onPageFinished. url: $url")
                curUrl = url
                if (isTargetPage()) {
                    bootNFC()
                    rfidUtils.resume()
                } else {
                    closeNFC()
                    rfidUtils.closeRfidRead()
                }
                super.onPageFinished(view, url)
            }
        }
    }

    private fun isTargetPage(): Boolean {
        return curUrl?.endsWith(pageTag).orFalse()
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
//        return "http://192.168.1.12:8039"
    }

}
