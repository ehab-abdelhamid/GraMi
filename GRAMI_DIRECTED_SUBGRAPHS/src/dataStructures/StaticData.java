/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package dataStructures;

import java.util.HashMap;

public class StaticData {

	public static HashMap<String, HashMap<Integer, Integer>[]> hashedEdges;
	public static int getHashedEdgesFreq(String sig)
	{
		HashMap<Integer, Integer>[] hm = hashedEdges.get(sig);
		if(hm==null)
			return 0;
		int freq = hm[0].size();
		if(freq>hm[1].size())
			return hm[1].size();
		return freq;
	}
	public static int counter = 0;
}
