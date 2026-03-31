package mia.modmod.features.impl.internal.permissions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.config.ConfigStore;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.internal.commands.ChatConsumer;
import mia.modmod.features.impl.internal.commands.CommandScheduler;
import mia.modmod.features.impl.internal.commands.ScheduledCommand;
import mia.modmod.features.listeners.impl.AlwaysEnabled;
import mia.modmod.features.listeners.impl.RegisterCommandListener;
import mia.modmod.features.listeners.impl.ServerConnectionEventListener;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.BooleanDataField;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class PermissionTracker extends Feature implements AlwaysEnabled, ServerConnectionEventListener, RegisterCommandListener {
    private final BooleanDataField supportPermission;
    private final BooleanDataField moderatorPermission;
    private final BooleanDataField adminPermission;

    public PermissionTracker(Categories category) {
        super(category, "Permission Tracker", "perm_tracker", "Controls what features should be active based on your ranks.");

        String warningDescription = "Changing this parameter will not automatically enable related features.\n\nIn order to spoof your permissions, use /change_permissions <t/f> <t/f> <t/f>\nThen disable this feature as it will try to correct your permissions everytime you join.";
        supportPermission = new BooleanDataField("Support Permission", warningDescription, ParameterIdentifier.of(this, "support_permission"), false, true);
        moderatorPermission = new BooleanDataField("Moderator Permission", warningDescription, ParameterIdentifier.of(this, "moderator_permission"), false, true);
        adminPermission = new BooleanDataField("Admin Permission", warningDescription, ParameterIdentifier.of(this, "admin_permission"), false, true);
    }

    public static Permissions getPlayerPermissions() {
        PermissionTracker permissionTracker = FeatureManager.getFeature(PermissionTracker.class);
        return new Permissions(
                permissionTracker.supportPermission.getValue(),
                permissionTracker.moderatorPermission.getValue(),
                permissionTracker.adminPermission.getValue()
        );
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

    private void validateNewPermissions(Permissions newPermission) {
        ArrayList<Component> textList = new ArrayList<>();

        Permissions oldPermissions = getPlayerPermissions();

        if (!oldPermissions.equals(newPermission)) {
            textList.add(Component.literal("Permission change detected:"));
        }

        record PermissionValidate(
                String permissionName,
                boolean oldPermission,
                boolean newPermission,
                Categories categories
        ) { };

        List<PermissionValidate> validateList = List.of(
                new PermissionValidate("Support", oldPermissions.supportPermission(), newPermission.supportPermission(), Categories.SUPPORT),
                new PermissionValidate("Moderation", oldPermissions.moderatorPermission(), newPermission.moderatorPermission(), Categories.MODERATION),
                new PermissionValidate("Admin", oldPermissions.adminPermission(), newPermission.adminPermission(), null)
        );

        for (PermissionValidate permissionValidate : validateList) {
            if (permissionValidate.oldPermission() != permissionValidate.newPermission()) {
                textList.add(
                        (permissionValidate.newPermission() ? Component.literal("ENABLED").withColor(ColorBank.MC_GREEN) : Component.literal("DISABLED").withColor(ColorBank.MC_RED)).append(

                        Component.literal(" " + permissionValidate.permissionName() + " features").withColor(ColorBank.WHITE_GRAY))
                );
                if (permissionValidate.categories != null) permissionValidate.categories.getCategory().setEnabled(permissionValidate.newPermission());
            }
        }

        if (!oldPermissions.equals(newPermission)) {
            for (Component text : textList) {
                Mod.message(text);
            }
            Mod.message(Component.literal("Rejoin DF for features changes to take full effect.").withColor(ColorBank.MC_RED));
        }


        supportPermission.setValue(newPermission.supportPermission());
        moderatorPermission.setValue(newPermission.moderatorPermission());
        adminPermission.setValue(newPermission.adminPermission());
    }

    @Override
    public void DFConnectJoin(ClientPacketListener networkHandler) {
        CommandScheduler.addCommand(
                new ScheduledCommand(
                        "whois",
                        0L,
                        List.of(
                                new ChatConsumer(
                                        Pattern.compile("^ {39}\\nProfile of (.{3,16}) (?:|\\(.*\\))\\n\\n→ Ranks: (.+)\\n→ Badges: .*\\n→ Joined:"),
                                        (matcher) -> {
                                            if (matcher.group(1).equals(Mod.getPlayerName())) {
                                                Permissions playerPermissions = new Permissions();
                                                String rankString = matcher.group(2);

                                                ArrayList<DFRank> permissiveRanks = new ArrayList<>();

                                                for (DFRank rank : DFRank.values()) {
                                                    if (rankString.contains(rank.getPattern())) {
                                                        //Mod.messageError(rank.name());
                                                        playerPermissions = playerPermissions.add(rank.getPermissions());
                                                        if (!Permissions.NONE.hasPerm(rank.getPermissions())) permissiveRanks.add(rank);
                                                    }
                                                }

                                                if (!getPlayerPermissions().equals(playerPermissions)) {
                                                    if (permissiveRanks.isEmpty()) Mod.message(Component.literal("No permissive ranks detected!").withColor(ColorBank.MC_RED));
                                                    else Mod.message("Detected the following permissive ranks:");

                                                    for (DFRank rank : permissiveRanks) {
                                                        Mod.message(Component.literal(rank.getPattern()).withColor(ColorBank.WHITE_GRAY));
                                                    }
                                                    Mod.message(Component.empty());
                                                }

                                                validateNewPermissions(playerPermissions);
                                            }
                                        },
                                        () -> {
                                            Mod.messageError("Error grabbing rank data... try again later?");
                                        },
                                        5000L,
                                        true
                                )
                        )
                )
        );
    }

    @Override
    public void DFConnectDisconnect(ClientPacketListener networkHandler) {

    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("change_permissions")
                .then(ClientCommandManager.argument("support_permission", BoolArgumentType.bool())
                        .then(ClientCommandManager.argument("moderator_permission", BoolArgumentType.bool())
                                .then(ClientCommandManager.argument("admin_permission", BoolArgumentType.bool())
                                        .executes(commandContext -> {
                                            boolean support_permission = BoolArgumentType.getBool(commandContext, "support_permission");
                                            boolean moderator_permission = BoolArgumentType.getBool(commandContext, "moderator_permission");
                                            boolean admin_permission = BoolArgumentType.getBool(commandContext, "admin_permission");

                                            validateNewPermissions(new Permissions(support_permission, moderator_permission, admin_permission));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}