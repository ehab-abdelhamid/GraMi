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

import java.awt.List;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.ls.LSInput;

import Dijkstra.DijkstraEngine;


public class myNode 
{
	private	int ID;
	private	int label;
	private int[] shortestPaths;	
	private HashMap<Integer, ArrayList<Integer>> reachableNodes; //represented by Label~nodeID, represents the outgoing nodes
	
	private HashMap<Integer, ArrayList<Integer>> reachedBYNodes; //represented by Label~nodeID, represents the ingoing nodes
	
	@Override
	public String toString() {
	
		String format="("+ID+":"+label+")";
		return format;
	}
	
	public myNode(int ID, int label)
	{
		this.ID = ID;
		this.label = label;
	}
	
	public int getOutDegree(int label)
	{
		if(reachableNodes==null)
			return 0;
		if(reachableNodes.get(label)==null)
			return 0;
		return reachableNodes.get(label).size();
	}
	public int getinDegree(int label)
	{
		if(reachedBYNodes==null)
			return 0;
		if(reachedBYNodes.get(label)==null)
			return 0;
		return reachedBYNodes.get(label).size();
	}
	
	public int getID()
	{
		return ID;
	}
	public int getLabel()
	{
		return label;
	}
	
	public void addreachableNode(myNode node)
	{
		if(reachableNodes==null)
			reachableNodes= new HashMap<Integer, ArrayList<Integer>>();
		
		ArrayList<Integer> list=reachableNodes.get(node.getLabel());
		if(list==null)
			{
				list = new ArrayList<Integer>();
				reachableNodes.put(node.getLabel(), list);
			}
		if(!list.contains(node.getID()))
			list.add(node.getID());
		node.addreachedBYNodes(this);
		
	}
	
	
	private void addreachedBYNodes(myNode node)
	{
		if(reachedBYNodes==null)
			reachedBYNodes= new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> list=reachedBYNodes.get(node.getLabel());
		if(list==null)
			{
				list = new ArrayList<Integer>();
				reachedBYNodes.put(node.getLabel(), list);
			}
		if(!list.contains(node.getID()))
			list.add(node.getID());
	}
	
	public void printOutReachableNodes()
	{
		if(reachableNodes==null)
			return;
		for (Iterator<ArrayList<Integer>> iterator = reachableNodes.values().iterator(); iterator.hasNext();)
		{
			ArrayList<Integer> arr =  iterator.next();
			for (int i = 0; i < arr.size(); i++) 
			{
				System.out.println("Node: "+ID+" is within reach of Node "+arr.get(i));
			}
			
		}
		
	}
	
	public void setReachableNodes(DijkstraEngine dj,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel)
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			HashMap<Integer,myNode> tmp = ar.getValue();

			for (Iterator<myNode> iterator = tmp.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				if(ID==node.getID())
					continue;
				double dist=dj.getShortestDistance(node.getID());
				if(dist!=Double.MAX_VALUE )
					addreachableNode(node);
			}
		}
	}
	
	/**
	 * fast set reachable function
	 * @param graph
	 * @param freqNodesByLabel
	 */
	public void setReachableNodes_1hop(Graph graph,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel)
	{
		//get edge for each node
		IntIterator it= graph.getListGraph().getOutEdgeIndices(getID());
		for (; it.hasNext();) 
		{
			int edge =  it.next();
			myNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, getID()));
			if(freqNodesByLabel.containsKey(otherNode.getLabel()))
					addreachableNode(otherNode);
		}
	}
	
	public boolean hasReachableNodes()
	{
		if(reachableNodes==null)
			return false;
		else
			return true;
	}
	
	public boolean isWithinTheRangeOf(int NodeIndex,int nodeLabel)
	{
		if(reachableNodes.get(nodeLabel)==null)
			return false;
		return reachableNodes.get(nodeLabel).contains(NodeIndex);
	}
	
	 public ArrayList<Integer> getRechableWithNodeIDs(int label)
	 {
		 if(reachableNodes==null) return new ArrayList<Integer>();
		 return reachableNodes.get(label);
	 }
	 
	 public HashMap<Integer, ArrayList<Integer>> getReachableWithNodes()
	 {
		 return reachableNodes;
	 }
	 
	 public HashMap<Integer, ArrayList<Integer>> getReachableByNodes()
	 {
		 return reachedBYNodes;
	 }
	 
	 public ArrayList<Integer> getRechableByNodeIDs(int label)
	 {
		 if(reachedBYNodes==null) return new ArrayList<Integer>();
		 return reachedBYNodes.get(label);
	 }
	
}
