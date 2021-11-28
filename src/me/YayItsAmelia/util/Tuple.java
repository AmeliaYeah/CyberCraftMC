package me.YayItsAmelia.util;

public class Tuple<T, T1>
{
	T item1;
	T1 item2;
	
	public Tuple(T item1, T1 item2)
	{
		this.item1 = item1;
		this.item2 = item2;
	}
	
	public T getFirst()
	{
		return item1;
	}
	
	public T1 getSecond()
	{
		return item2;
	}
}
