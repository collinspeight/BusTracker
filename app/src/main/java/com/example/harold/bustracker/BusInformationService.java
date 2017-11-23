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
        boolean run = true;

        // Temp loop to illustrate real time updates
        while (run)
        {
            Bundle bundle = new Bundle();

            // Getting data from Lynx/DoubleMap API
            try {
                busLoc = getJSONFromURL("http://golynx.doublemap.com/map/v2/buses");
                // System.out.println(busLoc);
                for (int j = 0; j < busLoc.length(); j++) {
                    temp = busLoc.getJSONObject(j);

                    if (adminMode)
                    {
                        int routeNumber2 = intent.getIntExtra("routeNumber2", 0);
                        int routeNumber3 = intent.getIntExtra("routeNumber3", 0);
                        int routeNumber4 = intent.getIntExtra("routeNumber4", 0);
                        int routeNumber5 = intent.getIntExtra("routeNumber5", 0);

                        if(temp.optInt("route") == routeNumber)
                        {
                            bundle.putString("name1",temp.optString("name"));
                            bundle.putDouble("lat1", temp.optDouble("lat"));
                            bundle.putDouble("lng1", temp.optDouble("lon"));
                        }
                        else if(temp.optInt("route") == routeNumber2)
                        {
                            bundle.putString("name2",temp.optString("name"));
                            bundle.putDouble("lat2", temp.optDouble("lat"));
                            bundle.putDouble("lng2", temp.optDouble("lon"));
                        }
                        else if(temp.optInt("route") == routeNumber3)
                        {
                            bundle.putString("name3",temp.optString("name"));
                            bundle.putDouble("lat3", temp.optDouble("lat"));
                            bundle.putDouble("lng3", temp.optDouble("lon"));
                        }
                        else if(temp.optInt("route") == routeNumber4)
                        {
                            bundle.putString("name4",temp.optString("name"));
                            bundle.putDouble("lat4", temp.optDouble("lat"));
                            bundle.putDouble("lng4", temp.optDouble("lon"));
                        }
                        else if(temp.optInt("route") == routeNumber5)
                        {
                            bundle.putString("name5",temp.optString("name"));
                            bundle.putDouble("lat5", temp.optDouble("lat"));
                            bundle.putDouble("lng5", temp.optDouble("lon"));
                        }
                    }
                    else
                    {
                        if(temp.optInt("route") == routeNumber)
                        {
                            bundle.putString("name",temp.optString("name"));
                            bundle.putDouble("lat", temp.optDouble("lat"));
                            bundle.putDouble("lng", temp.optDouble("lon"));
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                run = false;
            } catch (JSONException e) {
                e.printStackTrace();
                run = false;
            }

            rec.send(Activity.RESULT_OK, bundle);

            try
            {
                synchronized (this)
                {
                    wait(10000);
                }
            }
            catch (InterruptedException e)
            {
                run = false;
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
