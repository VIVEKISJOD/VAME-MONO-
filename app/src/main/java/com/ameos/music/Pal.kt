package com.ameos.music

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// The look of the app (step71). `look` picks one of four: Lime (black + lime green, flat cards),
// Nova (deep purple glass), Light (soft grey + orange) or Custom (your own hue / strength with the old
// modes: 0 = colour glass, 1 = AMOLED flat, 2 = AMOLED glass). Everything reads its colours from here.
object Pal {
    var hue by mutableFloatStateOf(20f)
    var sat by mutableFloatStateOf(1f)
    var mode by mutableIntStateOf(0)

    // step71: the look. 0 = Lime (black + lime, flat cards), 1 = Nova (purple glass), 2 = Light, 3 = Custom
    // (the old dark looks: `mode` 0 colour glass, 1 AMOLED, 2 AMOLED glass, with `hue` / `sat`).
    var look by mutableIntStateOf(0)

    // step73: the app style (layout, shapes, spacing): 0 Material, 1 Apple, 2 Spotify. Separate from the look.
    var style by mutableIntStateOf(1)

    // old name: true = the light look. Setting it to false leaves the light look (to Lime).
    var light: Boolean
        get() = false
        set(v) {
            look = 0
        }

    // Settings > Glass (all saved)
    var glassMat by mutableIntStateOf(2)          // 0 Fastest, 1 Balanced, 2 Quality
    var glassColour by mutableFloatStateOf(0.70f) // how much colour the glass takes (0..1)
    var glassFrost by mutableFloatStateOf(4f)     // frost in dp (0..24)
    var glassTint by mutableFloatStateOf(-1f)     // tint hue 0..360, -1 = none
    var glassFilm by mutableFloatStateOf(0.50f)   // white film over the glass (0..1)
    var glassActive by mutableFloatStateOf(0.26f) // strength of the selected tab highlight (0..1)
    var glassBlur by mutableFloatStateOf(14f)     // real blur in dp behind the dock / mini player (Android 12+)
    var glassReal by mutableStateOf(true)         // real blur on / off

    val lime: Boolean get() = look == 0
    val nova: Boolean get() = look == 1
    val amoled: Boolean get() = look == 0 || (look == 3 && mode != 0)
    // flat = solid dark cards without a glass sheen (Lime, and the Custom AMOLED look)
    val flat: Boolean get() = look == 0 || (look == 3 && mode == 1)
    val glassy: Boolean get() = !flat

    // text / icon colour on pages, and the colour for things that sit on a filled `fg` button
    val fg: Color get() = if (light) Color(0xFF16161A) else Color.White
    val onFg: Color get() = if (light) Color.White else Color(0xFF16161A)

    // text / icon colour on a filled accent button (dark on lime, white on purple / orange)
    val onAccent: Color get() = Color(0xFF080808)

    private fun hsv(s: Float, v: Float, a: Float = 1f): Color =
        Color.hsv(hue.coerceIn(0f, 359.9f), (s * sat).coerceIn(0f, 1f), v.coerceIn(0f, 1f), a)

    val bgTop: Color get() = Color(0xFF111111)
    val bgMid: Color get() = Color(0xFF080808)
    val bgBot: Color get() = Color(0xFF050505)
    val accent: Color get() = Color(0xFFF2F2F2)
    val accentSoft: Color get() = Color(0xFFFFFFFF)
    val accentDeep: Color get() = Color(0xFF8A8A8A)
    val textDim: Color get() = Color(0xFFAAAAAA)
    val muted: Color get() = Color(0xFF777777)
    val surface: Color get() = Color(0xFF171717)
    val surface2: Color get() = Color(0xFF242424)
    val miniA: Color get() = Color(0xFF343434)
    val miniB: Color get() = Color(0xFF191919)
    val glassTop: Color get() = Color(0xF2222222)
    val glassBottom: Color get() = Color(0xF2111111)
    val panelTop: Color get() = Color(0xF51D1D1F)
    val panelBottom: Color get() = Color(0xF50C0C0D)
    // soft glow in the bottom corner of every page (Lime: lime, Nova: purple)
    val glow: Color get() = Color.Transparent

    // colour of the glass tint (Settings > Glass > Tint), or null when it is off
    val tintColor: Color? get() =
        if (glassTint < 0f) null else Color.hsv(glassTint.coerceIn(0f, 359.9f), 0.62f, 1f)

    private const val LOOK_VERSION = 71

    fun load(c: Context) {
        val sp = c.getSharedPreferences("vame", Context.MODE_PRIVATE)
        if (sp.getInt("lookVersion", 0) < LOOK_VERSION) {
            // first start of the new look (step71): Lime. Everything else stays as it was.
            sp.edit().putInt("lookVersion", LOOK_VERSION)
                .putInt("themeLook", 0).putFloat("themeHue", 20f).putFloat("themeSat", 1f)
                .putInt("themeMode", 0).apply()
        }
        hue = sp.getFloat("themeHue", 20f)
        sat = sp.getFloat("themeSat", 1f)
        mode = sp.getInt("themeMode", 0).coerceIn(0, 2)
        look = 0
        style = 1
        glassMat = sp.getInt("glassMat", 2).coerceIn(0, 2)
        glassColour = sp.getFloat("glassColour", 0.70f).coerceIn(0f, 1f)
        glassFrost = sp.getFloat("glassFrost", 4f).coerceIn(0f, 24f)
        glassTint = sp.getFloat("glassTint", -1f)
        glassFilm = sp.getFloat("glassFilm", 0.50f).coerceIn(0f, 1f)
        glassActive = sp.getFloat("glassActive", 0.26f).coerceIn(0f, 1f)
        glassBlur = sp.getFloat("glassBlur", 14f).coerceIn(0f, 30f)
        glassReal = sp.getBoolean("glassReal", true)
    }

    fun save(c: Context) {
        c.getSharedPreferences("vame", Context.MODE_PRIVATE).edit()
            .putFloat("themeHue", hue).putFloat("themeSat", sat).putInt("themeMode", mode)
            .putInt("themeLook", look).putInt("appStyle", style)
            .putInt("glassMat", glassMat).putFloat("glassColour", glassColour)
            .putFloat("glassFrost", glassFrost).putFloat("glassTint", glassTint)
            .putFloat("glassFilm", glassFilm).putFloat("glassActive", glassActive)
            .putFloat("glassBlur", glassBlur).putBoolean("glassReal", glassReal)
            .apply()
    }

    fun resetGlass() {
        glassMat = 2
        glassColour = 0.70f
        glassFrost = 4f
        glassTint = -1f
        glassFilm = 0.50f
        glassActive = 0.26f
        glassBlur = 14f
        glassReal = true
    }
}
