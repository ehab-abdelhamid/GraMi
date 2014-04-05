/**
 * created May 19, 2006
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
 * Represents a possible extension used during the test for beingcanonical.
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
public class MinExtension<NodeType, EdgeType> extends
		GSpanEdge<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	MinExtension<NodeType, EdgeType> forward, backward;

	int gNodeBi;

	int gEdgei;

	/**
	 * creates a new MinExtension object
	 * 
	 * @param tenv
	 */
	public MinExtension() {
		super();
	}

	private final int compareSameA(final MinExtension<NodeType, EdgeType> other) {
		if (this.getNodeB() != other.getNodeB()) {
			return this.getNodeB() - other.getNodeB();
		}
		if (this.getDirection() != other.getDirection()) {
			return other.getDirection() - this.getDirection();
		}
		if (this.getEdgeLabel() != other.getEdgeLabel()) {
			return this.getEdgeLabel() - other.getEdgeLabel();
		}
		return this.getLabelB() - other.getLabelB();
	}

	/**
	 * compares if this extensions is equal to the given <code>other</code>
	 * one
	 * 
	 * @param other
	 * @return <0; 0; or >0
	 */
	protected int compareTo(final MinExtension<NodeType, EdgeType> other) {
		if (this.getNodeB() == DFSCode.UNUSED
				&& other.getNodeB() != DFSCode.UNUSED) {
			return 1;
		}
		if (other.getNodeB() == DFSCode.UNUSED
				&& this.getNodeB() != DFSCode.UNUSED) {
			return -1;
		}
		if (this.getNodeB() == DFSCode.UNUSED) { // both are node Extensions
			// greater Node has earlier node Extension
			if (this.getNodeA() != other.getNodeA()) {
				return other.getNodeA() - this.getNodeA();
			}
			return compareSameA(other);
		} else { // both are edge Extensions
			// greater Node has later node Extension (should never be needed for
			// dfs)
			if (this.getNodeA() != other.getNodeA()) {
				return this.getNodeA() - other.getNodeA();
			}
			return compareSameA(other);
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
		return obj instanceof MinExtension
				&& compareTo((MinExtension) obj) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.gSpan.GSpanEdge#hashCode()
	 */
	@Override
	public int hashCode() {
		return gNodeBi << 8 + gEdgei;
	}

	protected MinExtension<NodeType, EdgeType> set(final int nodeA,
			final int nodeB, final int labelA, final int edgeLabel,
			final int labelB, final int dir, final int gEdge, final int gNodeB, int theLabelA, int theLabelB) {
		super.set(nodeA, nodeB, labelA, edgeLabel, labelB, dir,theLabelA,theLabelB);
		this.gNodeBi = gNodeB;
		this.gEdgei = gEdge;
		return this;
	}
}
