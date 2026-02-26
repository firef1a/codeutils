package mia.codeutils.core;

import mia.codeutils.Mod;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

public class InventoryHelper {
    public static void setHandItem(ItemStack item) {
        if(Mod.MC.getConnection() == null || Mod.MC.player == null) return;
        NetworkManager.sendPacket(new ServerboundSetCreativeModeSlotPacket(36 + Mod.MC.player.getInventory().getSelectedSlot(), item));
    }
    public static void setSlotItem(int slot, ItemStack item) {
        if(Mod.MC.getConnection() == null || Mod.MC.player == null) return;
        NetworkManager.sendPacket(new ServerboundSetCreativeModeSlotPacket(slot, item));
        updateClientInventoryScreen(slot, item);
    }
    public static void updateClientInventoryScreen(int slot, ItemStack item) {
        Mod.MC.player.inventoryMenu.setItem(slot, 0, item);
    }
}
