package mia.codeutils.features.impl.internal;

import com.mojang.brigadier.CommandDispatcher;
import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.impl.AlwaysEnabled;
import mia.codeutils.features.listeners.impl.RegisterCommandListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.features.listeners.impl.TickEvent;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public final class ConfigScreenFeature extends Feature implements RegisterCommandListener, TickEvent, RenderHUD, AlwaysEnabled {

    public ConfigScreenFeature(Categories category) {
        super(category, "conifg screen", "congifscreen", "description");
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

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
            ClientCommandManager.literal("codeutils")
                .executes(commandContext -> {
                    Mod.message(Component.translatable("codeutils.config.open_config"));
                    return 1;
                })
        );
    }
}
