package mia.codeutils.features.listeners;

public enum DFMode {
    NONE("None", false, false, false, false),
    SPAWN("Spawn", false, false, false, false),
    PLAY("Play", true, false, false, false),
    BUILD("Build", true, true, false, false),
    DEV("Dev", true, true, true, true),
    CODE_SPECTATE("Code Spectate", true, false, false, true);

    private final String name;
    private final boolean onPlot;
    private final boolean canBuild;
    private final boolean canEditCode;
    private final boolean canViewCode;

    private DFMode(String name, boolean onPlot, boolean canBuild, boolean canEditCode, boolean canViewCode) {
        this.name = name;
        this.onPlot = onPlot;
        this.canBuild = canBuild;
        this.canEditCode = canEditCode;
        this.canViewCode = canViewCode;
    }

    public String getName() { return name; }
    public boolean isOnPlot() { return onPlot; }

    public boolean canBuild() { return canBuild; }
    public boolean canEditCode() { return canEditCode; }
    public boolean canViewCode() { return canViewCode; }


    public boolean isDev() { return this.equals(DFMode.DEV); }
    public boolean isBuild() { return this.equals(DFMode.BUILD); }
    public boolean isPlay() { return this.equals(DFMode.PLAY); }
    public boolean isSpawn() { return this.equals(DFMode.SPAWN); }
    public boolean isCodeSpectate() { return this.equals(DFMode.CODE_SPECTATE); }
}
