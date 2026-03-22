package mia.modmod.render.screens;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.core.MathUtils;
import mia.modmod.features.impl.development.scanner.PlotScanner;
import mia.modmod.render.util.ARGB;
import mia.modmod.render.util.AxisBinding;
import mia.modmod.render.util.DrawBinding;
import mia.modmod.render.util.Point;
import mia.modmod.render.util.elements.DrawButton;
import mia.modmod.render.util.elements.DrawRect;
import mia.modmod.render.util.elements.DrawText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class PlotScanScreen extends Screen {
    private final Screen parent;
    private final PlotScanner plotScanner;
    private Animation animation;

    private ArrayList<DrawButton> buttons;

    public PlotScanScreen(Screen parent, PlotScanner plotScanner) {
        super(Component.literal("Plot Scanner Screen"));
        this.parent = parent;
        this.plotScanner = plotScanner;
        this.animation = new Animation(AnimationStage.CLOSED, 0f, (state) -> (float) MathUtils.easeInOutSine(state));
    }

    @Override
    protected void init() {
        this.animation.setAnimationStage(AnimationStage.OPENING);
    }

    private void draw(GuiGraphics context, int mouseX, int mouseY) {
        // top
        float animationScale = animation.getProgress();
        buttons = new ArrayList<>();

        Point screenSize = new Point(Mod.getScaledWindowWidth(), Mod.getScaledWindowHeight());
        int mainContainerWidth = 300;
        int mainContainerHeight = 200;

        DrawRect mainContainer = new DrawRect(screenSize.mul(0.5, 0.5).add((int)(50*(1-animationScale)),0), new Point(mainContainerWidth, mainContainerHeight), 0, new ARGB(ColorBank.BLACK, 0.6f * animationScale));
        mainContainer.setSelfBinding(new DrawBinding(AxisBinding.MIDDLE, AxisBinding.MIDDLE));

        DrawRect titleBar = new DrawRect(new Point(0,0), new Point(mainContainer.getWidth(), 10), 0, new ARGB(ColorBank.BLACK, 0.6f * animationScale), mainContainer);

        DrawText title = new DrawText(
                new Point(5, titleBar.getHeight() / 2),
                Component.literal("Plot Scan ").withColor(ColorBank.MC_GRAY).withStyle(ChatFormatting.ITALIC).append(Component.literal("ID-" + (plotScanner.plot == null ? "?" : plotScanner.plot.plotId)).withColor(ColorBank.WHITE_GRAY).withStyle(ChatFormatting.ITALIC)),
                0,
                animationScale,
                true,
                titleBar
        );
        title.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
        title.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.MIDDLE));

        switch (plotScanner.plotScanningMode) {
            case REQUESTING_LOCATE_INFO: {

                break;
            }
            case PTP_WAITING: {

                break;
            }
            case REQUESTING_CODE_LINES: {

                break;
            }
            case GRABBING_CODE: {

                break;
            }
            default: {

                break;
            }
        }

        mainContainer.render(context, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (parent != null) parent.render(context, Integer.MAX_VALUE, Integer.MAX_VALUE, delta);
        //this.applyBlur(context);

        draw(context, mouseX, mouseY);

        animation.updateAnimation(0.1f);

        super.render(context, mouseX, mouseY, delta);
    }
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) { }


    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        for (DrawButton button : buttons) {
            button.mouseClick(click, doubled);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return super.charTyped(input);
    }


    @Override
    public boolean keyPressed(KeyEvent input) {
        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        if (parent == null) Mod.MC.setScreen((Screen) null);
    }
}
