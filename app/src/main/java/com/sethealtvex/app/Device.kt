package com.sethealtvex.app

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("sethealtvex", Context.MODE_PRIVATE)
    fun bool(k: String, d: Boolean = false) = sp.getBoolean(k, d)
    fun int(k: String, d: Int = 0) = sp.getInt(k, d)
    fun str(k: String, d: String = "") = sp.getString(k, d) ?: d
    fun put(k: String, v: Boolean) = sp.edit().putBoolean(k, v).apply()
    fun put(k: String, v: Int) = sp.edit().putInt(k, v).apply()
    fun put(k: String, v: String) = sp.edit().putString(k, v).apply()
}

class PBool(private val p: Prefs, private val k: String, d: Boolean) {
    var v by mutableStateOf(p.bool(k, d))
    fun set(x: Boolean) { v = x; p.put(k, x) }
}
class PInt(private val p: Prefs, private val k: String, d: Int) {
    var v by mutableStateOf(p.int(k, d))
    fun set(x: Int) { v = x; p.put(k, x) }
}
class PStr(private val p: Prefs, private val k: String, d: String) {
    var v by mutableStateOf(p.str(k, d))
    fun set(x: String) { v = x; p.put(k, x) }
}

data class DevInfo(
    val model: String, val maker: String, val android: String, val sdk: Int, val abi: String,
    val battery: Int, val charging: Boolean, val batTemp: Float,
    val ramTotal: Long, val ramFree: Long, val stTotal: Long, val stFree: Long,
    val cores: Int, val refresh: Float, val thermal: String, val net: String,
    val screen: String, val time: String
) {
    val ramUsedFrac: Float get() = if (ramTotal > 0) 1f - ramFree.toFloat() / ramTotal else 0f
    val stUsedFrac: Float get() = if (stTotal > 0) 1f - stFree.toFloat() / stTotal else 0f
}

fun gb(b: Long) = String.format(Locale.US, "%.1f GB", b / 1073741824.0)
fun mb(b: Long) = String.format(Locale.US, "%.2f MB", b / 1048576.0)

@Suppress("DEPRECATION")
fun readDevice(ctx: Context): DevInfo {
    val bi = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val lvl = bi?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = bi?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val pct = if (lvl >= 0 && scale > 0) lvl * 100 / scale else -1
    val st = bi?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val charging = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
    val temp = (bi?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f

    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)

    val sf = StatFs(Environment.getDataDirectory().path)
    val stTotal = sf.totalBytes
    val stFree = sf.availableBytes

    var refresh = 60f
    try {
        refresh = (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate
    } catch (e: Exception) { }

    val thermal = if (Build.VERSION.SDK_INT >= 29) {
        try {
            val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
            when (pm.currentThermalStatus) {
                0 -> "Normal"; 1 -> "Ringan"; 2 -> "Sedang"; 3 -> "Berat"; 4 -> "Kritis"
                else -> "Sangat panas"
            }
        } catch (e: Exception) { "Tidak tersedia" }
    } else "Tidak tersedia"

    val net = try {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val base = when {
            caps == null -> "Tidak terhubung"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Seluler"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Lainnya"
        }
        val noInternet = caps != null && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        if (noInternet) "$base (internet belum tervalidasi)" else base
    } catch (e: Exception) { "Tidak diketahui" }

    val dm = ctx.resources.displayMetrics
    return DevInfo(
        model = Build.MODEL, maker = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
        android = Build.VERSION.RELEASE, sdk = Build.VERSION.SDK_INT,
        abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "-",
        battery = pct, charging = charging, batTemp = temp,
        ramTotal = mi.totalMem, ramFree = mi.availMem, stTotal = stTotal, stFree = stFree,
        cores = Runtime.getRuntime().availableProcessors(), refresh = refresh,
        thermal = thermal, net = net,
        screen = "${dm.widthPixels}x${dm.heightPixels} @${dm.densityDpi}dpi",
        time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    )
}

fun dirSize(f: File?): Long {
    if (f == null || !f.exists()) return 0L
    if (f.isFile) return f.length()
    var s = 0L
    f.listFiles()?.forEach { s += dirSize(it) }
    return s
}
fun cacheSize(ctx: Context) = dirSize(ctx.cacheDir) + dirSize(ctx.externalCacheDir)
fun clearCache(ctx: Context): Long {
    val before = cacheSize(ctx)
    try {
        ctx.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        ctx.externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
    } catch (e: Exception) { }
    return (before - cacheSize(ctx)).coerceAtLeast(0L)
}

fun recs(d: DevInfo): List<String> {
    val r = mutableListOf<String>()
    if (d.ramUsedFrac > 0.85f) r += "RAM terpakai tinggi: tutup aplikasi latar belakang sebelum bermain."
    if (d.battery in 0..19 && !d.charging) r += "Baterai rendah: colok charger atau aktifkan penghemat baterai."
    if (d.batTemp >= 40f) r += "Suhu baterai ${d.batTemp}°C: lepas casing dan istirahatkan HP sebentar."
    if (d.thermal !in listOf("Normal", "Tidak tersedia")) r += "Status termal ${d.thermal}: turunkan grafis atau FPS di game."
    if (d.stTotal > 0 && d.stFree.toFloat() / d.stTotal < 0.1f) r += "Penyimpanan hampir penuh (sisa ${gb(d.stFree)}): hapus file yang tidak perlu."
    if (d.refresh < 90f) r += "Layar ${d.refresh.toInt()} Hz: pilih frame rate game sesuai kemampuan layar."
    if (r.isEmpty()) r += "Kondisi perangkat baik. Tidak ada tindakan khusus yang diperlukan."
    return r
}

fun quickOptimize(ctx: Context, clear: Boolean): String {
    val freed = if (clear) clearCache(ctx) else 0L
    val d = readDevice(ctx)
    return buildString {
        if (clear) appendLine("• Cache SETHEALTVEX dibersihkan: ${mb(freed)}")
        appendLine("• RAM terpakai: ${(d.ramUsedFrac * 100).toInt()}% (${gb(d.ramFree)} bebas)")
        appendLine("• Baterai: ${d.battery}% • ${d.batTemp}°C • Termal: ${d.thermal}")
        appendLine("• Rekomendasi:")
        recs(d).forEach { appendLine("   - $it") }
        append("Catatan: Android tidak mengizinkan aplikasi biasa membersihkan RAM aplikasi lain.")
    }
}

fun toast(ctx: Context, m: String) = Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show()

fun openSettings(ctx: Context, action: String) {
    try {
        ctx.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        try {
            ctx.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e2: Exception) { toast(ctx, "Pengaturan tidak tersedia di perangkat ini") }
    }
}

fun copyText(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("SETHEALTVEX", text))
    toast(ctx, "Disalin ke clipboard")
}
