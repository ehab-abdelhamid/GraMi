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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.ls.LSInput;

import Dijkstra.DijkstraEngine;


public class myNode 
{
	private	int ID;
	private	ArrayList<Integer> label;
	private HashMap<Integer, ArrayList<Integer>> reachableNodes; //represented by Label~nodeID, represents the outgoing nodes
	
	@Override
	public String toString() {
	
		String format="("+ID+":"+label+")";
		return format;
	}
	
	public myNode(int ID, ArrayList<Integer> label)
	{
		this.ID = ID;
		this.label = label;
	}
	
	public void addLabel(int label)
	{
		this.label.add(label);
	}
	
	public int getOutDegree(int label)
	{
		if(reachableNodes==null)
			return 0;
		if(reachableNodes.get(label)==null)
			return 0;
		return reachableNodes.get(label).size();
	}
	
	public int getID()
	{
		return ID;
	}
	public ArrayList<Integer> getLabel()
	{
		return label;
	}
	
	public void addreachableNode(myNode node,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel)
	{
		if(reachableNodes==null)
			reachableNodes= new HashMap<Integer, ArrayList<Integer>>();
		
		ArrayList<Integer> reachableLabels= node.getLabel();
		for (int i = 0; i < reachableLabels.size(); i++) 
		{
			int reachableLabel=reachableLabels.get(i);
			if(freqNodesByLabel!=null&&!freqNodesByLabel.containsKey(reachableLabel))
				continue;
			ArrayList<Integer> list=reachableNodes.get(reachableLabel);
			if(list==null)
				{
					list = new ArrayList<Integer>();
					reachableNodes.put(reachableLabel, list);
				}
			if(!list.contains(node.getID()))
				list.add(node.getID());
		}
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
	
	private int getReachableNodesSize()
	{
		int count = 0;
		if(reachableNodes==null)
			return count;
		for (Iterator<ArrayList<Integer>> iterator = reachableNodes.values().iterator(); iterator.hasNext();)
		{
			ArrayList<Integer> arr =  iterator.next();
			for (int i = 0; i < arr.size(); i++) 
			{
				count++;
			}
			
		}
		return count;
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
					addreachableNode(node,freqNodesByLabel);
			}
		}
	}
	
	/**
	 * a fast set reachable function
	 * @param graph
	 * @param freqNodesByLabel
	 */
	public void setReachableNodes_1hop(Graph graph,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel)
	{
		//get edge for each node
		IntIterator it= graph.getListGraph().getEdgeIndices(getID());
		for (; it.hasNext();) 
		{
			int edge =  it.next();
			myNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, getID()));
			ArrayList<Integer> labels = otherNode.getLabel();
			for(int i=0;i<labels.size();i++)
			{
				if(freqNodesByLabel.containsKey(labels.get(i)))
				{
					addreachableNode(otherNode,freqNodesByLabel);
				}
			}
		}
	}
	
	/**
	 * a fast set reachable function
	 * @param graph
	 * @param freqNodesByLabel
	 */
	public void setReachableNodes2(Graph graph,HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel, int shortestDistance)
	{
		Hashtable<Integer, NodeDistPair> visitedNodes = new Hashtable<Integer, NodeDistPair>();
		Vector<NodeDistPair> toBeVisited = new Vector<NodeDistPair>();
		
		toBeVisited.add(new NodeDistPair(this, 0));
		
		while(toBeVisited.size()>0)
		{
			NodeDistPair ndp = toBeVisited.elementAt(0);
			toBeVisited.remove(0);
			
			if(ndp.node.getID()!=this.getID())
				visitedNodes.put(ndp.node.getID(), ndp);
			
			//get edge for each node
			IntIterator it= graph.getListGraph().getEdgeIndices(ndp.node.getID());
			for (; it.hasNext();) 
			{
				int edge=  it.next();
				myNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, ndp.node.getID()));
				//add in order
				double newDistance = ndp.distance+Double.parseDouble(graph.getListGraph().getEdgeLabel(edge)+"");
				if(otherNode.getID()==this.getID() || newDistance>shortestDistance)
					continue;
				
				NodeDistPair nndp = new NodeDistPair(otherNode, newDistance);
				
				NodeDistPair oldNDP = visitedNodes.get(nndp.node.getID());
				if(oldNDP!=null && ndp.distance>oldNDP.distance)
					continue;
				
				int i = toBeVisited.size()-1;
				for(;i>=0;i--)
				{
					NodeDistPair temp = toBeVisited.get(i);
					if(temp.distance<nndp.distance)
					{
						i++;
						break;
					}
					if(temp.node.getID()==nndp.node.getID())
					{
						toBeVisited.remove(i);
					}
				}
				if(i==-1)
					i = 0;
				toBeVisited.insertElementAt(nndp, i);
			}		
		}
		
		//add visited nodes to the reachable nodes
		Enumeration<NodeDistPair> enum1 = visitedNodes.elements();
		while(enum1.hasMoreElements())
		{
			NodeDistPair NDP = enum1.nextElement();
			boolean add = true;
			for(int i=0;i<NDP.node.getLabel().size();i++)
			{
				if(freqNodesByLabel.get(NDP.node.getLabel().get(i))==null)
					add = false;
			}
			if(add)
				addreachableNode(NDP.node,freqNodesByLabel);
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
}

class NodeDistPair
{
	myNode node;
	double distance;
	
	public NodeDistPair(myNode node, double distance)
	{
		this.node = node;
		this.distance = distance;
	}
}
