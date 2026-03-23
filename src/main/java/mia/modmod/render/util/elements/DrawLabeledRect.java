package mia.modmod.render.util.elements;

import mia.modmod.render.util.ARGB;
import mia.modmod.render.util.AxisBinding;
import mia.modmod.render.util.DrawBinding;
import mia.modmod.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DrawLabeledRect extends DrawRect {

    public DrawLabeledRect(Point position, Point size, Component text, boolean shadow, ARGB color) {
        this(position, size, text, color, shadow, null);
    }

    public DrawLabeledRect(Point position, Point size, Component text, ARGB color, boolean shadow, DrawObject parent) {
        super(position, size, color, parent);

        DrawText label = new DrawText(new Point(0, 0), text, 1f, shadow, this);
        label.setParentBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        label.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));
        addDrawable(label);
    }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        super.draw(context,mouseX,mouseY);
    }
}
