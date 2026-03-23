package mia.modmod.render.util.elements;

import mia.modmod.render.util.ARGB;
import mia.modmod.render.util.DrawContextHelper;
import mia.modmod.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;

public class DrawToggleOutlineRect extends DrawOutlineRect {
    private boolean enabled;

    public DrawToggleOutlineRect(Point position, Point size, ARGB color, DrawObject parent) {
        super(position, size, color, parent);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        if (enabled) DrawContextHelper.drawRectBorder(context, x1(), y1(), getWidth(), getHeight(), color);
    }

}
