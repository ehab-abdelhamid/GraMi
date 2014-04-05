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

package decomposer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map.Entry;

import dataStructures.DFSCode;
import dataStructures.HPListGraph;
import dataStructures.IntIterator;

public class Decomposer<NodeType, EdgeType>
{

	DFSCode<NodeType, EdgeType> code;
	HPListGraph<NodeType, EdgeType> graph;
	ArrayList<HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> mappings ;// graph ~ node mappings
	public ArrayList<HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> getMappings() {
		return mappings;
	}

	public Decomposer(DFSCode<NodeType, EdgeType> code) 
	{
		this.code=code;
		mappings = new ArrayList<HashMap<HPListGraph<NodeType,EdgeType>,ArrayList<Integer>>>();
	}
	
	public Decomposer(HPListGraph<NodeType, EdgeType> graph) 
	{
		this.graph=graph;
		mappings = new ArrayList<HashMap<HPListGraph<NodeType,EdgeType>,ArrayList<Integer>>>();
	}
	
	
	public void printResults()
	{
		for (int i = 0; i < mappings.size(); i++) 
		{
			HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>> currentMap= mappings.get(i);
			System.out.println("when removing edge "+ i);
			for (Iterator< Entry<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> iterator = currentMap.entrySet().iterator(); iterator.hasNext();) 
			{
				Entry<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>> entry  = iterator.next();
				System.out.println(entry.getKey());
				System.out.println("with mappings: ");
				ArrayList<Integer> maps= entry.getValue();
				for (int j = 0; j < maps.size(); j++) 
				{
					System.out.println(j+" : "+maps.get(j));
				}
				System.out.println("-------------");
				
			}
			System.out.println("*************************************************");
		}
		
	}
	
	public void decompose()
	{
		HPListGraph<NodeType, EdgeType> currentGraph = graph;
		
		int[] colored;
		
		//iterate over each edge!!!
		for (int i = 0; i < currentGraph.getEdgeCount(); i++) 
		{
			colored= new int[currentGraph.getNodeCount()];
			int currentEdge=i;
			HPListGraph<NodeType, EdgeType> newGraph=(HPListGraph<NodeType, EdgeType>)currentGraph.clone();
			newGraph.removeEdge(currentEdge);
			HashMap<HPListGraph<NodeType,EdgeType>, ArrayList<Integer>> currentEdgeMapping = new  HashMap<HPListGraph<NodeType,EdgeType>, ArrayList<Integer>>();
			//Now pass by each node !!
			for (int j = 0; j < newGraph.getNodeCount(); j++) 
			{
				
				HPListGraph<NodeType, EdgeType> connectedComp= new HPListGraph<NodeType, EdgeType>(); //new connected component!!
				ArrayList<Integer> mapping = new ArrayList<Integer>(); //corresponding mapping !!
				
				int nodeID = j; //start from here and search !!
				if(colored[j]==1)
					continue;
				
				Stack<Integer> DFSstack = new Stack<Integer>();
				DFSstack.push(nodeID);
				
				connectedComp.addNodeIndex(newGraph.getNodeLabel(nodeID));  
				mapping.add(nodeID);
				
				while(!DFSstack.isEmpty())
				{
					int currentNodeIDmapping=DFSstack.pop();
					if(colored[currentNodeIDmapping]==1)
						continue;
					
					colored[currentNodeIDmapping]=1;
					
					
					for (IntIterator it = newGraph.getEdgeIndices(currentNodeIDmapping); it.hasNext();) 
					{
						int edge = (int) it.next();
						int otherNodeMapping = newGraph.getOtherNode(edge,currentNodeIDmapping);
						if(colored[otherNodeMapping]==1)
							continue;
						
						//else
						int otherNode=mapping.indexOf(otherNodeMapping);
						int currentNodeID= mapping.indexOf(currentNodeIDmapping);
						if(otherNode==-1)
						{
							
							otherNode=connectedComp.getNodeCount();
							connectedComp.addNodeIndex(newGraph.getNodeLabel(otherNodeMapping));  
							mapping.add(otherNodeMapping);
						}
						
						connectedComp.addEdgeIndex(currentNodeID, otherNode, (EdgeType)"1", newGraph.getDirection(edge,currentNodeIDmapping));
						
						DFSstack.push(otherNodeMapping);
					}
				}
				if(connectedComp.getNodeCount()>1 && mapping.contains(currentGraph.getNodeCount()-1))
					currentEdgeMapping.put(connectedComp, mapping);
			}
			mappings.add(currentEdgeMapping);
		}
	
	}
	
	
}
