package com.github.badoualy.kanjistroke;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final KanjiStrokeView strokeView = findViewById(R.id.view_stroke);
        strokeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strokeView.startDrawAnimation();
            }
        });

        strokeView.loadPathData(Arrays.asList("M 0,0 c 0.67,1.54 1,3.26 1,5.79 c 0,14.77 -0.33,47 -1.5,60.48 c -0.26,3 1.01,3.97 2.81,3.8 c 16.11,-1.53 44.49,-2.91 60.2,-2.56 c 3.32,0.07 6.12,0.24 8.17,0.71"));
    }
}
