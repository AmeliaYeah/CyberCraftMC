package me.YayItsAmelia.util;

import java.util.ArrayList;
import java.util.HashMap;

//Class designed to ensure safe comparison checking, mainly in the case of modified classes not comparing as they should
//For example: if we change the title of an inventory named "Hello" to "Goodbye", the comparison check would return false
public class ModifiedClonesList<T>
{
	//The "modified" hashmap contains two values: the original (the key is the cloned) and the data associated with the clone
	//It's okay to leave this as null, but it can be used in cases where clones need to have some type of metadata attached
	HashMap<T,Tuple<T, Object>> modified = new HashMap<T,Tuple<T, Object>>();
	ArrayList<T> originals = new ArrayList<T>();
	
	public void addOriginal(T original)
	{
		//If we already added it to the original, return
		if(originals.contains(original)) return;
		
		//Add it to the original
		originals.add(original);
	}
	
	public void registerClone(T original, T clone, Object data)
	{
		//Adds the entry to the modified clones list
		//Also adds the original to the originals to ensure we can retrieve it later on
		modified.put(clone, new Tuple<T, Object>(original, data));
		addOriginal(original);
	}
	
	public Tuple<T, Object> locateClone(T cloned, boolean delete)
	{
		//Check if the modified hashmap contains the value
		if(modified.containsKey(cloned))
		{
			//If it contains the value: obtain it, remove it from the hashmap if the parameter says so (to conserve memory), and return it
			Tuple<T, Object> obtained = modified.get(cloned);
			if(delete) modified.remove(cloned);
			return obtained;
		}
		
		//Return null if it wasn't found
		return null;
	}
	
	public Tuple<T, Object> convertCloneToOriginal(T cloned, boolean delete)
	{
		//Check if the instance was cloned
		//If it was and it doesn't return null, we return that value
		Tuple<T, Object> located = locateClone(cloned, delete);
		if(located != null) return located;
		
		//If the hashmap doesn't contain the value, check to see if the originals arraylist does
		//This is in the case that "cloned" is actually just an unmodified and uncloned item
		if(originals.contains(cloned))
		{
			//Return the value
			//We don't remove it this time since "originals" is meant to be static and permanent rather than dynamic
			return new Tuple<T, Object>(cloned, null);
		}
		
		//If these fail, return null;
		return null;
	}
	
	public ArrayList<T> getOriginals()
	{
		return originals;
	}
}
