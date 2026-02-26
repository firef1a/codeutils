package mia.codeutils.core;

public final class MathUtils {
    public static double roundToDecimalPlaces(double number, int decimals) {
        return ((int) (number * Math.pow(10, decimals)) / (Math.pow(10, decimals)));
    }

    public static double easeInOutSine(int x) {
        return -(Math.cos(Math.PI * x) - 1) / 2;
    }
}
