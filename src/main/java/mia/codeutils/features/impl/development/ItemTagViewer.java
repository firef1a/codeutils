package mia.codeutils.features.impl.development;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.core.KeyBindCategories;
import mia.codeutils.core.KeyBindManager;
import mia.codeutils.core.MiaKeyBind;
import mia.codeutils.core.items.DFItem;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.impl.RegisterKeyBindEvent;
import mia.codeutils.features.listeners.impl.RenderTooltip;
import mia.codeutils.features.listeners.impl.TickEvent;
import mia.codeutils.features.parameters.ParameterIdentifier;
import mia.codeutils.features.parameters.impl.ColorDataField;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

public final class ItemTagViewer extends Feature implements RenderTooltip, RegisterKeyBindEvent, TickEvent {
    private static ColorDataField regularKeyColor;
    private static ColorDataField stringValueColor;
    private static ColorDataField numberValueColor;

    //private static final int componentDataKeyColor = 0x9cffca;
    //private static final int cKeyColor = 0xaaaaff;

    private final MiaKeyBind showItemTagsKeybind;
    private boolean showItemTags;

    private static final Component tagText = Component.literal("› ").withColor(ColorBank.MC_GRAY).append(Component.literal("Tags:").withColor(ColorBank.WHITE));
    private static final Component delimiterText = Component.literal(" : ").withColor(ColorBank.MC_DARK_GRAY);

    public ItemTagViewer(Categories category) {
        super(category, "Item Tag Viewer", "itemtagviewer", "Shows hypercube item tags while in dev mode.");
        regularKeyColor = new ColorDataField("Tag Key Color", ParameterIdentifier.of(this, "tag_key_color"), new Color(0xeebdff), true);
        stringValueColor = new ColorDataField("String Value Color", ParameterIdentifier.of(this, "string_value_color"), new Color(0xbdd7ff), true);
        numberValueColor = new ColorDataField("Number Value Color", ParameterIdentifier.of(this, "number_value_color"), new Color(0xff5555), true);

        showItemTagsKeybind = new MiaKeyBind("Show Hypercube Item Tag", GLFW.GLFW_KEY_LEFT_ALT, KeyBindCategories.DEVELOPMENT_CATEGORY, () -> {
            showItemTags = !showItemTags;
        });
    }

    public static List<Component> getItemLoreWithTags(ItemStack item) {
        //if (!LocationAPI.getMode().canViewCode()) return;
        Optional<HashMap<String,Tag>> hypercubeTags = new DFItem(item).getHypercubeItemTags(true);
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
        KeyBindManager.registerKeyBind(showItemTagsKeybind);
    }

    @Override
    public void tickR(int tick) {

    }

    @Override
    public void tickF(int tick) {
        showItemTagsKeybind.tick();
    }
}

