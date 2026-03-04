package mia.codeutils.features.impl.support.hud;

public record SupportQuestionEntry(String name, String rank, String message, long timestamp) {
    public SupportQuestionEntry(String name, String rank, String message) {
        this(name, rank, message, (Math.ceilDiv(System.currentTimeMillis(), 1000L) * 1000L) + 3L);
    }
}
