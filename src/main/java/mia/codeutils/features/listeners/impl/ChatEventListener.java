package mia.codeutils.features.listeners.impl;

import mia.codeutils.features.listeners.AbstractEventListener;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface ChatEventListener extends AbstractEventListener {
    ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci);
}
