package edu.chop.dgd.dgdObjects;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.*; // For HashMap, HashSet


/**
 * Created by jayaramanp on 11/9/16.
 */
public final class DirectedGraph<T> implements Iterable<T> {
        /* A map from nodes in the graph to sets of outgoing edges.  Each
         * set of edges is represented by a map from edges to doubles.
         */
        private final Map<T, Set<T>> mGraph = new HashMap<T, Set<T>>();

        /**
         * Adds a new node to the graph.  If the node already exists, this
         * function is a no-op.
         *
         * @param node The node to add.
         * @return Whether or not the node was added.
         */
        public boolean addNode(T node) {
        /* If the node already exists, don't do anything. */
            if (mGraph.containsKey(node))
                return false;

        /* Otherwise, add the node with an empty set of outgoing edges. */
            mGraph.put(node, new HashSet<T>());
            return true;
        }

        /**
         * Given a start node, and a destination, adds an arc from the start node
         * to the destination.  If an arc already exists, this operation is a
         * no-op.  If either endpoint does not exist in the graph, throws a
         * NoSuchElementException.
         *
         * @param start The start node.
         * @param dest The destination node.
         * @throws NoSuchElementException If either the start or destination nodes
         *                                do not exist.
         */
        public void addEdge(T start, T dest) {
        /* Confirm both endpoints exist. */
            if (!mGraph.containsKey(start) || !mGraph.containsKey(dest))
                throw new NoSuchElementException("Both nodes must be in the graph.");

        /* Add the edge. */
            mGraph.get(start).add(dest);
        }

        /**
         * Removes the edge from start to dest from the graph.  If the edge does
         * not exist, this operation is a no-op.  If either endpoint does not
         * exist, this throws a NoSuchElementException.
         *
         * @param start The start node.
         * @param dest The destination node.
         * @throws NoSuchElementException If either node is not in the graph.
         */
        public void removeEdge(T start, T dest) {
        /* Confirm both endpoints exist. */
            if (!mGraph.containsKey(start) || !mGraph.containsKey(dest))
                throw new NoSuchElementException("Both nodes must be in the graph.");

            mGraph.get(start).remove(dest);
        }

        /**
         * Given two nodes in the graph, returns whether there is an edge from the
         * first node to the second node.  If either node does not exist in the
         * graph, throws a NoSuchElementException.
         *
         * @param start The start node.
         * @param end The destination node.
         * @return Whether there is an edge from start to end.
         * @throws NoSuchElementException If either endpoint does not exist.
         */
        public boolean edgeExists(T start, T end) {
        /* Confirm both endpoints exist. */
            if (!mGraph.containsKey(start) || !mGraph.containsKey(end))
                throw new NoSuchElementException("Both nodes must be in the graph.");

            return mGraph.get(start).contains(end);
        }

        /**
         * Given a node in the graph, returns an immutable view of the edges
         * leaving that node as a set of endpoints.
         *
         * @param node The node whose edges should be queried.
         * @return An immutable view of the edges leaving that node.
         * @throws NoSuchElementException If the node does not exist.
         */
        public Set<T> edgesFrom(T node) {
        /* Check that the node exists. */
            Set<T> arcs = mGraph.get(node);
            if (arcs == null)
                throw new NoSuchElementException("Source node does not exist.");

            return Collections.unmodifiableSet(arcs);
        }

        /**
         * Returns an iterator that can traverse the nodes in the graph.
         *
         * @return An iterator that traverses the nodes in the graph.
         */
        public Iterator<T> iterator() {
            return mGraph.keySet().iterator();
        }

        /**
         * Returns the number of nodes in the graph.
         *
         * @return The number of nodes in the graph.
         */
        public int size() {
            return mGraph.size();
        }

        /**
         * Returns whether the graph is empty.
         *
         * @return Whether the graph is empty.
         */
        public boolean isEmpty() {
            return mGraph.isEmpty();
        }
    }
    /*public LinkedHashMap<String, DirectedGraph> createDAGOfHetDimers(List<OligoObject> oligoKeysList, SequenceObject sObj, int counter, int spacingKB, LinkedHashMap<OligoObject, List<OligoObject>> filteredhetDimerMapForSO) {


        //LinkedHashMap<String, List<OligoObject>> hashSetOfPrimers = new LinkedHashMap<String, List<OligoObject>>();
        LinkedHashMap<String, DirectedGraph> hashMapOfDag = new LinkedHashMap<String, DirectedGraph>();

        String set = "treeset"+counter;

        while(Integer.parseInt(startingOligoHetObject.getInternalStart())-sObj.getStart()<=4000){
            int i=oligoKeysList.indexOf(startingOligoHetObject);

            DirectedGraph<OligoObject, DefaultEdge> g = new DefaultDirectedGraph<OligoObject, DefaultEdge>(DefaultEdge.class);


            for(; i<oligoKeysList.size(); ){

                if(hashMapOfDag.size()==0){
                    if(Integer.parseInt(startingOligoHetObject.getInternalStart())-sObj.getStart()>=2000){
                        //List<OligoObject> setOfHets = new ArrayList<OligoObject>();
                        //setOfHets.add(oligoKeysList.get(i));
                        //hashSetOfPrimers.put(set, setOfHets);

                        g.addVertex(oligoKeysList.get(i));
                        hashMapOfDag.put(set, g);

                        //i++;
                    }else{
                        i++;
                        startingOligoHetObject = oligoKeysList.get(i);
                    }

                }else if(hashMapOfDag.size()>0){
                    if(sObj.getStop()-Integer.parseInt(oligoKeysList.get(i).getInternalStart())>=2000){
                        //List<OligoObject> setOfhets =  new ArrayList<OligoObject>();
                        //if(hashSetOfPrimers.get(set)!=null){
                        //    setOfhets = hashSetOfPrimers.get(set);
                        //}

                        //OligoObject nextHetObj;
                        try {
                            //nextHetObj = new MfoldDimer().getNext8KBOligoObj(oligoKeysList.get(i), oligoKeysList, spacingKB);

                            List<OligoObject> nexthetObjects = new MfoldDimer().getNext8_10KBOligoObjs(oligoKeysList.get(i), oligoKeysList, spacingKB);
                            for(OligoObject nextHetObj : nexthetObjects){

                                if(nextHetObj!=null){

                                    //add to dag and create edge
                                    if(hashMapOfDag.containsKey(set) && hashMapOfDag.get(set) != null){
                                        DirectedGraph<OligoObject, DefaultEdge> dagObj = hashMapOfDag.get(set);
                                        dagObj.addVertex(nextHetObj);
                                        dagObj.addEdge(oligoKeysList.get(i), nextHetObj);
                                        hashMapOfDag.put(set, dagObj);

                                    }else{
                                        System.out.println("it shouldnt not have a key.. what's going on??");
                                        throw new Exception("breaking at the DAG creation step");
                                    }

                                    //add to hash set
                                    i = oligoKeysList.indexOf(nextHetObj);
                                    //setOfhets.add(nextHetObj);
                                    //hashSetOfPrimers.put(set, setOfhets);


                                }else{

                                    //hashSetOfPrimers.put(set, setOfhets);

                                    //increase Counter start new set
                                    counter+=1;
                                    set = "set"+counter;
                                    i=oligoKeysList.indexOf(startingOligoHetObject);
                                    i+=1;
                                    startingOligoHetObject=oligoKeysList.get(i);
                                    break;
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }else{
                        counter+=1;
                        set = "set"+counter;
                        i=oligoKeysList.indexOf(startingOligoHetObject);
                        i+=1;
                        if(i<oligoKeysList.size()){
                            startingOligoHetObject=oligoKeysList.get(i);
                        }
                        break;
                        //something here!!
                    }
                }

                if(i>=oligoKeysList.size()){
                    break;
                }
            }

            if(i>=oligoKeysList.size()){
                break;
            }
        }


        return hashMapOfDag;

    }*/


