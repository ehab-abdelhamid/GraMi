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

/**
 * This interface encapsulate the required abilities of canonizable objects.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public interface Canonizable {

	/**
	 * @return <code>true</code>, if the current object is a canonical
	 *         representation
	 */
	public boolean isCanonical();

}
