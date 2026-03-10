package mia.codeutils.core.items;

import mia.codeutils.Mod;
import mia.codeutils.core.Base64Utils;
import mia.codeutils.core.GzipUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents an item, with configurable data, lore, name, and more.
 */
public class DFItem {
    ItemStack item;
    ItemData data;

    /**
     * Creates a new DFItem from an ItemStack.
     *
     * @param item The item to create the DFItem from.
     */
    public DFItem(ItemStack item) {
        this.item = item;
        this.data = new ItemData(item);
    }

    /**
     * Creates a new DFItem from an ItemStack.
     *
     * @param item The item to create the DFItem from.
     * @return The new DFItem.
     */
    public static DFItem of(ItemStack item) {
        return new DFItem(item);
    }

    /**
     * Gets the item's data.
     *
     * @return The item's data.
     */
    public ItemData getItemData() {
        return data;
    }

    /**
     * Sets the item's data.
     *
     * @param itemData The new data to set.
     */
    public void setItemData(ItemData itemData) {
        data = itemData;
    }

    /**
     * Edits the item's data with a consumer, creates the data if it doesn't exist.
     * <br>
     * Example:
     * <pre>{@code
     * item.editData(data -> {
     *    data.setStringValue("key", "value");
     * });
     * }</pre>
     *
     * @param consumer The consumer to edit the data with.
     */
    public void editData(Consumer<ItemData> consumer) {
        if (!data.hasCustomData()) data = ItemData.getEmpty();
        consumer.accept(data);
    }

    /**
     * Delegates to {@link ItemData#getHypercubeStringValue(String)}.
     *
     * @param key The key to get, without the hypercube: prefix.
     * @return The value of the key, or an empty string if it doesn't exist.
     */
    public Optional<String> getHypercubeStringValue(String key) {
        var itemData = getItemData();
        if (itemData == null) return "".describeConstable();
        return itemData.getHypercubeStringValue(key);
    }

    /**
     * Delegates to {@link ItemData#hasHypercubeKey(String)}.
     *
     * @param key The key to check, without the hypercube: prefix.
     * @return Whether the key exists.
     */
    public boolean hasHypercubeKey(String key) {
        var itemData = getItemData();
        if (itemData == null) return false;
        return itemData.hasHypercubeKey(key);
    }

    /**
     * Converts the DFItem back into an ItemStack.
     *
     * @return The ItemStack.
     */
    public ItemStack getItemStack() {
        if (data != null) item.set(DataComponents.CUSTOM_DATA, getItemData().toComponent());
        return item;
    }

    /**
     * Delegates to {@link ItemData#getPublicBukkitValues()}.
     *
     * @return The PublicBukkitValues.
     */
    public PublicBukkitValues getPublicBukkitValues() {
        return getItemData().getPublicBukkitValues();
    }

    /**
     * Gets the lore of the item.
     *
     * @return The lore of the item.
     */
    public List<Component> getLore() {
        ItemLore loreComponent = item.get(DataComponents.LORE);
        if (loreComponent == null) return List.of();
        return loreComponent.lines();
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The new lore to set.
     */
    public void setLore(List<Component> lore) {
        item.set(DataComponents.LORE, new ItemLore(lore));
    }

    /**
     * Gets the name of the item.
     *
     * @return The name of the item.
     */
    public Component getName() {
        return item.getHoverName();
    }

    /**
     * Sets the name of the item.
     *
     * @param name The new name to set.
     */
    public void setName(Component name) {
        item.set(DataComponents.CUSTOM_NAME, name);
    }

    /**
     * Hides additional information about the item, such as additional tooltip, jukebox playable, fireworks, and attribute modifiers.
     */
    public void hideFlags() {
       // item.set(DataComponentTypes.TOOLTIP_DISPLAY, Unit.INSTANCE);
        item.remove(DataComponents.JUKEBOX_PLAYABLE);
        item.remove(DataComponents.FIREWORKS);
        item.remove(DataComponents.ATTRIBUTE_MODIFIERS);
    }

    /**
     * Sets the dye color of the item.
     *
     * @param color The new dye color to set.
     */
    public void setDyeColor(int color) {
        //item.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param modelData The new custom model data to set.
     */
    public void setCustomModelData(int modelData) {
        //item.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(modelData));
    }

    /**
     * Sets the profile of the item, for use with player heads.
     *
     * @param uuid      The UUID of the player.
     * @param value     The value of the profile.
     * @param signature The signature of the profile.
     */
    public void setProfile(UUID uuid, String value, String signature) {
       // PropertyMap map = new PropertyMap();
        //map.put("textures", new Property("textures", value, signature));
       // item.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.ofNullable(uuid), map));
    }

    /**
     * Removes the item's data.
     */
    public void removeItemData() {
        item.remove(DataComponents.CUSTOM_DATA);
        data = null;
    }


    // This method doesn't fit the theme of entire abstraction of item data, but its use case is very specific.

    /**
     * Gets the container of the item.
     *
     * @return The container of the item.
     */
    @Nullable
    public ItemContainerContents getContainer() {
        return item.get(DataComponents.CONTAINER);
    }

    public static ItemStack makeTemplate(String code) {
        ItemStack template = new ItemStack(Items.ENDER_CHEST);
        DFItem dfItem = DFItem.of(template);
        dfItem.editData(data -> data.setHypercubeStringValue("codetemplatedata", "{\"author\":\"" + Mod.MOD_ID +"\",\"name\":\"Template to be placed\",\"version\":1,\"code\":\"" + code + "\"}"));
        return dfItem.getItemStack();
    }


    public Optional<HashMap<String, Tag>> getHypercubeItemTags(boolean ignoreInternalTags) {
        HashMap<String, Tag> hypercubeTags = new HashMap<>();
        CustomData data = this.item.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        Set<Map.Entry<String, Tag>> dataSet = data.copyTag().entrySet();

        Tag publicBukkitValues = null;
        for (Map.Entry<String, Tag> entry : dataSet) {
            if (entry.getKey().equals("PublicBukkitValues")) {
                publicBukkitValues = entry.getValue();
                break;
            }
        }

        if (publicBukkitValues != null) {
            Optional<CompoundTag> tag = publicBukkitValues.asCompound();
            if (tag.isPresent()) {
                for (Map.Entry<String, Tag> entry : tag.get().entrySet()) {
                    String key = entry.getKey().substring(10); // chops off "hypercube:";
                    if (List.of("varitem", "item_instance", "codetemplatedata").contains(key) && ignoreInternalTags) continue;
                    hypercubeTags.put(key, entry.getValue());
                }
            }
            return Optional.of(hypercubeTags);
        }
        return Optional.empty();
    }

    public static String serializeTemplateJSON(String jsonData) throws IOException {
        return Base64Utils.encodeBase64Bytes(GzipUtils.compress(jsonData));
    }
}