/**
 * created May 16, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import pruning.SPpruner;

import decomposer.Decomposer;

import search.SearchLatticeNode;
import utilities.DfscodesCache;
import utilities.MyPair;
import utilities.Settings;
import utilities.Util;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import CSP.Variable;


/**
 * Implements the DFSCode that represents a subgraph during the
 * search.
 * <p>
 * It can/will be stored in local object pool to avoid object generation/garbage
 * collection.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class DFSCode<NodeType, EdgeType> extends
		SearchLatticeNode<NodeType, EdgeType> implements
		Comparable<DFSCode<NodeType, EdgeType>>, Generic<NodeType, EdgeType>,
		Canonizable, Frequented {

	/**
	 * 
	 */
	
	private ArrayList<Integer> sortedFreqLabels;
	private ArrayList<Double> freqEdgeLabels;
	
	public ArrayList<Integer> getSortedFreqLabels() {
		return sortedFreqLabels;
	}
	
	public ArrayList<Double> getFreqEdgeLabels() {
		return freqEdgeLabels;
	}

	private static final long serialVersionUID = 1L;

	protected final static int UNUSED = -1;

	private  GSpanEdge<NodeType, EdgeType> first, last;

	private int psize;

	private int threadIdx;
	
	private HPListGraph<NodeType, EdgeType> me;
	
	private IntFrequency finalFrequency=null;
	
	private Graph singleGraph;
	
	private Variable[] currentVariables=null;
	private Variable[] previousStepVariables=null;
	
	private HashMap<Integer, HashSet<Integer>> nonCandidates=null;
	
	public HashMap<Integer, HashSet<Integer>> getNonCandidates() {
		return nonCandidates;
	}

	transient private ArrayList<GSpanEdge<NodeType, EdgeType>> parents;

	public ArrayList<GSpanEdge<NodeType, EdgeType>> getParents() {
		return parents;
	}

	/** used for the object pool */
	// transient public DFSCode<NodeType,EdgeType> next;
	/**
	 * creates a new DFSCode
	 * 
	 * @param tenv
	 */
	public DFSCode(ArrayList<Integer> sortedFreqLabels, ArrayList<Double> freqEdgeLabels,Graph singleGraph, HashMap<Integer, HashSet<Integer>> nonCands) 
	{
		this.sortedFreqLabels=sortedFreqLabels;
		this.freqEdgeLabels = freqEdgeLabels;
		this.singleGraph=singleGraph;
		this.nonCandidates=nonCands;
	}

	public Graph getSingleGraph() {
		return singleGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final DFSCode<NodeType, EdgeType> arg0) {
		GSpanEdge<NodeType, EdgeType> ack1 = this.first;
		GSpanEdge<NodeType, EdgeType> ack2 = arg0.first;
		while (ack1 != null && ack2 != null && ack1.compareTo(ack2) == 0) {
			ack1 = ack1.next;
			ack2 = ack2.next;
		}
		if (ack1 == null) {
			if (ack2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (ack2 == null) {
			return 1;
		} else {
			return ack1.compareTo(ack2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof DFSCode && compareTo((DFSCode) obj) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#extend(de.parsemis.miner.Extension)
	 */
	@Override
	public SearchLatticeNode<NodeType, EdgeType> extend(
			final Extension<NodeType, EdgeType> extension) {
		assert extension instanceof GSpanExtension : "DFSCode.extend(..) is just applicable for GSpanExtensions";
		final GSpanExtension<NodeType, EdgeType> ext = (GSpanExtension<NodeType, EdgeType>) extension;
		//final GThreadEnvironment<NodeType, EdgeType> tenv = tenv();

		// clone current DFS-List
		final GSpanEdge<NodeType, EdgeType> nextFirst = first.clone();
		GSpanEdge<NodeType, EdgeType> nextLast = nextFirst;
		final HPGraph<NodeType, EdgeType> g = ext.getFragment().getHPlistGraph();
		final ArrayList<GSpanEdge<NodeType, EdgeType>> nextParents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(
				g.getNodeCount());

		// generate parent map
		for (int i = g.getNodeCount(); i > 0; --i) {
			nextParents.add(null);
		}
		nextParents.set(nextFirst.getNodeA(), nextFirst);
		nextParents.set(nextFirst.getNodeB(), nextFirst);

		for (GSpanEdge<NodeType, EdgeType> ack = first.next; ack != null; ack = ack.next) {
			nextLast.next = ack.clone();
			nextLast = nextLast.next;
			if (nextLast.isForward()) {
				nextParents.set(nextLast.getNodeB(), nextLast);
			}
		}

		// append new edge
		nextLast.next = ext.edge;
		nextLast = ext.edge;
		if (nextLast.isForward()) {
			nextParents.set(nextLast.getNodeB(), nextLast);
		}

		// get "new" DFSCode object
		if(currentVariables==null)
			System.out.println("Error");
		return new DFSCode<NodeType, EdgeType>(sortedFreqLabels, freqEdgeLabels,singleGraph,Util.clone(nonCandidates)).set(ext.getFragment().getHPlistGraph(), nextFirst, nextLast, nextParents);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#finalizeIt()
	 */
	@Override
	public void finalizeIt() {
		parents = null;
	}
	
	private int getMaxDegree()
	{
		int max=-1;
		for (int i = 0; i < me.getNodeCount(); i++) 
		{
			if(max>me.getDegree(i))
				max=me.getDegree(i);
		}
		return max;
	}
	
	private int getNumOfMaxDistinctLabel()
	{
		HashMap<Integer, Integer> labelCounter=new HashMap<Integer, Integer>();
		for (int i = 0; i < me.getNodeCount(); i++) 
		{
			int label= (Integer) me.getNodeLabel(i);
			Integer counter=labelCounter.get(label);
			if(counter==null)
				{labelCounter.put(label, 1);}
			else
				labelCounter.put(label, counter+1);
		}
		int max=-1;
		
		for (Iterator<Entry<Integer, Integer>> iterator = labelCounter.entrySet().iterator(); iterator.hasNext();) 
		{
			Entry<Integer, Integer> entry = iterator.next();
			if(max<entry.getValue())
				max=entry.getValue();
		}
		
		return max;
	}
	
	private boolean areAllLabelsDistinct()
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
	
	private boolean areAllLabelsTheSame()
	{
		int label=(Integer)me.getNodeLabel(0);
		for (int i = 1; i < me.getNodeCount(); i++) 
		{
			if(label!=(Integer)me.getNodeLabel(i))
				return false;
		}
		return true;
	}
	

	/** @return the frequency (finally) associated with this DFS-code */
	public final Frequency frequency() {
		
		//check frequency !!!
		if(finalFrequency!=null)
			return finalFrequency;
		else
		{
			//list of constraints
			//PRUNER
			//******* 1
//			if(getMaxDegree()>singleGraph.getMaxNodeDegreeThreshold())
//			{
//				finalFrequency=new IntFrequency(0);
//				return finalFrequency;
//			}
			//PRUNER
			//******* 2
//			if(me.getNodeCount()>=5)
//			{
//				finalFrequency=new IntFrequency(0);
//				return finalFrequency;
//			}
			
			//PRUNER
			//******* 3
//			if(getNumOfMaxDistinctLabel()>2)
//			{
//				finalFrequency=new IntFrequency(0);
//				return finalFrequency;
//			}
//			if(me.getEdgeCount()>6)
//			{
//				finalFrequency=new IntFrequency(0);
//				return finalFrequency;
//			}
			
			//PRUNER
			//******* 4
//			if(!areAllLabelsDistinct())
//			{
//				finalFrequency=new IntFrequency(0);
//				return finalFrequency;
//			}
			
			//PRUNER
			//******* 5
//			if(areAllLabelsTheSame())
//			{
//				finalFrequency=new IntFrequency(singleGraph.getFreqThreshold()+1);
////				finalFrequency=new IntFrequency(0);
//				return finalFrequency;
//			}
			
			//check for 1-edge patterns, to get its actual frequency from Graph.hashedEdges
			if(this.getHPlistGraph().getEdgeCount()==1)
			{
				double edgeLabel = Double.parseDouble(getHPlistGraph().getEdgeLabel(getHPlistGraph().getEdge(0, 1)).toString());
				int node1Label = Integer.parseInt(getHPlistGraph().getNodeLabel(0).toString());
				int node2Label = Integer.parseInt(getHPlistGraph().getNodeLabel(1).toString());
				String signature;
				if(node1Label<node2Label)
					signature = node1Label+"_"+edgeLabel+"_"+node2Label;
				else
					signature = node2Label+"_"+edgeLabel+"_"+node1Label;
				HashMap[] hm = StaticData.hashedEdges.get(signature);
				if(hm==null)
				{
					finalFrequency=new IntFrequency(0);
					return finalFrequency;
				}
				else
				{
					int freq = hm[0].size();
					if(hm[1].size()<freq)
						freq = hm[1].size();
				}
			}
			
			if(nonCandidates==null)
			{
				nonCandidates= new HashMap<Integer, HashSet<Integer>>();
				Query q = new Query((HPListGraph<Integer, Double>)me);
				if(Settings.CACHING)
				{
					//non-candidates preparation
					Decomposer<NodeType, EdgeType> com= new Decomposer<NodeType, EdgeType>(me);
					com.decompose();
					ArrayList<HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> maps=com.getMappings();
					for (int i = 0; i < maps.size(); i++) //iterate over edges removed!! 
					{
						HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>> edgeRemoved= maps.get(i);
						
						for (Iterator<Entry<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> iterator = edgeRemoved.entrySet().iterator(); iterator.hasNext();) 
						{
							Entry<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>> removedEdgeEntry = iterator.next();
							HPListGraph<NodeType, EdgeType> listGraph= removedEdgeEntry.getKey();
							String key=listGraph.toString();
							if(DfscodesCache.cache.containsKey(key))
							{
								ArrayList<Integer> graphMappings=removedEdgeEntry.getValue();
								HashMap<Integer, HashSet<Integer>>  nodeNonCandidates = DfscodesCache.cache.get(key); //node ~ noncandidates
								for (int j = 0; j < listGraph.getNodeCount(); j++) 
								{
									HashSet<Integer> nonCans = nonCandidates.get(graphMappings.get(j));
									HashSet<Integer> nonCansToBeAdded=nodeNonCandidates.get(j);
									if(nonCans!=null && nonCansToBeAdded!=null)
									{
										nonCans.addAll(nonCansToBeAdded);
									}
								}
							}
						}
					}
				}
				
				ConstraintGraph cg = new ConstraintGraph(singleGraph, q, (HashMap<Integer, HashSet<Integer>>)nonCandidates);
				DFSSearch df = new DFSSearch(cg,singleGraph.getFreqThreshold(),nonCandidates);
				df.searchExistances();
				
				currentVariables=df.getResultVariables();
				int freq=df.getFrequencyOfPattern();
				
				if(Settings.CACHING)
				{
				if(freq>=singleGraph.getFreqThreshold())
					nonCandidates=df.getNonCandidates();
				}
				
				if(Settings.CACHING)
				{
					String code = me.toString(); if(code==null) ;
					HashMap<Integer, HashSet<Integer>> nonCands= nonCandidates; if(nonCands==null);
					DfscodesCache.cache.put(code, nonCands);
				}

				finalFrequency= new IntFrequency(freq);
			}
			else
			{
				Query q = new Query((HPListGraph<Integer, Double>)me);
				
				if(Settings.CACHING)
				{
					// non-candidates preparation
					Decomposer<NodeType, EdgeType> com= new Decomposer<NodeType, EdgeType>(me);
					com.decompose();
					ArrayList<HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> maps=com.getMappings();
					for (int i = 0; i < maps.size(); i++) //iterate over edges removed!! 
					{
						HashMap<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>> edgeRemoved= maps.get(i);
						
						for (Iterator<Entry<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>>> iterator = edgeRemoved.entrySet().iterator(); iterator.hasNext();) 
						{
							Entry<HPListGraph<NodeType, EdgeType>, ArrayList<Integer>> removedEdgeEntry = iterator.next();
							HPListGraph<NodeType, EdgeType> listGraph= removedEdgeEntry.getKey(); // each graph candidate
							String key=listGraph.toString();
							ArrayList<Integer> graphMappings=removedEdgeEntry.getValue();//pattern nodeID ~ original ID
							
							if(DfscodesCache.cache.containsKey(key))
							{
								HashMap<Integer, HashSet<Integer>>  nodeNonCandidates = DfscodesCache.cache.get(key); //node ~ noncandidates
								for (int j = 0; j < listGraph.getNodeCount(); j++) 
								{
									HashSet<Integer> nonCans = nonCandidates.get(graphMappings.get(j));
									HashSet<Integer> nonCansToBeAdded=nodeNonCandidates.get(j);
									if(nonCans!=null && nonCansToBeAdded!=null)
									{
										nonCans.addAll(nonCansToBeAdded);
									}
								}
							}
						}
					}
				}
				
				ConstraintGraph cg = new ConstraintGraph(singleGraph, q, (HashMap<Integer, HashSet<Integer>>)nonCandidates);
				DFSSearch df = new DFSSearch(cg,singleGraph.getFreqThreshold(),nonCandidates);
				
				df.searchExistances();
				currentVariables=df.getResultVariables();
				
				int freq=df.getFrequencyOfPattern();
				
				if(Settings.CACHING)
				{
				if(freq>=singleGraph.getFreqThreshold())
					nonCandidates=df.getNonCandidates();
				}
				
				if(Settings.CACHING)
				{
					String code = me.toString(); if(code==null) ;
					HashMap<Integer, HashSet<Integer>> nonCands= nonCandidates; if(nonCands==null);
					DfscodesCache.cache.put(code, nonCands);
				}
				
				finalFrequency= new IntFrequency(freq);
				
			}
			return finalFrequency;
		}
	}
	
	
	public Variable[] getCurrentVariables() {
		return currentVariables;
	}

	/*
	 * generates a single connected list of possible unused extensions of the
	 * nodeA/gNodeA
	 */
	private final MinExtension<NodeType, EdgeType> getExtensions(
			final int nodeA, final HPGraph<NodeType, EdgeType> graph,
			final int gNodeA, final int[] usedEdges, final int[] usedNodes)
	{
		MinExtension<NodeType, EdgeType> last = null;
		for (int i = graph.getDegree(gNodeA) - 1; i >= 0; --i) {
			// for each adjazent edge of A
			final int edge = graph.getNodeEdge(gNodeA, i);
			final int gNodeB = graph.getOtherNode(edge, gNodeA);
			int thelabelA = (Integer) graph.getNodeLabel(gNodeA);
			int thelabelB = (Integer) graph.getNodeLabel(gNodeB);
			int edgeLabel = Integer.parseInt(graph.getEdgeLabel(edge)+"");
			if (usedEdges[edge] == UNUSED) {
				// build extension for unused edges
				final MinExtension<NodeType, EdgeType> next = new MinExtension<NodeType, EdgeType>().set(nodeA, usedNodes[gNodeB],
								sortedFreqLabels.indexOf(thelabelA),
								edgeLabel,
								sortedFreqLabels.indexOf(thelabelB),
								graph.getDirection(edge, gNodeA), edge, gNodeB,thelabelA,thelabelB);
				next.next = last;
				last = next;
			}
		}
		return last;
	}

	/** @return the initial GSpanEdge of this DFSCode */
	public final GSpanEdge<NodeType, EdgeType> getFirst() {
		return first;
	}

	/** @return the last GSpanEdge of this DFSCode */
	public final GSpanEdge<NodeType, EdgeType> getLast() {
		return last;
	}

	/**
	 * generates the parents array, if necessary
	 * 
	 * @param node
	 * @return the GSpanEdge that introduced the given node
	 */
	public GSpanEdge<NodeType, EdgeType> getParent(final int node) {
		if (parents == null) {
			parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(psize);
			for (int i = 0; i < psize; i++) {
				parents.add(null);
			}
			parents.set(0, first);
			for (GSpanEdge<NodeType, EdgeType> ack = first; ack != null; ack = ack.next) {
				if (ack.isForward()) {
					parents.set(ack.getNodeB(), ack);
				}
			}
		}
		assert parents.size() > node : this + " " + node;
		return parents.get(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.SearchLatticeNode#getThreadNumber()
	 */
	@Override
	public int getThreadNumber() {
		return threadIdx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return first == null ? 0 : first.hashCode();
	}

	/**
	 * helper function for the test of beeing canonical
	 * 
	 * search for a smaller first edge or starts recursive extension for equally
	 * ones
	 * 
	 * @param set
	 * @param ackNodes
	 * @param usedNodes
	 * @param usedEdges
	 * @return <code>false</code>, the part of the serach determines this
	 */
	private boolean isCan(final MinExtensionSet<NodeType, EdgeType> set,
			final int[] ackNodes, final int[] usedNodes, final int[] usedEdges) {
		final HPGraph<NodeType, EdgeType> hp = me;

		for (int node = hp.getMaxNodeIndex() - 1; node >= 0; --node) {
			// try each node as potential start node for a smaller DFSCode
			if (!hp.isValidNode(node)) {
				continue;
			}

			final int nodeLabelIndex = sortedFreqLabels.indexOf((hp
					.getNodeLabel(node)));
			
			int foundEdge = -2;
			GSpanEdge<NodeType, EdgeType> ack = first;
			if (ack.getNodeA() == ack.getNodeB()) { // self edge
				final int edge = hp.getEdge(node, node);
				if (edge != -1) {
					if (nodeLabelIndex < ack.getLabelA()) {
						return false; // a smaller DFSCode is found
					}
					if (nodeLabelIndex == ack.getLabelA()) {
						// only DFSCodes will searched which starts same as this
						final int edgeIndex = edge;

						final int edgeLabelIndex = 0;
						if (edgeLabelIndex < ack.getEdgeLabel()) {
							return false; // a smaller DFSCode is found
						}
						if (edgeLabelIndex == ack.getEdgeLabel()) {
							// equal starting edge found
							usedNodes[node] = 0;
							ackNodes[0] = node;
							usedEdges[edgeIndex] = 1;
							foundEdge = edgeIndex;
							ack = ack.next;
						}
					}
				}
			} else { // no self-edges
				if (hp.getEdge(node, node) != -1) {
					return false; // a smaller DFSCode is found
				}
				if (nodeLabelIndex <= ack.getLabelA()) {
					// only DFSCodes will searched which starts same as this
					// first edge will be detected by recursion
					usedNodes[node] = 0;
					ackNodes[0] = node;
					foundEdge = -1;
				}
			}
			if (foundEdge > -2 && ack != null) {// node is a start node
				final MinExtension<NodeType, EdgeType> exts = getExtensions(0,
						hp, node, usedEdges, usedNodes);
				set.addAll(exts);
				// recursiv extension to find a smaller DFSCode
				if (!isCan2(ack, set, 0, usedNodes, usedEdges, ackNodes, hp)) {
					// a smaller DFSCode is found
					set.removeAndFreeAll(exts);
					return false;
				}
				set.removeAndFreeAll(exts);
			}
			usedNodes[node] = -1;
			if (foundEdge > -1) {
				usedEdges[foundEdge] = -1;
			}
		}

		// no smaller is found
		return true;
	}

	/**
	 * helper function for the test of beeing canonical
	 * 
	 * extends current detected DFSCode and searches for new extensions
	 * 
	 * @param ackEdge
	 * @param set
	 * @param lastNode
	 * @param usedNodes
	 * @param usedEdges
	 * @param ackNodes
	 * @param graph
	 * @return <code>false</code>, the part of the serach determines this
	 */
	private boolean isCan2(final GSpanEdge<NodeType, EdgeType> ackEdge,
			final MinExtensionSet<NodeType, EdgeType> set, final int lastNode,
			final int[] usedNodes, final int[] usedEdges, final int[] ackNodes,
			final HPGraph<NodeType, EdgeType> graph) {
		final MinExtension<NodeType, EdgeType> first = set.forward;
		for (MinExtension<NodeType, EdgeType> ack = first; ack.compareTo(first) == 0; ack = ack.forward) {
			// for each extension that fits the first one

			// remove it from set
			final MinExtension<NodeType, EdgeType> next = set.unlink(ack);

			if (usedEdges[ack.gEdgei] != UNUSED) {
				// skip already used edges
				if (!isCan2(ackEdge, set, lastNode, usedNodes, usedEdges,
						ackNodes, graph)) {
					// smaller DFSCode found
					set.relink(ack, next);
					return false;
				}
			} else {
				if (ack.getNodeB() == UNUSED) { // forward edge
					final int tmp = ackEdge.compareTo(ack, lastNode + 1);
					if (tmp > 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return false;
					}
					if (ackEdge.next == null || tmp < 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return true;
					}
					// compute extensions from the new node
					usedNodes[ack.gNodeBi] = lastNode + 1;
					ackNodes[lastNode + 1] = ack.gNodeBi;
					usedEdges[ack.gEdgei] = 1;
					final MinExtension<NodeType, EdgeType> exts = getExtensions(
							lastNode + 1, graph, ack.gNodeBi, usedEdges,
							usedNodes);
					set.addAll(exts);
					// recursiv search
					if (!isCan2(ackEdge.next, set, lastNode + 1, usedNodes,
							usedEdges, ackNodes, graph)) {
						// smaller DFSCode found
						set.removeAndFreeAll(exts);
						set.relink(ack, next);
						return false;
					}
					set.removeAndFreeAll(exts);
					usedNodes[ack.gNodeBi] = UNUSED;
					ackNodes[lastNode + 1] = UNUSED;
					usedEdges[ack.gEdgei] = UNUSED;
				} else { // backward edge
					final int tmp = ackEdge.compareTo(ack, ack.getNodeB());
					if (tmp > 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return false;
					}
					if (ackEdge.next == null || tmp < 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return true;
					}
					// mark edge as used
					usedEdges[ack.gEdgei] = 1;
					// recursiv search
					if (!isCan2(ackEdge.next, set, lastNode, usedNodes,
							usedEdges, ackNodes, graph)) {
						// smaller DFSCode found
						set.relink(ack, next);
						return false;
					}
					usedEdges[ack.gEdgei] = UNUSED;
				}
			}
			set.relink(ack, next);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Canonizable#isCanonical()
	 */
	public boolean isCanonical() {
		// create and initilize embedding Arrays
		final int nc = me.getNodeCount();
		final int ec = me.getEdgeCount();
		final int[] ackNodes = getIntArray(nc, UNUSED);
		final int[] usedNodes = getIntArray(nc, UNUSED);
		final int[] usedEdges = getIntArray(ec, UNUSED);
		final MinExtensionSet<NodeType, EdgeType> set = new MinExtensionSet<NodeType, EdgeType>();
		final boolean ret = isCan(set, ackNodes, usedNodes, usedEdges);
		return ret;
	}

	private int[] getIntArray(final int length, final int def) {
		final int[] ret = getIntArray(length);
		for (int i = 0; i < length; ++i) {
			ret[i] = def;
		}
		return ret;
	}
	private int[] getIntArray(final int length) {
		return new int[length];
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#release()
	 */
	@Override
	public void release()
	{
		me = null;
	}

	/**
	 * initialisation do allow reusability
	 * 
	 * @param me
	 * @param first
	 * @param last
	 * @param parents
	 * @return a newly initialized DFSCode
	 */
	public DFSCode<NodeType, EdgeType> set(
			final HPListGraph<NodeType, EdgeType> me,
			final GSpanEdge<NodeType, EdgeType> first,
			final GSpanEdge<NodeType, EdgeType> last,
			final ArrayList<GSpanEdge<NodeType, EdgeType>> parents) {
		//storedEmbeddingsPath=storeEmbPath;
		this.parents = parents;
		this.psize = parents.size();
		this.first = first;
		this.last = last;
		this.me=me;
		setLevel(me.getEdgeCount() - 1);
		store(true);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#setThreadNumber(int)
	 */
	@Override
	public void setThreadNumber(final int idx) {
//		if (threadIdx != idx) {
//			threadIdx = idx;
//			tenv = null;
//		}
	}
	
	public HPListGraph<NodeType, EdgeType> getHPlistGraph()
	{
		return me; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return DFScodeSerializer.serialize(me);
	}

}
