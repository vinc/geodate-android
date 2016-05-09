package com.vinua.geodate;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class GeoDateWidget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Start updating the clock
        Intent alarmIntent = new Intent(context, GeoDateWidgetTickReceiver.class);
        PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingAlarmIntent);

        // Show main activity on click
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_geodate);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingMainActivityIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds); // FIXME: Remove this
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        // Get widget size
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int size = 48;
        if (minHeight > 300 && minWidth > 300) {
            size = 256;
        } else if (minHeight > 200) {
            size = 128;
        } else if (minHeight > 100) {
            size = 96;
        }

        // Change font size accordingly
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_geodate);
        views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_SP, size);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

