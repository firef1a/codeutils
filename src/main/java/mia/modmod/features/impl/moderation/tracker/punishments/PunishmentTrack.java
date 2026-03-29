package mia.modmod.features.impl.moderation.tracker.punishments;

import java.util.List;

public enum PunishmentTrack {
    SPAMMING("Spamming", List.of("Spamming", "Spam", "Join spam")),
    PLOT_AD("Plot Ad Misuse", List.of("Plot Ad (Misuse|missue|misues)")),
    FILTER_BYPASS("Filter Bypass", List.of("Filter Bypass", "Bilter Bypass", "bypassing filter", "chat filter bypass", "bypass filter")),
    TOXICITY_SUICIDE("Suicide Encouragement", List.of("Suicide Encouragement", "Suicide")),
    TOXICITY_HARASSMENT("Harassment", List.of("Harassment")),
    DISCRIMINATION("Discrimination", List.of("Discrimination", "Slurs", "Discrimination / (Filter Bypass|Bilter|Bilter Bypass|Filter Flypass)", "(Trans|Xeno|Homo)phobia", "Anti(|-)semitism", "n(|-)word")),
    BANNED_TOPICS("Banned Topics", List.of("Banned Topics", "Politics")),
    INAPPROPRIATE_CHAT("Inappropriate Chat", List.of("^Inappropriate (Chat|Topic)")),
    SEVERELY_INAPPROPRIATE_CHAT("Extremely Inappropriate Chat", List.of("(Severely|Extremely|Very) Inappropriate (Chat|Topics)", "pedo(|philia)", "Sexual", "minors", "creepy")),

    HACKED_CLIENT("Hacked Client", List.of("Hacked Client", "Reach", "Movement Hacks", "Hacks", "Hacking", "Criticals", "Bhop", "Killaura", "Aimbot", "flying")),
    MACROING("Macroing / Autoclicking", List.of("Macroing", "Autoclicking", "anti( |-)afk")),
    MALICIOUS_ITEMS("Malicious Items", List.of("Malicious Items", "Crash (Items|Shulker)", "Chunk Banning", "Crashing players")),
    INFORMATION_MODS("Disallowed Information Mods", List.of("Xray", "Information Mods", "Informational Mods", "Disallowed Information Mods", "Visible Barriers")),
    CLIENT_EXPLOITING("Client Exploiting", List.of("Tabbing", "Client Exploits")),

    SERVER_CRASHING("Intentional Server Crashing", List.of("Crashing (Server|Node)", "Server (Crash|Crashing|Crashes|Node)", "Intentionally Crashing (Server|Node)", "Intentional (Server|Node) Crashing", "Exploit Abuse")),


    INAPPROPRIATE_SKIN_USERNAME("Inappropriate Skin / Username (Appeal when changed)", List.of("(Inappropriate|explicit) Skin", "(Inappropriate|explicit) (|User)Name")),
    BAN_EVASION("Ban Evasion", List.of("Ban Evasion")),
    MUTE_EVASION("Mute Evasion", List.of()); // this one is special <3


    private final String reasonText;
    private final List<String> patterns;

    PunishmentTrack(String reasonText, List<String> patterns) {
        this.reasonText = reasonText;
        this.patterns = patterns;
    }

    public String getReasonText() { return reasonText; }
    public List<String> getPatterns() { return patterns; }
}
