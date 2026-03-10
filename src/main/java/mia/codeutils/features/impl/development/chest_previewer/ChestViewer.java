package mia.codeutils.features.impl.development.chest_previewer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mia.codeutils.Mod;
import mia.codeutils.core.InventoryHelper;
import mia.codeutils.core.NetworkManager;
import mia.codeutils.core.items.DFItem;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.DFMode;
import mia.codeutils.features.listeners.impl.PacketListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.render.util.DrawContextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ChestViewer extends Feature implements RenderHUD, PacketListener {
    private BlockPos lastBlockPos;
    private BlockPos currentChestPos;
    private boolean expectingChestData;
    private List<Component> overlayTextList;
    private int removedSlot;
    private ItemStack removedItem;

    public ChestViewer(Categories category) {
        super(category, "Chest Contents Viewer", "chest_contents_viewer", "Shows a preview of code chest arguments.");
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (!LocationAPI.getMode().equals(DFMode.DEV)) return;
        if (Mod.MC.player == null) return;

        if (packet instanceof ClientboundContainerSetSlotPacket slot && expectingChestData) {
            Mod.MC.execute(() -> {
                DFItem item = DFItem.of(slot.getItem());
                ItemContainerContents container = item.getContainer();
                if (container == null) return;
                ArrayList<ItemStack> items = new ArrayList<>();
                container.nonEmptyItems().forEach(items::add);
                overlayTextList = getOverlayText(items);

                InventoryHelper.setSlotItem(removedSlot, removedItem);

                expectingChestData = false;
                ci.cancel();
            });
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        if (!LocationAPI.getMode().equals(DFMode.DEV)) return;
        if (Mod.MC.player == null) return;
        if (Mod.MC.level == null) return;

        if (Mod.MC.hitResult instanceof BlockHitResult hitResult) {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockEntity blockState = Mod.MC.level.getBlockEntity(blockPos);

            if (blockState instanceof ChestBlockEntity) {
                if (currentChestPos != null && currentChestPos.equals(blockPos) && overlayTextList != null) {
                    DrawContextHelper.drawTooltip(context, overlayTextList, Mod.getScaledWindowWidth() / 2, Mod.getScaledWindowHeight() / 2, 0.5f);
                }

                if ((currentChestPos != null && !lastBlockPos.equals(blockPos) && !currentChestPos.equals(blockPos)) || lastBlockPos == null) {
                    currentChestPos = blockPos;

                    removedSlot = 36 + Mod.MC.player.getInventory().getSelectedSlot();

                    removedItem = Mod.MC.player.getMainHandItem();

                    InventoryHelper.setSlotItem(removedSlot, ItemStack.EMPTY);
                    NetworkManager.sendPacket(new ServerboundPickItemFromBlockPacket(blockPos, true));
                    InventoryHelper.setSlotItem(removedSlot, removedItem);

                    overlayTextList = null;
                    expectingChestData = true;
                }

                lastBlockPos = blockPos;
            }


        }
    }

    public List<Component> getOverlayText(List<ItemStack> items) {
        ArrayList<Component> texts = new ArrayList<>();
        if (items.isEmpty()) {
            texts.add(Component.literal("No Arguments").withStyle(ChatFormatting.GOLD));
        } else {
            texts.add(Component.literal(items.size() + " Argument" + ((items.size() > 1) ? "s" : "")).withStyle(ChatFormatting.GOLD));
            for (ItemStack item : items) {
                DFItem dfItem = DFItem.of(item);
                List<Component> currentLore = dfItem.getLore();
                ArrayList<Component> lore = new ArrayList<>(currentLore);


                MutableComponent text = Component.empty();
                text.append(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY));
                Optional<String> varItem = dfItem.getHypercubeStringValue("varitem");
                if (varItem.isEmpty()) {
                    text.append(item.getCount() + "x ");
                    text.append(item.getHoverName());
                } else {
                    JsonObject object = JsonParser.parseString(varItem.get()).getAsJsonObject();
                    try {
                        Type type = Type.valueOf(object.get("id").getAsString());
                        JsonObject data = object.get("data").getAsJsonObject();
                        text.append(Component.literal(type.name.toUpperCase()).withStyle(Style.EMPTY.withColor(type.color)).append(" "));
                        if (type == Type.var) {
                            Scope scope = Scope.valueOf(data.get("scope").getAsString());
                            text.append(scope.getShortName()).withStyle(Style.EMPTY.withColor(scope.color)).append(" ");
                        }
                        if (type == Type.num || type == Type.txt || type == Type.comp || type == Type.var || type == Type.g_val || type == Type.pn_el) {
                            text.append(item.getHoverName());
                        }
                        if (type == Type.loc) {
                            JsonObject loc = data.get("loc").getAsJsonObject();
                            text.append("[%.2f, %.2f, %.2f, %.2f, %.2f]".formatted(
                                    loc.get("x").getAsFloat(),
                                    loc.get("y").getAsFloat(),
                                    loc.get("z").getAsFloat(),
                                    loc.get("pitch").getAsFloat(),
                                    loc.get("yaw").getAsFloat()));
                        }
                        if (type == Type.vec) {
                            text.append(Component.literal("<%.2f, %.2f, %.2f>".formatted(
                                    data.get("x").getAsFloat(),
                                    data.get("y").getAsFloat(),
                                    data.get("z").getAsFloat())
                            ).withStyle(Style.EMPTY.withColor(Type.vec.color)));
                        }
                        if (type == Type.snd) {
                            text.append(lore.getFirst());
                            text.append(Component.literal(" P: ").withStyle(ChatFormatting.GRAY));
                            text.append(Component.literal("%.1f".formatted(data.get("pitch").getAsFloat())));
                            text.append(Component.literal(" V: ").withStyle(ChatFormatting.GRAY));
                            text.append(Component.literal("%.1f".formatted(data.get("vol").getAsFloat())));
                        }
                        if (type == Type.part) {
                            text.append(Component.literal("%dx ".formatted(data.get("cluster").getAsJsonObject().get("amount").getAsInt())));
                            text.append(lore.getFirst());
                        }
                        if (type == Type.pot) {
                            text.append(lore.getFirst());
                            text.append(Component.literal(" %d ".formatted(data.get("amp").getAsInt() + 1)));
                            int dur = data.get("dur").getAsInt();
                            text.append(dur >= 1000000 ? "Infinite" : dur % 20 == 0 ? "%d:%02d".formatted((dur / 1200), (dur / 20) % 60) : (dur + "ticks"));
                        }
                        if (type == Type.bl_tag) {
                            text.append(Component.literal(data.get("tag").getAsString()).withStyle(ChatFormatting.YELLOW));
                            text.append(Component.literal(" » ").withStyle(ChatFormatting.DARK_AQUA));
                            text.append(Component.literal(data.get("option").getAsString()).withStyle(ChatFormatting.AQUA));
                        }
                        if (type == Type.hint) continue;
                    } catch (IllegalArgumentException ignored) {
                        text.append(Component.literal(object.get("id").getAsString().toUpperCase())
                                .withStyle(style -> style.withColor(TextColor.fromRgb(0x808080)))
                                .append(" "));
                        text.append(item.getHoverName());
                    }
                }
                texts.add(text);
            }
        }
        return texts;
    }

    enum Type {
        txt("str", ChatFormatting.AQUA),
        comp("txt", TextColor.fromRgb(0x7fd42a)),
        num("num", ChatFormatting.RED),
        loc("loc", ChatFormatting.GREEN),
        vec("vec", TextColor.fromRgb(0x2affaa)),
        snd("snd", ChatFormatting.BLUE),
        part("par", TextColor.fromRgb(0xaa55ff)),
        pot("pot", TextColor.fromRgb(0xff557f)),
        var("var", ChatFormatting.YELLOW),
        g_val("val", TextColor.fromRgb(0xffd47f)),
        pn_el("param", TextColor.fromRgb(0xaaffaa)),
        bl_tag("tag", ChatFormatting.YELLOW),
        hint("hint", TextColor.fromRgb(0xaaff55));

        public final String name;
        public final TextColor color;

        Type(String name, TextColor color) {
            this.name = name;
            this.color = color;
        }

        Type(String name, ChatFormatting color) {
            this.name = name;
            this.color = TextColor.fromLegacyFormat(color);
        }
    }

    enum Scope {
        unsaved(TextColor.fromLegacyFormat(ChatFormatting.GRAY), "GAME", "G", "g"),
        local(TextColor.fromLegacyFormat(ChatFormatting.GREEN), "LOCAL", "L", "l"),
        saved(TextColor.fromLegacyFormat(ChatFormatting.YELLOW), "SAVE", "S", "s"),
        line(TextColor.fromRgb(0x55aaff), "LINE", "I", "i");

        public final TextColor color;
        public final String longName;
        public final String shortName;
        public final String tag;

        Scope(TextColor color, String longName, String shortName, String tag) {
            this.color = color;
            this.longName = longName;
            this.shortName = shortName;
            this.tag = tag;
        }

        public String getShortName() {
            return shortName;
        }
    }
}
