package com.balgibi.reelscounter

import android.content.Context
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CounterStore {
    private const val PREFS = "reels_counter"
    private const val KEY_DATE = "date"
    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    @Synchronized private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    @Synchronized fun values(context: Context): Triple<Int, Int, Int> {
        val p = prefs(context)
        if (p.getString(KEY_DATE, "") != today()) p.edit().clear().putString(KEY_DATE, today()).apply()
        val ig = p.getInt("instagram", 0)
        val tt = p.getInt("tiktok", 0)
        return Triple(ig + tt, ig, tt)
    }
    @Synchronized fun increment(context: Context, app: String) {
        val p = prefs(context)
        if (p.getString(KEY_DATE, "") != today()) p.edit().clear().putString(KEY_DATE, today()).apply()
        val key = if (app == "instagram") "instagram" else "tiktok"
        p.edit().putInt(key, p.getInt(key, 0) + 1).apply()
        CounterWidgetProvider.updateAll(context)
        CounterAccessibilityService.refreshOverlay(context)
    }
}
