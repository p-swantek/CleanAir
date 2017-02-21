package com.transitiontose.wildfire;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class StartServiceOnBootReceiver extends BroadcastReceiver {
    Alarm alarm = new Alarm();
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean userWantsNotifications = sp.getBoolean("notifications", true);
        boolean userIsCollector = sp.getBoolean("sensorUploadBox", false);
        if (userWantsNotifications && !userIsCollector) {
            alarm.SetAlarm(context);
        }
    }
}