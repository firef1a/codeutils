package mia.modmod.features.impl.general.chat;

import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.listeners.ModifiableEventData;
import mia.modmod.features.listeners.ModifiableEventResult;
import mia.modmod.features.listeners.impl.ChatEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.network.chat.Component.literal;

public final class SimplifiedStaffChatTags extends Feature implements ChatEventListener {
    public static final Component SUPPORT = getPrefix(0x55aaff);
    public static final Component SR_HELPER = getPrefix(0x7fffd4);
    public static final Component MOD = getPrefix(0x2ad42a);
    public static final Component ADMIN = getPrefix(0x2a70d4);

    private static Component getPrefix(int color) { return Component.empty().append(literal("›").withColor(color)); }

    public SimplifiedStaffChatTags(Categories category) {
        super(category, "Simple Staff Tags", "simple_staff_tags", "Simplifies staff chat tags.");
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        Component modified = message.modified();

        // regular stuff
        ArrayList<SCR> scrs = new ArrayList<>(List.of(
                new SCR(Pattern.compile("\\[SUPPORT] "), SUPPORT),
                new SCR(Pattern.compile("\\[MOD] "), MOD),
                new SCR(Pattern.compile("\\[ADMIN] "), ADMIN)
        ));

        for (SCR scr : scrs) modified = replaceTextNew(modified, scr.pattern(), scr.replacement());

        // session peek
        Matcher matcher;
        matcher = Pattern.compile("^\\* (\\[.*])*([a-zA-Z0-9_]{3,16}): (.*)").matcher(message.base().getString());
        if (matcher.find()) {
            String ranks = matcher.group(1);
            String name = matcher.group(2);
            String text = matcher.group(3);
            modified = Component.empty()
                    .append(SR_HELPER.copy().append(" "))
                    .append(Component.literal(name + ": ").withColor(0x2affaa))
                    .append(Component.literal(text).withColor(0x9cffde));
        }

        return message.modified(modified);
    }

    private record SCR(Pattern pattern, Component replacement) {
        @Override
        public Component replacement() {
            return withSpace(replacement);
        }
    };

    private static Component withSpace(Component text) { return text.copy().append(" "); }

    private static Component replaceTextNew(Component text, Pattern pattern, Component replace) {
        MutableComponent newText = MutableComponent.create(text.getContents()).setStyle(text.getStyle());
        List<Component> siblings = text.copy().getSiblings();

        if (pattern.matcher(newText.getString()).find()) newText = replace.copy();
        for (Component sibling : siblings) {
            newText.append(replaceTextNew(sibling, pattern, replace));
        }
        return newText;
    }

}
