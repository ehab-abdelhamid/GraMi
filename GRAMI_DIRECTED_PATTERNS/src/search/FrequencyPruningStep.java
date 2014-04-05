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

import dataStructures.Extension;
import dataStructures.Frequency;
import dataStructures.Frequented;


/**
 * This class implements the general pruning of fragments according to their
 * frequency.
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
public class FrequencyPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	private final Frequency min, max;

	/**
	 * creates a new frequency pruning
	 * 
	 * @param next
	 * @param min
	 * @param max
	 */
	public FrequencyPruningStep(final MiningStep<NodeType, EdgeType> next,
			final Frequency min, final Frequency max) {
		super(next);
		this.min = min;
		this.max = max;
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
		final Frequency freq = ((Frequented) node).frequency();  //HERE THE FREQUENCY CALCULATION OCCURS !!!
		if (max != null && max.compareTo(freq) < 0) {
			node.store(false);
		}
		if (min.compareTo(freq) > 0) {
			node.store(false);
		} else {
			callNext(node, extensions);
		}
	}

}
