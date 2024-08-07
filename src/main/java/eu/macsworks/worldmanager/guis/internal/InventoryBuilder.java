package eu.macsworks.worldmanager.guis.internal;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InventoryBuilder {

	private HashMap<Integer, ItemStack> items = new HashMap<>();
	private int slots = 27;
	private ItemStack filler;
	private String name = "MacsLibs Inventory";


	public static InventoryBuilder builder(){
		return new InventoryBuilder();
	}

	public InventoryBuilder slots(int slots){
		this.slots = slots;
		return this;
	}

	public InventoryBuilder filled(ItemStack filler){
		this.filler = filler.clone();
		return this;
	}

	public InventoryBuilder named(String name){
		this.name = name;
		return this;
	}

	public InventoryBuilder addItem(int slot, ItemStack item){
		items.put(slot, item);
		return this;
	}

	public InventoryBuilder setItems(HashMap<Integer, ItemStack> items){
		this.items = items;
		return this;
	}

	//Here below some utility classes for inventory creation
	public Inventory build() {
		Inventory inv = Bukkit.createInventory(null, slots, name);

		if(filler != null){
			for(int i = 0; i < slots; i++) {
				inv.setItem(i, filler.clone());
			}
		}

		items.keySet().forEach(i -> {
			inv.setItem(i, items.get(i).clone());
		});

		return inv;
	}


}
