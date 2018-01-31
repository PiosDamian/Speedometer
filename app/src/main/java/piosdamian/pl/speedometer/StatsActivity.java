package piosdamian.pl.speedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import piosdamian.pl.speedometer.service.FloatingWidgetService;
import piosdamian.pl.speedometer.service.GPSService;
import piosdamian.pl.speedometer.service.StoreService;

import static piosdamian.pl.speedometer.service.StoreService.KMH;
import static piosdamian.pl.speedometer.service.StoreService.MPH;

/**
 * Created by Damian Pioś on 30.01.2018.
 */

public class StatsActivity extends AppCompatActivity {
    AppCompatTextView time_tv, distance_tv, currentSpeed_tv, maxSpeed_tv, avgSpeed_tv;
    AppCompatRadioButton kmhBtn, mphBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        registerReceiver(storeReceiver, new IntentFilter(StoreService.STORE_RECEIVER));

        time_tv = findViewById(R.id.time);
        distance_tv = findViewById(R.id.distance);
        currentSpeed_tv = findViewById(R.id.current_speed);
        maxSpeed_tv = findViewById(R.id.max_speed);
        avgSpeed_tv = findViewById(R.id.avg_speed);

        kmhBtn = findViewById(R.id.kmh);
        mphBtn = findViewById(R.id.mph);

        setUnitChecked();

        kmhBtn.setOnClickListener(onClickListener);
        mphBtn.setOnClickListener(onClickListener);

        findViewById(R.id.switch_to_widget).setOnClickListener(switchToWidget);
    }

    private BroadcastReceiver storeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setView(intent);
        }
    };
    private View.OnClickListener switchToWidget = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startService(new Intent(getApplicationContext(), FloatingWidgetService.class));
            finish();
        }
    };

    private void setView(Intent intent) {
        resolveBroadcast(intent);
    }

    private void resolveBroadcast(Intent intent) {
        Log.d(StoreService.CURRENT_SPEED, intent.getDoubleExtra(StoreService.CURRENT_SPEED, 0.0) + "");
        time_tv.post(() ->
                time_tv.setText(intent.getLongExtra(StoreService.TIME, 0) / 1000 + "s")
        );
        distance_tv.post(() ->
                distance_tv.setText(String.format(getResources().getConfiguration().locale, "%.02f", intent.getFloatExtra(StoreService.DISTANCE, 0)))
        );
        currentSpeed_tv.post(() ->
                currentSpeed_tv.setText(String.format(getResources().getConfiguration().locale, "%.02f", intent.getDoubleExtra(StoreService.CURRENT_SPEED, 0.0)))
        );
        maxSpeed_tv.post(() ->
                maxSpeed_tv.setText(String.format(getResources().getConfiguration().locale, "%.02f", intent.getDoubleExtra(StoreService.MAX_SPEED, 0.0)))
        );
        avgSpeed_tv.post(() ->
                avgSpeed_tv.setText(String.format(getResources().getConfiguration().locale, "%.02f", intent.getDoubleExtra(StoreService.AVG_SPEED, 0.0)))
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(storeReceiver);
        stopService(new Intent(getApplicationContext(), GPSService.class));
        stopService(new Intent(getApplicationContext(), StoreService.class));
    }

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
        }
    };

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
}
