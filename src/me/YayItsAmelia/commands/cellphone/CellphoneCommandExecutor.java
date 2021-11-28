package me.YayItsAmelia.commands.cellphone;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandExecutor;
import me.YayItsAmelia.commands.GenericCommandsListener;
import me.YayItsAmelia.commands.GenericInventoryCommandExecutor;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.ui.ClickableInventoryItemList;
import me.YayItsAmelia.util.ModifiedClonesList;
import me.YayItsAmelia.util.Utils;
import net.md_5.bungee.api.ChatColor;

public class CellphoneCommandExecutor extends GenericInventoryCommandExecutor
{
	public CellphoneCommandExecutor()
	{
		//Build the inventory list and run the superclass constructor
		super(new CellphoneInventoryItemBuilder().build());
	}

	ModifiedClonesList<Inventory> inventories = new ModifiedClonesList<Inventory>();
	ClickableInventoryItemList itemList = null;
	
	Inventory mainScreen = null;

	@Override
	public void run(CommandSender send, String[] args)
	{
		//Get the player
		Player player = (Player)send;
		
		//Get the current player's data and check if they have a phone
		//"False" is the default value
		ConfigurationSection playerData = FileManager.getSaveData(player);
		if(!playerData.getBoolean("hasPhone", false))
		{
			//Check if the configuration file allows for cellphone access fees
			double cellCost = Main.instance.getConfig().getDouble("phoneCost", 60);
			if(cellCost != -1)
			{
				//If the player wasn't able to pay the money, return
				if(!Utils.playerTransaction(player, cellCost, Utils.TransactionType.PAY)) return;
			}
			
			//If we got here and the transaction was successful (if we needed to do one), give them access to the phone
			playerData.set("hasPhone", true);
			
			//Begin construction of the notification message
			String greetingMessage = ChatColor.GRAY + Main.instance.getConfig().getString("registerPhoneMessage","The greeting message has not been set.");
			String message = "Greetings, " + ChatColor.DARK_GREEN + player.getName() + ChatColor.WHITE + "!\n" + greetingMessage + ChatColor.WHITE + "\nThank you for your purchase";
			
			//Send the notification and return as, aesthetically it's more pleasing if the phone didn't open
			CellphoneNotificationHandler.sendNotification(player, message, null);
			return;
		}
		
		super.run(send, args);
	}
	
}
