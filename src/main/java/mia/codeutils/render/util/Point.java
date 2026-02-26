package mia.codeutils.render.util;

public class Point {
    public static final Point ZERO = new Point(0,0);
    private int x, y;
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }


    public void set(int x, int y) {
        setX(x);
        setY(y);
    }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public Point add(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }
    public Point add(Point point) {
        return new Point(point.x + x, point.y + y);
    }


    public Point mul(double x, double y) {
        return new Point((int) (this.x * x), (int) (this.y * y));
    }
    public Point mul(Point point) {
        return new Point(point.x() * x, point.y() * y);
    }

}
