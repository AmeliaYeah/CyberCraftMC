package me.YayItsAmelia.ui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class InteractiveInventoryCreationData
{
	String inventoryName;
	int inventorySize;
	
	public InteractiveInventoryCreationData(String name, int size)
	{
		inventoryName = name;
		inventorySize = size;
	}
}