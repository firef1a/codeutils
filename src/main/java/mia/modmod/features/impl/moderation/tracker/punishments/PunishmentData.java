package mia.modmod.features.impl.moderation.tracker.punishments;

import mia.modmod.Mod;

public record PunishmentData(String offender, ServerPunishmentType punishmentType, String issuer, String reason, boolean isActive, ChronoTimestamp chronoTimestamp) {
    public PunishmentData(String offender, String punishmentData, String issuer, String reason, String activeString, ChronoTimestamp chronoTimestamp) {
        this(offender, parsePunishData(punishmentData), issuer, reason, activeString.equals("Active"), chronoTimestamp);
    }

    private static ServerPunishmentType parsePunishData(String punishmentData) {
        return switch (punishmentData) {
            case "warned" -> ServerPunishmentType.WARN;
            case "muted" -> ServerPunishmentType.MUTE;
            case "banned" -> ServerPunishmentType.BAN;
            case "kicked" -> ServerPunishmentType.KICK;
            default -> {
                Mod.error("Error while parsing punishmentData, \"" + punishmentData + "\" is not a valid type");
                yield null;
            }
        };
    }

}
