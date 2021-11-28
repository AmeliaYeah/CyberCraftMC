package me.YayItsAmelia.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.util.ModifiedClonesList;

//Class designed for ease of use regarding inventory "onclick" itemstack validation
public class ClickableInventoryItemList
{
	HashMap<Inventory, ArrayList<ClickableInventoryItem>> data = new HashMap<Inventory, ArrayList<ClickableInventoryItem>>();
	Inventory mainInventory = null;
	
	public void setMainInventory(Inventory main)
	{
		mainInventory = main;
	}
	
	public Inventory getMainInventory()
	{
		return mainInventory;
	}
	
	public void insertInventoryData(Inventory inv, ArrayList<ClickableInventoryItem> items)
	{
		//Check if the data already contains the inventory
		//If it does, return
		if(data.containsKey(inv)) return;
		
		//Add the inventory and the items to the data
		data.put(inv, items);
	}
	
	public ModifiedClonesList<Inventory> searchForItemAndExecute(Inventory inv, String targetDisplayName, Player player, ModifiedClonesList<Inventory> clonesList)
	{
		//Locate the entry for the inventory
		//If it doesn't exist, return with a warming
		ArrayList<ClickableInventoryItem> items = data.get(inv);
		if(items == null)
		{
			Main.instance.getLogger().warning("Item '" + targetDisplayName + "' apparently doesn't exist (ClickableInventoryItemList; searchForItemAndExecute). You might want to notify the plugin author.");
			return clonesList;
		}
		
		//Search the items to see if the display name matches
		for(ClickableInventoryItem item : items)
		{
			//Run the conditions in order for verification to actually work
			if(!item.getAppearance().hasItemMeta()) continue;
			if(!item.getAppearance().getItemMeta().hasDisplayName()) continue;
			if(!item.getAppearance().getItemMeta().getDisplayName().equals(targetDisplayName)) continue;
			
			//If the conditions are met, execute the item and save the value
			//Then break so we can save resources and time
			clonesList = item.execute(player, clonesList);
			break;
		}
		
		//After we're all done, return the clones list
		return clonesList;
	}
	
	public Inventory[] getInventories()
	{
		//Convert the data keys into an object array
		//Then, create a blank inventory array with the length of the object array
		Object[] keys = data.keySet().toArray();
		Inventory[] returnArray = new Inventory[keys.length];
		
		//Loop through the object array
		for(int i = 0; i < keys.length; i++)
		{
			//Get the current value
			Object current = keys[i];
			
			//Convert it into an inventory object and add it to the return array
			returnArray[i] = (Inventory)current;
		}
		
		//Return the newly formatted inventory array
		return returnArray;
	}
}
