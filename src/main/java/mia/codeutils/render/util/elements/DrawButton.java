package mia.codeutils.render.util.elements;

import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.DrawContextHelper;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;

public class DrawButton extends DrawObject {
    private ARGB disabledColor, enabledColor;
    private boolean enabled;
    private Runnable callback = () -> {};

    public DrawButton(Point position, Point size, int z, ARGB disabledColor, ARGB enabledColor) {
        this(position, size, z, disabledColor, enabledColor, null);
    }

    public DrawButton(Point position, Point size, int z, ARGB disabledColor, ARGB enabledColor, DrawObject parent) {
        this.position = position;
        this.size = size;
        this.z = z;
        this.disabledColor = disabledColor;
        this.enabledColor = enabledColor;
        if (parent != null) parent.addDrawable(this);
        this.drawables = new ArrayList<>();
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public void leftMouseClick(MouseButtonEvent click, boolean doubled) {
        callback.run();
    };

    @Override
    public boolean mouseClick(MouseButtonEvent click, boolean doubled) {
        if (containsPoint(click.x(), click.y())) {
            if (click.button() == 0) {
                leftMouseClick(click, doubled);
            }
        }
        super.mouseClick(click, doubled);
        return false;
    };

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        DrawContextHelper.drawRect(context, x1(), y1(), getWidth(), getHeight(), enabled || containsPoint(mouseX, mouseY) ? enabledColor : disabledColor);
    }
}
