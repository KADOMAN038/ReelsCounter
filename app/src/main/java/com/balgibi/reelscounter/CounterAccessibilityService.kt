package com.balgibi.reelscounter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

class CounterAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var activeApp: String? = null
    private var candidateSince = 0L
    private var countedThisDwell = false
    private var overlay: TextView? = null
    private var windowManager: WindowManager? = null

    private val checker = object : Runnable {
        override fun run() {
            val root = rootInActiveWindow
            val detected = detectFeed(root, activeApp)
            if (detected == null) {
                activeApp = null; candidateSince = 0L; countedThisDwell = false
                hideOverlay()
            } else {
                if (activeApp != detected) {
                    activeApp = detected; candidateSince = System.currentTimeMillis(); countedThisDwell = false
                }
                showOverlay()
                if (!countedThisDwell && System.currentTimeMillis() - candidateSince >= 3000L) {
                    CounterStore.increment(this@CounterAccessibilityService, detected)
                    countedThisDwell = true
                }
                updateOverlayText()
            }
            handler.postDelayed(this, 500L)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        instance = this
        handler.post(checker)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED &&
            (pkg == "com.instagram.android" || pkg == "com.zhiliaoapp.musically" || pkg == "com.ss.android.ugc.trill")) {
            candidateSince = System.currentTimeMillis()
            countedThisDwell = false
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (pkg != "com.instagram.android" && pkg != "com.zhiliaoapp.musically" && pkg != "com.ss.android.ugc.trill") {
                activeApp = null; candidateSince = 0L; countedThisDwell = false; hideOverlay()
            }
        }
    }

    private fun detectFeed(root: AccessibilityNodeInfo?, current: String?): String? {
        if (root == null) return null
        val pkg = root.packageName?.toString() ?: current ?: return null
        val app = when (pkg) {
            "com.instagram.android" -> "instagram"
            "com.zhiliaoapp.musically", "com.ss.android.ugc.trill" -> "tiktok"
            else -> return null
        }
        val words = StringBuilder()
        collectText(root, words, 0)
        val text = words.toString().lowercase()
        val hasLike = listOf("like", "beğen", "liked").any { text.contains(it) }
        val hasComment = listOf("comment", "yorum").any { text.contains(it) }
        val hasShare = listOf("share", "paylaş", "send").any { text.contains(it) }
        val hasFeedMarker = if (app == "instagram") {
            text.contains("reels") || text.contains("reel") || text.contains("remix")
        } else {
            listOf("for you", "following", "sana özel", "takip").any { text.contains(it) }
        }
        return if (hasLike && hasComment && hasShare && hasFeedMarker) app else null
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: StringBuilder, depth: Int) {
        if (node == null || depth > 18 || out.length > 12000) return
        node.text?.let { out.append(it).append(' ') }
        node.contentDescription?.let { out.append(it).append(' ') }
        for (i in 0 until node.childCount) collectText(node.getChild(i), out, depth + 1)
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this) || overlay != null) return
        val view = TextView(this).apply {
            setTextColor(Color.WHITE); textSize = 13f; setPadding(14, 9, 14, 9)
            setBackgroundColor(Color.rgb(38, 38, 48)); elevation = 8f
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 14; y = 70 }
        try { windowManager?.addView(view, params); overlay = view; updateOverlayText() } catch (_: Exception) { }
    }

    private fun updateOverlayText() {
        val (total, ig, tt) = CounterStore.values(this)
        overlay?.text = "Reels $total  |  IG $ig · TT $tt"
    }

    private fun hideOverlay() {
        val view = overlay ?: return
        try { windowManager?.removeView(view) } catch (_: Exception) { }
        overlay = null
    }

    override fun onInterrupt() { }
    override fun onDestroy() {
        handler.removeCallbacks(checker); hideOverlay(); instance = null; super.onDestroy()
    }

    companion object {
        @Volatile private var instance: CounterAccessibilityService? = null
        fun refreshOverlay(context: Context) { instance?.updateOverlayText() }
    }
}
