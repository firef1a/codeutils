package mia.codeutils.core;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class MiaKeyBind extends KeyMapping {
    private long lastPressed;

    public MiaKeyBind(String translationKey, int code, KeyBindCategories category) {
        super(translationKey, code, Category.register(Identifier.withDefaultNamespace(category.displayName())));
        lastPressed = 0L;
    }

    public boolean isDown() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastPressed;
        if (diff < 100L) return true;
        if (super.isDown()) {
            lastPressed = currentTime;
            return true;
        };
        return false;
    }
}
