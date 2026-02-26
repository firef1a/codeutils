package mia.codeutils.core;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class KeyBindManager {
    public static MiaKeyBind registerKeyBind(MiaKeyBind keyBinding) {
        return (MiaKeyBind) KeyBindingHelper.registerKeyBinding(keyBinding);
    }
}
