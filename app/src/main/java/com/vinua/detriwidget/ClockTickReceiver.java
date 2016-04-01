package com.vinua.detriwidget;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class ClockTickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long nextTick = updateClockWidget(context);

        Intent alarmIntent = new Intent(context, ClockTickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + nextTick, pendingIntent);
    }

    private long updateClockWidget(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        String locationProvider = LocationManager.GPS_PROVIDER;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("Detri", "No permission to read location");
            //return;
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        double longitude = location.getLongitude();
        Log.d("Detri", String.format("Got longitude '%f'", longitude));

        ClockTime clockTime = new ClockTime(System.currentTimeMillis() / 1000, longitude);
        String text = clockTime.toString();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
        views.setTextViewText(R.id.appwidget_text, text);

        // Instruct the widget manager to update the widget
        ComponentName clockWidget = new ComponentName(context, ClockWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(clockWidget, views);

        return clockTime.nextTick();
    }
}
