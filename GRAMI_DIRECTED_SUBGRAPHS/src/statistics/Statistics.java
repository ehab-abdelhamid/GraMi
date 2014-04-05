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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import dataStructures.HPListGraph;

public class Statistics<NodeType, EdgeType>
{
	ArrayList<HPListGraph<NodeType, EdgeType>> result;
	int numberOfPatterns;
	int maxDistinctLabels;
	int maxDistinctLabelsIndex;
	Point maxNode;
	int maxNodeIndex;
	Point maxEdge;
	int maxEdgeIndex;
	HashMap<Integer, DistinctLabelStat> distinctLabels;
	
	public Statistics(ArrayList<HPListGraph<NodeType, EdgeType>> result) 
	{
		this.result=result;
		distinctLabels= new HashMap<Integer, DistinctLabelStat>();
		computeStats();
	}
	
	private void computeStats()
	{
		//Global
		numberOfPatterns=result.size();
		maxDistinctLabels=-1;
		maxNode= new Point(-1,-1);
		maxEdge= new Point(-1,-1);
		//Local
		HashMap<Integer, Integer> distinctLabelsSize= new HashMap<Integer, Integer>();
		HashMap<Integer, Point> distinctLabelsMaxNode= new HashMap<Integer, Point>();
		HashMap<Integer, Integer> distinctLabelsMaxNodeIndex= new HashMap<Integer, Integer>();
		HashMap<Integer, Point> distinctLabelsMaxEdge= new HashMap<Integer, Point>();
		HashMap<Integer, Integer> distinctLabelsMaxEdgeIndex= new HashMap<Integer, Integer>();
		for (int i = 0; i < result.size(); i++) 
		{
			HPListGraph<NodeType, EdgeType> graph = result.get(i);
			//Global!!
			if(maxNode.x<graph.getNodeCount())
			{
				maxNode.x=graph.getNodeCount();
				maxNode.y=graph.getEdgeCount();
				maxNodeIndex=i;
			}
			if(maxEdge.y<graph.getEdgeCount())
			{
				maxEdge.y=graph.getEdgeCount();
				maxEdge.x=graph.getNodeCount();
				maxEdgeIndex=i;
			}
			int numOfDistinct=getNumOfDistinctLabels(graph);
			if(maxDistinctLabels<numOfDistinct)
			{
				maxDistinctLabels=numOfDistinct;
				maxDistinctLabelsIndex=i;
			}
			
			//now go local !!
			Integer localSize=distinctLabelsSize.get(numOfDistinct);
			if(localSize==null)
			{
				distinctLabelsSize.put(numOfDistinct, 1);
				int nodeCount=graph.getNodeCount();
				int edgeCount=graph.getEdgeCount();
				distinctLabelsMaxNode.put(numOfDistinct, new Point(nodeCount,edgeCount));
				distinctLabelsMaxNodeIndex.put(numOfDistinct, i);
				distinctLabelsMaxEdge.put(numOfDistinct, new Point(nodeCount,edgeCount));
				distinctLabelsMaxEdgeIndex.put(numOfDistinct, i);
			}
			else
			{
				distinctLabelsSize.put(numOfDistinct, localSize+1);
				int nodeCount=graph.getNodeCount();
				int edgeCount=graph.getEdgeCount();
				
				Point previousMaxNode= distinctLabelsMaxNode.get(numOfDistinct);  //compare with x
				Point previousMaxEdge= distinctLabelsMaxEdge.get(numOfDistinct);  //compare with y
				
				if(nodeCount>previousMaxNode.x)
					{
					distinctLabelsMaxNode.put(numOfDistinct, new Point(nodeCount,edgeCount));
					distinctLabelsMaxNodeIndex.put(numOfDistinct, i);
					}
				
				if(edgeCount>previousMaxEdge.y)
					{
					distinctLabelsMaxEdge.put(numOfDistinct, new Point(nodeCount,edgeCount));
					distinctLabelsMaxEdgeIndex.put(numOfDistinct, i);
					}
			}
		}
		
		
		for (Iterator<Integer> iterator = distinctLabelsSize.keySet().iterator(); iterator.hasNext();) 
		{
			Integer numOfDistinct =  iterator.next();
			int distinctSize= distinctLabelsSize.get(numOfDistinct);
			Point localMaxNode= distinctLabelsMaxNode.get(numOfDistinct);
			Point localMaxEdge= distinctLabelsMaxEdge.get(numOfDistinct);
			int indexlocalMaxNode=distinctLabelsMaxNodeIndex.get(numOfDistinct);
			int indexlocalMaxEdge=distinctLabelsMaxEdgeIndex.get(numOfDistinct);
			
			DistinctLabelStat stat = new DistinctLabelStat(distinctSize,localMaxNode,localMaxEdge,indexlocalMaxNode,indexlocalMaxEdge);
			distinctLabels.put(numOfDistinct, stat);
		}
		
		
	}
	
	private int getNumOfDistinctLabels(HPListGraph<NodeType, EdgeType> list)
    {
        HashSet<Integer> difflabels= new HashSet<Integer>();
        for (int i = 0; i < list.getNodeCount(); i++) 
        {
            int label= (Integer)list.getNodeLabel(i);
            if(!difflabels.contains(label))
                difflabels.add(label);
        }
        
        return difflabels.size();
    }
	
	public String toString2() 
	{
		
		String out=""+numberOfPatterns+",";
		out+=maxDistinctLabels+",";
		out+=printPoint2(maxNode,maxNodeIndex)+",";
		out+=printPoint2(maxEdge,maxEdgeIndex)+",";
		
		for (Iterator<Integer> iterator = distinctLabels.keySet().iterator(); iterator.hasNext();) 
		{
			Integer numOfDistinct =  iterator.next();
			DistinctLabelStat stat= distinctLabels.get(numOfDistinct);
			out+=numOfDistinct+","+stat.toString2()+",";
		}
		
		
		return out;
	}
	
	@Override
	public String toString() 
	{
		
		String out=""+numberOfPatterns+", ";
		out+=maxDistinctLabels+", ";
		out+=printPoint(maxNode,maxNodeIndex)+", ";
		out+=printPoint(maxEdge,maxEdgeIndex)+", ";
		
		for (Iterator<Integer> iterator = distinctLabels.keySet().iterator(); iterator.hasNext();) 
		{
			Integer numOfDistinct =  iterator.next();
			DistinctLabelStat stat= distinctLabels.get(numOfDistinct);
			out+=numOfDistinct+":"+stat.toString()+", ";
		}
		
		
		return out;
	}
	
	
	private String printPoint(Point p,int index)
	{
		String pair="(";
		pair+=p.x+","+p.y+")"+":"+index;
		return pair;
	}
	
	private String printPoint2(Point p,int index)
	{
		String pair="";
		pair+=p.x+"-"+p.y;
		return pair;
	}
	
}
