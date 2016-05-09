package com.vinua.geodate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
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
    private int clockArcSunriseIndex;
    private int clockArcSunsetIndex;
    private GeoDate lastGeoDate;
    private GeoDate.ClockFormat clockFormat;
    SharedPreferences settings;

    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            Context context = getApplicationContext();

            try {
                long timestamp = System.currentTimeMillis() / 1000;
                GeoLocation geoLocation = new GeoLocation(context);
                double latitude = geoLocation.getLatitude();
                double longitude = geoLocation.getLongitude();
                GeoDate geoDate = new GeoDate(timestamp, latitude, longitude, false);

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

                    float c = geoDate.getCentidays();
                    float b = geoDate.getDimidays();
                    float percent = c + b / 100.0f;

                    clockArc.addEvent(new DecoEvent.Builder(percent)
                            .setIndex(clockArcIndex)
                            .setDuration(0)
                            .build());

                    long timestamp_midnight = geoDate.getTimeOfMidnight();
                    long timestamp_sunrise = geoDate.getTimeOfSunrise();
                    long timestamp_sunset = geoDate.getTimeOfSunset();
                    float sunrise = (timestamp_sunrise - timestamp_midnight) * 100.00f / 86400.00f;
                    float sunset = (timestamp_sunset - timestamp_midnight) * 100.00f / 86400.00f;

                    clockArc.addEvent(new DecoEvent.Builder(sunrise)
                            .setIndex(clockArcSunriseIndex)
                            .setDuration(0)
                            .build());

                    clockArc.addEvent(new DecoEvent.Builder(100 - sunset)
                            .setIndex(clockArcSunsetIndex)
                            .setDuration(0)
                            .build());
                }
            } catch (GeoLocation.LocationNotFoundException e) {
                clockText.setTextSize(24);
                clockText.setText("Location not found");
                clockArc.addEvent(new DecoEvent.Builder(0)
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

        float fgArcLineWidth = 40f;
        float bgArcLineWidth = 40f;

        SeriesItem seriesItem;

        // Background ark
        clockArc.addSeries(
                new SeriesItem.Builder(Color.parseColor("#E0E0E0"))
                        .setCapRounded(false)
                        .setRange(0, 100, 100)
                        .setLineWidth(bgArcLineWidth)
                        .build()
        );

        // Clock ark
        seriesItem = new SeriesItem.Builder(Color.parseColor("#4DB6AC"))
                .setCapRounded(false)
                .addEdgeDetail(new EdgeDetail(EdgeDetail.EdgeType.EDGE_INNER, Color.parseColor("#11000000"), 0.2f))
                .setRange(0, 100, 0)
                .setLineWidth(fgArcLineWidth)
                .build();
        clockArcIndex = clockArc.addSeries(seriesItem);

        // Darker background before sunrise
        seriesItem = new SeriesItem.Builder(Color.parseColor("#22000000")) // FIXME: Use colors.xml
                .setCapRounded(false)
                .setRange(0, 100, 0)
                .setLineWidth(bgArcLineWidth)
                .build();
        clockArcSunriseIndex = clockArc.addSeries(seriesItem);

        // Darker background after sunset
        seriesItem = new SeriesItem.Builder(Color.parseColor("#22000000")) // FIXME: Use colors.xml
                .setSpinClockwise(false)
                .setCapRounded(false)
                .setRange(0, 100, 0)
                .setLineWidth(bgArcLineWidth)
                .build();
        clockArcSunsetIndex = clockArc.addSeries(seriesItem);

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
