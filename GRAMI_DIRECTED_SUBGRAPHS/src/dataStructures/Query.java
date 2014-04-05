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

package dataStructures;

import java.util.ArrayList;

public class Query 
{
	private HPListGraph<Integer, Double> listGraph; 
	
	public Query(Graph g) {
		listGraph=g.getListGraph();
	}
	
	public Query(HPListGraph<Integer, Double> lsGraph) {
		listGraph=lsGraph;
	}
	
	

	public HPListGraph<Integer, Double> getListGraph() {
		return listGraph;
	}

	
	public ArrayList<ConnectedComponent> getConnectedLabels()
	{
		ArrayList<ConnectedComponent> cls = new ArrayList<ConnectedComponent>();
		
		HPListGraph<Integer, Double> hp = listGraph;
		for (int edge = hp.getEdges().nextSetBit(0); edge >= 0; edge = hp.getEdges().nextSetBit(edge + 1)) 
		{
			int nodeA,nodeB,labelA,labelB;
			
			if(hp.getDirection(edge)>=0)
			{
			nodeA= hp.getNodeA(edge); labelA=hp.getNodeLabel(nodeA);
			nodeB = hp.getNodeB(edge);labelB=hp.getNodeLabel(nodeB);
			}
			else
			{
				nodeB= hp.getNodeA(edge); labelB=hp.getNodeLabel(nodeB);
				nodeA = hp.getNodeB(edge);labelA=hp.getNodeLabel(nodeA);
			}
			
			
			ConnectedComponent cl = new ConnectedComponent(nodeA,labelA, nodeB,labelB, Double.parseDouble(listGraph.getEdgeLabel(nodeA, nodeB)+"")); 
			cls.add(cl);
		}
		return cls;
	}
}
