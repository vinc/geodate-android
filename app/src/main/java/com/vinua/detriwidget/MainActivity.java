package com.vinua.detriwidget;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private TextView clockText;
    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            double longitude = new ClockLocation(context).getLongitude();
            ClockTime clockTime = new ClockTime(System.currentTimeMillis() / 1000, longitude, true);
            String text = clockTime.toString();
            clockText.setText(text);
            handler.postDelayed(textRunnable, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockText = (TextView) findViewById(R.id.textView);
        handler.postDelayed(textRunnable, 1000);
    }
}
