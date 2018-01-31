package piosdamian.pl.speedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Created by Damian Pioś on 30.01.2018.
 */

public class StoreService extends Service {
    public static final String STORE_RECEIVER = "pl.piosdamian.speedreceiver";
    public static final String CURRENT_SPEED = "speed";
    public static final String DISTANCE = GPSService.DISTANCE;
    public static final String MAX_SPEED = "max speed";
    public static final String AVG_SPEED = "avg speed";
    public static final String TIME = "time";

    public static final String UNITS = "units";
    public static final int DEF_PREF = -1;
    public static final int KMH = 0;
    public static final int MPH = 1;

    private Intent intent;
    private static SharedPreferences preferences;
    private static int units = KMH;

    private static float overallDistance = 0;
    private static double topSpeed = 0;
    private double currentSpeed;
    private double avgSpeed;
    private long timeSinceStart;
    private long startTime;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private long startT = 0, currentT = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            startT = currentT;
            currentT = new Date().getTime();

            float dst = intent.getFloatExtra(GPSService.DISTANCE, 0);
            if (units == MPH)
                dst *= 0.621;
            overallDistance += dst;
            countAvgSpeed();
            timeSinceStart = new Date().getTime() - startTime;
            currentSpeed = countSpeed(dst, currentT - startT);
            update();
        }

        private Double countSpeed(float distance, long time) {
            double speed;
            if (time != 0) {
                speed = (distance / (time / 1000)) * 3.6;
                if (speed != Double.POSITIVE_INFINITY && speed != Double.NEGATIVE_INFINITY)
                    topSpeed = speed > topSpeed && speed != Double.POSITIVE_INFINITY ? speed : topSpeed;
                else
                    speed = 0;
            } else {
                speed = topSpeed;
            }
            return speed;
        }

        private void update() {
            intent.putExtra(CURRENT_SPEED, currentSpeed);
            intent.putExtra(AVG_SPEED, avgSpeed);
            intent.putExtra(TIME, timeSinceStart);
            intent.putExtra(DISTANCE, overallDistance);
            intent.putExtra(MAX_SPEED, topSpeed);
            sendBroadcast(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(UNITS, MODE_PRIVATE);
        int pref = preferences.getInt(UNITS, DEF_PREF);
        if (pref == DEF_PREF)
            setUnits(KMH);
        else
            setUnits(pref);

        startTime = new Date().getTime();
        intent = new Intent(STORE_RECEIVER);
        registerReceiver(broadcastReceiver, new IntentFilter(GPSService.DISTANCE_RECEIVER));
    }

    private void countAvgSpeed() {
        avgSpeed = (overallDistance / (timeSinceStart / 1000)) * 3.6;
    }

    private static void setUnits(int unit) {
        int tmpUnit = units;
        units = unit;
        if (tmpUnit != units){
            if (units == MPH) {
                overallDistance *= 0.621;
                topSpeed *= 0.621;
            }
            else {
                overallDistance *= 1.61;
                topSpeed *= 1.61;
            }
        }
    }

    public static void changeUnits(int unit) {
        setUnits(unit);
        if (preferences != null)
            preferences.edit().putInt(UNITS, units).commit();
    }

    public static int getUnits() {
        return units;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
