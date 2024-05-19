package com.astro.dsoplanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.base.Point;

public class AutoLocation {
    private static final String TAG = AutoLocation.class.getSimpleName();
    private static boolean started = false;

    private static final int AUTO_LOCATION = 12;

    private static List<MyLocationListener> listeners = new ArrayList<>();

    public static boolean isStarted() {
        return started;
    }

    /**
     * @param context
     * @return true if successfully started updating
     */
    public static boolean start(Context context) {
        if (!started) {
            Log.d(TAG, "started");
            LocationManager manager = (LocationManager) Global.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            Log.d(TAG, "manager=" + manager);
            if (manager == null) return false;
            Criteria criteria = new Criteria();

            boolean isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "isNetworkEnabled=" + isNetworkEnabled);
            boolean isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "isGpsEnabled=" + isGpsEnabled);

            if (!isNetworkEnabled && !isGpsEnabled) return false;

            Location locLastKnownNetwork = null;
            try {
                locLastKnownNetwork = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                Log.d(TAG, "e=" + e);

            }
            Location locLastKnownGPS = null;
            try {
                locLastKnownGPS = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                Log.d(TAG, "e=" + e);
            }
            long timeNetwork = 0;
            if (locLastKnownNetwork != null) timeNetwork = locLastKnownNetwork.getTime();

            long timeGPS = 0;
            if (locLastKnownGPS != null) timeGPS = locLastKnownGPS.getTime();

            Location locLastKnown = null;
            String provider = "";
            if (timeNetwork == 0 && timeGPS == 0) {
                locLastKnown = null;
                provider = null;
            } else if (timeNetwork > timeGPS) {
                locLastKnown = locLastKnownNetwork;
                provider = LocationManager.NETWORK_PROVIDER;
            } else if (timeGPS >= timeNetwork) {
                locLastKnown = locLastKnownGPS;
                provider = LocationManager.GPS_PROVIDER;
            }


            Log.d(TAG, "location=" + locLastKnown);
            //we use either last known loc or current (from the listener)
            //if !current, update with last known
            //needed as could be started from several places in the program
            //thus if there is a current location it will not be overwritten
            //until a new location from listener arrives
            if (SettingsActivity.getSharedPreferences(context).getBoolean(Constants.GEO_LAST_KNOWN, true)) {
                if (locLastKnown != null) {
                    updateSharedPrefs(locLastKnown, provider, true, Calendar.getInstance());
                    Point.setLat(locLastKnown.getLatitude());
                    sendBroadcast();
                }
            }

            MyLocationListener listener = new MyLocationListener(LocationManager.NETWORK_PROVIDER);
            try {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, listener);
                listeners.add(listener);
            } catch (Exception e) {
                Log.d(TAG, "listener exception " + e);
                return false;
            }

            listener = new MyLocationListener(LocationManager.GPS_PROVIDER);
            try {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
                listeners.add(listener);
            } catch (Exception e) {
                Log.d(TAG, "listener exception " + e);
                return false;
            }

            SettingsActivity.putSharedPreferences(Constants.GEO_LAST_START, Calendar.getInstance().getTimeInMillis(), context);
            started = true;
            return true;

        }
        return true;
    }

    public static void stop(MyLocationListener listener) {
        LocationManager manager = (LocationManager) Global.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            manager.removeUpdates(listener);
            listeners.remove(listener);
            Log.d(TAG, "listener stopped " + listener);
            if (listeners.size() == 0) started = false;
        }
    }

    public static void stopAll() {

        started = false;
        LocationManager manager = (LocationManager) Global.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            for (MyLocationListener l : listeners) {
                manager.removeUpdates(l);
                Log.d(TAG, "listener stopped " + l);
            }
            listeners = new ArrayList<>();
        }

    }

    private static void sendBroadcast() {
        if (Global.getAppContext() != null) {
            Intent intent = new Intent(Constants.GEO_BROADCAST);
            LocalBroadcastManager.getInstance(Global.getAppContext()).sendBroadcast(intent);
        }
    }

    private static boolean isToUpdateLocation(String provider, Calendar c) {
        Context context = Global.getAppContext();
        long last_update_time = SettingsActivity.getSharedPreferences(context).getLong(Constants.GEO_LAST_UPDATE, -1);
        String last_update_provider = SettingsActivity.getStringFromSharedPreferences(context, Constants.GEO_PROVIDER, "");
        boolean last_known = SettingsActivity.getSharedPreferences(context).getBoolean(Constants.GEO_LAST_KNOWN, true);
        if (last_known) return true; //always override last known location

        long now = c.getTimeInMillis();
        if (provider.equals(LocationManager.NETWORK_PROVIDER) && last_update_provider.equals(LocationManager.GPS_PROVIDER) && (now - last_update_time < 100000)) // within 100 sec
            return false;
        else return true;

    }

    private static void updateSharedPrefs(Location location, String provider, boolean lastKnown, Calendar c) {
        if (Global.getAppContext() == null) return;
        double lat = location.getLatitude();
        double lon = location.getLongitude();


        SettingsActivity.putSharedPreferences(Constants.LATITUDE, lat, Global.getAppContext());
        SettingsActivity.putSharedPreferences(Constants.LONGITUDE, lon, Global.getAppContext());


        SettingsActivity.putSharedPreferences(Constants.GEO_LAST_KNOWN, lastKnown, Global.getAppContext());
        SettingsActivity.putSharedPreferences(Constants.GEO_PROVIDER, provider, Global.getAppContext());
        SettingsActivity.putSharedPreferences(Constants.GEO_LAST_UPDATE, c.getTimeInMillis(), Global.getAppContext());
        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, Global.getAppContext());
        SettingsActivity.putSharedPreferences(Constants.GEO_DETAILS_UPDATE, true, Global.getAppContext());
        Log.d(TAG, "updated shared prefs with " + location + " " + provider + " " + lastKnown);

    }


    private static class MyLocationListener implements LocationListener {

        private static final String MY_LOCATION_LISTENER_PROVIDER = "MyLocationListener [provider=";
        String provider;
        public MyLocationListener(String provider) {
            this.provider = provider;
            Log.d(TAG, "Listener for " + provider + "created");
        }

        @Override
        public String toString() {
            return MY_LOCATION_LISTENER_PROVIDER + provider + "]";
        }

        public void onLocationChanged(Location location) {
            Log.d(TAG, "OnLocationChanged:" + location + "provider=" + provider);
            if (!isToUpdateLocation(provider, Calendar.getInstance())) {
                stop(this);
                return;
            }
            Log.d(TAG, "update shared prefs");
            updateSharedPrefs(location, provider, false, Calendar.getInstance());
            Point.setLat(location.getLatitude());

            sendBroadcast();
            stop(this);
            if (Global.getAppContext() != null) {
                String s = Global.getAppContext().getString(R.string.location_updated_);
                InputDialog.toast(s, Global.getAppContext()).show();

            }
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}
