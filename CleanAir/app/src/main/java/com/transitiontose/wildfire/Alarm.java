package com.transitiontose.wildfire;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.location.LocationManager;

public class Alarm extends BroadcastReceiver {

    static Context c;
    public LocationManager locationManager;
    public BackgroundLocationListener listener;
    static protected double co2ValueToConsiderDangerous;
    static protected double coValueToConsiderDangerous;
    static protected double o3ValueToConsiderDangerous;
    static protected double no2ValueToConsiderDangerous;
    public JSONArray root;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        c = context;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean userWantsNotifications = sp.getBoolean("notifications", true);
        if (userWantsNotifications) {
            wl.acquire();
            checkAir();
            wl.release();
        } else {

        }
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 30, pi); // Millisec * Second * Minute
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void checkAir() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        boolean userWantsNotifications = sp.getBoolean("notifications", true);

        if (userWantsNotifications) {
            locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            listener = new BackgroundLocationListener();

            try {
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
            } catch (SecurityException s) {
                System.out.println("User wants notifications, but does not have location services enabled to update location for service.");
            }

            // periodic action here (check the air data from Paul's API, temporarily just a toast for testing)
            if (isNetworkAvailable()) {
                checkNearbyNodesFromAPI();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void checkNearbyNodesFromAPI() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        double currentLatitude = sp.getFloat("currentLat", 14.0f);
        double currentLongitude = sp.getFloat("currentLon", 14.0f);
        co2ValueToConsiderDangerous = Double.parseDouble(sp.getString("CO2ET", "2500.0"));
        coValueToConsiderDangerous = Double.parseDouble(sp.getString("COET", "100.0"));
        o3ValueToConsiderDangerous = Double.parseDouble(sp.getString("O3ET", "0.1"));
        no2ValueToConsiderDangerous = Double.parseDouble(sp.getString("NO2ET", "0.05"));
        double minLat = currentLatitude - 1;
        double maxLat = currentLatitude + 1;
        double minLong = currentLongitude - 1;
        double maxLong = currentLongitude + 1;

        String url = "http://168.62.234.19/api/nodes?minLat=" + minLat + "&maxLat=" + maxLat + "&minLong=" + maxLong + "&maxLong=" + minLong;
        System.out.println("Executing downloadwebpage with values: " + minLat + " " + maxLat + " " + minLong + " " + maxLong);
        new DownloadWebpageTask().execute(url);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        boolean coIssued = false;
        boolean co2Issued = false;
        boolean o3Issued = false;
        boolean no2Issued = false;

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page.");
                return "Unable to retrieve web page.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                root = new JSONArray(result);
            } catch (org.json.JSONException j) {
                System.out.println("Root JSONArray creation failed.");
                return;
            }

            int rootLength = root.length();

            for(int i = 0; i < rootLength; i++) {
                JSONObject currentJSONObject = null;
                double coLevel = -1;
                double co2Level = -1;
                double o3Level = -1;
                double no2Level = -1;

                try {
                    currentJSONObject = root.getJSONObject(i);
                    coLevel = currentJSONObject.getDouble("co_Lvl");
                    co2Level = currentJSONObject.getDouble("co2_Lvl");
                    o3Level = currentJSONObject.getDouble("o3_Lvl");
                    no2Level = currentJSONObject.getDouble("no2_Lvl");

                } catch (JSONException j) {
                    System.out.println("Error making JSONObject from JSONArray element.");
                }

                ArrayList<String> list = new ArrayList<>();
                list.add(String.valueOf(co2Level));
                list.add(String.valueOf(coLevel));
                list.add(String.valueOf(o3Level));
                list.add(String.valueOf(no2Level));
                checkValsAndSendNotificationIfNecessary(list);
            }
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                //int response = conn.getResponseCode();
                is = conn.getInputStream();
                String contentAsString = convertStreamToString(is);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        private String convertStreamToString(InputStream is) {
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        private void checkValsAndSendNotificationIfNecessary(ArrayList<String> a) {
            //2500
            if (Double.parseDouble(a.get(0)) >= co2ValueToConsiderDangerous && co2Issued == false) {
                sendNotification("CO2", Double.parseDouble(a.get(0)), 0);
                co2Issued = true;
            }

            if (Double.parseDouble(a.get(1)) >= coValueToConsiderDangerous && coIssued == false) {
                sendNotification("CO", Double.parseDouble(a.get(1)), 1);
                coIssued = true;
            }

            if (Double.parseDouble(a.get(2)) >= o3ValueToConsiderDangerous && o3Issued == false) {
                sendNotification("O3", Double.parseDouble(a.get(2)), 2);
                o3Issued = true;
            }

            if (Double.parseDouble(a.get(3)) >= no2ValueToConsiderDangerous && no2Issued == false) {
                sendNotification("NO2", Double.parseDouble(a.get(3)), 3);
                no2Issued = true;
            }
        }

        // real method
        public void sendNotification(String gas, double gasValue, int notificationID) {
            Intent intent = new Intent(c, HeatmapActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
            builder.setSmallIcon(R.drawable.ic_stat_notification);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher));
            builder.setContentTitle("Air Quality Warning");
            builder.setContentText("Unhealthy levels of " + gas + " in your area.");
            builder.setSubText(gas + " is at " + gasValue + " ppm.");
            NotificationManager notificationManager = (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, builder.build());
        }
    }
}