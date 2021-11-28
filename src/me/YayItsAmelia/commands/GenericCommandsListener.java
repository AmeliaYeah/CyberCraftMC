package me.YayItsAmelia.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.ui.ClickableInventoryItem;
import me.YayItsAmelia.ui.ClickableInventoryItemList;
import me.YayItsAmelia.util.ModifiedClonesList;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public final class GenericCommandsListener
{
	public static void genericDragEvent(InventoryDragEvent e, ModifiedClonesList<Inventory> inventories)
	{
		//Cancel the drag event if we find a match
		if(inventories.convertCloneToOriginal(e.getInventory(), false) != null) e.setCancelled(true);
	}
	
	public static ModifiedClonesList<Inventory> genericClickEvent(InventoryClickEvent e, ModifiedClonesList<Inventory> inventories, ClickableInventoryItemList itemList)
	{
		//If inventories is null, return
		//Put an error in the console too, as it could be possible that this is a programming error
		if(inventories == null)
		{
			Main.instance.getLogger().severe("The inventories parameter appears to be...null? This is likely a programming error. Please contact the plugin author (GenericCommandsListener; genericClickEvent())");
			return inventories;
		}
		
		//Do the same for ClickableInventoryItemList
		if(itemList == null)
		{
			Main.instance.getLogger().severe("The itemList parameter is null. This is likely a programming error; contact the plugin author (GenericCommandsListener; genericClickEvent())");
			return inventories;
		}
		
		//Get the inventory data and return if the result is null
		Tuple<Inventory, Object> clicked = inventories.convertCloneToOriginal(e.getClickedInventory(), true);
		if(clicked == null) return inventories;
		
		//If the inventory is valid, cancel the event.
		//This is because we want to prevent any additional modification to the inventory
		//For example, the player dragging items in
		e.setCancelled(true);
		
		//Get the current clicked item and check if it's valid
		ItemStack currentItem = e.getCurrentItem();
		if(!Utils.itemNullCheck(currentItem)) return inventories;
				
		//Get the executing player
		Player player = (Player)e.getWhoClicked();
		
		//Generate the inventory and the inventory's data
		Inventory inv = clicked.getFirst();
		Object data = clicked.getSecond();
		
		//Check if the data isn't equal to null and is a clickedinventoryitem array
		//If these are true, it means we're dealing with a modified inventory
		if(data != null && data instanceof ClickableInventoryItem[])
		{
			//Reset the inventory item list
			itemList = new ClickableInventoryItemList();
			
			//Convert the data into an array of clickableinventoryitems
			//Create an empty arraylist aswell
			ClickableInventoryItem[] itemsArr = (ClickableInventoryItem[])data;
			ArrayList<ClickableInventoryItem> items = new ArrayList<ClickableInventoryItem>();
			
			//Add the array data into the list
			Collections.addAll(items, itemsArr);
			
			//Add the data into the inventory list
			itemList.insertInventoryData(inv, items);
		}
				
		//Run the code execution for the clicked item and update the clones list
		return itemList.searchForItemAndExecute(inv, currentItem.getItemMeta().getDisplayName(), player, inventories);
	}
}
