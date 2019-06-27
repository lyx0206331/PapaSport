package com.adrian.rfidmodule

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android_serialport_api.sample.SerialPortPreferences
import com.by100.util.AppConfig
import kotlinx.android.synthetic.main.rfidmodule_activity_main.*
import java.io.FileInputStream

class MainActivity : Activity() {

    private lateinit var rfidUtils: RFIDUtils

    private var isOpen = false
    private var isPlay: Boolean = false
    private val player by lazy { MediaPlayer.create(this, R.raw.success) }
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rfidmodule_activity_main)

        rfidUtils = RFIDUtils(this, object : RFIDUtils.IRfidListener {
            override fun onReadSuccess(txtInfo: String?, state: Int) {
                textView1.text = txtInfo

                if (state == 1) {
                    val fis = FileInputStream(
                        Environment.getExternalStorageDirectory().toString() + "/wltlib/zp.bmp"
                    )
                    val bmp = BitmapFactory.decodeStream(fis)
                    fis.close()
                    imageView1.setImageBitmap(bmp)
                } else {
                    textView1.append("照片解码失败，请检查路径" + AppConfig.RootFile)
                    imageView1.setImageBitmap(
                        BitmapFactory.decodeResource(
                            resources, R.mipmap.face
                        )
                    )
                }
                if (isPlay)
                    player.start()
            }

            override fun onError(state: Int) {
                imageView1.setImageBitmap(
                    BitmapFactory.decodeResource(
                        resources, R.mipmap.face
                    )
                )
//                textView1.text = when(state) {
//                    -2 -> "连接异常"
//                    -3 -> "无卡或卡片已读过"
//                    -4 -> "无卡或卡片已读过"
//                    -5 -> "读卡失败"
//                    -99 -> "操作异常"
//                    -1000 -> "读取数据异常！"
//                    else -> "未知错误"
//                }
                if (state == -2) {
                    textView1.text = "连接异常"
                }
                if (state == -3) {
                    textView1.text = "无卡或卡片已读过"
                }
                if (state == -4) {
                    textView1.text = "无卡或卡片已读过"
                }
                if (state == -5) {
                    textView1.text = "读卡失败"
                }
                if (state == -99) {
                    textView1.text = "操作异常"
                }
                if (state == -1000) {
                    textView1.text = "读取数据异常！"
                }
            }
        })

        btconn.setOnClickListener {
            startActivity(Intent(this, SerialPortPreferences::class.java))
        }

        btread.setOnClickListener {
            isOpen = !isOpen
            rfidUtils.isOpen = isOpen
            btread.text = if (isOpen) "停止" else "读卡"
        }
    }

    override fun onResume() {
        super.onResume()
        isPlay = prefs.getBoolean("checkbox", true)

        rfidUtils.resume()
    }

    override fun onDestroy() {
        rfidUtils.release()
        super.onDestroy()
    }
}
