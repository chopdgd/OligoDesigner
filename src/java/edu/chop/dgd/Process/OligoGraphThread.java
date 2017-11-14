package edu.chop.dgd.Process;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import edu.chop.dgd.dgdObjects.*;
import edu.chop.dgd.dgdUtils.OligoUtils;
import org.mapdb.HTreeMap;

import java.util.*;
import java.util.concurrent.Callable;

public class OligoGraphThread implements Callable<Multimap<String, String>> {

	private int threadCount;
    private String oligoobjid;
    private HTreeMap<String, Object> hetDimerMapForSO_mapDB_sorted;
    private Multimap<String, String> filteredHetDimerMapForSO_multimap;
    private SequenceObject so;
    private int startingcounter;
    private HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb;
    private int spacing;



    public OligoGraphThread(String oligoobjid, HTreeMap<String, Object> hetDimerMapForSO_mapDB_sorted, Multimap<String, String> filteredHetDimerMapForSO_multimap, SequenceObject so, int threadcount, int startingcounter, HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb, int spacing) {
        this.threadCount = threadcount;
        this.oligoobjid = oligoobjid;
        this.hetDimerMapForSO_mapDB_sorted = hetDimerMapForSO_mapDB_sorted;
        this.filteredHetDimerMapForSO_multimap = filteredHetDimerMapForSO_multimap;
        this.so = so;
        this.startingcounter = startingcounter;
        this.allHetDimerPairsObjectsMapMapdb = allHetDimerPairsObjectsMapMapdb;
        this.spacing = spacing;
    }


    /**
     *
     * @return
     * @throws Exception
     */
    public Multimap<String, String> call() throws Exception {

        Multimap<String, String> setOfOligosMultimapFromSeed = LinkedListMultimap.create();
        System.out.println("Job:" + String.valueOf(this.threadCount) + " starting.");

        OligoObject obj = (OligoObject) hetDimerMapForSO_mapDB_sorted.get(oligoobjid);

        Graph<String> dagOligo = new Graph<String>();
        //Vertex<String> rootvertex = new Vertex<String>("root:"+obj.getInternalPrimerId());
        Vertex<String> rootvertex = new Vertex<String>(obj.getInternalPrimerId());
        //rootvertex.setData(obj);
        rootvertex.setMarkState(0);
        dagOligo.addVertex(rootvertex);
        dagOligo.setRootVertex(rootvertex);

        int nextspacing = (int) Math.ceil(spacing * 0.5);

        //if(so.getStop()-Integer.parseInt(obj.getInternalStart())>=2000){
        if(so.getStop()-Integer.parseInt(obj.getInternalStart())>=(nextspacing*1000)){
            //traverse(rootvertex, filteredhetDimerMapForSO, dagOligo, so);
            traverse_mapDB(rootvertex, filteredHetDimerMapForSO_multimap, dagOligo, so, hetDimerMapForSO_mapDB_sorted, nextspacing);
        }

        startingcounter+=1;
        //LinkedHashMap<String, ArrayList<OligoObject>> arrayOfPathsForRoot = new LinkedHashMap<String, ArrayList<OligoObject>>();
        Multimap<java.lang.String, java.lang.String> arrayOfPathsForRoot_multimap = LinkedHashMultimap.create();
        final ArrayList<String> pathArrays = new ArrayList<String>();
        //int counter=1;
        //dagOligo.setMapOfOligoPathArrays(arrayOfPathsForRoot);
        dagOligo.setMapOfOligoidsPathMultimapArrays(arrayOfPathsForRoot_multimap);
        dagOligo.dfsSpanningTree(rootvertex, pathArrays, startingcounter, dagOligo, new DFSVisitor<String>() {

            @Override
            public void visit(Graph<String> g, Vertex<String> v) {
                //System.out.println("at dag: "+ v.getName() + " num outgoing vertices: "+ v.getOutgoingEdgeCount() + " and outgoing verticeshash = ");
            }

            @Override
            public void visit(Graph<String> g, Vertex<String> v, Edge<String> e) {

            }
        });

        Set<String> keyset = dagOligo.getMapOfOligoidsPathMultimapArrays().keySet();
        Iterator<String> keyit = keyset.iterator();
        while (keyit.hasNext()){
            String key1part = keyit.next();
            String key2part = obj.getInternalPrimerId();
            String key = key1part+key2part;

            //System.out.println(key);
            Collection<String> pathCollection = dagOligo.getMapOfOligoidsPathMultimapArrays().get(key1part);
            ArrayList<String> pathArray = new ArrayList<String>(pathCollection);
            //sort by region and subsection. because we have het dimer interactions only for sorted Oligos.
            pathArray = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(pathArray);
            int toremoveFlag=0;
            Float deltagForThisArray = Float.parseFloat("0.00");

            for(int p=0; p<(pathArray.size()-1); p++){
                for(int q=p+1; q<pathArray.size(); q++){
                    String key_part1 = pathArray.get(p);
                    String key_part2 = pathArray.get(q);

                    if(!(allHetDimerPairsObjectsMapMapdb.containsKey(key_part1+"&"+key_part2))){
                        toremoveFlag=1;
                    }
                }
            }
            if(toremoveFlag==0){
                for(String pathid : pathArray){
                    setOfOligosMultimapFromSeed.put(key, pathid);
                }
            }
        }


        return setOfOligosMultimapFromSeed;
    }




    /**
     *
     * @param vertex
     * @param filteredhetDimerIdsMapForSO
     * @param dagOligo
     * @param so
     * @param hetdimerMapForSO_sorted
     * @param spacing
     */
    private void traverse_mapDB(Vertex<String> vertex, Multimap<String, String> filteredhetDimerIdsMapForSO, Graph<String> dagOligo, SequenceObject so, HTreeMap<String, Object> hetdimerMapForSO_sorted, int spacing) {
        String parentObjId = vertex.getName();

        if(filteredhetDimerIdsMapForSO.get(parentObjId)!=null && filteredhetDimerIdsMapForSO.get(parentObjId).size()>0){

            Collection<String> childrenObjCollection = filteredhetDimerIdsMapForSO.get(parentObjId);

            List<String> childrenObj = new ArrayList<String>(childrenObjCollection);
            Collections.shuffle(childrenObj);

            //only return 5-10 or so children at a time.Subject to change.
            /*if(childrenObj.size()>=3){
                childrenObj = childrenObj.subList(0, 2);
            }
            */

            if(childrenObj.size()>=3){
                childrenObj = childrenObj.subList(0, 2);
            }

            for(String childObjid : childrenObj){
                OligoObject childOligoObj = (OligoObject) hetdimerMapForSO_sorted.get(childObjid);

                Vertex<String> childVertex = new Vertex<String>(childObjid, childObjid);
                //System.out.println("iterating through all children for "+vertex.getName()+" Now at childVertex:" + childObjid+ " Same as:"+ childVertex.getName()+" POS: "+ childOligoObj.getInternalStart());

                //uncomenting today 18thnov 2016
                dagOligo.addVertex(vertex);
                childVertex.setData(childObjid);
                dagOligo.addVertex(childVertex);

                boolean result = dagOligo.addEdge(vertex, childVertex, 1);

                //List<Edge<String>> edgesList = dagOligo.getEdges();
                //System.out.println("edges list:"+ edgesList.size());

                //if(so.getStop()-Integer.parseInt(childOligoObj.getInternalStart())>=2000){
                if(so.getStop()-Integer.parseInt(childOligoObj.getInternalStart())>=(spacing*1000)){
                    //System.out.println("Traversing from childVertex:" + childObjid + " " + childOligoObj.getInternalStart()+" to its children");
                    if(filteredhetDimerIdsMapForSO.size()>0){
                        traverse_mapDB(childVertex, filteredhetDimerIdsMapForSO, dagOligo, so, hetdimerMapForSO_sorted, spacing);
                    }
                }

                /*//if(so.getStop()-Integer.parseInt(childOligoObj.getInternalStart())<2000){
                if(so.getStop()-Integer.parseInt(childOligoObj.getInternalStart())<(spacing*1000)){
                    //System.out.println("End of this path at:" + childOligoObj.getInternalPrimerId() + " at " + childOligoObj.getInternalStart() + " with so stop at:" + so.getStop() + " starting at root vertex:"+ dagOligo.getRootVertex().getName());
                }*/
            }
        }
    }






}




