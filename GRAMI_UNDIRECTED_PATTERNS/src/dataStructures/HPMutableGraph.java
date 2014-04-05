/**
 * created May 31, 2006
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

/**
 * Declares the functions to add (and remove) nodes and edges from a HPGraph.
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
public interface HPMutableGraph<NodeType, EdgeType> extends
		HPGraph<NodeType, EdgeType> {

	/**
	 * adds a new edge between the given nodes
	 * 
	 * @param nodeAIdx
	 *            the index of the nodeA
	 * @param nodeBIdx
	 *            the index of the nodeB
	 * @param label
	 *            the label of the new edge
	 * @param direction
	 *            the direction of the new edge (in respect to nodeA)
	 * @return the index of the new edge
	 */
	public int addEdgeIndex(int nodeAIdx, int nodeBIdx, EdgeType label,
			int direction);

	/**
	 * adds a new node and connect if with a new edge to the given nodeA
	 * 
	 * @param nodeAIdx
	 *            the index of the nodeA
	 * @param nodeLabel
	 *            the label of the new node
	 * @param edgeLabel
	 *            the label of the new edge
	 * @param direction
	 *            the direction of the new edge (in respect to nodeA)
	 * @return the index of the new node
	 */
	public int addNodeAndEdgeIndex(int nodeAIdx, NodeType nodeLabel,
			EdgeType edgeLabel, int direction);

	/**
	 * adds a new node
	 * 
	 * @param label
	 *            the label of the new node
	 * @return the index of the new node
	 */
	public int addNodeIndex(NodeType label);

	/**
	 * removes the given edge
	 * 
	 * @param edgeIdx
	 *            the index of the edge
	 * @return <code>true</code>, if the edge is removed succesfully
	 */
	public boolean removeEdge(int edgeIdx);

	/**
	 * removes the given node and all its connected edges
	 * 
	 * @param nodeIdx
	 *            the index of the node
	 * @return <code>true</code>, if the node is removed succesfully
	 */
	public boolean removeNode(final int nodeIdx);

}
