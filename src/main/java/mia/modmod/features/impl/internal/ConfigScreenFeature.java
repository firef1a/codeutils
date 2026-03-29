package mia.modmod.features.impl.internal;

import com.mojang.brigadier.CommandDispatcher;
import mia.modmod.Mod;
import mia.modmod.config.ConfigStore;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.permissions.Permissions;
import mia.modmod.features.listeners.impl.AlwaysEnabled;
import mia.modmod.features.listeners.impl.RegisterCommandListener;
import mia.modmod.features.listeners.impl.RenderHUD;
import mia.modmod.features.listeners.impl.TickEvent;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public final class ConfigScreenFeature extends Feature implements RegisterCommandListener, TickEvent, RenderHUD, AlwaysEnabled {
    private boolean openConfigScreenNextTick;

    public ConfigScreenFeature(Categories category) {
        super(category, "conifg screen", "congifscreen", "description");
    }

    @Override
    public void tickR(int tick) {
        if (Mod.getCurrentScreen() == null && openConfigScreenNextTick) {
            Mod.setCurrentScreen(ConfigStore.getLibConfig().generateScreen(Mod.getCurrentScreen()));
            openConfigScreenNextTick = false;
        }
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
            ClientCommandManager.literal("modmod")
                .executes(commandContext -> {
                    Mod.message(Component.translatable("modmod.config.open_config"));
                    Mod.MC.execute(() -> {
                        Mod.setCurrentScreen(ConfigStore.getLibConfig().generateScreen(Mod.getCurrentScreen()));
                    });
                    openConfigScreenNextTick = true;
                    return 1;
                })
        );
    }
}
