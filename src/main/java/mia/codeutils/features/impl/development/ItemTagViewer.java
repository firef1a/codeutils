package mia.codeutils.features.impl.development;

import mia.codeutils.ColorBank;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.impl.RenderTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

// This code is ancient and should prob be recoded for the new component system at some point
// I just copied in some inputs from another mod to make it work with components

public final class ItemTagViewer extends Feature implements RenderTooltip {
    private static final int regularKeyColor = 0xf0c2ff;
    private static final int componentDataKeyColor = 0x9cffca;
    private static final int stringValueColor = 0xc2daff;
    private static final int numberValueColor = 0xff5555;
    private static final int cKeyColor = 0xaaaaff;

    private static final Component delimiterText = Component.literal(" : ").withColor(ColorBank.MC_DARK_GRAY);

    public ItemTagViewer(Categories category) {
        super(category, "Item Tag Viewer", "itemtagviewer", "Press shift to show item tags, ctrl to enable verbose mode.");
    }

    public static void onInjectTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> list) {
        handleClipboardCopy(stack);
        if (ModConfig.INSTANCE.triggerType.shouldShowTooltip(context, type)) {
            if (autoscroll_locks > 0) autoscroll_locks = 2;
            int lines = ModConfig.INSTANCE.maxLinesShown;
            if (ModConfig.INSTANCE.ctrlSuppressesRest && Screen.hasControlDown()) {
                lines += list.size();
                list.clear();
            } else {
                list.add(Text.literal(""));
            }

            ArrayList<Text> ttip = new ArrayList<>(lines);
            NbtCompound tag = encodeStack(stack, context.getRegistryLookup().getOps(NbtOps.INSTANCE));
            if (!tag.isEmpty()) {
                if (ModConfig.INSTANCE.showDelimiters) {
                    ttip.add(Text.literal(Formatting.DARK_PURPLE + " - nbt start -"));
                }
                if (ModConfig.INSTANCE.compress) {
                    ttip.add(Text.literal(FORMAT + tag));
                } else {
                    getRenderingEngine().parseTagToList(ttip, tag, ModConfig.INSTANCE.splitLongLines);
                }
                if (ModConfig.INSTANCE.showDelimiters) {
                    ttip.add(Text.literal(Formatting.DARK_PURPLE + " - nbt end -"));
                }
                ttip = NBTTooltip.transformTtip(ttip, lines);
                list.addAll(ttip);
            } else {
                list.add(Text.literal(FORMAT + "No NBT data"));
            }
        }
    }

    private static NbtCo encodeStack(ItemStack stack, DynamicOps<NbtElement> ops) {
        DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(ops, stack.getComponentChanges());
        result.ifError(e->{

        });
        NbtElement nbtElement = result.getOrThrow();
        // cast here, as soon as this breaks, the mod will need to update anyway
        return (NbtCompound) nbtElement;
    }


    @Override
    public void tooltip(ItemStack item, Item.TooltipContext context, TooltipFlag type, List<Component> textList) {

    }
}

