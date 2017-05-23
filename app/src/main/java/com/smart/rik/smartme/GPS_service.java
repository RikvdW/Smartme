package com.smart.rik.smartme;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by Rik on 21/05/2017.
 */

public class GPS_service extends Service {


    private String comp;
    private LocationListener listener;
    private LocationManager locationManager;
    private BroadcastReceiver broadcastReceiver_COMP;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        listener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                Intent i = new Intent("location_update");
                i.putExtra("coordinates",location.getLongitude()+"\n"+location.getLatitude()+"\n"+currentDateTimeString+"\n"+comp);
                sendBroadcast(i);
                writeToFile(location.getLongitude()+"/"+location.getLatitude()+"/"+currentDateTimeString+"/"+comp+"\n",getBaseContext());
            }

            @Override
            public void onStatusChanged(String s, int status, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        if (broadcastReceiver_COMP == null) {
            broadcastReceiver_COMP = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    comp = ""+intent.getExtras().get("degrees");
                }
            };
        }

        registerReceiver(broadcastReceiver_COMP, new IntentFilter("comp_update"));

        locationManager= (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null) locationManager.removeUpdates(listener);

    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


}
