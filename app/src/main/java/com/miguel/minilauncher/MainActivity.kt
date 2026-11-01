package com.miguel.minilauncher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

/**
 * Launcher minimo: fondo liso, reloj y una fila de mosaicos.
 * Sin librerias, sin servicios, sin animaciones, sin fondos de pantalla.
 */
class MainActivity : Activity() {

    private class Tile(val pkg: String, val label: String)

    // Edita esta lista para cambiar las apps del inicio.
    // Si agregas un paquete nuevo, agregalo tambien en <queries> del manifest.
    private val tiles = listOf(
        Tile("com.lite.fczx", "Xuper TV"),
        Tile("com.google.android.youtube.tv", "YouTube"),
        Tile("com.android.tv.settings", "Ajustes")
    )

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(0xFF0F1115.toInt()))

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(48), dp(32), dp(48), dp(32))
        }

        val clock = TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "HH:mm"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.END
        }
        root.addView(
            clock,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        root.addView(
            row,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        var first: View? = null
        for (t in tiles) {
            val v = buildTile(t)
            row.addView(
                v,
                LinearLayout.LayoutParams(dp(220), dp(180)).apply {
                    setMargins(dp(14), 0, dp(14), 0)
                }
            )
            if (first == null) first = v
        }

        setContentView(root)
        first?.requestFocus()
    }

    private fun rounded(fill: Int, stroke: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(14).toFloat()
            if (stroke != 0) setStroke(dp(3), stroke)
        }

    private fun buildTile(t: Tile): View {
        val pm = packageManager

        val img = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        try {
            img.setImageDrawable(pm.getApplicationBanner(t.pkg) ?: pm.getApplicationIcon(t.pkg))
        } catch (_: Exception) {
            // app no instalada: queda el mosaico sin imagen
        }

        val label = TextView(this).apply {
            text = t.label
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val bgNormal = rounded(0xFF1C2028.toInt(), 0)
        val bgFocus = rounded(0xFF2B3342.toInt(), 0xFF7CE38B.toInt())

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = bgNormal
            isFocusable = true
            isClickable = true
            addView(img, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(
                label,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
            )
            setOnFocusChangeListener { v, has ->
                v.background = if (has) bgFocus else bgNormal
                val s = if (has) 1.08f else 1f
                v.scaleX = s
                v.scaleY = s
            }
            setOnClickListener { launch(t.pkg) }
        }
    }

    private fun launch(pkg: String) {
        val pm = packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(pkg)
            ?: pm.getLaunchIntentForPackage(pkg)
            ?: if (pkg == "com.android.tv.settings") Intent(Settings.ACTION_SETTINGS) else null
        if (intent == null) {
            Toast.makeText(this, "No instalada: $pkg", Toast.LENGTH_SHORT).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir", Toast.LENGTH_SHORT).show()
        }
    }

    // Un launcher no debe cerrarse con Atras
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}
}
