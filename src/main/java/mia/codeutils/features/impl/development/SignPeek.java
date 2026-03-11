package mia.codeutils.features.impl.development;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.core.KeyBindCategories;
import mia.codeutils.core.MiaKeyBind;
import mia.codeutils.core.items.DFItem;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.impl.PacketListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.features.listeners.impl.TickEvent;
import mia.codeutils.render.util.DrawContextHelper;
import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SignPeek extends Feature implements RenderHUD, TickEvent, PacketListener {
    public final MiaKeyBind getSignName;

    private static final List<String> grabbableFunctionNamesHeaders = List.of(
            "FUNCTION",
            "PROCESS",
            "CALL FUNCTION",
            "CALL PROCESS"
    );

    public SignPeek(Categories category) {
        super( category, "Sign Peek", "signpeek", "sign that ur gay");
        getSignName = new MiaKeyBind("Get Sign Name", GLFW.GLFW_KEY_F, KeyBindCategories.DEVELOPMENT_CATEGORY, () -> {
            if (!LocationAPI.getMode().canViewCode()) return;
            if (Mod.MC.player == null) return;
            if (Mod.MC.level == null) return;

            HitResult hitResult = Mod.MC.player.pick(4.5, 0, false);
            if (hitResult instanceof BlockHitResult blockHitResult) {
                BlockPos blockPos = blockHitResult.getBlockPos();

                BlockEntity blockState = Mod.MC.level.getBlockEntity(blockPos);
                if (blockState instanceof SignBlockEntity signBlockEntity) {
                    ArrayList<String> messages = new ArrayList<>(Arrays.stream(signBlockEntity.getFrontText().getMessages(true)).map(Component::getString).toList());
                    ArrayList<String> validHeaders = new ArrayList<>(grabbableFunctionNamesHeaders);

                    String header = messages.get(0);
                    String body = messages.get(1);
                    if (validHeaders.contains(header)) {
                        if (!body.isEmpty()) {
                            Mod.message(Component.literal("Created ").append(Component.literal("[STR]: ").withColor(ColorBank.MC_AQUA)).append(Component.literal(body).withColor(ColorBank.WHITE)));
                            String stringValue = body;
                            ItemStack item = new ItemStack(Items.STRING);
                            CompoundTag bukkit = new CompoundTag();
                            CompoundTag hypercube = new CompoundTag();

                            hypercube.putString("hypercube:varitem", "{\"id\":\"txt\",\"data\":{\"name\":\"" + stringValue +"\"}}");
                            bukkit.put("PublicBukkitValues", hypercube);
                            item.applyComponents(DataComponentMap.builder()
                                    .set(DataComponents.CUSTOM_NAME, Component.literal(stringValue).withStyle(style -> style.withItalic(false)))
                                    .set(DataComponents.CUSTOM_DATA, CustomData.of(bukkit))
                                            .set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(5000.0f), List.of(), List.of(), List.of()))
                                        .build());
                            Inventory inventory = Mod.MC.player.getInventory();
                            int selectedSlot = inventory.getSelectedSlot();
                            if (inventory.getFreeSlot() > 8) {
                                inventory.setItem(selectedSlot, item);
                            } else {
                                inventory.add(item);
                            }
                            Mod.message("" + inventory.getSelectedSlot());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void tickR(int tick) {

    }

    @Override
    public void tickF(int tick) {
        if (getSignName.down && !getSignName.isDown()) getSignName.down = false;
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        if (!LocationAPI.getMode().canViewCode()) return;
        if (Mod.MC.player == null) return;
        if (Mod.MC.level == null) return;

        HitResult hitResult = Mod.MC.player.pick(4.5, 0, false);
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();

            BlockEntity blockState = Mod.MC.level.getBlockEntity(blockPos);
            if (blockState instanceof SignBlockEntity signBlockEntity) {
                ArrayList<String> messages = new ArrayList<>(Arrays.stream(signBlockEntity.getFrontText().getMessages(true)).map(Component::getString).toList());
                ArrayList<String> validHeaders = new ArrayList<>(List.of("PLAYER EVENT", "ENTITY EVENT", "GAME EVENT", "FUNCTION", "CALL FUNCTION", "START PROCESS", "PROCESS"));

                String header = messages.get(0);
                String body = messages.get(1);
                if (validHeaders.contains(header)) {
                    if (!body.isEmpty()) {
                        DrawContextHelper.drawTooltip(
                                context,
                                List.of(
                                        Component.literal(header.strip()).withColor(ColorBank.MC_GRAY),
                                        Component.literal(body).withColor(0xd6d6d6)
                                ),
                                Mod.getScaledWindowWidth() / 2,
                                Mod.getScaledWindowHeight() / 2,
                                0.5f
                        );
                    }
                }
            }
        }
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {

    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundPlayerActionPacket serverboundPlayerActionPacket) {
            if (serverboundPlayerActionPacket.getAction().equals(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND)) {
                if (getSignName.rawIsDown()) ci.cancel();
            }
        }
    }
}
