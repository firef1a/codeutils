package mia.codeutils.render.util.elements;

import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.DrawContextHelper;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;

public class DrawRect extends DrawObject {
    protected final ARGB color;

    public DrawRect(Point position, Point size, int z, ARGB color) {
        this(position, size, z, color, null);
    }

    public DrawRect(Point position, Point size, int z, ARGB color, DrawObject parent) {
        this.position = position;
        this.size = size;
        this.z = z;
        this.color = color;
        if (parent != null) parent.addDrawable(this);
        this.drawables = new ArrayList<>();
    }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        DrawContextHelper.drawRect(context, x1(), y1(), getWidth(), getHeight(), color);
    }
}
