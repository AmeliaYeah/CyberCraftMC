package me.YayItsAmelia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;

public final class ItemManager
{
	static List<Material> mats = new ArrayList<Material>();
	static List<String> matsStrings = new ArrayList<String>();
	public static void initialize()
	{
		//Loop through every and all material available
		for(Material mat : Material.values())
		{
			//If the material is not valid, continue the for loop
			if(!mat.isItem()) continue;
			
			//Add the material to the mats arraylist
			//Also get the name of the material and add it to the matsStrings list (for usage in tab completes and whatnot)
			mats.add(mat);
			matsStrings.add(mat.name().toLowerCase());
		}
	}
	
	public static List<String> getMaterialStrings() { return matsStrings; }
	
	public static Material getRandomItem()
	{
		return mats.get(new Random().nextInt(mats.size()));
	}
}
