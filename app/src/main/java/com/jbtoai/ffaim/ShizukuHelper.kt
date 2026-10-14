package com.jbtoai.ffaim

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuHelper {

    const val PERMISSION_CODE = 1001

    fun isRunning(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: Throwable) { false }

    fun hasPermission(): Boolean = try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) { false }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(PERMISSION_CODE)
        } catch (_: Throwable) {}
    }

    fun exec(cmd: String): String {
        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", cmd), null, null
            )
            val out = process.inputStream.bufferedReader().readText()
            process.waitFor()
            out.trim()
        } catch (e: Throwable) {
            "ERR: ${e.message}"
        }
    }

    fun getScreenSize(): Pair<Int, Int> {
        val out = exec("wm size")
        val match = Regex("(\\d+)x(\\d+)").find(out)
        return if (match != null) {
            val (w, h) = match.destructured
            Pair(w.toInt(), h.toInt())
        } else Pair(1080, 2400)
    }
}