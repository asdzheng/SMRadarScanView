package com.asdzheng.smradarscanview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private RadarScanView radar;

    int i = 0;

    int j = 0;

    private Handler handler;

    int num = 0;

    Runnable textRun = new Runnable() {
        @Override
        public void run() {
            radar.setCollectionNum(num);
            radar.setUnit("M");
            num++;
            handler.postDelayed(textRun, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        radar = (RadarScanView) findViewById(R.id.radar);
        radar.setClearTime(360);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
              if(i % 2 == 0) {
                  radar.stopScan();
                  radar.setWhiteLayer(true);
                  handler.removeCallbacksAndMessages(null);
              } else {
                  radar.startScan();
                  handler.post(textRun);
              }
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
