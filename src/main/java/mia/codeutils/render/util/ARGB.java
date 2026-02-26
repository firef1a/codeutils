package mia.codeutils.render.util;


import java.awt.*;

public class ARGB {
    private final int rgb;
    private final double alpha;

    public ARGB(int rgb, double alpha) {
        this.alpha = alpha;
        this.rgb = rgb;
    }

    public int getARGB() {
        return getARGB(rgb, alpha);
    }

    public int getRGB() {
        return rgb;
    }
    public double getAlpha() {
        return alpha;
    }

    public static int getARGB(int rgb) { return getARGB(rgb, 1f); }
    public static int getARGB(int rgb, double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            return 0;
        }

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int alphaInt = (int) Math.round(alpha * 255);

        return (alphaInt << 24) | (red << 16) | (green << 8) | blue;
    }

    public ARGB mul(int rgb, double alpha) { return new ARGB(getRGB() * rgb, getAlpha()*alpha); }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    public static int getBlue(int color) {
        return color & 0xFF;
    }
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int lerpColor(int color1, int color2, float fraction) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;


        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);
        int a = (int) (a1 + (a2 - a1) * fraction);


        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static ARGB getRainbowARGB(long phase, int period) {
        return new ARGB(Color.HSBtoRGB((((System.currentTimeMillis() + (phase * period)) % (1000L * period)) / (1000f * period)), 0.5f , 1f), 1f);
    }
}



