package me.YayItsAmelia.commands.cellphone.websites.freelancing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import me.YayItsAmelia.ui.NamedInventory;
import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.GenericCommandInventoryBuilder;
import me.YayItsAmelia.commands.cellphone.CellphoneInventoryItemBuilder;
import me.YayItsAmelia.commands.cellphone.CellphoneNotificationHandler;
import me.YayItsAmelia.io.ArrayListSerializer;
import me.YayItsAmelia.io.FileManager;
import me.YayItsAmelia.listeners.PlayerInput;
import me.YayItsAmelia.ui.ClickableInventoryItem;
import me.YayItsAmelia.ui.GenericInventoryBuilderFunctions;
import me.YayItsAmelia.ui.InteractiveInventoryCreationData;
import me.YayItsAmelia.ui.InventoryBuilder;
import me.YayItsAmelia.util.Quadruplet;
import me.YayItsAmelia.util.Quintuplet;
import me.YayItsAmelia.util.Triplet;
import me.YayItsAmelia.util.Tuple;
import me.YayItsAmelia.util.Utils;

public final class FreelanceSiteManager
{
	public static boolean freelancingDisabled()
	{
		return Main.instance.getConfig().getBoolean("disableFreelancing", false);
	}
	
	public static int maxFreelancingTasks()
	{
		return Main.instance.getConfig().getInt("freelancingMaxAmount", 4);
	}
	
	public static int getMaxBudget()
	{
		return Main.instance.getConfig().getInt("freelancingMaxBudget", 10000);
	}
	
	public static int getMinBudget()
	{
		return Main.instance.getConfig().getInt("freelancingMinBudget", 50);
	}
	
	public static int getMaxHours()
	{
		return Main.instance.getConfig().getInt("freelancingMaxHours", 168);
	}
	
	public static int getMinHours()
	{
		return Main.instance.getConfig().getInt("freelancingMinHours", 2);
	}
	
	public enum FreelanceOption { HACKING, OBTAINING };
	static boolean isFreelanceOptionEnabled(FreelanceOption option)
	{
		//Check if the option is "hacking" specifically
		//If so, and if hacking is disabled, return false
		if(option == FreelanceOption.HACKING && Utils.isHackingDisabled()) return false;
		
		//Otherwise, convert the enum to it's corresponding name and return it's config setting
		return Main.instance.getConfig().getBoolean(option.name().toLowerCase(), true);
	}
	
	static final String FREELANCE_PROJECTS_CATEGORY = "freelanceProjects";
	static final String CURRENT_RUNNING_PROJECTS_SECTION = "freelanceRunningProjects";
	public static final String PLAYER_CLAIM_SECTION = "freelanceClaim";
	static ConfigurationSection getFreelancingProjectsSection()
	{
		return FileManager.getRootSection(FREELANCE_PROJECTS_CATEGORY);
	}
	
	public static InventoryBuilder buildSite(InventoryBuilder builder, Inventory webScreen)
	{
		//Create the freelance site inventory
		Inventory site = CellphoneInventoryItemBuilder.buildWebsiteInventory("freelance.io");
		
		//Build the main icon and return the builder if there's no need to build the freelance site
		builder.addItemToBake(webScreen, 3, buildMainIcon(site));
		if(!canBuildFreelanceSite) return builder;
		
		//Build the site icons
		builder.addItemToBake(site, 2, buildFreelanceHub(Utils.createModifiedItem(Material.FILLED_MAP, ChatColor.RED + "Browse Projects", null, null)));
		builder.addItemToBake(site, 4, buildPersonalProjectsItem());
		builder.addItemToBake(site, 6, GenericCommandInventoryBuilder.produceModifiedInvOnClick(Material.MAP, ChatColor.GOLD + "Current Projects", null, new InteractiveInventoryCreationData(CellphoneInventoryItemBuilder.buildWebsiteName("freelance.io/myprojects"), 27), (Tuple<Player, NamedInventory> dataInput) ->
		{
			//Set the section
			ConfigurationSection section = FileManager.getGenericSection(dataInput.getFirst(), FREELANCE_PROJECTS_CATEGORY);
			
			//Get the player's freelancing project data
			Quintuplet<List<String>,List<String>,List<FreelancingSiteProject>,List<String>,List<Material>> data = getAllProjects(section);
			List<String> names = data.getFirst();
			List<String> lores = data.getSecond();
			List<FreelancingSiteProject> projects = data.getThird();
			List<String> keys = data.getFourth();
			List<Material> mats = data.getFifth();
			
			//Generate the grid
			return GenericInventoryBuilderFunctions.modifiedInventoryGrid(dataInput.getSecond(), names, lores, mats, (Integer i, ItemStack stack) ->
			{
				//Set the stack's amount to the required item's amount
				stack.setAmount(projects.get(i).requiredItem.getAmount());
				
				//Generate the item using the current itemstack
				return ClickableInventoryItem.generateRunOnClick(stack, (Player player) ->
				{
					//Get the required item
					ItemStack requiredItem = projects.get(i).requiredItem;
					
					//Wait for the player's input
					PlayerInput.addPlayerToWait(player, "Please hold the required item (or items) in your hand, then type 'y' when you are ready", (String message) ->
					{
						if(message == null) return;
						
						//Get the section and check if the project exists
						if(!section.contains(keys.get(i)))
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The project doesn't exist and/or expired");
							return;
						}
						
						//Check if the itemstack being held is valid
						PlayerInventory inv = player.getInventory();
						if(!(inv.getItemInMainHand().getType().equals(requiredItem.getType()) && inv.getItemInMainHand().getItemMeta().equals(requiredItem.getItemMeta())))
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The item you are holding isn't what the project requires.");
							return;
						}
						
						//Check if the item in their hand is LESS THAN the required amount
						//We do this by subtracting and checking if the remainder is greater than zero (since it means the items we're holding isn't enough)
						Integer subtracted = requiredItem.getAmount() - inv.getItemInMainHand().getAmount();
						if(subtracted > 0)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You are holding the correct item, but you aren't holding enough of them. You need " + subtracted + " more.");
							return;
						}
						
						//Check if the economy is functional
						if(!Utils.economyCheck(player, player)) return;
						
						//If "subtracted" is equal to zero, it means we don't have any items. So, we set the held item to "null"
						//If "subtracted" is less than zero, it means we had a surplus amount of items. If this is the case, we get the itemstack, set it's value to the absolute value version, and set the itemstack
						if(subtracted == 0) inv.setItemInMainHand(null);
						else
						{
							//Get the itemstack and set it's amount to the absolute value version of "subtracted"
							ItemStack held = inv.getItemInMainHand();
							held.setAmount(Math.abs(subtracted));
							
							//Re-set the item
							inv.setItemInMainHand(held);
						}
						
						//Get the project
						FreelancingSiteProject project = projects.get(i);
						
						//Get the current player's bid in the project
						double bid = 0;
						for(Entry<UUID, Double> bidEntry : project.bids.entrySet())
						{
							//If the UUID isn't the player's current UUID, continue
							if(!bidEntry.getKey().equals(player.getUniqueId())) continue;
							
							//Set the bid to the value and break
							bid = bidEntry.getValue();
							break;
						}
						
						//Give the current player their bid money, set the key, and remove the project from the section
						Utils.playerTransaction(player, bid, Utils.TransactionType.GIVE);
						String key = keys.get(i);
						section.set(key, null);
						
						//Get the author UUID and run if the player exists
						UUID playerAuthorUUID = project.authorToPlayer();
						if(playerAuthorUUID != null)
						{
							//Get the player and check if the UUID points to a valid player
							OfflinePlayer playerTarget = Bukkit.getOfflinePlayer(playerAuthorUUID);
							if(playerTarget != null)
							{
								//Get the section
								ConfigurationSection playerClaims = FileManager.getGenericSection(playerTarget, PLAYER_CLAIM_SECTION);
								
								//Create the serializer and load from the claim section
								ArrayListSerializer<ItemStack> serializer = new ArrayListSerializer<ItemStack>();
								ArrayList<ItemStack> items = serializer.load(playerClaims);
								
								//Add the required item to the items arraylist
								items.add(requiredItem);
								
								//Save the modified arraylist
								serializer.save(playerClaims.getCurrentPath(), items);
								
								//Send the notification to the player and give them the remainder (the difference between the bid and the budget)
								//We give them the remainder because, when setting up the freelance project, they pay the ENTIRE budget
								String messageNotif = "Your freelance project was successfully completed by " + ChatColor.GREEN + player.getName() + ChatColor.RESET + ".\n" + ChatColor.RED + requiredItem.getItemMeta().getDisplayName() + ChatColor.RESET + " was successfully received. Try using " + ChatColor.DARK_GRAY + "/freelance claim" + ChatColor.RESET + " to access it!";
								double bidDifference = project.budget - bid;
								
								//If the bid difference is greater than zero, deposit the money into their account and append to the message
								if(bidDifference > 0)
								{
									Main.economy.depositPlayer(playerTarget, bidDifference);
									messageNotif += "\n" + ChatColor.GOLD + "You have been given " + Main.economy.format(bidDifference) + " as the excess payment given to freelance.io";
								}
								
								//Send the notification and unregister the project
								CellphoneNotificationHandler.sendNotification(playerTarget, messageNotif, "freelance.io");
								unregisterProject(key, playerTarget);
							}
						}
					}, 'y');
				});
			}, 0);
		}));
		
		//Return the builder once the build is complete
		return builder;
	}
	
	static boolean canBuildFreelanceSite = true;
	static ClickableInventoryItem buildMainIcon(Inventory site)
	{
		//Create the itemstack for the item
		ItemStack display = Utils.createModifiedItem(Material.FISHING_ROD, ChatColor.AQUA + "" + ChatColor.BOLD + "freelance.io", "Do you want to earn money for\n" + ChatColor.GOLD + ChatColor.ITALIC + "Completing Tasks?\nDo you want to pay money for a task\n" + ChatColor.GOLD + ChatColor.ITALIC + "YOU want done?\nCheck out freelance.io!", null);
		
		//If freelancing is disabled, make the display item send the player a "disallowed" message
		//Also set the "canBuildFreelanceSite" variable to false, just so we don't have to build the actual freelance site if we don't need to
		if(freelancingDisabled())
		{
			canBuildFreelanceSite = false;
			return ClickableInventoryItem.generateRunOnClick(display, (Player player) -> { player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The ability to access the freelancing site has been disabled by the server."); });
		}
		
		return ClickableInventoryItem.generateShowInventoryOnClick(display, site);
	}
	
	static Quintuplet<String,String,FreelancingSiteProject,String,Material> getProjectInfo(ConfigurationSection section, String key)
	{
		//Load the data from the current key and convert it to a freelancing site project object
		//If the project is null, return null
		FreelancingSiteProject project = (FreelancingSiteProject)section.get(key);
		if(project == null) return null;
		
		//Check if the category isn't null and it's not disabled. If it is, return null
		if(project.option == null) return null;
		if(!isFreelanceOptionEnabled(project.option)) return null;
		
		//Get the date and check if the project is still active
		Tuple<Long,String> dateDistance = Utils.getDateDistance(project.expiresAt, new Date());
		if(dateDistance.getFirst() <= 0)
		{
			//Delete the project from the data
			FileManager.getRootSection(section.getCurrentPath()).set(key, null);
			
			//Check if the project's author was a player.
			//If they were, run the refund code
			UUID id = project.authorToPlayer();
			if(id != null)
			{
				OfflinePlayer player = Bukkit.getOfflinePlayer(id);
				if(player != null)
				{
					//Get the penalty percentage
					double penalty = (Main.instance.getConfig().getDouble("freelancingPenaltyPercentage", 20) / 100);
					
					//Give the player back the project budget, but with the penalty removed from it
					Main.economy.depositPlayer(player, project.budget - (project.budget * penalty));
					
					//Send the player a notification and unregister the project
					CellphoneNotificationHandler.sendNotification(player, "Your project for " + ChatColor.RED + project.requiredItem.getItemMeta().getDisplayName() + ChatColor.RESET + " wasn't able to be completed in time. " + Main.economy.format(penalty) + " (" + (100 - (penalty * 100)) + "% of original budget) was refunded to your account.", "freelance.io");
					unregisterProject(key, player);
				}
			}
			
			//Return null
			return null;
		}
		
		//Get the material name if the display name isn't defined
		//Otherwise, set the item lore name to the display name
		String name = project.requiredItem.getType().name();
		ItemMeta meta = project.requiredItem.getItemMeta();
		if(meta.hasDisplayName()) name = meta.getDisplayName();
		
		//Construct the lore base
		String lore = ChatColor.DARK_GRAY + "Requested by: " + project.getAuthor() + "\n" + ChatColor.GOLD + "Requested item: " + ChatColor.AQUA + name + "; " + project.requiredItem.getAmount() + "\n" + ChatColor.GREEN + "Budget: " + project.budget + "\n" + ChatColor.LIGHT_PURPLE + "Expires in " + dateDistance.getFirst() + " " + dateDistance.getSecond() + "\n" + ChatColor.RESET + "BIDS:";
		
		//Loop through the project's bids and append them to the lore
		for(Entry<UUID, Double> bid : project.bids.entrySet())
		{
			lore += "\n -" + Utils.uuidToPlayerName(bid.getKey()) + ": " + Main.economy.format(bid.getValue());
		}
		
		//Finally, construct the data and return it
		return new Quintuplet<String,String,FreelancingSiteProject,String,Material>(project.option.name(), lore, project, key,project.requiredItem.getType());
	}
	
	static Quintuplet<List<String>,List<String>,List<FreelancingSiteProject>,List<String>,List<Material>> getAllProjects(ConfigurationSection section)
	{
		//Loop through all the sections in the freelance projects category
		List<String> names = new ArrayList<String>();
		List<String> lores = new ArrayList<String>();
		ArrayList<FreelancingSiteProject> projects = new ArrayList<FreelancingSiteProject>();
		List<String> keys = new ArrayList<String>();
		List<Material> mats = new ArrayList<Material>();
		for(String key : FileManager.getSectionNames(section))
		{
			//Get the project info, and continue the loop if it is null
			Quintuplet<String,String,FreelancingSiteProject,String,Material> info = getProjectInfo(section, key);
			if(info == null) continue;
			
			//Add to the lists
			names.add(info.getFirst());
			lores.add(info.getSecond());
			projects.add(info.getThird());
			keys.add(info.getFourth());
			mats.add(info.getFifth());
		}
		
		//Once the loop is complete, generate the triplet and return it
		return new Quintuplet<List<String>,List<String>,List<FreelancingSiteProject>,List<String>,List<Material>>(names, lores, projects, keys, mats);
	}
	
	static ClickableInventoryItem buildFreelanceHub(ItemStack display)
	{
		return ClickableInventoryItem.generateShowModifiedInventoryOnClick(display, new InteractiveInventoryCreationData(CellphoneInventoryItemBuilder.buildWebsiteName("freelance.io/browse"), 27), (Tuple<Player, NamedInventory> dataInput) ->
		{
			//Get all the projects from the root section
			Quintuplet<List<String>,List<String>,List<FreelancingSiteProject>,List<String>,List<Material>> projectsRes = getAllProjects(getFreelancingProjectsSection());
			List<String> names = projectsRes.getFirst();
			List<String> lores = projectsRes.getSecond();
			List<FreelancingSiteProject> projects = projectsRes.getThird();
			List<String> keys = projectsRes.getFourth();
			List<Material> mats = projectsRes.getFifth();
			
			//Generate the inventory grid
			return GenericInventoryBuilderFunctions.modifiedInventoryGrid(dataInput.getSecond(), names, lores, mats, (Integer i, ItemStack stack) ->
			{
				//Set the stack's amount to the required item's amount
				stack.setAmount(projects.get(i).requiredItem.getAmount());
				
				//Create the clickable inventory item for the current index
				return ClickableInventoryItem.generateRunOnClick(stack, (Player player) ->
				{
					//Load the player's freelancing projects
					ArrayList<String> playerProjects = new ArrayListSerializer<String>().load(FileManager.getGenericSection(player, FREELANCE_PROJECTS_CATEGORY));
					
					//Check if the projects length is greater than or equal to the maximum limit.
					//If it is, return and explain to the player why
					if(playerProjects.size() >= maxFreelancingTasks())
					{
						player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You have reached the maximum number of concurrent freelancing tasks.");
						return;
					}
					
					//If the economy check fails, return
					//There's no need to send the player a message, since that will be handled for us
					if(!Utils.economyCheck(player, player)) return;
					
					//Get the current project
					FreelancingSiteProject project = projects.get(i);
					
					//Check if the project's bids already contain the player's UUID
					//If it does, return
					if(project.bids.containsKey(player.getUniqueId()))
					{
						player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You already placed a bid!");
						return;
					}
					
					//Ask the player for a bid
					PlayerInput.addPlayerToWait(player, "How much money would you like to bid? The project budget is " + Main.economy.format(project.budget), (String message) ->
					{
						if(message == null) return;
						
						//Get the root section
						ConfigurationSection rootSection = getFreelancingProjectsSection();
						
						//Check if the project exists
						if(!rootSection.contains(keys.get(i)))
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The project doesn't exist and/or expired");
							return;
						}
						
						//Try to parse the message into a number
						double num = 0;
						try {
							num = Double.parseDouble(message);
						}catch(Exception e)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Could not convert your message to a number: " + e.getMessage());
							return;
						}
						
						//Check if the number is less than the minimum amount
						if(num < getMinBudget())
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Your bid is lower than the minimum amount required (" + Main.economy.format(getMinBudget()) + ")");
							return;
						}
						
						//Check if the number exceeds the budget
						if(num > project.budget)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The bid you entered has exceeded the project budget.");
							return;
						}
						
						//Place the bid, notify the player, and save the project
						project.bids.put(player.getUniqueId(), num);
						player.sendMessage(Utils.getChatPrefix() + ChatColor.GREEN + "Your bid (" + Main.economy.format(num) + ") has been placed!");
						rootSection.set(keys.get(i), project);
					}, PlayerInput.NUMERIC_CHARACTERS);
				});
				
			}, 0);
		});
	}
	
	static ClickableInventoryItem buildPersonalProjectsItem()
	{
		return ClickableInventoryItem.generateShowModifiedInventoryOnClick(Utils.createModifiedItem(Material.IRON_AXE, ChatColor.BLUE + "Your Projects", null, null), new InteractiveInventoryCreationData(CellphoneInventoryItemBuilder.buildWebsiteName("freelance.io/runningprojects"), 27), (Tuple<Player,NamedInventory> dataInput) ->
		{
			//Get the player's owned projects section aswell as the root one
			//We aren't using the getAllProjects() method since the project classes aren't actually located here; only pointers are
			ConfigurationSection ownedSection = FileManager.getGenericSection(dataInput.getFirst(), CURRENT_RUNNING_PROJECTS_SECTION);
			ConfigurationSection rootSection = getFreelancingProjectsSection();
			
			//Define the lists and run the loop
			List<String> names = new ArrayList<String>();
			List<String> lores = new ArrayList<String>();
			List<FreelancingSiteProject> projects = new ArrayList<FreelancingSiteProject>();
			List<String> keys = new ArrayList<String>();
			List<Material> mats = new ArrayList<Material>();
			for(String key : FileManager.getSectionNames(ownedSection))
			{
				//Get the data for the current key by searching in the root section
				//Continue the loop if it is null
				Quintuplet<String,String,FreelancingSiteProject,String,Material> data = getProjectInfo(rootSection,key);
				if(data == null) continue;
				
				//Add the items to the lists
				names.add(data.getFirst());
				lores.add(data.getSecond());
				projects.add(data.getThird());
				keys.add(data.getFourth());
				mats.add(data.getFifth());
			}
			
			//Generate the grid
			return GenericInventoryBuilderFunctions.modifiedInventoryGrid(dataInput.getSecond(), names, lores, mats, (Integer i, ItemStack stack) ->
			{
				//Set the stack's amount to the required item's amount
				stack.setAmount(projects.get(i).requiredItem.getAmount());
				
				//Generate the item
				return ClickableInventoryItem.generateRunOnClick(stack, (Player player) ->
				{
					//Get the current project
					FreelancingSiteProject project = projects.get(i);
					
					//Begin construction of a string containing all the bidders and their values
					//Construct a hashmap containing the key as a UUID and the value as an integer
					//The hashmap is purely so the player can send a chat message with the index and it'll interpret that as the UUID (if it exists)
					String bidString = ChatColor.GOLD + "Current bids: " + ChatColor.RESET + ChatColor.BOLD + "\n";
					Object[] bids = project.bids.entrySet().toArray();
					HashMap<UUID, Integer> playerUUIDS = new HashMap<>();
					for(int j = 0; j < bids.length; j++)
					{
						//Get the current bidder
						@SuppressWarnings("unchecked")
						Entry<UUID,Double> bidder = (Entry<UUID,Double>)bids[j];
						
						//Create the display index (this is used so it appears better aesthetically to the player)
						//We increment it by 1 since the index starts at 0
						int displayIndex = j + 1;
						
						//Add the bidder to the string, then add the uuid and index to the UUIDs hashmap
						bidString += "#" + displayIndex + ": " + Utils.uuidToPlayerName(bidder.getKey()) + " (bid: " + Main.economy.format(bidder.getValue()) + ")";
						playerUUIDS.put(bidder.getKey(), displayIndex);
						
						//If the "j" index is less than (bids.length - 1), add the comma seperator
						//This is purely done so the bid string doesn't end with the seperator, which would look aesthetically displeasing
						if(j < bids.length - 1) bidString += ", \n";
					}
					
					//Get the player's chat message
					PlayerInput.addPlayerToWait(player, "Respond with the appropriate number for the bid offer you want to accept.\n" + bidString, (String message) ->
					{
						if(message == null) return;
						
						//Get the root section aswell as the key and check if the project exists
						ConfigurationSection section = getFreelancingProjectsSection();
						String key = keys.get(i);
						if(!section.contains(key))
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The project doesn't exist and/or expired");
							return;
						}
						
						//Try to parse the input into a number
						int index = 0;
						try
						{
							index = Integer.parseInt(message);
						}catch(Exception e)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "Could not convert your message into a number: " + e.getMessage());
							return;
						}
						
						//Loop through the playerUUIDs to see if there is a match with the index
						UUID id = null;
						for(Entry<UUID, Integer> possibleID : playerUUIDS.entrySet())
						{
							//Set the id and break the loop if the integer matches the index
							if(possibleID.getValue().equals(index))
							{
								id = possibleID.getKey();
								break;
							}
						}
						
						//Return if the id is still null
						if(id == null)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "You have entered an invalid index. There is no bidder carrying that same value.");
							return;
						}
						
						//Attempt to get the target player from the UUID
						//If this fails, return and notify the player
						OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(id);
						if(targetPlayer == null)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "The index is valid and points to a valid player...but they appear to not exist. This is likely a server issue; contact an administrator.");
							return;
						}
						
						//Attempt to parse the key into a UUID
						UUID parsed = null;
						try
						{
							parsed = UUID.fromString(key);
						}catch(Exception e)
						{
							player.sendMessage(Utils.getChatPrefix() + ChatColor.RED + "There was an error while converting the data key '" + key + "' into a UUID: " + e.getMessage() + ". This is most definitely a server issue; contact an administrator.");
							return;
						}
						
						//Let the target player claim the project and notify the current player that the operation was successful
						claimProject(parsed, targetPlayer);
						player.sendMessage(Utils.getChatPrefix() + ChatColor.GREEN + "You have now accepted the bid of " + targetPlayer.getName() + "!");
						
						
					}, PlayerInput.NUMERIC_CHARACTERS);
				});
			}, 0);
		});
	}
	
	public static String createProject(FreelancingSiteProject project, @Nullable OfflinePlayer player)
	{
		//Get the root section and generate the UUID
		ConfigurationSection rootSection = getFreelancingProjectsSection();
		UUID id = UUID.randomUUID();
		
		//If the player isn't null, run some extra code for them
		if(player != null)
		{
			//Get the player's owned projects section
			ConfigurationSection ownedSection = FileManager.getGenericSection(player, CURRENT_RUNNING_PROJECTS_SECTION);
			
			//Check if their owned projects are greater than or equal to the max
			//If it is, return null
			if(FileManager.getSectionNames(ownedSection).size() >= Main.instance.getConfig().getInt("freelancingMaxOwnedProjectsAmount", 2)) return ChatColor.RED + "You have exceeded the maximum amount of projects that can be currently owned at a time";
			
			//Set a pointer to the generated UUID in the owned section
			ownedSection.set(id.toString(), true);
		}
		
		//Set the project into the root section and return the UUID
		rootSection.set(id.toString(), project);
		return ChatColor.GREEN + "The project has been created!";
	}
	
	public static void claimProject(UUID key, OfflinePlayer claimer)
	{
		//Get the sections
		ConfigurationSection rootSection = getFreelancingProjectsSection();
		ConfigurationSection playerSection = FileManager.getGenericSection(claimer, FREELANCE_PROJECTS_CATEGORY);
		
		//Try to get the project
		//If it doesn't exist, return
		FreelancingSiteProject project = (FreelancingSiteProject)rootSection.get(key.toString());
		if(project == null) return;
		
		//Remove the project from the root and place it in the player
		rootSection.set(key.toString(), null);
		playerSection.set(key.toString(), project);
		
		//Send the claimer a notification
		CellphoneNotificationHandler.sendNotification(claimer, "Congratulations! Your bid for the project by " + ChatColor.GREEN + project.getAuthor() + ChatColor.RESET + " was accepted!\nCheck your current projects on the freelance.io website, and if you manage to get the item required, click it's icon for more options.", "freelance.io");
	}
	
	//Method created so, when a project is complete, it doesn't hog up space and make it impossible to create new projects
	static void unregisterProject(String key, OfflinePlayer owner)
	{
		//If the owner is null, return
		if(owner == null) return;
		
		//Get the section and set the pointer to the key to null
		ConfigurationSection ownedSection = FileManager.getGenericSection(owner, CURRENT_RUNNING_PROJECTS_SECTION);
		ownedSection.set(key, null);
	}
}
