package me.YayItsAmelia.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.cellphone.CellphoneNotificationHandler;
import me.YayItsAmelia.commands.cellphone.websites.ServerFarmSiteBuilder;
import me.YayItsAmelia.commands.ftp.FtpCommandExecutor;
import net.md_5.bungee.api.ChatColor;

public class SetFTPServerFlagsExecutor extends GenericCommandExecutor
{
	@Override
	public void run(CommandSender sender, String[] args)
	{
		//Check if there are no arguments
		if(args.length == 0)
		{
			sendNoArgumentMessage(sender, "player");
			return;
		}
		
		//Get the player and check if it's valid
		Player player = Bukkit.getPlayer(args[0]);
		if(player == null)
		{
			showErrorMessage(sender, PLAYER_NOT_FOUND_MESSAGE);
			return;
		}
		
		//Check if the server argument was specified
		if(args.length == 1)
		{
			sendNoArgumentMessage(sender, "server");
			return;
		}
		
		//Check if the specified server is valid
		if(!FtpCommandExecutor.isServerValid(player, args[1]))
		{
			showErrorMessage(sender, "The server specified doesn't exist");
			return;
		}
		
		//Check if the upgrade name argument was specified
		if(args.length == 2)
		{
			sendNoArgumentMessage(sender, "upgradeName");
			return;
		}
		
		//If the upgrade name is invalid, return
		if(!ServerFarmSiteBuilder.getUpgradeNames().contains(args[2]))
		{
			showErrorMessage(sender, "The upgrade name specified doesn't exist");
			return;
		}
		
		//Check if the method was specified
		if(args.length == 3)
		{
			sendNoArgumentMessage(sender, "mode");
			return;
		}
		
		//Run the switch statement using the mode specified
		//Using the modes, we set the "revoke" parameter designed for use in executing the flag method
		//We also specify a "flagString", which will be used later for generating the notification
		boolean revoke = false;
		String flagString = "";
		switch(args[3])
		{
			case "add":
				//If the flag already exists, return and show the error message
				if(FtpCommandExecutor.hasFlag(player, args[1], args[2]))
				{
					showErrorMessage(sender, "Upgrade is already applied to the specified server");
					return;
				}
				
				//Otherwise, set the revoke parameter to false (since we aren't revoking) and break
				revoke = false;
				flagString = "installed";
				break;
			case "revoke":
				//If the flag does NOT exist, return and show the error message (since we can't revoke what isn't there)
				if(!FtpCommandExecutor.hasFlag(player, args[1], args[2]))
				{
					showErrorMessage(sender, "The specified upgrade does not exist!");
					return;
				}
				
				//Otherwise, set the revoke parameter to TRUE and break
				revoke = true;
				flagString = "revoked";
				break;
			default:
				showErrorMessage(sender, "Invalid mode");
				return;
		}
		
		//Run the flag set command
		FtpCommandExecutor.setFlag(player, args[1], args[2], revoke);
		
		//Set the sender
		String senderName = "Server";
		if(sender instanceof Player && !AdminCommandUtil.areAdminsAnonymous()) senderName = ((Player)sender).getName();
		
		//Construct the message and send the notification to the player
		String message = "Your server (" + ChatColor.AQUA + args[1] + ChatColor.RESET + ") just got the upgrade '" + ChatColor.GOLD + args[2] + ChatColor.RESET + "' " + flagString + " by " + ChatColor.GREEN + senderName;
		CellphoneNotificationHandler.sendNotification(player, message, senderName);
	}
}
