package piosdamian.pl.speedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Damian Pioś on 30.01.2018.
 */

public class StoreService extends Service {
    public static final String STORE_RECEIVER = "pl.piosdamian.speedreceiver";
    public static final String CURRENT_SPEED = "speed";
    public static final String SPEED_HISTORY = "speed history";
    public static final String DISTANCE = GPSService.DISTANCE;
    public static final String MAX_SPEED = "max speed";
    public static final String AVG_SPEED = "avg speed";
    public static final String TIME = "time";

    private Intent intent;
    private float overalDistance = 0;
    private List<Double> speedHistory = new ArrayList<>();
    private double avgSpeed;
    private long timeSinceStart;
    private long startTime;
    private double topSpeed = 0;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private long startT = 0, currentT = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            startT = currentT;
            currentT = new Date().getTime();

            float dst = intent.getFloatExtra(GPSService.DISTANCE, 0);
            overalDistance += dst;
            countAvgSpeed();
            timeSinceStart = new Date().getTime() - startTime;
            if (startT != 0)
                speedHistory.add(countSpeed(dst, currentT - startT));
            else
                speedHistory.add(0.0);

            update();
        }

        private Double countSpeed(float distance, long time) {
            double speed = 0;
            if (time != 0) {
                speed = (distance / (time / 1000)) * 3.6;
                topSpeed = speed > topSpeed && speed != Double.POSITIVE_INFINITY ? speed : topSpeed;
            }
            return speed != Double.POSITIVE_INFINITY ? speed : 0.0;
        }

        private void update() {
            intent.putExtra(CURRENT_SPEED, speedHistory.get(speedHistory.size() - 1));
            intent.putExtra(SPEED_HISTORY, speedHistory.toArray(new Double[0]));
            intent.putExtra(AVG_SPEED, avgSpeed);
            intent.putExtra(TIME, timeSinceStart);
            intent.putExtra(DISTANCE, overalDistance);
            intent.putExtra(MAX_SPEED, topSpeed);
            sendBroadcast(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        startTime = new Date().getTime();
        intent = new Intent(STORE_RECEIVER);
        registerReceiver(broadcastReceiver, new IntentFilter(GPSService.DISTANCE_RECEIVER));
    }

    private void countAvgSpeed() {
        avgSpeed = (overalDistance / (timeSinceStart / 1000)) * 3.6;
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
