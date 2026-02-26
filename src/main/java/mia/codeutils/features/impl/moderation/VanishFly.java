package mia.codeutils.features.impl.moderation;

import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;

public final class VanishFly extends Feature {
    public VanishFly(Categories category) {
        super(category, "Vanish Fly", "vanishfly", "removed default spectator acceleration");
    }
}
