package com.vinua.geodate;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private TextView clockText;
    private DecoView clockArc;
    private int clockArcIndex;
    private GeoDate lastGeoDate;
    private GeoDate.ClockFormat clockFormat;

    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            double longitude = new GeoLocation(context).getLongitude();
            long timestamp = System.currentTimeMillis() / 1000;
            GeoDate geoDate = new GeoDate(timestamp, longitude, false);

            if (!geoDate.equals(lastGeoDate)) {
                lastGeoDate = geoDate;

                int color = ContextCompat.getColor(context, R.color.colorPrimaryText);
                SpannableString text = new SpannableString(geoDate.toString(clockFormat));
                switch (clockFormat) {
                    case YYMMDDCCBB:
                        text.setSpan(new ForegroundColorSpan(color), 12, 14, 0);
                        text.setSpan(new ForegroundColorSpan(color), 9, 11, 0);
                        text.setSpan(new ForegroundColorSpan(color), 6, 8, 0);
                    case CCBB:
                        text.setSpan(new ForegroundColorSpan(color), 3, 5, 0);
                    case CC:
                        text.setSpan(new ForegroundColorSpan(color), 0, 2, 0);
                        break;
                }
                clockText.setText(text, TextView.BufferType.SPANNABLE);

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

        clockFormat = GeoDate.ClockFormat.YYMMDDCCBB;

        clockArc.configureAngles(360, 180);

        // Create background track
        clockArc.addSeries(new SeriesItem.Builder(Color.parseColor("#E0E0E0")) // FIXME: Use colors.xml
                .setRange(0, 100, 100)
                .setLineWidth(32f)
                .build());

        //Create data series track
        SeriesItem elapsedTime = new SeriesItem.Builder(Color.parseColor("#B2DFDB"))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        clockArcIndex = clockArc.addSeries(elapsedTime);

        handler.post(textRunnable);
    }

    public void changeClockFormat(View view) {
        switch (clockFormat) {
            case YYMMDDCCBB:
                clockFormat = GeoDate.ClockFormat.CCBB;
                break;
            case CCBB:
                clockFormat = GeoDate.ClockFormat.CC;
                break;
            case CC:
                clockFormat = GeoDate.ClockFormat.YYMMDDCCBB;
                break;
        }
        lastGeoDate = null;
        handler.removeCallbacks(textRunnable);
        handler.post(textRunnable);
    }
}
