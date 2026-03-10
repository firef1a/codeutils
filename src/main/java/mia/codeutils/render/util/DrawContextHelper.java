package mia.codeutils.render.util;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DrawContextHelper {
    public static void drawRect(GuiGraphics context, int x, int y, int width, int height, ARGB color) {
        context.fill(x,y,x+width,y+height,color.getARGB());
    }
    public static void drawRectBorder(GuiGraphics context, int x, int y, int width, int height, ARGB color) {
        drawRect(context, x, y, width, 1, color);
        drawRect(context, x, y, 1, height, color);
        drawRect(context, x+width-1, y, 1, height, color);
        drawRect(context, x, y+height-1, width, 1, color);
    }

    public static void drawText(GuiGraphics context, Component text, int x, int y, float alpha, boolean shadow) {
        context.drawString(Mod.MC.font, text, x, y, ARGB.getARGB(ColorBank.WHITE, alpha), shadow);
    }
    public static void drawTooltip(GuiGraphics context, Component text, int x, int y, float yAnchor) {
        drawTooltip(context, List.of(text), x, y, yAnchor);
    }


    // yAnchor: a value of 0.5 will center it around y and a value of 1 will make it render above y
    public static void drawTooltip(GuiGraphics context, List<Component> list, int x, int y, float yAnchor) {
        int baseWidth = 0;
        int baseHeight = (list.size() * Mod.MC.font.lineHeight) + ((list.size()-1) * 2);
        for (Component text : list) {
            int textWidth = Mod.MC.font.width(text);
            if (textWidth > baseWidth) baseWidth = textWidth;
        }

        int margin = 3;
        int baseX = x + 6;
        int baseY = (int) (y - (yAnchor * (baseHeight + (2 * margin))));
        int width = baseWidth + margin * 2;
        int height = baseHeight + margin * 2;

        drawRect(context, baseX+1, baseY+1, width-1, height-1, new ARGB(0x121212, 0.75));
        drawRectBorder(context, baseX, baseY, width, height, new ARGB(ColorBank.MIA_PURPLE, 1f));

        int i = 0;
        for (Component text : list) {
            drawText(context, text, baseX+margin, baseY + margin + i * (Mod.MC.font.lineHeight + 2), 1f, true);
            i++;
        }

    }

    public static void addGUIElement(GuiGraphics context, GuiElementRenderState elementRenderState) {
        context.guiRenderState.submitGuiElement(elementRenderState);
    }
}

