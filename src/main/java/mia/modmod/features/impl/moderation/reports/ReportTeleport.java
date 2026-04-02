package mia.modmod.features.impl.moderation.reports;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.internal.commands.CommandScheduler;
import mia.modmod.features.impl.internal.commands.ScheduledCommand;
import mia.modmod.features.impl.moderation.tracker.PlayerTracker;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.listeners.impl.RegisterCommandListener;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.BooleanDataField;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReportTeleport extends Feature implements ChatEventListener, RegisterCommandListener, TickEvent {
    public static final Pattern REPORT_PATTERN = Pattern.compile("^! Incoming Report \\(([A-Za-z0-9_]{3,16})\\)\\n\\|  Offender: ([A-Za-z0-9_]{3,16})\\n\\|  Offense: (.*)\\n\\|  Location: (Private |)(.*) (\\d*) ((?:Mode|Spawn|Existing).*)$");

    private final BooleanDataField runalts;
    private final BooleanDataField msgOnReportTeleport;

    public static boolean requestingHistory = false;
    private static boolean isInternalReportTeleport = false;

    private static String requestPlayerName;
    private static String requestNodeID;

    public static final String HASH_PREFIX = "handling report ID=";

    public ReportTeleport(Categories category) {
        super(category, "Report Teleport", "reportteleport", "Click on report msgs to teleport the offender.");
        runalts = new BooleanDataField("Run /alts", "Runs /alts when you click on a report", ParameterIdentifier.of(this, "runalts"), true, true);
        msgOnReportTeleport = new BooleanDataField("Send /mb Handled Message Hash", "Send an automated message when you click on a new report\n\n'" + HASH_PREFIX + "$HASH' $HASH is a hash of the report.\n\nOther mods using " + Mod.MOD_ID + " will mark the report with the same hash as handled.", ParameterIdentifier.of(this, "report_msg_hash"), true, true);

    }

    public static MutableComponent getFollowComponent(String offender, String node_formatted) {
        return Component.empty()
                .append(Component.literal("Follow ").withColor(ColorBank.MC_GRAY))
                .append(Component.literal(offender).withColor(ColorBank.WHITE_GRAY))
                .append(Component.literal(" to ").withColor(ColorBank.MC_GRAY))
                .append(Component.literal(node_formatted).withColor(ColorBank.WHITE_GRAY)).copy();
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String base = message.base().getString();
        Matcher matcher = REPORT_PATTERN.matcher(base);
        if (matcher.find()) {
            String reporter = matcher.group(1);
            String offender = matcher.group(2);
            String offense = matcher.group(3);
            String private_text = matcher.group(4);
            String node_text = matcher.group(5);
            String node_number = matcher.group(6);
            String mode = matcher.group(7);
            //Mod.error("REPORT DETECTED: " + reporter + " " + offender + " " + offender + " " + private_text + " " + node_text + " " + node_number);

            boolean is_private = private_text.isEmpty();


            String node_formatted = private_text + node_text + " " + node_number;
            String node_id = is_private ? "node" + node_number : "private" + node_number;

            int reportHash = new DatedReport(reporter, offender, offense, private_text, node_text, node_number, mode, System.currentTimeMillis()).getReportHash();

            return message.modified(message.modified().copy().withStyle(
                    style -> style.withHoverEvent(new HoverEvent.ShowText(getFollowComponent(offender, node_formatted)))
                                        .withClickEvent(new ClickEvent.RunCommand("/internal_report_teleport " + node_id + " " + offender + " " + reportHash))
            ));
        }
        return message.pass();
    }


    public static void internalReportTeleport(String player_name, String node_id) {
        PlayerTracker.removeTrackedPlayer(player_name);
        PlayerTracker.addTrackedPlayer(player_name);
        requestingHistory = true;
        isInternalReportTeleport = true;

        requestPlayerName = player_name;
        requestNodeID = node_id;
    }

    @Override
    public void tickR(int tick) {
        if (isInternalReportTeleport && !requestingHistory) {
            CommandScheduler.addCommand(new ScheduledCommand("preference mod_vanish true", 500L));
            CommandScheduler.addCommand(new ScheduledCommand("server " + requestNodeID));
            CommandScheduler.addCommand(new ScheduledCommand("tp " + requestPlayerName, 250L));
            if (FeatureManager.getFeature(ReportTeleport.class).runalts.getValue()) CommandScheduler.addCommand(new ScheduledCommand("alts " + requestPlayerName));

            isInternalReportTeleport = false;
        }
    }

    @Override
    public void tickF(int tick) {

    }

    public static void sendModChatReportHash(int hashcode) {
        if (FeatureManager.getFeature(ReportTeleport.class).msgOnReportTeleport.getValue()) {
            Mod.messageError("mb " + HASH_PREFIX + hashcode);
            CommandScheduler.addCommand(new ScheduledCommand("mb " + HASH_PREFIX + hashcode));
        }
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("internal_report_teleport")
            .then(ClientCommandManager.argument("node_id", StringArgumentType.string())
                .then(ClientCommandManager.argument("offender", StringArgumentType.string())
                    .then(ClientCommandManager.argument("hashcode", StringArgumentType.string())
                        .executes(commandContext -> {
                            String node_id = StringArgumentType.getString(commandContext, "node_id");
                            String offender = StringArgumentType.getString(commandContext, "offender");
                            String hashcode = StringArgumentType.getString(commandContext, "hashcode");

                            for (DatedReport report : FeatureManager.getFeature(ReportTracker.class).reports) {
                                if (!report.handled()) {
                                    if (report.getReportHash() == Integer.parseInt(hashcode)) {
                                        report.setHandled(true);
                                        sendModChatReportHash(report.getReportHash());
                                        break;
                                    }
                                }
                            }

                            internalReportTeleport(offender, node_id);
                            return 1;
                        })
                    )
                )
            )
        );
    }


}
