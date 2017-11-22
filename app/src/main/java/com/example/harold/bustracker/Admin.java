package com.example.harold.bustracker;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

public class Admin extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private BusInformationReceiver busInformationReceiver;
    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    ArrayList<LatLng> busStopLocation;
    private GoogleMap map;
    ArrayList<Integer> busStopNumber, routeNumber;
    private LatLng[] busStopsLocation;
    ArrayList<LatLng> path;
    private int[] stops;
    private boolean debug = false;
    //Saves the marker position so it can be removed
    private Marker bus;
    private Marker stop;
    private Intent intent;
    private LatLng camera = new LatLng(28.316620, -81.447269);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin);

        initializeBusStopsLocation();
        busStopLocation = new ArrayList<>();
        busStopNumber = new ArrayList<>();
        routeNumber = new ArrayList<>();
        // Get bus stop number and location
        busStopNumber.add(getIntent().getIntExtra("BusStop", 0));
        busStopLocation.add(busStopsLocation[busStopNumber.get(0)]);
        busStopNumber.add(getIntent().getIntExtra("BusStop2", 0));
        busStopLocation.add(busStopsLocation[busStopNumber.get(1)]);
        busStopNumber.add(getIntent().getIntExtra("BusStop3", 0));
        busStopLocation.add(busStopsLocation[busStopNumber.get(2)]);
        busStopNumber.add(getIntent().getIntExtra("BusStop4", 0));
        busStopLocation.add(busStopsLocation[busStopNumber.get(3)]);
        busStopNumber.add(getIntent().getIntExtra("BusStop5", 0));
        busStopLocation.add(busStopsLocation[busStopNumber.get(4)]);

        routeNumber.add(getIntent().getIntExtra("RouteNumber", 0));
        routeNumber.add(getIntent().getIntExtra("RouteNumber2", 0));
        routeNumber.add(getIntent().getIntExtra("RouteNumber3", 0));
        routeNumber.add(getIntent().getIntExtra("RouteNumber4", 0));
        routeNumber.add(getIntent().getIntExtra("RouteNumber5", 0));
        // Create service receiver
        setupServiceReceiver();

        // Start service
        intent = new Intent(this, BusInformationService.class);
        intent.putExtra("receiver", busInformationReceiver);
        intent.putExtra("adminMode", false);
        intent.putExtra("routeNumber", routeNumber.get(0));
        intent.putExtra("routeNumber2", routeNumber.get(1));
        intent.putExtra("routeNumber3", routeNumber.get(2));
        intent.putExtra("routeNumber4", routeNumber.get(3));
        intent.putExtra("routeNumber5", routeNumber.get(4));
        startService(intent);

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();

        signOut = findViewById(R.id.logOut);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
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
                    setBusInformation(new LatLng(resultData.getDouble("lat"), resultData.getDouble("lng")));
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

        map.animateCamera(CameraUpdateFactory.zoomTo(1f));

        // Create marker for bus stop
        try {
            setBusStops();
        } catch (JSONException e) {
            e.printStackTrace();
        }



        map.moveCamera(CameraUpdateFactory.newLatLng(camera));
        map.animateCamera(CameraUpdateFactory.zoomTo(10.5f));
        map.setOnMarkerClickListener(this);


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        //Toast.makeText(getApplicationContext(), marker.getTag().toString(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Admin.this, BusETA.class);
        i.putExtra("StopID", marker.getTag().toString());
        i.putExtra("Name", marker.getTitle());
        stopService(intent);
        startActivity(i);
        return false;
    }

    private void setBusStops() throws JSONException {
        JSONArray routeArray = getJSONFromRaw(0);
        JSONArray stopsArray = getJSONFromRaw(1);
        JSONObject temp, routeObj;
        String color;
        path = new ArrayList<>();
        ArrayList<Integer> stops = new ArrayList<>();
        int length;
        int oldLength;
        int save;
        // Get route information
        length = routeArray.length();
        for(int i = 0; i < length; i++) {
            stops = new ArrayList<>();
            path = new ArrayList<>();
            temp = routeArray.getJSONObject(i);
            if (temp.optInt("id") == routeNumber.get(0)){
                if(debug) {
                    System.out.println(temp);
                }
                save = i;
                oldLength = length;
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
                i = save;
                length = oldLength;
                plot(stopsArray, temp, stops);
            }
            if (temp.optInt("id") == routeNumber.get(1)){
                if(debug) {
                    System.out.println(temp);
                }
                save = i;
                oldLength = length;
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
                i = save;
                length = oldLength;
                plot(stopsArray, temp, stops);
            }
            if (temp.optInt("id") == routeNumber.get(2)){
                if(debug) {
                    System.out.println(temp);
                }
                save = i;
                oldLength = length;
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
                i = save;
                length = oldLength;
                plot(stopsArray, temp, stops);
            }
            if (temp.optInt("id") == routeNumber.get(3)){
                if(debug) {
                    System.out.println(temp);
                }
                save = i;
                oldLength = length;
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
                i = save;
                length = oldLength;
                plot(stopsArray, temp, stops);
            }
            if (temp.optInt("id") == routeNumber.get(4)){
                if(debug) {
                    System.out.println(temp);
                }
                save = i;
                oldLength = length;
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
                i = save;
                length = oldLength;
                plot(stopsArray, temp, stops);
            }

        }

    }

    private void plot(JSONArray stopsArray, JSONObject temp, ArrayList<Integer> stops) throws JSONException{
        // Adding stop markers
        double lat = 0;
        double lon = 0;
        int length = stopsArray.length();
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
                .snippet("stop code:" + code)
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

    private void setBusInformation(LatLng latlng)
    {
        //If marker has already been placed. Remove it.
        if(bus!= null)
            bus.remove();
        if(stop!= null)
            stop.remove();
        bus = map.addMarker(new MarkerOptions().position(latlng).title("Current Location of Bus"));
        stop = map.addMarker(new MarkerOptions().position(busStopLocation.get(0)).title("Selected Bus Stop"));
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
