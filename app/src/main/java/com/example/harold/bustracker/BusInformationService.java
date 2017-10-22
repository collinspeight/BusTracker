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

        double lat = 0;
        double lng = 0;

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

            Bundle bundle = new Bundle();
            bundle.putDouble("lat", lat);
            bundle.putDouble("lng", lng);

            rec.send(Activity.RESULT_OK, bundle);
        }
    }

}
