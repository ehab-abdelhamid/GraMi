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

import java.awt.Point;
import java.util.Comparator;

public class freqComparator implements Comparator<Point> {
	
		  public int compare(Point o1, Point o2) {
		    
			  if(o1.y>o2.y)
				  return -1;
			  else if(o1.y<o2.y)
				  return 1;
			  else
				  return 0;
		  }
}

