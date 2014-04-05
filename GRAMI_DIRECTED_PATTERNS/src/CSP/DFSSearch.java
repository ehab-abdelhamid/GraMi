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

import java.awt.Point;
//import java.math.BigDecimal;
//import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Map.Entry;

import automorphism.Automorphism;

import pruning.SPpruner;

import statistics.TimedOutSearchStats;
import utilities.DfscodesCache;
import utilities.Settings;
import utilities.Util;

import dataStructures.ConnectedComponent;
import dataStructures.HPListGraph;
import dataStructures.IntIterator;
import dataStructures.Query;
import dataStructures.myNode;
import decomposer.Decomposer;

public class DFSSearch
{
	
    class StopTask extends TimerTask {
        public void run() {
        	if(Settings.LimitedTime)
        	{
        		System.out.format("Time's up!");
        		stopSearching();
        	}
        }
    }
	
    
   
	
	//private ConstraintGraph cGraph;
	private Variable[] variables;
	private Variable[] result;
	private int resultCounter=0;
	private HashSet<Integer> visitedVariables;
	private SearchOrder sOrder;
	private int minFreqThreshold;
	private Timer timer;
	private Query qry;
	private BigInteger numberOfIterations;
	private BigDecimal worst;
	private final BigDecimal weight = new BigDecimal(Settings.approxEpsilon);
	private BigInteger finalWeight;

	public static int COSTTHRESHOLD=1;
	
	private volatile boolean isStopped=false;
	
	private HashMap<Integer, HashSet<Integer>> nonCandidates;
	
	
	public HashMap<Integer, HashSet<Integer>> getNonCandidates() {
		return nonCandidates;
	}
	
	public DFSSearch(ConstraintGraph cg,int minFreqThreshold,HashMap<Integer, HashSet<Integer>> nonCands) 
	{
		if(!Settings.CACHING)
			nonCandidates=(HashMap<Integer, HashSet<Integer>>) nonCands.clone();
		else
			nonCandidates=nonCands;
		
		this.minFreqThreshold=minFreqThreshold;
		variables=cg.getVariables();
		qry = cg.getQuery();
		result= new Variable[variables.length];
		for (int i = 0; i < variables.length; i++) 
		{
			HashMap<Integer, myNode> list = new HashMap<Integer, myNode>();
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith(),variables[i].getDistanceConstrainedBy()); 
		}
		visitedVariables= new HashSet<Integer>();
		sOrder= new SearchOrder(variables.length);
	}
	
	//for automorphisms and non-cached search
	public DFSSearch(SPpruner sp,Query qry,int minFreqThreshold) 
	{
		this.minFreqThreshold=minFreqThreshold;
		nonCandidates= new HashMap<Integer, HashSet<Integer>>();
		variables=sp.getVariables();
		this.qry = qry;
		result= new Variable[variables.length];
		for (int i = 0; i < variables.length; i++) 
		{
			HashMap<Integer, myNode> list = new HashMap<Integer, myNode>();
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith(),variables[i].getDistanceConstrainedBy()); 
		}
		visitedVariables= new HashSet<Integer>();
		sOrder= new SearchOrder(variables.length);
	}
	
	private void AC_3_New(Variable[] input, int freqThreshold)
	{
		LinkedList<VariablePair> Q= new LinkedList<VariablePair>();
		HashSet<String> contains = new HashSet<String> ();
		VariablePair vp;
		//initialize...
		for (int i = 0; i < input.length; i++) 
		{
			Variable currentVar= input[i];
			ArrayList<Integer> list=currentVar.getDistanceConstrainedWith();
			for (int j = 0; j < list.size(); j++) 
			{
				Variable consVar=variables[list.get(j)];
				vp =new VariablePair(currentVar,consVar);
				Q.add(vp);
				contains.add(vp.getString());
				
			}
		}
		
		while(!Q.isEmpty())
		{			
			vp = Q.poll();
			contains.remove(vp.getString());
			Variable v1 = vp.v1;
			Variable v2 = vp.v2;

			if(v1.getListSize()<freqThreshold || v2.getListSize()<freqThreshold) return;
			int oldV1Size = v1.getListSize();
			int oldV2Size = v2.getListSize();
			refine_Newest(v1, v2, freqThreshold);
			if(oldV1Size!=v1.getListSize())
			{
				if(v1.getListSize()<freqThreshold) return;
				//add to queue
				ArrayList<Integer> list=v1.getDistanceConstrainedBy();
				for (int j = 0; j < list.size(); j++) 
				{
					Integer tempMP = list.get(j); 
					Variable consVar=variables[tempMP];
					vp =new VariablePair(consVar,v1);
					if(!contains.contains(vp.getString()))
					{
						insertInOrder(Q, vp);
						//add new variables at the begining
						contains.add(vp.getString());
					}
				}
			}
			if(oldV2Size!=v2.getListSize())
			{
				if(v2.getListSize()<freqThreshold) return;
				//add to queue
				ArrayList<Integer> list=v2.getDistanceConstrainedBy();
				for (int j = 0; j < list.size(); j++) 
				{
					Variable consVar=variables[list.get(j)];
					vp =new VariablePair(consVar,v2);
					if(!contains.contains(vp.getString()))
					{
						insertInOrder(Q, vp);
						contains.add(vp.getString());
					}
				}
			}
		}
	}
	
	/**
	 * should be the fatsest version
	 */
	private void refine_Newest(Variable v1, Variable v2, int freqThreshold)
	{
		HashMap<Integer,myNode> listA,listB;
		
		int labelB=v2.getLabel();//lebel of my neighbor
		listA=v1.getList();//the first column
		listB=v2.getList();//the second column
		HashMap<Integer,myNode> newList= new HashMap<Integer,myNode>();//the newly assigned first column
		HashMap<Integer, myNode> newReachableListB = new HashMap<Integer, myNode>();//the newly asigned second column
		
		//go over the first column
		for (Iterator<myNode> iterator = listA.values().iterator(); iterator.hasNext();)
		{
			myNode n1= iterator.next();//get the current node
			if(n1.hasReachableNodes()==false)//prune a node without reachable nodes
				continue;
			
			ArrayList<Integer> neighbors = n1.getRechableWithNodeIDs(labelB);//get a list of current node's neighbors
			if(neighbors==null)
				continue;
				
			for (Iterator<Integer> iterator2 = neighbors.iterator(); iterator2.hasNext();)//go over each neighbor
			{
				Integer mp = iterator2.next();//get current neighbor details
				//check the second column if it contains the current neighbor node
				if(listB.containsKey(mp))
				{
					//if true, put the current node in the first column, and the neighbor node in the second column
					newList.put(n1.getID(),n1);
					newReachableListB.put(mp, listB.get(mp));
				}
			}
		}
		
		//set the newly assigned columns
		v1.setList(newList);
		v2.setList(newReachableListB);
	}
	
	public int hasBeenPrecomputed(Variable[] autos,int[] preComputed,int index) //if returns same index should search in it!!
	{
		HashMap<Integer, myNode> list = autos[index].getList();
		
		for (Iterator<Integer> iterator = list.keySet().iterator(); iterator.hasNext();) 
		{
			int nodeIndex = iterator.next();
			if(preComputed[nodeIndex]==1)
				return nodeIndex;
		}		
		return index; //else return the same index
	}
	
	
	
	
	private boolean areAllLabelsDistinct(HPListGraph<Integer, Double> me)
	{
		for (int i = 0; i < me.getNodeCount(); i++) 
		{
			int labelChecker=((Integer)me.getNodeLabel(i));
			for (int j = i+1; j < me.getNodeCount(); j++) 
			{
				int label= (Integer) me.getNodeLabel(j);
				if(labelChecker==label)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * given the graph, check whether it is Acyclic or not (we assume the graph is connected)
	 * @param me
	 * @return
	 */
	public static boolean isItAcyclic(HPListGraph<Integer, Double> me)
	{
		HashSet<Integer> visited = new HashSet<Integer>();
		Vector<Integer> toBeVisited = new Vector<Integer>();
		int currentNodeID;
		toBeVisited.add(0);
		while(visited.size()<me.getNodeCount())
		{
			if(toBeVisited.size()==0)
				break;
			
			currentNodeID = toBeVisited.get(0);
			toBeVisited.remove(0);
			visited.add(currentNodeID);
			//get all neighbor nodes (incoming and outgoing)
			int alreadyVisitedNeighbors = 0;//this should not be more than 1
			
			//all edges!
			for (final IntIterator eit = me.getEdgeIndices(currentNodeID); eit.hasNext();)
			{
				int edgeID = eit.next();
				int nID = me.getNodeA(edgeID);
				if(nID==currentNodeID) nID=me.getNodeB(edgeID);
				
				if(visited.contains(nID))
				{
					alreadyVisitedNeighbors++;
					if(alreadyVisitedNeighbors>1)
					{
						return false;
					}
				}
				else
				{
					toBeVisited.add(nID);
				}
			}
		}
		
		return true;
	}
	
	public void searchExistances()
	{
		if(Settings.isApproximate)
			numberOfIterations=new BigInteger("0");
		ArrayList<Integer> X= new ArrayList<Integer>();
				
		//fast check for the min size of all the candidates, if any of them is below the minimum threshold break !!!
		int min=variables[0].getListSize();
		for (int i = 0; i < variables.length; i++) 
		{
			if(min>variables[i].getListSize())
					min=variables[i].getListSize();
			X.add(variables[i].getID());
		}
		if(min<minFreqThreshold)
			return;
		if(variables.length==2 && variables[0].getLabel()!=variables[1].getLabel())
		{
			
			result=cloneDomian(variables);
			return;
		}
		if(Settings.DISTINCTLABELS && areAllLabelsDistinct(qry.getListGraph()) && DFSSearch.isItAcyclic(qry.getListGraph()))
		{
			AC_3_New(variables, minFreqThreshold);
			result=cloneDomian(variables);
			return;
		}
		
		//automorphisms
		Variable[] autos=null;
		Automorphism<Integer, Double> atm=null;
		int[] preComputed=null;
		if(Settings.isAutomorphismOn)
		{
			HPListGraph<Integer, Double> listGraph=qry.getListGraph();
			preComputed=new int[variables.length];
			for (int i = 0; i < preComputed.length; i++) 
			{
				preComputed[i]=0;
			}
			atm=new Automorphism<Integer, Double>(listGraph);
			autos= atm.getResult();
		}
		
		//SEARCH
		ArrayList<myNode> tmp= new ArrayList<myNode>();
		ArrayList<Integer> costs= new ArrayList<Integer>();
		for (int i = variables.length-1; i >=0 ; i--) 
		{
			TimedOutSearchStats.numberOfDomains++;

			boolean search=true;
			if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
			{
				
				int preIndex= hasBeenPrecomputed(autos, preComputed, i);
				if(i!=preIndex)
				{
					search=false;
					if(Settings.PRINT)
						System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$it has automorphisms");
					variables[i].setList((HashMap<Integer, myNode>) variables[preIndex].getList().clone());
					result[i].setList((HashMap<Integer, myNode>) result[preIndex].getList().clone());
				}
			}
			if(search==true)
			{
			//fast check
			min=variables[0].getListSize();
			for (int l = 0; l < variables.length; l++) 
			{
				if(min>variables[l].getListSize())
						min=variables[l].getListSize();
			}
			if(min<minFreqThreshold)
				return;
			
			setVariableVisitingOrder(i);
			int index=-1;
			index = sOrder.getNext();
			Variable firstVB = variables[index];
			HashMap<Integer,myNode> firstList= firstVB.getList();
			AssignmentInstance instance = new AssignmentInstance(variables.length);
			if(tmp.size()>TimedOutSearchStats.maximum)
				TimedOutSearchStats.maximum = tmp.size();
			tmp.clear();
			for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)
			{
				myNode firstNode= iterator.next();
				//if already marked dont search it
				if(result[index].getList().containsKey(firstNode.getID()))
				{
					if(Settings.PRINT)
						System.out.println("ALready searched before !!");
						continue;
				}
				sOrder.reset();
				instance.assign(firstVB.getID(), firstNode);
				if(Settings.PRINT)
					System.out.println(instance);
				
				
		        timer = new Timer(true);
		        timer.schedule(new StopTask(), 5*1000);
		        
		        int value=-1;
		       
	        	if(Settings.isApproximate)
	        	{
	        		numberOfIterations=new BigInteger("0");
	        		worst=new BigDecimal("1");
	        		for (int k = 0; k < variables.length; k++) 
	        		{
	        			int listSize = (int)(variables[k].getList().size()*weight.doubleValue());
	        			worst= worst.multiply(new BigDecimal(listSize));
	        		}
	        		finalWeight= new BigInteger("1");
				
	        		finalWeight=finalWeight.multiply(worst.toBigInteger());
	        		finalWeight=finalWeight.multiply(new BigInteger((variables.length*2)+""));
	        		finalWeight=finalWeight.add(new BigDecimal(Settings.approxConstant).toBigInteger());
	        	}
	        		
	        	value=searchExistances(instance);//TODO

		        //reset number of iterations!!
		        numberOfIterations=new BigInteger("0");
		        
				if(value==-3)
				{
					tmp.add(firstNode);
					TimedOutSearchStats.totalNumber++;

					if(Settings.PRINT)
						System.out.println("passed the time threshold!!");
					isStopped=false;
				}
				timer.cancel();
				
				if(value==-2) //not Found!!!
				{
					//remove element !!
					iterator.remove();
					if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
					{
						HashMap<Integer, myNode> list= autos[firstVB.getID()].getList();
						for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();) 
						{
							int nodeIndex= iterator2.next();
							HashSet<Integer> nonCan=nonCandidates.get(nodeIndex);
							if(nonCan==null)
							{
								nonCan= new HashSet<Integer>();
								nonCan.add(firstNode.getID());
								nonCandidates.put(nodeIndex, nonCan);
							}
							else
							{
								if(!nonCan.contains(firstNode.getID()))
									nonCan.add(firstNode.getID());
							}
							
						}
					}
					else
					{
						HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
						if(nonCan==null)
						{
							nonCan= new HashSet<Integer>();
							nonCan.add(firstNode.getID());
							nonCandidates.put(firstVB.getID(), nonCan);
						}
						else
						{
							if(!nonCan.contains(firstNode.getID()))
								nonCan.add(firstNode.getID());
						}
					}
					if(Settings.PRINT)
						System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Not Found: ");
				}
				if(value==-1)
				{
					//instance Found
					for (int j = 0; j < variables.length; j++) 
					{
						myNode assignedNode=instance.getAssignment(j);						
						if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
						{
								HashMap<Integer, myNode> list= autos[j].getList();
								for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();) 
								{
									int nodeIndex= iterator2.next();
									if(!result[nodeIndex].getList().containsKey(assignedNode.getID()))
									{
										result[nodeIndex].getList().put(assignedNode.getID(), assignedNode);
									}
								}
						}
						else
						{
							if(!result[j].getList().containsKey(assignedNode.getID()))
							{
								result[j].getList().put(assignedNode.getID(), assignedNode);
							}
						}
					}
					//check if the size of the list has passed already the minFreqThreshold!!
					if(result[index].getList().size()>=minFreqThreshold)
						break;
				}
				else if(value>=0)
					if(Settings.PRINT)
					System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Value: "+value);	
				
				instance.clear();
			}
			//Timedout search
			if(Settings.isApproximate==false)	
			if(result[index].getList().size()<minFreqThreshold)
			{
				System.out.println("into TMP Part 1");
				//fast check 
				if(result[index].getList().size()+tmp.size()<minFreqThreshold)
					return;
				if(Settings.isApproximate==true)
				{
				}	
				else //TRULY SEARCH INTO IT!!
				{
					for (int j = 0; j < tmp.size(); j++) 
					{
						if((result[index].getList().size()+(tmp.size()-j))<minFreqThreshold)
							return;
						
						boolean isExistant=true;
						
						if(Settings.isDecomposeOn==true) //decomposition is ON !!!
						{
							
							HPListGraph<Integer, Double> actualPatternGraph = qry.getListGraph();
							Decomposer<Integer, Double> com= new Decomposer<Integer, Double>(actualPatternGraph);
							com.decompose();
							ArrayList<HashMap<HPListGraph<Integer, Double>, ArrayList<Integer>>> maps=com.getMappings();
							int counter=0;
							for (int k = 0; k < maps.size(); k++) //iterate over edges removed!! 
							{
								HashMap<HPListGraph<Integer, Double>, ArrayList<Integer>> edgeRemoved= maps.get(k);
								
								for (Iterator<Entry<HPListGraph<Integer, Double>, ArrayList<Integer>>> iterator = edgeRemoved.entrySet().iterator(); iterator.hasNext();) 
								{
									System.out.println(counter++);
									Entry<HPListGraph<Integer, Double>, ArrayList<Integer>> removedEdgeEntry = iterator.next();
									HPListGraph<Integer, Double> listGraph= removedEdgeEntry.getKey();	// ---------------------------->each graph candidate
									String key=listGraph.toString();
									myNode firstNode=tmp.get(j);
									
									
									ArrayList<Integer> graphMappings=removedEdgeEntry.getValue();	//pattern nodeID ~ original ID
									int correspondingINdex = searchMappings(graphMappings, i); //check if i==index
									if(correspondingINdex==-1)
										continue;
																		
									instance.assign(correspondingINdex, firstNode);
									
									Query qry = new Query((HPListGraph<Integer, Double>)listGraph);
									SPpruner sp = new SPpruner();
									ArrayList<HashMap<Integer,myNode>> candidatesByNodeID = new ArrayList<HashMap<Integer,myNode>> ();
									for (int l = 0; l < listGraph.getNodeCount(); l++) 
									{
										candidatesByNodeID.add((HashMap<Integer, myNode>) variables[graphMappings.get(l)].getList().clone());
									}
									sp.getPrunedLists(candidatesByNodeID, qry);
									DFSSearch df = new DFSSearch(sp,qry,-1);
									
									isExistant=df.searchParticularExistance(instance, correspondingINdex);
									if(isExistant==false)
										{
											firstList.remove(firstNode.getID());
											HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
											if(nonCan==null)
											{
												nonCan= new HashSet<Integer>();
												nonCan.add(firstNode.getID());
												nonCandidates.put(firstVB.getID(), nonCan);
											}
											else
											{
												if(!nonCan.contains(firstNode.getID()))
													nonCan.add(firstNode.getID());
											}
											break;
										}
									instance.clear();
								}
								if(isExistant==false)
									break;

							}
						}
						
						if(isExistant==false)
						{
							continue;
						}
						
						myNode firstNode=tmp.get(j);
						sOrder.reset();
						instance.assign(firstVB.getID(), firstNode);
						if(Settings.PRINT)
							System.out.println(instance);
						//TODO
						int value;
						value=searchExistances(instance);
						
						if(value==-2)
						{
							firstList.remove(firstNode.getID());
							
							if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
							{
								HashMap<Integer, myNode> list= autos[firstVB.getID()].getList();
								for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();) 
								{
									int nodeIndex= iterator2.next();
									HashSet<Integer> nonCan=nonCandidates.get(nodeIndex);
									if(nonCan==null)
									{
										nonCan= new HashSet<Integer>();
										nonCan.add(firstNode.getID());
										nonCandidates.put(nodeIndex, nonCan);
									}
									else
									{
										if(!nonCan.contains(firstNode.getID()))
											nonCan.add(firstNode.getID());
									}
									
								}
							}
							else
							{							
								HashSet<Integer> nonCan=nonCandidates.get(firstVB.getID());
								if(nonCan==null)
								{
									nonCan= new HashSet<Integer>();
									nonCan.add(firstNode.getID());
									nonCandidates.put(firstVB.getID(), nonCan);
								}
								else
								{
									if(!nonCan.contains(firstNode.getID()))
										nonCan.add(firstNode.getID());
								}
							}
							if(Settings.PRINT)
									System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Not Found: ");
							
						}
						if(value==-1)
						{
							//instance Found
							for (int k = 0; k < variables.length; k++) 
							{
								myNode assignedNode=instance.getAssignment(k);
								if(Settings.isAutomorphismOn && atm.hasAutomorphisms())
								{
										HashMap<Integer, myNode> list= autos[k].getList();
										for (Iterator<Integer>  iterator2= list.keySet().iterator(); iterator2.hasNext();) 
										{
											int nodeIndex= iterator2.next();
											if(!result[nodeIndex].getList().containsKey(assignedNode.getID()))
											{
												result[nodeIndex].getList().put(assignedNode.getID(), assignedNode);
											}
										}
								}
								else
								{
									if(!result[k].getList().containsKey(assignedNode.getID()))
									{
										result[k].getList().put(assignedNode.getID(), assignedNode);
									}
								}
							}
							//check if the size of the list has passed already the minFreqThreshold!!
							if(result[index].getList().size()>=minFreqThreshold)
								break;
						}
						instance.clear();
					}
				}
			}
			//end of Search
			if(result[index].getList().size()<minFreqThreshold)
				return;
		}
			
			
		resetVariableVisitingOrder();			
		
		AC_3_New(variables, minFreqThreshold);
		if(Settings.isAutomorphismOn)
			preComputed[i]=1;
		}
	}
	
	private void printVariablesSize(Variable[] vars)
	{
		for (int i = 0; i < vars.length; i++) 
		{
			System.out.println("Var["+i+"]"+vars[i].getList().size());
		}
	}
	
	private int calculateCost(AssignmentInstance instance)
	{
		int cost=0;
		for (int k = 0; k < variables.length; k++) 
		{
			myNode assignedNode=instance.getAssignment(k);
			if(assignedNode==null)
			{
				//cost of the missing Node
				cost+=1;
				//aggregate the cost of the missing edges!!
				int in=variables[k].getDistanceConstrainedBy().size();
				int out=variables[k].getDistanceConstrainedWith().size();
				cost=cost+in+out;
			}
		}
		return cost;
	}
	
	
	public boolean searchParticularExistance(AssignmentInstance instance,int orderINdex)
	{
		sOrder.reset();
		setVariableVisitingOrder(orderINdex);
		timer = new Timer(true);
        timer.schedule(new StopTask(), 7*1000);
		int value=searchExistances(instance);
		isStopped=false;
		timer.cancel();
		if(value==-2) return false;
		else return true;
		
	}
	
	private int searchMappings(ArrayList<Integer> mappings, int variableINdex)
	{
		for (int i = 0; i < mappings.size(); i++) 
		{
			if(mappings.get(i)==variableINdex)
				return i;
		}
		return -1;
	}
	
	private int searchExistances(AssignmentInstance instance)
	{
		if(Settings.isApproximate)
		{
			if(finalWeight.compareTo(numberOfIterations)<1)
			{
				return -3;
			}
		}
		else
		{
			if(isStopped)
			{
				return -3;
			}
		}
		
		int index = sOrder.getNext();

		if(index!=-1)
		{
			Variable currentVB=variables[index];
			ArrayList<Integer> constrainingVariables=currentVB.getDistanceConstrainedWith();
			
			ArrayList<ArrayList<Integer>> candidates= new ArrayList<ArrayList<Integer>>();
			ArrayList<VariableCandidates> variableCandidates= new ArrayList<VariableCandidates>();
			
			//check Validty with constraintVariables
			for (int i = 0; i < constrainingVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingVariables.get(i)];
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel()));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel())));
				}
			}
			
			ArrayList<Integer> constrainingBYVariables=currentVB.getDistanceConstrainedBy();
			for (int i = 0; i < constrainingBYVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingBYVariables.get(i)];
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel()));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel())));
				}
			}
			
			ArrayList<Integer> finalCandidates= Util.getIntersection(candidates);
			if(finalCandidates.size()==0)
			{
				//learn the new constraints !!!
				ArrayList<Point> constrainedVariableIndices=Util.getZerosIntersectionIndices(variableCandidates);
				if(constrainedVariableIndices.size()!=0)
				{
					Point p =constrainedVariableIndices.get(0);
					int minValue=sOrder.getSecondOrderValue(p.x, p.y);
					for (int i = 1; i < constrainedVariableIndices.size(); i++) 
					{
						p = constrainedVariableIndices.get(i);
						int value=sOrder.getSecondOrderValue(p.x, p.y);
						if(minValue>value)
							minValue=value;
					}
						int jumpToIndex=sOrder.getVariableIndex(minValue);
						sOrder.stepBack();
						if(Settings.isApproximate)
							numberOfIterations=numberOfIterations.add(new BigInteger("1"));
						
						instance.deAssign(currentVB.getID());
						return jumpToIndex;
				}
				
			}

						
			int hasResult=0;
			
			for (int i = 0; i < finalCandidates.size(); i++) 
			{
				int candidateIndex=finalCandidates.get(i);
				myNode candidateNode = currentVB.getList().get(candidateIndex);
								
				if(candidateNode!=null)
				{
					instance.assign(currentVB.getID(), candidateNode);
					
					//check identity Validity
					if(AssignmentInstance.ensureIDValidty(instance))
					{
						//proceed with next
						hasResult = searchExistances(instance);
						if(hasResult==-3)
							return -3;
						if(hasResult==-1)
							return -1;
						else if(hasResult>=0)
						{
							if (currentVB.getID()!=hasResult)
							{
								sOrder.stepBack();
								if(Settings.isApproximate)
									numberOfIterations=numberOfIterations.add(new BigInteger("1"));
								instance.deAssign(currentVB.getID());
								return hasResult;
							}
						}
						else
							;
					}
					else
					{
						if(Settings.isApproximate)
							numberOfIterations=numberOfIterations.add(new BigInteger("1"));
						instance.deAssign(currentVB.getID());
						
					}	
				}
				//End ID Validity
			}
			//after finishing... step back to before state
			sOrder.stepBack();
			if(Settings.isApproximate)
				numberOfIterations=numberOfIterations.add(new BigInteger("1"));
			instance.deAssign(currentVB.getID());
		}
		else// index ==-1 means that I reached the point where the assignment is legal
		{
			return -1; //return True
		}
		return -2; //return False
	}
	
	
	
	public void stopSearching()
	{
		isStopped=true;
	}
	
	
	public int getResultCounter()
	{
		return resultCounter;
	}
	
	private void resetVariableVisitingOrder()
	{
		sOrder= new SearchOrder(variables.length);
		visitedVariables.clear();
	}
	
	private void setVariableVisitingOrder(int begin)
	{
		sOrder.addNext(begin);
		visitedVariables.add(begin);
		searchOrder(variables[begin]);
	}
	
	private void searchOrder(Variable vb)
	{
		HashMap<Integer, myNode> list = vb.getList();
		ArrayList<Integer> constrains= vb.getDistanceConstrainedWith();
		for (int i = 0; i < constrains.size(); i++) 
		{
			Variable currentVB= variables[constrains.get(i)];
			if(!visitedVariables.contains(currentVB.getID()))
			{
				visitedVariables.add(currentVB.getID());
				sOrder.addNext(currentVB.getID());
				searchOrder(currentVB);
			}
		}
		ArrayList<Integer> constrainsBY= vb.getDistanceConstrainedBy();
		for (int i = 0; i < constrainsBY.size(); i++) 
		{
			Variable currentVB= variables[constrainsBY.get(i)];
			if(!visitedVariables.contains(currentVB.getID()))
			{
				visitedVariables.add(currentVB.getID());
				sOrder.addNext(currentVB.getID());
				searchOrder(currentVB);
			}
		}
		
	}
	
	public int getFrequencyOfPattern()
	{
		
		int min= result[0].getListSize();
		for (int i = 1; i < result.length; i++) 
		{
			if(min>result[i].getListSize())
				min= result[i].getListSize();
		}
		return min;
	}
	
	public Variable[] getResultVariables() {
		return result;
	}


	public void printListFrequencies()
	{
		for (int i = 0; i < result.length; i++) 
		{
			System.out.println("Result["+result[i].getID()+"] (Label:"+result[i].getLabel()+")=  "+result[i].getListSize());
			HashMap<Integer, myNode> list = result[i].getList();
		}
	}
	
	
	private int getMaxDegreeVariableIndex()
	{
		Variable[] vs= variables;
		int index=0;
		int max=vs[0].getConstraintDegree();
		for (int i = 1; i < vs.length; i++) 
		{
			int degree=vs[i].getConstraintDegree();
			if(max< degree)
			{
				max=degree;
				index=i;
			}
		}	
		return index;
	}
	
	private int getMinListVariableIndex()
	{
		Variable[] vs= variables;
		int index=0;
		int min=vs[0].getListSize();
		for (int i = 1; i < vs.length; i++) 
		{
			int listSize=vs[i].getListSize();
			if(min>listSize)
			{
				index=i;
				min=listSize;
			}
		}
		return index;
	}

	private boolean isAnyEmpty(Variable[] domain)
	{
		boolean isAnyEmpty=false;
		for (int i = 0; i < domain.length; i++) 
		{
			if(domain[i].getList().size()==0)
				{isAnyEmpty=true;break;}
		}
		return isAnyEmpty;
	}
	
	private Variable[] cloneDomian(Variable[] domain)
	{
		Variable[] cloneDomian= new Variable[domain.length];
		for (int i = 0; i < cloneDomian.length; i++) 
		{
			Variable currentDomain=domain[i];
			cloneDomian[i]=new Variable(currentDomain.getID(), currentDomain.getLabel(), (HashMap<Integer, myNode>) currentDomain.getList().clone(), null,null);
		}
		
		//add constraints !!
		for (int i = 0; i < cloneDomian.length; i++) 
		{
			cloneDomian[i].setDistanceConstrainedBy(domain[i].getDistanceConstrainedBy());
			cloneDomian[i].setDistanceConstrainedWith(domain[i].getDistanceConstrainedWith());
		}
		return cloneDomian;
	}
	
	private Variable[] look_ahead(int index, myNode node ,Variable[] currentDomain)
	{
		Variable[] result=cloneDomian(currentDomain);
		
		//assert!!
		if(!result[index].getList().containsKey(node.getID()))
			;
		
		//assign this node !!
		result[index].getList().clear();
		result[index].getList().put(node.getID(), node);
		{
		HashSet<Integer> asserter= new HashSet<Integer>();
		boolean collision=false;
		//assert that all assigned nodes are distinct
		for (int i = 0; i < result.length; i++) 
		{
			if(result[i].getList().size()==1)
			{
				int nodeID=result[i].getList().keySet().iterator().next();
				if(asserter.contains(nodeID))
				{
					collision=true;
					break;
				}
				else
					asserter.add(nodeID);
			}
		}
		if(collision==true)
		{
			return null;
		} //assignment not valid !!
		}
		
		//now refine..
		AC_3_New(result, minFreqThreshold);
		
		return result;
	}
	
	private int solve(ArrayList<Integer> X,Variable[] currentDomain,AssignmentInstance instance)
	{
		int index = sOrder.getNext();

		if(index!=-1)
		{
			Variable[] D=currentDomain;
			Variable[] D_dash=D;
			
			boolean isAnyEmptyD_dash=isAnyEmpty(D_dash);
			Variable currentVariable = D_dash[index];
						
			if(!isAnyEmptyD_dash)
			{
				//iterate over them !!
				for (Iterator<Entry<Integer, myNode>> iterator = currentVariable.getList().entrySet().iterator();iterator.hasNext();) 
				{
					Entry<Integer, myNode> nodeEntry = iterator.next();
					Variable[] D_dash_dash=look_ahead(index, nodeEntry.getValue(), D_dash);
					if(D_dash_dash!=null && !isAnyEmpty(D_dash_dash))
					{
						X.remove((Integer)currentVariable.getID());
						instance.assign(currentVariable.getID(), nodeEntry.getValue());
						int hasSoln=solve(X, D_dash_dash,instance);
						if(hasSoln==-1)
							return -1;
					}
					else
						;
				}
			}
			else //if any domain is Empty!!
			{
				//System.out.println("Domain is Empty !!");
			}
			sOrder.stepBack();
			instance.deAssign(currentVariable.getID());
			X.add(index);
		}
		else  //found solution!!
		{
			return -1;  //Found !!
		}
		
		return -2; //not found
	}
	
	private Variable[] forwardCheck(Variable[] domain)
	{
		
		for (int i = 0; i < domain.length; i++) 
		{
			Variable currentDomain=domain[i];
			if(currentDomain.getList().size()==1)
			{
				myNode node=currentDomain.getList().values().iterator().next();
				
				
				ArrayList<Integer> consBY =domain[i].getDistanceConstrainedBy();
				HashMap<Integer, ArrayList<Integer>> nodereachBy= node.getReachableByNodes(); //Label ~ NodeIDs
				
				for (int j = 0; j < consBY.size(); j++) 
				{
					int variableIndex = consBY.get(j);
					Variable vb = domain[variableIndex];
					HashMap<Integer, myNode> vbList = vb.getList();
					ArrayList<Integer> candNodes= nodereachBy.get(vb.getLabel());
					HashMap<Integer, myNode> newList = new HashMap<Integer, myNode>();
					for (int k = 0; k < candNodes.size(); k++) 
					{
						int candID=candNodes.get(k);
						myNode candNode =vbList.get(candID);
						if(candNode!=null)
							newList.put(candNode.getID(),candNode);
					}
					domain[variableIndex].setList(newList);
				}
				
				ArrayList<Integer> consWith =domain[i].getDistanceConstrainedWith();
				HashMap<Integer, ArrayList<Integer>> nodereachWith= node.getReachableWithNodes(); //Label ~ NodeIDs
				
				for (int j = 0; j < consWith.size(); j++) 
				{
					int variableIndex = consWith.get(j);
					Variable vb = domain[variableIndex];
					HashMap<Integer, myNode> vbList = vb.getList();
					ArrayList<Integer> candNodes= nodereachWith.get(vb.getLabel());
					HashMap<Integer, myNode> newList = new HashMap<Integer, myNode>();
					for (int k = 0; k < candNodes.size(); k++) 
					{
						int candID=candNodes.get(k);
						myNode candNode =vbList.get(candID);
						if(candNode!=null)
							newList.put(candNode.getID(),candNode);
					}
					domain[variableIndex].setList(newList);
				}
			}
		}
		
		
		return domain;
	}
	
	private void search(AssignmentInstance instance)
	{
		int index = sOrder.getNext();
		if(index!=-1)
		{
			Variable currentVB=variables[index];
			ArrayList<Integer> constrainingVariables=currentVB.getDistanceConstrainedWith();
			
			ArrayList<ArrayList<Integer>> candidates= new ArrayList<ArrayList<Integer>>();
			ArrayList<VariableCandidates> variableCandidates= new ArrayList<VariableCandidates>();
			
			//check Validty with constraintVariables
			for (int i = 0; i < constrainingVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingVariables.get(i)];
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel()));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel())));
				}
			}
			
			ArrayList<Integer> constrainingBYVariables=currentVB.getDistanceConstrainedBy();
			for (int i = 0; i < constrainingBYVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingBYVariables.get(i)];
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					candidates.add(cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel()));
					variableCandidates.add(new VariableCandidates(cnVariableIndex, cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel())));
				}
			}
			
			ArrayList<Integer> finalCandidates= Util.getIntersection(candidates);						
			int hasResult=0;
			
			////end check Validty with constraintVariables
			for (int i = 0; i < finalCandidates.size(); i++) 
			{
				int candidateIndex=finalCandidates.get(i);
				myNode candidateNode = currentVB.getList().get(candidateIndex);
				if(candidateNode!=null)
				{
					instance.assign(currentVB.getID(), candidateNode);
					//check identity Validity
					if(AssignmentInstance.ensureIDValidty(instance))
					{
						//proceed with next
						search(instance);
					}
					else
					{
						instance.deAssign(currentVB.getID());
					}
				}
				//End ID Validity
			}
			//after finishing... step back to before state
			sOrder.stepBack();
			instance.deAssign(currentVB.getID());
			
		}
		else// index ==-1 means that I reached the point where the assignment is legal
		{
			//ADD element
			resultCounter++;
			for (int i = 0; i < instance.getAssignmentSize(); i++) 
			{
				myNode nodeInstance=instance.getAssignment(i);
				if(!result[i].getList().containsKey(nodeInstance.getID()))
					result[i].getList().put(nodeInstance.getID(), nodeInstance);
			}
		}
	}
	
	public void searchAll()
	{
		setVariableVisitingOrder(getMaxDegreeVariableIndex()); //set variable visit order
		int index=-1;
		
		index = sOrder.getNext();
		Variable firstVB = variables[index];
		HashMap<Integer,myNode> firstList= firstVB.getList();
		
		int tempCounter=0;
		
		AssignmentInstance instance = new AssignmentInstance(variables.length);
		
		for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)
		{
			myNode firstNode= iterator.next();
			instance.assign(firstVB.getID(), firstNode);
			search(instance);
		}
	}
	
	//insert vp into order according to their variable values length
	private void insertInOrder(LinkedList<VariablePair> Q, VariablePair vp)
	{
		int i = 0;
		Iterator itr = Q.iterator();
	    while(itr.hasNext())
	    {
	    	VariablePair tempVP = (VariablePair)itr.next();
	    	if(tempVP.getMinValuesLength()>vp.getMinValuesLength())
			{
				Q.add(i, vp);
				return;
			}
	    	i++;
	    }
	    Q.add(i, vp);
	}
}
