package com.adrian.papasport

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import com.adrian.basemodule.PhoneUtils
import com.adrian.papasport.model.DeviceInfo
import com.alibaba.fastjson.JSON
import com.just.agentweb.AgentWeb

/**
 * author:RanQing
 * date:2019/6/29 0029 4:07
 * description: 提供js调用android数据接口
 **/
class AndroidInterface(val context: Context, val agentWeb: AgentWeb, val jsListener: IJsListener) {

    private val deliver = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun callAndroid(msg: String) {


        deliver.post {
            Log.i("Info", "main Thread:" + Thread.currentThread())
//            Toast.makeText(context.applicationContext, "" + msg, Toast.LENGTH_LONG).show()
            jsListener.printMsg(msg)
        }


        Log.i("Info", "Thread:" + Thread.currentThread())

    }

    @JavascriptInterface
    fun calledByJs(msg: String) {
        deliver.post {
            //            ToastUtils.showToastShort(msg)
            jsListener.printMsg(msg)
        }
    }

    /**
     * 扫描二维码
     */
    @JavascriptInterface
    fun scanQRCode() {
        deliver.post {
            jsListener.startScan()
        }
    }

    /**
     * 打开NFC.识别会员卡
     */
    @JavascriptInterface
    fun turnOnNFC() {
        deliver.post {
            jsListener.turnOnNFC()
        }
    }

    /**
     * 打开RFID.识别身份证
     */
    @JavascriptInterface
    fun turnOnRFID() {
        deliver.post {
            jsListener.turnOnRFID()
        }
    }

    /**
     * 打印小票.内容来自web
     */
    @JavascriptInterface
    fun printJsContent(content: String) {
        deliver.post {
            jsListener.printMsg(content)
        }
    }

    /**
     * 获取IMEI号
     */
    @JavascriptInterface
    fun getImei(): String {
        return JSON.toJSONString(DeviceInfo(PhoneUtils.getImeiNum()))
    }

    interface IJsListener {
        fun printMsg(msg: String)
        fun startScan()
        fun turnOnNFC()
        fun turnOnRFID()
    }
}