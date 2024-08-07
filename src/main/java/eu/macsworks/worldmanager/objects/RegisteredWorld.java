package eu.macsworks.worldmanager.objects;

import eu.macsworks.worldmanager.WorldManager;
import eu.macsworks.worldmanager.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

@Data @RequiredArgsConstructor
public class RegisteredWorld {

	private final String name;
	private World bukkitWorldReference;
	private WorldInfo worldInfo;

	private Material worldMaterial = Material.BEACON;

	private long creationEpoch;
	private String creatorName;

	private long lastUsedEpoch;
	private String unloderName = "";
	private long unloadedEpoch = 0;

	/**
	 * Used upon world creation at runtime
	 * @param name Name of the world
	 * @param creator Who created the world
	 * @param creationEpoch When the world was created (epoch in seconds format)
	 */
	public RegisteredWorld(String name, String creator, long creationEpoch) {
		this.name = name;

		this.creationEpoch = creationEpoch;
		this.creatorName = creator;
		this.lastUsedEpoch = ZonedDateTime.now().toEpochSecond();

		load();
	}

	public boolean isLoaded(){
		return bukkitWorldReference != null;
	}

	public void load(){
		if(bukkitWorldReference != null){
			Bukkit.getLogger().warning("A RegisteredWorld was already loaded when trying to load!");
			return;
		}

		try {
			Path unloadedWorldPath = Path.of(WorldManager.getInstance().getMacsPluginLoader().getLoadableWorldsFolder().getCanonicalPath() + "/" + name);
			Utils.copyDirectory(unloadedWorldPath, Path.of(name));
			Utils.deleteDirectory(unloadedWorldPath.toFile());
		} catch (IOException e) {
			Bukkit.getLogger().warning("A RegisteredWorld failed to load! (" + name + ")");
			return;
		}

		try{
			lastUsedEpoch = ZonedDateTime.now().toEpochSecond();
			bukkitWorldReference = WorldCreator.name(name).createWorld();

			if(worldInfo == null){
				worldInfo = new WorldInfo(bukkitWorldReference.getEnvironment(), bukkitWorldReference.getSeed());
			}

			Bukkit.getLogger().info("World " + name + " loaded!");
		}catch (Exception e){
			Bukkit.getLogger().warning("A RegisteredWorld failed to load! (" + name + ")");
			return;
		}
	}

	public void unload(String unloader){
		unloadedEpoch = ZonedDateTime.now().toEpochSecond();
		unloderName = unloader;

		if(!Bukkit.unloadWorld(bukkitWorldReference, true)){
			Bukkit.getLogger().warning("A RegisteredWorld failed to unload! (" + name + ")");
			return;
		}

		bukkitWorldReference = null;

		try {
			Path unloadedWorldPath = Path.of(WorldManager.getInstance().getMacsPluginLoader().getLoadableWorldsFolder().getCanonicalPath() + "/" + name);
			Utils.copyDirectory(Path.of(name), unloadedWorldPath);
			Utils.deleteDirectory(new File(name));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public double getSize(){
		try{
			File folder = isLoaded() ? new File(name) : new File(WorldManager.getInstance().getMacsPluginLoader().getLoadableWorldsFolder().getCanonicalPath() + "/" + name);
			return FileUtils.sizeOfDirectory(folder) / 1000000D;
		}catch (Exception e){
			return 0D;
		}
	}

	public boolean delete() {
		if(bukkitWorldReference != null){
			return false;
		}

		try {
			if(!(Utils.deleteDirectory(new File(name)) || Utils.deleteDirectory(new File(WorldManager.getInstance().getMacsPluginLoader().getLoadableWorldsFolder().getCanonicalPath() + "/" + name)))) return false;

			WorldManager.getInstance().getMacsPluginLoader().removeWorld(this);
			return true;
		}catch (IOException e){
			return false;
		}
	}

	public void checkUsage(){
		if(ZonedDateTime.now().toEpochSecond() - lastUsedEpoch <= WorldManager.getInstance().getMacsPluginLoader().getWorldUsageThreshold()) return;

		unload("Server");
	}

	public void load(ConfigurationSection section){
		lastUsedEpoch = section.getInt("last-use");
		creationEpoch = section.getInt("creation");
		creatorName = section.getString("creator");
		unloderName = section.getString("unloader");
		worldMaterial = Material.getMaterial(section.getString("world-material"));
		worldInfo = new WorldInfo(World.Environment.valueOf(section.getString("info.env")), section.getLong("info.seed"));
	}

	public void save(ConfigurationSection section){
		section.set("last-use", lastUsedEpoch);
		section.set("creation", creationEpoch);
		section.set("creator", creatorName);
		section.set("unloader", unloderName);
		section.set("world-material", worldMaterial.name());
		section.set("info.env", worldInfo.environment.name());
		section.set("info.seed", worldInfo.seed);
	}

	@AllArgsConstructor @Data
	public static class WorldInfo{
		private final World.Environment environment;
		private final long seed;
	}
}
