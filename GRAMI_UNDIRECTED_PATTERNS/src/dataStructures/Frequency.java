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
package dataStructures;

import java.io.Serializable;

/**
 * This interface describes the functionality of a frequency for graph mining
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public interface Frequency extends Comparable<Frequency>, Serializable {

	/**
	 * increases this frequency by the given amount
	 * 
	 * @param freq
	 */
	public void add(Frequency freq);

	/**
	 * @return a clone of the Frequency
	 */
	public Frequency clone();

	/**
	 * (skalar) multiplies the frequency with the given factor
	 * 
	 * @param fac
	 */
	public void smul(float fac);

	/**
	 * decreases this frequency by the given amount
	 * 
	 * @param freq
	 */
	public void sub(Frequency freq);

}
