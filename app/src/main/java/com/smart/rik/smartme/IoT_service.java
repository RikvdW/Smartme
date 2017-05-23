package com.smart.rik.smartme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Rik on 23/05/2017.
 */

public class IoT_service extends Service {
    final private String USERNAME = "use-token-auth";
    final private String PASSWORD = "xxxxxxxx";
    final private String CLIENT_IDENTIFIER = "d:kbjo2y:phone_A:phone";
    private Timer timer;
    private TimerTask task;
    private Timer timer_send;
    private TimerTask task_send;
    private MqttAndroidClient client;
    private boolean connection;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new Timer();
        timer_send = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {

                    if (isConnectedViaWifi()) {
                        if(!connection) {
                            connect();
                        }
                    } else {
                        if (client != null) {
                            disConnect();
                        }
                    }

            }
        };

        task_send = new TimerTask() {
            @Override
            public void run() {
                if(connection){
                    String send=readFromFile(getBaseContext());
                    if(send!=null) {
                        send_iot(send);
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
        timer_send.scheduleAtFixedRate(task_send, 100, 60000);
    }

    private void connect (){

        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://kbjo2y.messaging.internetofthings.ibmcloud.com:1883",
                        CLIENT_IDENTIFIER);

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Intent i = new Intent("connect");
                    i.putExtra("connected","connected");
                    sendBroadcast(i);
                    connection =true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Intent i = new Intent("connect");
                    i.putExtra("connected","no connection");
                    sendBroadcast(i);
                    connection =false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void send_iot(String payload) {
        String topic = "iot-2/evt/status/fmt/json";
        byte[] encodedPayload = new byte[0];

        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            clearFile(getBaseContext());
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private boolean isConnectedViaWifi() {
        boolean isWiFi;
        ConnectivityManager cm =
                (ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }else{
            isWiFi= false;
        }
        return isWiFi;
    }

    public void disConnect() {
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Intent i = new Intent("connect");
                    i.putExtra("connected","no connection");
                    sendBroadcast(i);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Intent i = new Intent("connect");
                    i.putExtra("connected","no connection");
                    sendBroadcast(i);
                    connection =false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void clearFile(Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";
        JSONObject json = new JSONObject();
        int i = 0;
        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                while ( (receiveString = bufferedReader.readLine()) != null ) {

                    stringBuilder.append(receiveString+",\n");
                    try {
                        json.put("" + i, receiveString);
                    }
                    catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                    }
                    i++;
                }
                stringBuilder.append("}");
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        if (json.toString().length() > 5) {
            return json.toString();
        }else{
            return null;
        }


    }


}
