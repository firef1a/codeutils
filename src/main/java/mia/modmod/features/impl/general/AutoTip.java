package mia.modmod.features.impl.general;

import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.commands.CommandScheduler;
import mia.modmod.features.impl.internal.commands.ScheduledCommand;
import mia.modmod.features.impl.internal.permissions.Permissions;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class AutoTip extends Feature implements ChatEventListener {
    public AutoTip(Categories category) {
        super(category, "AutoTip", "autotip", "Automatically tips boosters", Permissions.NONE);
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        if (Pattern.compile("^⏵⏵⏵ Use /tip to show your appreciation and receive a □ token notch!").matcher(message.base().getString()).find()) {
            CommandScheduler.addCommand(new ScheduledCommand("tip", 3000L));
        }
        return message.pass();
    }
}
