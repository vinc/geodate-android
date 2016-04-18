package com.vinua.geodate;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private TextView clockText;
    private DecoView clockArc;
    private int clockArcIndex;
    private long lastClockArcUpdate;


    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            double longitude = new GeoLocation(context).getLongitude();
            long timestamp = System.currentTimeMillis() / 1000;
            GeoDate geoDate = new GeoDate(timestamp, longitude, true);

            SpannableString text = new SpannableString(geoDate.toString());
            int color = ContextCompat.getColor(context, R.color.colorPrimaryText);
            text.setSpan(new ForegroundColorSpan(color), 0, 2, 0);
            text.setSpan(new ForegroundColorSpan(color), 3, 5, 0);
            text.setSpan(new ForegroundColorSpan(color), 6, 8, 0);
            text.setSpan(new ForegroundColorSpan(color), 9, 11, 0);
            text.setSpan(new ForegroundColorSpan(color), 12, 14, 0);
            clockText.setText(text, TextView.BufferType.SPANNABLE);

            // No need to update the clockArc too often
            if (timestamp - lastClockArcUpdate > 5) {
                lastClockArcUpdate = timestamp;

                clockArc.addEvent(new DecoEvent.Builder(geoDate.getCentidays())
                        .setIndex(clockArcIndex)
                        .setDuration(0)
                        .build());
            }

            handler.postDelayed(textRunnable, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockText = (TextView) findViewById(R.id.textView);
        clockArc = (DecoView) findViewById(R.id.dynamicArcView);

        lastClockArcUpdate = 0;
        clockArc.configureAngles(360, 180);

        // Create background track
        clockArc.addSeries(new SeriesItem.Builder(Color.parseColor("#DDDDDD"))
                .setRange(0, 100, 100)
                .setLineWidth(32f)
                .build());

        //Create data series track
        SeriesItem seriesItem1 = new SeriesItem.Builder(Color.parseColor("#AADDDD"))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        clockArcIndex = clockArc.addSeries(seriesItem1);

        handler.postDelayed(textRunnable, 0);
    }
}
