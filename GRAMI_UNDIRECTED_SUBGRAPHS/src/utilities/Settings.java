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

package utilities;

public class Settings 
{

	public static boolean isApproximate=false;   //EXACT
	public static double approxEpsilon = 1.0;
	public static double approxConstant = 0;
	
	public static boolean isAutomorphismOn= true;
	
	public static boolean isDecomposeOn= true;

	public static boolean CACHING = true;
	
	public static boolean DISTINCTLABELS = true;
	
	public static boolean LimitedTime = true;

	public static boolean PRINT = false;
	
	public static boolean multipleAtts = false;
		
	//datasets folder
	public static String datasetsFolder = "../Datasets/";
	
	//given frequency, if not given then its value is -1
	public static int frequency = -1;
	
		//the filename
	public static String fileName = null;
}
