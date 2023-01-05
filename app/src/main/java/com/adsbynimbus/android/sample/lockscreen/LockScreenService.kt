package com.adsbynimbus.android.sample.lockscreen

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import com.adsbynimbus.Nimbus
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.request.NimbusRequest

class LockScreenAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    var controller: AdController? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adManager.showAd(NimbusRequest.forBannerAd("lock_screen", Format.MREC, Position.HEADER), 30, this) {
            controller = it
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        controller?.destroy()
    }
}

class LockScreenService : Service() {

    inner class LockScreenBinder(val service: Service = this) : Binder()

    val binder = LockScreenBinder()

    inline val channelId get() = Nimbus.sdkName

    inline val lockScreenIntent get () = Intent(this, LockScreenActivity::class.java)

    val notification @SuppressLint("RemoteViewLayout") get() = NotificationCompat.Builder(this, channelId)
        .setAutoCancel(false)
        .setCustomBigContentView(RemoteViews(packageName, R.layout.layout_lockscreen))
        .setContentIntent(PendingIntent.getActivity(this, 125, lockScreenIntent, FLAG_MUTABLE))
        .setPriority(IMPORTANCE_HIGH)
        .build()

    inline val notificationChannel get() = NotificationChannelCompat.Builder(channelId, IMPORTANCE_DEFAULT)
        .setName("LockScreen Ads")
        .build()

    override fun onCreate() {
        super.onCreate()
        NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
        startForeground(2999, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder = binder
}

