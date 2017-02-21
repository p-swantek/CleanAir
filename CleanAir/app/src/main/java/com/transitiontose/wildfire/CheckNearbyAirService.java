package com.transitiontose.wildfire;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CheckNearbyAirService extends Service {

    /** Called when the service is being created. */
    public static Alarm alarm = new Alarm();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Let it continue running until it is stopped.
        //checkAir();
        alarm.SetAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        alarm.SetAlarm(this);
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}