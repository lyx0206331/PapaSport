package com.adrian.papasport

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.adrian.basemodule.BaseActivity
import com.adrian.basemodule.ToastUtils
import com.adrian.rfidmodule.IDCardInfo
import com.adrian.rfidmodule.RFIDUtils
import kotlinx.android.synthetic.main.activity_rfid.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.textResource

class RFIDActivity : BaseActivity() {

    private var rfidUtils: RFIDUtils? = null
    private val player by lazy { MediaPlayer.create(this, R.raw.success) }

    override fun getLayoutResId(): Int {
        return R.layout.activity_rfid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.title_bg_color)
        }
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.backgroundColorResource = R.color.title_bg_color
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.mipmap.back)
        tvTitle.textResource = R.string.id_identification

        initRFID()
    }

    /**
     * 初始化rfid
     */
    private fun initRFID() {
        rfidUtils = RFIDUtils(this, object : RFIDUtils.IRfidListener {
            override fun onReadSuccess(idCardInfo: IDCardInfo?, state: Int) {
                rfidUtils?.release()
                player.start()
                val jsonStr = idCardInfo?.toJsonString().orEmpty()
                val intent = Intent()
                intent.putExtra("rfid_data", jsonStr)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            override fun onError(state: Int) {
                ToastUtils.showToastShort(
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

        rfidUtils?.isOpen = true
    }

    override fun onResume() {
        super.onResume()
        rfidUtils?.resume()
    }

    override fun onPause() {
        super.onPause()
//        rfidUtils?.closeRfidRead()
    }

    override fun onDestroy() {
        super.onDestroy()
//        rfidUtils?.release()
    }
}
