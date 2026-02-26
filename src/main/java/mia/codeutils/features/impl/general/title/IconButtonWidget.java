package mia.codeutils.features.impl.general.title;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class IconButtonWidget extends ImageButton {
    public IconButtonWidget(int x, int y, int width, int height, WidgetSprites textures, OnPress pressAction) {
        super(x, y, width, height, textures, pressAction);
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Identifier identifier = this.sprites.get(true, this.isHoveredOrFocused());
        context.blit(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, width,height);
    }
}
