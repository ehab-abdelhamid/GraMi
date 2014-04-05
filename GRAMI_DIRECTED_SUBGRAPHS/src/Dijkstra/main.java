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

package Dijkstra;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Timer;

import automorphism.Automorphism;

import CSP.ConstraintGraph;
import CSP.DFSSearch;

import pruning.SPpruner;

import search.Searcher;
import statistics.DistinctLabelStat;
import statistics.TimedOutSearchStats;

import utilities.CommandLineParser;
import utilities.DfscodesCache;
import utilities.Settings;
import utilities.StopWatch;

import dataStructures.DFSCode;
import dataStructures.DFScodeSerializer;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import decomposer.Decomposer;

public class main {
	static int APPROX=0;
	static int EXACT=1;
	
	static int FSM=0;
	
	
	public static void main(String[] args) 
	{
		int maxNumOfDistinctNodes=1;
		
		//default frequency
		int freq=1000;
		
		//parse the command line arguments
		CommandLineParser.parse(args);
		
		if(utilities.Settings.frequency>-1)
			freq = utilities.Settings.frequency;
		
		Searcher<String, String> sr=null;
		StopWatch watch = new StopWatch();	
		watch.start();
				
		try
		{
			if(Settings.fileName==null)
			{
				System.out.println("You have to specify a filename");
				System.exit(1);
			}
			else
			{
				sr = new Searcher<String, String>(Settings.datasetsFolder+Settings.fileName, freq, 1);
			}
		
			sr.initialize();
			sr.search();
			
			watch.stop();
		
				
			//write output file for the following things:
			//1- time
			//2- number of resulted patterns
			//3- the list of frequent subgraphs
			FileWriter fw;
			try
			{
				String fName = "Output.txt";
			
				fw = new FileWriter(fName);
				fw.write(watch.getElapsedTime()/1000.0+"\n");
				fw.write(sr.result.size()+"\n");
			
				//write the frequent subgraphs
				for (int i = 0; i < sr.result.size(); i++) 
				{		
					String out=DFScodeSerializer.serialize(sr.result.get(i));
				
					fw.write(i+":\n");
					fw.write(out);
				}
				fw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
