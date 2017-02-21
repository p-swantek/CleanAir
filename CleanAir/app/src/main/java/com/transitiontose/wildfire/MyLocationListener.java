package com.transitiontose.wildfire;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Context;

public class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        //System.out.println("onLocationChanged called");

        // put values into persistent preferences:
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("currentLat", Float.parseFloat(String.valueOf(location.getLatitude())));
        editor.putFloat("currentLon", Float.parseFloat(String.valueOf(location.getLongitude())));
        editor.commit();

        MainActivity.latitude = location.getLatitude();
        MainActivity.longitude = location.getLongitude();
        //System.out.println("latitude changed to: " + location.getLatitude());
        //System.out.println("longitude changed to: " + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
