package com.example.harold.bustracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class User extends AppCompatActivity implements OnMapReadyCallback{

    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user);
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
        //Bus Stop 1
        LatLng stop1 = new LatLng(28.597925, -81.209885);
        googleMap.addMarker(new MarkerOptions().position(stop1)
                .title("Bus Stop 1"));
        //Bus 1
        LatLng bus1 = new LatLng(28.597821, -81.211167);
        googleMap.addMarker(new MarkerOptions().position(bus1)
                .title("Bus 1"));
        //Bus 2
        LatLng bus2 = new LatLng(28.602569, -81.207583);
        googleMap.addMarker(new MarkerOptions().position(bus2)
                .title("Bus 2"));
        //Bus 3
        LatLng bus3 = new LatLng(28.607211, -81.207548);
        googleMap.addMarker(new MarkerOptions().position(bus3)
                .title("Bus 3"));
        //Bus 4
        LatLng bus4 = new LatLng(28.597111, -81.202904);
        googleMap.addMarker(new MarkerOptions().position(bus4)
                .title("Bus 4"));
        //Bus 5
        LatLng bus5 = new LatLng(28.594626, -81.207941);
        googleMap.addMarker(new MarkerOptions().position(bus5)
                .title("Bus 5"));
        //set zoom
        double lat = (stop1.latitude + bus1.latitude + bus2.latitude + bus3.latitude
                + bus4.latitude + bus5.latitude)/6;
        double lon = (stop1.longitude + bus1.longitude + bus2.longitude + bus3.longitude
                + bus4.longitude + bus5.longitude)/6;
        LatLng zoom = new LatLng(lat,lon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoom,15));

    }
}
