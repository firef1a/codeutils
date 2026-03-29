package mia.modmod.features.impl.moderation;

import mia.modmod.ColorBank;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.general.chat.SimplifiedStaffChatTags;
import mia.modmod.features.impl.internal.permissions.ModeratorPermission;
import mia.modmod.features.impl.internal.permissions.Permissions;
import mia.modmod.features.impl.internal.permissions.SupportPermission;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BetterVanishMSG extends Feature implements ChatEventListener {
    public BetterVanishMSG(Categories category) {
        super(category, "Better Vanish MSG", "vanishmsgs", "Changes vanish messages.", new Permissions(SupportPermission.NONE, ModeratorPermission.JR_MOD));
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String text = message.base().getString();
        Matcher matcher;
        boolean betterSCTags = FeatureManager.getFeature(SimplifiedStaffChatTags.class).getEnabled();

        matcher = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.").matcher(text);
        if (matcher.find()) {
            return message.modified(Component.empty()
                    .append(betterSCTags ? SimplifiedStaffChatTags.MOD : Component.literal("[MOD]").withColor(ColorBank.MC_DARK_GREEN))
                    .append(Component.translatable("modmod.vanishmsgs.enabled").withColor(ColorBank.MC_GRAY))
                    .append(Component.literal(" ✔").withColor(ColorBank.MC_GREEN)));
        }

        matcher = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.").matcher(text);
        if (matcher.find()) {
            return message.modified(Component.empty()
                    .append(betterSCTags ? SimplifiedStaffChatTags.MOD : Component.literal("[MOD]").withColor(ColorBank.MC_DARK_GREEN))
                    .append(Component.translatable("modmod.vanishmsgs.disabled").withColor(ColorBank.MC_GRAY))
                    .append(Component.literal(" ❌").withColor(ColorBank.MC_RED)));
        }

        return message.pass();
    }
}
