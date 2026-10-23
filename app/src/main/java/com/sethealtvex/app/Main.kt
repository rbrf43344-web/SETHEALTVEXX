package com.sethealtvex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val BG = Color(0xFF0A0A0C)
val PanelBg = Color(0xFF15161A)
val CardBg = Color(0xFF1C1D23)
val Line = Color(0xFF2A2C35)
val Red = Color(0xFFE5192D)
val Cyan = Color(0xFF19E3FF)
val Txt = Color(0xFFE8E8EC)
val Muted = Color(0xFF8A8D96)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(primary = Red, secondary = Cyan, background = BG, surface = PanelBg)
            ) { SethApp() }
        }
    }
}

// ---------- komponen UI ----------
@Composable
fun SCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    var m = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(CardBg)
        .border(1.dp, Line, RoundedCornerShape(14.dp))
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
fun H(t: String) = Text(t, color = Txt, fontSize = 18.sp, fontWeight = FontWeight.Bold)

@Composable
fun P(t: String, c: Color = Muted) = Text(t, color = c, fontSize = 13.sp, lineHeight = 18.sp)

@Composable
fun SBtn(t: String, modifier: Modifier = Modifier, red: Boolean = true, onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    colors = ButtonDefaults.buttonColors(containerColor = if (red) Red else Line, contentColor = Color.White)
) { Text(t, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }

@Composable
fun Bar(frac: Float, color: Color = Cyan) = LinearProgressIndicator(
    progress = { frac.coerceIn(0f, 1f) },
    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
    color = color,
    trackColor = Line
)

@Composable
fun ToggleRow(title: String, desc: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Txt, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            P(desc)
        }
        Text(if (on) "ON" else "OFF", color = if (on) Cyan else Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
        Switch(
            checked = on, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Red, checkedThumbColor = Color.White)
        )
    }
}

val aimItems = listOf(
    "Smart Sensi" to "Preset sensitivitas & slider tersimpan",
    "Headtrick" to "Panduan latihan kontrol bidikan",
    "Precision Aim" to "Checklist persiapan bidikan",
    "Crosshair Assist" to "Pratinjau crosshair di dalam aplikasi",
    "Smart Lock (BETA)" to "Latihan reaksi: ketuk target bergerak",
    "Headlock 45%" to "Latihan target kecil (45% ukuran)"
)
val optItems = listOf(
    "Memory Optimize" to "Status RAM & saran",
    "FPS Optimize" to "Refresh rate layar & saran FPS",
    "Graphics Optimize" to "Rekomendasi grafis sesuai spesifikasi",
    "CPU Stabilizer" to "Suhu & status termal",
    "Cache Cleaner" to "Bersihkan cache SETHEALTVEX",
    "Optimize Battery" to "Status baterai & pengaturan hemat daya"
)
val toolItems = listOf(
    "Quick Optimize" to "Satu tombol: bersihkan cache & cek kondisi",
    "Touch Check" to "Uji multi-touch layar",
    "Network Refresh" to "Cek koneksi & buka pengaturan jaringan",
    "Background Check" to "Cek beban latar belakang",
    "Preset & Tools" to "Preset Free Fire tersimpan lokal",
    "Device Information" to "Detail perangkat"
)

@Composable
fun MenuList(items: List<Pair<String, String>>, onOpen: (String) -> Unit) {
    items.forEach { (n, d) ->
        SCard(onClick = { onOpen(n) }) {
            Text(n, color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            P(d)
        }
    }
}

// ---------- aplikasi ----------
@Composable
fun SethApp() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val auto = remember { PBool(prefs, "auto_refresh", true) }
    val keepOn = remember { PBool(prefs, "keep_on", false) }
    val quickClear = remember { PBool(prefs, "quick_clear", true) }
    var tab by remember { mutableIntStateOf(0) }
    var sub by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf(readDevice(ctx)) }
    var quickReport by remember { mutableStateOf("") }
    val view = LocalView.current
    SideEffect { view.keepScreenOn = keepOn.v }
    LaunchedEffect(auto.v) {
        while (auto.v) {
            delay(5000)
            info = readDevice(ctx)
        }
    }
    BackHandler(sub != null) { sub = null }
    val refresh: () -> Unit = { info = readDevice(ctx) }
    val tabs = listOf(
        "Home" to Icons.Default.Home, "Aim" to Icons.Default.Star, "Optimize" to Icons.Default.Build,
        "Tools" to Icons.Default.Settings, "About" to Icons.Default.Info
    )

    Scaffold(
        containerColor = BG,
        topBar = {
            Column(Modifier.fillMaxWidth().background(PanelBg).statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("SETHEALTVEX", color = Red, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 3.sp)
                Text(sub ?: "Optimize Tools — Android", color = Cyan, fontSize = 12.sp)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = PanelBg) {
                tabs.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i; sub = null },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan, selectedTextColor = Cyan, indicatorColor = Line,
                            unselectedIconColor = Muted, unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val s = sub
            if (s != null) {
                SBtn("← Kembali", red = false) { sub = null }
                Detail(s, ctx, prefs, info, refresh, quickClear.v)
            } else when (tab) {
                0 -> Home(info, prefs, auto, keepOn, quickClear, quickReport,
                    onQuick = { quickReport = quickOptimize(ctx, quickClear.v); refresh() })
                1 -> MenuList(aimItems) { sub = it }
                2 -> MenuList(optItems) { sub = it }
                3 -> MenuList(toolItems) { sub = it }
                else -> About()
            }
        }
    }
}

@Composable
fun Home(
    d: DevInfo, prefs: Prefs, auto: PBool, keepOn: PBool, quickClear: PBool,
    report: String, onQuick: () -> Unit
) {
    val preset = prefs.str("ff_preset", "Belum dipilih")
    val active = listOf(auto.v, keepOn.v, quickClear.v).count { it }
    SCard {
        Text("SETHEALTVEX", color = Txt, fontWeight = FontWeight.Black, fontSize = 26.sp, letterSpacing = 2.sp)
        Text("STATUS: AKTIF • $active/3 fitur ON", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        P("${d.maker} ${d.model} • Android ${d.android} (API ${d.sdk})")
        P("Diperbarui ${d.time} • Preset Free Fire: $preset")
    }
    SCard {
        H("Baterai ${if (d.battery >= 0) "${d.battery}%" else "-"}")
        Bar(if (d.battery >= 0) d.battery / 100f else 0f, if (d.battery in 0..19) Red else Cyan)
        P("${if (d.charging) "Mengisi daya" else "Tidak mengisi"} • ${d.batTemp}°C")
    }
    SCard {
        H("Penyimpanan ${gb(d.stFree)} tersisa")
        Bar(d.stUsedFrac, if (d.stUsedFrac > 0.9f) Red else Cyan)
        P("Total ${gb(d.stTotal)}")
    }
    SCard {
        H("RAM ${(d.ramUsedFrac * 100).toInt()}% terpakai")
        Bar(d.ramUsedFrac, if (d.ramUsedFrac > 0.85f) Red else Cyan)
        P("Bebas ${gb(d.ramFree)} dari ${gb(d.ramTotal)}")
    }
    SCard {
        SBtn("QUICK OPTIMIZE", Modifier.fillMaxWidth(), onClick = onQuick)
        if (report.isNotEmpty()) P(report, Txt)
    }
    SCard {
        H("Easy Toggle")
        ToggleRow("Auto-refresh info", "Perbarui data perangkat tiap 5 detik", auto.v) { auto.set(it) }
        ToggleRow("Layar tetap menyala", "Cegah layar mati saat SETHEALTVEX terbuka", keepOn.v) { keepOn.set(it) }
        ToggleRow("Bersihkan cache di Quick Optimize", "Hapus cache SETHEALTVEX saat Quick Optimize", quickClear.v) { quickClear.set(it) }
    }
    SCard {
        H("Special Features")
        P("• FREE SETTINGS FOR OPTIMIZE FREE FIRE — preset di Tools > Preset & Tools\n• Easy Toggle ON/OFF — tersimpan otomatis\n• Local Dashboard — data langsung dari Android\n• Ready To Use — tanpa akun, tanpa root\n• No Ribet Menu — 5 tab utama\n• Android Support — Android 7.0 (API 24) ke atas", Txt)
    }
}

@Composable
fun About() {
    SCard {
        H("About SETHEALTVEX")
        P("SETHEALTVEX — Optimize Tools (Android) v1.0.0", Txt)
        P("Aplikasi utilitas gaming untuk memantau kondisi perangkat dan memberi rekomendasi pengaturan agar bermain game lebih nyaman.")
    }
    SCard {
        H("Privasi")
        P("Semua data tersimpan di perangkat. Tidak ada akun, tidak ada backend, tidak ada pengiriman data ke pihak lain. Izin yang dipakai hanya status jaringan.")
    }
    SCard {
        H("Batasan")
        P("Preset Free Fire adalah rekomendasi, tidak mengubah pengaturan internal game. SETHEALTVEX tidak memodifikasi game, tidak menyuntikkan kode, dan tidak mengunci bidikan. Android tidak mengizinkan aplikasi biasa membersihkan RAM aplikasi lain atau menaikkan FPS secara otomatis.")
    }
}
