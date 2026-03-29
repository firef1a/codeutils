package mia.modmod.features.impl.moderation.tracker.punishments;

import mia.modmod.ColorBank;
import net.minecraft.network.chat.Component;

public enum ServerPunishmentType {
    KICK("kick",Component.literal("[K]").withColor(ColorBank.MC_GRAY)),
    WARN("warn",Component.literal("[W]").withColor(0xffd942)),
    MUTE("mute", Component.literal("[M]").withColor(ColorBank.MC_GREEN)),
    BAN("ban", Component.literal("[B]").withColor(ColorBank.MC_RED));

    private final String commandHeader;
    private final Component prefixText;

    ServerPunishmentType(String commandHeader, Component prefixText) {
        this.commandHeader = commandHeader;
        this.prefixText = prefixText;
    }

    public String getCommandHeader() { return commandHeader; }
    public Component getPrefixText() { return prefixText; }
}
