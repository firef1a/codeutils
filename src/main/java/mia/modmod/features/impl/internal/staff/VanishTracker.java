package mia.modmod.features.impl.internal.staff;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.general.chat.SimplifiedStaffChatTags;
import mia.modmod.features.impl.internal.permissions.PermissionTracker;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.AlwaysEnabled;
import mia.modmod.features.listeners.impl.ChatEventListener;
import mia.modmod.features.listeners.impl.PacketListener;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.BooleanDataField;
import mia.modmod.features.parameters.impl.InternalBooleanDataField;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VanishTracker extends Feature implements ChatEventListener, PacketListener, AlwaysEnabled, TickEvent {
    private final BooleanDataField modVanishEnabledField;
    private final BooleanDataField adminVanishEnabledField;
    private final BooleanDataField ytVanishEnabledField;

    private final BooleanDataField customVanishMessage;

    private boolean isAdminVanish;

    public static final Pattern VANISH_ENABLED = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.$");
    public static final Pattern VANISH_DISABLED = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.$");

    public static final Pattern VANISH_PREFERENCE_ENABLED = Pattern.compile("^» The preference Mod Vanish has been set to true\\.$");
    public static final Pattern VANISH_PREFERENCE_DISABLED = Pattern.compile("^» The preference Mod Vanish has been set to false\\.$");

    public VanishTracker(Categories category) {
        super(category, "Vanish Tracker", "vstatetracker", "Tracks vanish state");
        modVanishEnabledField = new BooleanDataField("Mod Vanish", "", ParameterIdentifier.of(this, "mod_vanish"), false, true);
        adminVanishEnabledField = new BooleanDataField("Admin Vanish", "", ParameterIdentifier.of(this, "admin_vanish"), false, true);
        ytVanishEnabledField = new BooleanDataField("YT Vanish", "", ParameterIdentifier.of(this, "yt_vanish"), false, true);

        customVanishMessage = new BooleanDataField("Custom Vanish MSGs", "Makes admin and mod vanish msgs look better imo", ParameterIdentifier.of(this, "custom_vanish_msg"), true, true);
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundChatCommandPacket(String command)) {
            if (Pattern.matches("^adminv (?:on|off)", command) && PermissionTracker.getPlayerPermissions().adminPermission()) {
                if (modVanishEnabledField.getValue()) {
                    Mod.messageError("Please disable mod vanish before enabling admin vanish.");
                    ci.cancel();
                    return;
                }
                isAdminVanish = true;
            }
            if (Pattern.matches("^mod vanish", command) && PermissionTracker.getPlayerPermissions().moderatorPermission()) {
                if (adminVanishEnabledField.getValue()) {
                    Mod.messageError("Please disable admin vanish before enabling mod vanish.");
                    ci.cancel();
                    return;
                }
                isAdminVanish = false;
            }
            //Mod.messageError(command);
        }
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String text = message.base().getString();
        Matcher matcher, vMatcher, pMatcher;
        boolean localIsAdminVanish = isAdminVanish;


        // enabled
        vMatcher = VANISH_ENABLED.matcher(text);
        pMatcher = VANISH_PREFERENCE_ENABLED.matcher(text);
        if (vMatcher.find()) {
            isAdminVanish = false;
            if (localIsAdminVanish) adminVanishEnabledField.setValue(true);
            else modVanishEnabledField.setValue(true);
        }
        if (pMatcher.find()) {
            isAdminVanish = false;
            modVanishEnabledField.setValue(true);
        }


        // disabled
        vMatcher = VANISH_DISABLED.matcher(text);
        pMatcher = VANISH_PREFERENCE_DISABLED.matcher(text);
        if (vMatcher.find()) {

            isAdminVanish = false;
            if (localIsAdminVanish) adminVanishEnabledField.setValue(false);
            else modVanishEnabledField.setValue(false);
        }
        if (pMatcher.find()) {
            isAdminVanish = false;
            modVanishEnabledField.setValue(true);
        }


        // modify vanish msgs
        if (customVanishMessage.getValue()) {
            boolean simplifiedTags = FeatureManager.getFeature(SimplifiedStaffChatTags.class).getEnabled();

            matcher = Pattern.compile("^» Vanish enabled\\. You will not be visible to other players\\.").matcher(text);
            if (matcher.find()) {
                return message.modified(Component.empty()
                        .append(
                                localIsAdminVanish ?
                                        (simplifiedTags ? SimplifiedStaffChatTags.ADMIN : Component.literal("[ADMIN]").withColor(ColorBank.DF_ADMIN)) :
                                        (simplifiedTags ? SimplifiedStaffChatTags.MOD : Component.literal("[MOD]").withColor(ColorBank.MC_DARK_GREEN))

                        )
                        .append(Component.literal(localIsAdminVanish ? " Admin Vanish Enabled" : " Mod Vanish Enabled").withColor(ColorBank.MC_GRAY))
                        .append(Component.literal(" ✔").withColor(ColorBank.MC_GREEN)));
            }

            matcher = Pattern.compile("^» Vanish disabled\\. You will now be visible to other players\\.").matcher(text);
            if (matcher.find()) {
                return message.modified(Component.empty()
                        .append(
                                localIsAdminVanish ?
                                        (simplifiedTags ? SimplifiedStaffChatTags.ADMIN : Component.literal("[ADMIN]").withColor(ColorBank.DF_ADMIN)) :
                                        (simplifiedTags ? SimplifiedStaffChatTags.MOD : Component.literal("[MOD]").withColor(ColorBank.MC_DARK_GREEN))

                        )
                        .append(Component.literal(localIsAdminVanish ? " Admin Vanish Disabled" : " Mod Vanish Disabled").withColor(ColorBank.MC_GRAY))
                        .append(Component.literal(" ❌").withColor(ColorBank.MC_RED)));
            }
        }

        return message.pass();
    }

    public boolean isInModVanish() { return modVanishEnabledField.getValue(); }
    public boolean isInAdminVanish() { return adminVanishEnabledField.getValue(); }
    public boolean isInYTVanish() { return ytVanishEnabledField.getValue(); }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {

    }


    @Override
    public void tickR(int tick) {
        Component mv = Component.literal("[MOD]").withColor(0x30CC20).append(
                Component.literal(" Mod Vanished").withColor(0x92FF87)
        );
        Component av = Component.literal("[ADMIN]").withColor(ColorBank.DF_ADMIN).append(
                Component.literal(" Admin Vanished").withColor(0xFF9B9B)
        );
        if ((modVanishEnabledField.getValue() && adminVanishEnabledField.getValue())) {
            Mod.hotbarMessage(Component.empty().append(mv).append(Component.literal(" + ").withColor(ColorBank.MC_GRAY)).append(av));
        } else {
            if (modVanishEnabledField.getValue()) {
                Mod.hotbarMessage(mv);
            }
            if (adminVanishEnabledField.getValue()) {
                Mod.hotbarMessage(av);
            }
        }

    }

    @Override
    public void tickF(int tick) {

    }
}
