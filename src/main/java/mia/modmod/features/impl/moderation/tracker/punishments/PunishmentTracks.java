package mia.modmod.features.impl.moderation.tracker.punishments;

import java.util.ArrayList;
import java.util.HashMap;

public class PunishmentTracks {
    private final ArrayList<PunishmentData> rawPunishments;
    private final HashMap<PunishmentTrack, ArrayList<PunishmentData>> trackedPunishments;

    public PunishmentTracks() {
        trackedPunishments = new HashMap<>();
        rawPunishments = new ArrayList<>();

        for (PunishmentTrack track : PunishmentTrack.values()) {
            trackedPunishments.put(track, new ArrayList<>());
        }
    }

    public void addPunishment(PunishmentTrack track, PunishmentData data) {
        trackedPunishments.get(track).add(data);
    }

    public void addUntrackedPunishment(PunishmentData data) {
        rawPunishments.add(data);
    }

    public ArrayList<PunishmentData> getAllPunishments() { return rawPunishments; }
    public HashMap<PunishmentTrack, ArrayList<PunishmentData>> getTrackedPunishments() { return trackedPunishments; }
}
