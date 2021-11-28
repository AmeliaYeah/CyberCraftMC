package me.YayItsAmelia.util;

public class Quadruplet<T1,T2,T3,T4>
{
	T1 one;
	T2 two;
	T3 three;
	T4 four;
	
	public Quadruplet(T1 one, T2 two, T3 three, T4 four)
	{
		this.one = one;
		this.two = two;
		this.three = three;
		this.four = four;
	}
	
	public T1 getFirst()
	{
		return one;
	}
	
	public T2 getSecond()
	{
		return two;
	}
	
	public T3 getThird()
	{
		return three;
	}
	
	public T4 getFourth()
	{
		return four;
	}
}
