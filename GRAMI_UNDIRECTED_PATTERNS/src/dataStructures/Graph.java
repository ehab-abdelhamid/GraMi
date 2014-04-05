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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
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
	private HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel;//= new HashMap<Integer, ArrayList<Node>>();
	private HashMap<Integer, HashMap<Integer,myNode>> nodesByLabel;//= new HashMap<Integer, ArrayList<Node>>();
	private ArrayList<Integer> sortedFreqLabels; //sorted by frequency !!! Descending......
	
	private ArrayList<Point> sortedFreqLabelsWithFreq;

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
		
		freqThreshold=freqThresh;
	}
	
	public ArrayList<Integer> getSortedFreqLabels() {
		return sortedFreqLabels;
	}

	public HashMap<Integer, HashMap<Integer,myNode>> getFreqNodesByLabel()
	{
		return freqNodesByLabel;
	}
	
	public void loadFromFile(String fileName) throws Exception
	{		
		String text = "";
		final BufferedReader bin = new BufferedReader(new FileReader(new File(fileName)));
		File f = new File(fileName);
		FileInputStream fis = new FileInputStream(f);
		byte[] b = new byte[(int)f.length()];
		int read = 0;
		while (read < b.length) {
		  read += fis.read(b, read, b.length - read);
		}
		text = new String(b);
		final String[] rows = text.split("\n");

		// read graph from rows
		// nodes
		int i = 0;
		int numberOfNodes=0;
		
		if(Settings.multipleAtts)
		{
			
			HashMap<String, Integer> multiAttLabels= new HashMap<String, Integer>();
			int multiAttLabelsCounter=100;
			
			int counter=0;
			boolean isNewNode;
			for (i = 1; (i < rows.length) && (rows[i].charAt(0) == 'v'); i++) {
				final String[] parts = rows[i].split("\\s+");
				final int index = Integer.parseInt(parts[1]);
				final int label = Integer.parseInt(parts[2]);
				isNewNode=false;

				if(index==counter)
				{
					counter++;
					isNewNode=true;
				}
				if (index != counter - 1) {
					throw new ParseException("The node list is not sorted", i);
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
					//now add the new label combinations!!
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
						//now add the node label info !!!
						HashMap<Integer,myNode> tmp = nodesByLabel.get(labelValue);
						if(tmp==null)
						{
							tmp = new HashMap<Integer,myNode>();
							nodesByLabel.put(labelValue, tmp);
						}
						tmp.put(n.getID(), n);
						
					}
					//now the node label alone
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
				}
		}
		else
		{
			for (i = 1; (i < rows.length) && (rows[i].charAt(0) == 'v'); i++) {
				final String[] parts = rows[i].split("\\s+");
				final int index = Integer.parseInt(parts[1]);
				final int label = Integer.parseInt(parts[2]);

				if (index != i - 1) {
					throw new ParseException("The node list is not sorted", i);
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
		}
		
		// edges
		for (; (i < rows.length) && (rows[i].charAt(0) == 'e'); i++) {
			final String[] parts = rows[i].split("\\s+");
			final int index1 = Integer.parseInt(parts[1]);
			final int index2 = Integer.parseInt(parts[2]);
			final double label = Double.parseDouble(parts[3]);
			addEdge(index1, index2, label);
		}
		
		//now prune the infrequent nodes
		
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();			
			if(ar.getValue().size()>=freqThreshold)
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());
			}
		}
		
		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) 
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
			System.out.println("index: "+j+" Label: "+sortedFreqLabels.get(j) );
			
		}
		
		bin.close();		
	}
	
	public void printFreqNodes()
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next(); //label ~ list of nodes
			
			System.out.println("Freq Label: "+ar.getKey()+" with size: "+ar.getValue().size());
		}
	}
	
	public void setShortestPaths(DijkstraEngine dj, int shortestDistance)
	{
		for (Iterator<  java.util.Map.Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();) 
		{
			java.util.Map.Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
			int counter=0;
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				dj.execute(node.getID(),null);
				System.out.println(counter++);
				
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
			int counter=0;
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
				System.out.println(counter++);
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
		return m_matrix.addEdgeIndex(nodeA, nodeB, edgeLabel, 0);
	}
}
