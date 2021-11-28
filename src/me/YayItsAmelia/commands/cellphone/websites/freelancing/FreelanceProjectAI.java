package me.YayItsAmelia.commands.cellphone.websites.freelancing;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.commands.cellphone.websites.freelancing.FreelanceSiteManager.FreelanceOption;
import me.YayItsAmelia.item.ItemManager;

public final class FreelanceProjectAI
{
	static long min;
	static long max;
	static ArrayList<FreelanceOption> options = new ArrayList<FreelanceOption>();
	public static void initialize()
	{
		//Get the config and set the min/max
		FileConfiguration config = Main.instance.getConfig();
		min = config.getInt("freelancingRandomGenerationFrequencyMin", 2);
		max = config.getInt("freelancingRandomGenerationFrequencyMax", 40);
		
		//Convert the min and max from minutes to seconds to ticks
		min = min * 60 * 20;
		max = max * 60 * 20;
		
		//Loop through the freelance options and add it to the options if it is allowed from the config
		for(FreelanceOption option : FreelanceOption.values()) if(FreelanceSiteManager.isFreelanceOptionEnabled(option)) options.add(option);
		
		//Generate the project
		generateProject();
	}
	
	static void generateProject()
	{
		//Calculate a random value between the minimum and the maximum
		//Then, execute a bukkit task that will wait that time before running
		//"Max" is incremented by 1 since the outer bound is exclusive
		long calculatedTime = ThreadLocalRandom.current().nextLong(min, max + 1);
		Main.instance.getServer().getScheduler().runTaskLater(Main.instance, () ->
		{
			//Generate the itemstack required for the project
			ItemStack stack = null;
			
			//Get a random option and run a switch statement on it
			FreelanceOption option = options.get(ThreadLocalRandom.current().nextInt(options.size()));
			switch(option)
			{
				//The simplest case
				//Generates a random item from the item manager and generates a random amount
				case OBTAINING:
					stack = new ItemStack(ItemManager.getRandomItem());
					stack.setAmount(ThreadLocalRandom.current().nextInt(1,65)); //This is 65 rather than 64 because the outer bound is exclusive
					break;
					
				//If the option was not found, print to the console and re-run the loop
				default:
					Main.instance.getLogger().info("The freelance option '" + option.name() + "' doesn't have a corresponding switch event attached. This is a plugin issue most likely caused from it simply not being implemented; contact the plugin author.");
					generateProject();
					return;
			}
			
			//Generate the extra project variables
			double budget = ThreadLocalRandom.current().nextDouble(FreelanceSiteManager.getMinBudget(), FreelanceSiteManager.getMaxBudget() + 1);
			int hours = ThreadLocalRandom.current().nextInt(FreelanceSiteManager.getMinHours(), FreelanceSiteManager.getMaxHours() + 1);
			
			//Generate the project instance and register it
			FreelancingSiteProject project = FreelancingSiteProject.generateProject(option, null, stack, budget, hours);
			FreelanceSiteManager.createProject(project, null);
			
			//Run this current method again so recursion is created and projects won't just be created one time throughout the server's runtime
			generateProject();
			
		}, calculatedTime);
	}
}
