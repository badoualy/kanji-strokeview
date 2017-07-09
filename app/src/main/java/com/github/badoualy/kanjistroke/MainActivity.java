package com.github.badoualy.kanjistroke;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

        strokeView.loadPathData(Arrays.asList("M34.25,16.25c1,1,1.48,2.38,1.5,4c0.38,33.62,2.38,59.38-11,73.25",
                                              "M36.25,19c4.12-0.62,31.49-4.78,33.25-5c4-0.5,5.5,1.12,5.5,4.75c0,2.76-0.5,49.25-0.5,69.5c0,13-6.25,4-8.75,1.75",
                                              "M37.25,38c10.25-1.5,27.25-3.75,36.25-4.5",
                                              "M37,58.25c8.75-1.12,27-3.5,36.25-4"));
    }
}
