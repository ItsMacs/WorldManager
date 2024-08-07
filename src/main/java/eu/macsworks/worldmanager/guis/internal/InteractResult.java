package eu.macsworks.worldmanager.guis.internal;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
public class InteractResult {

    private final ItemStack item;
    private final Player clicker;
    private final InventoryClickEvent event;

    public InteractResult(ItemStack item, Player clicker, InventoryClickEvent event){
        this.item = item;
        this.clicker = clicker;
        this.event = event;
    }

}
