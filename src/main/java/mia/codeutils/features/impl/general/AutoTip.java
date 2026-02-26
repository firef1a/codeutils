package mia.codeutils.features.impl.general;

import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.commands.CommandScheduler;
import mia.codeutils.features.impl.internal.commands.ScheduledCommand;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.ChatEventListener;
import mia.codeutils.features.parameters.ParameterIdentifier;
import mia.codeutils.features.parameters.impl.BooleanDataField;
import mia.codeutils.features.parameters.impl.IntegerDataField;
import mia.codeutils.features.parameters.impl.IntegerSliderDataField;
import mia.codeutils.features.parameters.impl.StringDataField;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class AutoTip extends Feature implements ChatEventListener {
    public AutoTip(Categories category) {
        super(category, "AutoTip", "autotip", "Automatically tips boosters");
        new BooleanDataField("testing", ParameterIdentifier.of(this, "testing"), true, true);
        new StringDataField("testing", ParameterIdentifier.of(this, "abc"), "hi", true);
        new IntegerDataField("testing", ParameterIdentifier.of(this, "dc"), 23424, true);
        new IntegerSliderDataField("testing", ParameterIdentifier.of(this, "dc"), 50, 0, 100, true);
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        if (Pattern.compile("⏵⏵⏵ Use /tip to show your appreciation and receive a □ token notch!").matcher(message.base().getString()).find()) {
            CommandScheduler.addCommand(new ScheduledCommand("tip", 2500L));
        }
        return message.pass();
    }
}
