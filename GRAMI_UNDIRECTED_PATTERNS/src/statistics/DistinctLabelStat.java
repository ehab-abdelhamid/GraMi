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

package statistics;

import java.awt.Point;

public class DistinctLabelStat 
{
	int numberOfPatterns;
	Point maxNode;
	Point maxEdge;
	int maxNodeIndex;
	int maxEdgeIndex;
	public DistinctLabelStat(int numberOfPatterns,Point maxNode,Point maxEdge,int maxNodeIndex,int maxEdgeIndex) 
	{
		this.numberOfPatterns=numberOfPatterns;
		this.maxNode=maxNode;
		this.maxEdge=maxEdge;
		this.maxNodeIndex=maxNodeIndex;
		this.maxEdgeIndex=maxEdgeIndex;
	}
	
	public String toString2() 
	{
		String out =""+numberOfPatterns+",";
		out+=printPoint2(maxNode,maxNodeIndex)+",";
		out+=printPoint2(maxEdge,maxEdgeIndex);
		return out;
	}
	
	private String printPoint2(Point p,int index)
	{
		String pair="";
		pair+=p.x+"-"+p.y;
		return pair;
	}
	
	@Override
	public String toString() 
	{
		String out ="{ "+numberOfPatterns+",";
		out+=printPoint(maxNode,maxNodeIndex)+",";
		out+=printPoint(maxEdge,maxEdgeIndex);
		out+=" }";
		return out;
	}
	
	private String printPoint(Point p,int index)
	{
		String pair="(";
		pair+=p.x+","+p.y+")"+":"+index;
		return pair;
	}
}
