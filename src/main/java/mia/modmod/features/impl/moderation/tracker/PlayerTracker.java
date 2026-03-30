package mia.modmod.features.impl.moderation.tracker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.core.StreamUtils;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.internal.commands.CommandScheduler;
import mia.modmod.features.impl.internal.commands.ScheduledCommand;
import mia.modmod.features.impl.moderation.reports.ReportTeleport;
import mia.modmod.features.impl.moderation.tracker.punishments.ChronoTimestamp;
import mia.modmod.features.impl.moderation.tracker.punishments.PunishmentData;
import mia.modmod.features.impl.moderation.tracker.punishments.PunishmentTrack;
import mia.modmod.features.impl.moderation.tracker.punishments.PunishmentTracks;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.listeners.impl.RegisterCommandListener;
import mia.modmod.features.listeners.impl.TickEvent;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerTracker extends Feature implements RegisterCommandListener, TickEvent, ChatEventListener {
    private final ArrayList<String> trackedPlayers;
    private final HashMap<String, PunishmentTracks> trackedPlayerPunishmentTracks;
    private final HashMap<String, String> playerJoinDateStringMap;
    private final ArrayList<String> getPlayerHistoryQueue;
    private String currentGetPlayerHistory = null;
    private int totalPunishments;
    private int capturedPunishments;
    private HistoryState historyState;
    private ChronoTimestamp punishmentChronoTimestamp;
    private long capturePunishmentStartTimestamp;
    private PunishmentData latestPunishment;

    private static final Pattern PUNISH_BODY_PATTERN = Pattern.compile("^ (.{3,16}) was (warned|banned|muted|kicked) by (.{3,16}): '(?:((?s).*)'(?: \\[(Active|Expired)]|)|)");


    private enum HistoryState {
        NONE,
        HEAD,
        DATE,
        PUNISHMENT
    }

    public PlayerTracker(Categories category) {
        super(category, "Player Tracker", "player_tracker", "Tracks players and automatically executes appropriate moderation actions. (this is prob a bad idea)");
        trackedPlayers = new ArrayList<>();
        trackedPlayerPunishmentTracks = new HashMap<>();
        getPlayerHistoryQueue = new ArrayList<>();
        playerJoinDateStringMap = new HashMap<>();
        historyState = HistoryState.NONE;
    }

    public static ArrayList<String> getTrackerPlayers() { return FeatureManager.getFeature(PlayerTracker.class).trackedPlayers; }
    public static String getPlayerJoinDateString(String name) { return FeatureManager.getFeature(PlayerTracker.class).playerJoinDateStringMap.getOrDefault(name, "N/A"); }

    public static Optional<PunishmentTracks> getTrackedPlayerPunishmentTracks(String name) {
        if (FeatureManager.getFeature(PlayerTracker.class).trackedPlayerPunishmentTracks.containsKey(name)) return Optional.of(FeatureManager.getFeature(PlayerTracker.class).trackedPlayerPunishmentTracks.get(name));
        return Optional.empty();
    }

    public static void addTrackedPlayer(String name) {
        if (!PlayerTracker.getTrackerPlayers().contains(name)) FeatureManager.getFeature(PlayerTracker.class).internalAddTrackedPlayer(name);
    }

    private void internalAddTrackedPlayer(String name) {
        trackedPlayers.addFirst(name);
        if (!getPlayerHistoryQueue.contains(name)) getPlayerHistoryQueue.add(name);
    }

    private void initGetPlayerHistory(String name) {
        historyState = HistoryState.HEAD;
        capturePunishmentStartTimestamp = System.currentTimeMillis();
        trackedPlayerPunishmentTracks.put(name, new PunishmentTracks());
        if (!playerJoinDateStringMap.containsKey(currentGetPlayerHistory)) CommandScheduler.addCommand(new ScheduledCommand("whois " + name));
        CommandScheduler.addCommand(new ScheduledCommand("history " + name + " 9999"));
    }

    public ArrayList<Component> getTrackedHistoryText(String player) {
        ArrayList<Component> text = new ArrayList<>();
        text.add(Component.literal("Punishment History for ").append(Component.literal("'" + player + "'").withColor(ColorBank.WHITE_GRAY)));
        ArrayList<PunishmentData> activePunishments = new ArrayList<>();

        int numPunishments = 0;
        for (Map.Entry<PunishmentTrack, ArrayList<PunishmentData>> entry : trackedPlayerPunishmentTracks.get(player).getTrackedPunishments().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                numPunishments += entry.getValue().size();

                long earliestTimestamp = 0;
                for (PunishmentData punishmentData : entry.getValue()) {
                    if (punishmentData.isActive()) activePunishments.add(punishmentData);
                    if (punishmentData.chronoTimestamp().getTimestamp() > earliestTimestamp) earliestTimestamp = punishmentData.chronoTimestamp().getTimestamp();
                }

                Component trackEntry = Component.literal( entry.getKey().getReasonText() + " ").withColor(ColorBank.WHITE_GRAY)
                        .append(Component.literal("[" + entry.getValue().size() + "]").withColor(ColorBank.MC_RED));


                if (PunishmentTrack.expiringPunishments.contains(entry.getKey())) {
                    int numInvalidPunishments = 0;
                    if (PunishmentTrack.expiringPunishments.contains(entry.getKey())) {
                        for (PunishmentData punishmentData : entry.getValue()) {
                            if (punishmentData.chronoTimestamp().getTimestamp() < ChronoTimestamp.PAST_from_DHMS(14, 0, 0, 0).getTimestamp()) {
                                numInvalidPunishments++;
                            }
                        }
                    }
                    if (numInvalidPunishments > 0) trackEntry = trackEntry.copy().append(Component.literal(" (" + numInvalidPunishments + " uncounted)").withColor(ColorBank.MC_GRAY));
                }

                text.add(trackEntry);
            }
        }

        if (numPunishments == 0) {
            if (trackedPlayerPunishmentTracks.get(player).getAllPunishments().isEmpty()) {
                text.add(Component.literal("No punishments found!").withColor(ColorBank.MC_RED));
            }
        }

        int delta = trackedPlayerPunishmentTracks.get(player).getAllPunishments().size() - numPunishments;
        if (delta > 0) {
            text.add(Component.literal(delta + " unidentifiable or informal punishments found!").withColor(ColorBank.MC_RED));
        }
        if (!activePunishments.isEmpty()) {
            text.add(Component.empty());
            text.add(
                    Component.literal("Active Punishments for ").append(Component.literal("'" + player + "'").withColor(ColorBank.WHITE_GRAY))
            );

            for (PunishmentData punishmentData : activePunishments) {
                text.add(
                        Component.literal(punishmentData.reason()).withColor(ColorBank.MC_RED)
                                .append(Component.literal(" [Expires: " + ((punishmentData.getExpirationString().isPresent() ?  punishmentData.getExpirationString().get() : "N/A")) + "]").withColor(0xc2301d))
                );
            }
        }

        return text;
    }

    public ArrayList<Component> getTrackedHistoryLastestPunishmentText(String player) {
        ArrayList<Component> text = new ArrayList<>();

        for (Map.Entry<PunishmentTrack, ArrayList<PunishmentData>> entry : trackedPlayerPunishmentTracks.get(player).getTrackedPunishments().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                long earliestTimestamp = 0;

                for (PunishmentData punishmentData : entry.getValue()) {
                    if (punishmentData.chronoTimestamp().getTimestamp() > earliestTimestamp) earliestTimestamp = punishmentData.chronoTimestamp().getTimestamp();
                }
                text.add(Component.literal(" Latest: " + ChronoTimestamp.ABSOLUTE_from_Timestamp(earliestTimestamp).PAST_DHMS_string(true) + " ago").withColor(ColorBank.MC_GRAY));
            }
        }
        return text;
    }

    private void endGetPlayerHistory() {
        ReportTeleport.requestingHistory = false;
        historyState = HistoryState.NONE;

        if (currentGetPlayerHistory != null) {
            ArrayList<Component> components = getTrackedHistoryText(currentGetPlayerHistory);
            components.addFirst(Component.empty());
            for (Component component : components) Mod.message(component);
        }


        totalPunishments = 0;
        capturedPunishments = 0;
        currentGetPlayerHistory = null;
    }


    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String content = message.base().getString();
        Matcher matcher;


        matcher = PUNISH_BODY_PATTERN.matcher(content);
        if (matcher.find()) {
            String offender = matcher.group(1);
            String punishmentType = matcher.group(2);
            String issuer = matcher.group(3);
            String reason = (matcher.groupCount() >= 4 && matcher.group(4) != null) ? matcher.group(4) : "[MALFORMED_REASON]";
            String activeExpired = (matcher.groupCount() >= 5 && matcher.group(5) != null) ? matcher.group(5) : "Expired";
            PunishmentTrack punishmentTrack = null;

            if (!offender.equals(issuer)) {
                for (PunishmentTrack track : PunishmentTrack.values()) {
                    for (String pattern : track.getPatterns()) {
                        if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(reason).find()) {
                            punishmentTrack = track;
                            break;
                        }
                    }
                    if (punishmentTrack != null) break;
                }
            }

            if (currentGetPlayerHistory == null) {
                if (punishmentTrack == null) return message.pass();
                return message.modified(
                        message.modified().copy().append(
                                Component.literal("\n Punishment ID: ").withColor(ColorBank.WHITE_GRAY).append(Component.literal(punishmentTrack.getReasonText()).withColor(ColorBank.MC_GRAY))
                        )
                );
            }
            if (historyState.equals(HistoryState.PUNISHMENT)) {
                PunishmentTracks punishmentTracks = trackedPlayerPunishmentTracks.get(currentGetPlayerHistory);
                PunishmentData punishmentData = new PunishmentData(offender, punishmentType, issuer, reason, activeExpired, punishmentChronoTimestamp);
                latestPunishment = punishmentData;

                boolean addUntracked = true;

                if (punishmentTrack != null) {
                    if ((punishmentTrack.equals(PunishmentTrack.SPAMMING) && issuer.equals("Console")) || punishmentType.equals("kicked")) {
                        addUntracked = false;
                    } else {
                        punishmentTracks.addPunishment(punishmentTrack, punishmentData);
                    }
                }

                if (addUntracked) punishmentTracks.addUntrackedPunishment(punishmentData);


                capturedPunishments++;
                capturePunishmentStartTimestamp = System.currentTimeMillis();
                if (capturedPunishments == totalPunishments) {
                    endGetPlayerHistory();
                } else {
                    historyState = HistoryState.DATE;
                }
                ci.cancel();
            }
        }


        if (currentGetPlayerHistory == null) {
            if (System.currentTimeMillis() - capturePunishmentStartTimestamp < 250L) {
                matcher = Pattern.compile("^Expires in (.+)\\.").matcher(content);
                if (matcher.find()) {
                    if (latestPunishment != null) latestPunishment.setExpirationString(matcher.group(1));
                }
                ci.cancel();
            }
            return message.pass();
        }
        else ci.cancel();

        matcher = Pattern.compile("^Expires in (.+)\\.").matcher(content);
        if (matcher.find()) {
            if (latestPunishment != null) latestPunishment.setExpirationString(matcher.group(1));
        }

        matcher = Pattern.compile("→ Joined: (.*)\\n").matcher(content);
        if (matcher.find() && !playerJoinDateStringMap.containsKey(currentGetPlayerHistory)) {
            playerJoinDateStringMap.put(currentGetPlayerHistory, matcher.group(1));
        }

        if (historyState.equals(HistoryState.HEAD)) {
            matcher = Pattern.compile("^History for " + currentGetPlayerHistory +" \\(Limit: (\\d*)\\):").matcher(content);
            if (matcher.find()) {
                totalPunishments = Integer.parseInt(matcher.group(1));
                capturedPunishments = 0;
                historyState = HistoryState.DATE;
                capturePunishmentStartTimestamp = System.currentTimeMillis();
                if (totalPunishments == 9999) {
                    endGetPlayerHistory();
                }
            }

            if (Pattern.matches("^No history found\\.", content)) {
                endGetPlayerHistory();
            }
        }

        if (historyState.equals(HistoryState.DATE)) {
            matcher = Pattern.compile("^ -- \\[(.*) ago] --$").matcher(content);
            if (matcher.find()) {
                String DHMS_DATA = matcher.group(1);

                ArrayList<Integer> DMHS = new ArrayList<>(List.of(0,0,0,0));
                String[] DMHS_PATTERNS = {
                        "(\\d*) day(?:s|)",
                        "(\\d*) hour(?:s|)",
                        "(\\d*) minute(?:s|)",
                        "(\\d*) second(?:s|)"
                };

                Matcher DMHS_MATCHER;
                int i = 0;
                for (String pattern : DMHS_PATTERNS) {
                    DMHS_MATCHER = Pattern.compile(pattern).matcher(DHMS_DATA);
                    if (DMHS_MATCHER.find()) {
                        DMHS.set(i, Integer.parseInt(DMHS_MATCHER.group(1)));
                    }
                    i++;
                }

                punishmentChronoTimestamp = ChronoTimestamp.PAST_from_DHMS(
                        DMHS.get(0),
                        DMHS.get(1),
                        DMHS.get(2),
                        DMHS.get(3)
                );

                capturePunishmentStartTimestamp = System.currentTimeMillis();
                historyState = HistoryState.PUNISHMENT;
            }
        }
        // ignore unmutes/unbans cus they dont matter + expiration is shown in punish message :3
        return message.pass();
    }



    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("clear_history_tracker")
                .executes(commandContext -> {
                    trackedPlayerPunishmentTracks.clear();
                    getPlayerHistoryQueue.clear();
                    currentGetPlayerHistory = null;
                    Mod.messageError("CLEARED");
                    endGetPlayerHistory();
                    return 1;
                }));

        dispatcher.register(ClientCommandManager.literal("track")
                .then(ClientCommandManager.argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            List<String> list = StreamUtils.getPlayerList(true);
                            list.add("clear");
                            list.addAll(trackedPlayers);
                            return SharedSuggestionProvider.suggest(
                                    list,
                                    builder
                            );
                        })
                        .executes(commandContext -> {
                            String username = StringArgumentType.getString(commandContext, "username");

                            if (username.equals("clear")) {
                                Mod.message(Component.literal("Tracker Cleared!").withColor(ColorBank.WHITE).append(Component.literal(" (" + trackedPlayers.size() + " player" + (trackedPlayers.size() == 1 ? "" : "s") + ")").withColor(ColorBank.WHITE_GRAY)));
                                trackedPlayers.clear();
                                return 1;
                            } else {
                                if (trackedPlayers.contains(username)) {
                                    trackedPlayers.remove(username);

                                    Mod.message(Component.literal("Stopped Tracking: ").withColor(ColorBank.WHITE).append(Component.literal(username).withColor(ColorBank.WHITE_GRAY)));
                                } else {
                                    addTrackedPlayer(username);
                                    Mod.message(Component.literal("Tracking: ").withColor(ColorBank.WHITE).append(Component.literal(username).withColor(ColorBank.WHITE_GRAY)));
                                }
                            }

                            return 1;
                        })
                )
        );
    }


    @Override
    public void tickR(int tick) {
        if (getPlayerHistoryQueue != null) {
            if (!getPlayerHistoryQueue.isEmpty()) {
                if (currentGetPlayerHistory == null) {
                    currentGetPlayerHistory = getPlayerHistoryQueue.removeFirst();
                    initGetPlayerHistory(currentGetPlayerHistory);
                }
            }
        }
        if (currentGetPlayerHistory != null) {
            if (System.currentTimeMillis() - capturePunishmentStartTimestamp > (2.5 * 1000L)) {
                Mod.messageError("Collecting player " + currentGetPlayerHistory + "'s history took too long... aborting...");
                endGetPlayerHistory();
            }
        }
    }

    @Override
    public void tickF(int tick) {

    }

}
