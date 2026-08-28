package com.melber.barcodecmm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/**
 * Einstellungen, die ohne neue APK änderbar sind. Jede Änderung wird sofort
 * gespeichert; [MainActivity] übernimmt sie beim Zurückkehren automatisch.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var etAutoReset: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settings = Settings(this)

        bindSwitch(R.id.swShowReset, settings.showResetButton) { settings.showResetButton = it }
        bindSwitch(R.id.swShowManual, settings.showManualEntryButton) { settings.showManualEntryButton = it }
        bindSwitch(R.id.swSound, settings.soundEnabled) { settings.soundEnabled = it }
        bindSwitch(R.id.swVibration, settings.vibrationEnabled) { settings.vibrationEnabled = it }
        bindSwitch(R.id.swFullscreenFlash, settings.fullscreenFlash) { settings.fullscreenFlash = it }
        bindSwitch(R.id.swHighlightDiff, settings.highlightDifferences) { settings.highlightDifferences = it }
        bindSwitch(R.id.swToleranceContains, settings.toleranceContains) { settings.toleranceContains = it }
        bindSwitch(R.id.swToleranceZero, settings.toleranceExtraZero) { settings.toleranceExtraZero = it }

        etAutoReset = findViewById(R.id.etAutoReset)
        if (settings.autoResetSeconds > 0) {
            etAutoReset.setText(settings.autoResetSeconds.toString())
        }

        findViewById<Button>(R.id.btnDone).setOnClickListener { finish() }
    }

    override fun onPause() {
        super.onPause()
        settings.autoResetSeconds = etAutoReset.text.toString().toIntOrNull() ?: 0
    }

    private fun bindSwitch(viewId: Int, current: Boolean, onChange: (Boolean) -> Unit) {
        findViewById<SwitchCompat>(viewId).apply {
            isChecked = current
            setOnCheckedChangeListener { _, checked -> onChange(checked) }
        }
    }
}
