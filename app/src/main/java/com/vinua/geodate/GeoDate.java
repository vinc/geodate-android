package com.vinua.geodate;

public class GeoDate {
    private static double J2000 = 2451545.0009;

    @Override
    public int hashCode() {
        long result = 17;
        result = 31 * result + this.y;
        result = 31 * result + this.m;
        result = 31 * result + this.d;
        result = 31 * result + this.c;
        result = 31 * result + this.b;

        return (int) result; //FIXME: Change long to int
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GeoDate)) {
            return false;
        }

        GeoDate other = (GeoDate) o;

        return other.getYears() == this.getYears()
                && other.getMonths() == this.getMonths()
                && other.getDays() == this.getDays()
                && other.getCentidays() == this.getCentidays()
                && other.getDimidays() == this.getDimidays();
    }

    public long getYears() {
        return y;
    }

    public long getMonths() {
        return y;
    }

    public long getDays() {
        return d;
    }

    public long getCentidays() {
        return c;
    }

    public long getDimidays() {
        return b;
    }

    public enum ClockFormat { CC, CCBB, YYMMDDCCBB }

    boolean isShortDate;
    long y;
    long m;
    long d;
    long c;
    long b;

    public GeoDate(long timestamp, double longitude, boolean computeOnlyShortDate) {
        isShortDate = computeOnlyShortDate;

        double lon = longitude;
        long now = timestamp;
        long tom = now + 86400;
        long mid = getMidnight(now, lon);

        if (mid > now) {
            tom = now;
            mid = getMidnight(now - 86400, lon);
        }

        int n = 2 + (int) (now / 86400 / 365);
        long[] seasonalEvents = new long[n];

        for (int i = 0; i < n; i++) {
            // FIXME: Avoid bugs by picking a date around the middle of the year
            long newYearTimestamp = (long) (((double) i) * 86400.0 * 365.25);
            long midYearTimestamp = newYearTimestamp - 180 * 86400;

            seasonalEvents[i] = getDecemberSolstice(midYearTimestamp);
            //Log.d("Detri", String.format("winter solstice %d", seasonalEvents[i]));
        }

        long[] newMoons = new long[n * 13];
        for (int i = 0; i < n * 13; i++) {
            // Lunations since the first new moon of January 2000
            double lunationNumber = ((double) i) - 371.0;

            newMoons[i] = getNewMoon(lunationNumber);
            //Log.d("Detri", String.format("new moon #%f %d", lunationNumber, seasonalEvents[i]));
        }

        d = 0;
        m = 0;
        y = 0;

        if (!computeOnlyShortDate) {
            long t = getMidnight(0, lon);

            if (t < 0) {
                t += 86400;
            }

            int i = 1;
            int j = 1;
            while (t < mid - 2000) { // Mean solar day approximation
                d += 1;
                t += 86400;

                //Log.d("Detri", String.format("%d => %02d:%02d:%02d", t, y, m, d));
                if (newMoons[j] < (t + 86400)) { // New month
                    //Log.d("Detri", String.format("%d => new moon %d", t, newMoons[j]));

                    j += 1;
                    d = 0;
                    m += 1;
                    if (seasonalEvents[i] < (t + 86400)) { // New year
                        //Log.d("Detri", String.format("%d => winter solstice %d", t, seasonalEvents[i]));
                        i += 1;
                        m = 0;
                        y += 1;
                    }
                }
            }
        }

        long e = (10000 * (now - mid)) / (getMidnight(tom, lon) - mid);
        c = e / 100;
        b = e % 100;
    }

    @Override
    public String toString() {
        return this.toString(ClockFormat.YYMMDDCCBB);
    }

    public String toString(ClockFormat clockFormat) {
        switch (clockFormat) {
            case CC:
                return String.format("%02d", c);
            case CCBB:
                return String.format("%02d:%02d", c, b);
            case YYMMDDCCBB:
            default:
                return String.format("%02d:%02d:%02d:%02d:%02d", y, m, d, c, b);
        }
    }

    // Returns result in milliseconds
    public long nextTick() {
        long oneDimiday = 8640; // TODO: Compute real value

        return oneDimiday * (isShortDate ? (100 - b) : 1);
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
        return noon + 0.0053 * sinDeg(anomaly)
                - 0.0069 * sinDeg(2.0 * eclipticLongitude);
    }

    private static double computeJdme(int i, double m) {
        double[][] jdmeTerms = {
                {2451623.80984, 365242.37404, 0.05169, -0.00411, -0.00057}, // March Equinoxe
                {2451716.56767, 365241.62603, 0.00325, 0.00888, -0.00030}, // June Solstice
                {2451810.21715, 365242.01767, -0.11575, 0.00337, 0.00078}, // September Equinoxe
                {2451900.05952, 365242.74049, -0.06223, -0.00823, 0.00032}  // December Solstice
        };

        double a = jdmeTerms[i][0];
        double b = jdmeTerms[i][1];
        double c = jdmeTerms[i][2];
        double d = jdmeTerms[i][3];
        double e = jdmeTerms[i][4];

        return a + b * Math.pow(m, 1.0)
                + c * Math.pow(m, 2.0)
                + d * Math.pow(m, 3.0)
                + e * Math.pow(m, 4.0);
    }

    private static double computePeriodicTerms(double t) {
        double[][] terms = {
                {485.0, 324.96, 1934.136},
                {203.0, 337.23, 32964.467},
                {199.0, 342.08, 20.186},
                {182.0, 27.85, 445267.112},
                {156.0, 73.14, 45036.886},
                {136.0, 171.52, 22518.443},
                {77.0, 222.54, 65928.934},
                {74.0, 296.72, 3034.906},
                {70.0, 243.58, 9037.513},
                {58.0, 119.81, 33718.147},
                {52.0, 297.17, 150.678},
                {50.0, 21.02, 2281.226},
                {45.0, 247.54, 29929.562},
                {44.0, 325.15, 31555.956},
                {29.0, 60.93, 4443.417},
                {18.0, 155.12, 67555.328},
                {17.0, 288.79, 4562.452},
                {16.0, 198.04, 62894.029},
                {14.0, 199.76, 31436.921},
                {12.0, 95.39, 14577.848},
                {12.0, 287.11, 31931.756},
                {12.0, 320.81, 34777.259},
                {9.0, 227.73, 1222.114},
                {8.0, 15.45, 16859.074}
        };

        double sum = 0.0;

        for (double[] abc : terms) {
            double a = abc[0];
            double b = abc[1];
            double c = abc[2];

            sum += a * cosDeg(b + c * t);
        }

        return sum;
    }

    // Returns the Julian year for a given Julian ephemeris day
    private static double jdeToJulianYear(double jde) {
        return 2000.0 + (jde - J2000) / 365.25;
    }

    private static long getSunEphemeris(int i, long timestamp) {
        double jd = unixToJulian(timestamp);

        double y = Math.floor(jdeToJulianYear(jd));

        // Convert AD year to millenia, from 2000 AD
        double m = (y - 2000.0) / 1000.0;

        double jdme = computeJdme(i, m);

        // Julian century
        double t = (jdme - J2000) / 36525.0;

        double w = 35999.373 * t - 2.47;

        double l = 1.0 + 0.0334 * cosDeg(w) + 0.0007 * cosDeg(2.0 * w);

        double s = computePeriodicTerms(t);

        return julianToUnix(jdme + (0.00001 * s) / l);
    }

    private static double cosDeg(double num) {
        return Math.cos(num * Math.PI / 180.0);
    }

    // From "Astronomical Algorithms"
    // By Jean Meeus
    private static long getMoonPhase(int phase, double lunationNumber) {
        double k = lunationNumber;
        double t = k / 1236.85;

        double e = 1.0 - 0.002516 * t - 0.0000074 * Math.pow(t, 2.0);

        // Sun's mean anomaly at time JDE
        double s = 2.5534
                + 29.1053567 * k
                - 0.0000014 * Math.pow(t, 2)
                - 0.00000011 * Math.pow(t, 3);

        // Moon's mean anomaly
        double m = 201.5643
                + 385.81693528 * k
                + 0.0107582 * Math.pow(t, 2)
                + 0.00001238 * Math.pow(t, 3)
                - 0.000000058 * Math.pow(t, 4);

        // Moon's argument of latitude
        double f = 160.7108
                + 390.67050284 * k
                - 0.0016118 * Math.pow(t, 2)
                - 0.00000227 * Math.pow(t, 3)
                + 0.000000011 * Math.pow(t, 4);

        // Longitude of the ascending node of the lunar orbit
        double o = 124.7746
                - 1.56375588 * k
                + 0.0020672 * Math.pow(t, 2)
                + 0.00000215 * Math.pow(t, 3);

        e = ((e % 360.0) + 360.0) % 360.0;
        s = ((s % 360.0) + 360.0) % 360.0;
        m = ((m % 360.0) + 360.0) % 360.0;
        f = ((f % 360.0) + 360.0) % 360.0;
        o = ((o % 360.0) + 360.0) % 360.0;

        double jde = 2451550.097660
                + 29.530588861 * k
                + 0.000154370 * Math.pow(t, 2)
                - 0.000000150 * Math.pow(t, 3)
                + 0.000000000730 * Math.pow(t, 4);

        // Correction to be added to JDE

        // [New Moon, First Quarter, Full Moon, Last Quarter]
        double[][] numCors = {
                {-0.40720, -0.62801, -0.40614, -0.62801},
                {0.17241, 0.17172, 0.17302, 0.17172},
                {0.01608, -0.01183, 0.01614, -0.01183},
                {0.01039, 0.00862, 0.01043, 0.00862},
                {0.00739, 0.00804, 0.00734, 0.00804},
                {-0.00514, 0.00454, -0.00515, 0.00454},
                {0.00208, 0.00204, 0.00209, 0.00204},
                {-0.00111, -0.00180, -0.00111, -0.00180},
                {-0.00057, -0.00070, -0.00057, -0.00070},
                {0.00056, -0.00040, 0.00056, -0.00040},
                {-0.00042, -0.00034, -0.00042, -0.00034},
                {0.00042, 0.00032, 0.00042, 0.00032},
                {0.00038, 0.00032, 0.00038, 0.00032},
                {-0.00024, -0.00028, -0.00024, -0.00028},
                {-0.00017, 0.00027, -0.00017, 0.00027},
                {-0.00007, -0.00017, -0.00007, -0.00017},
                {0.00004, -0.00005, 0.00004, -0.00005},
                {0.00004, 0.00004, 0.00004, 0.00004},
                {0.00003, -0.00004, 0.00003, -0.00004},
                {0.00003, 0.00004, 0.00003, 0.00004},
                {-0.00003, 0.00003, -0.00003, 0.00003},
                {0.00003, 0.00003, 0.00003, 0.00003},
                {-0.00002, 0.00002, -0.00002, 0.00002},
                {-0.00002, 0.00002, -0.00002, 0.00002},
                {0.00002, -0.00002, 0.00002, -0.00002}
        };

        // Multiply each previous terms by E to a given power
        // [new moon, first quarter, full moon, last quarter]
        int[][] powCors = {
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 1, 0, 1},
                {0, 0, 0, 0},
                {1, 0, 1, 0},
                {1, 1, 1, 1},
                {2, 2, 2, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {1, 0, 1, 0},
                {0, 1, 0, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 2, 1, 2},
                {0, 1, 0, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };

        // Sum the following terms multiplied a number of times
        // given in the next table, and multiply the sinus of the
        // result by the previously obtained number.
        double[] terms = {s, m, f, o};

        // [new and full moon, first and last quarter]
        double[][][] mulCors = {
                {{0.0, 1.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0}},
                {{1.0, 0.0, 0.0, 0.0}, {1.0, 0.0, 0.0, 0.0}},
                {{0.0, 2.0, 0.0, 0.0}, {1.0, 1.0, 0.0, 0.0}},
                {{0.0, 0.0, 2.0, 0.0}, {0.0, 2.0, 0.0, 0.0}},
                {{-1.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 2.0, 0.0}},
                {{1.0, 1.0, 0.0, 0.0}, {-1.0, 1.0, 0.0, 0.0}},
                {{2.0, 0.0, 0.0, 0.0}, {2.0, 0.0, 0.0, 0.0}},
                {{0.0, 1.0, -2.0, 0.0}, {0.0, 1.0, -2.0, 0.0}},
                {{0.0, 1.0, 2.0, 0.0}, {0.0, 1.0, 2.0, 0.0}},
                {{1.0, 2.0, 0.0, 0.0}, {0.0, 3.0, 0.0, 0.0}},
                {{0.0, 3.0, 0.0, 0.0}, {-1.0, 2.0, 0.0, 0.0}},
                {{1.0, 0.0, 2.0, 0.0}, {1.0, 0.0, 2.0, 0.0}},
                {{1.0, 0.0, -2.0, 0.0}, {1.0, 0.0, -2.0, 0.0}},
                {{-1.0, 2.0, 0.0, 0.0}, {2.0, 1.0, 0.0, 0.0}},
                {{0.0, 0.0, 0.0, 1.0}, {1.0, 2.0, 0.0, 0.0}},
                {{2.0, 1.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 1.0}},
                {{0.0, 2.0, -2.0, 0.0}, {-1.0, 1.0, -2.0, 0.0}},
                {{3.0, 0.0, 0.0, 0.0}, {0.0, 2.0, 2.0, 0.0}},
                {{1.0, 1.0, -2.0, 0.0}, {1.0, 1.0, 2.0, 0.0}},
                {{0.0, 2.0, 2.0, 0.0}, {-2.0, 1.0, 0.0, 0.0}},
                {{1.0, 1.0, 2.0, 0.0}, {1.0, 1.0, -2.0, 0.0}},
                {{-1.0, 1.0, 2.0, 0.0}, {3.0, 0.0, 0.0, 0.0}},
                {{-1.0, 1.0, -2.0, 0.0}, {0.0, 2.0, -2.0, 0.0}},
                {{1.0, 3.0, 0.0, 0.0}, {-1.0, 1.0, 2.0, 0.0}},
                {{0.0, 4.0, 0.0, 0.0}, {1.0, 3.0, 0.0, 0.0}}
        };

        int j = phase;
        double cor = 0.0;
        for (int i = 0; i < 25; i++) {
            double sinCor = 0.0;
            for (int si = 0; si < 4; si++) {
                sinCor += mulCors[i][j % 2][si] * terms[si];
            }

            cor += numCors[i][j] * Math.pow(e, powCors[i][j]) * sinDeg(sinCor);
        }

        // Additional corrections for quarters
        double w = 0.00306
                - 0.00038 * e * cosDeg(s)
                + 0.00026 * cosDeg(m)
                - 0.00002 * cosDeg(m - s)
                + 0.00002 * cosDeg(m + s)
                + 0.00002 * cosDeg(2.0 * f);

        switch (phase) {
            case 1:
                cor += w;
                break;
            case 3:
                cor -= w;
                break;
        }

        // Additional corrections for all phases
        double add = 0.0
                + 0.000325 * sinDeg(299.77 + 0.107408 * k - 0.009173 * Math.pow(t, 2))
                + 0.000165 * sinDeg(251.88 + 0.016321 * k)
                + 0.000164 * sinDeg(251.83 + 26.651886 * k)
                + 0.000126 * sinDeg(349.42 + 36.412478 * k)
                + 0.000110 * sinDeg(84.66 + 18.206239 * k)
                + 0.000062 * sinDeg(141.74 + 53.303771 * k)
                + 0.000060 * sinDeg(207.14 + 2.453732 * k)
                + 0.000056 * sinDeg(154.84 + 7.306860 * k)
                + 0.000047 * sinDeg(34.52 + 27.261239 * k)
                + 0.000042 * sinDeg(207.19 + 0.121824 * k)
                + 0.000040 * sinDeg(291.34 + 1.844379 * k)
                + 0.000037 * sinDeg(161.72 + 24.198154 * k)
                + 0.000035 * sinDeg(239.56 + 25.513099 * k)
                + 0.000023 * sinDeg(331.55 + 3.592518 * k);

        jde += cor + add;

        long tt = julianToUnix(jde);
        long dt = (long) Math.floor(deltaTime(unixToYear(tt)));

        return tt - dt;
    }

    private static double unixToYear(long timestamp) {
        return 1970.0 + ((double) timestamp) / 86400.0 / 365.25;
    }

    // From "Polynomial Expressions for Delta T"
    // By Fred Espenak, GSFC Planetary Systems Laboratory
    private static double deltaTime(double year) {
        double y;
        if (1961.0 <= year && year < 1986.0) {
            y = 1975.0;
        } else if (1986.0 <= year && year < 2005.0) {
            y = 2000.0;
        } else if (2005.0 <= year && year < 2050.0) {
            y = 2000.0;
        } else {
            y = 0.0; // FIXME
        }

        double t = year - y;

        if (1961.0 <= year && year < 1986.0) {
            return 45.45 + 1.067 * t
                    - Math.pow(t, 2) / 260.0
                    - Math.pow(t, 3) / 718.0;
        } else if (1986.0 <= year && year < 2005.0) {
            return 63.86
                    + 0.3345 * t
                    - 0.060374 * Math.pow(t, 2)
                    + 0.0017275 * Math.pow(t, 3)
                    + 0.000651814 * Math.pow(t, 4)
                    + 0.00002373599 * Math.pow(t, 5);

        } else if (2005.0 <= year && year < 2050.0) {
            return 62.92 + 0.32217 * t + 0.005589 * Math.pow(t, 2);

        } else {
            return 0.0;
        }
    }

    private static long getNewMoon(double lunationNumber) {
        return getMoonPhase(0, lunationNumber);
    }

    private static long getDecemberSolstice(long timestamp) {
        return getSunEphemeris(3, timestamp);
    }
}
