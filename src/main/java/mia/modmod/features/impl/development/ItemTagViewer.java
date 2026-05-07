package mia.modmod.features.impl.development;

import mia.modmod.ColorBank;
import mia.modmod.Mod;
import mia.modmod.core.items.MDFItem;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.impl.internal.mode.LocationAPI;
import mia.modmod.features.listeners.impl.RegisterKeyBindEvent;
import mia.modmod.features.listeners.impl.RenderTooltip;
import mia.modmod.features.listeners.impl.TickEvent;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.BooleanDataField;
import mia.modmod.features.parameters.impl.ColorDataField;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class ItemTagViewer extends Feature implements RenderTooltip, RegisterKeyBindEvent, TickEvent {
    private static BooleanDataField devRestricted;
    private static BooleanDataField requireCTRL;
    private static ColorDataField regularKeyColor;
    private static ColorDataField stringValueColor;
    private static ColorDataField numberValueColor;

    private static final Component tagText = Component.literal("› ").withColor(ColorBank.MC_GRAY).append(Component.literal("Tags:").withColor(ColorBank.WHITE));
    private static final Component delimiterText = Component.literal(" : ").withColor(ColorBank.MC_DARK_GRAY);

    public ItemTagViewer(Categories category) {
        super(category, "Item Tag Viewer", "itemtagviewer", "Shows hypercube item tags while in dev mode.");
        devRestricted = new BooleanDataField("Dev-mode Restricted", "Only shows item tags in dev mode", ParameterIdentifier.of(this, "dev_mode_restricted"), true, true);
        requireCTRL = new BooleanDataField("Require CTRL", "Only shows item tags if CTRL is pressed", ParameterIdentifier.of(this, "ctrl_required"), false, true);
        regularKeyColor = new ColorDataField("Tag Key Color", "", ParameterIdentifier.of(this, "tag_key_color"), new Color(0xeebdff), true);
        stringValueColor = new ColorDataField("String Value Color", "", ParameterIdentifier.of(this, "string_value_color"), new Color(0xbdd7ff), true);
        numberValueColor = new ColorDataField("Number Value Color", "", ParameterIdentifier.of(this, "number_value_color"), new Color(0xff5555), true);

    }

    public static List<Component> getItemLoreWithTags(ItemStack item) {
        if (!LocationAPI.getMode().canViewCode()) {
            if (devRestricted.getValue()) return List.of();
        }
        if (!Mod.MC.hasControlDown() && requireCTRL.getValue()) return List.of();

        Optional<HashMap<String,Tag>> hypercubeTags = new MDFItem(item).getHypercubeItemTags(true);
        ArrayList<Component> loreLines = new ArrayList<>();

        if (hypercubeTags.isPresent()) {
            for (Map.Entry<String, Tag> entry : hypercubeTags.get().entrySet()) {
                String key = entry.getKey();
                if (List.of("varitem", "item_instance", "codetemplatedata").contains(key)) continue;
                Tag rawValue = entry.getValue();

                Component valueEntry = null;
                if (rawValue.asString().isPresent()) {
                    valueEntry = Component.literal(rawValue.asString().get()).withColor(stringValueColor.getRGB());
                } else if (rawValue.asFloat().isPresent()) {
                    valueEntry = Component.literal(String.valueOf(rawValue.asFloat().get())).withColor(numberValueColor.getRGB());
                }

                if (valueEntry != null) {
                    loreLines.add(
                            Component.literal(key).withColor(regularKeyColor.getRGB())
                                    .append(
                                            delimiterText.copy().
                                                    append(
                                                            valueEntry
                                                    )
                                    )
                    );
                }
            }
        }

        if (!loreLines.isEmpty()) {
            loreLines.addFirst(tagText.copy());
            loreLines.addFirst(Component.empty());
        }
        return loreLines;
    }


    @Override
    public void tooltip(ItemStack item, Item.TooltipContext context, TooltipFlag type, List<Component> textList) {
        List<Component> tagLore = getItemLoreWithTags(item);
        textList.addAll(tagLore);
    }



    @Override
    public void registerKeyBind() {
        //KeyBindManager.registerKeyBind(showItemTagsKeybind);
    }

    @Override
    public void tickR(int tick) {

    }

    @Override
    public void tickF(int tick) {
        //showItemTagsKeybind.tick();
    }
}

