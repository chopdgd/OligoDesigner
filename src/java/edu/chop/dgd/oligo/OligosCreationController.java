package edu.chop.dgd.oligo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import edu.chop.dgd.Process.OligoSerializer;
import edu.chop.dgd.dgdObjects.*;
import edu.chop.dgd.dgdUtils.OligoUtils;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.template.SequenceView;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerJava;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jayaramanp on 6/1/14.
 */
public class OligosCreationController implements Controller{

    private static String blatInpDir;
    private static String blatOpDir;
    private static String oligoProcessScriptDir;
    private static String dataDir;
    private static String downloadsDir;
    private static String oligoInputDir;
    private static String oligoOutputDir;
    private static String mfoldInpDir;
    private static String mfoldOpDir;
    private static String homodimerOpDir;
    private static String heterodimerInpDir;
    private static String heterodimerOpDir;
    private static String finalOligos;
    private static String min_gc;
    private static String max_gc;
    private static String opt_gc;
    private static String min_tm;
    private static String opt_tm;
    private static String max_tm;
    private static String min_len;
    private static String opt_len;
    private static String max_len;
    private static String na_ion;
    private static String mg_ion;
    private static String self_any;
    private static String self_end;
    private static int spacing=0;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        ArrayList error = new ArrayList();
        ArrayList warning = new ArrayList();
        ArrayList status = new ArrayList();
        String exampleFile = "/data/antholigo_test/antholigo_test.txt";
        String upFile = request.getParameter("uploadFolderPath");
        String projectId = request.getParameter("proj_id");
        String assembly = request.getParameter("assembly");
        String origFileName = request.getParameter("origFile");
        String spacing2 = request.getParameter("separation");
        spacing = Integer.parseInt(spacing2);

        String minGC = request.getParameter("minGC");
        if(minGC.length()>0){
            min_gc = minGC;
        }else{
            min_gc = "NA";
        }

        String maxGC = request.getParameter("maxGC");
        if(maxGC.length()>0){
            max_gc = maxGC;
        }else{
            max_gc = "NA";
        }

        String optGC = request.getParameter("optGC");
        if(optGC.length()>0){
            opt_gc = optGC;
        }else{
            opt_gc = "NA";
        }

        String minTm = request.getParameter("minTm");
        if(minTm.length()>0){
            min_tm = minTm;
        }else{
            min_tm = "NA";
        }

        String maxTm = request.getParameter("maxTm");
        if(maxTm.length()>0){
            max_tm = maxTm;
        }else{
            max_tm = "NA";
        }

        String optTm = request.getParameter("optTm");
        if(optTm.length()>0){
            opt_tm = optTm;
        }else{
            opt_tm = "NA";
        }

        String minLen = request.getParameter("minLen");
        if(minLen.length()>0){
            min_len = minLen;
        }else{
            min_len = "NA";
        }

        String maxLen = request.getParameter("maxLen");
        if(maxLen.length()>0){
            max_len = maxLen;
        }else{
            max_len = "NA";
        }

        String optLen = request.getParameter("optLen");
        if(optLen.length()>0){
            opt_len = optLen;
        }else{
            opt_len = "NA";
        }

        String naIon = request.getParameter("Na");
        if(naIon.length()>0){
            na_ion = naIon;
        }else{
            na_ion = "NA";
        }

        String mgIon = request.getParameter("Mg");
        if(mgIon.length()>0){
            mg_ion = mgIon;
        }else{
            mg_ion = "NA";
        }

        String selfAny = request.getParameter("selfAny");
        if(selfAny.length()>0){
            self_any = selfAny;
        }else{
            self_any = "NA";
        }

        String selfEnd = request.getParameter("selfEnd");
        if(selfEnd.length()>0){
            self_end = selfEnd;
        }else{
            self_end = "NA";
        }

        File fileToParse = new File(upFile+projectId+"/"+origFileName);
        ArrayList<SequenceObject> objects =  getObjectsFromFile(fileToParse, error, assembly);
        SequenceObjectSubsections soss = new SequenceObjectSubsections();
        String reportFile="";String heterodimerReport="";

        List<OligoObject> heteroDimerObjectsList = new ArrayList<OligoObject>();
        MfoldDimer mfd = new MfoldDimer();

        //create new MapDB database to store all hetdimer objects:
        Serializer<OligoObject> serializer = new OligoSerializer();
        DB hetdimerdb = DBMaker.fileDB("/data/"+heterodimerInpDir+"/"+assembly+"_"+projectId+".db")
                //.closeOnJvmShutdown()
                .fileDeleteAfterClose().make();
        //stores all hetdimer object info. so that only storing the ids for the other maps, are enough further down.
        HTreeMap<String, Object> hetDimerHashMapMAPDB = hetdimerdb.hashMap("hetDimerHashMap").keySerializer(Serializer.STRING).valueSerializer(new SerializerJava()).createOrOpen();

        for(SequenceObject so : objects){
            //testing new method
            String inpFilename = assembly+"_"+projectId+"_"+so.getChr()+":"+so.getStart()+"-"+so.getStop()+".txt";
            List<SequenceObjectSubsections> sosSubsList = so.generateSequenceSubsections(inpFilename, dataDir);
            String fileName="oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());

            reportFile = generateReportSetSubsectionIds(sosSubsList, projectId, fileName, so);

            String detailFile = dataDir+finalOligos+fileName+"_detail.html";
            String secondaryFile = dataDir+finalOligos+fileName+"_secondary.txt";
            List<SequenceObjectSubsections> oligosSubsectionList = soss.retrieveResultsFromAnalyses(fileName, sosSubsList, dataDir, oligoOutputDir, blatInpDir, blatOpDir, mfoldInpDir, mfoldOpDir, homodimerOpDir, heterodimerInpDir, heterodimerOpDir);
            List<OligoObject> heteroDimerObjectsListFromSO = mfd.filterOligosCreateHeterodimers(oligosSubsectionList);
            so.setHetDimerOligosList(heteroDimerObjectsListFromSO);
            heteroDimerObjectsList.addAll(heteroDimerObjectsListFromSO);

            for(OligoObject hetdimerObj : heteroDimerObjectsListFromSO){
                //Using MAPDB: Creates a hashmap of all hetdimer objects instead of list.
                hetDimerHashMapMAPDB.put(hetdimerObj.getInternalPrimerId(), hetdimerObj);
            }

            System.out.println("adding for Heterodimer analysis");
            so.setOligoObjectSubsections(oligosSubsectionList);
            so.setDetailsFile(detailFile);
            so.setSecondaryFile(secondaryFile);
            so.setReportFile(reportFile);
            so.setFilename(fileName);

        }

        System.out.println("Running Heterodimer analysis now");
        heteroDimerObjectsList = new OligoUtils().sortOligosBySubsectionAndSerialNum(heteroDimerObjectsList);
        //NEED to parallellize this section!

        String hetdimerFilename = "oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());

        //using MapDB, it will only add the two ids as a string and a float value as the value.
        DB db2 = DBMaker.tempFileDB().fileDeleteAfterClose().make();
        //HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb = db2.hashMap("allHetDimerPairsObjectsMapMapdb").keySerializer(Serializer.STRING).valueSerializer(Serializer.FLOAT).createOrOpen();
        HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb = db2.hashMap("allHetDimerPairsObjectsMapMapdb").keySerializer(Serializer.STRING).valueSerializer(Serializer.FLOAT).createOrOpen();

        //Oct9th 2017 need to change this to: https://github.com/harishreedharan/MapDB/blob/master/src/test/java/examples/MultiMap.java
        LinkedHashMap<OligoObject, List<OligoObject>> oligoObjectsMap = mfd.mapOligosCreateHetDimerInpSections_new(heteroDimerObjectsList);
        //Multimap<String, String> oligoObjectsMap = mfd.mapOligosCreateHetDimerInpSections_newMapDB(heteroDimerObjectsList);
        ArrayList<String[]> inputlistforHetDimerAnalysis = mfd.createSubsetofhetDimersRunHeterodimerAnalysis(oligoObjectsMap);

        int numfiles = 1; int numlines = 10000; //int numlinescopy = numlines;
        double temp = Math.ceil((inputlistforHetDimerAnalysis.size())/(double)(numlines));
        int temp1= (int) temp;

        if(temp1 != 0){
            numfiles = temp1;
        }else{
            numfiles = temp1 + 1;
        }

        //getting first oligoId to begin with.
        int oligoIdStoppedAt = 0;

        for(int n=1; n<=numfiles; n++){

            System.out.println("starting hetdimer run in subset of file");
            String hetDimerSubsectionIndexes = mfd.createFileRunHeterodimerAnalysis(inputlistforHetDimerAnalysis, heterodimerInpDir, dataDir, hetdimerFilename, n, oligoIdStoppedAt, numlines);
            oligoIdStoppedAt = Integer.parseInt(hetDimerSubsectionIndexes.split("&", -1)[0]);
            System.out.println("getting deltaG values for HetDimer Pairs");
            allHetDimerPairsObjectsMapMapdb = mfd.getDeltaGValuesForHetDimerPairs_createMapDBHash(allHetDimerPairsObjectsMapMapdb, dataDir, heterodimerOpDir, hetdimerFilename, n);

        }

        for(SequenceObject so : objects){

            DB db = DBMaker.memoryDB().fileDeleteAfterClose().make();
            ArrayList<String> hetDimerIdListForSO = mfd.getHetDimersIdsForRegion(allHetDimerPairsObjectsMapMapdb, so, hetDimerHashMapMAPDB);
            hetDimerIdListForSO = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(hetDimerIdListForSO);

            HTreeMap<String, Object> hetDimerMapForSO_mapDB_sorted = db.hashMap("hetDimerMapOnlySO_sorted").keySerializer(Serializer.STRING).valueSerializer(new SerializerJava()).createOrOpen();
            for(String id : hetDimerIdListForSO){
                hetDimerMapForSO_mapDB_sorted.put(id, hetDimerHashMapMAPDB.get(id));
            }

            //Create NavigableSet. set tuple serializer
            Multimap<String, String> filteredHetDimerMapForSO_multimap = ArrayListMultimap.create();
            filteredHetDimerMapForSO_multimap = mfd.filterMapCreateOnlyHetsWithinDistanceMap_MapDB(hetDimerIdListForSO, spacing, hetDimerMapForSO_mapDB_sorted, filteredHetDimerMapForSO_multimap);

            //now create tree.
            Set<String> filteredHetDimerMapForSO_multimap_keys = filteredHetDimerMapForSO_multimap.keySet();
            ArrayList<String> filteredHetDimerMapForSO_multimap_keys_sorted = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(new ArrayList<String>(filteredHetDimerMapForSO_multimap_keys));

            int startingcounter = 1;

            //Creating a multi-map instead
            Multimap<String, String> setsOfOligoSets_mapDB = LinkedListMultimap.create();

            //sorting the keys.
            for(String oligoobjid : filteredHetDimerMapForSO_multimap_keys_sorted){
                OligoObject obj = (OligoObject) hetDimerMapForSO_mapDB_sorted.get(oligoobjid);
                if((Integer.parseInt(obj.getInternalStart())-so.getStart()<=2500) && (Integer.parseInt(obj.getInternalStart())-so.getStart()>=1)){

                    //Graph<OligoObject> dagOligo = new Graph<OligoObject>();
                    Graph<String> dagOligo = new Graph<String>();
                    //Vertex<String> rootvertex = new Vertex<String>("root:"+obj.getInternalPrimerId());
                    Vertex<String> rootvertex = new Vertex<String>(obj.getInternalPrimerId());
                    //rootvertex.setData(obj);
                    rootvertex.setMarkState(0);

                    dagOligo.addVertex(rootvertex);
                    dagOligo.setRootVertex(rootvertex);

                    if(so.getStop()-Integer.parseInt(obj.getInternalStart())>=3000){
                        //traverse(rootvertex, filteredhetDimerMapForSO, dagOligo, so);
                        traverse_mapDB(rootvertex, filteredHetDimerMapForSO_multimap, dagOligo, so, hetDimerMapForSO_mapDB_sorted);
                    }

                    startingcounter+=1;
                    //LinkedHashMap<String, ArrayList<OligoObject>> arrayOfPathsForRoot = new LinkedHashMap<String, ArrayList<OligoObject>>();
                    Multimap<java.lang.String, java.lang.String> arrayOfPathsForRoot_multimap = LinkedHashMultimap.create();
                    final ArrayList<String> pathArrays = new ArrayList<String>();
                    int counter=1;
                    //dagOligo.setMapOfOligoPathArrays(arrayOfPathsForRoot);
                    dagOligo.setMapOfOligoidsPathMultimapArrays(arrayOfPathsForRoot_multimap);
                    dagOligo.dfsSpanningTree(rootvertex, pathArrays, counter, dagOligo, new DFSVisitor<String>() {

                        @Override
                        public void visit(Graph<String> g, Vertex<String> v) {
                            System.out.println("at dag: "+ v.getName() + " num outgoing vertices: "+ v.getOutgoingEdgeCount() + " and outgoing verticeshash = ");
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
                                setsOfOligoSets_mapDB.put(key, pathid);
                            }
                        }
                    }
                }
            }

            so.setOligoSetsFullMapMultiMap(setsOfOligoSets_mapDB);
            db.close();
            System.out.println("done with this so. sets of oligo sets size:"+ setsOfOligoSets_mapDB.size());

        }

        System.out.println("checking Oligos interaction across SO");

        ///will come to this in the evening!!!!! 12th Oct 2017.
        objects = new SequenceObject().checkOligosInteractAcrossSO_mapDB(objects, allHetDimerPairsObjectsMapMapdb);

        System.out.println("sorting oligo sets by minDeltaG");


        //objects = new SequenceObject().sortSequenceObjectSetsByMinDeltaG(objects);

        System.out.println("writing oligos to file");
        String oligosFilename = writeOligosFinalFile_MapDB(objects, dataDir, finalOligos, projectId, hetDimerHashMapMAPDB);


        //clear everything!!!
        for(SequenceObject so : objects){
            so.getHetDimerOligosList().clear();
        }

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/processOligos.jsp");
        mvObj.addObject("uploadedPath", upFile);
        mvObj.addObject("sequenceObjects", objects);
        mvObj.addObject("optimalOligosFile", oligosFilename);
        mvObj.addObject("projectId", projectId);
        hetdimerdb.close();
        db2.close();
        return mvObj;

    }

    private String writeOligosFinalFile_MapDB(ArrayList<SequenceObject> objects, String dataDir, String finalOligos, String projectId, HTreeMap<String, Object> hetDimerHashMapMAPDB) throws Exception{

        String firstOptimalOligosFile = dataDir+finalOligos+projectId+"_primary.txt";
        File firstOligosFile = new File(firstOptimalOligosFile);
        PrintWriter pwFirst = new PrintWriter(firstOligosFile);
        pwFirst.println("Primer Set\tPrimer Id\tPrimerChr\tPrimer Start\tPrimer End\tSequence\tSequence Rev. Complement\tGC\tTm\tSize\tSelf Dimer\tHairpin Tm\tHairpin dG\tBlat");

        for(SequenceObject so : objects){

            LinkedHashMap<String, ArrayList<String>> optimalOligosLinkedHashmap = so.getPrimaryOptimalSetOfOligosForSet();

            //write optimal oligos file
            pwFirst.println("For query region: "+so.getChr()+":"+so.getStart()+"-"+so.getStop());
            System.out.println("For query region: " + so.getChr() + ":" + so.getStart() + "-" + so.getStop());

            if(so.getPrimaryOptimalSetOfOligosForSet()!=null && so.getPrimaryOptimalSetOfOligosForSet().size()>0){
                System.out.println("primary oligos set size:"+so.getPrimaryOptimalSetOfOligosForSet());
                LinkedHashMap<String, ArrayList<String>> primaryOligosSet = so.getPrimaryOptimalSetOfOligosForSet();

                System.out.println("primary oligo keyset size is:"+primaryOligosSet.keySet().size());
                String primarySet = primaryOligosSet.keySet().iterator().next();

                for(String o : primaryOligosSet.get(primarySet)){
                    OligoObject oligoobj = (OligoObject) hetDimerHashMapMAPDB.get(o);
                    DNASequence seq = new DNASequence(oligoobj.getInternalSeq());
                    SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                    String revCompSeq = revcomp.getSequenceAsString();
                    pwFirst.println(primarySet.split("inpSeq")[0]+"\t"+ oligoobj.getInternalPrimerId() + "\t" + so.getChr() + "\t" + oligoobj.getInternalStart()
                            + "\t" + (Integer.parseInt(oligoobj.getInternalStart())+oligoobj.getInternalLen())
                            + "\t" + oligoobj.getInternalSeq() + "\t"+ revCompSeq +"\t"
                            + oligoobj.getInternalGc() + "\t" + oligoobj.getInternalTm() + "\t" + oligoobj.getInternalLen()
                            + "\t" + oligoobj.getHomodimerValue() + "\t-\t" + oligoobj.getHairpinValue() + "\t" + oligoobj.getInternalPrimerBlatList().size());
                }

            }else{
                pwFirst.println("NO oligos found");
            }
        }

        pwFirst.close();

        return firstOptimalOligosFile;

    }



    private void traverse_mapDB(Vertex<String> vertex, Multimap<String, String> filteredhetDimerIdsMapForSO, Graph<String> dagOligo, SequenceObject so, HTreeMap<String, Object> hetdimerMapForSO_sorted) {
        String parentObjId = vertex.getName();

        if(filteredhetDimerIdsMapForSO.get(parentObjId)!=null && filteredhetDimerIdsMapForSO.get(parentObjId).size()>0){

            Collection<String> childrenObjCollection = filteredhetDimerIdsMapForSO.get(parentObjId);
            List<String> childrenObj = new ArrayList<String>(childrenObjCollection);

            //only return 5-10 or so children at a time.Subject to change.
            if(childrenObj.size()>=8){
                childrenObj = childrenObj.subList(0, 7);
            }

            for(String childObjid : childrenObj){
                OligoObject childOligoObj = (OligoObject) hetdimerMapForSO_sorted.get(childObjid);

                Vertex<String> childVertex = new Vertex<String>(childObjid, childObjid);
                System.out.println("iterating through all children for "+vertex.getName()+" Now at childVertex:" + childObjid+ " Same as:"+ childVertex.getName()+" POS: "+ childOligoObj.getInternalStart());

                //uncomenting today 18thnov 2016
                dagOligo.addVertex(vertex);
                childVertex.setData(childObjid);
                dagOligo.addVertex(childVertex);

                boolean result = dagOligo.addEdge(vertex, childVertex, 1);

                List<Edge<String>> edgesList = dagOligo.getEdges();
                System.out.println("edges list:"+ edgesList.size());

                if(so.getStop()-Integer.parseInt(childOligoObj.getInternalStart())>=3000){
                    System.out.println("Traversing from childVertex:" + childObjid + " " + childOligoObj.getInternalStart()+" to its children");
                    if(filteredhetDimerIdsMapForSO.size()>0){
                        //traverse(childVertex, filteredhetDimerMapForSO, dagOligo, so);
                        traverse_mapDB(childVertex, filteredhetDimerIdsMapForSO, dagOligo, so, hetdimerMapForSO_sorted);
                    }
                }

                /*if(so.getStop()-Integer.parseInt(childObj.getInternalStart())<2000){
                    //System.out.println("End of this path at:" + childObj.getInternalPrimerId() + " at " + childObj.getInternalStart() + " with so stop at:" + so.getStop() + " starting at root vertex:"+ dagOligo.getRootVertex().getName());
                }*/
            }
        }
    }


    /**
     *
     * @param ampliconObjList
     * @param projectId
     * @param filename
     * @param so
     * @return
     * @throws Exception
     */

    private String generateReportSetSubsectionIds(List<SequenceObjectSubsections> ampliconObjList, String projectId, String filename, SequenceObject so) throws Exception{

        Date dt = new Date();

        //create a placeholder for filename. it should be sent in from parameter. this is for backward compatibility. although i see no reason for filename to be set here.
        String fileN="oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
        if(filename.length()>0){
            fileN = filename;
        }

        File oligoInputFile = new File(dataDir+oligoInputDir+fileN);
        PrintWriter pw = new PrintWriter(oligoInputFile);
        int counter = 0;

        for(SequenceObjectSubsections oss : ampliconObjList){
            counter+=1;
            String subsectionId = "inpSeq"+so.getChr()+":"+so.getStart()+":"+so.getStop()+"_"+counter;
            oss.setSubsectionid(subsectionId);
            pw.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n"+
                    "PRIMER_INTERNAL_MAX_TM="+max_tm+"\nPRIMER_INTERNAL_OPT_TM="+opt_tm+"\nPRIMER_INTERNAL_MIN_TM="+min_tm+"\n"+
                    "PRIMER_INTERNAL_MAX_GC="+max_gc+"\nPRIMER_INTERNAL_OPT_GC_PERCENT="+opt_gc+"\nPRIMER_INTERNAL_MIN_GC="+min_gc+"\n"+
                    "PRIMER_INTERNAL_MAX_SIZE="+max_len+"\nPRIMER_INTERNAL_OPT_SIZE="+opt_len+"\nPRIMER_INTERNAL_MIN_SIZE="+min_len+"\n"+
                    "PRIMER_INTERNAL_SALT_MONOVALENT="+na_ion+"\nPRIMER_INTERNAL_SALT_DIVALENT="+mg_ion+"\nPRIMER_INTERNAL_MAX_SELF_ANY="+self_any+"\n"+
                    "PRIMER_INTERNAL_MAX_SELF_END="+self_end+"\n=");
            System.out.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n"+
                    "PRIMER_INTERNAL_MAX_TM="+max_tm+"\nPRIMER_INTERNAL_OPT_TM="+opt_tm+"\nPRIMER_INTERNAL_MIN_TM="+min_tm+"\n"+
                    "PRIMER_INTERNAL_MAX_GC="+max_gc+"\nPRIMER_INTERNAL_OPT_GC=PERCENT"+opt_gc+"\nPRIMER_INTERNAL_MIN_GC="+min_gc+"\n"+
                 "PRIMER_INTERNAL_MAX_SIZE="+max_len+"\nPRIMER_INTERNAL_OPT_SIZE="+opt_len+"\nPRIMER_INTERNAL_MIN_SIZE="+min_len+"\n"+
                "PRIMER_INTERNAL_SALT_MONOVALENT="+na_ion+"\nPRIMER_INTERNAL_SALT_DIVALENT="+mg_ion+"\nPRIMER_INTERNAL_MAX_SELF_ANY="+self_any+"\n"+
                "PRIMER_INTERNAL_MAX_SELF_END="+self_end+"\n=");
        }

        pw.flush();
        pw.close();

        String resultPrimer3Blat = runOligoProcessBuilder(fileN);
        //System.out.println(resultPrimer3Blat);

        return resultPrimer3Blat;

    }


    private ArrayList<SequenceObject> getObjectsFromFile(File fileToParse, ArrayList error, String assembly) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToParse)));
        ArrayList<SequenceObject> oligoList = new ArrayList<SequenceObject>();

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                String lineArr[] = line.split("\t", -1);
                //System.out.println(line);
                SequenceObject obj = new SequenceObject();
                obj.setAssembly(assembly);
                obj.setChr(lineArr[0]);
                obj.setStart(Integer.parseInt(lineArr[1]));
                obj.setStop(Integer.parseInt(lineArr[2]));
                oligoList.add(obj);
            }

        }catch (Exception e){
            error.add("Something went wrong wile parsing file");
            error.add(e.getStackTrace());
        }
        finally {
            reader.close();
        }

        return oligoList;
    }



    public String runOligoProcessBuilder(String inputFileName) throws Exception {

        String answer;
        String errAnswer="NA";

        ProcessBuilder pb = new ProcessBuilder(getOligoProcessScriptDir()+"OligoProcess.sh",inputFileName);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
        path += ":/usr/local/mfold/bin";
        path += ":/usr/local/mfold/share";
        env.put("PATH", path);

        pb.directory(new File(oligoProcessScriptDir));

        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the oligoProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the oligoProcess.sh..");

            BufferedReader bErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errsb = new StringBuilder();
            String errline;
            while ((errline = bErr.readLine()) != null) {
                errsb.append(errline).append("\n");
            }
            String erranswer = errsb.toString();
            //System.out.println(erranswer);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {

                if(line.equals("Sorry, the BLAT/iPCR server seems to be down.  Please try again later") ||
                        line.equals("Error in TCP non-blocking connect() 61 - Connection refused") ||
                        line.equals("Couldn't connect to localhost 17779"))
                {
                    errAnswer+=line;
                }else{
                    sb.append(line).append("\n");
                }

            }
            answer = sb.toString();

            System.out.println("should have got an output from Primer3 and BLAT..");

            if(errAnswer.length()>2){

                throw new Exception("Exception: Blat server issues. The BLAT server may have " +
                        "not been started. Please start server using gfServer.");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;
    }


    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        OligosCreationController.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        OligosCreationController.blatOpDir = blatOpDir;
    }

    public static String getOligoProcessScriptDir() {
        return oligoProcessScriptDir;
    }

    public void setOligoProcessScriptDir(String oligoProcessScriptDir) {
        OligosCreationController.oligoProcessScriptDir = oligoProcessScriptDir;
    }

    public static String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        OligosCreationController.dataDir = dataDir;
    }

    public static String getOligoInputDir() {
        return oligoInputDir;
    }

    public void setOligoInputDir(String oligoInputDir) {
        OligosCreationController.oligoInputDir = oligoInputDir;
    }

    public static String getOligoOutputDir() {
        return oligoOutputDir;
    }

    public void setOligoOutputDir(String oligoOutputDir) {
        OligosCreationController.oligoOutputDir = oligoOutputDir;
    }

    public static String getMfoldInpDir() {
        return mfoldInpDir;
    }

    public void setMfoldInpDir(String mfoldInpDir) {
        OligosCreationController.mfoldInpDir = mfoldInpDir;
    }

    public static String getMfoldOpDir() {
        return mfoldOpDir;
    }

    public void setMfoldOpDir(String mfoldOpDir) {
        OligosCreationController.mfoldOpDir = mfoldOpDir;
    }

    public static String getHomodimerOpDir() {
        return homodimerOpDir;
    }

    public void setHomodimerOpDir(String homodimerOpDir) {
        OligosCreationController.homodimerOpDir = homodimerOpDir;
    }

    public static String getHeterodimerInpDir() {
        return heterodimerInpDir;
    }

    public void setHeterodimerInpDir(String heterodimerInpDir) {
        OligosCreationController.heterodimerInpDir = heterodimerInpDir;
    }

    public static String getHeterodimerOpDir() {
        return heterodimerOpDir;
    }

    public void setHeterodimerOpDir(String heterodimerOpDir) {
        OligosCreationController.heterodimerOpDir = heterodimerOpDir;
    }

    public static String getFinalOligos() {
        return finalOligos;
    }

    public void setFinalOligos(String finalOligos) {
        OligosCreationController.finalOligos = finalOligos;
    }

    public static String getDownloadsDir() {
        return downloadsDir;
    }

    public void setDownloadsDir(String downloadsDir) {
        OligosCreationController.downloadsDir = downloadsDir;
    }

    public static String getMin_gc() {
        return min_gc;
    }

    public static void setMin_gc(String min_gc) {
        OligosCreationController.min_gc = min_gc;
    }

    public static String getMax_gc() {
        return max_gc;
    }

    public static void setMax_gc(String max_gc) {
        OligosCreationController.max_gc = max_gc;
    }

    public static String getOpt_gc() {
        return opt_gc;
    }

    public static void setOpt_gc(String opt_gc) {
        OligosCreationController.opt_gc = opt_gc;
    }

    public static String getMin_tm() {
        return min_tm;
    }

    public static void setMin_tm(String min_tm) {
        OligosCreationController.min_tm = min_tm;
    }

    public static String getOpt_tm() {
        return opt_tm;
    }

    public static void setOpt_tm(String opt_tm) {
        OligosCreationController.opt_tm = opt_tm;
    }

    public static String getMax_tm() {
        return max_tm;
    }

    public static void setMax_tm(String max_tm) {
        OligosCreationController.max_tm = max_tm;
    }

    public static String getMin_len() {
        return min_len;
    }

    public static void setMin_len(String min_len) {
        OligosCreationController.min_len = min_len;
    }

    public static String getOpt_len() {
        return opt_len;
    }

    public static void setOpt_len(String opt_len) {
        OligosCreationController.opt_len = opt_len;
    }

    public static String getMax_len() {
        return max_len;
    }

    public static void setMax_len(String max_len) {
        OligosCreationController.max_len = max_len;
    }

    public static String getNa_ion() {
        return na_ion;
    }

    public static void setNa_ion(String na_ion) {
        OligosCreationController.na_ion = na_ion;
    }

    public static String getMg_ion() {
        return mg_ion;
    }

    public static void setMg_ion(String mg_ion) {
        OligosCreationController.mg_ion = mg_ion;
    }

    public static String getSelf_any() {
        return self_any;
    }

    public static void setSelf_any(String self_any) {
        OligosCreationController.self_any = self_any;
    }

    public static String getSelf_end() {
        return self_end;
    }

    public static void setSelf_end(String self_end) {
        OligosCreationController.self_end = self_end;
    }

    public static int getSpacing() {
        return spacing;
    }

    public static void setSpacing(int spacing) {
        OligosCreationController.spacing = spacing;
    }
}