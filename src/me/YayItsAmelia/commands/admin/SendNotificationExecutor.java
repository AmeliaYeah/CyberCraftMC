package me.YayItsAmelia.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.cellphone.CellphoneNotificationHandler;
import me.YayItsAmelia.commands.util.CommandUtil;
import me.YayItsAmelia.util.Utils;
import net.md_5.bungee.api.ChatColor;

public class SendNotificationExecutor extends GenericCommandExecutor
{
	
	public void run(CommandSender sender, String[] args)
	{
		//Checks if there are arguments
		if(args.length == 0)
		{
			sendNoArgumentMessage(sender, "player");
			return;
		}
		
		//Try and get the target player
		//If it is null, it means they don't exist, and we return explaining that
		Player targetPlayer = Bukkit.getPlayer(args[0]);
		if(targetPlayer == null)
		{
			showErrorMessage(sender, PLAYER_NOT_FOUND_MESSAGE);
			return;
		}
		
		//Checks if a valid message was provided
		if(args.length < 2)
		{
			sendNoArgumentMessage(sender, "message");
			return;
		}
		
		//Generates the sender's name
		//The default is null, which would be the server. If the command sender is a player, we set it to the player's name.
		String senderName = null;
		if(sender instanceof Player && !AdminCommandUtil.areAdminsAnonymous()) senderName = ((Player)sender).getName();
		
		//Send the notification and inform the sender that we did it
		CellphoneNotificationHandler.sendNotification(targetPlayer, CommandUtil.combineArgumentsIntoMessage(args, 1), senderName);
		sender.sendMessage(Utils.getChatPrefix() + ChatColor.GREEN + "Notification sent!");
	}

}
