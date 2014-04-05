/**
 * created May 15, 2006
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

/**
 * This interface encapsulates the functionality to extend one (or more) parent
 * nodes to a set of children
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
public interface Extender<NodeType, EdgeType> extends
		Generic<NodeType, EdgeType> {

	/**
	 * @param nodes
	 * @return an iterator over all children of the set of given nodes
	 * @throws UnsupportedOperationException
	 *             if not supported for the corresponding algorithm
	 */
	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			Collection<SearchLatticeNode<NodeType, EdgeType>> nodes)
			throws UnsupportedOperationException;

	/**
	 * @param node
	 * @return an iterator over all children of the given node
	 * @throws UnsupportedOperationException
	 *             if not supported for the corresponding algorithm
	 */
	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			SearchLatticeNode<NodeType, EdgeType> node)
			throws UnsupportedOperationException;

}
