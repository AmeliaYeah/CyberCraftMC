package me.YayItsAmelia.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.item.EnchantmentManager;
import net.milkbowl.vault.economy.EconomyResponse;

public final class Utils
{
	public static String uuidToPlayerName(UUID id)
	{
		//Try to search for a player via UUID
		OfflinePlayer player = Bukkit.getOfflinePlayer(id);
		if(player == null) return "[player not found]";
				
		//Return the player's name
		return player.getName();
	}
	
	public static List<String> getOnlinePlayerNames()
	{
		//Generate an empty list
		List<String> names = new ArrayList<String>();
		
		//Loop through all the online players and add their name to the list
		for(Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
		
		//Return the names
		return names;
	}
	
	public static Tuple<Long,String> getDateDistance(Date date1, Date date2)
	{
		//Get the milliseconds distance and convert it to seconds
		long val = (date1.getTime() - date2.getTime()) / 1000;
		
		//Format the miliseconds into the highest date unit
		String format = "seconds";
		if(val >= 60)
		{
			val /= 60;
			format = "minutes";
		}
		if(val >= 60)
		{
			val /= 60;
			format = "hours";
		}
		if(val >= 24)
		{
			val /= 24;
			format = "days";
		}
		
		//Return the value along with the format
		return new Tuple<Long,String>(val,format);
	}
	
	public static String formatDate(Date date)
	{
		//Construct the format and return the formatted date
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy @ HH:mm");
		return format.format(date);
	}
	
	//Set the command sender to null if you don't want error messages to be logged
	public static boolean economyCheck(OfflinePlayer playerOffline, @Nullable CommandSender sender)
	{
		//Message prefix
		String prefix = getChatPrefix() + ChatColor.RED;
		
		//If the server economy isn't enabled, return false
		if(!Main.economy.isEnabled())
		{
			//If the sender isn't null, send the error message. Then return false
			if(sender != null) sender.sendMessage(prefix + "Server economy isn't enabled. Contact an administrator.");
			return false;
		}
		
		//If the player currently doesn't have an account, create one
		if(!Main.economy.hasAccount(playerOffline))
		{
			//If we were unsuccessful in creating a player account, return false and notify the player
			if(!Main.economy.createPlayerAccount(playerOffline))
			{
				//Send the message and return false
				if(sender != null) sender.sendMessage(prefix + "There is no account associated with " + playerOffline.getName() + " and creation of one was unsuccessful. Contact an administrator.");
				return false;
			}
		}
		
		return true;
	}
	
	public static OfflinePlayer playerToOfflinePlayer(Player player)
	{
		return Main.instance.getServer().getOfflinePlayer(player.getUniqueId());
	}
	
	public enum TransactionType { PAY, GIVE };
	public static boolean playerTransaction(@Nonnull Player player, double amount, TransactionType type)
	{
		//Generate the message prefix aswell as the offline player instance
		String prefix = getChatPrefix() + ChatColor.RED;
		OfflinePlayer playerOffline = playerToOfflinePlayer(player);
		
		//If the amount is negative or equal to zero (nothing), return false
		if(amount <= 0)
		{
			player.sendMessage(prefix + "The amount for the transaction appears to be 0 or less (" + amount + "). Please contact an administrator (or maybe even the plugin author)");
			return false;
		}
		
		//Run the economy check and check the result
		//If the economy check failed, return false
		if(!economyCheck(playerOffline, player)) return false;
		
		//Utilize the transaction type to see if we are giving or paying money
		//Create the economy response here aswell, since this will be used for validation regarding payment/deposit
		//Also create an "action message", which will be used later
		EconomyResponse response = null;
		String actionMessage = "";
		switch(type)
		{
			case PAY:
				//If the player does not have enough money, notify them and return false
				if(!Main.economy.has(playerOffline, amount))
				{
					double moneyShort = amount - Main.economy.getBalance(playerOffline);
					player.sendMessage(prefix + "You cannot afford to do this. You are " + Main.economy.format(moneyShort) + " short.");
					return false;
				}
				
				//If they ended up at this point, perform the transaction.
				response = Main.economy.withdrawPlayer(playerOffline, amount);
				actionMessage = "subtracted from";
				break;
				
			case GIVE:
				//There's no need to do any validation checking for giving money. Yahoo!
				response = Main.economy.depositPlayer(playerOffline, amount);
				actionMessage = "added to";
				break;
		}
		
		//If the response was a success, return true.
		//If it was a failure, return false and explain the error.
		if(response.type == EconomyResponse.ResponseType.SUCCESS)
		{
			player.sendMessage(prefix + ChatColor.GREEN + "Transaction successful! " + Main.economy.format(amount) + " was " + actionMessage + " your account.");
			return true;
		}
		else
		{
			player.sendMessage(prefix + "The transaction was unsuccessful. " + response.type.toString() + ": " + response.errorMessage + ". Please contact an administrator and/or the plugin author.");
			return false;
		}
	}
	
	public static String getChatPrefix()
	{
		return ChatColor.RESET + "" + ChatColor.WHITE + "[" + ChatColor.GRAY + "Cyber" + ChatColor.DARK_GRAY + "Craft" + ChatColor.WHITE + "] " + ChatColor.RESET;
	}
	
	public static boolean itemNullCheck(ItemStack stack)
	{
		//Returns to see if the item is valid or not
		//True = item is valid
		//False = item is invalid
		return (stack != null && !stack.getType().isAir());
	}
	
	public static ItemStack createModifiedItem(Material type, @Nullable String name, @Nullable String lore, @Nullable EnchantmentManager enchants)
	{
		//Create the itemstack to return, then get the metadata of the itemstack
		ItemStack stack = new ItemStack(type);
		ItemMeta meta = stack.getItemMeta();
		
		//Set the name
		//RESET is added in the beginning to remove the italics that result in renaming an item
		if(name != null) meta.setDisplayName(ChatColor.RESET + name);
		
		//Set the lore (if it isn't null)
		if(lore != null)
		{
			//Loop through the array generated by splitting the lore with the newline (\n) character
			//Generate an empty arraylist aswell, to contain each line
			ArrayList<String> loreGenerated = new ArrayList<String>();
			for(String lorePart : lore.split("\\r?\\n"))
			{
				//Insert the "Reset" chatcolor and the "Aqua" chatcolor before the lore part
				//This is so we can replace the ugly purple italic lore with a normal text Aqua lore
				lorePart = ChatColor.RESET + "" + ChatColor.AQUA + lorePart;
				
				//Add the lore part to the generated lore arraylist
				loreGenerated.add(lorePart);
			}
			
			//Set the meta's lore to the generated lore
			meta.setLore(loreGenerated);
		}
		
		//Set the enchantments
		if(enchants != null)
		{
			//Loop through all the enchantments and add them to the metadata
			for(Tuple<Enchantment, Integer> entry : enchants.getEnchants())
			{
				//"IgnoreLevelRestriction" is set to true because it could be possible for the level to be higher than one allowed in vanilla
				meta.addEnchant(entry.getFirst(), entry.getSecond(), true);
			}
		}
		
		//Update the stack's metadata and return it
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static boolean isHackingDisabled()
	{
		return Main.instance.getConfig().getBoolean("hackingDisabled", false);
	}
	
	public static boolean isPeaceful()
	{
		//The "true" parameter means that the default value is true
		//We're returning the inverted boolean since "peaceful" is the opposite of "pvp"
		return !Main.instance.getConfig().getBoolean("pvp", true) || isHackingDisabled();
	}
	
	public static boolean canNpcsHack()
	{
		return Main.instance.getConfig().getBoolean("canNpcsHack", true) && !isHackingDisabled();
	}
}
