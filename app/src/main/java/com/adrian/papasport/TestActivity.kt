package com.adrian.papasport

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.posapi.PosApi
import android.provider.Settings
import com.adrian.basemodule.BaseActivity
import com.adrian.basemodule.LogUtils
import com.adrian.basemodule.ToastUtils.showToastShort
import com.adrian.basemodule.orFalse
import com.adrian.nfcmodule.NFCUtils
import com.adrian.papasport.model.NFCTagInfo
import com.adrian.printmodule.PrintUtils
import com.adrian.rfidmodule.IDCardInfo
import com.adrian.rfidmodule.RFIDUtils
import kotlinx.android.synthetic.main.activity_test.*
import java.util.*

class TestActivity : BaseActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null
    private var ndefPushMessage: NdefMessage? = null
    private lateinit var nfcDialog: AlertDialog

    private lateinit var nfcUtils: NFCUtils

    private lateinit var rfidUtils: RFIDUtils
    private val player by lazy { MediaPlayer.create(this, R.raw.success) }

    private lateinit var scanPrintUtils: PrintUtils

    override fun getLayoutResId(): Int {
        return R.layout.activity_test
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnPrintText.setOnClickListener { scanPrintUtils.printText("asdfasdfasdf") }
        btnPrintQr.setOnClickListener { scanPrintUtils.printQRCode("assdasdffd") }
        btnPrintBar.setOnClickListener { scanPrintUtils.printBarCode("asdfasdffasd") }
        btnScan.setOnClickListener { scanPrintUtils.ScanDomn() }

        initNFC()
//        initRFID()
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
                LogUtils.logE(BaseWebActivity.TAG, ticketNum)
                scanPrintUtils.printText(ticketNum)
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
                LogUtils.logE(BaseWebActivity.TAG, jsonStr)
                showToastShort(jsonStr)
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
                LogUtils.logE(BaseWebActivity.TAG, jsonStr)
                showToastShort(jsonStr)
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
        bootNFC()
//        rfidUtils.resume()

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
//        rfidUtils.closeRfidRead()
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
//        rfidUtils.release()
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
}
