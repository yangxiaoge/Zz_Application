package com.yjn.rainview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yjn.danmu.R;

import java.util.Random;


public class RainActivity extends AppCompatActivity {
    // 两两弹幕之间的间隔时间
     public static final int DELAY_TIME = 800;

    private Random random = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        System.exit(0);
    }
}