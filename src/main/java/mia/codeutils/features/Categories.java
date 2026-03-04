package mia.codeutils.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Categories {
    GENERAL(new Category("General", "General Features")),
    DEV(new Category("Development", "Dev-mode Features")),
    SUPPORT(new Category("Support", "Support Staff Features")),
    MODERATION(new Category("Moderation", "Moderator features")),
    SILLY(new Category("Silly :3", ":3:3:3:3:3")),
    INTERNAL(new Category("Internal", "Machine, turn back now. The layers of this palace are not for your kind."));

    private final Category category;

    Categories(Category category) {
        this.category = category;
    }

    public static ArrayList<Category> getCategories() { return Arrays.stream(values()).map(Categories::getCategory).collect(Collectors.toCollection(ArrayList::new)); }
    public Category getCategory() { return this.category; }
}
