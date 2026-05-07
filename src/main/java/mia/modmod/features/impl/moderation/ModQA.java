package mia.modmod.features.impl.moderation;

import mia.modmod.Mod;
import mia.modmod.core.KeyBindCategories;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.server.ServerManager;
import mia.modmod.features.listeners.impl.RegisterKeyBindEvent;
import mia.modmod.features.listeners.impl.RenderHUD;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.BooleanDataField;
import mia.modmod.render.screens.AnimationStage;
import mia.modmod.render.screens.modqa.ModQAScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public final class ModQA extends Feature implements RegisterKeyBindEvent, TickEvent, RenderHUD {
    public KeyMapping openQA;
    private ModQAScreen modQAScreen;
    public final BooleanDataField safetyMode;

    public ModQA(Categories category) {
        super(category, "Mod Quick Access Screen", "modqa", "Screen for selecting common ban / mute reasons");
        openQA = new KeyMapping("Open Mod Screen", GLFW.GLFW_KEY_X, KeyBindCategories.STAFF.getCategory());
        safetyMode = new BooleanDataField("Safety Mode", "prevents u from banning yourself (i already made it pretty hard this is for testing purposes only)", ParameterIdentifier.of(this, "safe_mode"), true, true);
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        if (modQAScreen != null) {
            if (modQAScreen.animation.getAnimationStage().equals(AnimationStage.CLOSING)) modQAScreen.draw(context, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
    }

    @Override
    public void registerKeyBind() {
        KeyBindingHelper.registerKeyBinding(openQA);
    }

    @Override
    public void tickR(int tick) {
        if (ServerManager.isNotOnDiamondFire()) return;
        if (openQA.isDown()) {
            if ((Mod.getCurrentScreen() == null)) Mod.setCurrentScreen(modQAScreen = new ModQAScreen(null));
        }
    }

    @Override
    public void tickF(int tick) {

    }

}
