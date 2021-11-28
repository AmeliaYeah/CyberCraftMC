package me.YayItsAmelia.commands.cellphone.websites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandInventoryBuilder;
import me.YayItsAmelia.commands.cellphone.CellphoneInventoryItemBuilder;
import me.YayItsAmelia.commands.ftp.FtpCommandExecutor;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.ui.ClickableInventoryItem;
import me.YayItsAmelia.ui.GenericInventoryBuilderFunctions;
import me.YayItsAmelia.ui.InteractiveInventoryCreationData;
import me.YayItsAmelia.ui.InventoryBuilder;
import me.YayItsAmelia.ui.NamedInventory;
import me.YayItsAmelia.util.Triplet;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public class ServerFarmSiteBuilder
{
	public static InventoryBuilder createServerFarmWebsite(InventoryBuilder builder, Inventory webScreen)
	{
		//Create the main site aswell as the URL icon
		Inventory serverFarmWebsite = CellphoneInventoryItemBuilder.buildWebsiteInventory("server-farm.co");
		builder.addItemToBake(webScreen, 1, GenericCommandInventoryBuilder.produceShowOnClick(Material.CHEST, ChatColor.GOLD + "" + ChatColor.BOLD + "server-farm.co", "The FTP solution for you!", serverFarmWebsite));
		
		//Create the sub-sites
		Inventory serverFarmWebsiteFTPPurchase = CellphoneInventoryItemBuilder.buildWebsiteInventory("server-farm.co/purchase");
		Inventory serverFarmUpgradeFTPSite = CellphoneInventoryItemBuilder.buildWebsiteInventory("server-farm.co/upgrade");
				
		//Website (server farm FTP main)
		builder.addItemToBake(serverFarmWebsite, 2, GenericCommandInventoryBuilder.produceShowOnClick(Material.EMERALD, ChatColor.GOLD + "" + ChatColor.BOLD + "Purchase", null, serverFarmWebsiteFTPPurchase));
		builder.addItemToBake(serverFarmWebsite, 4, GenericCommandInventoryBuilder.produceShowOnClick(Material.HEART_OF_THE_SEA, ChatColor.AQUA + "" + ChatColor.BOLD + "Upgrade", null, serverFarmUpgradeFTPSite));
		builder.addItemToBake(serverFarmWebsite, 6, GenericCommandInventoryBuilder.produceModifiedInvOnClick(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Revoke", null, new InteractiveInventoryCreationData(CellphoneInventoryItemBuilder.buildWebsiteName("server-farm.co/revoke"), 27), (Tuple<Player,NamedInventory> dataInput) ->
		{
			//Generate the grid containing all the player's servers
			return showServers(dataInput.getFirst(), dataInput.getSecond(), (Player player, String serverName) ->
			{
				//Check if the server has items in it
				//If it does, error to the player and return
				if(FtpCommandExecutor.getServerItems(player, serverName).size() > 0)
				{
					createServerDeletionMessage(player, "Your server needs to be completely empty before it can be revoked.");
					return;
				}
				
				//Try to delete the server
				Tuple<Boolean,String> res = FtpCommandExecutor.deleteServerForPlayer(player, serverName, "Server Farms Inc.", "Requested by owner");
				if(!res.getFirst()) createServerDeletionMessage(player, res.getSecond());
				else player.sendMessage(Utils.getChatPrefix() + ChatColor.GREEN + res.getSecond());
			});
		}));
		builder = createServerFarmPurchaseSite(builder, serverFarmWebsiteFTPPurchase);
		builder = createServerFarmUpgradeSite(builder, serverFarmUpgradeFTPSite);
		
		//Finally, return the builder
		return builder;
	}
	
	static void createServerDeletionMessage(Player player, String message)
	{
		player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Server revocation failed: " + message);
	}
	
	static Tuple<Inventory, ClickableInventoryItem[]> showServers(Player player, NamedInventory invNamed, BiConsumer<Player, String> runFunc)
	{
		//Loop through the player's owned servers and generate an empty list for the display names
		//Also generate a list containing the flags for each server, to use as the lore
		List<String> ownedServers = FtpCommandExecutor.getAllOwnedServers(player);
		List<String> ownedServerDisplayNames = new ArrayList<String>();
		ArrayList<String> lores = new ArrayList<String>();
		for(String server : ownedServers)
		{
			//Get the server size integer aswell as the string to contain the size
			//Then, loop through the FtpServerSizes to see if it matches
			int serverSize = FtpCommandExecutor.getServerSize(player, server);
			String serverSizeStr = serverSize + " slots";
			for(FtpCommandExecutor.ServerSize size : FtpCommandExecutor.ServerSize.values())
			{
				//If the size isn't the same to the server size, continue the loop
				if(size.inventorySize != serverSize) continue;
				
				//Otherwise, set the server size string to the name of the current server size enum
				//Then break the loop to save resources
				//We set the string to all capitals for better visual aesthetic
				serverSizeStr = size.name().toUpperCase();
				break;
			}
			
			//Add a modified version of the string to the ownedServerDisplayNames list
			//The modified version will contain the name aswell as the server size
			ownedServerDisplayNames.add(server + ChatColor.LIGHT_PURPLE + " (" +  serverSizeStr + ")");
			
			//Construct the current lore beginning
			//Then, get the flags and loop through them
			String loreBeginning = ChatColor.GRAY + "" + ChatColor.BOLD + "UPGRADES:" + ChatColor.WHITE;
			for(String flag : FtpCommandExecutor.loadFlags(player, server))
			{
				//Add to the lore beginning by beginning with a newline (since the beginning didn't end with one) and inputting the flag
				loreBeginning += "\n -" + flag;
			}
			
			//Add the constructed lore to the arraylist
			lores.add(loreBeginning);
		}
		
		//Generate the grid
		return GenericInventoryBuilderFunctions.modifiedInventoryGrid(invNamed, ownedServerDisplayNames, lores, Arrays.asList(Material.END_CRYSTAL), (Integer i, ItemStack stack) ->
		{
			//Create the inventory item
			return ClickableInventoryItem.generateRunOnClick(stack, (Player playerClicked) ->
			{
				//Execute the run function with the ACTUAL server name aswell as the player who clicked
				runFunc.accept(player, ownedServers.get(i));
			});
		}, 0);
	}
	
	static List<String> upgradeNames = new ArrayList<String>();
	public static List<String> getUpgradeNames()
	{
		return upgradeNames;
	}
	
	static InventoryBuilder createServerFarmUpgradeSite(InventoryBuilder builder, Inventory site)
	{
		//Get the base path
		String basePath = "ftpUpgradesCost" + FileManager.getPathSeperator();
		
		//Generate the arraylist containing each of the upgrades
		//List containing the default material, name + description, and default cost
		ArrayList<Triplet<Material, Tuple<String,String>, Integer>> upgrades = new ArrayList<Triplet<Material, Tuple<String,String>, Integer>>();
		upgrades.add(new Triplet<Material,Tuple<String,String>,Integer>(Material.BONE_MEAL, new Tuple<String,String>("disk_encryption","Encrypts the server's disk\nForeign parties without the decryption key\nwill not be able to view the drive"),9600));
		
		//Loop through the arraylist
		for(int i = 0; i < upgrades.size(); i++)
		{
			//Get the current upgrade
			Triplet<Material,Tuple<String,String>,Integer> upgrade = upgrades.get(i);
			
			//Get the values
			Material mat = upgrade.getFirst();
			String name = upgrade.getSecond().getFirst();
			String description = ChatColor.GOLD + upgrade.getSecond().getSecond(); //Dark_Gray is put before since the first sentence before the new line is meant to be the brief description, while the one after it is meant as a longer one
			int defaultCost = upgrade.getThird();
			
			//Get the cost
			double cost = Main.instance.getConfig().getDouble(basePath + name, defaultCost);
			
			//If the cost is -1, continue the for loop
			//This is because, in the config file, a value of -1 indicates that the server owner doesn't want the upgrade to be available
			if(cost == -1) continue;
			
			//Create the item
			builder.addItemToBake(site, i, GenericCommandInventoryBuilder.produceModifiedInvOnClick(mat, name, description + "\n" + ChatColor.GREEN + "Cost: " + Main.economy.format(cost), new InteractiveInventoryCreationData(CellphoneInventoryItemBuilder.buildWebsiteName("server-farm.co/upgrade/?upgrade="+ name), 27), (Tuple<Player,NamedInventory> dataInput) ->
			{
				//Show the servers
				return showServers(dataInput.getFirst(), dataInput.getSecond(), (Player player, String serverName) ->
				{
					//Check if the server already has the flag
					//If it does, return and error to the player
					if(FtpCommandExecutor.hasFlag(player, serverName, name))
					{
						player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The server you've chosen already contains the upgrade!");
						return;
					}
					
					//Check if the transaction was successful
					//If it was unsuccessful and returns false, we also return
					//There's no need to error to the player since the function does that for us
					if(!Utils.playerTransaction(player, cost, Utils.TransactionType.PAY)) return;
					
					//If everything was successful, set the flag
					FtpCommandExecutor.setFlag(player, serverName, name, false);
				});
			}));
			
			//Once the item is added, add the upgrade's name to the upgradeNames list so it can be accessed by other scripts
			upgradeNames.add(name);
		}
		
		//Finally, return the builder
		return builder;
	}
	
	static InventoryBuilder createServerFarmPurchaseSite(InventoryBuilder builder, Inventory serverFarmPurchase)
	{
		//Get the base path
		String basePath = "ftpServerCost" + FileManager.getPathSeperator();
		
		//Generate an arraylist containing a triplet of each of the sizes
		//The integer will be the default cost, the material will be the item to display, and the string will be the name
		ArrayList<Triplet<Integer, Material, String>> costs = new ArrayList<Triplet<Integer,Material,String>>();
		costs.add(new Triplet<Integer,Material,String>(1300, Material.COAL, "small"));
		costs.add(new Triplet<Integer,Material,String>(2700, Material.DIAMOND, "medium"));
		costs.add(new Triplet<Integer,Material,String>(5600, Material.EMERALD, "large"));
		
		//Sort the array
		costs.sort((Triplet<Integer,Material,String> item1, Triplet<Integer,Material,String> item2) ->
		{
			//Get the cost values of item1 and item2
			int cost1 = item1.getFirst();
			int cost2 = item2.getFirst();
			
			//Compare them
			if(cost1 == cost2) return 0;
			else if(cost1 > cost2) return 1;
			else return -1;
		});
		
		//Loop through the arraylist
		//We generated a "currentSlot" value since it allows for padding
		int currentSlot = 0;
		for(int i = 0; i < costs.size(); i++)
		{
			//Increment currentSlot by 2
			currentSlot += 2;
			
			//Get the current cost and structure the data
			Triplet<Integer,Material,String> costData = costs.get(i);
			int defaultVal = costData.getFirst();
			Material material = costData.getSecond();
			String sizeStr = costData.getThird();
			
			//Get the cost
			double cost = Main.instance.getConfig().getDouble(basePath + sizeStr, defaultVal);
			
			//Generate the inventory item
			builder.addItemToBake(serverFarmPurchase, currentSlot, GenericCommandInventoryBuilder.produceFunctionOnClick(material, ChatColor.GREEN + "Server Size: " + ChatColor.GOLD + sizeStr, "Cost: " + Main.economy.format(cost), (Player player) ->
			{
				//Generate the variable pertaining to the size enum
				//Also store the uppercase server size name
				FtpCommandExecutor.ServerSize size = null;
				String sizeName = sizeStr.toUpperCase();
				
				//Try to parse the cost string
				//If it can't be parsed, return
				try {
					size = FtpCommandExecutor.ServerSize.valueOf(sizeName);
				}catch(Exception e)
				{
					player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There was an error parsing '" + sizeName + "' into a proper size enum. This is likely a programming error, please contact the plugin author.");
					return;
				}
				
				//If the player doesn't meet the server creation requirements, return
				Tuple<Boolean,String> requirementsMet = FtpCommandExecutor.meetsServerCreationRequirements(player);
				if(!requirementsMet.getFirst())
				{
					player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + requirementsMet.getSecond());
					return;
				}
				
				//If the transaction for the server didn't work, return
				if(!Utils.playerTransaction(player, cost, Utils.TransactionType.PAY)) return;
				
				//If we got here, it is assumed that everything was successful. We create the server for the player and configure the result settings
				Tuple<Boolean,String> res = FtpCommandExecutor.createServerForPlayer(player, size, "Server Farms Inc.");
				ChatColor resColor = ChatColor.GREEN;
				if(!res.getFirst()) resColor = ChatColor.RED;
				
				//Send the result to the player's chat
				player.sendMessage(Utils.getChatPrefix() + resColor + res.getSecond());
			}));
		}
		
		//Finally, after the purchase site was created, return the builder
		return builder;
	}
}
