package mia.modmod.features.impl.moderation.reports;

import mia.modmod.Mod;
import mia.modmod.core.KeyBindCategories;
import mia.modmod.core.KeyBindManager;
import mia.modmod.core.MiaKeyBind;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.permissions.ModeratorPermission;
import mia.modmod.features.impl.internal.permissions.Permissions;
import mia.modmod.features.impl.internal.permissions.SupportPermission;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.listeners.impl.RegisterKeyBindEvent;
import mia.modmod.features.listeners.impl.RenderHUD;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.render.screens.AnimationStage;
import mia.modmod.render.screens.reportscreen.ReportScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;

public final class ReportScreenFeature extends Feature implements RegisterKeyBindEvent, TickEvent, RenderHUD, ChatEventListener {
    public MiaKeyBind openQA;
    private ReportScreen reportScreen;
    public ArrayList<DatedReport> reports;


    public ReportScreenFeature(Categories category) {
        super(category, "Report Screen", "report_screen", "Shows recent reports", new Permissions(SupportPermission.NONE, ModeratorPermission.JR_MOD));
        openQA = new MiaKeyBind("Open Report Screen", GLFW.GLFW_KEY_O, KeyBindCategories.GENERAL_CATEGORY);
        reports = new ArrayList<>();
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        Matcher reportMatcher = ReportTeleport.REPORT_PATTERN.matcher(message.base().getString());
        if (reportMatcher.find()) {
            String reporter = reportMatcher.group(1);
            String offender = reportMatcher.group(2);
            String offense = reportMatcher.group(3);
            String private_text = reportMatcher.group(4);
            String node_text = reportMatcher.group(5);
            String node_number = reportMatcher.group(6);
            long timestamp = System.currentTimeMillis();

            reports.add(new DatedReport(reporter, offender, offense, private_text, node_text, node_number, timestamp));
        }
        return message.pass();
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        if (reportScreen != null) {
            if (reportScreen.animation.getAnimationStage().equals(AnimationStage.CLOSING)) reportScreen.draw(context, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
    }

    @Override
    public void registerKeyBind() {
        KeyBindManager.registerKeyBind(openQA);
    }

    @Override
    public void tickR(int tick) {
        if (openQA.isDown()) {
            if ((Mod.getCurrentScreen() == null)) Mod.setCurrentScreen(reportScreen = new ReportScreen(null));
        }
    }

    @Override
    public void tickF(int tick) {

    }


}
