package eu.macsworks.worldmanager.listeners;

import eu.macsworks.worldmanager.WorldManager;
import eu.macsworks.worldmanager.guis.internal.InteractResult;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.function.Consumer;

public class RegisteredButtonClickListener implements Listener {

    @Getter private static final NamespacedKey interactiveItemKey = new NamespacedKey(WorldManager.getInstance(), "macsitem");
    private static final HashMap<String, Consumer<InteractResult>> onClick = new HashMap<>();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if(item == null) return;
        if(item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if(meta == null) return;
        if(!meta.getPersistentDataContainer().has(interactiveItemKey)) return;

        String itemId = item.getItemMeta().getPersistentDataContainer().get(interactiveItemKey, PersistentDataType.STRING);
        event.setCancelled(true);

        if(itemId == null || !onClick.containsKey(itemId)) return;
        if(itemId.equalsIgnoreCase("nothing")) return;

        onClick.get(itemId).accept(new InteractResult(item, (Player) event.getWhoClicked(), event));
    }

    public static void addAction(String id, Consumer<InteractResult> onClick){
        RegisteredButtonClickListener.onClick.put(id, onClick);
    }
}
