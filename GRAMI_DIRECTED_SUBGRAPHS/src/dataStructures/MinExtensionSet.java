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
 * Stores and sorts the created extensions during the test for being canonical.
 * <p>
 * It represents the head marker of a double linked ring list.
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
public class MinExtensionSet<NodeType, EdgeType> extends
		MinExtension<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int size = 0;

	protected MinExtensionSet() {
		super();
		forward = backward = this;
	}

	/**
	 * inserts the given <code>extension</code>
	 * 
	 * @param extension
	 * @return <code>true</code>, if correctly added
	 */
	public boolean add(final MinExtension<NodeType, EdgeType> extension) {
		if (extension.forward != null || extension.backward != null) {
			return false;
		}
		MinExtension<NodeType, EdgeType> ack = this;
		while (ack.forward.compareTo(extension) < 0) {
			ack = ack.forward;
		}
		extension.forward = ack.forward;
		extension.backward = ack;
		ack.forward = extension.forward.backward = extension;
		++size;
		return true;
	}

	/**
	 * insert the given <code>extension</code> and all extensions that are
	 * connected with the given on to the set
	 * 
	 * @param ext
	 * @return <code>true</code>, if correctly added
	 */
	public boolean addAll(MinExtension<NodeType, EdgeType> ext) {
		boolean ret = true;
		while (ext != null) {
			ret = ret && add(ext);
			ext = (MinExtension<NodeType, EdgeType>) ext.next;
		}
		return ret;
	}

	/**
	 * finally removes all remaining extension
	 */
	public void clear() {
		while (!isEmpty()) {
			removeAndFree(this.forward);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gSpan.MinExtension#compareTo(de.parsemis.algorithms.gSpan.MinExtension)
	 */
	@Override
	protected int compareTo(final MinExtension<NodeType, EdgeType> ext) {
		return 1;
	}

	/**
	 * @return <code>true</code>, if the set is empty
	 */
	public boolean isEmpty() {
		return this.forward == this;
	}

	/**
	 * reinsert the given <code>extension</code> before the <code>next</code>
	 * one in the ring
	 * 
	 * @param extension
	 * @param next
	 */
	public final void relink(final MinExtension<NodeType, EdgeType> extension,
			final MinExtension<NodeType, EdgeType> next) {
		extension.backward = next.backward;
		next.backward.forward = extension;
		extension.forward = next;
		next.backward = extension;
	}

	/**
	 * finally remove the given <code>extension</code>
	 * 
	 * @param extension
	 * @return <code>true</code>, if removed correctly
	 */
	public boolean removeAndFree(
			final MinExtension<NodeType, EdgeType> extension) {
		if (extension.forward == null || extension.backward == null) {
			return false;
		}
		extension.forward.backward = extension.backward;
		extension.backward.forward = extension.forward;
		extension.backward = extension.forward = null;
		//extension.release();
		--size;
		return true;
	}

	/**
	 * finally remove the given <code>extension</code> and all extensions that
	 * are connected with the given on
	 * 
	 * @param ext
	 * @return <code>true</code>, if removed correctly
	 */
	public boolean removeAndFreeAll(MinExtension<NodeType, EdgeType> ext) {
		boolean ret = true;
		while (ext != null) {
			final MinExtension<NodeType, EdgeType> next = (MinExtension<NodeType, EdgeType>) ext.next;
			ext.next = null;
			ret = ret && removeAndFree(ext);
			ext = next;
		}
		return ret;

	}

	/**
	 * @return the number of elements in the set
	 */
	public int size() {
		return size;
	}

	/**
	 * remove the given <code>extension</code> from the ring
	 * 
	 * @param extension
	 * @return the extension after the removed one
	 */
	public final MinExtension<NodeType, EdgeType> unlink(
			final MinExtension<NodeType, EdgeType> extension) {
		final MinExtension<NodeType, EdgeType> next = extension.forward;
		// remove ack
		extension.backward.forward = next;
		next.backward = extension.backward;
		extension.forward = extension.backward = null;
		return next;
	}
}
