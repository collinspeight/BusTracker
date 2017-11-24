package com.example.harold.bustracker;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class Admin extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private BusInformationReceiver busInformationReceiver;
    private FirebaseAuth mAuth;
    private Button signOut;
    MapView mMapView;
    private GoogleMap map;
    ArrayList<Integer>  routeNumber;
    ArrayList<LatLng> path;
    private boolean debug = false;
    //Saves the marker position so it can be removed
    private Marker bus1;
    private Marker bus2;
    private Marker bus3;
    private Marker bus4;
    private Marker bus5;
    private Intent intent;
    private LatLng camera = new LatLng(28.316620, -81.447269);
    private  Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin Mode");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        FloatingActionButton signOut = (FloatingActionButton) findViewById(R.id.fab_signout);

        routeNumber = new ArrayList<>();

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
        intent.putExtra("adminMode", true);
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
                    setBusInformation(resultData);
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

        if(marker.equals(bus1) || marker.equals(bus2) ||
                marker.equals(bus3) || marker.equals(bus4) ||
                marker.equals(bus5)){
            return true;
        }

        //Toast.makeText(getApplicationContext(), marker.getTag().toString(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Admin.this, BusETA.class);
        i.putExtra("StopID", marker.getTag().toString());
        i.putExtra("Name", marker.getTitle());
        i.putExtra("StopCode", marker.getSnippet());
        i.putExtra("URL", "http://www.golynx.com/maps-schedules/routes-schedules.stml");
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

    private void setBusInformation(Bundle resultData)
    {
        //If marker has already been placed. Remove it.
        LatLng latLng1 = new LatLng(resultData.getDouble("lat1"), resultData.getDouble("lng1"));
        String name1 = resultData.getString("name1");
        LatLng latLng2 = new LatLng(resultData.getDouble("lat2"), resultData.getDouble("lng2"));
        String name2 = resultData.getString("name2");
        LatLng latLng3 = new LatLng(resultData.getDouble("lat3"), resultData.getDouble("lng3"));
        String name3 = resultData.getString("name3");
        LatLng latLng4 = new LatLng(resultData.getDouble("lat4"), resultData.getDouble("lng4"));
        String name4 = resultData.getString("name4");
        LatLng latLng5 = new LatLng(resultData.getDouble("lat5"), resultData.getDouble("lng5"));
        String name5 = resultData.getString("name5");

        if (bus1 != null && bus2 != null && bus3 != null && bus4 != null && bus5 != null)
        {
            animateBusMarker(name1, bus1.getPosition(), latLng1, false,
                    name2, bus2.getPosition(), latLng2, false,
                    name3, bus3.getPosition(), latLng3, false,
                    name4, bus4.getPosition(), latLng4, false,
                    name5, bus5.getPosition(), latLng5, false);
        }
        else
        {
            bus1 = map.addMarker(new MarkerOptions()
                    .position(latLng1)
                    .title("Bus: " + name1));
            bus2 = map.addMarker(new MarkerOptions()
                    .position(latLng2)
                    .title("Bus: " + name2));
            bus3 = map.addMarker(new MarkerOptions()
                    .position(latLng3)
                    .title("Bus: " + name3));
            bus4 = map.addMarker(new MarkerOptions()
                    .position(latLng4)
                    .title("Bus: " + name4));
            bus5 = map.addMarker(new MarkerOptions()
                    .position(latLng5)
                    .title("Bus: " + name5));
        }
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

    // For Admin mode, change the parameters to accept the bus marker so that
    // specific marker would change instead of just one marker.
    // TODO Might need to edit this method for multiple bus or just do the regular
    public void animateBusMarker(final String name1, final LatLng startPosition1, final LatLng toPosition1, final boolean hideMarker1,
                                 final String name2, final LatLng startPosition2, final LatLng toPosition2, final boolean hideMarker2,
                                 final String name3, final LatLng startPosition3, final LatLng toPosition3, final boolean hideMarker3,
                                 final String name4, final LatLng startPosition4, final LatLng toPosition4, final boolean hideMarker4,
                                 final String name5, final LatLng startPosition5, final LatLng toPosition5, final boolean hideMarker5) {

        bus1.remove();

        bus1 = map.addMarker(new MarkerOptions()
                .position(startPosition1)
                .title("Bus: " + name1));

        bus2.remove();

        bus2 = map.addMarker(new MarkerOptions()
                .position(startPosition2)
                .title("Bus: " + name2));

        bus3.remove();

        bus3 = map.addMarker(new MarkerOptions()
                .position(startPosition3)
                .title("Bus: " + name3));

        bus4.remove();

        bus4 = map.addMarker(new MarkerOptions()
                .position(startPosition4)
                .title("Bus: " + name4));

        bus5.remove();

        bus5 = map.addMarker(new MarkerOptions()
                .position(startPosition5)
                .title("Bus: " + name5));


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
                double lng = t * toPosition1.longitude + (1 - t)
                        * startPosition1.longitude;
                double lat = t * toPosition1.latitude + (1 - t)
                        * startPosition1.latitude;

                bus1.setPosition(new LatLng(lat, lng));

                lng = t * toPosition2.longitude + (1 - t)
                        * startPosition2.longitude;
                lat = t * toPosition2.latitude + (1 - t)
                        * startPosition2.latitude;

                bus2.setPosition(new LatLng(lat, lng));

                lng = t * toPosition3.longitude + (1 - t)
                        * startPosition3.longitude;
                lat = t * toPosition3.latitude + (1 - t)
                        * startPosition3.latitude;

                bus3.setPosition(new LatLng(lat, lng));

                lng = t * toPosition4.longitude + (1 - t)
                        * startPosition4.longitude;
                lat = t * toPosition4.latitude + (1 - t)
                        * startPosition4.latitude;

                bus4.setPosition(new LatLng(lat, lng));

                lng = t * toPosition5.longitude + (1 - t)
                        * startPosition5.longitude;
                lat = t * toPosition5.latitude + (1 - t)
                        * startPosition5.latitude;

                bus5.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker1) {
                        bus1.setVisible(false);
                    } else {
                        bus1.setVisible(true);
                    }
                    if (hideMarker2) {
                        bus2.setVisible(false);
                    } else {
                        bus2.setVisible(true);
                    }
                    if (hideMarker3) {
                        bus3.setVisible(false);
                    } else {
                        bus3.setVisible(true);
                    }
                    if (hideMarker4) {
                        bus4.setVisible(false);
                    } else {
                        bus4.setVisible(true);
                    }
                    if (hideMarker5) {
                        bus5.setVisible(false);
                    } else {
                        bus5.setVisible(true);
                    }
                }
            }
        });

    }
}
