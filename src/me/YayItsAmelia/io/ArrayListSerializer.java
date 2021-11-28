package me.YayItsAmelia.io;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import me.YayItsAmelia.Main;

public class ArrayListSerializer<T>
{
	public void save(String path, ArrayList<T> list)
	{
		//Set the original path to null
		//This will erase all the data within the section
		FileManager.config.set(path, null);
		
		//If the configuration section does not exist, create it
		if(!FileManager.config.isConfigurationSection(path))
			FileManager.config.createSection(path);
		
		//Since the configuration section now exists, we can retrieve it
		ConfigurationSection section = FileManager.config.getConfigurationSection(path);
		
		//Begin looping through the save data
		//We are using a forloop rather than a for-each loop since the index is important
		for(int i = 0; i < list.size(); i++)
		{
			//Get the current item and set it in the section
			T item = list.get(i);
			section.set("NUM" + i, item);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> load(ConfigurationSection section)
	{
		//Construct the arraylist to return
		ArrayList<T> returnList = new ArrayList<T>();
		
		//If the section provided is null, it likely means it doesn't exist
		//Return the empty returnList if this is the case
		if(section == null) return returnList;
		
		//Generate an iterator and get all the keys
		Iterator<String> keys = section.getKeys(false).iterator();
		while(keys.hasNext())
		{
			//Get the current key and put the loaded data in the arraylist
			String key = keys.next();
			returnList.add((T)section.get(key));
		}
		
		//Return the arraylist
		return returnList;
	}
}
