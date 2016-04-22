package com.vinua.geodate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class GeoLocation {
    public class LocationNotFoundException extends Exception {
    }

    private double longitude;

    public GeoLocation(Context context) throws LocationNotFoundException {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

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

        String locationProvider = LocationManager.GPS_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location == null) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(locationProvider);
        }
        if (location == null) {
            Log.e("GeoDate", "No location found");
            throw new LocationNotFoundException();
        }

        longitude = location.getLongitude();
        //Log.d("GeoDate",String.format("Location provider: '%s'", locationProvider));
        //Log.d("GeoDate", String.format("Got longitude '%f'", longitude));
    }

    // NOTE: Can return null
    public double getLongitude() {
        return longitude;
    }
}
