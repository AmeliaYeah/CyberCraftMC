package me.YayItsAmelia.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.YayItsAmelia.util.Utils;

public abstract class GenericCommandExecutor
{
	protected final String PLAYER_NOT_FOUND_MESSAGE = "Could not locate the player. Either they are offline or you mispelled the name.";
	
	protected Command command;
	public abstract void run(CommandSender sender, String[] args);
	
	protected void showErrorMessage(CommandSender sender, String message)
	{
		//Generate the message and send it
		message = ChatColor.RED + "Cannot execute the commmand; " + message;
		sendMessage(sender, message);
		
		//Show the sender the usage aswell
		sender.sendMessage(command.getUsage().replace("<command>", command.getName()));
	}
	
	public void setCommand(Command cmd)
	{
		command = cmd;
	}
	
	protected void sendMessage(CommandSender sender, String message)
	{
		sender.sendMessage(Utils.getChatPrefix() + message);
	}
	
	protected void sendNoArgumentMessage(CommandSender sender, String argument)
	{
		showErrorMessage(sender, "There is no <" + argument + "> argument specified!");
	}
	
	protected int argumentTimeToSeconds(CommandSender sender, String arg)
	{
		//Try to parse the argument to an int (but dropping the last character, since that's the time marker)
		int parsed = 0;
		try {
			parsed = Integer.parseInt(arg.substring(0, arg.length() - 1));
		}catch(Exception e)
		{
			sender.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There was an error trying to convert your input to a number: " + e.getMessage());
			return -1;
		}
		
		//Check if the parsed number is negative or equal to zero
		if(parsed <= 0)
		{
			sender.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The time supplied is less than or equal to zero. This isn't allowed.");
			return -1;
		}
		
		//Run a switch statement on the last character
		switch(arg.substring(arg.length() - 1).toLowerCase())
		{
			case "s":
				return parsed;
			case "m":
				return parsed * 60;
			case "h":
				return parsed * 60 * 60;
			case "d":
				return parsed * 60 * 60 * 24;
			default:
				sender.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The time declaration you specified is invalid. The correct format is [time][marker]\n" + ChatColor.RESET + "Valid formats:\n" + ChatColor.DARK_GRAY + "s = seconds\nm = minutes\nh = hours\nd = days\n" + ChatColor.RESET + "Example: 16d\nThis translates to 16 days");
				return -1;
		}
	}
}
