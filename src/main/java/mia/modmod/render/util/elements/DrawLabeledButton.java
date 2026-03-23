package mia.modmod.render.util.elements;

import mia.modmod.render.util.ARGB;
import mia.modmod.render.util.AxisBinding;
import mia.modmod.render.util.DrawBinding;
import mia.modmod.render.util.Point;
import net.minecraft.network.chat.Component;

public class DrawLabeledButton extends DrawButton {

    public DrawLabeledButton(Point position, Point size, Component text, boolean shadow, ARGB disabledColor, ARGB enabledColor) {
        this(position, size, text, shadow, disabledColor, enabledColor, null);
    }

    public DrawLabeledButton(Point position, Point size, Component text, boolean shadow, ARGB disabledColor, ARGB enabledColor, DrawObject parent) {
        super(position, size, disabledColor, enabledColor, parent);

        DrawText label = new DrawText(new Point(0, 0), text, 1f, shadow, this);
        label.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        label.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        addDrawable(label);
    }

}
