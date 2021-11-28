package me.YayItsAmelia.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.GenericCommandsListener;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.ui.ClickableInventoryItemList;
import me.YayItsAmelia.util.ModifiedClonesList;
import me.YayItsAmelia.util.Utils;
import net.md_5.bungee.api.ChatColor;

public class GenericInventoryCommandExecutor extends GenericCommandExecutor implements Listener
{
	ModifiedClonesList<Inventory> inventories = new ModifiedClonesList<Inventory>();
	ClickableInventoryItemList itemList = null;
	
	Inventory mainScreen = null;
	
	public GenericInventoryCommandExecutor(ClickableInventoryItemList list)
	{
		//Set the item list
		itemList = list;
		
		//Create a variable pertaining to the built inventories
		//Then, loop through them
		Inventory[] invs = itemList.getInventories();
		for(Inventory inv : invs)
		{
			//Add the inventory to the clones list
			inventories.addOriginal(inv);
		}
		
		//Set the main screen to the main inventory
		mainScreen = itemList.getMainInventory();
	}

	@Override
	public void run(CommandSender send, String[] args)
	{
		//Convert the sender to a player, as this is a player-only command
		//Then, open the inventory
		Player player = (Player)send;
		player.openInventory(mainScreen);
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e)
	{
		//Perform the standard drag event
		GenericCommandsListener.genericDragEvent(e, inventories);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e)
	{
		//Perform the generic click event and update the inventories array with the value
		inventories = GenericCommandsListener.genericClickEvent(e, inventories, itemList);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e)
	{
		//We run the clone location function since, if the inventory was cloned, we'd be able to free up it's memory occupation
		//This is because the inventory is no longer in use anymore, and it would not be neccessary to let it hog up memory it doesn't need
		inventories.locateClone(e.getInventory(), true);
	}
	
}
