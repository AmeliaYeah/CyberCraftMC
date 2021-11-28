package me.YayItsAmelia.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.YayItsAmelia.Main;

public final class FileManager
{
	static final String fileName = "data.yml";
	static final char pathSeperator = '|';
	
	public static char getPathSeperator()
	{
		return pathSeperator;
	}
	
	public static String sanitize(String old)
	{
		//Sanitize the string so there are no path seperator objects
		old = old.replace(pathSeperator, '/');
		
		return old;
	}
	
	static File file = null;
	static YamlConfiguration config = new YamlConfiguration();
	
	public static boolean initialize()
	{
		//Create a try-catch that returns false if an error occured
		//This is so, when executed from the main class, it will know if it should disable the plugin or not.
		try
		{
			//Sets the file and creates it along with the directory if it doesn't already exist
			file = new File(Main.instance.getDataFolder(), fileName);
			if(!file.exists())
			{
				file.getParentFile().mkdir();
				file.createNewFile();
			}
			
			//Set the configuration options before loading
			//This was done after the configuration file was loaded and it essentially broke the data
			config.options().pathSeparator(pathSeperator);
			
			//Loads the YamlConfiguration
			config.load(file);
			
		}catch(Exception e)
		{
			Main.instance.getLogger().severe("An error occured during file initialization: '" + e.getMessage() + "'. Quitting...");
			return false;
		}
		
		//Initializes the save-loop runnable
		initializeRunnable();
		
		//Returns true if initialization was successful
		return true;
	}
	
	public static void initializeMainConfig()
	{
		//Set the main config's configuration options
		Main.instance.getConfig().options().pathSeparator(pathSeperator);
		
		//Get the config YML and check if it exists
		File configYML = new File(Main.instance.getDataFolder(), "config.yml");
		if(!configYML.exists())
		{
			//Try to save the default config and load it into the configuration
			//If it doesn't succeed, error to the console
			try
			{
				Main.instance.saveDefaultConfig();
				Main.instance.getConfig().load(configYML);
			}catch(Exception e)
			{
				Main.instance.getLogger().severe("Error while instantiating the config.yml file: " + e.getMessage() + ". Please contact the plugin author.");
			}
		}
	}
	
	static void initializeRunnable()
	{
		//Saves the configuration data every 40 ticks (2 seconds)
		new BukkitRunnable()
		{

			@Override
			public void run()
			{
				saveConfiguration();
			}
			
		}.runTaskTimerAsynchronously(Main.instance, 40, 40);
	}
	
	public static void saveConfiguration()
	{
		//Create a try-catch so errors can occur without breaking the plugin
		try
		{
			//Saves the data
			config.save(file);
		}catch(Exception e)
		{
			//Notify the console
			Main.instance.getLogger().severe("An error occured while trying to save the plugin data: " + e.getMessage());
		}
	}
	
	public static ConfigurationSection getRootSection(String section)
	{
		//If there is no section and it doesn't exist, create it
		if(!config.isConfigurationSection(section))
		{
			config.createSection(section);
		}
				
		//Return the config section
		return config.getConfigurationSection(section);
	}
	
	public static ConfigurationSection getGenericSection(OfflinePlayer player, String section)
	{
		// Get the save data
		ConfigurationSection saveData = FileManager.getSaveData(player);

		// Check if the configuration section is valid. If it isn't, create it
		if (!saveData.isConfigurationSection(section)) {
			saveData.createSection(section);
		}

		// Return the configuration section
		return saveData.getConfigurationSection(section);
	}
	
	public static ConfigurationSection getSaveData(OfflinePlayer player)
	{
		//Get the location pertaining to the player's save data and load the section
		String playerID = "players" + pathSeperator + player.getUniqueId().toString();
		return getRootSection(playerID);
	}
	
	public static List<String> getSectionNames(ConfigurationSection section)
	{
		//Generate the return value
		ArrayList<String> returnVal = new ArrayList<String>();
		
		//If the section is null, return the empty list
		if(section == null) return returnVal;
		
		//Generate an iterator and place all the values in the arraylist
		Iterator<String> it = section.getKeys(false).iterator();
		while(it.hasNext())
		{
			returnVal.add(it.next());
		}
		
		//Return the arraylist
		return returnVal;
	}
	
	public static List<ConfigurationSection> getSections(ConfigurationSection section)
	{
		//Get all the keys and generate an empty configuration section list
		List<String> keys = FileManager.getSectionNames(section);
		ArrayList<ConfigurationSection> sections = new ArrayList<ConfigurationSection>();
				
		//Loop through the keys and add to the sections array
		for(String key : keys)
		{
			//Generate the path and store the obtained configuration section in the array
			String path = section.getCurrentPath() + pathSeperator + key;
			sections.add(config.getConfigurationSection(path));
		}
				
		//Return the sections
		return sections;
	}
	
}
