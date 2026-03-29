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
import mia.modmod.features.impl.moderation.tracker.PlayerOutliner;
import mia.modmod.features.impl.moderation.tracker.PlayerTracker;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.listeners.impl.RegisterCommandListener;
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

public final class ReportTeleport extends Feature implements ChatEventListener, RegisterCommandListener {
    public static final Pattern REPORT_PATTERN = Pattern.compile("^! Incoming Report \\(([A-Za-z0-9_]{3,16})\\)\\n\\|  Offender: ([A-Za-z0-9_]{3,16})\\n\\|  Offense: (.*)\\n\\|  Location: (Private |)(.*) (\\d*) ((?:Mode|Spawn|Existing).*)$");
    private final BooleanDataField runalts;

    public ReportTeleport(Categories category) {
        super(category, "ReportTeleport", "reportteleport", "Click on report msgs to teleport the offender.");
        runalts = new BooleanDataField("Run /alts", ParameterIdentifier.of(this, "runalts"), true, true);

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

            int hashCode = (reporter + offender + offender + private_text + node_text + node_number + mode).hashCode();

            return message.modified(message.modified().copy().withStyle(
                    style -> style.withHoverEvent(new HoverEvent.ShowText(
                    getFollowComponent(offender, node_formatted)))
                            .withClickEvent(new ClickEvent.RunCommand("/internal_report_teleport " + node_id + " " + offender + " " + hashCode))));
        }
        return message.pass();
    }


    public static void internalReportTeleport(String player_name, String node_id) {;
        PlayerTracker.addTrackedPlayer(player_name);
        CommandScheduler.addCommand(new ScheduledCommand("preference mod_vanish true", 500L));
        CommandScheduler.addCommand(new ScheduledCommand("server " + node_id));
        CommandScheduler.addCommand(new ScheduledCommand("tp " + player_name, 250L));
        if (FeatureManager.getFeature(ReportTeleport.class).runalts.getValue()) CommandScheduler.addCommand(new ScheduledCommand("alts " + player_name));
    }


    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("internal_report_teleport")
            .then(ClientCommandManager.argument("node_id", StringArgumentType.string())
                .then(ClientCommandManager.argument("player_name", StringArgumentType.string())
                        .then(ClientCommandManager.argument("hashcode", StringArgumentType.string())
                    .executes(commandContext -> {
                        String offender = StringArgumentType.getString(commandContext, "offender");
                        String node_id = StringArgumentType.getString(commandContext, "node_id");
                        String hashcode = StringArgumentType.getString(commandContext, "hashcode");
                        internalReportTeleport(offender, node_id);

                        Mod.MC.execute(() -> {
                            for (DatedReport report : FeatureManager.getFeature(ReportTracker.class).reports) {
                                int reportHashcode = (report.reporter() + report.offender() + report.offender() + report.private_text() + report.node_text() + report.node_number() + report.mode()).hashCode();
                                if (!report.handled()) {
                                    if (reportHashcode == Integer.parseInt(hashcode)) {
                                        report.setHandled(true);
                                        break;
                                    }
                                }
                            }
                        });

                        return 1;
                    })
                )
                )
            )
        );
    }
}
