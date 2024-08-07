package eu.macsworks.worldmanager.commands.internal;

import eu.macsworks.worldmanager.WorldManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class MacsCommand implements CommandExecutor {

    @Getter private final String commandId;
    @Getter @Setter private String requiredPerm;
    /**
     * -- SETTER --
     *  Sets whether this command accepts being run without any arguments
     *
     * @param acceptsNoArgs
     */
    @Setter
    private boolean acceptsNoArgs = true;

    /**
     * -- SETTER --
     *  Sets the default behavior if no relevant subcommands were found. Do not set to send the usage command.
     *
     * @param consumer
     */
    @Setter
    private BiConsumer<Player, String[]> defaultBehavior;
    private BiConsumer<CommandSender, String[]> defaultBehaviorConsole;;

    @Getter @Setter private String usage;
    @Getter @Setter private String requiredArgs;

    public MacsCommand(String command){
        this.commandId = command;
        defaultBehavior = (player, args) -> player.sendMessage("§cThis command has no player behavior set.");
        defaultBehaviorConsole = (player, args) -> player.sendMessage("§cThis command has no console behavior set.");
    }

    /**
     * Sets the default console behavior if no relevant subcommands were found. Do not set to send the usage command.
     * @param consumer
     */
    public void setDefaultConsoleBehavior(BiConsumer<CommandSender, String[]> consumer){
        this.defaultBehaviorConsole = consumer;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player p)){
            if(strings.length == 0){
                if(acceptsNoArgs){
                    defaultBehaviorConsole.accept(commandSender, strings);
                    return true;
                }

                return true;
            }

            if(defaultBehavior != null){
                defaultBehaviorConsole.accept(commandSender, strings);
                return true;
            }

            return true;
        }

        //If there's a required perm & the player doesn't have it, cancel the command with an error message
	    if(requiredPerm != null && !p.hasPermission(requiredPerm)){
            p.sendMessage(WorldManager.getInstance().getMacsPluginLoader().getLang("no-permission"));
            return true;
        }

        //If there's no args specified, and we accept that, execute the command
        if(strings.length == 0){
            if(acceptsNoArgs){
                defaultBehavior.accept(p, strings);
                return true;
            }

            return true;
        }

        if(defaultBehavior != null){
            defaultBehavior.accept(p, strings);
            return true;
        }

        return true;
    }
}
