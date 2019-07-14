package com.adrian.papasport

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import com.adrian.basemodule.BaseActivity

class WelcomeActivity : BaseActivity() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_welcome
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        Handler().postDelayed({
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}
