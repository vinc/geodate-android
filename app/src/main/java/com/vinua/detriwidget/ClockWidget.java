package com.vinua.detriwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * Implementation of App Widget functionality.
 */
public class ClockWidget extends AppWidgetProvider {

    private static double longitude;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Intent alarmIntent = new Intent(context, ClockTickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);

        /*
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
        longitude = location.getLongitude();
        Log.d("Detri", String.format("Got longitude '%f'", longitude));

        String text = ClockTime.getTime(System.currentTimeMillis() / 1000, longitude);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
        views.setTextViewText(R.id.appwidget_text, text);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        */
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
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

