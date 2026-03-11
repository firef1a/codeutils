package mia.codeutils.core;

import mia.codeutils.Mod;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.Random;

public class MiaKeyBind extends KeyMapping {
    private long lastPressed;
    private long threshold = 100L;
    public boolean down;
    public Runnable onKeyDown;

    public MiaKeyBind(String translationKey, int code, KeyBindCategories category) {
        //super(translationKey, code, Category.register(Identifier.fromNamespaceAndPath(Mod.MOD_ID, category.displayName())));
        this(translationKey, code, category, () -> {});
    }

    public MiaKeyBind(String translationKey, int code, KeyBindCategories category, Runnable onKeyDown) {
        //super(translationKey, code, Category.register(Identifier.fromNamespaceAndPath(Mod.MOD_ID, category.displayName())));
        super(translationKey, code, Category.register(Identifier.fromNamespaceAndPath("test", "sadf" + Math.random())));
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

    public boolean rawIsDown() {
        return super.isDown();
    }

    public boolean isDown() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastPressed;
        if (diff < threshold) return true;
        if (super.isDown()) {
            lastPressed = currentTime;
            return true;
        };
        return false;
    }
}
