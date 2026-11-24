package com.balgibi.reelscounter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.content.Context
import android.graphics.Color
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var total: TextView
    private lateinit var breakdown: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.rgb(247, 245, 250))
        }
        val title = TextView(this).apply { text = "ReelsCounter"; textSize = 28f; setTextColor(Color.rgb(32, 33, 42)); gravity = Gravity.CENTER }
        total = TextView(this).apply { textSize = 54f; setTextColor(Color.rgb(103, 80, 164)); gravity = Gravity.CENTER; setPadding(0, 22, 0, 0) }
        breakdown = TextView(this).apply { textSize = 16f; setTextColor(Color.DKGRAY); gravity = Gravity.CENTER; setPadding(0, 4, 0, 24) }
        val info = TextView(this).apply {
            text = "Instagram Reels ve TikTok akışında bir video 3 saniye algılandığında sayılır. Sayaç her gün 00.00'da sıfırlanır."
            textSize = 15f; setTextColor(Color.DKGRAY); gravity = Gravity.CENTER; setPadding(0, 8, 0, 24)
        }
        val accessibility = Button(this).apply { text = "1. Erişilebilirlik iznini aç"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        val overlay = Button(this).apply { text = "2. Üstte gösterme iznini aç"; setOnClickListener {
            if (!Settings.canDrawOverlays(this@MainActivity)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } }
        val refresh = Button(this).apply { text = "Sayacı yenile"; setOnClickListener { updateNumbers() } }
        val note = TextView(this).apply {
            text = "Not: Instagram/TikTok arayüzleri erişilebilirlik bilgilerini her ekranda sunmayabilir. Bu ilk sürüm algılamayı ekrandaki metin ve kaydırma olaylarından yapar; sonuçları gerçek telefonda test etmek gerekir."
            textSize = 12f; setTextColor(Color.GRAY); gravity = Gravity.CENTER; setPadding(0, 22, 0, 0)
        }
        listOf(title, total, breakdown, info, accessibility, overlay, refresh, note).forEach { v ->
            root.addView(v, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        setContentView(root)
        updateNumbers()
    }
    override fun onResume() { super.onResume(); updateNumbers() }
    private fun updateNumbers() {
        val (all, ig, tt) = CounterStore.values(this)
        total.text = all.toString()
        breakdown.text = "Instagram: $ig     TikTok: $tt"
    }
}
