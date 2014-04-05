/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package CSP;

public class SearchOrder 
{

	private int[] order;
	private int addCounter=0;
	private int getCounter=0;
	public SearchOrder(int size) 
	{
		order= new int[size];
		for (int i = 0; i < size; i++) 
		{
			order[i]=-1;
		}
	}
	
	public void addNext(int index)
	{
		if(addCounter>=order.length)
			{ System.out.println("ERROR IN ADDING order!!"); return;}
		order[addCounter]=index;
		addCounter++;
	}
	
	public int getNext()
	{
		if(getCounter>=order.length)
		{return -1;}
		getCounter++;
		return order[getCounter-1];
	}
	
	public void stepBack()
	{
		if(getCounter==0)
			return;
		getCounter--;
	}
	
	public void reset()
	{
		getCounter=1;
	}
	
	public int getSecondOrderValue(int index1, int index2)
	{
		int value1=-1,value2=-1;   //value represents the search order
		for (int i = 0; i < order.length; i++) 
		{
			if(order[i]==index1)
				value1=i;
			else if(order[i]==index2)
				value2=i;
		}
		
		if(value1>value2)  
			return value1;
		else
			return value2;
	}
	public int getVariableIndex(int searchOrderIndex)
	{
		return order[searchOrderIndex];
	}
	
	
}
