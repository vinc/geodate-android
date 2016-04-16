package com.vinua.geodate;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private TextView clockText;
    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            double longitude = new GeoLocation(context).getLongitude();
            GeoDate geoDate = new GeoDate(System.currentTimeMillis() / 1000, longitude, true);
            SpannableString text = new SpannableString(geoDate.toString());
            int color = ContextCompat.getColor(context, R.color.colorPrimaryText);
            text.setSpan(new ForegroundColorSpan(color), 0, 2, 0);
            text.setSpan(new ForegroundColorSpan(color), 3, 5, 0);
            text.setSpan(new ForegroundColorSpan(color), 6, 8, 0);
            text.setSpan(new ForegroundColorSpan(color), 9, 11, 0);
            text.setSpan(new ForegroundColorSpan(color), 12, 14, 0);
            clockText.setText(text, TextView.BufferType.SPANNABLE);
            handler.postDelayed(textRunnable, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockText = (TextView) findViewById(R.id.textView);
        handler.postDelayed(textRunnable, 0);
    }
}
