package mia.codeutils.render.util.elements;

import mia.codeutils.render.util.AxisBinding;
import mia.codeutils.render.util.DrawBinding;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;

public abstract class DrawObject {
    protected Point position, size;
    protected int z;

    protected DrawObject parent;
    protected ArrayList<DrawObject> drawables;

    protected DrawBinding parentBinding;
    protected DrawBinding selfBinding;

    public DrawBinding getParentBinding() { return parentBinding == null ? new DrawBinding(AxisBinding.NONE, AxisBinding.NONE) : parentBinding; }
    public DrawBinding getSelfBinding() { return selfBinding == null ? new DrawBinding(AxisBinding.NONE, AxisBinding.NONE) : selfBinding; }
    public void setParentBinding(DrawBinding binding) { this.parentBinding = binding; }
    public void setSelfBinding(DrawBinding binding) { this.selfBinding = binding; }

    public void setParent(DrawObject parent) { this.parent = parent; };
    public void addDrawable(DrawObject child) { drawables.add(child); child.setParent(this); };
    public void clearDrawables() { drawables.clear(); };

    public Point getRawPosition() { return position; }

    public Point getPosition() {
        return ((parent != null) ?
                getRawPosition().add(parent.getPosition().add(parentBinding == null ? new Point(0, 0) : parentBinding.pointMultiply(parent.getSize())))
                : getRawPosition()).add(selfBinding == null ? new Point(0, 0) : selfBinding.pointMultiply(this.getSize().mul(-1, -1)));
    }

    public int x1() { return getPosition().x(); }
    public int y1() { return getPosition().y(); }
    public int x2() { return x1()+getWidth(); }
    public int y2() { return y1()+getHeight(); }

    public Point topLeft() { return getPosition(); }
    public Point topRight() { return topLeft().add(getWidth(), 0); }
    public Point bottomLeft() { return topLeft().add(0, getHeight()); }
    public Point bottomRight() { return topLeft().add(getWidth(), getHeight()); }

    public Point getSize() { return size; }
    public int getHeight() { return getSize().y(); }
    public int getWidth() { return getSize().x(); }
    public int getZ() { return this.z; }

    public boolean mouseClick(MouseButtonEvent click, boolean doubled) {
        //Mod.message(this.toString() + " " + Mod.tick);
        for (DrawObject object : drawables) {
            //object.mouseClick(click, doubled);
        }
        return false;
    };

    public boolean containsPoint(double mouseX, double mouseY) {
        int x = (int) mouseX;
        int y = (int) mouseY;
        return mouseX >= x1() && mouseX <= x2() && mouseY >= y1() && mouseY <= y2();
    }

    protected abstract void draw(GuiGraphics context, int mouseX, int mouseY);

    public void render(GuiGraphics context, int mouseX, int mouseY) {
        draw(context, mouseX, mouseY);
        this.drawables.forEach(object -> object.render( context, mouseX, mouseY));
    };
}
