package com.vinua.detriwidget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private TextView clockText;
    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            String locationProvider = LocationManager.GPS_PROVIDER;

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Detri", "No permission to read location");
                //return;
            }

            Location location = locationManager.getLastKnownLocation(locationProvider);
            double longitude = location.getLongitude();

            ClockTime clockTime = new ClockTime(System.currentTimeMillis() / 1000, longitude, true);
            String text = clockTime.toString();

            clockText.setText(text);

            Log.d("Detri", "Updating the main activity: " + text);

            handler.postDelayed(textRunnable, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clockText = (TextView) findViewById(R.id.textView);
        handler.postDelayed(textRunnable, 1000);
    }
}
