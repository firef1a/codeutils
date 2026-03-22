package mia.modmod.features.impl.internal.permissions;

import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.listeners.impl.AlwaysEnabled;

public final class PermissionTracker extends Feature implements AlwaysEnabled {
    public PermissionTracker(Categories category) {
        super(category, "Permission Tracker", "perm_tracker", "Controls what features should be active based on your ranks.", Permissions.NONE);
    }
}