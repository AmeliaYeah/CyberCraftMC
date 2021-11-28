package me.YayItsAmelia.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;

import me.YayItsAmelia.util.Tuple;

public class EnchantmentManager
{
	HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
	
	public void addEnchant(Enchantment enchant, Integer level)
	{
		//Add the enchant to the hashmap
		enchants.put(enchant, level);
	}
	
	public ArrayList<Tuple<Enchantment, Integer>> getEnchants()
	{
		//Generate the return array
		ArrayList<Tuple<Enchantment, Integer>> returnVal = new ArrayList<Tuple<Enchantment, Integer>>();
		
		//Generate the iterator and begin the loop
		Iterator it = enchants.entrySet().iterator();
		while(it.hasNext())
		{
			//Get the current entry
			@SuppressWarnings("unchecked")
			Map.Entry<Enchantment, Integer> entry = (Map.Entry<Enchantment, Integer>)it.next();
			
			//Place it in the returnVal
			returnVal.add(new Tuple<Enchantment, Integer>(entry.getKey(), entry.getValue()));
		}
		
		//Return the returnVal
		return returnVal;
	}
}
