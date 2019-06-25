package com.adrian.basemodule

import android.widget.Toast

/**
 * author:RanQing
 * date:2019/6/26 0026 2:24
 * description:
 **/
object ToastUtils {

    fun showToastShort(msg: String) {
        Toast.makeText(BaseApplication.instance, msg, Toast.LENGTH_SHORT).show()
    }

    fun showToastLong(msg: String) {
        Toast.makeText(BaseApplication.instance, msg, Toast.LENGTH_LONG).show()
    }
}