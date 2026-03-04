package mia.codeutils.render.util.elements;

import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.AxisBinding;
import mia.codeutils.render.util.DrawBinding;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DrawLabeledRect extends DrawRect {

    public DrawLabeledRect(Point position, Point size, Component text, int z, boolean shadow, ARGB color) {
        this(position, size, text, z, color, shadow, null);
    }

    public DrawLabeledRect(Point position, Point size, Component text, int z, ARGB color, boolean shadow, DrawObject parent) {
        super(position, size, z, color, parent);

        DrawText label = new DrawText(new Point(0, 0), text, 0, 1f, shadow, this);
        label.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        label.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        addDrawable(label);
    }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        super.draw(context,mouseX,mouseY);
    }
}
