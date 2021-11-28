package me.YayItsAmelia.commands;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.ui.ClickableInventoryItem;
import me.YayItsAmelia.ui.InteractiveInventoryCreationData;
import me.YayItsAmelia.ui.NamedInventory;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public final class GenericCommandInventoryBuilder
{
	public static String formatID(int id)
	{
		return "#" + id + ": ";
	}
	
	@SuppressWarnings("unchecked")
	static ClickableInventoryItem constructItem(Material type, String name, String lore, ClickableInventoryItem.ClickMethod method, Object data)
	{
		//Generate the itemstack and the variable containing the item we wish to return
		ItemStack stack = Utils.createModifiedItem(type, name, lore, null);
		ClickableInventoryItem returnItem = null;
		
		//Parse the click method
		switch(method)
		{
			case RUN_FUNCTION:
				returnItem = ClickableInventoryItem.generateRunOnClick(stack, (Consumer<Player>)data);
				break;
			case OPEN_INVENTORY:
				returnItem = ClickableInventoryItem.generateShowInventoryOnClick(stack, (Inventory)data);
				break;
			case OPEN_MODIFIED_INVENTORY:
				
				//Convert the object to an entry and obtain the values from it
				Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>> unboxed = (Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>>)data;
				InteractiveInventoryCreationData original = unboxed.getFirst();
				Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>> func = unboxed.getSecond();
				
				//Create the item and break
				returnItem = ClickableInventoryItem.generateShowModifiedInventoryOnClick(stack, original, func);
				break;
		}
		
		//Return the item
		return returnItem;
	}
	
	public static ClickableInventoryItem produceFunctionOnClick(@Nonnull Material type, @Nonnull String name, @Nonnull String lore, @Nonnull Consumer<Player> function)
	{
		return constructItem(type, name, lore, ClickableInventoryItem.ClickMethod.RUN_FUNCTION, function);
	}
	
	public static ClickableInventoryItem produceShowOnClick(@Nonnull Material type, @Nonnull String name, @Nonnull String lore, @Nonnull Inventory show)
	{
		return constructItem(type, name, lore, ClickableInventoryItem.ClickMethod.OPEN_INVENTORY, show);
	}
	
	public static ClickableInventoryItem produceModifiedInvOnClick(@Nonnull Material type, @Nonnull String name, @Nonnull String lore, @Nonnull InteractiveInventoryCreationData original, @Nonnull Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>> func)
	{
		//Combine the inventory and function into an entry
		//Then, return the constructed item
		Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>> combined = new Tuple<InteractiveInventoryCreationData, Function<Tuple<Player, NamedInventory>, Tuple<Inventory, ClickableInventoryItem[]>>>(original, func);
		return constructItem(type, name, lore, ClickableInventoryItem.ClickMethod.OPEN_MODIFIED_INVENTORY, combined);
	}
}
