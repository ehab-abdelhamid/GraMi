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

package statistics;

import java.util.ArrayList;

import dataStructures.myNode;

public class TimedOutSearchStats {
	public static long totalNumber;
	public static long numberOfDomains;
	public static long maximum = 0;
		
	public static long getElementSize()
	{
		return 56;
	}
	
	public static double getAverage()
	{
		return totalNumber/numberOfDomains;
	}
	
	public static String getData()
	{
		return "Total: "+totalNumber+", Domains: "+numberOfDomains+", Average:"+getAverage()+", maximum: "+maximum;
	}
}
