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
package dataStructures;

import java.io.Serializable;

/**
 * This interface encapsulates the posability to be frequented
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public interface Frequented extends Serializable{

	/** @return the attached frequency */
	public Frequency frequency();

}
