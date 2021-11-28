package me.YayItsAmelia.ui;


import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.util.ModifiedClonesList;
import me.YayItsAmelia.util.Tuple;

public class ClickableInventoryItem
{
	public enum ClickMethod { RUN_FUNCTION, OPEN_INVENTORY, OPEN_MODIFIED_INVENTORY };
	
	public static ClickableInventoryItem generateRunOnClick(ItemStack appearance, Consumer<Player> func)
	{
		return new ClickableInventoryItem(appearance, ClickMethod.RUN_FUNCTION, func);
	}
	
	public static ClickableInventoryItem generateShowInventoryOnClick(ItemStack appearance, Inventory show)
	{
		return new ClickableInventoryItem(appearance, ClickMethod.OPEN_INVENTORY, show);
	}
	
	public static ClickableInventoryItem generateShowModifiedInventoryOnClick(ItemStack appearance, InteractiveInventoryCreationData fromClone, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>> onClone)
	{
		//Generate a new entry from the inventory and function parameter
		//Then, return the inventory item
		Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>> data = new Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>>(fromClone, onClone);
		return new ClickableInventoryItem(appearance, ClickMethod.OPEN_MODIFIED_INVENTORY, data);
	}
	
	ItemStack appearance;
	ClickMethod method;
	Object data;
	
	protected ClickableInventoryItem(ItemStack appearance, ClickMethod method, Object data)
	{
		this.appearance = appearance;
		this.method = method;
		this.data = data;
	}
	
	public ItemStack getAppearance()
	{
		return appearance;
	}
	
	@SuppressWarnings("unchecked")
	public ModifiedClonesList<Inventory> execute(Player player, ModifiedClonesList<Inventory> originalList)
	{
		//Run a switch statement for each of the click methods
		switch(method)
		{
			case RUN_FUNCTION:
				
				//Convert the data to a consumer
				Consumer<Player> func = (Consumer<Player>)data;
				
				//Order the player to close their inventories, then run the function
				player.closeInventory();
				func.accept(player);
				break;
				
			case OPEN_INVENTORY:
				
				//Convert the data to an inventory and tell the player to open it
				Inventory inv = (Inventory)data;
				player.openInventory(inv);
				break;
				
			case OPEN_MODIFIED_INVENTORY:
				
				//Convert the data to an inventory and function tuple
				Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>> modifyFuncData = (Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>,Tuple<Inventory, ClickableInventoryItem[]>>>)data;
				
				//Get the original inventory and the function from the data
				NamedInventory original = new NamedInventory(modifyFuncData.getFirst());
				Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>> modifyFunc = modifyFuncData.getSecond();
				
				//Generate the input data and catch the output
				Tuple<Player, NamedInventory> input = new Tuple<>(player, original);
				Tuple<Inventory, ClickableInventoryItem[]> cloned = modifyFunc.apply(input);
				
				//Register the clone and tell the player to open it
				originalList.registerClone(original.getInventory(), cloned.getFirst(), cloned.getSecond());
				player.openInventory(cloned.getFirst());
				break;
		}
		
		//Return the list
		return originalList;
	}
}
