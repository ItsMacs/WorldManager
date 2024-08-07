package eu.macsworks.worldmanager.listeners;

import eu.macsworks.worldmanager.WorldManager;
import eu.macsworks.worldmanager.objects.RegisteredWorld;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {

	@EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void worldInit(WorldInitEvent event) {
		World world = event.getWorld();

		for(RegisteredWorld regWorld : WorldManager.getInstance().getMacsPluginLoader().getAllWorlds()){
			if(regWorld.getName().equals(world.getName())){
				world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
				return;
			}
		}
	}

}
