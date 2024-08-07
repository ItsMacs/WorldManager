package eu.macsworks.worldmanager.objects;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum QueryFilter {

	LOADED(Material.COMPASS),
	UNLOADED(Material.RECOVERY_COMPASS),
	UNFILTERED(Material.BARRIER);

	private final Material material;

	QueryFilter(Material material) {
		this.material = material;
	}
}
