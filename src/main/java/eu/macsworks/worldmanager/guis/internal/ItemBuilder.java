package eu.macsworks.worldmanager.guis.internal;

import eu.macsworks.worldmanager.WorldManager;
import eu.macsworks.worldmanager.listeners.RegisteredButtonClickListener;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemBuilder {

	//GLOBAL FIELDS
	@Getter private static final NamespacedKey dynamicItemKeyName = new NamespacedKey(WorldManager.getInstance(), "dynamic_item_name");
	@Getter private static final NamespacedKey dynamicItemKeyLore = new NamespacedKey(WorldManager.getInstance(), "dynamic_item_lore");

	//Per-Instance fields
	private ItemStack item;

	public static ItemBuilder builder(){
		return new ItemBuilder();
	}

	public ItemBuilder item(ItemStack item){
		this.item = item;
		return this;
	}

	public ItemBuilder material(Material mat){
		checkItem();
		item.setType(mat);
		return this;
	}

	public ItemBuilder customModelData(int cmd){
		checkItem();
		ItemMeta meta = getMeta();
		meta.setCustomModelData(cmd);
		item.setItemMeta(meta);
		return this;
	}

	public ItemBuilder name(String name){
		checkItem();
		ItemMeta meta = getMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return this;
	}

	public ItemBuilder dynamicName(Supplier<String> predicate){
		checkItem();
		ItemMeta meta = getMeta();
		meta.setDisplayName(predicate.get());
		String id = UUID.randomUUID().toString();
		WorldManager.getInstance().getItemUpdater().addSupplier(id, predicate);
		return withPDCTag(dynamicItemKeyName, id);
	}

	public ItemBuilder dynamicLore(Supplier<String> predicate){
		checkItem();
		ItemMeta meta = getMeta();
		meta.setLore(Arrays.asList(predicate.get().split("\n")));
		String id = UUID.randomUUID().toString();
		WorldManager.getInstance().getItemUpdater().addSupplier(id, predicate);
		return withPDCTag(dynamicItemKeyLore, id);
	}

	public ItemBuilder lore(String name){
		checkItem();
		ItemMeta meta = getMeta();
		meta.setLore(Arrays.asList(name.split("\n")));
		item.setItemMeta(meta);
		return this;
	}

	public ItemBuilder interactive(Consumer<InteractResult> onInteract){
		checkItem();
		String id = UUID.randomUUID().toString();
		RegisteredButtonClickListener.addAction(id, onInteract);
		return withPDCTag(RegisteredButtonClickListener.getInteractiveItemKey(), id);
	}

	public ItemBuilder makeStatic(){
		checkItem();
		return withPDCTag(RegisteredButtonClickListener.getInteractiveItemKey(), "nothing");
	}

	public ItemBuilder withPDCTag(NamespacedKey key, String value){
		checkItem();
		ItemMeta meta = getMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
		item.setItemMeta(meta);
		return this;
	}

	public ItemStack build(){
		checkItem();
		return item.clone();
	}

	private void checkItem(){
		if(item == null){
			item = new ItemStack(Material.STICK, 1);
		}
	}

	private ItemMeta getMeta(){
		return item.getItemMeta();
	}

}
