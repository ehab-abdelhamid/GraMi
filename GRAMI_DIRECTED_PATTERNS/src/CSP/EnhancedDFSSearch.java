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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import CSP.DFSSearch.StopTask;

import dataStructures.ConnectedComponent;
import dataStructures.Query;
import dataStructures.myNode;

public class EnhancedDFSSearch 
{
	class StopTask extends TimerTask {
        public void run() {
            System.out.format("Time's up!");
            stopSearching();
        }
    }
	
	//private ConstraintGraph cGraph;
	private Variable[] variables;
	private Variable[] result;
	private HashSet<Integer> visitedVariables;
	private SearchOrder sOrder;
	private int minFreqThreshold;
	private ConstraintGraph cg;
	private Timer timer;
	
	private volatile boolean isStopped=false;
	
	private HashMap<Integer, HashSet<Integer>> nonCandidates;
	
	public HashMap<Integer, HashSet<Integer>> getNonCandidates() {
		return nonCandidates;
	}
	
	public void searchExistances()
	{
		ArrayList<Integer> X= new ArrayList<Integer>();
		
		for (int i = 0; i < variables.length; i++) 
		{
			System.out.println(variables[i].getID()+" size: "+variables[i].getListSize());
		}
		//fast check for the min size of all the candidates, if any of them is below the minimum threshold break !!!
		int min=variables[0].getListSize();
		for (int i = 1; i < variables.length; i++) 
		{
			if(min>variables[i].getListSize())
					min=variables[i].getListSize();
			X.add(variables[i].getID());
		}
		if(min<minFreqThreshold)
			return;
		
		
		//SEARCH
		ArrayList<myNode> tmp= new ArrayList<myNode>();
		for (int i = variables.length-1; i >= 0; i--) 
		{
			//fast check for the min size of all the candidates, if any of them is below the minimum threshold break
			min=variables[0].getListSize();
			for (int l = 1; l < variables.length; l++) 
			{
				if(min>variables[l].getListSize())
						min=variables[l].getListSize();
				X.add(variables[l].getID());
			}
			if(min<minFreqThreshold)
				return;

			setVariableVisitingOrder(i);
			int index=-1;
			index = sOrder.getNext();
			Variable firstVB = variables[index];
			HashMap<Integer,myNode> firstList= firstVB.getList();
			AssignmentInstance instance = new AssignmentInstance(variables.length);
			tmp.clear();
			for (Iterator<myNode> iterator = firstList.values().iterator(); iterator.hasNext();)
			{
				myNode firstNode= iterator.next();
				//if already marked dont search it
				if(result[index].getList().containsKey(firstNode.getID()))
				{
					System.out.println("ALready searched before !!");
					continue;
				}
				sOrder.reset();
				instance.assign(firstVB.getID(), firstNode);
				System.out.println(instance);
				
				
		        timer = new Timer(true);
		        timer.schedule(new StopTask(), 5*1000);

				Variable[] D= look_ahead(index, firstNode, variables);
				int value=solve(X, D,instance);
				if(value==-3)
				{
					tmp.add(firstNode);
					System.out.println("passed the time threshold!!");
					isStopped=false;
				}
				timer.cancel();
				
				if(value==-1)
				{
					//instance Found
					for (int j = 0; j < variables.length; j++) 
					{
						myNode assignedNode=instance.getAssignment(j);
						if(!result[j].getList().containsKey(assignedNode.getID()))
						{
							result[j].getList().put(assignedNode.getID(), assignedNode);
						}
					}
					//check if the size of the list has passed already the minFreqThreshold!!
					if(result[index].getList().size()>minFreqThreshold)
						break;
				}
				else if(value==-2)
				{
					iterator.remove();
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
					System.out.println("ERRRRRRRRRRRRRRRRRRR........................................Not Found: ");
				}
						
				
				instance.clear();
			}
			
			//Timeouted search
			if(result[index].getList().size()<minFreqThreshold)
			{
				System.out.println("into TMP Part 1");
				//fast check 
				if(result[index].getList().size()+tmp.size()<minFreqThreshold)
					return;
					
				else //TRULY SEARCH INTO IT!!
				{
					System.out.println("into TMP Part 2");
					for (int j = 0; j < tmp.size(); j++) 
					{
						System.out.println("found: "+result[index].getList().size()+" tmp: "+tmp.size()+" j: "+j);
						if((result[index].getList().size()+(tmp.size()-j))<minFreqThreshold)
							return;
						myNode firstNode=tmp.get(j);
						sOrder.reset();
						instance.assign(firstVB.getID(), firstNode);
						System.out.println(instance);
						Variable[] D= look_ahead(index, firstNode, variables);
						int value=solve(X, D,instance);
						if(value==-2)
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
						}
						if(value==-1)
						{
							for (int k = 0; k < variables.length; k++) 
							{
								myNode assignedNode=instance.getAssignment(k);
								if(!result[k].getList().containsKey(assignedNode.getID()))
								{
									result[k].getList().put(assignedNode.getID(), assignedNode);
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

			resetVariableVisitingOrder();
			
			//fast check for the min size of this candidate list, if any of them is below the minimum threshold break !!!
			if(result[index].getList().size()<minFreqThreshold)
				return;
			
			
			AC_3(variables);
		}
	}
	
	
	
	
	private int solve(ArrayList<Integer> X,Variable[] currentDomain,AssignmentInstance instance)
	{
		if(isStopped)
			return -3;
		int index = sOrder.getNext();
		if(index!=-1)
		{
			Variable[] D=currentDomain;
			//TODO get D_dash!!
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
					{
						//System.out.println("Not a solution");
					}
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
		
		return -2;
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
		forwardCheck(result);
		return result;
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
	
	private Variable[] propagate_neighborhood_constraints(ArrayList<Integer> X,Variable[] currentDomain)
	{
		Variable[] result=cloneDomian(currentDomain);
		boolean changes=true;
		
		while(changes)
		{
			changes=false;
			for (int i = 0; i < X.size(); i++) 
			{
				Variable Xvariable=result[X.get(i)]; //for all i in X
				for (Iterator<Entry<Integer, myNode>> iterator = Xvariable.getList().entrySet().iterator(); iterator.hasNext();) 
				 {
					Entry<Integer, myNode> nodeEntry = iterator.next();
				}
			}
		}
		
		return result;
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
	
	
	public EnhancedDFSSearch(ConstraintGraph cg,int minFreqThreshold,HashMap<Integer, HashSet<Integer>> nonCands) 
	{
		nonCandidates=nonCands;
		this.cg=cg;
		this.minFreqThreshold=minFreqThreshold;
		variables=cg.getVariables();
		result= new Variable[variables.length];
		for (int i = 0; i < variables.length; i++) 
		{
			HashMap<Integer, myNode> list = new HashMap<Integer, myNode>();
			result[i]= new Variable(variables[i].getID(), variables[i].getLabel(),list,variables[i].getDistanceConstrainedWith(),variables[i].getDistanceConstrainedBy()); 
		}
		visitedVariables= new HashSet<Integer>();
		sOrder= new SearchOrder(variables.length);
	}
	
	
	
	public void stopSearching()
	{
		isStopped=true;
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
	
	private void AC_3(Variable[] input)
	{
		Queue<VariablePair> Q= new LinkedList<VariablePair>();
		HashSet<String> contains = new HashSet<String> ();
		VariablePair vp;
		//initialize...
		for (int i = 0; i < input.length; i++) 
		{
			Variable currentVar= input[i];
			ArrayList<Integer> list=currentVar.getDistanceConstrainedWith();
			for (int j = 0; j < list.size(); j++) 
			{
				Variable consVar=input[list.get(j)];
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
			if(refine(v1, v2))
			{
				//add to queue
				ArrayList<Integer> list=v1.getDistanceConstrainedBy();
				for (int j = 0; j < list.size(); j++) 
				{
					Variable consVar=input[list.get(j)];
					vp =new VariablePair(consVar,v1);
					if(!contains.contains(vp.getString()))
					{
						Q.add(vp);
						contains.add(vp.getString());
					}
				}
				
				list=v2.getDistanceConstrainedBy();
				for (int j = 0; j < list.size(); j++) 
				{
					Variable consVar=input[list.get(j)];
					vp =new VariablePair(consVar,v2);
					if(!contains.contains(vp.getString()))
					{
						Q.add(vp);
						contains.add(vp.getString());
					}
				}
			}
			
		}
	}
	
	private boolean refine(Variable v1, Variable v2)
	{
		boolean changeHappened=false;
		
		HashMap<Integer,myNode> listA,listB;
		
		int labelB=v2.getLabel();
		listA=v1.getList();
		listB=v2.getList();
		HashMap<Integer,myNode> newList= new HashMap<Integer,myNode>();
		HashMap<Integer, myNode> newReachableListB = new HashMap<Integer, myNode>();
		
		for (Iterator<myNode> iterator = listA.values().iterator(); iterator.hasNext();)
		{
			myNode n1= iterator.next();
			if(n1.hasReachableNodes()==false)
				continue;
			boolean doesIntersect=false;
			for (Iterator<myNode> iterator2 = listB.values().iterator(); iterator2.hasNext();)
			{
				myNode n2= iterator2.next();
				if(n1.getID()==n2.getID())
					continue;
				if(n1.isWithinTheRangeOf(n2.getID(),labelB))
				{
					doesIntersect=true;
					newList.put(n1.getID(),n1);
					newReachableListB.put(n2.getID(), n2);
				}
			}
			if(doesIntersect==false)
			{
			changeHappened=true;
			}
		}
		v1.setList(newList);
		v2.setList(newReachableListB);
		
		return changeHappened;
	}
}
