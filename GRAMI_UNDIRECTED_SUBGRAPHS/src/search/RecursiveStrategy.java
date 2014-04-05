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
package search;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import utilities.DfscodesCache;

import AlgorithmInterface.Algorithm;

import dataStructures.DFSCode;
import dataStructures.HPListGraph;
import dataStructures.StaticData;


//import de.parsemis.utils.Frequented;

/**
 * This class represents the local recursive strategy.
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
public class RecursiveStrategy<NodeType, EdgeType> implements
		Strategy<NodeType, EdgeType> {

	private Extender<NodeType, EdgeType> extender;

	private Collection<HPListGraph<NodeType, EdgeType>> ret;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.Algorithm,
	 *      int)
	 */
	public Collection<HPListGraph<NodeType, EdgeType>> search(  //INITIAL NODES SEARCH
			final Algorithm<NodeType, EdgeType> algo,int freqThresh) {
		ret = new ArrayList<HPListGraph<NodeType, EdgeType>>();
		
		extender = algo.getExtender(freqThresh);

		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo
				.initialNodes(); it.hasNext();) {
			final SearchLatticeNode<NodeType, EdgeType> code = it.next();
			final long time = System.currentTimeMillis();
//			if (VERBOSE) {
//				out.print("doing seed " + code + " ...");
//			}
//			if (VVERBOSE) {
//				out.println();
//			}
			
			search(code);
			it.remove();
			
			//remove frequent edge labels that are already processed - test test test before approval
			double edgeLabel = Double.parseDouble(code.getHPlistGraph().getEdgeLabel(code.getHPlistGraph().getEdge(0, 1)).toString());
			int node1Label = Integer.parseInt(code.getHPlistGraph().getNodeLabel(0).toString());
			int node2Label = Integer.parseInt(code.getHPlistGraph().getNodeLabel(1).toString());
			String signature;
			if(node1Label<node2Label)
				signature = node1Label+"_"+edgeLabel+"_"+node2Label;
			else
				signature = node2Label+"_"+edgeLabel+"_"+node1Label;
			StaticData.hashedEdges.remove(signature);

//			if (VERBOSE) {
//				out.println("\tdone (" + (System.currentTimeMillis() - time)
//						+ " ms)");
			//}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void search(final SearchLatticeNode<NodeType, EdgeType> node) {  //RECURSIVE NODES SEARCH

		//System.out.println("Getting Children");
		final Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender
				.getChildren(node);
		//System.out.println("finished Getting Children");
		//System.out.println(node.getLevel());
		for (final SearchLatticeNode<NodeType, EdgeType> child : tmp) {
//			if (VVVERBOSE) {
//				out.println("doing " + child);
//			}
			//System.out.println("   branching into: "+child);
			//System.out.println("   ---------------------");
			search(child);
			
			
		}
//		if (VVERBOSE) {
//			out.println("node " + node + " done. Store: " + node.store()
//					+ " children " + tmp.size() + " freq "
//					+ ((Frequented) node).frequency());
//		}
		if (node.store()) {
			node.store(ret);
		} else {
			node.release();
		}

		node.finalizeIt();
	}

}
