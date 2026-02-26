package mia.codeutils.features.listeners.impl;

import com.mojang.brigadier.CommandDispatcher;
import mia.codeutils.features.listeners.AbstractEventListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

public interface RegisterCommandListener extends AbstractEventListener {
    void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess);
}
