package com.adsbynimbus.android.sample.lockscreen

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.render.AdController

class LockScreenActivity : AppCompatActivity(R.layout.layout_lockscreen) {

    var controller: AdController? = null

    val keyguardManager by lazy { getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(FLAG_KEEP_SCREEN_ON)
       /* setContentView(FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            adManager.showAd(NimbusRequest.forBannerAd("lock_screen", Format.MREC, Position.HEADER), 30, this) {
                controller = it
            }
        }) */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setShowWhenLocked(true)
            keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {})
        }

        ContextCompat.startForegroundService(this, Intent(this, LockScreenService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.destroy()
    }
}
