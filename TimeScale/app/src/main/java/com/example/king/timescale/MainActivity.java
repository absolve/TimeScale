package com.example.king.timescale;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import view.TimeScaleView;

public class MainActivity extends AppCompatActivity implements TimeScaleView.OnScrollListener {

    TimeScaleView tv_main1;
    TimeScaleView tv_main2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_main1 = (TimeScaleView) findViewById(R.id.tv_main1);
        tv_main2 = (TimeScaleView) findViewById(R.id.tv_main2);
        tv_main2.setScrollListener(this);
        //添加时间片段
        List<TimeScaleView.TimePart> time=new ArrayList<>();
        time.add(new TimeScaleView.TimePart(0,0,0,5,0,0));
        tv_main2.addTimePart(time);
    }

    @Override
    public void onScroll(int hour, int min, int sec) {

    }

    @Override
    public void onScrollFinish(int hour, int min, int sec) {
        Log.d("--onScrollFinish--","hour "+hour+" min "+min+" sec "+sec);
    }
}
