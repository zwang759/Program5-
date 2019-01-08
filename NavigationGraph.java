/////////////////////////////////////////////////////////////////////////////
// Semester: CS367 Spring 2017
// PROJECT: Program 5
// FILE: GraphADT.java, GraphNode.java, InvalidFileException.java,
//		 Location.java, MapApp.java, NavigationGraph.java, Path.java
//
// TEAM: p5 team 23
// Authors: Matthew Schmude, Xuezhan Yan, Nick Klabjan, Meredith Lou, 
//			Zhiheng Wang, Jingyao Wei
// Author1: Matthew Schmude, schmude@wisc.edu, 9074395576, Lec 002
// Author2: Xuezhan Yan, xyan56@wisc.edu, 9074973794, Lec 002
// Author3: Nick Klabjan, klabjan@wisc.edu, 9074842692, Lec 002
// Author4: Meredith Lou, ylou9@wisc.edu, 9071561857, Lece 001
// Author5: Zhiheng Wang, zwang759@wisc.edu, 9074796922, Lec 003
// Author6: Jingyao Wei, jwei44@wisc.edu, 9074518243, Lec 001
//
// ---------------- OTHER ASSISTANCE CREDITS
// Persons: Identify persons by name, relationship to you, and email.
// Describe in detail the the ideas and help they provided.
//
// Online sources: avoid web searches to solve your problems, but if you do
// search, be sure to include Web URLs and description of
// of any information you find.
//////////////////////////// 80 columns wide //////////////////////////////////

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class NavigationGraph implements GraphADT<Location, Path> {
	private String[] edgePropertyNames;
	private List<GraphNode<Location, Path>> vertices;
	private int numid = 0; // numid is to trace index of vertices and DijkstrasList

	public NavigationGraph(String[] edgePropertyNames) {
		this.edgePropertyNames = edgePropertyNames;
		vertices = new ArrayList<GraphNode<Location, Path>>();
	}

	/**
	 * Returns a Location object given its name
	 * 
	 * @param name
	 *            name of the location
	 * @return Location object
	 */
	public Location getLocationByName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Please enter non-null location name");
		// traverse whole vertices list to get location object with specific name
		for (GraphNode<Location, Path> node : vertices)
			if (node.getVertexData().getName().equals(name))
				return node.getVertexData();
		return null;
	}

	@Override
	/**
	 * Adds a vertex to the Graph
	 * 
	 * @param vertex
	 *            vertex to be added
	 */
	public void addVertex(Location vertex) {
		int id = getID(vertex);
		if (id == -1) {
			// do not have this source vertex
			vertices.add(new GraphNode<Location, Path>(vertex, numid++));
		}
	}

	@Override
	/**
	 * Creates a directed edge from src to dest
	 * 
	 * @param src
	 *            source vertex from where the edge is outgoing
	 * @param dest
	 *            destination vertex where the edge is incoming
	 * @param edge
	 *            edge between src and dest
	 */
	public void addEdge(Location src, Location dest, Path edge) {
		int id = getID(src);
		if (id == -1) {
			// do not have this source vertex
			List<Path> temp = new ArrayList<Path>();
			temp.add(edge);
			vertices.add(new GraphNode<Location, Path>(src, temp, 0));
		} else {
			// have this source vertex, add this path to existing path list
			for (GraphNode<Location, Path> node : vertices) {
				if (node.getId() == id) {
					node.getOutEdges().add(edge);
					return;
				}
			}
		}
	}

	@Override
	/**
	 * Getter method for the vertices
	 * 
	 * @return List of vertices of type V
	 */
	public List<Location> getVertices() {
		List<Location> temp = new ArrayList<Location>();
		for (GraphNode<Location, Path> node : vertices)
			temp.add(node.getVertexData());
		return temp;
	}

	@Override
	/**
	 * Returns edge if there is one from src to dest vertex else null
	 * 
	 * @param src
	 *            Source vertex
	 * @param dest
	 *            Destination vertex
	 * @return Edge of type E from src to dest
	 */
	public Path getEdgeIfExists(Location src, Location dest) {
		for (GraphNode<Location, Path> node : vertices)
			if (node.getVertexData().equals(src)) // if contains source
				for (Path path : node.getOutEdges())
					if (path.getDestination().equals(dest)) // if contains destination
						return path;
		return null;
	}

	@Override
	/**
	 * Returns the outgoing edges from a vertex
	 * 
	 * @param src
	 *            Source vertex for which the outgoing edges need to be obtained
	 * @return List of edges of type E
	 */
	public List<Path> getOutEdges(Location src) {
		for (GraphNode<Location, Path> node : vertices)
			if (node.getVertexData().equals(src))
				return node.getOutEdges();
		return null;
	}

	@Override
	/**
	 * Returns neighbors of a vertex
	 * 
	 * @param vertex
	 *            vertex for which the neighbors are required
	 * @return List of vertices(neighbors) of type V
	 */
	public List<Location> getNeighbors(Location vertex) {
		// neighbors means two ways connected (source and destination)
		List<Location> temp = new ArrayList<Location>();
		for (GraphNode<Location, Path> node : vertices)
			// if it is "vertex" is source
			if (node.getVertexData().equals(vertex))
				for (Path path : node.getOutEdges())
					temp.add(path.getDestination());
//			// if it is "vertex" is destination
//			else
//				for (Path path : node.getOutEdges())
//					if (path.getDestination().equals(vertex))
//						temp.add(path.getDestination());
		return temp;
	}

	@Override
	/**
	 * Calculate the shortest route from src to dest vertex using edgePropertyName
	 * 
	 * @param src
	 *            Source vertex from which the shortest route is desired
	 * @param dest
	 *            Destination vertex to which the shortest route is desired
	 * @param edgePropertyName
	 *            edge property by which shortest route has to be calculated
	 * @return List of edges that denote the shortest route by edgePropertyName
	 */
	public List<Path> getShortestRoute(Location src, Location dest, String edgePropertyName) {
		if (src == null || dest == null)
			throw new IllegalArgumentException("Please enter non-null locations");
		if (src == dest)
			throw new IllegalArgumentException(
					"Please enter distinct locations for source and destionation");
		List<Path> pathList = new ArrayList<Path>();
		// get index of which property matters
		int properties = Arrays.asList(this.edgePropertyNames).indexOf(edgePropertyName);

		// *** declare new class ***
		// <package> class that store graph node data, visited state, total weight, predecessor
		class DijkstrasNode {
			// initialize each vertex visited mark to false
			boolean verticesVisited = false;
			// initialize each vertex total weight to "infinity"
			int totalWeight = Integer.MAX_VALUE;
			// initialize each vertex predecessor to null
			DijkstrasNode predecessor = null;
			// id containing the same info as GraphNode's id
			// this is also a connection bw DijkstrasNode and GraphNode and indexing in the list
			int id;
			// reference to graphNode
			GraphNode<Location, Path> GraphNodeData;
		}

		// *** Dijkstras algorithm ***
		// Dijkstraslist is to represent more info of all vertices
		List<DijkstrasNode> Dijkstraslist = new ArrayList<DijkstrasNode>(vertices.size());
		// linking every elements in Dijkstraslist to vertices list, with respect to the indexing
		for (int i = 0; i < vertices.size(); i++) {
			Dijkstraslist.add(new DijkstrasNode());
			Dijkstraslist.get(i).GraphNodeData = vertices.get(i);
			// update id
			Dijkstraslist.get(i).id = vertices.get(i).getId();
		}

		// *** define comparator to pass to pq to compare totalWeight ***
		Comparator<DijkstrasNode> comparator = new Comparator<DijkstrasNode>() {
			@Override
			public int compare(DijkstrasNode d1, DijkstrasNode d2) {
				return (int) ((Integer) d1.totalWeight).compareTo(((Integer) d2.totalWeight));
			}
		};

		// create new priority queue pq
		PriorityQueue<DijkstrasNode> pq = new PriorityQueue<DijkstrasNode>(comparator);
		// traverse to find start vertex
		for (int i = 0; i < Dijkstraslist.size(); i++) {
			if (vertices.get(i).getVertexData().equals(src)) {
				DijkstrasNode startNode = Dijkstraslist.get(i);
				// set start vertex's visited state to true
				// startNode.verticesVisited = true;
				// mark visited true in the following while loop
				// set start vertex's total weight to 0
				startNode.totalWeight = 0;
				pq.add(startNode); // same as insert()
				break;
			}
		}
		while (!pq.isEmpty()) {
			// pick next unvisited vertex with lowest calculated distance
			DijkstrasNode curr = pq.poll(); // same as removeMin()
			
			/*
			 * the reason to do this condition is bc i am not update S's total weight in pq instead
			 * insert different total weight in to pq, and pq will always pop min total weight if
			 * poped node have already visited, it means this data should be updated
			 */
			if (!curr.verticesVisited) {

				curr.verticesVisited = true; // set visited state to true
				// for each unvisited successor S adjacent to curr
				for (Path path : curr.GraphNodeData.getOutEdges()) {
					DijkstrasNode S = Dijkstraslist.get(getID(path.getDestination()));
					// if this successor is not visited yet
					if (!S.verticesVisited) {
						// if S's total weight can be reduced
						// S's total weight = curr's total weight + edge weight from curr to S
						int newtotalWeight = (int) (curr.totalWeight
								+ getEdgeIfExists(curr.GraphNodeData.getVertexData(),
										S.GraphNodeData.getVertexData()).getProperties()
												.get(properties));
						if (S.totalWeight > newtotalWeight) {
							S.totalWeight = newtotalWeight;
							// update S's predecessor to curr
							S.predecessor = curr;
						}
						// pq.insert (S's total weight, S)
						pq.add(S);
					}
				}
			}
		}

		// *** add paths to array ***
		DijkstrasNode curr = Dijkstraslist.get(getID(dest));
		while (curr.predecessor != null) {
			for (Path path : curr.predecessor.GraphNodeData.getOutEdges())
				if (path.getDestination().equals(curr.GraphNodeData.getVertexData()))
					pathList.add(0, path);
			curr = curr.predecessor;
		}
		return pathList;
	}

	@Override
	/**
	 * Getter method for edge property names
	 * 
	 * @return array of String that denotes the edge property names
	 */
	public String[] getEdgePropertyNames() {
		return edgePropertyNames;
	}

	/**
	 * Return a string representation of the graph
	 * 
	 * @return String representation of the graph
	 */
	public String toString() {
		String string = "";
		for (int i = 0; i < vertices.size(); i++) {
			for (int j = 0; j < vertices.get(i).getOutEdges().size(); j++) {
				string += vertices.get(i).getVertexData().toString() + " ";
				for (double properties : vertices.get(i).getOutEdges().get(j).getProperties())
					string += properties + " ";
				string += vertices.get(i).getOutEdges().get(j).getDestination();
				if (j < vertices.get(i).getOutEdges().size() - 1)
					string += ",";
			}
			if (i < vertices.size() - 1)
				string += "\n";
		}
		return string;
	}

	/**
	 * helper method to get id with location input
	 * 
	 * @param loc
	 *            specific location
	 * @return id num or -1 when not find in the vertices list
	 */
	private int getID(Location loc) {
		for (int i = 0; i < vertices.size(); i++)
			if (vertices.get(i).getVertexData().equals(loc))
				return i;
		return -1;
	}
}
