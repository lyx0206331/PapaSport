package com.adrian.papasport

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
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

    @JavascriptInterface
    fun scanQRCode() {
        deliver.post {
            jsListener.startScan()
        }
    }

    @JavascriptInterface
    fun printJsContent(content: String) {
        deliver.post {
            jsListener.printMsg(content)
        }
    }

    interface IJsListener {
        fun printMsg(msg: String)
        fun startScan()
    }
}