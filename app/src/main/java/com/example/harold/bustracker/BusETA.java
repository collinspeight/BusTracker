package com.example.harold.bustracker;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

public class BusETA extends AppCompatActivity {


    private ArrayList<Integer> routes =  new ArrayList<>();
    private ArrayList<String> routeNames =  new ArrayList<>();
    private ArrayList<String> colors =  new ArrayList<>();
    private ArrayList<Integer> arrivals =  new ArrayList<>();
    private ETAInformationReceiver etaInformationReceiver;
    private boolean debug = false;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        JSONArray etaArray;
        JSONObject temp;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_et);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bus Estimated Arrival Time");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "For more scheduling information", Snackbar.LENGTH_LONG)
                        .setAction("VISIT", new FABListener()).show();
            }
        });

        String stopID = getIntent().getStringExtra("StopID");
        String name = getIntent().getStringExtra("Name");
        String stopCode = getIntent().getStringExtra("StopCode");
        url = getIntent().getStringExtra("URL");


        TextView nameTextView = (TextView) findViewById(R.id.textView_address);
        nameTextView.setText(name);
        TextView stopTextView = (TextView) findViewById(R.id.stop_code);
        stopTextView.setText(stopCode);

        setupServiceReceiver();

        // Start service
        Intent intent = new Intent(this, ETAInformationService.class);
        intent.putExtra("receiver", etaInformationReceiver);
        intent.putExtra("stopID", stopID);
        startService(intent);
    }



    /**
     * Creates a service receiver that will be notified of results of the BusInformationService
     * once the service receiver subscribes to the service.
     */
    public void setupServiceReceiver() {
        etaInformationReceiver = new ETAInformationReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        etaInformationReceiver.setReceiver(new ETAInformationReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    routes =resultData.getIntegerArrayList("routes");
                    colors = resultData.getStringArrayList("colors");
                    arrivals = resultData.getIntegerArrayList("arrivals");
                    if(routes.size() == 0){
                        Toast.makeText(BusETA.this, "Check back later..",
                                Toast.LENGTH_LONG).show();
                    }
                    populateListView();
                }
            }
        });
    }

    public class FABListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url)));

        }
    }


    private void populateListView() {

        try {
            getRouteNames();
            if (debug) {
                System.out.println(routes);
                System.out.println(routeNames);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomAdapter customAdapter = new CustomAdapter();

        ListView list = (ListView) findViewById(R.id.list_view);
        list.setAdapter(customAdapter);
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return routes.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.items, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.imageview_circle);
            TextView route = (TextView) view.findViewById(R.id.textview_route);
            TextView arrival = (TextView) view.findViewById(R.id.textview_arrival);

            imageView.setColorFilter(Integer.decode("0x7f" + colors.get(i)));
            route.setText(routeNames.get(i));
            if(arrivals.get(i) > 1){
                arrival.setText(String.valueOf(arrivals.get(i) - 1) + "-" + String.valueOf(arrivals.get(i)) + " minutes");
            } else {
                arrival.setText("Arriving soon");
            }

            return view;
        }
    }

    private void getRouteNames() throws JSONException {
        JSONArray routeArray = getJSONFromRaw(0);
        JSONObject temp;
        int rlength, jlength;

        // Get route names
        rlength = routes.size();
        jlength = routeArray.length();
        for(int i = 0; i < rlength; i++) {

            for(int j = 0; j < jlength; j++) {

                temp = routeArray.getJSONObject(j);

                if (routes.get(i).equals(temp.optInt("id"))) {

                    routeNames.add(temp.getString("name"));

                    break;
                }
            }
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



}
