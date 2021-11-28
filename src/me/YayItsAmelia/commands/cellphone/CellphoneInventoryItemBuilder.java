package me.YayItsAmelia.commands.cellphone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandInventoryBuilder;
import me.YayItsAmelia.commands.cellphone.websites.ServerFarmSiteBuilder;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceSiteManager;
import me.YayItsAmelia.commands.ftp.FtpCommandExecutor;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.ui.ClickableInventoryItem;
import me.YayItsAmelia.ui.ClickableInventoryItemList;
import me.YayItsAmelia.ui.GenericInventoryBuilderFunctions;
import me.YayItsAmelia.ui.InteractiveInventoryCreationData;
import me.YayItsAmelia.ui.InventoryBuilder;
import me.YayItsAmelia.ui.NamedInventory;
import me.YayItsAmelia.util.Triplet;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public class CellphoneInventoryItemBuilder
{
	public ClickableInventoryItemList build()
	{
		//Build the main screens
		Inventory mainScreen = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Personal Cellphone");
		Inventory webScreen = Bukkit.createInventory(null, 27, ChatColor.AQUA + "" + ChatColor.BOLD + "TOR Browser");
		
		//Create the main inventory items
		ClickableInventoryItem exitButton = GenericCommandInventoryBuilder.produceShowOnClick(Material.WHITE_STAINED_GLASS_PANE, "Home", "Return to the home screen", mainScreen);
		
		//Create the builder
		InventoryBuilder builder = new InventoryBuilder(mainScreen);
		
		//Create the inventory items
		//Main screen
		builder.addItemToBake(mainScreen, 2, GenericCommandInventoryBuilder.produceShowOnClick(Material.POPPED_CHORUS_FRUIT, "TOR", "Browse the web", webScreen));
		builder.addItemToBake(mainScreen, 4, GenericCommandInventoryBuilder.produceModifiedInvOnClick(Material.OAK_SIGN, "Notifications", "Views your phone's notifications", new InteractiveInventoryCreationData(ChatColor.DARK_AQUA + "Notifications", 54),  (Tuple<Player, NamedInventory> input) ->
		{
			//Get the namedinventory aswell as the inventory and store it
			NamedInventory invNamed = input.getSecond();
			Inventory inv = invNamed.getInventory();
			
			//Create the "exit" button
			inv.setItem(49, exitButton.getAppearance());
			
			//Get all the player's notifications and generate a blank "senders" arraylist
			List<ConfigurationSection> notifs = CellphoneNotificationHandler.getAllNotifications(input.getFirst());
			ArrayList<String> senders = new ArrayList<String>();
			
			//Loop through the configuration sections arraylist and add the sender to the senders list
			for(ConfigurationSection section : notifs)
			{
				senders.add(section.getString("sender"));
			}
			
			//Generate the grid
			return GenericInventoryBuilderFunctions.modifiedInventoryGrid(invNamed, senders, Arrays.asList("Unopened Message"), Arrays.asList(Material.PAPER), (Integer i, ItemStack stack) ->
			{
				//Create the item
				return ClickableInventoryItem.generateRunOnClick(stack, (Player readingPlayer) -> 
				{
					//Generate the section name via the index
					//Generate the seperator along with it
					String sectionName = notifs.get(i).getName();
					String seperator = ChatColor.GRAY + "------------------------------" + ChatColor.RESET;
					
					//Get the notification, then echo it to the player
					String notificationText = CellphoneNotificationHandler.getNotification(readingPlayer, sectionName);
					readingPlayer.sendMessage(ChatColor.GRAY + "        BEGIN NOTIFICATION\n" + seperator + "\n" + notificationText + "\n" + seperator);
				});
			}, 0, exitButton);
		}));
		
		//Build the websites
		builder.addItemToBake(webScreen, 22, exitButton);
		builder = ServerFarmSiteBuilder.createServerFarmWebsite(builder, webScreen);
		builder = FreelanceSiteManager.buildSite(builder, webScreen);
		
		//Finally, return the baked item list
		return builder.bake();
	}
	
	public static String buildWebsiteName(String name)
	{
		return ChatColor.GREEN + "" + ChatColor.ITALIC + "https://" + ChatColor.RED + name;
	}
	
	public static Inventory buildWebsiteInventory(String name)
	{
		return Bukkit.createInventory(null, 9, buildWebsiteName(name));
	}
}
