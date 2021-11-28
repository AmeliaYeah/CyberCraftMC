package me.YayItsAmelia.commands.cellphone.websites.freelancing;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceSiteManager.FreelanceOption;
import me.YayItsAmelia.io.ArrayListSerializer;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.util.Utils;

public class MainFreelanceCommandExecutor extends GenericCommandExecutor
{
	
	@Override
	public void run(CommandSender sender, String[] args)
	{
		//Convert the sender to a player, as this is a player-only command
		Player player = (Player)sender;
		
		//If freelancing is disabled, return immediately
		if(FreelanceSiteManager.freelancingDisabled())
		{
			showErrorMessage(player, "Freelancing is disabled on this server.");
			return;
		}
		
		//If the arguments aren't supplied, return
		if(args.length == 0)
		{
			showErrorMessage(player, "You need to supply a mode.");
			return;
		}
		
		//If the first argument is "claim", give the player their claims and return
		if(args[0].equalsIgnoreCase("claim"))
		{
			getPlayerClaims(player);
			return;
		}
		
		//Check if the arguments provided are anything apart from "create-project"
		if(!args[0].equalsIgnoreCase("create-project"))
		{
			showErrorMessage(player, "Invalid mode supplied!");
			return;
		}
		
		//Check the remainder of the arguments
		if(args.length == 1)
		{
			sendNoArgumentMessage(player, "item");
			return;
		}
		if(args.length == 2)
		{
			sendNoArgumentMessage(player, "amount");
			return;
		}
		if(args.length == 3)
		{
			sendNoArgumentMessage(player, "budget");
			return;
		}
		if(args.length == 4)
		{
			sendNoArgumentMessage(player, "time");
			return;
		}
		
		//Try to parse the material
		Material mat = null;
		try { mat = Material.valueOf(args[1].toUpperCase()); } catch(Exception e)
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There was an error converting your specified item to a material: " + e.getMessage() + ". This is likely a server or even plugin issue; contact an administrator and/or the plugin author.");
			return;
		}
		
		//Try to parse the second argument to an integer
		//Return also if the amount is greater than 64, as that will surely break something
		int amount = 0;
		try { amount = Integer.parseInt(args[2]); } catch(Exception e)
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There was an error converting the item amount to a number: " + e.getMessage());
			return;
		}
		if(amount > 64)
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Your inputted amount must be 64 or less!");
			return;
		}
		if(amount <= 0)
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You must set the item count to atleast 1");
			return;
		}
		
		//Try parsing the budget to an integer
		//Then, check if it is within the range of the budget config
		int budget = 0;
		try { budget = Integer.parseInt(args[3]); } catch(Exception e)
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There was an error converting the budget to a number: " + e.getMessage());
			return;
		}
		if(budget > FreelanceSiteManager.getMaxBudget())
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You cannot exceed the maximum budget of " + Main.economy.format(FreelanceSiteManager.getMaxBudget()) + "!");
			return;
		}
		if(budget < FreelanceSiteManager.getMinBudget())
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You cannot go below the minimum budget of " + Main.economy.format(FreelanceSiteManager.getMinBudget()) + "!");
			return;
		}
		
		//Get the seconds and return if they aren't valid
		int secs = argumentTimeToSeconds(player, args[4]);
		if(secs == -1) return;
		
		//Convert the seconds to hours and validate if they fit the min/max
		int hours = secs / 60 / 60;
		if(hours > FreelanceSiteManager.getMaxHours())
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You cannot go above " + FreelanceSiteManager.getMaxHours() + " hours!");
			return;
		}
		if(hours < FreelanceSiteManager.getMinHours())
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Your project must last for atleast " + FreelanceSiteManager.getMinHours() + " hours.");
			return;
		}
		
		//Attempt to perform a transaction on the player with the supplied budget
		//If it fails, return
		if(!Utils.playerTransaction(player, budget, Utils.TransactionType.PAY)) return;
		
		//Generate the itemstack using the material and amount
		ItemStack generated = new ItemStack(mat);
		generated.setAmount(amount);
		
		//Create the project and send the feedback to the player
		FreelancingSiteProject project = FreelancingSiteProject.generateProject(FreelanceOption.OBTAINING, player, generated, budget, hours);
		player.sendMessage(Utils.getChatPrefix() + FreelanceSiteManager.createProject(project, player));
	}
	
	void getPlayerClaims(Player player)
	{
		//Get the section and load the claim data
		ConfigurationSection playerClaims = FileManager.getGenericSection(player, FreelanceSiteManager.PLAYER_CLAIM_SECTION);
		ArrayList<ItemStack> stacks = new ArrayListSerializer<ItemStack>().load(playerClaims);
		
		//If the stacks are empty, notify the player and return
		if(stacks.size() == 0)
		{
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There are no items for you to claim!");
			return;
		}
		
		//Create a clone arraylist of the "stacks"
		//This is because modification of the stacks arraylist directly will mess with the for loop
		ArrayList<ItemStack> stacksCloned = new ArrayList<ItemStack>(stacks);
		
		//Get the player's inventory and loop through the stacks
		PlayerInventory inv = player.getInventory();
		for(int i = 0; i < stacks.size(); i++)
		{
			//Get the current stack
			ItemStack stack = stacks.get(i);
			
			//Get the first empty slot in the inventory
			//If it isn't equal to -1 (which means it exists), set it to the current stack and continue the loop
			//We also remove the FIRST element of the stacksCloned array, since the loop starts at index 0
			int firstEmpty = inv.firstEmpty();
			if(firstEmpty != -1)
			{
				inv.setItem(firstEmpty, stack);
				stacksCloned.remove(0);
				continue;
			}
			
			//If there are no more empty slots, calculate the remainder, notify the player, and break the loop
			int remainder = stacks.size() - (i + 1);
			player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Your inventory is full! There are " + remainder + " items left.");
			break;
		}
		
		//Serialize the stacksCloned arraylist (since it was to handle serialization and instances where the player's inventory was full)
		new ArrayListSerializer<ItemStack>().save(playerClaims.getCurrentPath(), stacksCloned);
	}
	
}
