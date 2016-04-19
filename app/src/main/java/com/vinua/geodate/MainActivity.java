package com.vinua.geodate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.EdgeDetail;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private TextView clockText;
    private DecoView clockArc;
    private int clockArcIndex;
    private GeoDate lastGeoDate;
    private GeoDate.ClockFormat clockFormat;
    SharedPreferences settings;

    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
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

                switch (clockFormat) {
                    case YYMMDDCCBB:
                        clockText.setTextSize(42);
                        break;
                    default:
                        clockText.setTextSize(64);
                        break;
                }

                clockArc.addEvent(new DecoEvent.Builder(geoDate.getCentidays())
                        .setIndex(clockArcIndex)
                        .setDuration(0)
                        .build());
            }

            long stop = System.currentTimeMillis();
            long elapsed = stop - start;
            //Log.d("GeoDate", String.format("Elapsed time: %d ms", elapsed));
            handler.postDelayed(textRunnable, 1000 - elapsed);
        }
    };

    private void saveClockFormat() {
        int code;
        switch (clockFormat) {
            case CC:         code = 2; break;
            case CCBB:       code = 1; break;
            case YYMMDDCCBB: code = 0; break;
            default:         code = 0; break;
        }
        settings.edit().putInt("clockFormat", code).apply();
    }

    private void restoreClockFormat() {
        settings = getPreferences(MODE_PRIVATE);
        switch (settings.getInt("clockFormat", 0)) {
            case 2:  clockFormat = GeoDate.ClockFormat.CC; break;
            case 1:  clockFormat = GeoDate.ClockFormat.CCBB; break;
            case 0:  clockFormat = GeoDate.ClockFormat.YYMMDDCCBB; break;
            default: clockFormat = GeoDate.ClockFormat.YYMMDDCCBB; break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockText = (TextView) findViewById(R.id.textView);
        clockArc = (DecoView) findViewById(R.id.dynamicArcView);

        restoreClockFormat();
        clockArc.configureAngles(360, 180);

        // Create background track
        clockArc.addSeries(new SeriesItem.Builder(Color.parseColor("#E0E0E0")) // FIXME: Use colors.xml
                .setRange(0, 100, 100)
                .setLineWidth(40f)
                .build());

        //Create data series track
        SeriesItem elapsedTime = new SeriesItem.Builder(Color.parseColor("#4DB6AC"))
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, Color.parseColor("#11000000"), 0.2f))
                .setRange(0, 100, 0)
                .setLineWidth(40f)
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
        saveClockFormat();
        lastGeoDate = null;
        handler.removeCallbacks(textRunnable);
        handler.post(textRunnable);
    }
}
