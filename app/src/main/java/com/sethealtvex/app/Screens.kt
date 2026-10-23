package com.sethealtvex.app

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

val ffPresets = mapOf(
    "Hemat" to listOf("Graphics: Smooth", "Frame rate: Medium", "Shadow: OFF", "Brightness: 60%", "Cocok untuk HP RAM ≤ 3 GB atau baterai rendah"),
    "Seimbang" to listOf("Graphics: Smooth", "Frame rate: High", "Shadow: OFF", "Brightness: 70%", "Cocok untuk HP RAM 4–6 GB"),
    "Performa" to listOf("Graphics: Standard/Smooth", "Frame rate: Max (jika tersedia)", "Shadow: OFF", "Brightness: 80%", "Cocok untuk HP RAM ≥ 6 GB; perhatikan suhu")
)

@Composable
fun Detail(name: String, ctx: Context, prefs: Prefs, d: DevInfo, refresh: () -> Unit, quickClear: Boolean) {
    when (name) {
        "Smart Sensi" -> SmartSensi(prefs)
        "Headtrick" -> SCard {
            H("Headtrick — Panduan Latihan")
            P("Ini panduan latihan kontrol bidikan, bukan alat otomatis.\n\n1. Arahkan crosshair setinggi kepala sebelum musuh muncul (pre-aim).\n2. Geser jari pelan dan pendek; hindari swipe besar.\n3. Latih di mode latihan 10 menit per hari.\n4. Gunakan Smart Lock (BETA) di app ini untuk melatih kecepatan reaksi.\n5. Turunkan sensitivitas sedikit jika bidikan sering melewati target.", Txt)
        }
        "Precision Aim" -> PrecisionAim(prefs)
        "Crosshair Assist" -> Crosshair(prefs)
        "Smart Lock (BETA)" -> {
            SCard { H("Smart Lock (BETA)"); P("Latihan reaksi. Fitur ini TIDAK mengunci bidikan di game apa pun — hanya melatih kecepatan dan ketepatan ketukan Anda.") }
            Trainer(prefs, "smart", 1f)
        }
        "Headlock 45%" -> {
            SCard { H("Headlock 45%"); P("Latihan target kecil dengan ukuran 45% dari target standar. Hanya latihan di dalam aplikasi, tidak memengaruhi game.") }
            Trainer(prefs, "head45", 0.45f)
        }
        "Memory Optimize" -> {
            SCard {
                H("RAM ${(d.ramUsedFrac * 100).toInt()}% terpakai")
                Bar(d.ramUsedFrac, if (d.ramUsedFrac > 0.85f) Red else Cyan)
                P("Bebas ${gb(d.ramFree)} dari ${gb(d.ramTotal)}")
                P(if (d.ramUsedFrac > 0.85f) "RAM hampir penuh. Tutup aplikasi lain lewat layar recent apps sebelum main." else "RAM masih lega.", Txt)
            }
            SCard {
                P("Android mengelola RAM sendiri dan tidak mengizinkan aplikasi biasa membersihkan RAM aplikasi lain. Yang bisa dilakukan: tutup aplikasi manual dan cek aplikasi boros.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SBtn("Perbarui", Modifier.weight(1f), onClick = refresh)
                    SBtn("Daftar Aplikasi", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_APPLICATION_SETTINGS) }
                }
            }
        }
        "FPS Optimize" -> {
            SCard {
                H("Layar ${d.refresh.toInt()} Hz")
                P(if (d.refresh >= 90f) "Layar mendukung refresh tinggi. Pilih frame rate tertinggi yang stabil di pengaturan game." else "Layar 60 Hz: FPS game di atas 60 tidak akan terlihat lebih halus.", Txt)
                P("Aplikasi ini tidak bisa menaikkan FPS secara otomatis. Saran: grafis Smooth, frame rate sesuai kemampuan HP, tutup aplikasi latar, jaga suhu.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SBtn("Pengaturan Layar", Modifier.weight(1f)) { openSettings(ctx, Settings.ACTION_DISPLAY_SETTINGS) }
                    SBtn("Opsi Developer", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS) }
                }
            }
        }
        "Graphics Optimize" -> {
            val g = d.ramTotal / 1073741824.0
            val (gfx, fps) = when {
                g < 3.5 -> "Smooth" to "Medium"
                g < 6.5 -> "Smooth" to "High"
                else -> "Standard (atau Smooth)" to "Max/High (jika tersedia)"
            }
            SCard {
                H("Rekomendasi untuk ${d.model}")
                P("RAM ${gb(d.ramTotal)} • ${d.cores} core • ${d.refresh.toInt()} Hz", Txt)
                P("Graphics: $gfx\nFrame rate: $fps\nShadow: OFF\nIni rekomendasi; atur sendiri di Settings game.", Txt)
                SBtn("Simpan sebagai preset kustom", Modifier.fillMaxWidth()) {
                    prefs.put("custom_gfx", "Graphics: $gfx • FPS: $fps"); toast(ctx, "Tersimpan")
                }
                val saved = prefs.str("custom_gfx")
                if (saved.isNotEmpty()) P("Tersimpan: $saved")
            }
        }
        "CPU Stabilizer" -> {
            SCard {
                H("Termal: ${d.thermal}")
                P("Suhu baterai ${d.batTemp}°C • ${d.cores} core CPU", Txt)
                P("Aplikasi tidak dapat mengatur frekuensi CPU tanpa root. Untuk menjaga performa stabil: lepas casing tebal, jangan main sambil mengisi daya, kurangi grafis/FPS jika panas, dan hindari sinar matahari langsung.")
                SBtn("Perbarui", Modifier.fillMaxWidth(), onClick = refresh)
            }
        }
        "Cache Cleaner" -> {
            var size by remember { mutableStateOf(cacheSize(ctx)) }
            var msg by remember { mutableStateOf("") }
            SCard {
                H("Cache SETHEALTVEX: ${mb(size)}")
                SBtn("Bersihkan Cache", Modifier.fillMaxWidth()) {
                    val f = clearCache(ctx); size = cacheSize(ctx); msg = "Dibersihkan: ${mb(f)}"
                }
                if (msg.isNotEmpty()) P(msg, Cyan)
                P("Aplikasi hanya dapat membersihkan cache miliknya sendiri. Untuk cache aplikasi lain, buka Pengaturan penyimpanan.")
                SBtn("Pengaturan Penyimpanan", Modifier.fillMaxWidth(), false) { openSettings(ctx, Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }
            }
        }
        "Optimize Battery" -> {
            SCard {
                H("Baterai ${d.battery}%")
                Bar(if (d.battery >= 0) d.battery / 100f else 0f, if (d.battery in 0..19) Red else Cyan)
                P("${if (d.charging) "Mengisi daya" else "Tidak mengisi"} • ${d.batTemp}°C", Txt)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SBtn("Penghemat Baterai", Modifier.weight(1f)) { openSettings(ctx, Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                    SBtn("Optimasi Baterai", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) }
                }
            }
        }
        "Quick Optimize" -> {
            var rep by remember { mutableStateOf("") }
            SCard {
                H("Quick Optimize")
                SBtn("JALANKAN", Modifier.fillMaxWidth()) { rep = quickOptimize(ctx, quickClear); refresh() }
                if (rep.isNotEmpty()) P(rep, Txt)
            }
        }
        "Touch Check" -> TouchCheck()
        "Network Refresh" -> {
            SCard {
                H("Jaringan: ${d.net}")
                SBtn("Periksa Ulang", Modifier.fillMaxWidth(), onClick = refresh)
                P("Aplikasi tidak bisa mereset jaringan. Jika koneksi lambat, coba mode pesawat 5 detik atau ganti Wi-Fi/seluler.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SBtn("Wi-Fi", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_WIFI_SETTINGS) }
                    SBtn("Mode Pesawat", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_AIRPLANE_MODE_SETTINGS) }
                }
            }
        }
        "Background Check" -> {
            SCard {
                H("RAM terpakai ${(d.ramUsedFrac * 100).toInt()}%")
                Bar(d.ramUsedFrac)
                P("Android tidak mengizinkan aplikasi biasa melihat daftar aplikasi lain yang berjalan. Gunakan layar bawaan berikut untuk memeriksanya.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SBtn("Pemakaian Baterai", Modifier.weight(1f)) { openSettings(ctx, Intent.ACTION_POWER_USAGE_SUMMARY) }
                    SBtn("Daftar Aplikasi", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_APPLICATION_SETTINGS) }
                }
            }
        }
        "Preset & Tools" -> PresetTools(ctx, prefs)
        "Device Information" -> {
            SCard {
                H("Device Information")
                listOf(
                    "Perangkat" to "${d.maker} ${d.model}", "Android" to "${d.android} (API ${d.sdk})", "ABI" to d.abi,
                    "CPU core" to "${d.cores}", "RAM" to "${gb(d.ramTotal)} (bebas ${gb(d.ramFree)})",
                    "Penyimpanan" to "${gb(d.stTotal)} (sisa ${gb(d.stFree)})", "Layar" to "${d.screen} • ${d.refresh.toInt()} Hz",
                    "Baterai" to "${d.battery}% • ${d.batTemp}°C", "Termal" to d.thermal, "Jaringan" to d.net, "Diperbarui" to d.time
                ).forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(k, color = Muted, fontSize = 13.sp)
                        Text(v, color = Txt, fontSize = 13.sp)
                    }
                }
                SBtn("Perbarui", Modifier.fillMaxWidth(), onClick = refresh)
            }
        }
        else -> SCard { P("Menu tidak ditemukan.") }
    }
}

@Composable
fun SmartSensi(prefs: Prefs) {
    val names = listOf("General", "Red Dot", "2x Scope", "4x Scope", "AWM Scope", "Free Look")
    val st = remember { names.map { PInt(prefs, "sensi_$it", 50) } }
    fun fill(v: List<Int>) = v.forEachIndexed { i, x -> st[i].set(x) }
    SCard {
        H("Preset")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SBtn("Balanced", Modifier.weight(1f)) { fill(listOf(50, 48, 45, 40, 35, 50)) }
            SBtn("Fast", Modifier.weight(1f)) { fill(listOf(75, 70, 65, 60, 55, 65)) }
            SBtn("Precise", Modifier.weight(1f)) { fill(listOf(40, 38, 35, 30, 25, 40)) }
        }
        P("Nilai tersimpan otomatis. Ini panduan; masukkan angka yang sama secara manual di Settings > Sensitivity pada game.")
    }
    SCard {
        names.forEachIndexed { i, n ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(n, color = Txt, fontSize = 14.sp)
                Text("${st[i].v}", color = Cyan, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = st[i].v.toFloat(), onValueChange = { st[i].set(it.toInt()) }, valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Red, inactiveTrackColor = Line)
            )
        }
    }
}

@Composable
fun PrecisionAim(prefs: Prefs) {
    val items = listOf(
        "Layar dibersihkan, tangan tidak berkeringat", "Brightness cukup & mode hemat daya dimatikan",
        "Sensitivitas sudah dicek di Smart Sensi", "Notifikasi / Do Not Disturb aktif",
        "Pemanasan 3 menit di mode latihan", "Koneksi internet stabil"
    )
    val st = remember { items.mapIndexed { i, _ -> PBool(prefs, "pa_$i", false) } }
    SCard {
        H("Checklist Precision Aim")
        P("Daftar persiapan sebelum bertanding. Centang tersimpan lokal.")
        items.forEachIndexed { i, t ->
            Row(Modifier.fillMaxWidth().clickable { st[i].set(!st[i].v) }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = st[i].v, onCheckedChange = { st[i].set(it) }, colors = CheckboxDefaults.colors(checkedColor = Red))
                Text(t, color = Txt, fontSize = 14.sp)
            }
        }
        SBtn("Reset checklist", red = false) { st.forEach { it.set(false) } }
    }
}

@Composable
fun Crosshair(prefs: Prefs) {
    val style = remember { PStr(prefs, "xh_style", "Cross") }
    val color = remember { PStr(prefs, "xh_color", "Cyan") }
    val sz = remember { PInt(prefs, "xh_size", 40) }
    val col = when (color.v) { "Red" -> Red; "White" -> Color.White; else -> Cyan }
    SCard {
        H("Pratinjau Crosshair")
        Canvas(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F1A14))) {
            val c = center
            val r = sz.v.toFloat() * 1.5f
            when (style.v) {
                "Dot" -> drawCircle(col, r / 6f, c)
                "Circle" -> { drawCircle(col, r, c, style = Stroke(4f)); drawCircle(col, 4f, c) }
                else -> {
                    drawLine(col, Offset(c.x - r, c.y), Offset(c.x - r / 4, c.y), 4f)
                    drawLine(col, Offset(c.x + r / 4, c.y), Offset(c.x + r, c.y), 4f)
                    drawLine(col, Offset(c.x, c.y - r), Offset(c.x, c.y - r / 4), 4f)
                    drawLine(col, Offset(c.x, c.y + r / 4), Offset(c.x, c.y + r), 4f)
                }
            }
        }
        P("Gaya")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Cross", "Dot", "Circle").forEach { s -> SBtn(s, Modifier.weight(1f), style.v == s) { style.set(s) } }
        }
        P("Warna")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Cyan", "Red", "White").forEach { s -> SBtn(s, Modifier.weight(1f), color.v == s) { color.set(s) } }
        }
        P("Ukuran: ${sz.v}")
        Slider(
            value = sz.v.toFloat(), onValueChange = { sz.set(it.toInt()) }, valueRange = 10f..100f,
            colors = SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Red, inactiveTrackColor = Line)
        )
        P("Pengaturan tersimpan. Crosshair hanya tampil di dalam aplikasi ini (tidak ada overlay di atas game).")
    }
}

@Composable
fun Trainer(p: Prefs, key: String, scale: Float) {
    var running by remember { mutableStateOf(false) }
    var hits by remember { mutableIntStateOf(0) }
    var px by remember { mutableFloatStateOf(0.5f) }
    var py by remember { mutableFloatStateOf(0.5f) }
    var shown by remember { mutableLongStateOf(0L) }
    var total by remember { mutableLongStateOf(0L) }
    var last by remember { mutableIntStateOf(0) }
    var best by remember { mutableIntStateOf(p.int("best_$key", 0)) }
    val rounds = 10
    SCard {
        P("Ketuk $rounds target secepat mungkin.")
        Text("Terakhir: ${if (last > 0) "$last ms" else "-"}   •   Terbaik: ${if (best > 0) "$best ms" else "-"}", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        BoxWithConstraints(Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F1115))) {
            val s = (64 * scale).dp
            if (running) {
                androidx.compose.foundation.layout.Box(
                    Modifier
                        .offset(x = (maxWidth - s) * px, y = (maxHeight - s) * py)
                        .size(s)
                        .clip(CircleShape)
                        .background(Red)
                        .clickable {
                            total += System.currentTimeMillis() - shown
                            hits++
                            if (hits >= rounds) {
                                running = false
                                last = (total / rounds).toInt()
                                if (best == 0 || last < best) { best = last; p.put("best_$key", best) }
                            } else {
                                px = Random.nextFloat(); py = Random.nextFloat(); shown = System.currentTimeMillis()
                            }
                        }
                )
            }
        }
        SBtn(if (running) "Ulangi" else "Mulai", Modifier.fillMaxWidth()) {
            hits = 0; total = 0L; px = Random.nextFloat(); py = Random.nextFloat()
            shown = System.currentTimeMillis(); running = true
        }
    }
}

@Composable
fun TouchCheck() {
    var pts by remember { mutableStateOf(listOf<Offset>()) }
    var maxP by remember { mutableIntStateOf(0) }
    SCard {
        H("Touch Check")
        P("Sentuh layar dengan beberapa jari. Sentuhan aktif: ${pts.size} • Maksimum terdeteksi: $maxP", Txt)
        Canvas(
            Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F1115))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent()
                            pts = e.changes.filter { it.pressed }.map { it.position }
                            if (pts.size > maxP) maxP = pts.size
                            e.changes.forEach { it.consume() }
                        }
                    }
                }
        ) {
            pts.forEach { drawCircle(Cyan, 56f, it); drawCircle(Red, 18f, it) }
        }
        SBtn("Reset", red = false) { maxP = 0 }
    }
}

@Composable
fun PresetTools(ctx: Context, prefs: Prefs) {
    val sel = remember { PStr(prefs, "ff_preset", "Belum dipilih") }
    SCard {
        H("FREE SETTINGS FOR OPTIMIZE FREE FIRE")
        P("Pilih preset. Pilihan tersimpan lokal. Preset adalah rekomendasi dan TIDAK otomatis mengubah pengaturan internal game — atur manual di Settings Free Fire.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ffPresets.keys.forEach { k -> SBtn(k, Modifier.weight(1f), sel.v == k) { sel.set(k) } }
        }
    }
    val lines = ffPresets[sel.v]
    if (lines != null) {
        SCard {
            H("Preset ${sel.v} (aktif)")
            P(lines.joinToString("\n") { "• $it" }, Txt)
            SBtn("Salin ringkasan", Modifier.fillMaxWidth(), false) {
                copyText(ctx, "SETHEALTVEX — Preset ${sel.v}\n" + lines.joinToString("\n"))
            }
        }
    }
    SCard {
        H("Pintasan Pengaturan Android")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SBtn("Layar", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_DISPLAY_SETTINGS) }
            SBtn("Baterai", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_BATTERY_SAVER_SETTINGS) }
            SBtn("Suara", Modifier.weight(1f), false) { openSettings(ctx, Settings.ACTION_SOUND_SETTINGS) }
        }
    }
}
