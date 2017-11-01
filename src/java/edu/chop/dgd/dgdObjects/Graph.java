package edu.chop.dgd.dgdObjects;

import com.google.common.collect.Multimap;

import java.util.*;


/**
 * Created by jayaramanp on 11/10/16.
 */
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */



/**
 * A directed graph data structure.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <String>
 */
@SuppressWarnings("unchecked")
public class Graph<String> {
    /** Color used to mark unvisited nodes */
    public static final int VISIT_COLOR_WHITE = 1;

    /** Color used to mark nodes as they are first visited in DFS order */
    public static final int VISIT_COLOR_GREY = 2;

    /** Color used to mark nodes after descendants are completely visited */
    public static final int VISIT_COLOR_BLACK = 3;

    /** Vector<Vertex> of graph verticies */
    public ArrayList<Vertex<String>> verticies;

    /** Vector<Edge> of edges in the graph */
    public ArrayList<Edge<String>> edges;

    /** The vertex identified as the root of the graph */
    public Vertex<String> rootVertex;

    public LinkedHashMap<String, ArrayList<OligoObject>> mapOfOligoPathArrays;
    public Multimap<java.lang.String, java.lang.String> mapOfOligoidsPathMultimapArrays;

    /**
     * Construct a new graph without any vertices or edges
     */
    public Graph() {
        //verticies = new ArrayList<Vertex<OligoObject>>();
        //edges = new ArrayList<Edge<OligoObject>>();

        verticies = new ArrayList<Vertex<String>>();
        edges = new ArrayList<Edge<String>>();
    }

    /**
     * Are there any verticies in the graph
     *
     * @return true if there are no verticies in the graph
     */
    public boolean isEmpty() {
        return verticies.size() == 0;
    }

    /**
     * Add a vertex to the graph
     *
     * @param v
     *          the Vertex to add
     * @return true if the vertex was added, false if it was already in the graph.
     */
    public boolean addVertex(Vertex<String> v) {
        boolean added = false;
        if (verticies.contains(v) == false) {
            added = verticies.add(v);
        }
        return added;
    }

    /**
     * Get the vertex count.
     *
     * @return the number of verticies in the graph.
     */
    public int size() {
        return verticies.size();
    }

    /**
     * Get the root vertex
     *
     * @return the root vertex if one is set, null if no vertex has been set as
     *         the root.
     */
    public Vertex<String> getRootVertex() {
        return rootVertex;
    }

    /**
     * Set a root vertex. If root does no exist in the graph it is added.
     *
     * @param root -
     *          the vertex to set as the root and optionally add if it does not
     *          exist in the graph.
     */
    public void setRootVertex(Vertex<String> root) {
        this.rootVertex = root;
        if (verticies.contains(root) == false)
            this.addVertex(root);
    }

    /**
     * Get the given Vertex.
     *
     * @param n
     *          the index [0, size()-1] of the Vertex to access
     * @return the nth Vertex
     */
    public Vertex<String> getVertex(int n) {
        return verticies.get(n);
    }

    /**
     * Get the graph verticies
     *
     * @return the graph verticies
     */
    public List<Vertex<String>> getVerticies() {
        return this.verticies;
    }

    /**
     * Insert a directed, weighted Edge<T> into the graph.
     *
     *
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @param cost -
     *          the Edge<T> weight/cost
     * @return true if the Edge<T> was added, false if from already has this Edge<T>
     * @throws IllegalArgumentException
     *           if from/to are not verticies in the graph
     */
    public boolean addEdge(Vertex<String> from, Vertex<String> to, int cost) throws IllegalArgumentException {
        if (verticies.contains(from) == false)
            throw new IllegalArgumentException("from is not in graph");
        if (verticies.contains(to) == false)
            throw new IllegalArgumentException("to is not in graph");

        Edge<String> e = new Edge<String>(from, to, cost);
        if (from.findEdge(to) != null)
            return false;
        else {
            from.addEdge(e);
            to.addEdge(e);
            edges.add(e);
            return true;
        }
    }

    /**
     * Insert a bidirectional Edge<T> in the graph
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @param cost -
     *          the Edge<T> weight/cost
     * @return true if edges between both nodes were added, false otherwise
     * @throws IllegalArgumentException
     *           if from/to are not verticies in the graph
     */
    public boolean insertBiEdge(Vertex<String> from, Vertex<String> to, int cost)
            throws IllegalArgumentException {
        return addEdge(from, to, cost) && addEdge(to, from, cost);
    }

    /**
     * Get the graph edges
     *
     * @return the graph edges
     */
    public List<Edge<String>> getEdges() {
        return this.edges;
    }

    /**
     * Remove a vertex from the graph
     *
     * @param v
     *          the Vertex to remove
     * @return true if the Vertex was removed
     */
    public boolean removeVertex(Vertex<String> v) {
        if (!verticies.contains(v))
            return false;

        verticies.remove(v);
        if (v == rootVertex)
            rootVertex = null;

        // Remove the edges associated with v
        for (int n = 0; n < v.getOutgoingEdgeCount(); n++) {
            Edge<String> e = v.getOutgoingEdge(n);
            v.remove(e);
            Vertex<String> to = e.getTo();
            to.remove(e);
            edges.remove(e);
        }
        for (int n = 0; n < v.getIncomingEdgeCount(); n++) {
            Edge<String> e = v.getIncomingEdge(n);
            v.remove(e);
            Vertex<String> predecessor = e.getFrom();
            predecessor.remove(e);
        }
        return true;
    }

    /**
     * Remove an Edge<T> from the graph
     *
     * @param from -
     *          the Edge<T> starting vertex
     * @param to -
     *          the Edge<T> ending vertex
     * @return true if the Edge<T> exists, false otherwise
     */
    public boolean removeEdge(Vertex<String> from, Vertex<String> to) {
        Edge<String> e = from.findEdge(to);
        if (e == null)
            return false;
        else {
            from.remove(e);
            to.remove(e);
            edges.remove(e);
            return true;
        }
    }

    /**
     * Clear the mark state of all verticies in the graph by calling clearMark()
     * on all verticies.
     *
     * @see Vertex#clearMark()
     */
    public void clearMark() {
        for (Vertex<String> w : verticies)
            w.clearMark();
    }

    /**
     * Clear the mark state of all edges in the graph by calling clearMark() on
     * all edges.
     */
    public void clearEdges() {
        for (Edge<String> e : edges)
            e.clearMark();
    }

    /**
     * Perform a depth first serach using recursion.
     *
     * @param v -
     *          the Vertex to start the search from
     * @param visitor -
     *          the vistor to inform prior to
     * @see Visitor#visit(Graph, Vertex)
     */
    public void depthFirstSearch(Vertex<String> v, final Visitor<String> visitor) {
        VisitorEX<String, RuntimeException> wrapper = new VisitorEX<String, RuntimeException>() {
            public void visit(Graph<String> g, Vertex<String> v) throws RuntimeException {
                if (visitor != null)
                    visitor.visit(g, v);
            }
        };
        this.depthFirstSearch(v, wrapper);
    }

    /**
     * Perform a depth first serach using recursion. The search may be cut short
     * if the visitor throws an exception.
     *
     * @param <E>
     *
     * @param v -
     *          the Vertex to start the search from
     * @param visitor -
     *          the vistor to inform prior to
     * @see Visitor#visit(Graph, Vertex)
     * @throws E
     *           if visitor.visit throws an exception
     */
    public <E extends Exception> void depthFirstSearch(Vertex<String> v, VisitorEX<String, E> visitor) throws E {
        if (visitor != null)
            visitor.visit(this, v);
        v.visit();
        for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
            Edge<String> e = v.getOutgoingEdge(i);
            if (!e.getTo().visited()) {
                depthFirstSearch(e.getTo(), visitor);
            }
        }
    }

    /**
     * Perform a breadth first search of this graph, starting at v.
     *
     * @param v -
     *          the search starting point
     * @param visitor -
     *          the vistor whose vist method is called prior to visting a vertex.
     */
    public void breadthFirstSearch(Vertex<String> v, final Visitor<String> visitor) {
        VisitorEX<String, RuntimeException> wrapper = new VisitorEX<String, RuntimeException>() {
            public void visit(Graph<String> g, Vertex<String> v) throws RuntimeException {
                if (visitor != null)
                    visitor.visit(g, v);
            }
        };
        this.breadthFirstSearch(v, wrapper);
    }

    /**
     * Perform a breadth first search of this graph, starting at v. The vist may
     * be cut short if visitor throws an exception during a vist callback.
     *
     * @param <E>
     *
     * @param v -
     *          the search starting point
     * @param visitor -
     *          the vistor whose vist method is called prior to visting a vertex.
     * @throws E
     *           if vistor.visit throws an exception
     */
    public <E extends Exception> void breadthFirstSearch(Vertex<String> v, VisitorEX<String, E> visitor)
            throws E {
        LinkedList<Vertex<String>> q = new LinkedList<Vertex<String>>();

        q.add(v);
        if (visitor != null)
            visitor.visit(this, v);
        v.visit();
        while (q.isEmpty() == false) {
            v = q.removeFirst();
            for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
                Edge<String> e = v.getOutgoingEdge(i);
                Vertex<String> to = e.getTo();
                if (!to.visited()) {
                    q.add(to);
                    if (visitor != null)
                        visitor.visit(this, to);
                    to.visit();
                }
            }
        }
    }

    /**
     * Find the spanning tree using a DFS starting from v.
     *
     * @param v -
     *          the vertex to start the search from
     * @param pathArrays
     * @param visitor -
     *          visitor invoked after each vertex is visited and an edge is added
     */
    public void dfsSpanningTree(Vertex<String> v, ArrayList<String> pathArrays, int count, Graph<String> dagOligo, DFSVisitor<String> visitor) {
        v.visit();
        if (visitor != null)
            pathArrays.add(v.getName());
            visitor.visit(this, v);

        //added to spit out array
        System.out.println("Count value is:"+ count);

        if(v.getOutgoingEdgeCount()==0){
            if(dagOligo.getMapOfOligoidsPathMultimapArrays().size()>0){
                //LinkedHashMap<String, ArrayList<edu.chop.dgd.dgdObjects.OligoObject>> oligoArraysMap = dagOligo.getMapOfOligoPathArrays();
                //MapOfOligoidsPathMultimapArrays
                Multimap<java.lang.String, java.lang.String> oligoArraysMap = dagOligo.getMapOfOligoidsPathMultimapArrays();
                count = oligoArraysMap.size()+1;
                java.lang.String key = "path"+count;
                for(String path : pathArrays){
                    oligoArraysMap.put(key, (java.lang.String) path);
                }

                dagOligo.setMapOfOligoidsPathMultimapArrays(oligoArraysMap);
                count+=1;
                //System.out.println("count is now!" + count);
            }else{

                Multimap<java.lang.String, java.lang.String> oligoArraysMap = dagOligo.getMapOfOligoidsPathMultimapArrays();
                java.lang.String key = ("path"+count);

                for(String path : pathArrays){
                    oligoArraysMap.put(key, (java.lang.String) path);
                }

                dagOligo.setMapOfOligoidsPathMultimapArrays(oligoArraysMap);
                count+=1;
                System.out.println("count is:" + count);
            }

            //System.out.println("\n");
        }


        for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
            Edge<String> e = v.getOutgoingEdge(i);
            if (!e.getTo().visited()) {
                if (visitor != null)
                    visitor.visit(this, v, e);
                e.mark();
                dfsSpanningTree(e.getTo(), new ArrayList<String>(pathArrays), count, dagOligo, visitor);
            }
        }
    }

    /**
     * Search the verticies for one with name.
     *
     * @param name -
     *          the vertex name
     * @return the first vertex with a matching name, null if no matches are found
     */
    public Vertex<String> findVertexByName(String name) {
        Vertex<String> match = null;
        for (Vertex<String> v : verticies) {
            if (name.equals(v.getName())) {
                match = v;
                break;
            }
        }
        return match;
    }

    /**
     * Search the verticies for one with data.
     *
     * @param data -
     *          the vertex data to match
     * @param compare -
     *          the comparator to perform the match
     * @return the first vertex with a matching data, null if no matches are found
     */
    public Vertex<String> findVertexByData(String data, Comparator<String> compare) {
        Vertex<String> match = null;
        for (Vertex<String> v : verticies) {
            if (compare.compare(data, v.getData()) == 0) {
                match = v;
                break;
            }
        }
        return match;
    }

    /**
     * Search the graph for cycles. In order to detect cycles, we use a modified
     * depth first search called a colored DFS. All nodes are initially marked
     * white. When a node is encountered, it is marked grey, and when its
     * descendants are completely visited, it is marked black. If a grey node is
     * ever encountered, then there is a cycle.
     *
     * @return the edges that form cycles in the graph. The array will be empty if
     *         there are no cycles.
     */
    public Edge<String>[] findCycles() {
        ArrayList<Edge<String>> cycleEdges = new ArrayList<Edge<String>>();
        // Mark all verticies as white
        for (int n = 0; n < verticies.size(); n++) {
            Vertex<String> v = getVertex(n);
            v.setMarkState(VISIT_COLOR_WHITE);
        }
        for (int n = 0; n < verticies.size(); n++) {
            Vertex<String> v = getVertex(n);
            visit(v, cycleEdges);
        }

        Edge<String>[] cycles = new Edge[cycleEdges.size()];
        cycleEdges.toArray(cycles);
        return cycles;
    }

    public void visit(Vertex<String> v, ArrayList<Edge<String>> cycleEdges) {
        LinkedHashMap<String, ArrayList<String>> pathMap = new LinkedHashMap<String, ArrayList<String>>();

        ArrayList<String> pathList = new ArrayList<String>();
        v.setMarkState(VISIT_COLOR_GREY);
        System.out.println("Visiting vertex:"+v.getName());

        int count = v.getOutgoingEdgeCount();
        for (int n = 0; n < count; n++) {

            Edge<String> e = v.getOutgoingEdge(n);
            //System.out.println("Getting edges:"+e.toString());

            Vertex<String> u = e.getTo();
            if (u.getMarkState() == VISIT_COLOR_GREY) {
                // A cycle Edge<T>
                cycleEdges.add(e);
            } else if (u.getMarkState() == VISIT_COLOR_WHITE) {
                visit(u, cycleEdges);
            }
        }
        v.setMarkState(VISIT_COLOR_BLACK);

    }

    /*public String toString() {
        StringBuffer tmp = new StringBuffer("Graph[");
        for (Vertex<String> v : verticies)
            tmp.append(v);
        tmp.append(']');
        return (String) tmp.toString();
    }*/


    public LinkedHashMap<String, ArrayList<OligoObject>> getMapOfOligoPathArrays() {
        return mapOfOligoPathArrays;
    }

    public void setMapOfOligoPathArrays(LinkedHashMap<String, ArrayList<OligoObject>> mapOfOligoPathArrays) {
        this.mapOfOligoPathArrays = mapOfOligoPathArrays;
    }

    public Multimap<java.lang.String, java.lang.String> getMapOfOligoidsPathMultimapArrays() {
        return mapOfOligoidsPathMultimapArrays;
    }

    public void setMapOfOligoidsPathMultimapArrays(Multimap<java.lang.String, java.lang.String> mapOfOligoidsPathMultimapArrays) {
        this.mapOfOligoidsPathMultimapArrays = mapOfOligoidsPathMultimapArrays;
    }
}




/*
 * JBoss, Home of Professional Open Source Copyright 2006, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * A graph visitor interface that can throw an exception during a visit
 * callback.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <String>
 * @param <E>
 */
interface VisitorEX<String, E extends Exception> {
    /**
     * Called by the graph traversal methods when a vertex is first visited.
     *
     * @param g -
     *          the graph
     * @param v -
     *          the vertex being visited.
     * @throws E
     *           exception for any error
     */
    public void visit(Graph<String> g, Vertex<String> v) throws E;
}

