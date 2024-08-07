package eu.macsworks.worldmanager.utils;

import eu.macsworks.worldmanager.WorldManager;
import eu.macsworks.worldmanager.objects.QueryFilter;
import eu.macsworks.worldmanager.objects.RegisteredWorld;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

@Getter
public class PluginLoader {

	@Getter(AccessLevel.NONE)
	private final Map<String, String> lang = new HashMap<>();

	@Getter(AccessLevel.NONE)
	private final Map<String, RegisteredWorld> worlds = new HashMap<>();

	private List<Material> worldIcons;

	private long worldUsageThreshold;
	private File loadableWorldsFolder;

	public void load() {
		//Check if the config.yml file exists, if not create it
		File configFile = new File(WorldManager.getInstance().getDataFolder() + "/config.yml");
		if (!configFile.exists()) WorldManager.getInstance().saveResource("config.yml", false);

		//Load config file & lang section, whilst translating it for HEX and Legacy colors
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		config.getConfigurationSection("lang").getKeys(false).forEach(s -> lang.put(s, ColorTranslator.translate(config.getString("lang." + s))));

		loadableWorldsFolder = new File(config.getString("unloaded-worlds-folder"));
		worldUsageThreshold = config.getLong("world-usage-threshold");

		//Convert the world icons string list into an immutable Material list
		worldIcons = config.getStringList("world-materials").stream().map(Material::getMaterial).toList();

		File storageFile = new File(WorldManager.getInstance().getDataFolder() + "/storage.yml");
		if (!storageFile.exists()) return;
		YamlConfiguration storage = YamlConfiguration.loadConfiguration(storageFile);

		//Load saved RegisteredWorlds
		storage.getKeys(false).forEach(s -> {
			RegisteredWorld world = new RegisteredWorld(s);
			world.load(storage.getConfigurationSection(s));
			worlds.put(s, world);
		});
	}

	public String getLang(String key) {
		if (!lang.containsKey(key)) return key + " not present in config.yml. Add it under lang!";
		return lang.get(key);
	}

	public Optional<RegisteredWorld> getWorld(String name){
		return Optional.ofNullable(worlds.get(name));
	}

	public void addWorld(RegisteredWorld world){
		worlds.put(world.getName(), world);
	}

	public boolean importWorld(String worldName, String creator){
		File worldFolder = new File(loadableWorldsFolder + "/" + worldName);
		if(!worldFolder.exists() || !worldFolder.isDirectory()) return false;

		RegisteredWorld world = new RegisteredWorld(worldName, creator, ZonedDateTime.now().toEpochSecond());
		addWorld(world);
		return true;
	}

	/**
	 * Returns a list of the worlds matching the QueryFilter and name query
	 * @param query String worlds will have to start with to be selected
	 * @param loaded Loading state of the world
	 * @return
	 */
	public List<RegisteredWorld> searchWorlds(String query, QueryFilter loaded){
		if(query.isEmpty() && loaded == QueryFilter.UNFILTERED) return getAllWorlds();

		List<RegisteredWorld> queryResult = new ArrayList<>(worlds.values().stream().filter(s -> s.getName().toLowerCase().startsWith(query.toLowerCase())).toList());
		if (loaded == null) return queryResult;

		if(loaded != QueryFilter.UNFILTERED) queryResult.removeIf(w -> w.isLoaded() != (loaded == QueryFilter.LOADED));
		return queryResult;
	}

	/**
	 * Returns a list of all RegisteredWorlds
	 * @return
	 */
	public List<RegisteredWorld> getAllWorlds(){
		return new ArrayList<>(worlds.values());
	}

	public void removeWorld(RegisteredWorld world){
		worlds.remove(world.getName());
	}

	public void save() {
		YamlConfiguration storage = new YamlConfiguration();

		worlds.values().forEach(w -> w.save(storage.createSection(w.getName())));

		try {
			storage.save(new File(WorldManager.getInstance().getDataFolder() + "/storage.yml"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}