package mia.codeutils.core;

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
}
