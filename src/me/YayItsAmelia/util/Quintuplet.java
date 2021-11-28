package me.YayItsAmelia.util;

public class Quintuplet<T1,T2,T3,T4,T5>
{
	T1 one;
	T2 two;
	T3 three;
	T4 four;
	T5 five;
	
	public Quintuplet(T1 one, T2 two, T3 three, T4 four, T5 five)
	{
		this.one = one;
		this.two = two;
		this.three = three;
		this.four = four;
		this.five = five;
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
	
	public T5 getFifth()
	{
		return five;
	}
}
