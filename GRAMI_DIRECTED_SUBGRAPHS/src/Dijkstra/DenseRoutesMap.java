package Dijkstra;

//TODO change the distances and all functions to be triangular !!

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.IntIterator;


/**
 * This map stores routes in a matrix, a nxn array. It is most useful when
 * there are lots of routes, otherwise using a sparse representation is
 * recommended.
 * 
 * @author Renaud Waldura &lt;renaud+tw@waldura.com&gt;
 * @version $Id: DenseRoutesMap.java 2367 2007-08-20 21:47:25Z renaud $
 */

public class DenseRoutesMap
	implements RoutesMap
{
	private HPListGraph<Integer, Double> listGraph;
	
	public DenseRoutesMap(Graph currentGraph)
	{
		listGraph=currentGraph.getListGraph();
	}
	
	/**
	 * Link two cities by a direct route with the given distance.
	 */
	public void addDirectRoute(int start, int end, int distance)
	{
	}
	
	/**
	 * @return the distance between the two cities, or 0 if no path exists.
	 */
	public double getDistance(int start, int end)
	{
		int index=listGraph.getEdge(start, end);
		return listGraph.getEdgeLabel(index);
	}
	
	/**
	 * @return the list of all valid destinations from the given city.
	 */
	public List<Integer> getDestinations(int city)
	{
		List<Integer> list = new ArrayList<Integer>();
		IntIterator it= listGraph.getEdgeIndices(city);
		for (; it.hasNext();) 
		{
			 int edge=  it.next();
			 if(listGraph.getDirection(edge, city)<0)
				 continue;
			 list.add(listGraph.getOtherNode(edge, city));
			
		}		
		return list;
	}
}
