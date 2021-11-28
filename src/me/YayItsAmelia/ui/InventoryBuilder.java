package me.YayItsAmelia.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.util.Triplet;
import me.YayItsAmelia.util.Tuple;

public class InventoryBuilder
{
	HashMap<Inventory, ArrayList<Tuple<Integer, ClickableInventoryItem>>> inventoryItemsToBake = new HashMap<Inventory, ArrayList<Tuple<Integer, ClickableInventoryItem>>>();
	Inventory mainInventory = null;
	
	public InventoryBuilder(Inventory mainInventory)
	{
		this.mainInventory = mainInventory;
	}
	
	public void addItemToBake(Inventory inv, Integer position, ClickableInventoryItem item)
	{
		//Get the inventory entry and set it to a new arraylist if it returned null
		ArrayList<Tuple<Integer, ClickableInventoryItem>> entry = inventoryItemsToBake.get(inv);
		if(entry == null) entry = new ArrayList<Tuple<Integer, ClickableInventoryItem>>();
		
		//Add the parameters to the entry and save it in the hashmap
		entry.add(new Tuple<Integer, ClickableInventoryItem>(position, item));
		inventoryItemsToBake.put(inv, entry);
	}
	
	@SuppressWarnings("unchecked")
	public ClickableInventoryItemList bake()
	{
		//Create the clickable inventory item list to return and set the main inventory
		ClickableInventoryItemList list = new ClickableInventoryItemList();
		list.setMainInventory(mainInventory);
		
		//Create the iterator and start the loop
		Iterator<Entry<Inventory, ArrayList<Tuple<Integer, ClickableInventoryItem>>>> it = inventoryItemsToBake.entrySet().iterator();
		while(it.hasNext())
		{
			//Get the current entry and the inventory
			Entry<Inventory, ArrayList<Tuple<Integer, ClickableInventoryItem>>> entry = (Entry<Inventory, ArrayList<Tuple<Integer, ClickableInventoryItem>>>)it.next();
			Inventory inv = entry.getKey();
			
			//Create an array list of ClickableInventoryItems and loop through the arraylist
			ArrayList<ClickableInventoryItem> items = new ArrayList<ClickableInventoryItem>();
			for(Tuple<Integer, ClickableInventoryItem> item : entry.getValue())
			{
				//Seperate the position and the item
				Integer position = item.getFirst();
				ClickableInventoryItem invItem = item.getSecond();
				
				//Get the item's appearance
				ItemStack appearance = invItem.getAppearance();
				
				//Set the itemstack at the position, then append the clickable inventory item to the array
				inv.setItem(position, appearance);
				items.add(invItem);
			}
			
			//Now that we have both the inventory aswell as a completed arraylist of clickable inventory items, we can add our data to the list
			list.insertInventoryData(inv, items);
		}
		
		//After the iterator is completely done, we may return our completed list
		return list;
	}
}








