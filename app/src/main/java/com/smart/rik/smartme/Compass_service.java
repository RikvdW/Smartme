package com.smart.rik.smartme;

import android.app.Service;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.math.BigDecimal;

/**
 * Created by Rik on 22/05/2017.
 */

public class Compass_service extends Service implements SensorEventListener {

    private float currentDegree = 0f;

    private SensorManager sensorM;

    private Sensor accelerometer;
    private Sensor magnetometer;


    private double bearing;
    private GeomagneticField geomagneticField;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorM = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorM.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorM.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            // east degrees of true North
            bearing = mOrientation[0];
            // convert from radians to degrees
            bearing = Math.toDegrees(bearing);

            // fix difference between true North and magnetical North
            if (geomagneticField != null) {
                bearing += geomagneticField.getDeclination();
            }

            Intent i = new Intent("comp_update");
            i.putExtra("degrees",round(bearing,2));
            sendBroadcast(i);
        }

    }

    public static BigDecimal round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorM.unregisterListener(this, accelerometer);
        sensorM.unregisterListener(this, magnetometer);

    }

}
