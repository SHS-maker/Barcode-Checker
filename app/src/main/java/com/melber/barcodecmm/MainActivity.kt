package com.melber.barcodecmm

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Displays two consecutive scans from the RS38's integrated scanner (configured
 * on-device to run in keyboard-emulation / HID mode) and checks that they match.
 * The scanner's output arrives as ordinary key events into [etScannerInput];
 * no CipherLAB SDK integration is needed.
 */
class MainActivity : AppCompatActivity() {

    private enum class ScanState { WAITING_BARCODE1, WAITING_BARCODE2, SHOWING_RESULT }

    private lateinit var root: LinearLayout
    private lateinit var tvBarcode1: TextView
    private lateinit var tvBarcode2: TextView
    private lateinit var tvResult: TextView
    private lateinit var etScannerInput: EditText
    private lateinit var btnReset: Button
    private lateinit var btnManualEntry: Button
    private lateinit var btnSettings: ImageButton

    private lateinit var settings: Settings
    private val handler = Handler(Looper.getMainLooper())
    private val autoResetRunnable = Runnable { resetAll() }

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    private var state = ScanState.WAITING_BARCODE1
    private var barcode1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableFullscreen()

        settings = Settings(this)

        root = findViewById(R.id.root)
        tvBarcode1 = findViewById(R.id.tvBarcode1)
        tvBarcode2 = findViewById(R.id.tvBarcode2)
        tvResult = findViewById(R.id.tvResult)
        etScannerInput = findViewById(R.id.etScannerInput)
        btnReset = findViewById(R.id.btnReset)
        btnManualEntry = findViewById(R.id.btnManualEntry)
        btnSettings = findViewById(R.id.btnSettings)

        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        setupScannerInput()
        root.setOnClickListener { etScannerInput.requestFocus() }
        btnReset.setOnClickListener { resetAll() }
        btnManualEntry.setOnClickListener { showManualEntryDialog() }
        btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        resetAll()
    }

    override fun onResume() {
        super.onResume()
        applySettings()
        etScannerInput.requestFocus()
    }

    /**
     * Übernimmt die im Einstellungs-Menü gesetzten Optionen. Wird bei jeder
     * Rückkehr aufgerufen, damit Änderungen sofort sichtbar sind.
     */
    private fun applySettings() {
        btnReset.visibility = if (settings.showResetButton) View.VISIBLE else View.GONE
        btnManualEntry.visibility = if (settings.showManualEntryButton) View.VISIBLE else View.GONE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullscreen()
            etScannerInput.requestFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoResetRunnable)
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun enableFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    /**
     * The scanner (keyboard-emulation mode) types the barcode as individual key
     * events and terminates it with an Enter key. That Enter is caught both as a
     * raw KeyEvent (typical for a single-line EditText) and, as a fallback, as a
     * literal '\n'/'\r' appended to the text (some scanner profiles insert it
     * into the buffer instead of dispatching a distinct key event).
     */
    private fun setupScannerInput() {
        etScannerInput.setShowSoftInputOnFocus(false)

        etScannerInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
            ) {
                consumeScan(etScannerInput.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        etScannerInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val text = s.toString()
                val terminatorIndex = text.indexOfFirst { it == '\n' || it == '\r' }
                if (terminatorIndex >= 0) {
                    consumeScan(text.substring(0, terminatorIndex))
                }
            }
        })
    }

    /** Fallback for damaged barcodes: lets the user type the expected value instead of scanning it. */
    private fun showManualEntryDialog() {
        if (state == ScanState.SHOWING_RESULT) return

        val titleRes = if (state == ScanState.WAITING_BARCODE1) {
            R.string.manual_entry_title_1
        } else {
            R.string.manual_entry_title_2
        }
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.manual_entry_hint)
        }

        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(input)
            .setPositiveButton(R.string.manual_entry_confirm) { _, _ -> consumeScan(input.text.toString()) }
            .setNegativeButton(R.string.manual_entry_cancel, null)
            .setOnDismissListener { etScannerInput.requestFocus() }
            .show()
    }

    private fun consumeScan(rawCode: String) {
        etScannerInput.text?.clear()
        val code = rawCode.trim()
        if (code.isEmpty()) return

        when (state) {
            ScanState.WAITING_BARCODE1 -> {
                barcode1 = code
                tvBarcode1.text = code
                state = ScanState.WAITING_BARCODE2
            }
            ScanState.WAITING_BARCODE2 -> {
                tvBarcode2.text = code
                val first = barcode1.orEmpty()
                val match = barcodesMatch(first, code)
                if (!match && settings.highlightDifferences) highlightDifferences(first, code)
                showResult(match)
                state = ScanState.SHOWING_RESULT
                val seconds = settings.autoResetSeconds
                if (seconds > 0) {
                    handler.postDelayed(autoResetRunnable, seconds * 1000L)
                }
            }
            ScanState.SHOWING_RESULT -> {
                // Ignore scans until the result is cleared.
            }
        }
    }

    /**
     * Exakte Übereinstimmung zählt immer. Zusätzlich – sofern in den
     * Einstellungen aktiviert – auch: ein Code vollständig im anderen enthalten,
     * oder der längere Code ist der kürzere mit genau einer zusätzlichen '0'.
     */
    private fun barcodesMatch(a: String, b: String): Boolean {
        if (a == b) return true
        if (settings.toleranceContains && (a.contains(b) || b.contains(a))) return true
        if (settings.toleranceExtraZero &&
            (matchesWithOneExtraZero(a, b) || matchesWithOneExtraZero(b, a))
        ) return true
        return false
    }

    private fun matchesWithOneExtraZero(shorter: String, longer: String): Boolean {
        if (longer.length != shorter.length + 1) return false
        return longer.indices.any { i -> longer[i] == '0' && longer.removeRange(i, i + 1) == shorter }
    }

    /** Markiert bei INCORRECT in beiden Feldern die Zeichen, die im anderen Code fehlen. */
    private fun highlightDifferences(first: String, second: String) {
        val (diffFirst, diffSecond) = differingIndices(first, second)
        tvBarcode1.text = withDiffHighlight(first, diffFirst)
        tvBarcode2.text = withDiffHighlight(second, diffSecond)
    }

    /**
     * Bestimmt über die längste gemeinsame Teilfolge, welche Positionen im
     * jeweils anderen Code keine Entsprechung haben. Ein einzelnes eingefügtes
     * Zeichen markiert dadurch nur sich selbst und nicht den ganzen Rest.
     */
    private fun differingIndices(a: String, b: String): Pair<Set<Int>, Set<Int>> {
        val n = a.length
        val m = b.length
        val lcs = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                lcs[i][j] = if (a[i] == b[j]) {
                    lcs[i + 1][j + 1] + 1
                } else {
                    maxOf(lcs[i + 1][j], lcs[i][j + 1])
                }
            }
        }

        val diffA = mutableSetOf<Int>()
        val diffB = mutableSetOf<Int>()
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                a[i] == b[j] -> { i++; j++ }
                lcs[i + 1][j] >= lcs[i][j + 1] -> diffA.add(i++)
                else -> diffB.add(j++)
            }
        }
        while (i < n) diffA.add(i++)
        while (j < m) diffB.add(j++)
        return diffA to diffB
    }

    private fun withDiffHighlight(code: String, diff: Set<Int>): CharSequence {
        if (diff.isEmpty()) return code
        val spannable = SpannableString(code)
        val background = ContextCompat.getColor(this, R.color.result_incorrect)
        val foreground = ContextCompat.getColor(this, R.color.result_text_on_fill)

        // Zusammenhängende Abschnitte in einem Zug markieren statt Zeichen für Zeichen
        var start = 0
        while (start < code.length) {
            if (start !in diff) {
                start++
                continue
            }
            var end = start
            while (end + 1 < code.length && (end + 1) in diff) end++
            val flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            spannable.setSpan(BackgroundColorSpan(background), start, end + 1, flag)
            spannable.setSpan(ForegroundColorSpan(foreground), start, end + 1, flag)
            spannable.setSpan(StyleSpan(Typeface.BOLD), start, end + 1, flag)
            start = end + 1
        }
        return spannable
    }

    private fun showResult(match: Boolean) {
        val fillColor = if (match) R.color.result_correct else R.color.result_incorrect
        tvResult.setTextColor(ContextCompat.getColor(this, R.color.result_text_on_fill))
        tvResult.text = getString(if (match) R.string.result_correct else R.string.result_incorrect)
        tvResult.setBackgroundResource(
            if (match) R.drawable.bg_result_correct else R.drawable.bg_result_incorrect
        )
        if (settings.fullscreenFlash) {
            root.setBackgroundColor(ContextCompat.getColor(this, fillColor))
        }
        if (settings.soundEnabled) {
            toneGenerator?.startTone(
                if (match) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK,
                if (match) 200 else 400
            )
        }
        if (settings.vibrationEnabled) {
            vibrate(match)
        }
    }

    /**
     * Deutlich unterscheidbare Muster, damit auch ohne Blick auf den Bildschirm
     * klar ist, was passiert ist: CORRECT einmal kurz, INCORRECT dreimal lang.
     */
    private fun vibrate(match: Boolean) {
        val device = vibrator ?: return
        if (!device.hasVibrator()) return

        val effect = if (match) {
            VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
        } else {
            // Zwei kurze Impulse: gut vom einzelnen bei CORRECT zu unterscheiden,
            // ohne aufdringlich zu sein (Pause, an, aus, an – ohne Wiederholung)
            VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
        }
        device.vibrate(effect)
    }

    private fun resetAll() {
        handler.removeCallbacks(autoResetRunnable)
        barcode1 = null
        state = ScanState.WAITING_BARCODE1
        tvBarcode1.text = getString(R.string.placeholder_waiting)
        tvBarcode2.text = getString(R.string.placeholder_waiting)
        tvResult.text = ""
        tvResult.setBackgroundResource(R.drawable.bg_field)
        tvResult.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.background))
        etScannerInput.requestFocus()
    }
}
