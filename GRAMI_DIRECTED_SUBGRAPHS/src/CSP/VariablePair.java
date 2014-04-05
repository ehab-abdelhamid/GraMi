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

public class VariablePair 
{
	public Variable v1;
	public Variable v2;
	public double edgeLabel;
	
	public VariablePair(Variable v1,Variable v2, double edgeLabel) 
	{
		this.v1=v1;
		this.v2=v2;
		this.edgeLabel = edgeLabel;
	}
	
	public String getString()
	{
		String x = v1.getID()+"-"+edgeLabel+"-"+v2.getID();
		return x;
	}
	
	public int getMinValuesLength()
	{
		if(v1.getListSize()<v2.getListSize())
			return v1.getListSize();
		else
			return v2.getListSize();
	}
}
