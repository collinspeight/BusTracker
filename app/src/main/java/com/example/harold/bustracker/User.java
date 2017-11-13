package com.example.harold.bustracker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.example.harold.bustracker.BusInformationReceiver;
import com.example.harold.bustracker.BusInformationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class User extends AppCompatActivity implements OnMapReadyCallback{

    private BusInformationReceiver busInformationReceiver;
    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    private LatLng busStopLocation;
    private GoogleMap map;
    private int busStopNumber;
    private LatLng[] busStopsLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user);

        initializeBusStopsLocation();

        // Get bus stop number and location
        busStopNumber = getIntent().getIntExtra("BusStop", 0);
        busStopLocation = busStopsLocation[busStopNumber];

        // Create service receiver
        setupServiceReceiver();

        // Start service
        final Intent i = new Intent(this, BusInformationService.class);
        i.putExtra("receiver", busInformationReceiver);
        i.putExtra("adminMode", false);
        startService(i);

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();

        signOut = findViewById(R.id.logOut);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(i);
                mAuth.signOut();
                startActivity(new Intent(User.this, LoginActivity.class));
                finish();
            }
        });
    }

    /**
     * Creates a service receiver that will be notified of results of the BusInformationService
     * once the service receiver subscribes to the service.
     */
    public void setupServiceReceiver() {
        busInformationReceiver = new BusInformationReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        busInformationReceiver.setReceiver(new BusInformationReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    setBusInformation(new LatLng(resultData.getDouble("lat"), resultData.getDouble("lng")));
                    double eta = resultData.getDouble("ETA");
                    TextView textViewToUpdate = (TextView) findViewById(R.id.textView3);
                    textViewToUpdate.setText("Bus' ETA: " + eta);
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                map.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Create marker for bus stop
        map.addMarker(new MarkerOptions().position(busStopLocation).title("Selected Bus Stop"));
        map.moveCamera(CameraUpdateFactory.newLatLng(busStopLocation));

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }
    private void setBusInformation(LatLng latlng)
    {
        map.clear();
        map.addMarker(new MarkerOptions().position(latlng).title("Current Location of Bus"));
        map.addMarker(new MarkerOptions().position(busStopLocation).title("Selected Bus Stop"));
    }

    private void initializeBusStopsLocation()
    {
        // Hard coded bus stop locations map. Key: bus stop number, Value: location (lat, lng)
        busStopsLocation = new LatLng[5];
        busStopsLocation[0] = new LatLng(1,2);
        busStopsLocation[1] = new LatLng(3,4);
        busStopsLocation[2] = new LatLng(5,6);
        busStopsLocation[3] = new LatLng(7,8);
        busStopsLocation[4] = new LatLng(9,10);
    }
}
