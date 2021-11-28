package me.YayItsAmelia.commands.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandUtil
{
	public static List<String> genericSearch(List<String> items, String search)
	{
		//Generate an empty list of the items parameter, since this is what we are going to return
		//Set the search to lowercase aswell for more versatile comparison
		List<String> returnList = new ArrayList<String>();
		search = search.toLowerCase();
		
		//Start looping through the list
		for(String item : items)
		{
			//If the current item (set to lowercase) begins with the search query, we add it to the list
			if(item.toLowerCase().startsWith(search)) returnList.add(item);
		}
		
		//Return the list
		return returnList;
	}
	
	public static String combineArgumentsIntoMessage(String[] args, int start)
	{
		//Generates the string to return
		String message = "";
		
		//Runs a for loop for the arguments starting at index "start"
		for(int i = start; i < args.length; i++)
		{
			message += args[i] + " ";
		}
		
		//Returns the message
		return message;
	}
}
