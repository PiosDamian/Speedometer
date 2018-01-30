package piosdamian.pl.speedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import piosdamian.pl.speedometer.service.FloatingWidgetService;
import piosdamian.pl.speedometer.service.GPSService;
import piosdamian.pl.speedometer.service.StoreService;

/**
 * Created by Damian Pioś on 30.01.2018.
 */

public class ChartActivity extends AppCompatActivity {
    LineChart speedChart_lc;
    AppCompatTextView time_tv, distance_tv, currentSpeed_tv, maxSpeed_tv, avgSpeed_tv;

    List<Entry> entries = new ArrayList<>();
    LineDataSet dataSet;
    boolean foo = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        registerReceiver(storeReceiver, new IntentFilter(StoreService.STORE_RECEIVER));

        speedChart_lc = findViewById(R.id.speed_chart);
        time_tv = findViewById(R.id.time);
        distance_tv = findViewById(R.id.distance);
        currentSpeed_tv = findViewById(R.id.current_speed);
        maxSpeed_tv = findViewById(R.id.max_speed);
        avgSpeed_tv = findViewById(R.id.avg_speed);
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
        if (foo) {
            setDataSet();
            refreshView();
        }
        foo = false;
    }

    private void resolveBroadcast(Intent intent) {
        intent.getDoubleArrayExtra(StoreService.SPEED_HISTORY);
        setEntries(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5});
        time_tv.post(() ->
                time_tv.setText(intent.getLongExtra(StoreService.TIME, 0) / 1000 + "")
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

    public void setEntries(double[] entry) {
        int i = 0;
        for (double data : entry) {
            entries.add(new Entry(i, (float) data));
            i++;
        }
    }

    public void setDataSet() {
        dataSet = new LineDataSet(entries, getString(R.string.speed_label));
    }

    private void refreshView() {
        LineData lineData = new LineData(dataSet);
        speedChart_lc.setData(lineData);
        speedChart_lc.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(storeReceiver);
        stopService(new Intent(getApplicationContext(), GPSService.class));
        stopService(new Intent(getApplicationContext(), StoreService.class));
    }
}
