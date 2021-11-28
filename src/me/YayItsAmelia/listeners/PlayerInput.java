package me.YayItsAmelia.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public class PlayerInput implements Listener
{
	public static final Character[] NUMERIC_CHARACTERS = new Character[]{'0','1','2','3','4','5','6','7','8','9'};
	
	public static PlayerInput instance = new PlayerInput();
	private static HashMap<Player, Tuple<Consumer<String>, Character[]>> waitingPlayers = new HashMap<>();
	
	public static void addPlayerToWait(Player player, String chatMessage, Consumer<String> func, Character... allowedCharacters)
	{
		//Send the player the formatted chat message and construct the tuple
		player.sendMessage(Utils.getChatPrefix() + chatMessage + "\n" + ChatColor.GOLD + "Please type the input in chat");
		Tuple<Consumer<String>, Character[]> tuple = new Tuple<>(func, allowedCharacters);
		
		//Put the tuple into the waitingPlayers map and check to see if an old value was present
		//If it WAS present, execute the tuple's function with a null string
		Tuple<Consumer<String>, Character[]> oldTuple = waitingPlayers.put(player, tuple);
		if(oldTuple != null) oldTuple.getFirst().accept(null);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		//Get the player
		Player player = e.getPlayer();
		
		//Get the data for the player and return if the player isn't waiting to chat
		Tuple<Consumer<String>, Character[]> data = waitingPlayers.get(player);
		if(data == null) return;
		
		//Remove the data from the waiting players so we know the player is being handled
		//Cancel the event aswell, since player input doesn't need to show up in chat.
		waitingPlayers.remove(player);
		e.setCancelled(true);
		
		//Get the chat message and seperate the function
		String message = e.getMessage();
		Consumer<String> func = data.getFirst();
		Character[] allowedCharacters = data.getSecond();
		
		//If the length of the allowed characters are greater than zero, run the validation check
		if(allowedCharacters.length == 0)
		{
			//Generate an arraylist of characters and add all the array elements into it
			//This is because we can make the char validation more simplistic using contains(), a method not available with a primitive array
			ArrayList<Character> chars = new ArrayList<Character>();
			Collections.addAll(chars, allowedCharacters);
			
			//Define a success boolean and a "disallowedChar" variable
			boolean checkSuccessful = true;
			char disallowedChar = '0';
			
			//Convert the message to lowercase and loop through the characters
			for(char ch : message.toLowerCase().toCharArray())
			{
				//If the current character is not contained in the chars array, set the success boolean to false and break
				//Also set the "disallowedChar" variable so we know what char was marked as invalid
				if(!chars.contains(ch))
				{
					checkSuccessful = false;
					disallowedChar = ch;
					break;
				}
			}
			
			//Once the loop is done, check if the success boolean is still true
			//If it isn't, send the player an error message and return
			if(!checkSuccessful)
			{
				//Convert the allowed characters to a string
				String allowedCharsString = "";
				for(int i = 0; i < allowedCharacters.length; i++)
				{
					//Initialize the seperator and set it ONLY if this isn't the last index (since there's no need to seperate the final character)
					String seperator = "";
					if(i != allowedCharacters.length - 1) seperator = ", ";
					
					//Add the character along with the seperator to the char string
					allowedCharsString += allowedCharacters[i] + seperator;
				}
				
				//Run the function with a parameter of "null" to signify a fail
				func.accept(null);
				
				//Send the message and return
				player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The character '" + disallowedChar + "' isn't allowed.\n" + ChatColor.RESET + "Valid characters are:\n" + ChatColor.GOLD + allowedCharsString);
				return;
			}
		}
		
		//If the characters are validated and everything is okay, run the function
		func.accept(message);
	}
}
