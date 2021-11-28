package me.YayItsAmelia.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.ftp.FtpCommandExecutor;
import me.YayItsAmelia.commands.util.CommandUtil;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;
import net.md_5.bungee.api.ChatColor;

public class RevokeFTPServerExecutor extends GenericCommandExecutor
{
	
	public void run(CommandSender sender, String[] args)
	{
		//Checks if there are no arguments, and returns with an error message if that is the case
		if(args.length == 0)
		{
			sendNoArgumentMessage(sender, "player");
			return;
		}
		
		//Get the player
		Player player = Bukkit.getPlayer(args[0]);
		if(player == null)
		{
			showErrorMessage(sender, PLAYER_NOT_FOUND_MESSAGE);
			return;
		}
		
		//Check if the server name was specified
		if(args.length == 1)
		{
			sendNoArgumentMessage(sender, "server");
			return;
		}
		
		//Get the reason and server name
		//Since this is an optional parameter, we don't return if it isn't specified. We instead default to null.
		String reason = null;
		String serverName = args[1];
		if(args.length >= 3) reason = CommandUtil.combineArgumentsIntoMessage(args, 2);
		
		//Get the remover. By default this is null (the server), but if a player ran the argument then we use their name
		String remover = null;
		if(sender instanceof Player && !AdminCommandUtil.areAdminsAnonymous()) remover = ((Player)sender).getName();
		
		//Attempt to delete the targetted player's serer and store the result
		Tuple<Boolean,String> res = FtpCommandExecutor.deleteServerForPlayer(player, serverName, remover, reason);
		ChatColor resColor = ChatColor.GREEN;
		if(!res.getFirst())
		{
			resColor = ChatColor.RED;
		}
		
		//Send the response message
		sender.sendMessage(Utils.getChatPrefix() + resColor + res.getSecond());
	}

}
