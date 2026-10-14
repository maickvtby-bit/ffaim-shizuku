package com.jbtoai.ffaim

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txtShizuku = findViewById<TextView>(R.id.txtShizuku)
        val btnShizuku = findViewById<Button>(R.id.btnShizuku)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        refresh(txtShizuku)

        btnShizuku.setOnClickListener {
            if (!ShizukuHelper.isRunning()) {
                toast("Shizuku chưa chạy. Mở app Shizuku trước.")
                return@setOnClickListener
            }
            if (ShizukuHelper.hasPermission()) {
                toast("Đã có quyền Shizuku")
            } else {
                ShizukuHelper.requestPermission()
                toast("Bấm Allow trong hộp thoại")
            }
        }

        btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)) {
                startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
            } else toast("Đã có quyền overlay")
        }

        btnStart.setOnClickListener {
            if (!ShizukuHelper.isRunning()) {
                toast("Shizuku chưa chạy"); return@setOnClickListener
            }
            if (!ShizukuHelper.hasPermission()) {
                toast("Chưa có quyền Shizuku"); return@setOnClickListener
            }
            if (!Settings.canDrawOverlays(this)) {
                toast("Chưa có quyền overlay"); return@setOnClickListener
            }

            startForegroundService(Intent(this, AimService::class.java))
            startForegroundService(Intent(this, OverlayService::class.java))
            toast("Đã bật. Mở FF để dùng nút.")
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, AimService::class.java))
            stopService(Intent(this, OverlayService::class.java))
            toast("Đã tắt")
        }
    }

    override fun onResume() {
        super.onResume()
        refresh(findViewById(R.id.txtShizuku))
    }

    private fun refresh(tv: TextView) {
        val running = ShizukuHelper.isRunning()
        val perm = ShizukuHelper.hasPermission()
        val overlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)
        tv.text = "Shizuku: ${if (running) "chạy ✓" else "chưa chạy ✗"}\n" +
                  "Quyền Shizuku: ${if (perm) "đã cấp ✓" else "chưa cấp ✗"}\n" +
                  "Quyền overlay: ${if (overlay) "đã cấp ✓" else "chưa cấp ✗"}"
    }

    private fun toast(s: String) =
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}