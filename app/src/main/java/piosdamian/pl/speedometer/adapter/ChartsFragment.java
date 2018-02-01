package piosdamian.pl.speedometer.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;

import piosdamian.pl.speedometer.R;

/**
 * Created by Damian Pioś on 31.01.2018.
 */

public class ChartsFragment extends Fragment {

    LineChart statsChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_stats_chart, container, false);

        statsChart = rootView.findViewById(R.id.stats_chart);
        return rootView;
    }
}
