package com.vinua.detriwidget;

/**
 * Created by v on 4/1/16.
 */
public class ClockTime {

    private static double J2000 = 2451545.0009;
    long c;
    long b;

    public ClockTime(long timestamp, double longitude) {
        double lon = longitude;
        long now = timestamp;
        long tom = now + 86400;
        long mid = getMidnight(now, lon);

        if (mid > now) {
            tom = now;
            mid = getMidnight(now - 86400, lon);
        }

        long e = (10000 * (now - mid)) / (getMidnight(tom, lon) - mid);
        c = e / 100;
        b = e % 100;
    }

    private static double sinDeg(double num) {
        return Math.sin(num * Math.PI / 180.0);
    }

    private static double unixToJulian(long timestamp) {
        return (timestamp / 86400.0) + 2440587.5;
    }

    private static long julianToUnix(double jd) {
        return (long) ((jd - 2440587.5) * 86400.0);
    }

    private static long getMidnight(long timestamp, double longitude) {
        return julianToUnix(julianTransit(timestamp, longitude) - 0.5);
    }

    private static double julianTransit(long timestamp, double longitude) {
        double jd = unixToJulian(timestamp);

        // Julian Cycle
        double n = Math.floor(jd - J2000 + longitude / 360.0 + 0.5);

        // Approximate Solar Noon
        double noon = J2000 + n - longitude / 360.0;

        // Solar Mean Anomaly
        double anomaly = (357.5291 + 0.98560028 * (noon - J2000)) % 360.0;

        // Equation of the Center
        double center = 1.9148 * sinDeg(1.0 * anomaly)
                + 0.0200 * sinDeg(2.0 * anomaly)
                + 0.0003 * sinDeg(3.0 * anomaly);

        // Ecliptic Longitude
        double eclipticLongitude = (anomaly + center + 102.9372 + 180.0) % 360.0;

        // Solar Transit
        double transit = noon
                + 0.0053 * sinDeg(anomaly)
                - 0.0069 * sinDeg(2.0 * eclipticLongitude);

        return transit;
    }

    private double cosDeg(double num) {
        return Math.cos(num * Math.PI / 180.0);
    }

    public String toString() {
        return String.format("%02d:%02d %d", c, b, nextTick());
    }

    public long nextTick() {
        return (10 - ((100 - b) % 10)) * 864; // In milliseconds
    }
}
