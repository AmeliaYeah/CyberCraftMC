package me.YayItsAmelia.commands.ftp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.cellphone.CellphoneNotificationHandler;
import me.YayItsAmelia.io.ArrayListSerializer;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;
import net.md_5.bungee.api.ChatColor;

public final class FtpCommandExecutor extends GenericCommandExecutor implements Listener
{
	static ConfigurationSection getOwnedServers(OfflinePlayer player)
	{
		//Returns a generic configuration section for the owned servers
		return FileManager.getGenericSection(player, "ownedServers");
	}
	
	static ConfigurationSection getOwnedServer(OfflinePlayer player, String serverName)
	{
		//Sanitize the server name
		serverName = FileManager.sanitize(serverName);
		
		//If the server isn't valid, return null
		if(!isServerValid(player, serverName)) return null;
		
		//Return the server
		ConfigurationSection server = getOwnedServers(player).getConfigurationSection(serverName);
		return server;
	}
	
	public static ArrayList<String> loadFlags(OfflinePlayer player, String serverName)
	{
		//Get the current server and return an empty array if it doesn't exist
		ConfigurationSection server = getOwnedServer(player, serverName);
		if(server == null) return new ArrayList<String>();
		
		//Load the flags
		return new ArrayListSerializer<String>().load(server.getConfigurationSection("flags"));
	}
	
	public static boolean hasFlag(OfflinePlayer player, String serverName, String flagName)
	{
		//Load the arraylist and return if the flag was found or not
		ArrayList<String> loaded = loadFlags(player, serverName);
		return loaded.contains(flagName);
	}
	
	public static void setFlag(OfflinePlayer player, String serverName, String flagName, boolean revoke)
	{
		//Load the arraylist
		ArrayList<String> flags = loadFlags(player, serverName);
		
		//Check if the flag already exists
		if(flags.contains(flagName))
		{
			//If the revoke permission is false, return
			if(!revoke) return;
			
			//Otherwise, delete the flag
			flags.remove(flagName);
		}else flags.add(flagName);
		
		//Save the flags
		new ArrayListSerializer<String>().save(getOwnedServer(player, serverName).getCurrentPath() + FileManager.getPathSeperator() + "flags", flags);
	}
	
	public static int getFlagCost(String flagName)
	{
		return Main.instance.getConfig().getInt("ftpUpgradesCost" + FileManager.getPathSeperator() + flagName, -1);
	}
	
	public static boolean isServerValid(OfflinePlayer player, String serverName)
	{
		//Sanitize the server name, then return the configuration section
		serverName = FileManager.sanitize(serverName);
		return getOwnedServers(player).isConfigurationSection(serverName);
	}
	
	public static int getServerSize(OfflinePlayer player, String serverName)
	{
		//Get the owned server
		ConfigurationSection server = getOwnedServer(player, serverName);
		
		//If the server returned is null, return zero
		//Otherwise, return the server's stored "size" variable
		if(server == null) return 0;
		else return server.getInt("size", 0);
	}
	
	
	public static ArrayList<ItemStack> getServerItems(OfflinePlayer player, String serverName)
	{
		//Get the base section and return an empty arraylist if the server doesn't exist
		ConfigurationSection base = getOwnedServer(player, serverName);
		if(base == null) return new ArrayList<ItemStack>();
		
		//Return the array
		return new ArrayListSerializer<ItemStack>().load(base.getConfigurationSection("inventory"));
	}
	
	public static void setServerItems(OfflinePlayer player, String serverName, ArrayList<ItemStack> stacks)
	{
		//Get the server
		ConfigurationSection server = getOwnedServer(player, serverName);
		if(server == null) return;
		
		//Save the data to the inventory
		new ArrayListSerializer<ItemStack>().save(server.getCurrentPath() + FileManager.getPathSeperator() + "inventory", stacks);
	}
	
	public static List<String> getAllOwnedServers(OfflinePlayer player)
	{
		//Load the owned servers section and return the names
		ConfigurationSection section = getOwnedServers(player);
		return FileManager.getSectionNames(section);
	}
	
	static int maxServerCount()
	{
		return Main.instance.getConfig().getInt("ftpServerCount", 10);
	}
	
	public static Tuple<Boolean, String> meetsServerCreationRequirements(OfflinePlayer player)
	{
		//If they don't have space for the server, return
		if(!hasSpaceForServer(player)) return new Tuple<Boolean,String>(false, "You've exceeded your FTP server quota (Maximum servers: " + maxServerCount() + ")");
		
		//Otherwise, return true
		return new Tuple<Boolean,String>(true, "You meet the requirements");
	}
	
	static boolean hasSpaceForServer(OfflinePlayer player)
	{
		//Gets the amount of servers the player currently has
		int amount = getAllOwnedServers(player).size();
		
		//Returns if the amount is less than the config-defined maximum
		return (amount < maxServerCount());
	}
	
	public enum ServerSize
	{
		SMALL(9),
		MEDIUM(27),
		LARGE(54);
		
		public final int inventorySize;
		ServerSize(int size)
		{
			this.inventorySize = size;
		}
	}
	public static Tuple<Boolean, String> createServerForPlayer(OfflinePlayer player, ServerSize size, @Nullable String giver)
	{
		//Check if they meet the server requirements
		//If they don't, return explaining why
		Tuple<Boolean,String> meetsServerRequirements = meetsServerCreationRequirements(player);
		if(!meetsServerRequirements.getFirst())
		{
			return new Tuple<Boolean,String>(false, meetsServerRequirements.getSecond());
		}
		
		//Generates the name of the server and creates it
		String serverName = IPAddressCreator.generateIP(getAllOwnedServers(player));
		ConfigurationSection server = getOwnedServers(player).createSection(serverName);
		server.set("size", size.inventorySize);
		server.set("original-owner", player.getUniqueId());
		
		//Sets the "giver" parameter to "Server" if it is set to null
		//Then, create the message
		if(giver == null) giver = "Server";
		String message = "You have just been given access rights to server " + ChatColor.AQUA + serverName + ChatColor.RESET + " (port " + IPAddressCreator.generatePort() + ") by user: " + ChatColor.GREEN + giver + ChatColor.RESET + "\nTry using /ftp <server-name> to access it!";
		
		//Send the notification to the player and return a success message
		CellphoneNotificationHandler.sendNotification(player, message, "PersonalFTP Service");
		return new Tuple<Boolean,String>(true, "Success creating server!");
	}
	
	public static Tuple<Boolean,String> deleteServerForPlayer(OfflinePlayer player, String serverName, @Nullable String remover, @Nullable String reason)
	{
		//If the server doesn't exist, return false and explain why
		if(!isServerValid(player, serverName)) return new Tuple<Boolean,String>(false, "Server does not exist!");
		
		//Set the server to null (this will delete it)
		getOwnedServers(player).set(serverName, null);
		
		//If the reason and/or remover are null, set it to the default
		//Construct the message afterwards
		if(remover == null) remover = "Server";
		if(reason == null) reason = "No reason specified.";
		String message = "User " + ChatColor.GREEN + remover + ChatColor.RESET + " has just revoked your access rights to server " + ChatColor.AQUA + serverName + ChatColor.RESET + "\nReason: " + ChatColor.GRAY + reason;
		
		//Send the notification and return a success message
		CellphoneNotificationHandler.sendNotification(player, message, "PersonalFTP Service");
		return new Tuple<Boolean,String>(true, "Server " + serverName + " has been revoked");
	}
	
	//This is added as a little security check, since the only way of checking to see if a GUI was related to the FTP command is through the title
	//Things like ModifiedCloneList<T> aren't going to work in this case since the itemstacks are unpredictable and malleable
	//Of course, this allows for players to do stuff like open fake GUIs (ex: naming a chest to the title and cheating the system that way)
	//This is what this validation check aims to fix
	ArrayList<Player> playersCurrentlyViewing = new ArrayList<Player>();
	
	/*
	 * Although, the player could still bypass this by manipulating their packets and skipping the inventory close event, which would
	 * essentially (in layman's terms) negate the entire point of this validation check. However, this is 100% deliberate and is
	 * likely not going to have a high chance of occuring aswell as having a zero chance of being done purely through coincidence.
	 * The risk is also very low, since all it's going to do is override the data with items they owned anyway.
	 */

	final String inventoryPrefix = ChatColor.GOLD + "" + ChatColor.BOLD + "FTP: " + ChatColor.RESET + ChatColor.AQUA;
	@Override
	public void run(CommandSender sender, String[] args)
	{
		//This is a player-only command, so we will convert the sender into a player
		Player player = (Player)sender;
		
		//If there are no arguments supplied, show an error and return
		if(args.length == 0)
		{
			showErrorMessage(sender, "Argument <Server> is required!");
			return;
		}
		
		//Get the server name and check if it's valid and exists
		//If it doesn't, return and show an error
		String serverName = args[0];
		if(!isServerValid(player, serverName))
		{
			showErrorMessage(sender, "Access Denied");
			return;
		}
		
		//Get the server data
		ConfigurationSection serverData = getOwnedServer(player, serverName);
		int inventorySize = serverData.getInt("size", 9);
		
		//If all the checks are complete, begin construction of the inventory
		Inventory serverInventory = Bukkit.createInventory(player, inventorySize, inventoryPrefix + serverName);
		
		//Get the server items
		//Then, run a for loop and set the inventory's contents to the itemstack arraylist
		ArrayList<ItemStack> stack = getServerItems(player, serverName);
		for(int i = 0; i < stack.size(); i++)
		{
			serverInventory.setItem(i, stack.get(i));
		}
		
		//Show the inventory to the player and add them to the playersCurrentlyViewing arraylist
		player.openInventory(serverInventory);
		playersCurrentlyViewing.add(player);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e)
	{
		//Get the title and the player
		String title = e.getView().getTitle();
		Player player = (Player)e.getPlayer();
		
		//If the inventory doesn't start with the prefix, return, as it isn't the correct inventory
		//Also return if they aren't in the playersCurrentlyViewing inventory (to avoid intentionally-matched incorrect inventories)
		if(!title.startsWith(inventoryPrefix)) return;
		if(!playersCurrentlyViewing.contains(player)) return;
		
		//Get the server name by removing the prefix, then check if the player owns the server
		title = title.replace(inventoryPrefix, "");
		if(!isServerValid(player, title)) return;
		
		//Generate an empty arraylist to contain the inventory contents, then dump the contents into it
		ArrayList<ItemStack> contents = new ArrayList<ItemStack>();
		Collections.addAll(contents, e.getInventory().getContents());
		
		//Remove the player from the list and save the contents
		playersCurrentlyViewing.remove(player);
		setServerItems(player, title, contents);
	}
}
