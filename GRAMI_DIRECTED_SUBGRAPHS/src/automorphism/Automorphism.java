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

package automorphism;

import java.util.HashMap;
import java.util.Iterator;

import pruning.SPpruner;

import CSP.DFSSearch;
import CSP.Variable;
import dataStructures.HPListGraph;
import dataStructures.IntIterator;
import dataStructures.Query;
import dataStructures.myNode;

public class Automorphism <NodeType, EdgeType>
{
	
	private HPListGraph<NodeType, EdgeType> patternGraph;
	private Variable[] result;
	public Variable[] getResult() {
		return result;
	}

	private HashMap<Integer,myNode> nodes;
	private HashMap<Integer,HashMap<Integer,myNode>> nodesByLabel;
	private int resultCounter;
	
	public Automorphism(HPListGraph<NodeType, EdgeType> graph) 
	{
		patternGraph=graph;
		result= new Variable[graph.getNodeCount()];
		nodes= new HashMap<Integer, myNode>();
		nodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		
		Query qry = new Query((HPListGraph<Integer, Double>)graph);
		
		//create my nodes first !!
		for (int i = 0; i < graph.getNodeCount(); i++) 
		{
			myNode newNode= new myNode(i,(Integer)graph.getNodeLabel(i));
			nodes.put(newNode.getID(), newNode);
		}
		for (int i = 0; i < graph.getNodeCount(); i++) 
		{
			myNode currentNode= nodes.get(i);
			for (IntIterator currentEdges = graph.getEdgeIndices(i); currentEdges.hasNext();) 
			{
				int edge = currentEdges.next();
				int direction=graph.getDirection(edge, i);
				int otherNodeIndex= graph.getOtherNode(edge, i);
				myNode otherNode=nodes.get(otherNodeIndex);
				if(direction==1)
					currentNode.addreachableNode(otherNode, Double.parseDouble(graph.getEdgeLabel(edge)+""));
				else if(direction ==-1)
					otherNode.addreachableNode(currentNode, Double.parseDouble(graph.getEdgeLabel(edge)+""));
			}
		}
		//now fill by label
		for (int i = 0; i < graph.getNodeCount(); i++) 
		{
			myNode currentNode = nodes.get(i);
			
			HashMap<Integer,myNode> currentLabelNodes= nodesByLabel.get(currentNode.getLabel());
			if(currentLabelNodes==null)
			{
				currentLabelNodes= new HashMap<Integer, myNode>();
				nodesByLabel.put(currentNode.getLabel(), currentLabelNodes);
			}
			currentLabelNodes.put(currentNode.getID(), currentNode);
		}
		
		SPpruner sp = new SPpruner();
		sp.getPrunedLists(nodesByLabel, qry);
		DFSSearch df = new DFSSearch(sp,qry,-1);
		df.searchAll();
		resultCounter=df.getResultCounter();
		result=df.getResultVariables();
	}
	
	public boolean hasAutomorphisms()
	{
		if(resultCounter==1)
			return false;
		return true;
	}
	
}
