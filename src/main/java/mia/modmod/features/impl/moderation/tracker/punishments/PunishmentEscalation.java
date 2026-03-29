package mia.modmod.features.impl.moderation.tracker.punishments;

public record PunishmentEscalation(ServerPunishmentType punishmentType, int chances, PunishmentDuration severity, PunishmentDuration maxDuration) {
    public static String buildCommand(String playerName, ServerPunishmentType type, String duration, String reason, boolean silent) {
        return "/" + type.getCommandHeader() + " " + playerName + (duration.equals("perm") ? "" : (duration.isEmpty() ? "" : (duration.equals("permanent") ? "" : " " + duration))) + (silent ? " -s" : "")  + " " + reason;
    }
}
