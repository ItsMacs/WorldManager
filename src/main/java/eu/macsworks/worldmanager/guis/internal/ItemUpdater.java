package eu.macsworks.worldmanager.guis.internal;

import eu.macsworks.worldmanager.utils.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemUpdater {

	private final Map<String, Supplier<String>> metaSuppliers = new HashMap<>();

	public void tick(){
		for(Player player : Bukkit.getOnlinePlayers()){
			InventoryView openInventory = player.getOpenInventory();
			if(openInventory.getTopInventory() == player.getInventory()) continue;
			Inventory inventory = openInventory.getTopInventory();

			for(ItemStack item : inventory.getContents()){
				if(item == null) continue;
				if(item.getType() == Material.AIR) continue;

				ItemMeta meta = item.getItemMeta();
				if(meta == null) continue;

				if(meta.getPersistentDataContainer().has(ItemBuilder.getDynamicItemKeyName())){
					Supplier<String> supplier = metaSuppliers.get(meta.getPersistentDataContainer().get(ItemBuilder.getDynamicItemKeyName(), PersistentDataType.STRING));
					if(supplier != null){
						meta.setDisplayName(ColorTranslator.translate(supplier.get()));
					}
				}

				if(meta.getPersistentDataContainer().has(ItemBuilder.getDynamicItemKeyLore())){
					Supplier<String> supplier = metaSuppliers.get(meta.getPersistentDataContainer().get(ItemBuilder.getDynamicItemKeyLore(), PersistentDataType.STRING));
					if(supplier != null){
						meta.setLore(Arrays.asList(ColorTranslator.translate(supplier.get()).split("\n")));
					}
				}

				item.setItemMeta(meta);
			}
		}
	}

	public void addSupplier(String uuid, Supplier<String> supplier) {
		metaSuppliers.put(uuid, supplier);
	}

}
