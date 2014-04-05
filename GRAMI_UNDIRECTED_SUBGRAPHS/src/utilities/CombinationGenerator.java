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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CombinationGenerator<E> 
        implements Iterator<List<E>>, Iterable<List<E>> {

    private final List<E> set;
    private int[] currentIdxs;
    private final int[] lastIdxs;
    
    public CombinationGenerator(List<E> set, int r) {
        if(r < 1 || r > set.size()) {
            throw new IllegalArgumentException("r < 1 || r > set.size()");
        }
        this.set = new ArrayList<E>(set);
        this.currentIdxs = new int[r];
        this.lastIdxs = new int[r];
        for(int i = 0; i < r; i++) {
            this.currentIdxs[i] = i;
            this.lastIdxs[i] = set.size() - r + i;
        }
    }

    public boolean hasNext() {
        return currentIdxs != null;
    }

    public Iterator<List<E>> iterator() {
        return this;
    }
    
    public List<E> next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        List<E> currentCombination = new ArrayList<E>();
        for(int i : currentIdxs) {
            currentCombination.add(set.get(i));
        }
        setNextIndexes();
        return currentCombination;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private void setNextIndexes() {
        for(int i = currentIdxs.length-1, j = set.size()-1; i >= 0; i--, j--) {
            if(currentIdxs[i] != j) {
                currentIdxs[i]++;
                for(int k = i+1; k < currentIdxs.length; k++) {
                    currentIdxs[k] = currentIdxs[k-1]+1;
                }
                return;
            }
        }
        currentIdxs = null;
    }
    
    
    public static String convertToString(List<Integer> combination)
    {
    	String out="";
    	for (int i = 0; i < combination.size(); i++) 
    	{
    		if(i==combination.size()-1)
    			out+=combination.get(i);
    		else
    			out+=combination.get(i)+"-";
		}
    	return out;
    }
    
    public static ArrayList<String> getNewCombinations(ArrayList<Integer> prevLabels, int newLabel)
    {
    	ArrayList<String> newCombs= new ArrayList<String>();
    	int size = prevLabels.size();
    	Collections.sort(prevLabels);
    	for (int i = 1; i <= size; i++) 
    	{
    		CombinationGenerator<Integer> cg = new CombinationGenerator<Integer>(prevLabels, i);
            for(List<Integer> combination : cg) 
            {
            	combination.add(newLabel);
            	Collections.sort(combination);
            	String out = convertToString(combination);
            	newCombs.add(out);
            }
		}
    	return newCombs;
    }
    
    
    public static void main(String[] args) 
    {
    	ArrayList<Integer> prevLabels = new ArrayList<Integer>();
    	prevLabels.add(5);prevLabels.add(2);prevLabels.add(7);prevLabels.add(1);
    	
    	
    	ArrayList<String> combs= getNewCombinations(prevLabels, 4);
    	for (int i = 0; i < combs.size(); i++) 
    	{
			System.out.println(combs.get(i));
		}
        
        
    }
}