package eu.macsworks.worldmanager.guis;

import eu.macsworks.worldmanager.WorldManager;
import eu.macsworks.worldmanager.guis.internal.InventoryBuilder;
import eu.macsworks.worldmanager.guis.internal.ItemBuilder;
import eu.macsworks.worldmanager.objects.QueryFilter;
import eu.macsworks.worldmanager.objects.RegisteredWorld;
import eu.macsworks.worldmanager.utils.ColorTranslator;
import eu.macsworks.worldmanager.utils.PluginLoader;
import eu.macsworks.worldmanager.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainGUI {

	private final PluginLoader loader;

	public MainGUI(){
		loader = WorldManager.getInstance().getMacsPluginLoader();
	}

	//Opens Main GUI
	public void open(Player player){
		player.openInventory(InventoryBuilder.builder()
				.named(loader.getLang("main-gui-name"))
				.slots(27)

					.addItem(10, ItemBuilder.builder().material(Material.LIME_DYE).name(loader.getLang("create-world")).interactive(interactResult -> interactResult.getClicker().sendMessage("Placeholder Item")).build())

					.addItem(16, ItemBuilder.builder().material(Material.COMPASS).name(loader.getLang("import-world")).interactive(interactResult -> importWorld(interactResult.getClicker())).build())

					.addItem(13, ItemBuilder.builder().material(Material.BOOK).name(loader.getLang("edit-worlds")).interactive(interactResult -> editWorlds(interactResult.getClicker(), 0, "", QueryFilter.UNFILTERED)).build())

				.filled(ItemBuilder.builder().material(Material.BLACK_STAINED_GLASS_PANE).name("§f ").makeStatic().build())
				.build());
	}

	private void editWorlds(Player player, int page, String query, QueryFilter loaded){
		HashMap<Integer, ItemStack> items = new HashMap<>();
		List<RegisteredWorld> queryResult = loader.searchWorlds(query, loaded);

		for(int i = (page * 36); i < (page + 1) * 36; i++){
			if(i >= queryResult.size()) break;

			RegisteredWorld world = queryResult.get(i);
			items.put(10 + i, ItemBuilder.builder()
					.material(world.getWorldMaterial())

					.name("§e" + world.getName())

					.dynamicLore(() -> loader.getLang((world.isLoaded() ? "" : "unloaded-") + "world-info")
							.replace("%env%", ColorTranslator.beautify(world.getWorldInfo().getEnvironment().name()))
							.replace("%seed%", String.valueOf(world.getWorldInfo().getSeed()))
							.replace("%last-used%", Utils.getTimeString(ZonedDateTime.now().toEpochSecond() - world.getLastUsedEpoch()))
							.replace("%creator%", world.getCreatorName())
							.replace("%unloader%", world.getUnloderName())
							.replace("%unloaded-for%", Utils.getTimeString(ZonedDateTime.now().toEpochSecond() - world.getUnloadedEpoch()))
							.replace("%size%", String.format("%.2f", world.getSize())))

					.interactive(interactResult -> {
								if(interactResult.getEvent().isShiftClick() && interactResult.getEvent().isLeftClick()){
									openMaterialSelection(player, world, query, loaded);
									return;
								}

								if(world.isLoaded()){
									if(interactResult.getEvent().isShiftClick()){
										world.unload(interactResult.getClicker().getName());
										editWorlds(player, 0, query, loaded);
										return;
									}

									interactResult.getClicker().teleport(world.getBukkitWorldReference().getSpawnLocation());
									return;
								}

								if(interactResult.getEvent().isShiftClick()){
									world.delete();
									editWorlds(player, 0, query, loaded);
									return;
								}

								world.load();
							})

					.build());
		}

		if(queryResult.size() > items.size()){
			items.put(53, ItemBuilder.builder()
					.material(Material.SPECTRAL_ARROW)
					.name(loader.getLang("next-page"))
					.interactive(interactResult -> {
						editWorlds(player, page + 1, query, loaded);
					})
					.build());
		}

		if(page > 0){
			items.put(45, ItemBuilder.builder()
					.material(Material.TIPPED_ARROW)
					.name(loader.getLang("prev-page"))
					.interactive(interactResult -> {
						editWorlds(player, page - 1, query, loaded);
					})
					.build());
		}

		items.put(47, ItemBuilder.builder()
				.material(loaded.getMaterial())
				.name(loader.getLang("switch-filter"))
				.lore(loader.getLang("filter-mode").replace("%mode%", ColorTranslator.beautify(loaded.name())))
				.interactive(interactResult -> {
					editWorlds(player, page, query, QueryFilter.values()[loaded.ordinal() >= 2 ? 0 : loaded.ordinal() + 1]);
				})
				.build());

		items.put(49, ItemBuilder.builder()
				.material(Material.BOOK)
				.name(loader.getLang("info"))
				.dynamicLore(() -> loader.getLang("info-lore")
						.replace("%tps%", String.format("%.2f", Utils.getTps()))
						.replace("%total-size%", String.format("%.2f", loader.getAllWorlds().stream().mapToDouble(RegisteredWorld::getSize).sum()))
						.replace("%worlds%", String.valueOf(loader.getAllWorlds().size()))
						.replace("%loaded-worlds%", String.valueOf(loader.getAllWorlds().size()))
						.replace("%unloaded-worlds%", String.valueOf(loader.getAllWorlds().size())))
				.makeStatic()
				.build());

		items.put(51, ItemBuilder.builder()
				.material(loaded.getMaterial())
				.name(loader.getLang("edit-query"))
				.lore(loader.getLang("current-query").replace("%query%", query))
				.interactive(interactResult -> {
					new AnvilGUI.Builder()
							.plugin(WorldManager.getInstance())
							.title(loader.getLang("edit-query-title"))
							.text(" ")
							.onClick((i, state) -> {
								String text = state.getText().startsWith(" ") ? state.getText().substring(1) : state.getText();
								editWorlds(player, 0, text, loaded);
								return Collections.singletonList(AnvilGUI.ResponseAction.close());
							})
							.itemOutput(ItemBuilder.builder().material(Material.LIME_DYE).name(loader.getLang("accept")).build())
							.open(interactResult.getClicker());
				})
				.build());

		player.openInventory(InventoryBuilder.builder()
						.setItems(items)
						.slots(54)
						.named(loader.getLang("worlds"))
						.filled(ItemBuilder.builder().material(Material.BLACK_STAINED_GLASS_PANE).name("§f ").makeStatic().build())
				.build());
	}

	private void importWorld(Player player){
		new AnvilGUI.Builder()
				.plugin(WorldManager.getInstance())
				.title(loader.getLang("input-world-name"))
				.text(" ")
				.onClick((i, state) -> {
					String text = state.getText().startsWith(" ") ? state.getText().substring(1) : state.getText();
					if(!loader.importWorld(text, player.getName())){
						player.sendMessage(loader.getLang("error-import-world"));
					}

					open(player);
					return Collections.singletonList(AnvilGUI.ResponseAction.close());
				})
				.itemOutput(ItemBuilder.builder().material(Material.LIME_DYE).name(loader.getLang("accept")).build())
				.open(player);
	}

	//Query and filter are only here to be able to open the same query as before upon icon selection
	//Page is back to 0 as we cannot assume the same page is valid, as the GUI can be used by another staff member to
	//massively delete worlds in the meantime, thus rendering the page non-existent
	private void openMaterialSelection(Player player, RegisteredWorld world, String query, QueryFilter filter){
		HashMap<Integer, ItemStack> items = new HashMap<>();

		int i = 0;
		for(Material availableIcon : loader.getWorldIcons()){
			items.put(i, ItemBuilder.builder()
							.material(availableIcon)
							.name("§f" + ColorTranslator.beautify(availableIcon.name()))
							.interactive(interactResult -> {
								world.setWorldMaterial(availableIcon);
								editWorlds(player, 0, query, filter);
							})
					.build());
			i++;
		}

		player.openInventory(InventoryBuilder.builder()
						.slots(54)
						.setItems(items)
						.named(loader.getLang("select-world-material").replace("%name%", world.getName()))
						.filled(ItemBuilder.builder().material(Material.BLACK_STAINED_GLASS_PANE).name("§f ").makeStatic().build())
				.build());
	}

}
