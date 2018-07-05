package com.yjn.alisophix;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //修复版
        Toast.makeText(this, "Hello Sophix!", Toast.LENGTH_SHORT).show();
    }
}
