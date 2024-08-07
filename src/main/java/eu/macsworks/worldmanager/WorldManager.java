/**
 * WorldManager developed by Alice in 2024
 **/

package eu.macsworks.worldmanager;

import eu.macsworks.worldmanager.commands.ManageWorldsCommand;
import eu.macsworks.worldmanager.guis.internal.ItemUpdater;
import eu.macsworks.worldmanager.listeners.RegisteredButtonClickListener;
import eu.macsworks.worldmanager.listeners.WorldLoadListener;
import eu.macsworks.worldmanager.objects.RegisteredWorld;
import eu.macsworks.worldmanager.utils.PluginLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class WorldManager extends JavaPlugin {

	private static WorldManager instance = null;

	public static WorldManager getInstance() {
		return WorldManager.instance;
	}

	private static void setInstance(WorldManager in) {
		WorldManager.instance = in;
	}

	@Getter
	private PluginLoader macsPluginLoader;

	@Getter
	private ItemUpdater itemUpdater;

	@Override
	public void onEnable() {
		setInstance(this);

		macsPluginLoader = new PluginLoader();
		macsPluginLoader.load();

		itemUpdater = new ItemUpdater();

		loadTasks();
		loadEvents();
		loadCommands();

		Bukkit.getLogger().info("--------------------------------------");
		Bukkit.getLogger().info("WorldManager was loaded successfully!");
		Bukkit.getLogger().info("--------------------------------------");
	}

	private void loadTasks() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			itemUpdater.tick();
		}, 0L, 10L);

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			macsPluginLoader.getAllWorlds().forEach(RegisteredWorld::checkUsage);
		}, 0L, 100L);
	}

	private void loadEvents() {
		Bukkit.getPluginManager().registerEvents(new RegisteredButtonClickListener(), this);
		Bukkit.getPluginManager().registerEvents(new WorldLoadListener(), this);
	}

	private void loadCommands() {
		Objects.requireNonNull(getCommand("manageworlds")).setExecutor(new ManageWorldsCommand());
	}

	@Override
	public void onDisable() {
		macsPluginLoader.save();

		Bukkit.getLogger().info("--------------------------------------");
		Bukkit.getLogger().info("WorldManager was unloaded successfully!");
		Bukkit.getLogger().info("--------------------------------------");
	}
}
