package piosdamian.pl.speedometer.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Damian Pioś on 26.01.2018.
 */

public class GPSService extends Service implements LocationListener {
    public static final String RECEIVER = "piosdamian.pl.receiver";
    public static final long NOTIFY_INTERVAL = 1500;
    public static final String DISTANCE = "distance";
    private static final String provider = LocationManager.GPS_PROVIDER;

    private LocationManager locationManager;
    private Location location, lastLocation;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private Intent intent;

    private float distance = 0;
    private float lastDistance = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, NOTIFY_INTERVAL);
        intent = new Intent(RECEIVER);
        location = null;
        lastLocation = null;

    }

    @SuppressLint("MissingPermission")
    private void retrieveLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(provider, NOTIFY_INTERVAL, 1, this);

        if (locationManager != null) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l != null) {
                this.location = l;
                update();
            }
        }
    }

    private void update() {

        if (lastLocation != null) {
            lastDistance = distance;
            distance = location.distanceTo(lastLocation);
            intent.putExtra(DISTANCE, (distance + lastDistance) / 2);
            sendBroadcast(intent);
        }
        lastLocation = location;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {
            mHandler.post(() -> {
                retrieveLocation();
            });
        }
    }
}
