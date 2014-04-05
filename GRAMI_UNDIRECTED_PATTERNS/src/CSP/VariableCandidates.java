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

package CSP;

import java.util.ArrayList;

public class VariableCandidates 
{

	private int variableID;
	private ArrayList<Integer> candidates;
	
	public VariableCandidates(int variableID,ArrayList<Integer> candidates) 
	{
		this.variableID=variableID;
		this.candidates=candidates;
	}

	public int getVariableID() {
		return variableID;
	}

	public ArrayList<Integer> getCandidates() {
		return candidates;
	}
	
	
}
