package com.balgibi.reelscounter

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class CounterWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) render(context, manager, id)
    }
    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, CounterWidgetProvider::class.java))
            for (id in ids) render(context, manager, id)
        }
        private fun render(context: Context, manager: AppWidgetManager, id: Int) {
            val (total, ig, tt) = CounterStore.values(context)
            val views = RemoteViews(context.packageName, R.layout.widget_counter)
            views.setTextViewText(R.id.widget_total, total.toString())
            views.setTextViewText(R.id.widget_breakdown, "Instagram $ig  •  TikTok $tt")
            manager.updateAppWidget(id, views)
        }
    }
}
