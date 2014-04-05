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

public class CommandLineParser {

	public static void parse(String[] args)
	{
		for(int i=0;i<args.length;i++)
		{
			String[] parts = args[i].split("=");
			String key = parts[0];
			String value = parts[1];
			
			//fill in the cases
			//get frequency
			if(key.compareTo("freq")==0)
				Settings.frequency = Integer.parseInt(value);
			//get the dataset filename
			if(key.compareTo("filename")==0)
				Settings.fileName = value;
			//assign the datasets folder
			if(key.compareTo("datasetFolder")==0)
				Settings.datasetsFolder = value;
			//assign the max number of label appearance
			if(key.compareTo("maxLabelAppearance")==0)
				Settings.numLabelAppears = Integer.parseInt(value);
			//approximate setting
			if(key.compareTo("approximate")==0)
			{
				double d = Double.parseDouble(value);
				if(d<1)
				{
					Settings.isApproximate = true;
					Settings.approxEpsilon = Double.parseDouble(value);
				}
				else
					Settings.isApproximate = false;
			}
			if(key.compareTo("approxConst")==0)
			{
				Settings.approxConstant = Double.parseDouble(value);
			}
			
			//set optimization parameters
			//Automorphism
			if(key.compareTo("automorphism")==0)
				Settings.isAutomorphismOn = (value.compareTo("true")==0);
			//Distinct labels
			if(key.compareTo("distinct")==0)
				Settings.DISTINCTLABELS = (value.compareTo("true")==0);
			//caching substructures
			if(key.compareTo("caching")==0)
				Settings.CACHING = (value.compareTo("true")==0);
			//partial consistency
			if(key.compareTo("partial")==0)
				Settings.isDecomposeOn = (value.compareTo("true")==0);
			//limited time
			if(key.compareTo("limitedtime")==0)
				Settings.LimitedTime = (value.compareTo("true")==0);
		}
	}
}
