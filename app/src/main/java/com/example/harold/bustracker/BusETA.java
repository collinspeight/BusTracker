package com.example.harold.bustracker;

import android.graphics.Color;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BusETA extends AppCompatActivity {

    int [] routes = {434,414, 424};
    String [] colors = {"00A884", "0DA884", "0FA884"};
    int [] arrivals = {0, 0, 0};


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

        try {
            etaArray = getJSONFromURL("http://golynx.doublemap.com/map/v2/eta?stop=" + stopID);

            //JSONObject ETAs = etaArray.getJSONObject(0);

            System.out.println(etaArray);

        } catch (IOException e) {
            e.printStackTrace();
        }

        populateListView();
    }

    private void populateListView() {

        CustomAdapter customAdapter = new CustomAdapter();

        ListView list = (ListView) findViewById(R.id.list_view);
        list.setAdapter(customAdapter);
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return routes.length;
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

            imageView.setColorFilter(Integer.decode("0x7f" + colors[i]));
            route.setText(String.valueOf(routes[i]));
            arrival.setText(String.valueOf(arrivals[i] - 1) + "-" + String.valueOf(arrivals[i]) + " minutes");

            return view;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private JSONArray getJSONFromURL (String reqURL) throws IOException {

        String forecastJsonStr;
        InputStream inputStream;
        JSONArray jsonArray = null;

        try {

            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.connect();

            System.out.println(url);
            // Read the input stream into a String

            inputStream = new BufferedInputStream(conn.getInputStream());

            System.out.println("IS:" + inputStream);

            Scanner scanner = new Scanner(inputStream);

            StringBuilder builder = new StringBuilder();

            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            forecastJsonStr = builder.toString();
            System.out.println("JSON"+ forecastJsonStr);
            jsonArray = new JSONArray(forecastJsonStr);
            conn.disconnect();


        } catch (JSONException e) {
            e.printStackTrace();
        } finally {

            return jsonArray;
        }


    }

}
