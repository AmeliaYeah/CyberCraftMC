package me.YayItsAmelia.commands.cellphone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.util.Utils;

public final class CellphoneNotificationHandler
{
	static final String dataName = "notifications";
	
	static boolean isNotificationEnabled(OfflinePlayer player)
	{
		//If the config file specifies that the player requires a phone, and they don't have a phone, we return
		return !(Main.instance.getConfig().getBoolean("notificationsRequirePhone", false) && !FileManager.getSaveData(player).getBoolean("hasPhone", false));
	}
	
	public static void sendNotification(OfflinePlayer playerOffline, String message, @Nullable String sender)
	{
		//Check if we can send a notification
		//Return if we cannot
		if(!isNotificationEnabled(playerOffline)) return;
		
		//If the sender is null, we set it to the default
		if(sender == null)
		{
			sender = "Server";
		}
		
		//Sanitize the message
		//Then, generate the message containing the base message along with the extra metadata
		message = FileManager.sanitize(message);
		message = message + "\n" + ChatColor.DARK_GREEN + "Sent at: " + Utils.formatDate(new Date());
		
		//Get the amount of messages the player has
		int amount = FileManager.getGenericSection(playerOffline, dataName).getKeys(false).size();
		
		//Get the section pertaining to the current notification
		//Then, set it's data to the message aswell as setting the sender to the sender
		ConfigurationSection notifications = FileManager.getGenericSection(playerOffline, dataName + FileManager.getPathSeperator() + amount);
		notifications.set("data", message);
		notifications.set("sender", sender);
		
		//Check if the player is currently online
		Player onlinePlayer = playerOffline.getPlayer();
		if(onlinePlayer != null)
		{
			//Send them a chat message saying they got a notification
			onlinePlayer.sendMessage(Utils.getChatPrefix() + ChatColor.WHITE + "You just got a notification; check your cellphone using /cellphone");
		}
	}
	
	public static List<ConfigurationSection> getAllNotifications(OfflinePlayer player)
	{
		return FileManager.getSections(FileManager.getGenericSection(player, dataName));
	}
	
	public static String getNotification(OfflinePlayer player, String sender)
	{
		//Get the section and return if the notification can't be found
		ConfigurationSection notifs = FileManager.getGenericSection(player, dataName);
		if(!notifs.isConfigurationSection(sender)) return "There are no notifications from: " + sender;
		
		//Get the notification data and delete the notification
		String data = notifs.getConfigurationSection(sender).getString("data");
		notifs.set(sender, null);
		
		//Return the data
		return data;
	}
}
