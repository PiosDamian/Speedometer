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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Damian Pioś on 26.01.2018.
 */

public class GPSService extends Service implements LocationListener {
    public static final String RECEIVER = "piosdamian.pl.receiver";
    public static final long NOTIFY_INTERVAL = 1000;
    public static final String DISTANCE = "distance";

    private LocationManager locationManager;
    private Location location, lastLocation;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private Intent intent;

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

        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, NOTIFY_INTERVAL, 0, this);
            if (locationManager != null) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (location == null || l.getAccuracy() < location.getAccuracy()) {
                    location = l;
                }
            }
        }

        if (location != null)
            update();
    }

    private void update() {
        if (lastLocation != null) {
            intent.putExtra(DISTANCE, location.distanceTo(lastLocation));
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
