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

public class User extends AppCompatActivity implements OnMapReadyCallback{

    private BusInformationReceiver busInformationReceiver;
    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    private LatLng busStop;
    private GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user);

        // Create service receiver
        setupServiceReceiver();

        // Start service
        Intent i = new Intent(this, BusInformationService.class);
        i.putExtra("receiver", busInformationReceiver);
        startService(i);

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();

        signOut = findViewById(R.id.logOut);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    private void setBusInformation(LatLng latlng)
    {
        // Clear previous markers from map
        map.clear();

        // Generate marker for the bus stop and current location of closest bus
        map.addMarker(new MarkerOptions().position(busStop).title("Selected Bus Stop"));
        map.addMarker(new MarkerOptions().position(latlng).title("Current Location of Bus"));
        map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }
}
