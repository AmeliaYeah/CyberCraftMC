package me.YayItsAmelia.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.admin.GiveFTPServerExecutor;
import me.YayItsAmelia.commands.admin.RevokeFTPServerExecutor;
import me.YayItsAmelia.commands.admin.SendNotificationExecutor;
import me.YayItsAmelia.commands.admin.SetFTPServerFlagsExecutor;
import me.YayItsAmelia.commands.cellphone.CellphoneCommandExecutor;
import me.YayItsAmelia.commands.cellphone.websites.ServerFarmSiteBuilder;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceSiteManager;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.MainFreelanceCommandExecutor;
import me.YayItsAmelia.commands.ftp.FtpCommandExecutor;
import me.YayItsAmelia.commands.util.CommandUtil;
import me.YayItsAmelia.item.ItemManager;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public final class CommandsInitializer
{
	public static HashMap<String, Tuple<Boolean, GenericCommandExecutor>> generateCommands()
	{
		//Generate the hashmap
		HashMap<String, Tuple<Boolean, GenericCommandExecutor>> commandsToCheck = new HashMap<String, Tuple<Boolean, GenericCommandExecutor>>();
		
		//Generate the /cellphone command
		CellphoneCommandExecutor cell = new CellphoneCommandExecutor();
		Main.instance.getServer().getPluginManager().registerEvents(cell, Main.instance);
		commandsToCheck.put("cellphone", new Tuple<Boolean, GenericCommandExecutor>(true, cell));
		
		//Generate the /ftp command
		FtpCommandExecutor ftp = new FtpCommandExecutor();
		Main.instance.getServer().getPluginManager().registerEvents(ftp, Main.instance);
		commandsToCheck.put("ftp", new Tuple<Boolean, GenericCommandExecutor>(true, ftp));
		
		//Generate the /freelance command
		MainFreelanceCommandExecutor freelance = new MainFreelanceCommandExecutor();
		commandsToCheck.put("freelance", new Tuple<Boolean, GenericCommandExecutor>(true, freelance));
		
		//Generate the admin commands
		commandsToCheck.put("send-notification", new Tuple<Boolean, GenericCommandExecutor>(false, new SendNotificationExecutor()));
		commandsToCheck.put("give-storage", new Tuple<Boolean, GenericCommandExecutor>(false, new GiveFTPServerExecutor()));
		commandsToCheck.put("revoke-storage", new Tuple<Boolean,GenericCommandExecutor>(false,new RevokeFTPServerExecutor()));
		commandsToCheck.put("set-server-upgrade", new Tuple<Boolean, GenericCommandExecutor>(false, new SetFTPServerFlagsExecutor()));
		
		//Return the hashmap when initialization is done
		return commandsToCheck;
	}
	
	public static HashMap<String, BiFunction<CommandSender, String[], List<String>>> generateTabCompletes()
	{
		//Generate the hashmap
		HashMap<String, BiFunction<CommandSender, String[], List<String>>> tabCompletes = new HashMap<String, BiFunction<CommandSender, String[], List<String>>>();
		
		//Creates the /cellphone tab completer
		//We do this because, visually, it is unpleasant to be recommended the player's name rather than nothing
		tabCompletes.put("cellphone", (CommandSender sender, String[] args) -> { return new ArrayList<String>(); });
		
		//Creates the /ftp tab completer
		tabCompletes.put("ftp", (CommandSender sender, String[] args) ->
		{
			//If the sender isn't a player, return nothing
			if(!(sender instanceof Player)) return new ArrayList<String>();
			
			//Get the player and return their owned servers
			Player player = (Player)sender;
			return FtpCommandExecutor.getAllOwnedServers(player);
		});
		
		//Creates the /freeelance tab completer
		tabCompletes.put("freelance", (CommandSender sender, String[] args) ->
		{
			//If the sender isn't a player or if freelancing is disabled in the config, return nothing
			//Also return nothing if the first argument isn't "create-project", since that doesn't need anymore completes
			if(!(sender instanceof Player)) return new ArrayList<String>();
			if(FreelanceSiteManager.freelancingDisabled()) return new ArrayList<String>();
			if(args.length > 1 && !args[0].equalsIgnoreCase("create-project")) return new ArrayList<String>();
			
			//If the argument length is 1, give the modes
			//If the argument length is 2, send the accepted materials
			//The rest of the arguments are all up to the player
			if(args.length == 1)
			{
				ArrayList<String> modes = new ArrayList<String>();
				modes.add("claim");
				modes.add("create-project");
				return modes;
			}
			if(args.length == 2) return CommandUtil.genericSearch(ItemManager.getMaterialStrings(), args[1]);
			else return new ArrayList<String>();
		});
		
		//Creates the /send-notification completer
		tabCompletes.put("send-notification", (CommandSender sender, String[] args) -> 
		{
			//If the arguments count isn't equal to 1, return an empty list, as tab completion for the online players is only done for the first argument
			if(args.length != 1) return new ArrayList<String>();
			
			//Otherwise, return all the online players, using the first argument as the search query
			return CommandUtil.genericSearch(Utils.getOnlinePlayerNames(), args[0]);
		});
		
		//Creates the /give-storage completer
		tabCompletes.put("give-storage", (CommandSender sender, String[] args) -> 
		{
			//If there is one argument, return the player search
			//If there are two arguments, return all the server sizes
			//Otherwise, return an empty string array
			if(args.length == 1) return CommandUtil.genericSearch(Utils.getOnlinePlayerNames(), args[0]);
			else if(args.length == 2)
			{
				ArrayList<String> sizes = new ArrayList<String>();
				sizes.add("small");
				sizes.add("medium");
				sizes.add("large");
				return sizes;
			}
			else return new ArrayList<String>();
			
		});
		
		//Creates the /revoke-storage completer
		tabCompletes.put("revoke-storage", (CommandSender sender, String[] args) ->
		{
			//If there is one argument, return the player search
			//If there are two arguments, return all the current player's servers
			//Otherwise, return an empty string array
			if(args.length == 1) return CommandUtil.genericSearch(Utils.getOnlinePlayerNames(), args[0]);
			else if(args.length == 2)
			{
				//Get the current player specified in the parameter
				//If the player doesn't exist, return an empty arraylist
				Player player = Bukkit.getPlayer(args[0]);
				if(player == null) return new ArrayList<String>();
				
				//Return all the player's owned servers
				return FtpCommandExecutor.getAllOwnedServers(player);
			}
			else return new ArrayList<String>();
		});
		
		//Creates the /set-server-upgrade completer
		tabCompletes.put("set-server-upgrade", (CommandSender sender, String[] args) ->
		{
			//If there is one argument, return the player search
			//If there are two arguments, return all the player's servers
			//If there are three arguments, return all available FTP upgrades
			//If there are four arguments, return a new list containing the modes of operation
			if(args.length == 1) return CommandUtil.genericSearch(Utils.getOnlinePlayerNames(), args[0]);
			else if(args.length == 2)
			{
				//Check if the specified player exists.
				Player player = Bukkit.getPlayer(args[0]);
				if(player == null) return new ArrayList<String>();
				
				//If they do, return their servers
				return FtpCommandExecutor.getAllOwnedServers(player);
			}
			else if(args.length == 3) return ServerFarmSiteBuilder.getUpgradeNames();
			else if(args.length == 4) return Arrays.asList("add","revoke");
			else return new ArrayList<String>();
		});
		
		//After the initialization is done, return the hashmap
		return tabCompletes;
	}
}
