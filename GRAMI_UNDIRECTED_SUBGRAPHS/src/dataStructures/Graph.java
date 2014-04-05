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


import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import utilities.CombinationGenerator;
import utilities.Settings;

//import Temp.SubsetReference;
import Dijkstra.DijkstraEngine;


public class Graph 
{

	public final static int NO_EDGE = 0;
	private HPListGraph<Integer, Double> m_matrix;
	private int nodeCount=0;
	private ArrayList<myNode> nodes;
	private HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel;
	private HashMap<Integer, HashMap<Integer,myNode>> nodesByLabel;
	private ArrayList<Integer> sortedFreqLabels;//sorted lebels by frequency (Descending)
	
	private ArrayList<Point> sortedFreqLabelsWithFreq;
	
	private HashMap<Double, Integer> edgeLabelsWithFreq;
	private ArrayList<Double> freqEdgeLabels;
	
	private int freqThreshold;
	public int getFreqThreshold() {
		return freqThreshold;
	}

	private int m_id;
	
	public Graph(int ID, int freqThresh) 
	{
		
		sortedFreqLabels= new ArrayList<Integer>();
		sortedFreqLabelsWithFreq = new ArrayList<Point>();
		
		m_matrix= new HPListGraph<Integer, Double>();
		m_id=ID;
		nodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		
		freqNodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		nodes= new ArrayList<myNode>();
				
		edgeLabelsWithFreq = new HashMap<Double, Integer>();
		freqEdgeLabels = new ArrayList<Double>();
		
		freqThreshold=freqThresh;
		
		if(StaticData.hashedEdges!=null)
		{
			StaticData.hashedEdges = null;
			System.out.println(StaticData.hashedEdges.hashCode());//throw an exception if more than one graph was created
		}
		StaticData.hashedEdges = new HashMap<String, HashMap<Integer, Integer>[]>();
	}
	
	public ArrayList<Integer> getSortedFreqLabels() {
		return sortedFreqLabels;
	}
	
	public ArrayList<Double> getFreqEdgeLabels() {
		return this.freqEdgeLabels;
	}

	public HashMap<Integer, HashMap<Integer,myNode>> getFreqNodesByLabel()
	{
		return freqNodesByLabel;
	}
	
	public void loadFromFile(String fileName) throws Exception
	{		
		String text = "";
		final BufferedReader rows = new BufferedReader(new FileReader(new File(fileName)));
		
		int numberOfNodes=0;

		String tempLine = null;//a temporary variable to store the last read node line
		
		if(Settings.multipleAtts)
		{
			
			HashMap<String, Integer> multiAttLabels= new HashMap<String, Integer>();
			int multiAttLabelsCounter=100;
			
			int counter=0;
			boolean isNewNode;
			String line;
			rows.readLine();
			while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
				final String[] parts = line.split("\\s+");
				final int index = Integer.parseInt(parts[1]);
				final int label = Integer.parseInt(parts[2]);
				isNewNode=false;
				if(index==counter)
				{
					counter++;
					isNewNode=true;
				}
				if (index != counter - 1) {
					throw new ParseException("The node list is not sorted", counter);
				}
				
				myNode n;
				if(isNewNode)
				{
					addNode(label);
					ArrayList<Integer> nLabels= new ArrayList<Integer>();
					nLabels.add(label);
					n = new myNode(numberOfNodes, nLabels);
					nodes.add(n);
					numberOfNodes++;
				}
				else
				{
					n=nodes.get(index);
					
					//add the new label combinations!!
					ArrayList<Integer> prevLabels=n.getLabel();
					ArrayList<String> newCombs=CombinationGenerator.getNewCombinations(prevLabels, label);
					for (int j = 0; j < newCombs.size(); j++) 
					{
						String newComb=newCombs.get(j);
						Integer labelValue= multiAttLabels.get(newComb);
						if(labelValue==null)
						{
							labelValue=multiAttLabelsCounter;
							multiAttLabels.put(newComb, labelValue);
							multiAttLabelsCounter++;
						}
						//add the node label info !!!
						HashMap<Integer,myNode> tmp = nodesByLabel.get(labelValue);
						if(tmp==null)
						{
							tmp = new HashMap<Integer,myNode>();
							nodesByLabel.put(labelValue, tmp);
						}
						tmp.put(n.getID(), n);
						
					}
					
					//add the node label
					n.addLabel(label);
					
				}
				
				HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
				if(tmp==null)
				{
					tmp = new HashMap<Integer,myNode>();
					nodesByLabel.put(label, tmp);
				}
				tmp.put(n.getID(), n);
				
			}
			nodeCount=numberOfNodes;
			
			if(Settings.multipleAtts)
				for (Iterator<Entry<String, Integer>> iterator = multiAttLabels.entrySet().iterator(); iterator.hasNext();) 
				{
					Entry<String, Integer> entry = iterator.next();
					System.out.println("MultiAtt "+entry.getKey()+" with label "+entry.getValue()+" and size "+nodesByLabel.get(entry.getValue()).size());	
				}
			
			
		}
		else
		{
			String line;
			int counter = 0;
			rows.readLine();
			while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
				final String[] parts = line.split("\\s+");
				final int index = Integer.parseInt(parts[1]);
				final int label = Integer.parseInt(parts[2]);
				counter++;
				
				if (index != counter - 1) {
					throw new ParseException("The node list is not sorted", counter);
				}
								
				addNode(label);
				ArrayList<Integer> nLabels= new ArrayList<Integer>();
				nLabels.add(label);
				myNode n = new myNode(numberOfNodes, nLabels);
				nodes.add(n);
				HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
				if(tmp==null)
				{
					tmp = new HashMap<Integer,myNode>();
					nodesByLabel.put(label, tmp);
				}
				
				tmp.put(n.getID(), n);
				numberOfNodes++;
			}
			nodeCount=numberOfNodes;
			
			tempLine = line;
		}
		
		//load edges
		String line;
		
		//use the first edge line
		if(tempLine.charAt(0)=='e')
			line = tempLine;
		else
			line = rows.readLine();
		
		if(line!=null)
		{
			do
			{
				final String[] parts = line.split("\\s+");
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				final double label = Double.parseDouble(parts[3]);
				addEdge(index1, index2, label);
			} while((line = rows.readLine()) !=null && (line.charAt(0) == 'e'));
		}
		
		//prune infrequent edge labels
		for (Iterator<  java.util.Map.Entry< Double,Integer> >  it= this.edgeLabelsWithFreq.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Double,Integer > ar =  it.next();			
			if(ar.getValue().doubleValue()>=freqThreshold)
			{
				this.freqEdgeLabels.add(ar.getKey());
			}
		}
		
		//now prune the infrequent nodes
		
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();			
			if(ar.getValue().size()>=freqThreshold)
			{
				//sortedFreqLabels.add(ar.getKey());
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());
			}
		}
		
		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) 
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
		}

		//prune frequent hashedEdges
		Vector toBeDeleted = new Vector();
		Set<String> s = StaticData.hashedEdges.keySet();
		for (Iterator<String>  it= s.iterator(); it.hasNext();) 
		{
			String sig =  it.next();
			HashMap[] hm = StaticData.hashedEdges.get(sig);
			if(hm[0].size()<freqThreshold || hm[1].size()<freqThreshold)
			{
				toBeDeleted.addElement(sig);
			}
			else
			{
				;
			}
		}
		Enumeration<String> enum1 = toBeDeleted.elements();
		while(enum1.hasMoreElements())
		{
			String sig = enum1.nextElement();
			StaticData.hashedEdges.remove(sig);
		}

		rows.close();		
	}
	
	public void printFreqNodes()
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next(); //label ~ list of nodes
			
			System.out.println("Freq Label: "+ar.getKey()+" with size: "+ar.getValue().size());
		}
	}
	
	public void setShortestPaths(DijkstraEngine dj)
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				dj.execute(node.getID(),null);
				node.setReachableNodes(dj, freqNodesByLabel);
			}
		}
	}
	
	//1 hop distance for the shortest paths
	public void setShortestPaths_1hop()
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				node.setReachableNodes_1hop(this, freqNodesByLabel);
			}
		}
	}
	
	public myNode getNode(int ID)
	{
		return nodes.get(ID);
	}
	
	public HPListGraph<Integer, Double> getListGraph()
	{
		return m_matrix;
	}
	public int getID() {
		return m_id;
	}
	
	public int getDegree(int node) {

		return m_matrix.getDegree(node);
	}
		
	public int getNumberOfNodes()
	{
		return nodeCount;
	}
	
	 
	public int addNode(int nodeLabel) {
		return m_matrix.addNodeIndex(nodeLabel);
	}
	public int addEdge(int nodeA, int nodeB, double edgeLabel) 
	{
		Integer I = edgeLabelsWithFreq.get(edgeLabel); 
		if(I==null)
			edgeLabelsWithFreq.put(edgeLabel, 1);
		else
			edgeLabelsWithFreq.put(edgeLabel, I.intValue()+1);
		
		//add edge frequency
		ArrayList<Integer> labelsA = nodes.get(nodeA).getLabel();
		ArrayList<Integer> labelsB = nodes.get(nodeB).getLabel();
		for(Iterator<Integer> it1 = labelsA.iterator(); it1.hasNext();)
		{
			int labelA = it1.next();
			for(Iterator<Integer> it2 = labelsB.iterator(); it2.hasNext();)
			{
				int labelB = it2.next();
				String hn;
				if(labelA<labelB)
					hn = labelA+"_"+edgeLabel+"_"+labelB;
				else
					hn = labelB+"_"+edgeLabel+"_"+labelA;
				HashMap<Integer,Integer>[] hm = StaticData.hashedEdges.get(hn); 
				if(hm==null)
				{
					hm = new HashMap[2];
					hm[0] = new HashMap();
					hm[1] = new HashMap();
					
					StaticData.hashedEdges.put(hn, hm);
				}
				else
				{}
				if(labelA<labelB)
				{
					hm[0].put(nodeA, nodeA);
					hm[1].put(nodeB, nodeB);
				}
				else if(labelA==labelB)
				{
					hm[0].put(nodeA, nodeA);
					hm[1].put(nodeA, nodeA);
					hm[0].put(nodeB, nodeB);
					hm[1].put(nodeB, nodeB);
				}
				else
				{
					hm[0].put(nodeB, nodeB);
					hm[1].put(nodeA, nodeA);
				}
			}
		}
		
		return m_matrix.addEdgeIndex(nodeA, nodeB, edgeLabel, 0);
	}
}
