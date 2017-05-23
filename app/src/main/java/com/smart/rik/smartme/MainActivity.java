package com.smart.rik.smartme;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private Button btn_start, btn_stop;
    private TextView textView_GPS, textView_DEG, textView_CON;
    private BroadcastReceiver broadcastReceiver_GPS;
    private BroadcastReceiver broadcastReceiver_COMP;
    private BroadcastReceiver broadcastReceiver_CON;




    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver_GPS == null) {
            broadcastReceiver_GPS = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    textView_GPS.setText("Coordinates: \n" + intent.getExtras().get("coordinates"));
                    String send_string = "COR: " + intent.getExtras().get("coordinates");

                }
            };
        }

        registerReceiver(broadcastReceiver_GPS, new IntentFilter("location_update"));

        if (broadcastReceiver_COMP == null) {
            broadcastReceiver_COMP = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    textView_DEG.setText("Compass: \n" + intent.getExtras().get("degrees"));
                    String send_string = "DEG: " + intent.getExtras().get("degrees");

                }
            };
        }

        registerReceiver(broadcastReceiver_COMP, new IntentFilter("comp_update"));

        if(broadcastReceiver_CON==null){
            broadcastReceiver_CON = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    textView_CON.setText("" + intent.getExtras().get("connected"));

                }
            };
        }

        registerReceiver(broadcastReceiver_CON, new IntentFilter("connect"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver_GPS != null) {
            unregisterReceiver(broadcastReceiver_GPS);
        }
        if (broadcastReceiver_COMP != null) {
            unregisterReceiver(broadcastReceiver_COMP);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = (Button) findViewById(R.id.button);
        btn_stop = (Button) findViewById(R.id.button2);
        textView_GPS = (TextView) findViewById(R.id.GPS_t);
        textView_DEG = (TextView) findViewById(R.id.COMP_t);
        textView_CON = (TextView) findViewById(R.id.CON);
        if (!runtime_permissions()) {
            enable_button();
        }


    }

    private void enable_button() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), GPS_service.class);
                Intent j = new Intent(getApplicationContext(), Compass_service.class);
                Intent k = new Intent(getApplicationContext(), IoT_service.class);
                startService(i);
                startService(j);
                startService(k);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), GPS_service.class);
                Intent j = new Intent(getApplicationContext(), Compass_service.class);
                Intent k = new Intent(getApplicationContext(), IoT_service.class);
                stopService(i);
                stopService(j);
                stopService(k);
            }
        });
    }


    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_button();
            } else {
                runtime_permissions();
            }
        }
    }





}
