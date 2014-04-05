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


/**
 * This class defines the functionality of a mining step, that encapsulate the
 * whole generation of extensions
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
public abstract class GenerationStep<NodeType, EdgeType> extends
		GenerationPartialStep<NodeType, EdgeType> {

	private GenerationPartialStep<NodeType, EdgeType> first;

	private final GenerationPartialStep<NodeType, EdgeType> finalize;

	/**
	 * creates a new GenerationStep
	 * 
	 * @param next
	 */
	public GenerationStep(final MiningStep<NodeType, EdgeType> next) {
		super(null);
		finalize = new GenerationPartialStep<NodeType, EdgeType>(next) {
			@Override
			public void call(final SearchLatticeNode<NodeType, EdgeType> node,
					final Collection<Extension<NodeType, EdgeType>> extensions) {
				// just call the next step of the outer GerenationStep
				callNext(node, extensions); //extend the node and add to search tree
			}

			@Override
			public void call(final SearchLatticeNode<NodeType, EdgeType> node) {
				// the final step for extension generation is nothing else than
				// stop
			}

			@Override
			public void reset() {
				// the final step for resetting is nothing else than stop
			}
		};
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
		if (first != null) {
			first.call(node, extensions);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      de.parsemis.graph.Embedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node) {
		if (first != null) {
			first.call(node);
		}
		//first.closeFiles();
	}
	
//	public void closeFiles()
//	{
//		first.closeFiles();
//	}

	/**
	 * @return the final step for this generation chain
	 */
	public GenerationPartialStep<NodeType, EdgeType> getLast() {
		return finalize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#getNext()
	 */
	@Override
	public MiningStep<NodeType, EdgeType> getNext() {
		return finalize.getNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		if (first != null) {
			first.reset();
		}
	}

	/**
	 * sets the start of the generation chain
	 * 
	 * @param first
	 */
	public void setFirst(final GenerationPartialStep<NodeType, EdgeType> first) {
		this.first = first;
	}

}
