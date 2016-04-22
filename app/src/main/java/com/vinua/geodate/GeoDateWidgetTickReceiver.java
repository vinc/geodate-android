package com.vinua.geodate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class GeoDateWidgetTickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("Detri", "Received broadcast");

        // TODO: Add method GeoDateWidget.getNextTick()
        long nextTick = updateGeoDateWidget(context);

        Intent alarmIntent = new Intent(context, GeoDateWidgetTickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + nextTick, pendingIntent);
    }

    private long updateGeoDateWidget(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_geodate);
        String text;
        long nextTick;
        try {
            double longitude = new GeoLocation(context).getLongitude();
            GeoDate geoDate = new GeoDate(System.currentTimeMillis() / 1000, longitude, true);
            text = geoDate.toString(GeoDate.ClockFormat.CC);
            nextTick = geoDate.nextTick();
        } catch (GeoLocation.LocationNotFoundException e) {
            // Could not find location, wait before retrying
            text = "00";
            nextTick = 10000; // FIXME: Find how long should we wait
        }

        views.setTextViewText(R.id.appwidget_text, text);

        //Log.d("Detri", "Updating the widget: " + text);

        // Instruct the widget manager to update the widget
        ComponentName geoDateWidget = new ComponentName(context, GeoDateWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(geoDateWidget, views);

        return nextTick;
    }
}
