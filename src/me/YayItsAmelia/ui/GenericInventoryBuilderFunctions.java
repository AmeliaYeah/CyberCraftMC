package me.YayItsAmelia.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.YayItsAmelia.commands.GenericCommandInventoryBuilder;
import me.YayItsAmelia.commands.cellphone.CellphoneNotificationHandler;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public final class GenericInventoryBuilderFunctions
{
	public static Tuple<Inventory, ClickableInventoryItem[]> modifiedInventoryGrid(NamedInventory invNamed, List<String> names, List<String> lores, List<Material> mats, BiFunction<Integer, ItemStack, ClickableInventoryItem> generateItem, int page, ClickableInventoryItem... itemsToAppend)
	{
		//Get the inventory of the named inventory
		Inventory inv = invNamed.getInventory();
		
		//Check if the page is less than zero
		//If it is, set it to zero
		if(page < 0) page = 0;
		
		//Generate the empty arraylist containing the clickable inventory items
		//Then, get the length of the names arraylist and store it
		ArrayList<ClickableInventoryItem> items = new ArrayList<ClickableInventoryItem>();
		
		//Loop through the items to append and add them to the arraylist
		if(itemsToAppend != null)
		{
			for(ClickableInventoryItem item : itemsToAppend)
			{
				items.add(item);
			}
		}
		
		//Subtract 9 from the inventory's size to get the max index
		//Then, calculate the "beginningOffset" using the max index (multiplied by the current page number)
		//We decrement the max index by 1 in the beginning offset since it doesn't start at zero
		int maxIndex = inv.getSize() - 9;
		int beginningOffset = (maxIndex - 1) * page;
		
		//Generate the variable pertaining to the actual indexes of all the lists
		//It is very important that we don't use "i" for this, as that is handled for actually placing the itemstacks on the inventory
		//We decrement the beginning offset by one as the index will be incremented at the start of the loop, effectively negating the change
		int pageSensitiveIndex = beginningOffset - 1;
		
		//Finally, before running the loop, check to see if the names list (minus 1) is less than the page sensitive index
		//This is done in the for loop too, but if the check fails right off the bat, it's safe to say that the inventory is going to be empty.
		//We also run this current function again but with the page decremented.
		//The reason this is added is so we can prevent the player just infinitely scrolling forward until a stackoverflow or something occurs
		if(names.size() - 1 < pageSensitiveIndex) return modifiedInventoryGrid(invNamed, names, lores, mats, generateItem, page - 1, itemsToAppend);
		
		//Run the loop
		for(int i = 0; i < maxIndex; i++)
		{
			//Increment the page sensitive index
			pageSensitiveIndex++;
			
			//Check if the names list size (minus 1, since it doesn't start at 0) is less than the page sensitive index
			//If it is, it means we don't have enough items to fit in the inventory. Thus, we break the loop and stop building
			if(names.size() - 1 < pageSensitiveIndex) break;
			
			//Removing the latest entry in the names list to get the name
			//Along with this, generate the ID associated with the name
			String name = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + GenericCommandInventoryBuilder.formatID(i + 1) + ChatColor.RESET + ChatColor.RED + names.get(pageSensitiveIndex);
			
			//Get the current lore index using the current "true" index
			//If the index is greater than the lores, use the last element
			//We also decrement the trueCounter by 1, since we want to start at 0 and the counter starts at 1
			int indexLores = pageSensitiveIndex - 1;
			if(lores.size() - 1 < indexLores) indexLores = lores.size() - 1; //size - 1 is used since we're working in indexes, not sizes (indexes start at 0)
			
			//Perform the same exact operation with the materials array
			int indexMats = pageSensitiveIndex - 1;
			if(mats.size() - 1 < indexMats) indexMats = mats.size() - 1;
			
			//Generate the itemstack and feed it to the function, alongside the counter
			ItemStack generatedStack = Utils.createModifiedItem(mats.get(indexMats), name, lores.get(indexLores), null);
			ClickableInventoryItem item = generateItem.apply(i, generatedStack);
			
			//Add the item to the items arraylist and set the item at the inventory's current index
			items.add(item);
			inv.setItem(i, item.getAppearance());
		}
		
		//Generate the base itemstack aswell as the individual itemstacks for the forward/back buttons
		ItemStack base = Utils.createModifiedItem(Material.GREEN_STAINED_GLASS_PANE, "Direction", null, null);
		ItemStack forward = base.clone();
		ItemStack backward = base.clone();
		
		//Set the itemstack metadata
		ItemMeta forwardMeta = forward.getItemMeta();
		ItemMeta backwardMeta = backward.getItemMeta();
		
		//Set the metadata and update the stacks
		forwardMeta.setDisplayName("Next Page");
		backwardMeta.setDisplayName("Previous Page");
		forward.setItemMeta(forwardMeta);
		backward.setItemMeta(backwardMeta);
		
		//Generate the clickable inventory item for both the forward and the backward stacks
		//They will just execute this same function again, but with the page incremented/decremented (depending on the direction)
		int pageClone = page; //The variable is here because if I use the "page" parameter, the clickable inventory items throw a syntax error. So, not really intentional on my part.
		ClickableInventoryItem forwardItem = ClickableInventoryItem.generateShowModifiedInventoryOnClick(forward, invNamed.getData(), (Tuple<Player, NamedInventory> data) ->
		{
			return modifiedInventoryGrid(invNamed, names, lores, mats, generateItem, pageClone + 1, itemsToAppend);
		});
		ClickableInventoryItem backwardItem = ClickableInventoryItem.generateShowModifiedInventoryOnClick(backward, invNamed.getData(), (Tuple<Player, NamedInventory> data) ->
		{
			return modifiedInventoryGrid(invNamed, names, lores, mats, generateItem, pageClone - 1, itemsToAppend);
		});
		
		//Add the items to the items list aswell as on the inventory itself
		items.add(forwardItem);
		items.add(backwardItem);
		inv.setItem(maxIndex, backward);
		inv.setItem(inv.getSize() - 1, forward);
		
		//Construct an array with the arraylist's size and add the arraylist's contents
		ClickableInventoryItem[] itemsArr = new ClickableInventoryItem[items.size()];
		for(int i = 0; i < itemsArr.length; i++) { itemsArr[i] = items.get(i); }
		
		//Finally, return the items 
		return new Tuple<Inventory,ClickableInventoryItem[]>(inv, itemsArr);
	}
}
