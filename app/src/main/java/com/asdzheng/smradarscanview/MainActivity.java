package com.asdzheng.smradarscanview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private RadarScanView radar;

    int i = 0;

    int j = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radar = (RadarScanView) findViewById(R.id.radar);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
              if(i % 2 == 0) {
                  radar.stopScan();
              } else {
                  radar.startScan();
              }
            }
        });

        findViewById(R.id.btn_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radar.changeLayer();
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                j++;
                if(j%2 == 0) {
                    radar.stopClear();
                } else {
                    radar.startClear();
                }

            }
        });
    }
}
