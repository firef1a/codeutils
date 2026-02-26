package mia.codeutils.render.util;

public class DrawBinding {
    private AxisBinding xBinding, yBinding;
    public DrawBinding(AxisBinding xBinding, AxisBinding yBinding) {
        this.xBinding = xBinding;
        this.yBinding = yBinding;
    }

    public AxisBinding getXBinding() { return xBinding; }
    public AxisBinding getYBinding() { return yBinding; }

    public void setXBinding(AxisBinding binding) { xBinding = binding; }
    public void setYBinding(AxisBinding binding) { yBinding = binding; }

    public Point pointMultiply(Point point) {
        if (point == null) {
            //Mod.error( this.toString() + " point is null!");
            return new Point(0,0);
        }
        return new Point((int) ((int)point.x()*xBinding.getScale()), (int) ((int)point.y()*yBinding.getScale()));
    }
}
