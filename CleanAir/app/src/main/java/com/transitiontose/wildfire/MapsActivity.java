package com.transitiontose.wildfire;

import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import android.view.View;
import com.google.android.gms.maps.model.Marker;
import android.widget.TextView;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // this block below is if we want to do a custom info window:

        /*mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                LatLng latLng = arg0.getPosition();
                TextView tv1 = (TextView) v.findViewById(R.id.textView1);
                TextView tv2 = (TextView) v.findViewById(R.id.textView2);
                TextView tv3 = (TextView) v.findViewById(R.id.textView3);
                tv1.setText("CO2: " + "x");
                tv2.setText("CO2: " + "y");
                tv3.setText("CO2: " + "z");
                return v;
            }
        });*/

        System.out.println("Requesting map with latitude " + MainActivity.latitude + " and longitude: " + MainActivity.longitude);
        LatLng userLocation = new LatLng(MainActivity.latitude, MainActivity.longitude); // user the latest device location coordinates
        mMap.addMarker(new MarkerOptions().position(userLocation).snippet("CO2: a, CO: b, O3: c, N02: d").title("Lat: " + userLocation.latitude + ", Lon: " + userLocation.longitude).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
    }
}
