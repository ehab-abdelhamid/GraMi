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

import java.io.Serializable;


/**
 * Represents the edge tuples used in gSpan to represent one edge in
 * the DFS-Code.
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
public class GSpanEdge<NodeType, EdgeType> implements
		Comparable<GSpanEdge<NodeType, EdgeType>>,
		Cloneable<GSpanEdge<NodeType, EdgeType>>, Generic<NodeType, EdgeType>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int nodeA, nodeB;;

	private int labelA, labelB;

	private int edgeLabel, direction;
	
	private int ThelabelA, ThelabelB;

	public int getThelabelA() {
		return ThelabelA;
	}

	public int getThelabelB() {
		return ThelabelB;
	}

	/** the next edge in the DFS-Code (or the pool-list) */
	protected GSpanEdge<NodeType, EdgeType> next;

	public GSpanEdge() {}

	/**
	 * adds this edge to the given <code>graph</code>
	 * 
	 * @param graph
	 */
	public final void addTo(final HPMutableGraph<NodeType, EdgeType> graph) 
	{
		if (graph.getNodeCount() == nodeA) {
			graph.addNodeIndex((NodeType)((Integer)ThelabelA));
		}
		if (graph.getNodeCount() == nodeB) {
			graph.addNodeIndex((NodeType)((Integer)ThelabelB));
		}
		graph
				.addEdgeIndex(nodeA, nodeB, (EdgeType)(edgeLabel+""), direction);
	}

	@Override
	public GSpanEdge<NodeType, EdgeType> clone() {
		return new GSpanEdge<NodeType, EdgeType>().set(nodeA, nodeB,
				labelA, edgeLabel, labelB, direction,ThelabelA,ThelabelB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final GSpanEdge<NodeType, EdgeType> arg0) {
		return compareTo(arg0, arg0.nodeB);
	}

	/**
	 * compares this edge with the given <code>other</code> one,
	 * 
	 * @param other
	 * @param nodeB
	 *            thid node is used as the second node for the other edge
	 * @return <0; 0; or >0
	 */
	public final int compareTo(final GSpanEdge<NodeType, EdgeType> other,
			final int nodeB) {
		if (this.nodeA == other.nodeA) {
			if (this.nodeB != nodeB) {
				return this.nodeB - nodeB;
			}
			if (this.direction != other.direction) {
				return other.direction - this.direction;
			}
			if (this.labelA != other.labelA) {
				return this.labelA - other.labelA;
			}
			if (this.edgeLabel != other.edgeLabel) {
				return this.edgeLabel - other.edgeLabel;
			}
			//TODO changed here<<!!
			return this.labelB - other.labelB;
		} else { 
			// schreiben
			if (this.nodeA < this.nodeB) {
				if (this.nodeB == other.nodeA) {
					return -1; // see paper
				} else {
					if (other.nodeA > this.nodeA) {
						if (other.nodeA > this.nodeB) {
							return -1;
						} else {
							return 1;
						}
					} else {
						if (this.nodeA >= nodeB) {
							return 1;
						} else {
							return -1;
						}
					}
				}
			} else if (other.nodeA < nodeB) {
				if (nodeB == this.nodeA) {
					return 1; // see paper
				} else {
					if (other.nodeA > this.nodeA) {
						if (other.nodeA >= this.nodeB) {
							return -1;
						} else {
							return 1;
						}
					} else {
						if (this.nodeA > nodeB) {
							return 1;
						} else {
							return -1;
						}
					}
				}
			} else { // compare two backwards edges with different nodeA
				return this.nodeA - other.nodeA;
			}
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
		return obj instanceof GSpanEdge && compareTo((GSpanEdge) obj) == 0;
	}

	/**
	 * calculates the edge of th given graph that corresponds to this gSpanEdge
	 * in the given "embedding"
	 * 
	 * @param graph
	 * @param ackNodes
	 * @return the calculated edge
	 */
	public final int getCorresponding(final HPGraph<NodeType, EdgeType> graph,
			final int[] ackNodes) {
		final int nA = ackNodes[nodeA];
		final int nB = ackNodes[nodeB];
		if (direction == Edge.INCOMING) {
			return graph.getEdge(nB, nA);
		} else {
			return graph.getEdge(nA, nB);
		}
	}

	/** @return the direction of the edge */
	public final int getDirection() {
		return direction;
	}

	/** @return the edge label index of the edge */
	public final int getEdgeLabel() {
		return edgeLabel;
	}

	/** @return the node label index of the first node of the edge */
	public final int getLabelA() {
		return labelA;
	}

	/** @return the node label index of the second node of the edge */
	public final int getLabelB() {
		return labelB;
	}

	/** @return the DFS-index of the first node of the edge */
	public final int getNodeA() {
		return nodeA;
	}

	/** @return the DFS-index of the second node of the edge */
	public final int getNodeB() {
		return nodeB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return nodeA << 20 + nodeB << 16 + labelA << 12 + labelB << 8 + edgeLabel << 4 + direction;
	}

	/** @return if this edge is a forward edge */
	public final boolean isForward() {
		return nodeA < nodeB;
	}

	public void release() {
		
		//TODO
//		if (target == tenv) {
//			target.push(this);
//		}
	}
	
	/**
	 * reinitializes the current edge
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param labelA
	 * @param edgeLabel
	 * @param labelB
	 * @param direction
	 * @return the reinitialized edge
	 */
	public GSpanEdge<NodeType, EdgeType> set(final int nodeA, final int nodeB,
			final int labelA, final int edgeLabel, final int labelB,
			final int direction, int THElabelA, int THElabelB) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.edgeLabel = edgeLabel;
		this.direction = direction;
		if (direction == Edge.UNDIRECTED && nodeA == 0 && nodeB == 1
				&& labelA > labelB) {
			this.labelA = labelB;
			this.labelB = labelA;
			this.ThelabelA=THElabelB;
			this.ThelabelB=THElabelA;
			
		} else {
			this.labelA = labelA;
			this.labelB = labelB;
			this.ThelabelA=THElabelA;
			this.ThelabelB=THElabelB;
		}
		this.next = null;
		
		return this;
	}

	@Override
	public String toString() {
		return "(" + nodeA + " " + nodeB + ": " + labelA + " " + edgeLabel
				+ " " + labelB + " " + direction + ")";
	}

}
