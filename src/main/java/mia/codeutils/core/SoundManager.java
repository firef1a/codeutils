package mia.codeutils.core;

import mia.codeutils.Mod;
import net.minecraft.client.resources.sounds.RidingEntitySoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class SoundManager {
    public static void playUIButtonClick() {
        Mod.MC.getSoundManager().play(new RidingEntitySoundInstance(
                Mod.MC.player,
                Mod.MC.player,
                false,
                SoundEvents.UI_BUTTON_CLICK.value(), // The SoundEvent to play
                SoundSource.UI,
                1.0F,
                1.0F, // The pitch (1.0F is normal pitch)
                1.0F
        ));
    }
}
