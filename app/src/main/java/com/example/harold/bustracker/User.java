package com.example.harold.bustracker;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.example.harold.bustracker.BusInformationReceiver;
import com.example.harold.bustracker.BusInformationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class User extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private BusInformationReceiver busInformationReceiver;
    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    private LatLng busStopLocation;
    private GoogleMap map;
    private int busStopNumber, routeNumber;
    private LatLng[] busStopsLocation;
    ArrayList<LatLng> path;
    private int[] stops;
    private boolean debug = false;
    //Saves the marker position so it can be removed
    private Marker bus;
    private Marker stop;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user);

        initializeBusStopsLocation();

        // Get bus stop number and location
        busStopNumber = getIntent().getIntExtra("BusStop", 0);
        busStopLocation = busStopsLocation[busStopNumber];

        routeNumber = getIntent().getIntExtra("RouteNumber", 0);
        // Create service receiver
        setupServiceReceiver();

        // Start service
        intent = new Intent(this, BusInformationService.class);
        intent.putExtra("receiver", busInformationReceiver);
        intent.putExtra("adminMode", false);
        intent.putExtra("routeNumber", routeNumber);
        startService(intent);

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

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
                    setBusInformation(new LatLng(resultData.getDouble("lat"), resultData.getDouble("lng")), resultData.getString("name"));
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(intent);
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
        stopService(intent);
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

        map.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        // Create marker for bus stop
        try {
            setBusStops();
        } catch (JSONException e) {
            e.printStackTrace();
        }



        map.moveCamera(CameraUpdateFactory.newLatLng(path.get(path.size()/2)));
        map.animateCamera(CameraUpdateFactory.zoomTo(12.5f));
        map.setOnMarkerClickListener(this);


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        if (marker.equals(bus)){
            return true;
        }

        //Toast.makeText(getApplicationContext(), marker.getTag().toString(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(User.this, BusETA.class);
        i.putExtra("StopID", marker.getTag().toString());
        i.putExtra("Name", marker.getTitle());
        i.putExtra("StopCode", marker.getSnippet());
        stopService(intent);
        startActivity(i);
        return true;
    }

    private void setBusStops() throws JSONException {
        JSONArray routeArray = getJSONFromRaw(0);
        JSONArray stopsArray = getJSONFromRaw(1);
        JSONObject temp, routeObj;
        String color;
        path = new ArrayList<>();
        ArrayList<Integer> stops = new ArrayList<>();
        int length;

        // Get route information
        length = routeArray.length();
        for(int i = 0; i < length; i++) {
            temp = routeArray.getJSONObject(i);


            if (temp.optInt("id") == routeNumber){
                if(debug) {
                    System.out.println(temp);
                }
                routeObj = temp;
                JSONArray tempArray;

                // Get path of the route
                tempArray = routeObj.optJSONArray("path");
                length = tempArray.length();

                if (tempArray != null) {
                    for (i = 0; i < length; i += 2) {
                        path.add(new LatLng(tempArray.getDouble(i), tempArray.getDouble(i + 1)));
                    }
                }

                if(debug) {
                    System.out.println(path);
                }


                // Get stops of the route
                tempArray = routeObj.optJSONArray("stops");
                length = tempArray.length();

                if (tempArray != null) {
                    for (i = 0; i < length; i += 2) {
                        stops.add(Integer.valueOf(tempArray.getInt(i)));
                    }
                }

                if(debug) {
                    System.out.println(stops);
                }


                // Adding Path
                color = routeObj.getString("color");
                drawRoute(path, color);

                break;
            }


        }
        // Adding stop markers
        length = stopsArray.length();
        for(int i = 0; i < length; i++) {
            temp = stopsArray.getJSONObject(i);

            if (stops.contains(temp.optInt("id"))) {
                if(debug) {
                    System.out.println(temp.getString("name"));
                    System.out.println(new LatLng(temp.getDouble("lat"), temp.getDouble("lon")));
                }

                setStopAndInfo(new LatLng(temp.getDouble("lat"), temp.getDouble("lon")),
                        temp.getString("name"),
                        temp.getString("code"),
                        temp.getInt("id"));

            }

        }

    }

    private void setStopAndInfo (LatLng latlng, String name, String code, int id) {

        if(debug) {
            System.out.println(latlng);
            System.out.println(name + code);
        }

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latlng)
                .title(name)
                .snippet("Stop Code: " + code)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_icon)));

        marker.setTag(id);

    }

    private void drawRoute(ArrayList<LatLng> path, String color) {
        int hexColor = Integer.decode("0x7f" + color);
        PolylineOptions rectOptions = new PolylineOptions()
                                        .color(hexColor)
                                        .jointType(JointType.ROUND)
                                        .width(15.0f);

        rectOptions.addAll(path);

        map.addPolyline(rectOptions);



    }

    private void setBusInformation(LatLng latlng, String name)
    {
        //If marker has already been placed. Remove it.
        if(bus!= null)
            animateBusMarker(name, bus.getPosition(),latlng, false);
        else {
            bus = map.addMarker(new MarkerOptions().position(latlng).title("Bus: " + name));
        }
        if(stop!= null)
            stop.remove();

        stop = map.addMarker(new MarkerOptions().position(busStopLocation).title("Selected Bus Stop"));
    }


    // For Admin mode, change the parameters to accept the bus marker so that
    // specific marker would change instead of just one marker.
    // TODO Might need to edit this method for multiple bus or just do the regular
    public void animateBusMarker(final String name, final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker) {

        bus.remove();

        bus = map.addMarker(new MarkerOptions()
                .position(startPosition)
                .title("Bus: " + name));


        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 10000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                bus.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        bus.setVisible(false);
                    } else {
                        bus.setVisible(true);
                    }
                }
            }
        });
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

    // Parsing JSON from raw assets
    // 0  = routes
    // 1  = stops
    private JSONArray getJSONFromRaw (int toggle) {

        String forecastJsonStr;
        InputStream inputStream;
        JSONArray jsonArray = null;

        try {

            Resources res = getResources();

            // Read the input stream into a String
            if ( toggle == 0) {
                inputStream = res.openRawResource(R.raw.routes);
            } else {
                inputStream = res.openRawResource(R.raw.stops);
            }

            Scanner scanner = new Scanner(inputStream);

            StringBuilder builder = new StringBuilder();

            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            forecastJsonStr = builder.toString();
            jsonArray = new JSONArray(forecastJsonStr);


        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return jsonArray;
        }


    }
}
