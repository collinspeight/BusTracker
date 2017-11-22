package com.example.harold.bustracker;

import android.content.Intent;
import android.graphics.Color;
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


    ArrayList<Integer> routes =  new ArrayList<>();
    ArrayList<String> colors =  new ArrayList<>();
    ArrayList<Integer> arrivals =  new ArrayList<>();
    private ETAInformationReceiver etaInformationReceiver;


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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        String stopID = getIntent().getStringExtra("StopID");
        String name = getIntent().getStringExtra("Name");

        TextView nameTextView = (TextView) findViewById(R.id.textView_address);
        nameTextView.setText(name);

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
//                    routes =resultData.getIntegerArrayList("routes");
//                    colors = resultData.getStringArrayList("colors");
//                    arrivals = resultData.getIntegerArrayList("arrivals");
                    routes.add(434);
                    routes.add(414);
                    routes.add(424);
                    colors.add("00A884");
                    colors.add("00A884");
                    colors.add("00A884");
                    arrivals.add(0);
                    arrivals.add(1);
                    arrivals.add(3);
                    populateListView();
                }
            }
        });
    }

    private void populateListView() {


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
            route.setText(String.valueOf(routes.get(i)));
            if(arrivals.get(i) > 1){
                arrival.setText(String.valueOf(arrivals.get(i) - 1) + "-" + String.valueOf(arrivals.get(i)) + " minutes");
            } else {
                arrival.setText("Arriving soon");
            }

            return view;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



}
