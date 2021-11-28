package me.YayItsAmelia.util;

public class Triplet<T,T1,T2>
{
	T first;
	T1 second;
	T2 third;
	
	public Triplet(T first, T1 second, T2 third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public T getFirst()
	{
		return first;
	}
	
	public T1 getSecond()
	{
		return second;
	}
	
	public T2 getThird()
	{
		return third;
	}
}
