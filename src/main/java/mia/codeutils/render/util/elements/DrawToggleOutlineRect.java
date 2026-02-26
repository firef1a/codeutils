package mia.codeutils.render.util.elements;

import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.DrawContextHelper;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;

public class DrawToggleOutlineRect extends DrawOutlineRect {
    private boolean enabled;

    public DrawToggleOutlineRect(Point position, Point size, int z, ARGB color, DrawObject parent) {
        super(position, size, z, color, parent);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        if (enabled) DrawContextHelper.drawRectBorder(context, x1(), y1(), getWidth(), getHeight(), color);
    }

}
