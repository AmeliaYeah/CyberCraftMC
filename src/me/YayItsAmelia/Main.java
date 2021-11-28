package me.YayItsAmelia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.YayItsAmelia.commands.*;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceProjectAI;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceSiteManager;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.NameGenerator;
import me.YayItsAmelia.commands.hacking.HackableWebsite;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.item.LootGenerator;
import me.YayItsAmelia.item.ItemManager;
import me.YayItsAmelia.listeners.PlayerInput;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin
{
	public static Main instance = null;
	
	public static Economy economy = null;
	
	//Generate a hashmap with the key of a string (the command name)
	//Along with this, the value will be an Entry containing a boolean (whether it's player-only) and a GenericCommandExecutor
	HashMap<String, Tuple<Boolean, GenericCommandExecutor>> commandsToCheck = new HashMap<String, Tuple<Boolean, GenericCommandExecutor>>();
	
	//Generate a hashmap with the key of a string (the command name)
	//Along with this, the value will be a function that takes in a command sender and spits out a string list
	HashMap<String, BiFunction<CommandSender, String[], List<String>>> tabCompletes = new HashMap<String, BiFunction<CommandSender, String[], List<String>>>();
	
	@Override
	public void onEnable()
	{
		//Set the instance so other classes can access this class
		instance = this;
		
		//Initializes the save file and disables the plugin if it was not successful
		//There is no need to log the error as the file manager already handles that
		if(!FileManager.initialize())
		{
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		//After the data file is initialized, load the config file
		FileManager.initializeMainConfig();
		
		//Begin loading the economy
		//If it was unsuccessful, terminate the plugin and explain what went wrong via the string response
		Tuple<Boolean, String> economyLoadResult = setEconomy();
		if(!economyLoadResult.getFirst())
		{
			getLogger().severe("An error occured regarding Vault economy integration: " + economyLoadResult.getSecond());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getConsoleSender().sendMessage(Utils.getChatPrefix() + ChatColor.GREEN + economyLoadResult.getSecond());
		
		//Initialize the commands and tab completes
		commandsToCheck = CommandsInitializer.generateCommands();
		tabCompletes = CommandsInitializer.generateTabCompletes();
		
		//Initialize the main event listeners
		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(PlayerInput.instance, this);
		
		//Initialize classes requiring initialization
		ItemManager.initialize(); //Must be the first initialized, since the other classes depend on it
		NameGenerator.initialize();
		LootGenerator.initialize();
		FreelanceProjectAI.initialize();
		HackableWebsite.initialize();
		
		//After initialization is done, create the console graphic
		createGraphic();
	}
	
	@Override
	public void onDisable()
	{
		//Save the game data
		FileManager.saveConfiguration();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		//Convert the command name to lowercase
		String commandName = command.getName().toLowerCase();
		
		//Return false if the command inputted is invalid and doesn't exist
		//If it does exist, get the chosen command
		if(!commandsToCheck.containsKey(commandName)) return false;
		Tuple<Boolean, GenericCommandExecutor> commandChosen = commandsToCheck.get(commandName);
		
		//If the command's "player only" boolean is set to true, check to see if we're the player.
		//If we aren't return true (so the command is marked as valid) and return a message explaining why.
		if(commandChosen.getFirst() && !(sender instanceof Player))
		{
			sender.sendMessage("Sorry, only a player can use the command: '/" + label + "'");
			return true;
		}
		
		//If it has been verified that the command is valid and a player is running it if it's set to player-only, run the executor and return true.
		GenericCommandExecutor executor = commandChosen.getSecond();
		executor.setCommand(command);
		executor.run(sender, args);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
	{
		//Convert the command name to lowercase
		String commandName = command.getName().toLowerCase();
		
		//Check if the tab completes contain the current command. If it does, get the function
		if(!tabCompletes.containsKey(commandName)) return null;
		BiFunction<CommandSender, String[], List<String>> tabCompleteFunction = tabCompletes.get(commandName);
		
		//Return the result of the function
		return tabCompleteFunction.apply(sender, args);
	}
	
	private Tuple<Boolean, String> setEconomy()
	{
		//Return false if the vault plugin was not found
		if(getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return new Tuple<Boolean, String>(false, "Vault plugin was not located");
		}
		
		//Return if the Economy class was not registrated
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null)
		{
			return new Tuple<Boolean, String>(false, "Vault economy class was not registered");
		}
		
		//If the economy is null, return false
		economy = rsp.getProvider();
		if(economy == null)
		{
			return new Tuple<Boolean, String>(false, "Vault economy class failed to load (RSP Provider is null)");
		}
		
		//When all is successful, return a success
		return new Tuple<Boolean, String>(true, "Vault economy loading successful!");
	}
	
	void createGraphic()
	{
		Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "Cyber" + ChatColor.DARK_GRAY + "Craft" + ChatColor.WHITE + ": A plugin by " + ChatColor.DARK_PURPLE + "Yay" + ChatColor.AQUA + "Its" + ChatColor.RED + "Amelia" + ChatColor.RESET + "\n" + ChatColor.WHITE + getDescription().getDescription() + ChatColor.RESET + "\n" + ChatColor.WHITE + "Initialization was completed successfully. Have fun!");
	}
	
	
	
}
