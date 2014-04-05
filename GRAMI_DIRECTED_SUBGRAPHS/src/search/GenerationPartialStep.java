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


/**
 * This class defines the functionality of a single step during the generation
 * of extensions for the mining process
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
public abstract class GenerationPartialStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	/**
	 * creates a new step, that will use the
	 * <code>next</next> MiningStep, if necessary
	 * @param next
	 */
	public GenerationPartialStep(final MiningStep<NodeType, EdgeType> next) {
		super(next);
	}
	
	//public void closeFiles(){}
	

	/**
	 * tells this step, that it is to be used for the given
	 * <code>embedding</code> of the given <code>node</code> this function
	 * has to call the next step, if necessary.
	 * 
	 * @param node
	 * @param embedding
	 */
//	public abstract void call(SearchLatticeNode<NodeType, EdgeType> node,
//			HPEmbedding<NodeType, EdgeType> embedding);
	
	public abstract void call(SearchLatticeNode<NodeType, EdgeType> node);

	/**
	 * calls the next step for the given <code>embedding</code> of the given
	 * <code>node</code>
	 * 
	 * @param node
	 * @param embedding
	 */
//	protected final void callNext(
//			final SearchLatticeNode<NodeType, EdgeType> node,
//			final HPEmbedding<NodeType, EdgeType> embedding) {
//		@SuppressWarnings("unchecked")
//		final GenerationPartialStep<NodeType, EdgeType> gen = (GenerationPartialStep<NodeType, EdgeType>) next;
//		if (gen != null) {
//			gen.call(node, embedding);
//		}
//	}
	
	protected final void callNext(
			final SearchLatticeNode<NodeType, EdgeType> node) {
		@SuppressWarnings("unchecked")
		final GenerationPartialStep<NodeType, EdgeType> gen = (GenerationPartialStep<NodeType, EdgeType>) next;
		if (gen != null) {
			gen.call(node);
		}
	}

	/**
	 * tells this step that the extensions for the next node will be searched
	 * furthermore
	 */
	public void reset() {
		resetNext();
	}

	/**
	 * resets the next step
	 */
	protected final void resetNext() {
		@SuppressWarnings("unchecked")
		final GenerationPartialStep<NodeType, EdgeType> gen = (GenerationPartialStep<NodeType, EdgeType>) next;
		if (next != null) {
			gen.reset();
		}
	}
}
