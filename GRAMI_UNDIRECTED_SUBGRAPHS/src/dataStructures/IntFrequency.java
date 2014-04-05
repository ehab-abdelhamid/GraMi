/**
 * created May 17, 2006
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
 * This class isa simple integer absed frequency for graphs/fragments/...
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class IntFrequency implements Frequency {

	private static final long serialVersionUID = 3344404461258667897L;

	private int freq;

	/**
	 * @param init
	 */
	public IntFrequency(final int init) {
		freq = init;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Frequency#add(de.parsemis.miner.Frequency)
	 */
	public void add(final Frequency freq) {
		this.freq += ((IntFrequency) freq).freq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	@Override
	public Frequency clone() {
		return new IntFrequency(freq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final Frequency arg0) {
		return this.freq - ((IntFrequency) arg0).freq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Frequency && compareTo((Frequency) obj) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return freq;
	}

	/**
	 * @return the integer representation of the current frequency
	 */
	public int intValue() {
		return freq;
	}

	public void smul(final float fac) {
		this.freq = (int) Math.ceil(freq * fac);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Frequency#sub(de.parsemis.miner.Frequency)
	 */
	public void sub(final Frequency freq) {
		this.freq -= ((IntFrequency) freq).freq;
	}

	@Override
	public String toString() {
		return "" + freq;
	}

}
