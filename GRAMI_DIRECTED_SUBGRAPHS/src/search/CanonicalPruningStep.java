/**
 * created May 25, 2006
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

import java.util.Collection;

import dataStructures.Canonizable;
import dataStructures.Extension;



/**
 * This class implements the general pruning of non-canonical fragments.
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
public class CanonicalPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	/**
	 * creates a new canonical pruning object
	 * 
	 * @param next
	 */
	public CanonicalPruningStep(final MiningStep<NodeType, EdgeType> next) {
		super(next);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		final Canonizable can = (Canonizable) node;

		System.out.println("calculating is Canonical?...");
		if (can.isCanonical()) {
			System.out.println("....is Canonical");
			this.callNext(node, extensions);
			
		} else {
			System.out.println("....is NOT Canonical");
			node.store(false);
		}

	}

}
