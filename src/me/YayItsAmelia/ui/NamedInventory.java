package me.YayItsAmelia.ui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class NamedInventory
{
	private Inventory inv;
	private InteractiveInventoryCreationData data;
	
	public NamedInventory(InteractiveInventoryCreationData data)
	{
		this.data = data;
		inv = Bukkit.createInventory(null, data.inventorySize, data.inventoryName);
	}
	
	public Inventory getInventory() { return inv; }
	public InteractiveInventoryCreationData getData(){ return data; }
}
