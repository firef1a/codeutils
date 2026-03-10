package mia.codeutils.features.impl.internal;

import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;

public final class PermissionTracker extends Feature {
    public PermissionTracker(Categories category) {
        super(category, "Permission Tracker", "perm_tracker", "Controls what features should be active based on your ranks.");
    }

}