package com.transitiontose.wildfire;

import android.bluetooth.BluetoothAdapter;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.widget.EditText;
import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.*;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import android.os.AsyncTask;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.app.PendingIntent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.os.*;
import java.io.*;
import java.net.*;
import android.widget.*;
import android.widget.Toast;
import android.content.*;
import android.view.*;
import org.json.*;
import java.util.*;
import android.graphics.*;
import android.text.TextUtils;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.app.AlertDialog;

/*

How to test location:  you need to connect to the emulator via Telnet.  Then you can set geolacation fix in the console.

1. Open command line and type:
2. telnet localhost 5554
3. Now you can pass it latitude and longitude, by entering:
4. geo fix 42 -87
5. This will make the emulator simulate the location of the coordinates you just entered.
6. You can change these values while the app is running and test that the map reacts correctly to the changes.

- Terry

 */

public class MainActivity extends AppCompatActivity {

    static protected boolean userIsCollector;
    static protected boolean userWantsNotifications;
    static protected double co2ValueToConsiderDangerous;
    static protected double coValueToConsiderDangerous;
    static protected double o3ValueToConsiderDangerous;
    static protected double no2ValueToConsiderDangerous;
    static protected double latitude = 10.0;
    static protected double longitude = -10.0;
    static private LocationManager locManager;
    static private LocationListener listener;
    static protected Location lastKnownLocation;
    static protected EditText usernameField;
    static protected EditText passwordField;
    static protected EditText usernameRegisterField;
    static protected EditText passwordRegisterField;
    static protected EditText emailRegisterField;
    static protected TextView statusText;
    static protected Button loginButton;
    static protected Button registerButton;
    static protected ImageView image;
    private static final String TAG = "MainActivity"; //tag for Log messages
    private static final int REQUEST_ENABLE_BT = 1; //int code representing the request to enable the bluetooth
    //private static final String TEST_MAC = "00:07:80:05:90:FD"; //for testing connecting to specific MAC (this is our waspmote bluetooth module's mac address)
    private BluetoothAdapter btAdapter;  //reference to bluetooth adapter on the android device
    private BluetoothGatt btGatt;  //reference to GATT server on waspmote's BLE
    private Handler handler = new Handler();
    private static final long SCAN_PERIOD = 20000; //scan time for LE scan is 20 seconds
    private static final UUID USER_SERVICE1 = UUID.fromString("eed82c0a-b1c2-401e-ae4a-afac80c80c72"); //UUID of the user service 1 profile on waspmote (arbitrarily chosen, UUID gained from documentation)
    private static final UUID CHARACTERISTIC = UUID.fromString("Be39a5dc-048b-4b8f-84cb-94c197edd26e"); //UUID of the characteristic that waspmote will write to (arbitrarily chosen, UUID gained from documentation)
    private static final UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");// UUID for the BLE client(this app) characteristic which is necessary for notifications.
    private static final String WEB_SERVER = "http://168.62.234.19/api/nodes";  //web server address for http posts
    private static final String[] USER_IDS = {"6", "7", "8"};  //test userIds for our 3 waspmote boards
    private String currUser = "6"; //default userId is user 6 if one isn't specified otherwise
    public static Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        usernameField = (EditText)findViewById(R.id.usernameField);
//        passwordField = (EditText)findViewById(R.id.passwordField);
//        usernameRegisterField = (EditText)findViewById(R.id.usernameRegisterField);
//        passwordRegisterField = (EditText)findViewById(R.id.passwordRegisterField);
//        emailRegisterField = (EditText)findViewById(R.id.emailRegisterField);
//        loginButton = (Button)findViewById(R.id.loginButton);
//        registerButton = (Button)findViewById(R.id.registerButton);
        statusText = (TextView)findViewById(R.id.statusTextView);
        image = (ImageView)findViewById(R.id.image);
        c = getApplicationContext();

        /*** code for test user ID selection spinner, will remove ***/

//        Spinner idSelector = (Spinner)findViewById(R.id.userid_selection);
        ArrayAdapter<String> userIdAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, USER_IDS);
        userIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        idSelector.setSelection(0);
//        idSelector.setAdapter(userIdAdapter);
//        idSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                currUser = USER_IDS[position];
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });

        /*** end spinner code ***/


        BluetoothManager bluetoothManager = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter(); //get a reference to the device's bluetooth adapter

        loadPreferences();
        if (isLocationEnabled(c) == false) {
            showSettingsAlert();
            //Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivity(gpsOptionsIntent);
        }

        initializeLocationData();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadPreferences(); // this will load any changes user made to the settings values
        checkBluetooth();  //when preferences are loaded in, check the bluetooth properties
        //checkIfNotCollector(); // method to start the work process needed for users who aren't collectors
        if (userWantsNotifications && !userIsCollector && isServiceRunning(CheckNearbyAirService.class) == false) {
            startService();
        }

        if (userWantsNotifications == false || userIsCollector == true) {
            if (isServiceRunning(CheckNearbyAirService.class) == true) {
                stopService(new Intent(getBaseContext(), CheckNearbyAirService.class));
                CheckNearbyAirService.alarm.CancelAlarm(this);
            }
        }
    }

    // Method to start the service
    public void startService() {
        startService(new Intent(getBaseContext(), CheckNearbyAirService.class));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void initializeLocationData() {
            this.locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            this.listener = new MyLocationListener();
            //this.lastKnownLocation = null;

            try {
                lastKnownLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //CheckNearbyAirService.lastKnownLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    latitude = lastKnownLocation.getLatitude();
                    longitude = lastKnownLocation.getLongitude();
                }
                locManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

            } catch (SecurityException e) {
                System.out.println("Security exception when trying to get first location");
            }

            System.out.println("Latitude after initialize: " + latitude);
            System.out.println("Longitude after initialize: " + longitude);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Location must be enabled.");
        alertDialog.setMessage("Please press OK to go to location settings.");
        alertDialog.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        alertDialog.show();
    }

    private void loadPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        userIsCollector = sp.getBoolean("sensorUploadBox", false); // will use the value from settings if it exists, otherwise uses the default value
        userWantsNotifications = sp.getBoolean("notifications", true); // will use the value from settings if it exists, otherwise uses the default value
        if (userIsCollector) {
            statusText.setText("App mode: collecting and uploading data.");
            image.setImageDrawable(getResources().getDrawable(R.drawable.upload));
        } else if (!userIsCollector && userWantsNotifications) {
            statusText.setText("App mode: viewing data and receiving notifications.");
            image.setImageDrawable(getResources().getDrawable(R.drawable.viewing));
        } else if (!userIsCollector && !userWantsNotifications) {
            statusText.setText("App mode: viewing data, no notifications.");
            image.setImageDrawable(getResources().getDrawable(R.drawable.viewing));
        }
        co2ValueToConsiderDangerous = Double.parseDouble(sp.getString("CO2ET", "2500.0"));
        coValueToConsiderDangerous = Double.parseDouble(sp.getString("COET", "100.0"));
        o3ValueToConsiderDangerous = Double.parseDouble(sp.getString("O3ET", "0.1"));
        no2ValueToConsiderDangerous = Double.parseDouble(sp.getString("NO2ET", "0.05"));
        System.out.println("CO2: " + co2ValueToConsiderDangerous);
        System.out.println("CO: " + coValueToConsiderDangerous);
        System.out.println("O3: " + o3ValueToConsiderDangerous);
        System.out.println("NO2: " + no2ValueToConsiderDangerous);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // called when user selects an option from the 3-dot menu:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) { // settings activity
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_map) { // map activity
            // below is the google map activity if needed in the future:
            //Intent i = new Intent(this, MapsActivity.class);

            // start activity that will show tony's map:
            Intent i = new Intent(this, CleanAirMapActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.heatmap) {
            Intent i = new Intent(this, HeatmapActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//
//    public void loginButtonPressed(View v) {
//        // grab values from the usernameEditText and passwordEditText
//        String username = usernameField.getText().toString();
//        String password = passwordField.getText().toString();
//
//        if (username.length() < 1 || password.length() < 1) {
//            // error... username and password can't be empty
//            Toast.makeText(this, "Login failure.  Check length of username and password.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        // make async http post here
//
//
//
//
//
//        System.out.println("Login pressed");
//    }

//    public void registerButtonPressed(View v) {
//        // grab values from the usernameEditText and passwordEditText
//        String username = usernameRegisterField.getText().toString();
//        String password = passwordRegisterField.getText().toString();
//        String email = emailRegisterField.getText().toString();
//
//        if (username.length() < 1 || password.length() < 1 || email.length() < 1) {
//            // error... username and password can't be empty
//            Toast.makeText(this, "Registration failure.  Check length of username, password and email.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        if (isEmailValid(email) == false) {
//            Toast.makeText(this, "Registration failure.  Invalid e-mail address.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        // make async http post here
//
//
//
//
//
//        System.out.println("Register pressed");
//    }

    static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    static boolean isEven(int n) {
        return n % 2 == 0;
    }

    @Override
    public void onDestroy() {

        if (btGatt != null) {
            btGatt.close();
            btGatt = null;
            Log.i(TAG, "Connection to server was closed.");
        }

        super.onDestroy();

    }

    /*
        Handle the result of asking the user to enable the bluetooth adapter, displays a toast displaying
        if the adapter was successfully turned on or not

     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {

            switch (resultCode){

                case RESULT_OK: //User selected to turn on bluetooth
                    Toast.makeText(this, "Bluetooth has been successfully enabled.", Toast.LENGTH_SHORT).show();
                    break;

                case RESULT_CANCELED: //User declined to turn on bluetooth, inform that data collection can't be done without bluetooth
                    Toast.makeText(this, "Note: bluetooth needed in order to collect data.", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    /*
        Check the status of the bluetooth adapter on the device.  If the device supports bluetooth, check to see if a user
        is a collector and turn the bluetooth on if true

     */

    private void checkBluetooth(){

        if (btAdapter != null){ //if the device can even use bluetooth
            if (!btAdapter.isEnabled() && userIsCollector){ //if the user wants to collect but bluetooth is off, prompt to turn it on
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }
            else if ((btAdapter.isEnabled() && userIsCollector) && btGatt == null){ //if you have bluetooth on and you want to collect, try scanning for the waspmote only if you haven't connected to it's GATT yet
                //Connect to the GATT server to get characteristics
                //waspmote = btAdapter.getRemoteDevice(TEST_MAC); //get a reference to the bluetooth device
                scanLeDevice(true);
            }
        }

        else{
            Toast.makeText(this, "The current device doesn't support bluetooth functionality.", Toast.LENGTH_SHORT).show();
        }
    }


    // real method
    public void sendNotification(String gas, double gasValue, int notificationID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_notification);
        builder.setAutoCancel(true);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        builder.setContentTitle("Air Quality Warning");
        builder.setContentText("Your sensors reveal unhealthy levels of " + gas + " .");
        builder.setSubText(gas + " is at " + gasValue + " ppm.");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, builder.build());
    }

    private void checkValsAndSendNotificationIfNecessary(ArrayList<String> a) {
        //2500
        if (Double.parseDouble(a.get(0)) >= co2ValueToConsiderDangerous) {
            sendNotification("CO2", Double.parseDouble(a.get(0)), 0);
        }

        if (Double.parseDouble(a.get(1)) >= coValueToConsiderDangerous) {
            sendNotification("CO", Double.parseDouble(a.get(1)), 1);
        }

        if (Double.parseDouble(a.get(2)) >= o3ValueToConsiderDangerous) {
            sendNotification("O3", Double.parseDouble(a.get(2)), 2);
        }

        if (Double.parseDouble(a.get(3)) >= no2ValueToConsiderDangerous) {
            sendNotification("NO2", Double.parseDouble(a.get(3)), 3);
        }
    }

    /*
        Implementation of callback methods to receive notifications of when characteristics are changed
     */
    private BluetoothGattCallback btGattCallback = new BluetoothGattCallback() {


        private ArrayList<String> values = new ArrayList<>(); //holds the values needed for the HTTP POST

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "Characterstic was changed!");
            Log.i(TAG, "Characterstic's current value: " + characteristic.getStringValue(0));

            values.add(characteristic.getStringValue(0)); //get the data point
            if (values.size() == 5){ //once we have all 5 items(4 gas concentrations and the current battery), perform the HTTP POST, then clear out the list for the next 5 values
                checkValsAndSendNotificationIfNecessary(values);
                String result = "";
                for (String val : values)
                    result += " " + val;
                Log.i(TAG, "Values array just got filled with 5 data items");
                Log.i(TAG, "Contents of the list: " + result);
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show(); // do a toast to show the user the gas values collected
                new AsyncHttpPost(values).execute(WEB_SERVER);  //start background task to do the post with the list of data values
                values.clear(); //clear out the list for the next round of data


                Log.i(TAG, "value list cleared, waiting to refill");
                Log.i(TAG, "Size of value list, should be 0. Is actually: " + values.size());
            }


        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Successfully connected to: " + gatt.getDevice().toString());
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(getApplicationContext(), "Successfully connected to the waspmote!", Toast.LENGTH_SHORT).show();
                    }
                });

                if (!gatt.discoverServices()) {
                    Log.i(TAG, "Failed to start discovering services!");
                }

            }
            else
                Log.i(TAG, "Failed to connect to: " + gatt.getDevice().toString());

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Service discovery completed!");
            }
            else {
                Log.i(TAG, "Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            BluetoothGattCharacteristic data = gatt.getService(USER_SERVICE1).getCharacteristic(CHARACTERISTIC);

            // Setup notifications on  characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(data, true)) {
                Log.i(TAG, "Couldn't set notifications for data characteristic!");
            }
            // Next update the characteristic's client descriptor to enable notifications.
            if (data.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = data.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    Log.i(TAG, "Couldn't write data client descriptor value!");
                }
            }
            else {
                Log.i(TAG, "Couldn't get data client descriptor!");
            }

        }
    };


   /*
    Callback for the result of performing an LE scan, will connect to our waspmote's GATT server when it is discovered
    */

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            btGatt = device.connectGatt(getApplicationContext(), true, btGattCallback); //connect to the GATT server on that device

        }
    };



    /*
        Scans for our waspmote device
     */
    private void scanLeDevice(boolean enable) {
        Log.i(TAG,"in scanLeDevice");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btAdapter.stopLeScan(scanCallback);

                }
            }, SCAN_PERIOD);

            Log.i(TAG,"Scanning Done");

            btAdapter.startLeScan(scanCallback);  //do a LE scan for the waspmote
        } else {
            btAdapter.stopLeScan(scanCallback);
        }

    }


    /*
        AsyncTask to handle making the HTTP POST of the data obtained from the waspmote to the server
     */
    public class AsyncHttpPost extends AsyncTask<String, Void, Integer> {
        private Map<String, String> mData = new HashMap<>();// post data

        /**
         * constructor, sets up the data values with their respective attributes. gets the current location data as well
         */
        public AsyncHttpPost(final List<String> data) {

            /*
            handler.post(new Runnable(){
                @Override
                public void run() {
                    initializeLocationData(); //update the lat and long to the current location when starting to make the post
                }
            });
            */

            //display the current sensor data in a toast
            handler.post(new Runnable(){
                @Override
                public void run() {
                    StringBuilder result = new StringBuilder();
                    result.append("Current Sensor Readings:\n");
                    result.append("CO2 ppm: " + data.get(0) + "\n");
                    result.append("CO ppm: " + data.get(1) + "\n");
                    result.append("O3 ppm: " + data.get(2) + "\n");
                    result.append("NO2 ppm: " + data.get(3) + "\n");
                    result.append("Battery % remaining: " + data.get(4) + "\n");
                    Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_SHORT).show(); // do a toast to show the user the gas values collected
                }
            });

            //put the required data in a hashmap
            mData.put("co2_Lvl", data.get(0));
            mData.put("co_Lvl", data.get(1));
            mData.put("o3_Lvl", data.get(2));
            mData.put("no2_Lvl", data.get(3));
            mData.put("lat", String.valueOf(latitude));
            mData.put("long", String.valueOf(longitude));
            mData.put("userId", currUser); //userID used is result of which one was selected from the spinner

            Log.i(TAG, "Current latitude: " + latitude);
            Log.i(TAG, "Current longitude: " + longitude);
            Log.i(TAG, "Size of mapping of attributes to values: " + mData.size());

        }

        /**
         * make a post using the obtained data
         */
        @Override
        protected Integer doInBackground(String... params) {
            Log.i(TAG, "doInBackground() starting, about to do the HTTP post.");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);// in this case, params[0] is URL
            post.setHeader(HTTP.CONTENT_TYPE,"application/x-www-form-urlencoded");

            int result = 0;
            try {
                // set up post data
                ArrayList<NameValuePair> nameValuePair = new ArrayList<>();

                for (String key : mData.keySet()) {
                    nameValuePair.add(new BasicNameValuePair(key, mData.get(key)));
                }

                //do the post and get the result
                post.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();
                result = statusLine.getStatusCode();

            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.w(TAG, "UnsupportedEncodingException thrown during the post");
            }
            catch (Exception e) {
                Log.w(TAG, "Exception thrown during the post");
            }
            return result;  //return the status code after doing the push
        }

        /**
         * on getting result, make a toast depending on the status code received after doing push
         */
        @Override
        protected void onPostExecute(Integer result) {

            switch (result){
                case HttpURLConnection.HTTP_CREATED:
                    Toast.makeText(getApplicationContext(), "A data reading has just been successfully posted to the server!", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "HTTP post was successfully created");
                    break;

                case HttpURLConnection.HTTP_BAD_REQUEST:
                    Toast.makeText(getApplicationContext(), "Data pushing failed: Bad Request", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "HTTP failed with a Bad Return");


                default:
                    Toast.makeText(getApplicationContext(), "Data pushing has failed", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Pushing failed.");
            }
        }
    }
}