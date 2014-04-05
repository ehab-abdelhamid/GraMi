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
import utilities.MyPair;
import utilities.Settings;
import utilities.Util;

import dataStructures.ConnectedComponent;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;
import dataStructures.IntIterator;
import decomposer.Decomposer;

public class DFSSearch
{
	
    class StopTask extends TimerTask {
        public void run() {
        	if(Settings.LimitedTime)
        	{
        		System.out.format("Time's up!%n");
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
	
	public HashMap<Integer, HashSet<Integer>> getNonCandidates()
	{
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
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith()); 
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
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith()); 
		}
		visitedVariables= new HashSet<Integer>();
		sOrder= new SearchOrder(variables.length);
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
	
	
	/*
	 * TODO 
	 */
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
	 * given the graph, check whether it is Acyclic or not (assumption: the graph is connected)
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
		System.out.println("Checking frequency for:\n"+qry.getListGraph());
		
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
		{
			System.out.println("Minimum variable size is less than the threshold, and it is: "+min);
			return;
		}
		
		if(variables.length==2 && variables[0].getLabel()!=variables[1].getLabel() )
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
		
		//Now automorphisms check
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
						System.out.println("it has automorphisms");
					
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
			{
				System.out.println("[Fast check] Minumum variable size is less than the threshold, and it is: "+min);
				return;
			}
			
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
				if(Settings.isApproximate==true)
					instance.setMinCOST(calculateCost(instance));
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
	        	value=searchExistances(instance);
		        
				if(value==-3)
				{
					tmp.add(firstNode);
					TimedOutSearchStats.totalNumber++;
					if(Settings.isApproximate==true)
					costs.add(instance.getMinCOST());
					
					isStopped=false;
				}
				timer.cancel();
				
				if(value==-2) //not Found!!!
				{
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
					//check if the size of the list has already passed the minimum Frequency Threshold
					if(result[index].getList().size()>=minFreqThreshold)
						break;
				}
				else if(value>=0)
					if(Settings.PRINT)
					System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Value: "+value);	
				
				instance.clear();
			}
			//INTO Timedout list
			if(Settings.isApproximate==false)	
			if(result[index].getList().size()<minFreqThreshold)
			{
				//quick check 
				if(result[index].getList().size()+tmp.size()<minFreqThreshold)
				{
					System.out.println("less than the threshold, and it is: "+(result[index].getList().size()+tmp.size()));
					return;
				}
				if(Settings.isApproximate==true)
				{
					for (int j = 0; j < tmp.size(); j++) 
					{
						if((result[index].getList().size()+(tmp.size()-j))<minFreqThreshold)
						{
							return;
						}
						
						int currentCost=costs.get(j);
						myNode node = tmp.get(j);
						if(currentCost<=COSTTHRESHOLD)
						{
							result[index].getList().put(node.getID(), node);
							if(result[index].getList().size()>=minFreqThreshold)
								break;
						}
					}
				}	
				else //TRULY SEARCH INTO IT!!
				{
					//loop over nodes that was searched before but timed out 
					for (int j = 0; j < tmp.size(); j++) 
					{
						if((result[index].getList().size()+(tmp.size()-j))<minFreqThreshold)
						{
							System.out.println("less than the threshold, and it is: "+result[index].getList().size()+(tmp.size()-j));
							return;
						}
						
						boolean isExistant=true;
						
						if(Settings.isDecomposeOn==true) //decomposition is ON !!!
						{
							
							HPListGraph<Integer, Double> actualPatternGraph = qry.getListGraph();//the original pattern

							//decompose the pattern into subgraphs generated by removing an edge then return the connected subgraph that contains the lastly added node to the original pattern
							//the return is a list of subgraphs for each one it is associated with a list of mappings; a map between the connected component nodes with the original pattern nodes 
							Decomposer<Integer, Double> com= new Decomposer<Integer, Double>(actualPatternGraph);
							com.decompose();
							
							//loop over items, each item is a list of connected components (with the above propoerties) after removing a specific edge (loop by edges)
							ArrayList<HashMap<HPListGraph<Integer, Double>, ArrayList<Integer>>> maps=com.getMappings();
							for (int k = 0; k < maps.size(); k++) //iterate over edges removed!! 
							{
								HashMap<HPListGraph<Integer, Double>, ArrayList<Integer>> edgeRemoved= maps.get(k);
								
								//loop over the list of connected components generated after removing a specific edge 
								for (Iterator<Entry<HPListGraph<Integer, Double>, ArrayList<Integer>>> iterator = edgeRemoved.entrySet().iterator(); iterator.hasNext();) 
								{
									//a specific subgraph of the original pattern
									Entry<HPListGraph<Integer, Double>, ArrayList<Integer>> removedEdgeEntry = iterator.next();
									
									HPListGraph<Integer, Double> listGraph= removedEdgeEntry.getKey();	// ---------------------------->each graph candidate
									String key=listGraph.toString();//its key (not needed!)
									myNode firstNode=tmp.get(j);//current node to search for (previously timed out)
									
									ArrayList<Integer> graphMappings=removedEdgeEntry.getValue();	//list of pattern nodeID ~ original ID
									
									//get the corresponding subgraph node id to the current pattern variable ID
									int correspondingINdex = searchMappings(graphMappings, i); //check if i==index
									//if the pattern variable id is not found in the current subgraph, then there is no need to check more with the current subgraph
									//, and go check the next subgraph 
									if(correspondingINdex==-1)
										continue;
									
									//pattern instance (an instance that looks like the pattern), is getting an assignment with 'firstNode' for the corresponding node with ID = correspondingINdex (the index of the current pattern variable ID)
									instance.assign(correspondingINdex, firstNode);
									
									//create a query using the original pattern
									Query qry = new Query((HPListGraph<Integer, Double>)listGraph);
									
									SPpruner sp = new SPpruner();
									
									//create the omains list and fill it with the current values
									ArrayList<HashMap<Integer,myNode>> candidatesByNodeID = new ArrayList<HashMap<Integer,myNode>> ();
									for (int l = 0; l < listGraph.getNodeCount(); l++) 
									{
										candidatesByNodeID.add((HashMap<Integer, myNode>) variables[graphMappings.get(l)].getList().clone());
									}
									
									//apply consistency checking using the current query
									sp.getPrunedLists(candidatesByNodeID, qry);
									DFSSearch df = new DFSSearch(sp,qry,-1);//<-- this is initiating 
									
									//search the pruned domains for any existence of the instance
									isExistant=df.searchParticularExistance(instance, correspondingINdex);
									//if there is no existance, then:
									if(isExistant==false)
									{
											firstList.remove(firstNode.getID());//1- remove the checked node from its domain
											//2- add the removed node to the noncandidates
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
											
											//3- break from the loops, as we found that part of the pattern can not be satisfied by the currently searched node
											//break from this loop, then go to the nexxt if condition (X) which will break from the outer loop
											break;
									}
									instance.clear();
								}
								if(isExistant==false)//break from the outer loop
									break;
							}
						}
						
						//if one of the the pattern subgraphs can not be satisfied, then no need to do search as this node is already removed 
						if(isExistant==false)
						{
							continue;
						}
						
						//continue the search
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
			{
				return;
			}
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
				int out=variables[k].getDistanceConstrainedWith().size();
				cost=cost+out;
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
				return -3;
		}
		
		int index = sOrder.getNext();
		if(index!=-1)
		{
			Variable currentVB=variables[index];
			ArrayList<MyPair<Integer, Double>> constrainingVariables=currentVB.getDistanceConstrainedWith();
			
			ArrayList<ArrayList<MyPair<Integer, Double>>> candidates= new ArrayList<ArrayList<MyPair<Integer, Double>>>();
			ArrayList<VariableCandidates> variableCandidates= new ArrayList<VariableCandidates>();
			
			//check Validty with constraintVariables
			for (int i = 0; i < constrainingVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingVariables.get(i).getA()];
				Double edgeLabel = constrainingVariables.get(i).getB();
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					ArrayList<MyPair<Integer, Double>> tempArr = cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel);
					candidates.add(tempArr);
					variableCandidates.add(new VariableCandidates(cnVariableIndex, tempArr));
				}
			}
			
			ArrayList<MyPair<Integer, Double>> finalCandidates= Util.getIntersection(candidates);
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
				int candidateIndex=finalCandidates.get(i).getA();
				myNode candidateNode = currentVB.getList().get(candidateIndex);
								
				if(candidateNode!=null)
				{
					instance.assign(currentVB.getID(), candidateNode);
					
					//check identity Validity
					if(AssignmentInstance.ensureIDValidty(instance))
					{
						
						if(Settings.isApproximate==true)
						{
							int previousInstanceMinCost=instance.getMinCOST();
							int currentInstanceMinCost=calculateCost(instance);
							if(currentInstanceMinCost<=previousInstanceMinCost)
								instance.setMinCOST(currentInstanceMinCost);
						}
						
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
			}
			//after finishing... step back to previous state
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
		ArrayList<MyPair<Integer, Double>> constrains= vb.getDistanceConstrainedWith();
		for (int i = 0; i < constrains.size(); i++) 
		{
			Variable currentVB= variables[constrains.get(i).getA()];
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
			cloneDomian[i]=new Variable(currentDomain.getID(), currentDomain.getLabel(), (HashMap<Integer, myNode>) currentDomain.getList().clone(), null);
		}
		
		//add constraints !!
		for (int i = 0; i < cloneDomian.length; i++) 
		{
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
			//
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
				;
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
				
				ArrayList<MyPair<Integer, Double>> consWith =domain[i].getDistanceConstrainedWith();
				HashMap<Integer, ArrayList<MyPair<Integer, Double>>> nodereachWith= node.getReachableWithNodes();
				
				for (int j = 0; j < consWith.size(); j++) 
				{
					int variableIndex = consWith.get(j).getA();
					Variable vb = domain[variableIndex];
					HashMap<Integer, myNode> vbList = vb.getList();
					ArrayList<MyPair<Integer, Double>> candNodes= nodereachWith.get(vb.getLabel());
					HashMap<Integer, myNode> newList = new HashMap<Integer, myNode>();
					for (int k = 0; k < candNodes.size(); k++) 
					{
						int candID=candNodes.get(k).getA();
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
			ArrayList<MyPair<Integer, Double>> constrainingVariables=currentVB.getDistanceConstrainedWith();
			
			ArrayList<ArrayList<MyPair<Integer, Double>>> candidates= new ArrayList<ArrayList<MyPair<Integer, Double>>>();
			ArrayList<VariableCandidates> variableCandidates= new ArrayList<VariableCandidates>();
			
			//check Validty with constraintVariables
			for (int i = 0; i < constrainingVariables.size(); i++) 
			{
				Variable cnVariable=variables[constrainingVariables.get(i).getA()];
				double edgeLabel = constrainingVariables.get(i).getB();
				int cnVariableIndex=cnVariable.getID();
				myNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
				if(cnVariableInstance!=null)
				{
					ArrayList<MyPair<Integer, Double>> tempArr = cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel);
					candidates.add(tempArr);
					variableCandidates.add(new VariableCandidates(cnVariableIndex, tempArr));
				}
			}
			
			ArrayList<MyPair<Integer, Double>> finalCandidates= Util.getIntersection(candidates);						
			int hasResult=0;
			
			//end check Validty with constraintVariables
			for (int i = 0; i < finalCandidates.size(); i++) 
			{
				int candidateIndex=finalCandidates.get(i).getA();
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
		
		AssignmentInstance instance = new AssignmentInstance(variables.length);
		
		for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)
		{
			myNode firstNode= iterator.next();
			instance.assign(firstVB.getID(), firstNode);
			search(instance);
		}
	}
	
	public Variable[] getVariables()
	{
		return this.variables;
	}
	
	//AC_3
	private void AC_3_New(Variable[] input, int freqThreshold)
	{
		LinkedList<VariablePair> Q= new LinkedList<VariablePair>();
		HashSet<String> contains = new HashSet<String> ();
		VariablePair vp;
		
		//initialize...
		for (int i = 0; i < input.length; i++) 
		{
			Variable currentVar= input[i];
			ArrayList<MyPair<Integer, Double>> list=currentVar.getDistanceConstrainedWith();
			boolean newVarAdded = false;
			for (int j = 0; j < list.size(); j++) 
			{
				Variable consVar=variables[list.get(j).getA()];
				vp =new VariablePair(currentVar,consVar,list.get(j).getB());
				insertInOrder(Q, vp);
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
			refine_Newest(v1, v2, vp.edgeLabel, freqThreshold);
			if(oldV1Size!=v1.getListSize())
			{
				if(v1.getListSize()<freqThreshold) return;
				//add to queue
				ArrayList<MyPair<Integer, Double>> list=v1.getDistanceConstrainedWith();
				for (int j = 0; j < list.size(); j++) 
				{
					MyPair<Integer, Double> tempMP = list.get(j); 
					Variable consVar=variables[tempMP.getA()];
					vp =new VariablePair(consVar,v1,tempMP.getB());
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
				ArrayList<MyPair<Integer, Double>> list=v2.getDistanceConstrainedWith();
				for (int j = 0; j < list.size(); j++) 
				{
					Variable consVar=variables[list.get(j).getA()];
					vp =new VariablePair(consVar,v2,list.get(j).getB());
					if(!contains.contains(vp.getString()))
					{
						insertInOrder(Q, vp);
						contains.add(vp.getString());
					}
				}
			}
		}
	}
	
	//refine
	private void refine_Newest(Variable v1, Variable v2, double edgeLabel, int freqThreshold)
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
			
			ArrayList<MyPair<Integer, Double>> neighbors = n1.getRechableWithNodeIDs(labelB, edgeLabel);//get a list of current node's neighbors
			for (Iterator<MyPair<Integer, Double>> iterator2 = neighbors.iterator(); iterator2.hasNext();)//go over each neighbor
			{
				MyPair<Integer, Double> mp = iterator2.next();//get current neighbor details
				//check the second column if it contains the current neighbor node
				if(listB.containsKey(mp.getA()))
				{
					//if true, put the current node in the first column, and the neighbor node in the second column
					newList.put(n1.getID(),n1);
					newReachableListB.put(mp.getA(), listB.get(mp.getA()));
				}
			}
		}
		
		//set the newly assigned columns
		v1.setList(newList);
		v2.setList(newReachableListB);
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