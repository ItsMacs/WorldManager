package eu.macsworks.worldmanager.commands;

import eu.macsworks.worldmanager.commands.internal.MacsCommand;
import eu.macsworks.worldmanager.guis.MainGUI;

public class ManageWorldsCommand extends MacsCommand {
	public ManageWorldsCommand() {
		super("manageworlds");

		setAcceptsNoArgs(true);
		setUsage("");
		setRequiredPerm("worldmanager.opengui");
		setRequiredArgs("");

		setDefaultBehavior((player, args) -> {
			new MainGUI().open(player);
		});
	}
}
