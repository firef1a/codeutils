package mia.modmod.render.util.elements;

import mia.modmod.render.util.ARGB;
import mia.modmod.render.util.DrawContextHelper;
import mia.modmod.render.util.Point;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class DrawRect extends DrawObject {
    protected final ARGB color;

    public DrawRect(Point position, Point size, ARGB color) { this(position, size, color, null); }
    public DrawRect(Point position, Point size, ARGB color, DrawObject parent) {
        this.position = position;
        this.size = size;
        this.color = color;
        if (parent != null) parent.addDrawable(this);
        this.drawables = new ArrayList<>();
    }

    @Override
    protected void draw(GuiGraphics context, int mouseX, int mouseY) {
        DrawContextHelper.drawRect(context, x1(), y1(), getWidth(), getHeight(), color);
    }
}
