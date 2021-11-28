package me.YayItsAmelia.commands.ftp;

import java.util.List;
import java.util.Random;

import me.YayItsAmelia.Main;

public final class IPAddressCreator
{
	public static String generateIP(List<String> alreadyUsed)
	{
		//Generate the IP string to return
		String ip = "";
		
		//Since an IPv4 address has 4 digits, we run a for loop 4 times
		for(int i = 0; i < 4; i++)
		{
			//Generate an integer array with the length of the alreadyUsed list
			int[] digits = new int[alreadyUsed.size()];
			
			//Loop through the alreadyUsed list
			for(int j = 0; j < digits.length; j++)
			{
				//Get the current IP
				String ipCurrentRaw = alreadyUsed.get(j);
				
				//Split the current IP using the period character (since each IPv4 digit is seperated by one)
				//Then, check if the split length is greater than the current index.
				//If it does happen to be less than the index, we can just continue the iteration and log why. There's no need to stop execution of the function or anything crazy like that.
				//The period character also begins with two front slashes, since we're dealing with escape characters (In other words: I have no idea why it only works like this, but it works, so yeah)
				String[] split = ipCurrentRaw.split("\\.");
				if(split.length - 1 < i) //We are using length - 1 since array lengths are exclusive
				{
					Main.instance.getLogger().warning("IP address " + ipCurrentRaw + " does not contain enough periods and digit " + (i + 1) + " has been skipped (IPAddressCreator; generateIP)");
					continue;
				}
				
				//Get the current digit. Then, try to parse it in a try-catch.
				String currentDigitUnparsed = split[i];
				try {
					digits[j] = Integer.parseInt(currentDigitUnparsed);
				}catch(Exception e)
				{
					Main.instance.getLogger().warning("Attempted to parse " + currentDigitUnparsed + " in IP address " + ipCurrentRaw + ". Returned " + e.getMessage() + ". IP digit skipped. (IPAddressCreator; generateIP)");
					continue;
				}
			}
			
			//Now that the digits array has been created, we run the recursive IP generation function and append the result to the IP string
			ip += recursiveIPGeneration(digits) + ".";
		}
		
		//Return the IP, but with the last character trimmed. This is because, since we add a period after each digit, the IP will end with a period
		//This is most definitely not going to look visually appealing, so this fixes that
		return ip.substring(0, ip.length() - 1);
	}
	
	static int recursiveIPGeneration(int[] notAllowed)
	{
		//Generate a new random
		Random rand = new Random();
		
		//Generate an integer that ranges from 0 to 255
		//We use 256 since the outer bound is exclusive
		int generated = rand.nextInt(256);
		
		//Loop through the unallowed integers
		for(int i : notAllowed)
		{
			//If the integer matches with the generated random, we perform it again
			if(i == generated) return recursiveIPGeneration(notAllowed);
		}
		
		//If we generated an IP segment that is allowed, we return it
		return generated;
	}
	
	public static int generatePort()
	{
		//Simple enough. Generates a new random and outputs a value between zero and 65535
		//The max is 65536 since it is exclusive
		return new Random().nextInt(65536);
	}
}
