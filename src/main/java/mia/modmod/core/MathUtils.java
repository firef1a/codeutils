package mia.modmod.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class MathUtils {
    public static double roundToDecimalPlaces(double number, int decimals) {
        return ((int) (number * Math.pow(10, decimals)) / (Math.pow(10, decimals)));
    }

    public static double easeInOutSine(double x) {
        return - (Math.cos(Math.PI * x) - 1) / 2;
    }

    public static String convertTimestampToHMS(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String convertTimestampRelativeHMS(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH'h'mm'm'ss's'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String ts = sdf.format(date);
        while (true) {
            if (ts.startsWith("0") && !ts.substring(1).equals("s")) {
                ts = ts.substring(1);
                continue;
            }
            if (ts.startsWith("h")) {
                ts = ts.substring(1);
                continue;
            }
            if (ts.startsWith("m")) {
                ts = ts.substring(1);
                continue;
            }
            break;
        }
        return ts;
    }
}
