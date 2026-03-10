package mia.codeutils.features.impl.general;

import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.commands.CommandScheduler;
import mia.codeutils.features.impl.internal.commands.ScheduledCommand;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.ChatEventListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class PlotAdBlocker extends Feature implements ChatEventListener {
    public PlotAdBlocker(Categories category) {
        super(category, "Plot Ad Blocker", "adblocker", "Blocks plot ads.");
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        return message.pass();
    }
}
