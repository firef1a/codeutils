package mia.modmod.features.impl.general.title;

public enum DFIcons implements QuickJoinIcon {
    df("diamondfire"),
    gay("pridefire"),
    trans("transfire"),
    melon("melon"),
    melon_king2("melon_king"),
    site03("site03");
    //mace("mace");



    private final String path;
    private DFIcons(String path) {
        this.path = path;
    }

    public String getAsString() { return this.toString(); }

    public String getDisabledPath() { return path + "/" + path + "_disabled.png"; }
    public String getEnabledPath() { return path + "/" + path + "_enabled.png"; }
}
