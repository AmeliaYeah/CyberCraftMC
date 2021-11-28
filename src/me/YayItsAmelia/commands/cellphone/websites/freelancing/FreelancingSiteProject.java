package me.YayItsAmelia.commands.cellphone.websites.freelancing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceSiteManager.FreelanceOption;
import me.YayItsAmelia.util.Utils;

public class FreelancingSiteProject implements ConfigurationSerializable
{
	final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();
	
	public FreelanceOption option = null;
	private String author = "";
	public ItemStack requiredItem = null;
	public double budget = 0;
	public Date expiresAt = new Date();
	public HashMap<UUID,Double> bids = new HashMap<UUID,Double>();
	
	public UUID authorToPlayer()
	{
		//Try to parse the UUID
		//If the parse is unsuccessful, return null, as it's likely not a UUID
		try
		{
			return UUID.fromString(author);
		}catch(Exception e)
		{
			return null;
		}
	}
	
	//Designed so, if a player creates the freelance project, their name will show up rather than their UUID
	public String getAuthor()
	{
		//Check if the UUID is valid
		//If it isn't, return the author string
		UUID parsed = authorToPlayer();
		if(parsed == null) return author;
		
		//Convert the UUID to a player name
		return Utils.uuidToPlayerName(parsed);
	}
	
	@Override
	public Map<String, Object> serialize()
	{
		//Create the hashmap
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		//Set the values
		map.put("category", option.name());
		map.put("author", author);
		map.put("required-item", requiredItem);
		map.put("budget", budget);
		map.put("expiresAt", DATE_FORMAT.format(expiresAt));
		map.put("bids", bids);
		
		//Return the map
		return map;
	}
	
	private FreelancingSiteProject(){}
	public static FreelancingSiteProject generateProject(FreelanceOption option, OfflinePlayer player, ItemStack requiredItem, double budget, int hoursDuration)
	{
		//Create the freelancing site project
		FreelancingSiteProject project = new FreelancingSiteProject();
		
		//Set the values
		project.option = option;
		project.requiredItem = requiredItem;
		project.budget = budget;
		
		//If the player is specified, set the author to their UUID
		//If they aren't, set the author to a randomly generated name
		if(player != null) project.author = player.getUniqueId().toString();
		else project.author = NameGenerator.generateName();
		
		//Generate a calender to do date math
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, hoursDuration);
		project.expiresAt = cal.getTime();
		
		//Return the project
		return project;
	}
	
	@SuppressWarnings("unchecked")
	public FreelancingSiteProject(Map<String,Object> data)
	{
		try
		{
			//Attempt to retrieve the items
			option = FreelanceOption.valueOf((String)data.get("category"));
			author = (String)data.get("author");
			requiredItem = (ItemStack)data.get("required-item");
			budget = (double)data.get("budget");
			expiresAt = DATE_FORMAT.parse((String)data.get("expiresAt"));
			bids = (HashMap<UUID,Double>)data.get("bids");
		}catch(Exception e)
		{
			Main.instance.getLogger().warning("Could not load freelancing site project: " + e.getMessage() + ". Some values may not be set.");
		}
	}
}
