package mia.modmod.features.impl.moderation.reports;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.core.KeyBindCategories;
import mia.modmod.core.KeyBindManager;
import mia.modmod.core.MiaKeyBind;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.permissions.ModeratorPermission;
import mia.modmod.features.impl.internal.permissions.Permissions;
import mia.modmod.features.impl.internal.permissions.SupportPermission;
import mia.modmod.features.impl.internal.server.ServerManager;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.listeners.impl.RegisterKeyBindEvent;
import mia.modmod.features.listeners.impl.RenderHUD;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.BooleanDataField;
import mia.modmod.features.parameters.impl.IntegerDataField;
import mia.modmod.render.screens.AnimationStage;
import mia.modmod.render.screens.reportscreen.ReportScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReportTracker extends Feature implements RegisterKeyBindEvent, TickEvent, RenderHUD, ChatEventListener {
    public MiaKeyBind openQA;
    private ReportScreen reportScreen;
    public ArrayList<DatedReport> reports;

    private static final String[] VALID_REPORT_HANDLED_MESSAGES = { "report handled", "handling report", "on it", "handled report" };

    private final IntegerDataField reportHandledTimespan;


    public ReportTracker(Categories category) {
        super(category, "Report Tracker", "report_tracker", "Shows recent reports", new Permissions(SupportPermission.NONE, ModeratorPermission.JR_MOD));
        openQA = new MiaKeyBind("Open Report Screen", GLFW.GLFW_KEY_O, KeyBindCategories.GENERAL_CATEGORY);
        reports = new ArrayList<>();

        reportHandledTimespan = new IntegerDataField("Report Handled Timespan (seconds)", ParameterIdentifier.of(this, "handled_timespan"), 30, true);
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String content = message.base().getString();
        Matcher reportMatcher = ReportTeleport.REPORT_PATTERN.matcher(content);
        if (reportMatcher.find()) {
            String reporter = reportMatcher.group(1);
            String offender = reportMatcher.group(2);
            String offense = reportMatcher.group(3);
            String private_text = reportMatcher.group(4);
            String node_text = reportMatcher.group(5);
            String node_number = reportMatcher.group(6);
            String mode = reportMatcher.group(7);
            long timestamp = System.currentTimeMillis();

            //Mod.message(Component.literal("REPORT INC: !"));
            //Mod.message("report: " + String.join(" ", List.of(reporter, offender, offense, private_text, node_text, node_number, mode)));

            reports.addFirst(new DatedReport(reporter, offender, offense, private_text, node_text, node_number, mode, timestamp));
        }

        Matcher mbMatcher = Pattern.compile("^\\[MOD] ([a-zA-Z0-9_]{3,16}): (" + String.join("|", VALID_REPORT_HANDLED_MESSAGES) + ")").matcher(content);
        if (mbMatcher.find()) {
            Mod.message(mbMatcher.group(1));
            if (!mbMatcher.group(1).equals(Mod.getPlayerName())) {
                for (DatedReport report : reports) {
                    if (System.currentTimeMillis() - report.timestamp() < (1000L * reportHandledTimespan.getValue())) {
                        Mod.message(report.reporter());
                        if (!report.handled()) {
                            report.setHandled(true);
                            Mod.message(
                                    Component.literal("Report by ")
                                            .append(Component.literal(report.reporter()).withColor(ColorBank.WHITE_GRAY))
                                            .append(" of ")
                                            .append(Component.literal(report.offender()).withColor(ColorBank.WHITE_GRAY))
                                            .append(Component.literal(" set to "))
                                            .append(Component.literal("Handled").withColor(ColorBank.MC_GREEN))
                            );
                        }
                    }
                }
            }
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
        if (ServerManager.isNotOnDiamondFire()) return;
        if (openQA.isDown()) {
            if ((Mod.getCurrentScreen() == null)) Mod.setCurrentScreen(reportScreen = new ReportScreen(null));
        }
    }

    @Override
    public void tickF(int tick) {

    }


}
