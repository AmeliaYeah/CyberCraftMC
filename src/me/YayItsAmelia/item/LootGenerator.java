package me.YayItsAmelia.item;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.io.FileManager;

public final class LootGenerator
{
	static HashMap<Material, Integer> items = new HashMap<>();
	static int noneChance = 50;
	
	public static void initialize()
	{
		//Get the configuration file's section
		ConfigurationSection section = Main.instance.getConfig().getConfigurationSection("acceptedLootItems");
		
		//Define a temporary items hashmap and begin looping through the sections
		HashMap<Material,Integer> itemsTemp = new HashMap<>();
		for(String key : FileManager.getSectionNames(section))
		{
			//If the key is "none", then we set the noneChance variable to it's value and continue
			if(key.toLowerCase().equals("none"))
			{
				noneChance = section.getInt(key, 50);
				continue;
			}
			
			//Get the rarity using the key and create the material variable
			int rarity = section.getInt(key, 0);
			Material mat = null;
			
			//Try to parse the key into a material
			//Continue to the next iteration if it isn't successful
			try
			{
				mat = Material.valueOf(key.toUpperCase());
			}catch(Exception e)
			{
				Main.instance.getLogger().info("Error while parsing '" + key + "' into a valid item: " + e.getMessage() + ". Skipping...");
				continue;
			}
			
			//Add the material and rarity into the hashmap
			itemsTemp.put(mat, rarity);
		}
		
		//Sort the itemsTemp hashmap and add the values into the items hashmap
		itemsTemp.entrySet().stream().sorted((k1,k2) -> k1.getValue().compareTo(k2.getValue())).forEach((k) ->
		{
			items.put(k.getKey(), k.getValue());
		});
	}
	
	public static ItemStack[] generate(int inventorySize)
	{
		//Create the itemstack array with a size equivalent to the inventory size
		//Create the random generator also
		ItemStack[] returnStack = new ItemStack[inventorySize];
		Random rand = new Random();
		
		//Create a for loop and loop for each element in the stack
		for(int i = 0; i < inventorySize; i++)
		{
			//If the generator rolls a number that is less than or equal to the noneChance, we set the current stack item to "null" and continue
			//It's important that we use the "less than or equal to" operator rather than anything else
			//Using "greater than" would mean that lower numbers create higher likelihoods, which is the opposite to our "lower numbers = more rarity" system
			//Using an operator that doesn't use "equal to" are specifically so we can guarantee that the "100" chance happens..100% of the time
			if(rand.nextInt(100) <= noneChance)
			{
				returnStack[i] = null;
				continue;
			}
			
			//Create the material variable and begin looping through the items 
			Material mat = null;
			for(Entry<Material, Integer> item : items.entrySet())
			{
				//Check if the generator rolls a number and it is valid
				//If it isn't, continue
				//We use "greater than" only as it's the inverse to the check described above
				if(rand.nextInt(100) > item.getValue()) continue;
				
				//Set the item amount and keep increasing it until either:
				//  1. The generator rolls an invalid number
				//  2. The item is above the limit (64)
				int itemAmount = 1;
				while(rand.nextInt(100) <= item.getValue() && itemAmount < 64) itemAmount++;
				
				//Generate the itemstack and set it's amount to the itemAmount
				ItemStack stack = new ItemStack(item.getKey());
				stack.setAmount(itemAmount);
				
				//Set the return stack and break the loop
				returnStack[i] = stack;
				break;
			}
		}
		
		//Return the returnStack
		return returnStack;
	}
}
