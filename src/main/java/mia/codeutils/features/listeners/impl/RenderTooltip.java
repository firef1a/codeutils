package mia.codeutils.features.listeners.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import mia.codeutils.features.listeners.AbstractEventListener;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public interface RenderTooltip extends AbstractEventListener {
    /*
        converts components into nbt compounds
     */
    static CompoundTag encodeStack(ItemStack stack, DynamicOps<Tag> ops) {
        DataResult<Tag> result = DataComponentPatch.CODEC.encodeStart(ops, stack.getComponentsPatch());
        Tag nbtElement = result.getOrThrow();
        return (CompoundTag) nbtElement;
    }

    void tooltip(ItemStack item, Item.TooltipContext context, TooltipFlag type, List<Component> textList);
}
