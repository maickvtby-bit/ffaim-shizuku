package com.jbtoai.ffaim

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private val views = mutableListOf<View>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(1, buildNotif())
        buildCrosshair()
        buildButtons()
    }

    private fun buildCrosshair() {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val view = CrosshairView(this)
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            y = -180
        }
        wm.addView(view, lp)
        views.add(view)
    }

    private fun buildButtons() {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        addBtn("RECOIL", 0, 100, type) {
            send(AimService.ACTION_RECOIL)
        }
        addBtn("FIRE", 0, 240, type) {
            if (AimService.isFiring) {
                send(AimService.ACTION_FIRE_STOP)
            } else {
                send(AimService.ACTION_FIRE_START)
            }
        }
        addBtn("SCOPE", 0, 380, type) {
            send(AimService.ACTION_SCOPE)
        }
        addBtn("SNAP", 0, 520, type) {
            AimService.snapX = 540
            AimService.snapY = 1200
            send(AimService.ACTION_SNAP)
        }
        addBtn("X", 0, 660, type) {
            stopSelf()
            stopService(Intent(this, AimService::class.java))
        }
    }

    private fun addBtn(
        label: String, x: Int, y: Int, type: Int, onClick: () -> Unit
    ) {
        val btn = TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 13f
            setBackgroundColor(Color.argb(190, 20, 60, 120))
            setPadding(35, 28, 35, 28)
            setOnClickListener { onClick() }
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            this.x = x + 20
            this.y = y + 100
        }
        wm.addView(btn, lp)
        views.add(btn)
    }

    private fun send(action: String) {
        startService(Intent(this, AimService::class.java).apply {
            this.action = action
        })
    }

    private fun buildNotif(): Notification {
        val ch = "ffaim_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(
                ch, "Overlay", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager).createNotificationChannel(c)
        }
        return NotificationCompat.Builder(this, ch)
            .setContentTitle("FF Aim Assist")
            .setContentText("Overlay active")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        views.forEach { runCatching { wm.removeView(it) } }
        views.clear()
        super.onDestroy()
    }
}

class CrosshairView(ctx: Context) : View(ctx) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(220, 255, 40, 40)
    }
    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = 30f
        canvas.drawCircle(cx, cy, r, p)
        canvas.drawLine(cx - r, cy, cx - r * 0.3f, cy, p)
        canvas.drawLine(cx + r * 0.3f, cy, cx + r, cy, p)
        canvas.drawLine(cx, cy - r, cx, cy - r * 0.3f, p)
        canvas.drawLine(cx, cy + r * 0.3f, cx, cy + r, p)
    }
    override fun onMeasure(w: Int, h: Int) {
        setMeasuredDimension(120, 120)
    }
}