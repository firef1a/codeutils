package mia.modmod.core;

import mia.modmod.Mod;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public enum KeyBindCategories {
    GENERAL("general"),
    STAFF("staff"),
    DEVELOPMENT("dev");

    private final KeyMapping.Category category;

    KeyBindCategories(String identifier) {
        this.category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Mod.MOD_ID, identifier));
    }

    public final KeyMapping.Category getCategory() { return this.category; }
}
