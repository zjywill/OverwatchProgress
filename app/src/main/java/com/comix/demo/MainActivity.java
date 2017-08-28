package com.comix.demo;

import android.app.Activity;
import android.os.Bundle;

import com.comix.overwatch.HiveProgressView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HiveProgressView progressView = (HiveProgressView) findViewById(R.id.hive_progress);
        progressView.setRainbow(false);
        progressView.setColor(0x000000);
    }
}
