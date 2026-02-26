package mia.codeutils.render.util.elements;

import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.AxisBinding;
import mia.codeutils.render.util.DrawBinding;
import mia.codeutils.render.util.Point;
import net.minecraft.network.chat.Component;

public class DrawLabeledButton extends DrawButton {

    public DrawLabeledButton(Point position, Point size, Component text, int z, boolean shadow, ARGB disabledColor, ARGB enabledColor) {
        this(position, size, text, z, shadow, disabledColor, enabledColor, null);
    }

    public DrawLabeledButton(Point position, Point size, Component text, int z, boolean shadow, ARGB disabledColor, ARGB enabledColor, DrawObject parent) {
        super(position, size, z ,disabledColor, enabledColor, parent);

        DrawText label = new DrawText(new Point(0, 0), text, 0, shadow, this);
        label.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        label.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        addDrawable(label);
    }

}
