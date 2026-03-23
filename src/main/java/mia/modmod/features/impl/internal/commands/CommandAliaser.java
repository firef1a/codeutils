package mia.modmod.features.impl.internal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.modmod.Mod;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.listeners.impl.AlwaysEnabled;
import mia.modmod.features.listeners.impl.RegisterCommandListener;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import java.util.List;

public final class CommandAliaser extends Feature implements RegisterCommandListener, AlwaysEnabled {
    public CommandAliaser(Categories category) {
        super(category, "cmd aliser", "cmdaliser", "registers shortcuts for common commands");
    }

    record SimpleAlias(String command,  @NotBlank String ...aliases) { }
    record GreedyAlias(String command, String greedyName, @NotBlank String ...aliases) { }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        List<SimpleAlias> simpleAliasList = List.of(
                new SimpleAlias("mod vanish", "mv", "modv"),
                new SimpleAlias("admin vanish", "av", "adminv"),
                new SimpleAlias("support queue", "queue"),
                new SimpleAlias("item get debug_stick", "debug", "debug_stick"),
                new SimpleAlias("item nbt", "nbt"),
                new SimpleAlias("lagslayer", "ls")
        );

        List<GreedyAlias> greedyAliasList = List.of(
                new GreedyAlias("plot vars", "pattern", "vars"),
                new GreedyAlias("support spectate-code", "id", "cs", "codespectate"),
                new GreedyAlias("support stats", "stats", "name")
        );

        for (SimpleAlias simpleAlias : simpleAliasList) {
            for (String alias : simpleAlias.aliases()) {
                dispatcher.register(
                    ClientCommandManager.literal(alias)
                        .executes(commandContext -> {
                            Mod.sendCommand("/" + simpleAlias.command());
                            return 1;
                        })
                );
            }
        }

        for (GreedyAlias greedyAlias : greedyAliasList) {
            for (String alias : greedyAlias.aliases()) {
                dispatcher.register(
                    ClientCommandManager.literal(alias)
                        .then(
                            ClientCommandManager.argument(greedyAlias.greedyName, StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                    String greedy = StringArgumentType.getString(commandContext, greedyAlias.greedyName);

                                    Mod.sendCommand("/" + greedyAlias.command() + " " + greedy);
                                    return 1;
                                })
                        )
                );
            }
        }
    }
}
