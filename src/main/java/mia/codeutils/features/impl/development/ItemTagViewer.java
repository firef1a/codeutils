package mia.codeutils.features.impl.development;

import mia.codeutils.ColorBank;
import mia.codeutils.core.KeyBindCategories;
import mia.codeutils.core.KeyBindManager;
import mia.codeutils.core.MiaKeyBind;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.impl.RegisterKeyBindEvent;
import mia.codeutils.features.listeners.impl.RenderTooltip;
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
import java.util.*;
import java.util.List;

public final class ItemTagViewer extends Feature implements RenderTooltip, RegisterKeyBindEvent {
    private static ColorDataField regularKeyColor;
    private static ColorDataField stringValueColor;
    private static ColorDataField numberValueColor;

    //private static final int componentDataKeyColor = 0x9cffca;
    //private static final int cKeyColor = 0xaaaaff;

    private final MiaKeyBind showItemTags;

    private static final Component tagText = Component.literal("› ").withColor(ColorBank.MC_GRAY).append(Component.literal("Tags:").withColor(ColorBank.WHITE));
    private static final Component delimiterText = Component.literal(" : ").withColor(ColorBank.MC_DARK_GRAY);

    public static final DataComponentType<CustomData> CUSTOM_DATA_DATA_COMPONENT_TYPE = DataComponents.CUSTOM_DATA;

    public ItemTagViewer(Categories category) {
        super(category, "Item Tag Viewer", "itemtagviewer", "Shows hypercube item tags while in dev mode.");
        regularKeyColor = new ColorDataField("Tag Key Color", ParameterIdentifier.of(this, "tag_key_color"), new Color(0xeebdff), true);
        stringValueColor = new ColorDataField("String Value Color", ParameterIdentifier.of(this, "string_value_color"), new Color(0xbdd7ff), true);
        numberValueColor = new ColorDataField("Number Value Color", ParameterIdentifier.of(this, "number_value_color"), new Color(0xff5555), true);

        showItemTags = new MiaKeyBind("Show Hypercube Item Tag", GLFW.GLFW_KEY_LEFT_ALT, KeyBindCategories.DEVELOPMENT_CATEGORY);
    }


    @Override
    public void tooltip(ItemStack item, Item.TooltipContext context, TooltipFlag type, List<Component> textList) {
        //if (!LocationAPI.getMode().canViewCode()) return;
        item.getComponents().forEach(typedDataComponent -> {
            //Mod.message(typedDataComponent.toString());
        });

        CustomData data = item.getComponents().getOrDefault(CUSTOM_DATA_DATA_COMPONENT_TYPE, CustomData.EMPTY);
        Set<Map.Entry<String, Tag>> dataSet = data.copyTag().entrySet();

        Tag publicBukkitValues = null;
        for (Map.Entry<String, Tag> entry : dataSet) {
            if (entry.getKey().equals("PublicBukkitValues")) {
                publicBukkitValues = entry.getValue();
                break;
            }
        }

        ArrayList<Component> hypercubeTags = new ArrayList<>();
        if (publicBukkitValues != null) {
            Optional<CompoundTag> tag = publicBukkitValues.asCompound();
            if (tag.isPresent()) {
                for (Map.Entry<String, Tag> entry : tag.get().entrySet()) {
                    String rawKey = entry.getKey().substring(10); // chops off "hypercube:"
                    if (List.of("varitem", "item_instance").contains(rawKey)) continue;
                    Tag rawValue = entry.getValue();

                    Component valueEntry = null;
                    if (rawValue.asString().isPresent()) {
                        valueEntry = Component.literal(rawValue.asString().get()).withColor(stringValueColor.getRGB());
                    } else if (rawValue.asFloat().isPresent()) {
                        valueEntry = Component.literal(String.valueOf(rawValue.asFloat().get())).withColor(numberValueColor.getRGB());
                    }

                    if (valueEntry != null) {
                        hypercubeTags.add(
                                Component.literal(rawKey).withColor(regularKeyColor.getRGB())
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
        }
        if (!hypercubeTags.isEmpty()) {
            hypercubeTags.addFirst(tagText.copy());
            hypercubeTags.addFirst(Component.empty());
            textList.addAll(hypercubeTags);
        }
    }

    @Override
    public void registerKeyBind() {
        KeyBindManager.registerKeyBind(showItemTags);
    }
}

