package piosdamian.pl.speedometer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;

import piosdamian.pl.speedometer.R;
import piosdamian.pl.speedometer.StatsActivity;

import static piosdamian.pl.speedometer.service.StoreService.KMH;
import static piosdamian.pl.speedometer.service.StoreService.MPH;

/**
 * Created by Damian Pioś on 25.01.2018.
 */

public class FloatingWidgetService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private AppCompatTextView speedTV;
    private View collapsedView, expandedView;
    RadioButton kmhBtn, mphBtn;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.mph:
                    if (((RadioButton) view).isChecked()) {
                        StoreService.changeUnits(MPH);
                    }
                    break;
                case R.id.kmh:
                    if (((RadioButton) view).isChecked()) {
                        StoreService.changeUnits(KMH);
                    }
                    break;
            }
            new android.os.Handler().postDelayed(() ->
                    closeExpandedView(), 500
            );
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double speed = intent.getDoubleExtra(StoreService.CURRENT_SPEED, 0);
            speedTV.post(() -> {
                String speedText = String.format(getResources().getConfiguration().locale, "%.02f", speed);
                speedTV.setText(speedText);
            });
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(broadcastReceiver, new IntentFilter(StoreService.STORE_RECEIVER));

        floatingView = LayoutInflater.from(this).inflate(R.layout.activity_widget, null);
        speedTV = floatingView.findViewById(R.id.speed_tv);

        kmhBtn = floatingView.findViewById(R.id.kmh);
        mphBtn = floatingView.findViewById(R.id.mph);
        kmhBtn.setOnClickListener(onClickListener);
        mphBtn.setOnClickListener(onClickListener);

        setUnitChecked();

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
            private final Handler handler = new Handler();
            private Runnable longPress = new Runnable() {
                @Override
                public void run() {
                    startActivity();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.postDelayed(longPress, 200);
                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(longPress);
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
                        handler.removeCallbacks(longPress);
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void startActivity() {
        startActivity(new Intent(this, StatsActivity.class));
        stopSelf();
    }

    private void setUnitChecked() {
        int units = StoreService.getUnits();
        if (units == KMH) {
            kmhBtn.setChecked(true);
            mphBtn.setChecked(false);
        } else {
            kmhBtn.setChecked(false);
            mphBtn.setChecked(true);
        }
    }

    private void stopAll() {
        stopService(new Intent(getApplicationContext(), GPSService.class));
        stopService(new Intent(getApplicationContext(), StoreService.class));
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

    private void closeExpandedView() {
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
    }
}
