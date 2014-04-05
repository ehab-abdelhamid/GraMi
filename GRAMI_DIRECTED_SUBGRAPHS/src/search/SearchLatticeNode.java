/**
 * created May 12, 2006
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

import java.io.Serializable;
import java.util.Collection;

import dataStructures.Extension;
import dataStructures.Generic;
import dataStructures.HPListGraph;
import dataStructures.serializableObject;

/**
 * This class defines the interface and basic functionality of a single node in
 * a search lattice.
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
public abstract class SearchLatticeNode<NodeType, EdgeType>  implements
		Generic<NodeType, EdgeType>, Serializable, serializableObject {

	private int level;

	/*
	 * TODO: perhaps multiple flags like frequent, connected, etc ... or store
	 * it in a fragment ???
	 */
	private boolean store = true;

	protected SearchLatticeNode() {
		this.level = -1;
	}

	protected SearchLatticeNode(final int level) {
		this.level = level;
	}

	/**
	 * @return all embeddings of this node
	 */
	//public abstract Collection<HPEmbedding<NodeType, EdgeType>> allEmbeddings();

	/**
	 * @param extension
	 * @return a new node resulted by extending this node with the given
	 *         <code>extension</code>
	 */
	public abstract SearchLatticeNode<NodeType, EdgeType> extend(
			Extension<NodeType, EdgeType> extension);

	/**
	 * release all internal structures to the local object pool that are never
	 * needed even if the node is stored
	 */
	public abstract void finalizeIt();

	/**
	 * @return the <code>level</code> (= depth in the search tree) of this
	 *         node
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * gets the thread index of the SearchLatticeNode
	 * 
	 * @return the thread index
	 */
	public abstract int getThreadNumber();

	/**
	 * release all internal structures to the local object pool that are never
	 * needed if the node is not stored
	 */
	public abstract void release();

	/**
	 * sets the final set of embeddings
	 * 
	 * @param embs
	 */
//	public abstract void setFinalEmbeddings(
//			Collection<HPEmbedding<NodeType, EdgeType>> embs);

	/**
	 * sets the <code>level</code> (= depth in the search tree) of this node
	 * 
	 * @param level
	 */
	public final void setLevel(final int level) {
		this.level = level;
	}

	/**
	 * sets the thread index of the SearchLatticeNode
	 * 
	 * @param threadIdx
	 */
	public abstract void setThreadNumber(int threadIdx);

	/**
	 * @return the set <code>store</code>-value
	 */
	public final boolean store() {
		return store;
	}

	/**
	 * sets the <code>store</code>-value of this node to the given boolean
	 * 
	 * @param store
	 */
	public final void store(final boolean store) {
		this.store = store;
	}

	/**
	 * stores the fragment into the given set
	 * 
	 * @param set
	 */
	public void store(final Collection<HPListGraph<NodeType, EdgeType>> set) {
		set.add(getHPlistGraph());
	}

	/**
	 * @return the fragment corresponding to this node
	 */
//	public Fragment<NodeType, EdgeType> toFragment() {
//		return toHPFragment().toFragment();
//	}

	
	public abstract HPListGraph<NodeType, EdgeType> getHPlistGraph();
	
	
	/**
	 * @return the high performance fragment corresponding to this node
	 */
	//public abstract HPFragment<NodeType, EdgeType> toHPFragment();

}
