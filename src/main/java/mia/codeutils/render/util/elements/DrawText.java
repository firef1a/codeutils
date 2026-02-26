package mia.codeutils.render.util.elements;

import mia.codeutils.Mod;
import mia.codeutils.render.util.DrawContextHelper;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class DrawText extends DrawObject {
    public Component text;
    private boolean shadow;

    public DrawText(Point position, Component text, int z, boolean shadow) {
        this(position, text, z, shadow, null);
    }

    public DrawText(Point position, Component text, int z, boolean shadow, DrawObject parent) {
        this.position = position;
        this.text = text;
        this.z = z;
        this.shadow = shadow;
        if (parent != null) parent.addDrawable(this);
        this.drawables = new ArrayList<>();
    }

    public void setText(Component text) { this.text = text; }

    @Override
    public Point getSize() { return new Point(Mod.MC.font.width(text), Mod.MC.font.lineHeight); }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        DrawContextHelper.drawText(context, text, x1(), y1(), shadow);
    }
}
