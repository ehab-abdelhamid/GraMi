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

import java.io.Serializable;

/**
 * Represents an extension to a DFS-Code.
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
public class GSpanExtension<NodeType, EdgeType> implements
		Extension<NodeType, EdgeType>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GSpanEdge<NodeType, EdgeType> edge;

	public  DFSCode frag;
	
	/** used for the pool */
	transient GSpanExtension<NodeType, EdgeType> next;

	public GSpanExtension() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final Extension<NodeType, EdgeType> arg0) {
		@SuppressWarnings("unchecked")
		final GSpanExtension<NodeType, EdgeType> ext = (GSpanExtension<NodeType, EdgeType>) arg0;
		return edge.compareTo(ext.edge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof GSpanExtension
				&& compareTo((GSpanExtension) obj) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Extension#frequency()
	 */
	public Frequency frequency() {
		return getFragment().frequency();
	}

	/**
	 * @return the fragment this extension will lead to
	 */
	@SuppressWarnings("unchecked")
	public final DFSCode<NodeType, EdgeType> getFragment() {
		if (frag instanceof DFSCode) {
			return (DFSCode<NodeType, EdgeType>) frag;
		}
		return  null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return edge.hashCode();
	}

	/**
	 * stores this extension to the given <code>target</code> environment, if
	 * possible
	 * 
	 * @param target
	 */
	public void release() 
	{
		//TODO
//		if (target == tenv) {
//			target.push(this);
//		}
	}

	@Override
	public String toString() {
		return edge.toString();
	}

}
