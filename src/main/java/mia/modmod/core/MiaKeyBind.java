package mia.modmod.core;

import net.minecraft.client.KeyMapping;

public class MiaKeyBind extends KeyMapping {
    private long lastPressed;
    private static final long threshold = 100L;
    public boolean down;
    public Runnable onKeyDown;

    public MiaKeyBind(String translationKey, int code, KeyBindCategories category) {
        //super(translationKey, code, Category.register(Identifier.fromNamespaceAndPath(Mod.MOD_ID, category.displayName())));
        this(translationKey, code, category, () -> {});
    }

    public MiaKeyBind(String translationKey, int code, KeyBindCategories category, Runnable onKeyDown) {
        super(translationKey, code, category.getCategory());
        this.onKeyDown = onKeyDown;
        lastPressed = 0L;
    }

    public void tick() {
        if (isDown() & !down) {
            down = true;
            onKeyDown.run();
        }
        if (!isDown()){
            down = false;
        }
    }

    public boolean isDown() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastPressed;
        if (diff < threshold) return true;
        if (super.isDown()) {
            lastPressed = currentTime;
            return true;
        }
        return false;
    }
}
