package com.astro.dsoplanner;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;

import java.io.File;

public class Tools {
    static boolean initialized = false;

    static void initialize() {
        if (!initialized) {
            //fake GPS
            //Requires in the Manifest:
            //<uses-permission android:name="android.permission.ACCESS_MOCK_LOCATION"/>
            LocationManager locationManager = (LocationManager) Global.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            String mocLocationProvider = LocationManager.GPS_PROVIDER;
            locationManager.addTestProvider(mocLocationProvider, false, false,
                    false, false, true, true, true, 0, 5);
            locationManager.setTestProviderEnabled(mocLocationProvider, true);

            //other tools goes here

            initialized = true;
        }
    }

    //Returns
    static Location fakeLocation() {
        initialize();

        String mocLocationProvider = LocationManager.GPS_PROVIDER;

        Location location = new Location(mocLocationProvider);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(0.0);
        location.setLongitude(0.0);
        location.setAltitude(0.0);

        return location;
    }
}
