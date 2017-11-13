package com.example.harold.bustracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

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

public class Admin extends AppCompatActivity implements OnMapReadyCallback{

    private BusInformationReceiver busInformationReceiver;
    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    private LatLng busStop;
    private GoogleMap map;
    private LatLng[] busStopsLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin);

        // Create service receiver
        setupServiceReceiver();

        // Start service
        final Intent i = new Intent(this, BusInformationService.class);
        i.putExtra("receiver", busInformationReceiver);
        i.putExtra("adminMode", true);
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
                startActivity(new Intent(Admin.this, LoginActivity.class));
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
                    setBusInformation(resultData);
                }
            }
        });
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

        // TODO set the bus stop from intent sent from the main activity

        // Temp bus stop of UCF's lat long until set from main activity
        busStop = new LatLng(28.6024, -81.2000);
        map.addMarker(new MarkerOptions().position(busStop).title("Selected Bus Stop"));
        map.moveCamera(CameraUpdateFactory.newLatLng(busStop));
    }
    private void setBusInformation(Bundle resultData)
    {
        // Clear previous markers from map
        map.clear();

        // Generate markers for all supported buses
        //map.addMarker(new MarkerOptions().position(new LatLng(11,12)));
        //map.addMarker(new MarkerOptions().position(new LatLng(13,14)));
        //map.addMarker(new MarkerOptions().position(new LatLng(15,16)));
        map.addMarker(new MarkerOptions().position(new LatLng(
                resultData.getDouble("lat1"),resultData.getDouble("lng1"))).title("Bus #1"));
        map.addMarker(new MarkerOptions().position(new LatLng(
                resultData.getDouble("lat2"),resultData.getDouble("lng2"))).title("Bus #2"));
        map.addMarker(new MarkerOptions().position(new LatLng(
                resultData.getDouble("lat3"),resultData.getDouble("lng3"))).title("Bus #3"));


        // Generate markers for all supported bus stops
        initializeBusStopsLocation();
        map.addMarker(new MarkerOptions().position(busStopsLocation[0]).title("Bus Stop #1234"));
        map.addMarker(new MarkerOptions().position(busStopsLocation[1]).title("Bus Stop #2345"));
        map.addMarker(new MarkerOptions().position(busStopsLocation[2]).title("Bus Stop #3456"));
        map.addMarker(new MarkerOptions().position(busStopsLocation[3]).title("Bus Stop #4567"));
        map.addMarker(new MarkerOptions().position(busStopsLocation[4]).title("Bus Stop #5678"));
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
