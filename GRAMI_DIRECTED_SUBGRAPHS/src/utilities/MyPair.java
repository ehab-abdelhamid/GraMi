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

package utilities;

import java.util.ArrayList;
import java.util.Iterator;

public class MyPair<A, B> {
	private A a;
	private B b;
	
	public MyPair(A a, B b)
	{
		this.a = a;
		this.b = b;
	}
	
	public A getA() {return a;}
	public B getB() {return b;}
	
	  @Override public boolean equals(Object aThat) {
		  Object t = null;
		  System.out.println(t.toString());
		    //check for self-comparison
		    if ( this == aThat ) return true;
		    //actual comparison
		    if(((Integer)this.a).intValue()==((Integer)((MyPair)aThat).getA()).intValue()) return true;
		    return false;
	  }
	  
	  public static int getIndexOf(ArrayList<MyPair<Integer, Double>> arr, int a)
	  {
		  int i = 0;
		  Iterator<MyPair<Integer, Double>> itr = arr.iterator(); 
		  while(itr.hasNext()) {
			  if(itr.next().getA().intValue()==a)
				  return i;
			  i++;
		  } 
		  return -1;
	  }

}
