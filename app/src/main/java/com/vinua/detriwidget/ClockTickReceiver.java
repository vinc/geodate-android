package com.vinua.detriwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ClockTickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("Detri", "Received broadcast");

        long nextTick = updateClockWidget(context);

        Intent alarmIntent = new Intent(context, ClockTickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + nextTick, pendingIntent);
    }

    private long updateClockWidget(Context context) {
        double longitude = new ClockLocation(context).getLongitude();

        ClockTime clockTime = new ClockTime(System.currentTimeMillis() / 1000, longitude, false);
        String text = clockTime.toString();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
        views.setTextViewText(R.id.appwidget_text, text);

        //Log.d("Detri", "Updating the widget: " + text);

        // Instruct the widget manager to update the widget
        ComponentName clockWidget = new ComponentName(context, ClockWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(clockWidget, views);

        return clockTime.nextTick();
    }
}
