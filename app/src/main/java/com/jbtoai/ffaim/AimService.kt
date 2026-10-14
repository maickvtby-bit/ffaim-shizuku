package com.jbtoai.ffaim

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AimService : Service() {

    companion object {
        const val ACTION_RECOIL = "recoil"
        const val ACTION_FIRE_START = "fire_start"
        const val ACTION_FIRE_STOP = "fire_stop"
        const val ACTION_SCOPE = "scope"
        const val ACTION_SNAP = "snap"

        var snapX = 0
        var snapY = 0
        var isFiring = false
    }

    private var screenW = 1080
    private var screenH = 2400
    private var fireThread: Thread? = null

    private val fireBtnX get() = (screenW * 0.86).toInt()
    private val fireBtnY get() = (screenH * 0.78).toInt()
    private val scopeBtnX get() = (screenW * 0.90).toInt()
    private val scopeBtnY get() = (screenH * 0.55).toInt()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(2, buildNotif())
        val (w, h) = ShizukuHelper.getScreenSize()
        screenW = w; screenH = h
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RECOIL -> doRecoil()
            ACTION_FIRE_START -> startFire()
            ACTION_FIRE_STOP -> stopFire()
            ACTION_SCOPE -> doScope()
            ACTION_SNAP -> doSnap()
        }
        return START_STICKY
    }

    private fun doRecoil() {
        val cx = screenW / 2
        val cy = screenH / 2
        ShizukuHelper.exec("input swipe $cx $cy ${cx + 15} ${cy + 130} 120")
    }

    private fun startFire() {
        if (isFiring) return
        isFiring = true
        fireThread = Thread {
            while (isFiring) {
                ShizukuHelper.exec("input tap $fireBtnX $fireBtnY")
                Thread.sleep(60)
            }
        }.also { it.start() }
    }

    private fun stopFire() {
        isFiring = false
        fireThread?.interrupt()
        fireThread = null
    }

    private fun doScope() {
        Thread {
            ShizukuHelper.exec("input tap $scopeBtnX $scopeBtnY")
            Thread.sleep(150)
            ShizukuHelper.exec("input tap $fireBtnX $fireBtnY")
            Thread.sleep(100)
            ShizukuHelper.exec("input tap $scopeBtnX $scopeBtnY")
        }.start()
    }

    private fun doSnap() {
        val cx = screenW / 2
        val cy = screenH / 2
        ShizukuHelper.exec("input swipe $cx $cy $snapX $snapY 200")
    }

    private fun buildNotif(): Notification {
        val ch = "ffaim_aim"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(
                ch, "Aim Inject", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager).createNotificationChannel(c)
        }
        return NotificationCompat.Builder(this, ch)
            .setContentTitle("FF Aim Assist")
            .setContentText("Inject active")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopFire()
        super.onDestroy()
    }
}