package me.YayItsAmelia.commands.cellphone.websites.freelancing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.YayItsAmelia.Main;
import me.YayItsAmelia.util.Tuple;

public class NameGenerator
{
	static Tuple<List<String>,List<String>> names = null;
	
	public static void initialize()
	{
		//Get the files pertaining to the first and last names
		File firstNamesFile = new File(Main.instance.getDataFolder(), "first-names.txt");
		File lastNamesFile = new File(Main.instance.getDataFolder(), "last-names.txt");
		
		//Generate the list variables by running the file handler
		List<String> firstNames = fileHandler(firstNamesFile);
		List<String> lastNames = fileHandler(lastNamesFile);
		
		//For each file, check to see if the handler returned null
		//If it did, run the file setup for the data and the file
		if(firstNames == null) firstNames = runFileSetup(initFirstNames(), firstNamesFile);
		if(lastNames == null) lastNames = runFileSetup(initLastNames(), lastNamesFile);
		
		//Save the names into the tuple
		names = new Tuple<List<String>,List<String>>(firstNames,lastNames);
	}
	
	//This method could have been a void, but we make it return the "data" parameter purely so the null verification statements above look nicer
	static List<String> runFileSetup(ArrayList<String> data, File file)
	{
		try
		{
			//Create the file
			file.createNewFile();
			
			//Create the file writer and write the joined data
			//Then, close the writer
			FileWriter writer = new FileWriter(file);
			writer.write(String.join("\n", data));
			writer.close();
			
		}catch(Exception e)
		{
			Main.instance.getLogger().warning("Something went wrong during the creation of '" + file.getName() + "': " + e.getMessage() + ". Using default values..");
		}
		
		//Return the data
		//There's no need to put this inside the try/catch block since the data is always going to be re-outputted
		return data;
	}
	
	static ArrayList<String> fileHandler(File file)
	{
		try
		{
			//Generate the return arraylist
			ArrayList<String> returnList = new ArrayList<String>();
			
			//Create the file reader and a buffered reader using the file reader
			FileReader readerRaw = new FileReader(file);
			BufferedReader reader = new BufferedReader(readerRaw);
			
			//Initialize the "currentLine" variable with a value that IS NOT null
			//Then, look through the reader's lines
			String currentLine = "";
			while(currentLine != null)
			{
				currentLine = reader.readLine();
				returnList.add(currentLine);
			}
			
			//Once everything is read, close the readers
			readerRaw.close();
			reader.close();
			
			//Return the arraylist
			return returnList;
		}catch(Exception e)
		{
			Main.instance.getLogger().info("Error while attempting to read file '" + file.getName() + "': " + e.getMessage() + ". Generating a new file..");
			return null;
		}
	}
	
	static ArrayList<String> initFirstNames()
	{
		//Create the arraylist and create the first names
		ArrayList<String> names = new ArrayList<String>();
		names.add("Alice");
		names.add("Robert");
		names.add("Stacy");
		names.add("Greg");
		names.add("Margret");
		names.add("Haley");
		names.add("Hailey");
		names.add("Adam");
		names.add("Amelia");
		names.add("Noa");
		names.add("Nathan");
		names.add("Theresa");
		names.add("Olivia");
		names.add("Oliver");
		names.add("Abby");
		names.add("Abigail");
		names.add("Natalie");
		names.add("Natalia");
		names.add("Thomas");
		names.add("Jackson");
		names.add("Miguel");
		names.add("Bethany");
		names.add("Tiffany");
		
		//Return the arraylist
		return names;
	}
	
	static ArrayList<String> initLastNames()
	{
		//Create the arraylist and create the last names
		ArrayList<String> names = new ArrayList<String>();
		
		names.add("Ross");
		names.add("Rowe");
		names.add("Adams");
		names.add("Jackson");
		names.add("Thomas");
		names.add("Rodriguez");
		
		//Return the arraylist
		return names;
	}
	
	public static String generateName()
	{
		//Get the arraylists
		List<String> firstNames = names.getFirst();
		List<String> lastNames = names.getSecond();
		
		//Create a random generator
		Random rand = new Random();
		
		//Generate the name
		return firstNames.get(rand.nextInt(firstNames.size())) + " " + lastNames.get(rand.nextInt(lastNames.size()));
	}
}
