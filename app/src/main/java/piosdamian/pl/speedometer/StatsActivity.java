package piosdamian.pl.speedometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.RadioButton;

import piosdamian.pl.speedometer.adapter.ChartsFragment;
import piosdamian.pl.speedometer.service.FloatingWidgetService;
import piosdamian.pl.speedometer.service.StoreService;

import static piosdamian.pl.speedometer.service.StoreService.KMH;
import static piosdamian.pl.speedometer.service.StoreService.MPH;

/**
 * Created by Damian Pioś on 31.01.2018.
 */

public class StatsActivity extends AppCompatActivity {
    private static final int NUM_PAGES = 2;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    AppCompatRadioButton kmhBtn, mphBtn;
    AppCompatButton startWidget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_chart);

        mPager = findViewById(R.id.stats_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        startWidget = findViewById(R.id.switch_to_widget);
        startWidget.setOnClickListener(switchToWidget);

        kmhBtn = findViewById(R.id.kmh);
        mphBtn = findViewById(R.id.mph);

        setUnitChecked();

        kmhBtn.setOnClickListener(onClickListener);
        mphBtn.setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0)
            super.onBackPressed();
        else
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new StatsFragment();
                case 1:
                    return new ChartsFragment();
                default:
                    return new StatsFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
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

    private View.OnClickListener switchToWidget = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startService(new Intent(StatsActivity.this.getApplicationContext(), FloatingWidgetService.class));
            finish();
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
