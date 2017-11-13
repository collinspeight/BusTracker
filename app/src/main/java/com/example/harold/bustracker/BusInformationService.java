package com.example.harold.bustracker;


import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

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
    protected void onHandleIntent(Intent intent) {
        // Extract the receiver passed into the service
        ResultReceiver rec = intent.getParcelableExtra("receiver");
        boolean adminMode = intent.getBooleanExtra("adminMode", false);

        double lat = 0;
        double lng = 0;
        double eta = 0;

        // Temp loop to illustrate real time updates
        for (int i = 0; i < 10000; i++)
        {
            try
            {
                synchronized (this)
                {
                    wait(1000);
                }
            }
            catch (InterruptedException e)
            {
            }

            //TODO Obtain lat and lng values from Lynx API
            lat = i;
            lng = i;
            eta = i;

            // Test for admin vs standard mode. If admin mode create 3 marks. If standard create one.
            // This section will be removed and replaced with logic to get all the supported buses
            // when in admin mode and only the closest in standard mode.
            Bundle bundle = new Bundle();
            if (adminMode)
            {
                bundle.putDouble("lat1", lat);
                bundle.putDouble("lng1", lng);
                bundle.putDouble("lat2", lat++);
                bundle.putDouble("lng2", lng++);
                bundle.putDouble("lat3", lat--);
                bundle.putDouble("lng3", lng--);
            }
            else
            {
                bundle.putDouble("lat", lat);
                bundle.putDouble("lng", lng);
                bundle.putDouble("ETA", eta);
            }

            rec.send(Activity.RESULT_OK, bundle);
        }
    }

}
