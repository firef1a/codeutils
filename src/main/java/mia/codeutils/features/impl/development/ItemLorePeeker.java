package mia.codeutils.features.impl.development;

import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.ChatEventListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.render.util.DrawContextHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

public final class ItemLorePeeker extends Feature implements RenderHUD {
    public ItemLorePeeker(Categories category) {
        super(category, "Item Lore HUD", "lorehud", "Shows the lore of an item on your HUD.");
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        if (!LocationAPI.getMode().isDev()) return;
        if (Mod.MC.player == null) return;
        Inventory inventory = Mod.MC.player.getInventory();
        ItemStack selectedItem  = inventory.getSelectedItem();


        List<Component> lore = new ArrayList<>();
        ItemLore itemLore = selectedItem.getComponents().get(DataComponents.LORE);
        if (itemLore != null) lore.addAll(itemLore.lines());
        lore.addAll(ItemTagViewer.getItemLoreWithTags(selectedItem));

        int x = (context.guiWidth() / 2) + 91 - 4; // magic number <3
        if (!lore.isEmpty()) {
            lore.addFirst(selectedItem.getItemName());
            try {
                DrawContextHelper.drawTooltip(context, lore, FeatureManager.getFeature(CPUDisplay.class).cpuText.x1() - 7, FeatureManager.getFeature(CPUDisplay.class).isDisplayed() ? FeatureManager.getFeature(CPUDisplay.class).container.y2() + 2 : FeatureManager.getFeature(CPUDisplay.class).cpuText.y1(), 0f);
            } catch (Exception e) {
                Mod.error(e.getMessage());
            }
        }
    }
}
