package com.adrian.basemodule

import android.app.Application

/**
 * author:RanQing
 * date:2019/6/26 0026 2:28
 * description:
 **/
open class BaseApplication : Application() {

    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}