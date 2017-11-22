package com.example.harold.bustracker;


import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class BusInformationService extends IntentService{

    public BusInformationService() {
        // Used to name the worker thread
        super("BusInformationService");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Extract the receiver passed into the service
        ResultReceiver rec = intent.getParcelableExtra("receiver");
        boolean adminMode = intent.getBooleanExtra("adminMode", false);
        int routeNumber = intent.getIntExtra("routeNumber", 0);
        JSONArray busLoc;
        JSONObject temp;

        String name = "name";
        double lat = 0;
        double lng = 0;
        double eta = 0;
        //TODO Remove this variable
        double inc = 0;
        // Temp loop to illustrate real time updates
        for (int i = 0; i < 100; i++)
        {

            // Getting data from Lynx/DoubleMap API
            try {
                busLoc = getJSONFromURL("http://golynx.doublemap.com/map/v2/buses");
                // System.out.println(busLoc);
                for (int j = 0; j < busLoc.length(); j++) {
                    temp = busLoc.getJSONObject(j);

                    if(temp.optInt("route") == routeNumber){
                        name = temp.optString("name");
                        lat = temp.optDouble("lat");
                        lng = temp.optDouble("lon");

                        System.out.println(lat);
                        System.out.println(lng);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            // Test for admin vs standard mode. If admin mode create 3 marks. If standard create one.
            // This section will be removed and replaced with logic to get all the supported buses
            // when in admin mode and only the closest in standard mode.
            Bundle bundle = new Bundle();
            if (adminMode)
            {
                bundle.putDouble("lat1", lat);
                bundle.putDouble("lng1", lng);
                bundle.putDouble("lat2", lat+1);
                bundle.putDouble("lng2", lng+1);
                bundle.putDouble("lat3", lat+2);
                bundle.putDouble("lng3", lng+2);
            }
            else
            {
                bundle.putString("name",name);
                bundle.putDouble("lat", lat);
                bundle.putDouble("lng", lng);
                bundle.putDouble("ETA", eta);
            }

            rec.send(Activity.RESULT_OK, bundle);

            try
            {
                synchronized (this)
                {
                    wait(9000);
                }
            }
            catch (InterruptedException e)
            {
            }
        }
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


            // Read the input stream into a String

            inputStream = new BufferedInputStream(conn.getInputStream());


            Scanner scanner = new Scanner(inputStream);

            StringBuilder builder = new StringBuilder();

            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            forecastJsonStr = builder.toString();
            jsonArray = new JSONArray(forecastJsonStr);
            inputStream.close();
            builder.delete(0,builder.length());
            conn.disconnect();


        } catch (JSONException e) {
            e.printStackTrace();
        } finally {

            return jsonArray;
        }


    }

}
