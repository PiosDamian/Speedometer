package piosdamian.pl.speedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import piosdamian.pl.speedometer.R;

/**
 * Created by Damian Pioś on 25.01.2018.
 */

public class FloatingWidgetService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private AppCompatTextView speedTV;
    private boolean MPH = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(getApplicationContext(), GPSService.class));
        registerReceiver(broadcastReceiver, new IntentFilter(GPSService.RECEIVER));

        floatingView = LayoutInflater.from(this).inflate(R.layout.activity_widget, null);
        speedTV = floatingView.findViewById(R.id.speed_tv);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        final View collapsedView = floatingView.findViewById(R.id.collapse_view);
        final View expandedView = floatingView.findViewById(R.id.expanded_container);

        ImageView closeButtonCollapsed = floatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener((view) -> {
            stopSelf();
        });

        ImageView closeButtonExpanded = floatingView.findViewById(R.id.close_btn_expanded);
        closeButtonExpanded.setOnClickListener((view) -> {
            collapsedView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.GONE);
        });

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
                String speed = String.format(getResources().getConfiguration().locale,"%.02f", countSpeed(distance, GPSService.NOTIFY_INTERVAL));
                speedTV.setText(speed);
            });
        }
    };

    private double countSpeed(double distance, long time) {
        if (MPH)
            distance = distance * 0.621;

        return (distance / time) * 3.6;
    }
}
