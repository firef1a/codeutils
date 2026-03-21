package mia.modmod.features.impl.internal.permissions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.OptionGroup;
import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.internal.commands.ChatConsumer;
import mia.modmod.features.impl.internal.commands.CommandScheduler;
import mia.modmod.features.impl.internal.commands.ScheduledCommand;
import mia.modmod.features.listeners.impl.ServerConnectionEventListener;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.features.parameters.ParameterDataField;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.InternalDataField;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class PermissionTracker extends Feature implements ServerConnectionEventListener, TickEvent {
    private int ticksSinceJoin = 0;
    private DFRankArrayDataField DFRanksData;

    private final ScheduledCommand whoisCommand = new ScheduledCommand(
            "whois",
            0L,
            List.of(
                    new ChatConsumer(
                            Pattern.compile("^ {39}\\nProfile of (.{3,16}) (?:|\\(.*\\))\\n\\n→ Ranks: (.*)\\n→ Badges: (.*)\\n→ Joined: (.*)\\n(?:|→ About: (.*))\\n {39}"),
                            (matcher) -> {
                                if (!DFRanksData.getValue().isEmpty()) return;
                                String name = matcher.group(1);
                                String serializedRanks = matcher.group(2);
                                String serializedBadges = matcher.group(3);
                                String serializedJoinData = matcher.group(4);


                                ArrayList<DFRank> ranks = new ArrayList<>();

                                if (name.equals(Mod.getPlayerName())) {
                                    for (DFRank rank : DFRank.values()) {
                                        if (serializedRanks.contains(rank.matcher)) {
                                            ranks.add(rank);
                                        }
                                    }
                                    DFRanksData.setValue(ranks);
                                    confirmPermissions();
                                } else {
                                    confirmError();
                                }
                            },
                            () -> {
                                if (!DFRanksData.getValue().isEmpty()) return;
                                confirmError();
                            },
                            5000L,
                            true
                    )
            )
    );

    private static class DFRankArrayDataField extends ParameterDataField<List<DFRank>> implements InternalDataField {
        public DFRankArrayDataField(String name, ParameterIdentifier identifier, List<DFRank> defaultValue, boolean isConfig) {
            super(name, identifier, defaultValue, isConfig);
        }

        @Override
        public void serialize(JsonObject jsonObject) {
            JsonArray array = new JsonArray();
            for (DFRank rank : getValue()) {
                array.add(rank.name());
            }

            jsonObject.add(identifier.getIdentifier(), array);
        }

        @Override
        public List<DFRank> deserialize(JsonElement jsonObject) {
            ArrayList<DFRank> ranks = new ArrayList<>();

            for (JsonElement serializedRank : jsonObject.getAsJsonArray()){
                ranks.add(DFRank.valueOf(serializedRank.getAsString()));
            }

            return ranks;
        }

        public Permissions getPermissions() {
            return DFRank.getStaffPermissions(getValue());
        }

        @Override
        public void addYACLParameter(OptionGroup.Builder featureGroup) {

        }
    }


    public PermissionTracker(Categories category) {
        super(category, "Permission Tracker", "perm_tracker", "Controls what features should be active based on your ranks.", Permissions.NONE);
        DFRanksData = new DFRankArrayDataField("df_ranks", new ParameterIdentifier(this, "df_ranks"), List.of(), false);

        if (DFRanksData.getValue().isEmpty()) {
            for (Feature feature : FeatureManager.getFeatures()) {
                if (!(feature.getRequiredPermissions().supportPermission().equals(SupportPermission.NONE) && feature.getRequiredPermissions().moderatorPermission().equals(ModeratorPermission.NONE))) {
                    feature.setEnabled(false);
                    Mod.log("Disabling feature: " + feature.getID());
                }
            }
        }
    }

    public static Permissions getPermissions() {
        if (!FeatureManager.hasFeature(PermissionTracker.class)) return Permissions.NONE;
        if (FeatureManager.getFeature(PermissionTracker.class) == null) return Permissions.NONE;
        return FeatureManager.getFeature(PermissionTracker.class).internalGetPermissions();
    }

    public Permissions internalGetPermissions() {
        return DFRanksData.getPermissions();
    }

    private void confirmPermissions() {
        Permissions permissions = internalGetPermissions();

        Mod.rawMessage(Component.empty());
        Mod.message(Component.literal("Detected the following ranks:"));
        for (DFRank rank : DFRanksData.getValue()) {
            Mod.message(Component.literal(rank.matcher).withColor(ColorBank.MC_GRAY));
        }
        Mod.rawMessage(Component.empty());
        Mod.message(Component.literal("Support Permissions - ").append(Component.literal(permissions.supportPermission().name()).withColor(0x69afff)));
        Mod.message(Component.literal("Moderator Permissions - ").append(Component.literal(permissions.moderatorPermission().name()).withColor(0x85ff75)));

        Mod.message("If the following permissions / ranks are incorrect, you may set them manually with /rank <add/del/list> <rank>");
        Mod.rawMessage(Component.empty());
        if (!permissions.equals(Permissions.NONE)) {
            Mod.message("The following features have been enabled due to your permissions: ");
            for (Feature feature : FeatureManager.getFeatures()) {
                Permissions requiredPermissions = feature.getRequiredPermissions();
                if (permissions.supportPermission().atLeast(requiredPermissions.supportPermission()) && permissions.moderatorPermission().atLeast(requiredPermissions.moderatorPermission()) &&
                        !(requiredPermissions.supportPermission().equals(SupportPermission.NONE) &&  requiredPermissions.moderatorPermission().equals(ModeratorPermission.NONE))
                ) {
                    Mod.message(Component.literal(feature.getName()).withColor(ColorBank.MC_GRAY));
                    feature.setEnabled(true);
                }
            }
        }
    }

    private static void confirmError() {
        Mod.error("Failed to grab /profile information...");
        Mod.error("Run /validate_permissions or relog to try again...");
        Mod.error("Ranks may be manually set as well via /rank <add/del/list> <rank>");
    }

    private void requestPlayerRanks() {
        CommandScheduler.addCommand(whoisCommand);
    }

    @Override
    public void tickR(int tick) {

    }

    @Override
    public void tickF(int tick) {
        ticksSinceJoin++;
    }

    @Override
    public void DFConnectJoin(ClientPacketListener networkHandler) {
        if (Mod.MC.level != null) {
            if (Mod.MC.player != null) {
                if (DFRanksData.getValue().isEmpty()) {
                    if (!CommandScheduler.getScheduledCommands().contains(whoisCommand)){
                        requestPlayerRanks();
                    }
                }
            }
        }
    }

    @Override
    public void DFConnectDisconnect(ClientPacketListener networkHandler) {

    }

    @Override
    public void serverConnectInit(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }

    @Override
    public void serverConnectJoin(ClientPacketListener networkHandler, PacketSender sender, Minecraft minecraftServer) {

    }

    @Override
    public void serverConnectDisconnect(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }


}