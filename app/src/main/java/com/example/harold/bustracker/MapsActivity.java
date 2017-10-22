package com.example.harold.bustracker;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private BusInformationReceiver busInformationReceiver;
    private LatLng busStop;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Create service receiver
        setupServiceReceiver();

        // Start service
        Intent i = new Intent(this, BusInformationService.class);
        i.putExtra("receiver", busInformationReceiver);
        startService(i);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // TODO set the bus stop from intent sent from the main activity

        // Temp bus stop of UCF's lat long until set from main activity
        busStop = new LatLng(28.6024, -81.2000);
        map.addMarker(new MarkerOptions().position(busStop).title("Selected Bus Stop"));
        map.moveCamera(CameraUpdateFactory.newLatLng(busStop));
    }

    /**
     * Generates a marker for the bus stop and current location of closest bus
     * @param latlng The latitude and longitude of the bus
     */
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
