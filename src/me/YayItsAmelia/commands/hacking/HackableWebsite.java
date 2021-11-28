package me.YayItsAmelia.commands.hacking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.YayItsAmelia.io.FileManager;

public class HackableWebsite implements ConfigurationSerializable
{
	private static ConfigurationSection websitesSection = null;
	private static List<String> domains = new ArrayList<String>();
	public static HackableWebsite generate()
	{
		
	}
	
	public static HackableWebsite getSite(String domain)
	{
		//Return the site
		return (HackableWebsite)websitesSection.get(domain);
	}
	
	public static void initialize()
	{
		//Set the websites section
		websitesSection = FileManager.getRootSection("hackable-sites");
		
		
	}
	
	
	
	@Override
	public Map<String, Object> serialize()
	{
		//Generate the hashmap
		HashMap<String, Object> map = new HashMap<>();
		
		//Return the hashmap
		return map;
	}
	
	private HackableWebsite() {}
	public HackableWebsite(Map<String,Object> data)
	{
		
	}
}
