package mia.modmod.features.impl.moderation.tracker.punishments;

import mia.modmod.core.FileManager;

import java.util.List;
import java.util.logging.Filter;

public enum PunishmentTrack {
    // these are matched w/ regex non-case-sensitively
    SPAMMING("Spamming", new PunishmentEscalation(ServerPunishmentType.MUTE,3, PunishmentDuration.D1, PunishmentDuration.D3), List.of("Spamming", "Spam", "Join spam")),
    PLOT_AD("Plot Ad Misuse", new PunishmentEscalation(ServerPunishmentType.MUTE,1, PunishmentDuration.D1, PunishmentDuration.D3), List.of("Plot Ad (Misuse|missue|misues)", "plot ad")),
    FILTER_BYPASS("Filter Bypass", new PunishmentEscalation(ServerPunishmentType.MUTE,3, PunishmentDuration.D1, PunishmentDuration.D14), List.of("Filter Bypass", "Bilter Bypass", "bypassing filter", "chat filter bypass", "bypass filter")),
    TOXICITY_SUICIDE("Suicide Encouragement", new PunishmentEscalation(ServerPunishmentType.MUTE,0, PunishmentDuration.D7, PunishmentDuration.PERM), List.of("Suicide Encouragement", "Suicide")),
    TOXICITY_HARASSMENT("Harassment", new PunishmentEscalation(ServerPunishmentType.MUTE,0, PunishmentDuration.D7, PunishmentDuration.PERM), List.of("Harassment")),
    TOXICITY_GENERAL_RUDENESS("Toxicity (Rudeness)", new PunishmentEscalation(ServerPunishmentType.MUTE,0, PunishmentDuration.D3, PunishmentDuration.D30), List.of("Toxicity", "Rudeness", "being rude", "rude", "Toxicity (Rudeness)", "Disrespect")),
    DISCRIMINATION("Discrimination", new PunishmentEscalation(ServerPunishmentType.MUTE,0, PunishmentDuration.D14, PunishmentDuration.PERM), List.of("Discrimination", "Slurs", "Discrimination / (Filter Bypass|Bilter|Bilter Bypass|Filter Flypass)", "(Trans|Xeno|Homo)phobia", "Anti(|-)semitism", "n(|-)word", "racism", "racist")),
    BANNED_TOPICS("Banned Topics", new PunishmentEscalation(ServerPunishmentType.MUTE,1, PunishmentDuration.D3, PunishmentDuration.PERM), List.of("Banned Topics", "Politics")),

    // this needs to be before so it catches extremely last
    INAPPROPRIATE_CHAT("Inappropriate Chat", new PunishmentEscalation(ServerPunishmentType.MUTE,1, PunishmentDuration.D3, PunishmentDuration.PERM), List.of("^Inappropriate (Chat|Topic)")),
    SEVERELY_INAPPROPRIATE_CHAT("Extremely Inappropriate Chat", new PunishmentEscalation(ServerPunishmentType.MUTE,0, PunishmentDuration.D14, PunishmentDuration.PERM), List.of("(Severely|Extremely|Very|Disturbing|explicit) Inappropriate (Chat|Topics)", "pedo(|philia)", "Sexual", "creepy")),

    // not sure why some of these are listed as having max durations, it doesn't really make sense to me as hacking and macroing are essentially the same thing and usually stuff like autoclicking is done through a hacked client or a custom python script
    // also tabbing is punished more harshly than autoclicking when tabbing is something that can be done in vanilla without any external modifications like wtf?!?
    HACKED_CLIENT("Hacked Client", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.D30, PunishmentDuration.PERM), List.of("Hacked Client", "Reach", "Movement Hacks", "Hacks", "Hacking", "Criticals", "Bhop", "Kill(|-| |_)aura", "Aimbot", "flying", "spider")),
    MACROING("Macroing / Autoclicking", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.D3, PunishmentDuration.PERM), List.of("Macroing", "Autoclicking", "anti( |-)afk")),
    MALICIOUS_ITEMS("Malicious Items", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.D3, PunishmentDuration.PERM), List.of("Malicious Items", "Crash (Items|Shulker)", "Chunk Banning", "Crashing players")),
    INFORMATION_MODS("Disallowed Information Mods", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.D3, PunishmentDuration.PERM), List.of("Xray", "Information Mods", "Informational Mods", "Disallowed Information Mods", "Visible Barriers")),
    CLIENT_EXPLOITING("Client Exploiting", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.D7, PunishmentDuration.PERM), List.of("Tabbing", "Client Exploits")),

    SERVER_CRASHING("Intentional Server Crashing", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.PERM, PunishmentDuration.PERM), List.of("Crashing (Server|Node)", "Server (Crash|Crashing|Crashes|Node)", "Intentionally Crashing (Server|Node)", "Intentional (Server|Node) Crashing", "Exploit Abuse")),

    INAPPROPRIATE_SKIN_USERNAME("Inappropriate Skin / Username (Appeal when changed)", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.PERM, PunishmentDuration.PERM), List.of("(Inappropriate|explicit) Skin", "(Inappropriate|explicit) (|User)Name")),
    BAN_EVASION("Ban Evasion", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.PERM, PunishmentDuration.PERM), List.of("Ban Evasion")),

    // Requires custom duration input

    INAPPROPRIATE_PLOT_CONTENT("Inappropriate Plot Content", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.PERM, PunishmentDuration.PERM), List.of("(inappropriate|explicit|sexual) (plot |)content")),
    MUTE_EVASION("Mute Evasion", new PunishmentEscalation(ServerPunishmentType.BAN,0, PunishmentDuration.PERM, PunishmentDuration.PERM), List.of("Mute Evasion", "Mute Bypass", "bypassing mute", "bypass mute", "mute evasion"));

    public static final List<PunishmentTrack> expiringPunishments = List.of(
            SPAMMING,
            PLOT_AD,
            FILTER_BYPASS
    );

    private final String reasonText;
    private final PunishmentEscalation punishmentEscalation;
    private final List<String> patterns;

    PunishmentTrack(String reasonText, PunishmentEscalation punishmentEscalation, List<String> patterns) {
        this.reasonText = reasonText;
        this.punishmentEscalation = punishmentEscalation;
        this.patterns = patterns;
    }

    public String getReasonText() { return reasonText; }
    public PunishmentEscalation getPunishmentEscalation() { return punishmentEscalation; }
    public List<String> getPatterns() { return patterns; }
}
