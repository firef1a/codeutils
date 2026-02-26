package mia.codeutils.features.impl.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.codeutils.Mod;
import mia.codeutils.core.StreamUtils;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.impl.AlwaysEnabled;
import mia.codeutils.features.listeners.impl.RegisterCommandListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.features.listeners.impl.TickEvent;
import mia.codeutils.render.screens.PunishScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;

public final class PunishFeature extends Feature implements RegisterCommandListener, TickEvent, RenderHUD, AlwaysEnabled {
    private static PunishScreen punishScreen;
    public PunishFeature(Categories category) {
        super(category, "Punish Screen", "punish", "Moderator punishment ");
    }


    @Override
    public void tickR(int tick) {
    }

    @Override
    public void tickF(int tick) {
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {

    }

    public static void resetPunishScreen() {
        punishScreen = null;
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
            ClientCommandManager.literal("punish")
                .then(
                    ClientCommandManager.argument("username", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                List<String> list = StreamUtils.getPlayerList(false);
                                return SharedSuggestionProvider.suggest(
                                        list,
                                        builder
                                );
                            })
                        .executes(commandContext -> {
                            String username = StringArgumentType.getString(commandContext, "username");
                            if (!(Mod.getCurrentScreen() instanceof PunishScreen) && (punishScreen == null)) {
                                Mod.message("Opening punish screen for " + username + "...");
                                Mod.MC.execute(() -> {
                                    punishScreen = new PunishScreen(null, username);
                                    Mod.setCurrentScreen(punishScreen);
                                });
                            } else {
                                Mod.error("Punish screen is already open!");
                            }
                            return 1;
                        })
                )
                .executes(commandContext -> {
                    Mod.error("Command usage: /punish <username>");
                    return 1;
                })
        );
    }
}
