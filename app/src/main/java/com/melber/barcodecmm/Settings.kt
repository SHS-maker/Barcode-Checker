package com.melber.barcodecmm

import android.content.Context
import android.content.SharedPreferences

/**
 * Alle zur Laufzeit änderbaren Einstellungen an einer Stelle.
 *
 * Wird über das Zahnrad-Symbol im Hauptbildschirm bearbeitet und dauerhaft auf
 * dem Gerät gespeichert – eine neue APK ist für diese Änderungen nicht nötig.
 */
class Settings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("barcode_checker_settings", Context.MODE_PRIVATE)

    var showResetButton: Boolean
        get() = prefs.getBoolean(KEY_SHOW_RESET, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_RESET, value).apply()

    var showManualEntryButton: Boolean
        get() = prefs.getBoolean(KEY_SHOW_MANUAL, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_MANUAL, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var fullscreenFlash: Boolean
        get() = prefs.getBoolean(KEY_FULLSCREEN_FLASH, true)
        set(value) = prefs.edit().putBoolean(KEY_FULLSCREEN_FLASH, value).apply()

    var highlightDifferences: Boolean
        get() = prefs.getBoolean(KEY_HIGHLIGHT_DIFF, true)
        set(value) = prefs.edit().putBoolean(KEY_HIGHLIGHT_DIFF, value).apply()

    var toleranceContains: Boolean
        get() = prefs.getBoolean(KEY_TOLERANCE_CONTAINS, true)
        set(value) = prefs.edit().putBoolean(KEY_TOLERANCE_CONTAINS, value).apply()

    var toleranceExtraZero: Boolean
        get() = prefs.getBoolean(KEY_TOLERANCE_ZERO, true)
        set(value) = prefs.edit().putBoolean(KEY_TOLERANCE_ZERO, value).apply()

    /** Sekunden bis zum automatischen Reset nach dem Ergebnis; 0 = nur manuell. */
    var autoResetSeconds: Int
        get() = prefs.getInt(KEY_AUTO_RESET, 0)
        set(value) = prefs.edit().putInt(KEY_AUTO_RESET, value.coerceIn(0, 60)).apply()

    private companion object {
        const val KEY_SHOW_RESET = "show_reset_button"
        const val KEY_SHOW_MANUAL = "show_manual_entry_button"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_VIBRATION = "vibration_enabled"
        const val KEY_FULLSCREEN_FLASH = "fullscreen_flash"
        const val KEY_HIGHLIGHT_DIFF = "highlight_differences"
        const val KEY_TOLERANCE_CONTAINS = "tolerance_contains"
        const val KEY_TOLERANCE_ZERO = "tolerance_extra_zero"
        const val KEY_AUTO_RESET = "auto_reset_seconds"
    }
}
