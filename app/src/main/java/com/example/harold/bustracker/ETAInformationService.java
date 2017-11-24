package com.example.harold.bustracker;

/**
 * Created by Harold on 11/21/2017.
 */


import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ETAInformationService extends IntentService{

    public ETAInformationService() {
        // Used to name the worker thread
        super("ETAInformationService");
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
        String stopID = intent.getStringExtra("stopID");
        JSONObject stopJSON, temp;
        ArrayList<Integer> routes =  new ArrayList<>();
        ArrayList<String> colors =  new ArrayList<>();
        ArrayList<Integer> arrivals =  new ArrayList<>();

        JSONArray etaArray;

        String name = "blah";


        try {
            stopJSON = getJSONFromURL("http://golynx.doublemap.com/map/v2/eta?stop=" + stopID).getJSONObject("etas").getJSONObject(stopID);

            name = stopJSON.getString("name");
            etaArray = stopJSON.getJSONArray("etas");
            System.out.println(name);
            System.out.println(etaArray);
            for(int i = 0;i < etaArray.length(); i++){
                temp = etaArray.getJSONObject(i);

                routes.add(temp.getInt("route"));
                colors.add(temp.getString("color"));
                arrivals.add(temp.getInt("avg"));

            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        // Test for admin vs standard mode. If admin mode create 3 marks. If standard create one.
            // This section will be removed and replaced with logic to get all the supported buses
            // when in admin mode and only the closest in standard mode.
            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            bundle.putIntegerArrayList("routes", routes);
            bundle.putStringArrayList("colors", colors);
            bundle.putIntegerArrayList("arrivals", arrivals);

            rec.send(Activity.RESULT_OK, bundle);

    }

    private JSONObject getJSONFromURL(String reqURL) throws IOException {

        String forecastJsonStr;
        InputStream inputStream;
        JSONObject jsonObject = null;

        try {

            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            System.out.println(reqURL);

            // Read the input stream into a String

            inputStream = new BufferedInputStream(conn.getInputStream());


            Scanner scanner = new Scanner(inputStream);

            StringBuilder builder = new StringBuilder();

            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            forecastJsonStr = builder.toString();
            jsonObject = new JSONObject(forecastJsonStr);
            inputStream.close();
            builder.delete(0, builder.length());
            conn.disconnect();


        } catch (JSONException e) {
            System.out.println("SOMETHING HAPPENED SHIIIT");
            e.printStackTrace();
        } finally {

            return jsonObject;
        }

    }




}

