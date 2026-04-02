package mia.modmod.features.impl.general;

import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.commands.CommandScheduler;
import mia.modmod.features.impl.internal.commands.ScheduledCommand;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.DoubleDataField;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class AutoTip extends Feature implements ChatEventListener {
    private final DoubleDataField autoTipDelay;
    public AutoTip(Categories category) {
        super(category, "Auto Tip", "autotip", "Automatically tips boosters");
        autoTipDelay = new DoubleDataField("Auto Tip Delay (seconds)", "", ParameterIdentifier.of(this, "delay"), 4.0, true);
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        if (Pattern.matches("^⏵+ Use /tip to show your appreciation and receive a □ token notch!", message.base().getString())) {
            CommandScheduler.addCommand(new ScheduledCommand("tip", Math.max((long) (autoTipDelay.getValue() * 1000L), 0L)));
        }
        return message.pass();
    }
}
