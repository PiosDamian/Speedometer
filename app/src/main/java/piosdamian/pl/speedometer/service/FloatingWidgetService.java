package piosdamian.pl.speedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;

import piosdamian.pl.speedometer.R;

/**
 * Created by Damian Pioś on 25.01.2018.
 */

public class FloatingWidgetService extends Service {
    private static final String UNITS = "units";
    private static final int DEF_PREF = -1;
    private static final int KMH = 0;
    private static final int MPH = 1;

    private WindowManager windowManager;
    private View floatingView;
    private AppCompatTextView speedTV;
    private int units = KMH;
    private SharedPreferences preferences;
    private View collapsedView, expandedView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(UNITS, MODE_PRIVATE);
        int pref = preferences.getInt(UNITS, DEF_PREF);
        if (pref == DEF_PREF)
            units = KMH;
        else
            units = pref;

        startService(new Intent(getApplicationContext(), GPSService.class));
        registerReceiver(broadcastReceiver, new IntentFilter(GPSService.RECEIVER));

        floatingView = LayoutInflater.from(this).inflate(R.layout.activity_widget, null);
        speedTV = floatingView.findViewById(R.id.speed_tv);

        RadioButton kmhBtn, mphBtn;
        kmhBtn = floatingView.findViewById(R.id.kmh);
        mphBtn = floatingView.findViewById(R.id.mph);
        kmhBtn.setOnClickListener(onClickListener);
        mphBtn.setOnClickListener(onClickListener);

        if (units == KMH) {
            kmhBtn.setChecked(true);
            mphBtn.setChecked(false);
        } else {
            kmhBtn.setChecked(false);
            mphBtn.setChecked(true);
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT :
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        collapsedView = floatingView.findViewById(R.id.collapse_view);
        expandedView = floatingView.findViewById(R.id.expanded_container);

        ImageView closeButtonCollapsed = floatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener((view) -> {
            stopAll();
        });

        ImageView closeButtonExpanded = floatingView.findViewById(R.id.close_btn_expanded);
        closeButtonExpanded.setOnClickListener((view) -> closeExpandedView());

        floatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void stopAll() {
        stopService(new Intent(getApplicationContext(), GPSService.class));
        stopSelf();
    }

    private boolean isViewCollapsed() {
        return floatingView == null || floatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float distance = intent.getFloatExtra(GPSService.DISTANCE, 0);
            speedTV.post(() -> {
                String speed = String.format(getResources().getConfiguration().locale, "%.02f", countSpeed(distance, GPSService.NOTIFY_INTERVAL));
                speedTV.setText(speed);
            });
        }
    };

    private double countSpeed(double distance, long time) {
        float t = (float) time / 1000;
        if (units == MPH)
            distance = distance * 0.621;

        return (distance / t) * 3.6;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.mph:
                    if (((RadioButton) view).isChecked()) {
                        setUnits(MPH);
                    }
                    break;
                case R.id.kmh:
                    if (((RadioButton) view).isChecked()) {
                        setUnits(KMH);
                    }
                    break;
            }
            closeExpandedView();
        }
    };

    private void closeExpandedView() {
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
    }

    private void setUnits(int unit) {
        units = unit;
        preferences.edit().putInt(UNITS, units).commit();
    }
}
