package me.YayItsAmelia.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.ftp.FtpCommandExecutor;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;
import net.md_5.bungee.api.ChatColor;

public class GiveFTPServerExecutor extends GenericCommandExecutor
{
	
	public void run(CommandSender sender, String[] args)
	{
		//If there aren't any arguments, return
		if(args.length == 0)
		{
			sendNoArgumentMessage(sender, "player");
			return;
		}
		
		//Checks if the player is valid
		Player player = Bukkit.getPlayer(args[0]);
		if(player == null)
		{
			showErrorMessage(sender, PLAYER_NOT_FOUND_MESSAGE);
			return;
		}
		
		//Checks if the second argument isn't defined
		if(args.length == 1)
		{
			sendNoArgumentMessage(sender, "size");
			return;
		}
		
		//Try to parse the 2nd argument
		FtpCommandExecutor.ServerSize size = null;
		try {
			size = FtpCommandExecutor.ServerSize.valueOf(args[1].toUpperCase()); //toUpperCase is used since the enum values are all uppercase
		}catch(Exception e)
		{
			showErrorMessage(sender, "Error while getting server size: " + e.getMessage());
			return;
		}
		
		//Create the giver
		//Null (server) is the default, but if it is being run by the player we set it to their name
		String giver = null;
		if(sender instanceof Player && !AdminCommandUtil.areAdminsAnonymous()) giver = ((Player)sender).getName();
		
		//Attempt to create a server for the player and check if it was successful
		//If it wasn't successful, notify the command executor
		Tuple<Boolean,String> serverCreationResult = FtpCommandExecutor.createServerForPlayer(player, size, giver);
		if(serverCreationResult.getFirst()) sender.sendMessage(Utils.getChatPrefix() + ChatColor.GREEN + "Server successfully added!");
		else sender.sendMessage(Utils.getChatPrefix() + "Server creation for the player was unsuccessful. The command was successfully run, but the server creation wasn't.\nThe response for the player reads:\n" + ChatColor.RED + serverCreationResult.getSecond());
	}
	
}
