package mia.modmod.features.impl.moderation.tracker.punishments;

import mia.modmod.ColorBank;
import net.minecraft.network.chat.Component;

public enum ServerPunishmentType {
    KICK("Kick", "kick",Component.literal("[K]").withColor(ColorBank.MC_GRAY)),
    WARN("Warn", "warn",Component.literal("[W]").withColor(0xffd942)),
    MUTE("Mute", "mute", Component.literal("[M]").withColor(ColorBank.MC_GREEN)),
    BAN("Ban", "ban", Component.literal("[B]").withColor(ColorBank.MC_RED));

    private final String name;
    private final String commandHeader;
    private final Component prefixText;

    ServerPunishmentType(String name, String commandHeader, Component prefixText) {
        this.name = name;
        this.commandHeader = commandHeader;
        this.prefixText = prefixText;
    }

    public String getName() { return this.name; }
    public String getCommandHeader() { return commandHeader; }
    public Component getPrefixText() { return prefixText; }
}
