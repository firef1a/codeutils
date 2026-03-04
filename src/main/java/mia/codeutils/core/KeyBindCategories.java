package mia.codeutils.core;

import mia.codeutils.Mod;

public enum KeyBindCategories {
    GENERAL_CATEGORY(Mod.MOD_NAME + " - General"),
    DEVELOPMENT_CATEGORY(Mod.MOD_NAME + " - Development");

    private final String name;
    KeyBindCategories(String name) { this.name = name; }

    public final String displayName() { return this.name; }
}
