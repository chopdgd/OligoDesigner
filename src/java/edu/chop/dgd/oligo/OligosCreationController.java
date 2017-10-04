package edu.chop.dgd.oligo;

import edu.chop.dgd.dgdObjects.*;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.template.SequenceView;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;
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


        //File uploadedFiles[] = newFile.listFiles();
        //File fileToParse = uploadedFiles[0];
        File fileToParse = new File(upFile+projectId+"/"+origFileName);
        ArrayList<SequenceObject> objects =  getObjectsFromFile(fileToParse, error, assembly);
        SequenceObjectSubsections soss = new SequenceObjectSubsections();
        String reportFile="";String heterodimerReport="";

        List<OligoObject> heteroDimerObjectsList = new ArrayList<OligoObject>();
        MfoldDimer mfd = new MfoldDimer();

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

            System.out.println("adding for Heterodimer analysis");

            so.setOligoObjectSubsections(oligosSubsectionList);
            so.setDetailsFile(detailFile);
            so.setSecondaryFile(secondaryFile);
            so.setReportFile(reportFile);
            so.setFilename(fileName);

        }

        System.out.println("Running Heterodimer analysis now");
        String hetdimerFilename = "oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());

        //going to try adding in Mapdb
        LinkedHashMap<String, List<OligoObject>> allHetDimerPairsObjectsMap = new LinkedHashMap<String, List<OligoObject>>();
        //import org.mapdb.*
        //DB db = DBMaker.tempFileDB().make();
        DB db2 = DBMaker.tempFileDB().fileDeleteAfterClose().make();
        //DB db = DBMaker.newTempFileDB().make();
        //DB db = DBMaker.fileDB(dataDir+heterodimerOpDir+hetdimerFilename+".db").fileChannelEnable().fileDeleteAfterClose().closeOnJvmShutdown().make();
        //ConcurrentMap map = db.hashMap("map").make();
        HTreeMap<String, List<OligoObject>> allHetDimerPairsObjectsMapMapdb = db2.hashMap("allHetDimerPairsObjectsMapMapdb").keySerializer(new SerializerCompressionWrapper(Serializer.STRING)).createOrOpen();
        //HTreeMap<String, List<OligoObject>> allHetDimerPairsObjectsMapMapdb = (HTreeMap<String, List<OligoObject>>) db.hashMap("allHetDimerPairsObjectsMapMapdb").createOrOpen();
        //HTreeMap<String, List<OligoObject>> allHetDimerPairsObjectsMapMapdb = db.createHashMap("allHetDimerPairsObjectsMapMapdb").make();
        //modified to new method.
        LinkedHashMap<OligoObject, List<OligoObject>> oligoObjectsMap = mfd.mapOligosCreateHetDimerInpSections_new(heteroDimerObjectsList);

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
            //String hetDimerSubsectionIndexes = mfd.createSubsetRunHeterodimerAnalysis(oligoObjectsMap, heterodimerInpDir, dataDir, hetdimerFilename, n, numlines, oligoIdStoppedAt, oligoidValueIndexStoppedAt);
            String hetDimerSubsectionIndexes = mfd.createFileRunHeterodimerAnalysis(inputlistforHetDimerAnalysis, heterodimerInpDir, dataDir, hetdimerFilename, n, oligoIdStoppedAt, numlines);
            oligoIdStoppedAt = Integer.parseInt(hetDimerSubsectionIndexes.split("&", -1)[0]);

            System.out.println("getting deltaG values for HetDimer Pairs");


            //commenting temporarily.
            oligoObjectsMap = mfd.getDeltaGValuesForHetDimerPairs_new(oligoObjectsMap, dataDir, heterodimerOpDir, hetdimerFilename, n);

            for(OligoObject objectKey : oligoObjectsMap.keySet()){

                if(allHetDimerPairsObjectsMap.containsKey(objectKey.getInternalPrimerId())){
                    List<OligoObject> oligosFromPrevRuns = allHetDimerPairsObjectsMap.get(objectKey.getInternalPrimerId());

                    oligosFromPrevRuns.addAll(oligoObjectsMap.get(objectKey));
                    allHetDimerPairsObjectsMap.put(objectKey.getInternalPrimerId(), oligosFromPrevRuns);
                    allHetDimerPairsObjectsMapMapdb.put(objectKey.getInternalPrimerId(), oligoObjectsMap.get(objectKey));

                }else{
                    allHetDimerPairsObjectsMap.put(objectKey.getInternalPrimerId(), oligoObjectsMap.get(objectKey));
                    allHetDimerPairsObjectsMapMapdb.put(objectKey.getInternalPrimerId(), oligoObjectsMap.get(objectKey));
                }

                /**
                // Get the Java runtime
                Runtime runtime = Runtime.getRuntime();
                // Run the garbage collector
                runtime.gc();
                // Calculate the used memory
                long memory = runtime.totalMemory() - runtime.freeMemory();
                System.out.println("Used memory is bytes: " + memory);
                System.out.println("Used memory is megabytes: "+ (memory/10000000.00));
                 **/
            }


        }

        //need to comment when done.
        //System.exit(1);


        for(SequenceObject so : objects){

            LinkedHashMap<String, List<OligoObject>>   hetDimerMapForSO = mfd.getHetDimersForRegion(allHetDimerPairsObjectsMap, so);
            Set<String> oligoKeys = hetDimerMapForSO.keySet();

            List<OligoObject> oligoKeysList = new ArrayList<OligoObject>();

            for(OligoObject sobj : so.getHetDimerOligosList()){
                for(String o : oligoKeys){
                    if(sobj.getInternalPrimerId().equalsIgnoreCase(o)){
                        oligoKeysList.add(sobj);
                        break;
                    }
                }
            }

            oligoKeysList = new OligoObject().sortOligosBySubsectionAndSerialNum(oligoKeysList);
            LinkedHashMap<OligoObject, List<OligoObject>> filteredhetDimerMapForSO = mfd.filterMapCreateOnlyHetsWithinDistanceMap(oligoKeysList, spacing, hetDimerMapForSO);
            LinkedHashMap<String, ArrayList<OligoObject>> setsOfOligoSets = new LinkedHashMap<String, ArrayList<OligoObject>>();

            int startingcounter = 1;
            //now create tree.

            ArrayList<OligoObject> filteredOligoKeysArrayList = new ArrayList<OligoObject>();

            //create array of keys from filteredhash
            filteredOligoKeysArrayList.addAll(filteredhetDimerMapForSO.keySet());

            //for(OligoObject obj : oligoKeysList){
            for(OligoObject obj : filteredOligoKeysArrayList){

                if((Integer.parseInt(obj.getInternalStart())-so.getStart()<=2000) && (Integer.parseInt(obj.getInternalStart())-so.getStart()>=1)){

                    Graph<OligoObject> dagOligo = new Graph<OligoObject>();
                    Vertex<OligoObject> rootvertex = new Vertex<OligoObject>("root:"+obj.getInternalPrimerId(), obj);
                    rootvertex.setData(obj);
                    rootvertex.setMarkState(0);

                    dagOligo.addVertex(rootvertex);
                    dagOligo.setRootVertex(rootvertex);

                    if(so.getStop()-Integer.parseInt(obj.getInternalStart())>=2000){
                        traverse(rootvertex, filteredhetDimerMapForSO, dagOligo, so);
                    }

                    startingcounter+=1;


                    LinkedHashMap<String, ArrayList<OligoObject>> arrayOfPathsForRoot = new LinkedHashMap<String, ArrayList<OligoObject>>();

                    final ArrayList<OligoObject> pathArrays = new ArrayList<OligoObject>();
                    int counter=1;

                    dagOligo.setMapOfOligoPathArrays(arrayOfPathsForRoot);
                    dagOligo.dfsSpanningTree(rootvertex, pathArrays, counter, dagOligo, new DFSVisitor<OligoObject>() {

                        //ArrayList<OligoObject> newPathArrays = new ArrayList<OligoObject>();
                        @Override
                        public void visit(Graph<OligoObject> g, Vertex<OligoObject> v) {
                            //System.out.println("at dag: "+ v.getName() + " num outgoing vertices: "+ v.getOutgoingEdgeCount() + " and outgoing verticeshash = ");
                        }

                        @Override
                        public void visit(Graph<OligoObject> g, Vertex<OligoObject> v, Edge<OligoObject> e) {

                        }
                    });

                    Set<String> keyset = dagOligo.getMapOfOligoPathArrays().keySet();

                    Iterator<String> keyit = keyset.iterator();
                    while (keyit.hasNext()){
                        String key1part = keyit.next();
                        String key2part = obj.getInternalPrimerId();
                        String key = key1part+key2part;

                        //System.out.println(key);
                        ArrayList<OligoObject> pathArray = dagOligo.getMapOfOligoPathArrays().get(key1part);

                        //sort by region and subsection. because we have het dimer interactions only for sorted Oligos.
                        pathArray = (ArrayList<OligoObject>) new OligoObject().sortOligosBySubsectionAndSerialNum(pathArray);


                        int toremoveFlag=0;
                        Float deltagForThisArray = Float.parseFloat("0.00");

                        for(int i=0; i<pathArray.size(); i++){
                            List<OligoObject> hetDimerOligoObjsList = hetDimerMapForSO.get(pathArray.get(i).getInternalPrimerId());

                            for(int j=i+1; j<pathArray.size(); j++){
                                int found=0;

                                for(OligoObject hetdimerOligoObjValue : hetDimerOligoObjsList){
                                    if(hetdimerOligoObjValue.getInternalPrimerId().equalsIgnoreCase(pathArray.get(j).getInternalPrimerId())){
                                        deltagForThisArray += hetdimerOligoObjValue.getHetdimerValue();
                                        found=1;
                                        break;
                                    }
                                }
                                if(found==0){
                                    toremoveFlag=1;
                                }

                            }

                            if(toremoveFlag==1){
                                break;
                            }
                        }

                        if(toremoveFlag==0){
                            Float avgDeltaGForThisArray = deltagForThisArray/pathArray.size();
                            String newKey = key+"&DelG="+avgDeltaGForThisArray;
                            setsOfOligoSets.put(newKey, dagOligo.getMapOfOligoPathArrays().get(key1part));
                        }
                    }


                }

                //clear hetDimerValues. 21stDec2016
                //obj.getHeterodimerValues().clear();
            }

            so.setOligoSetsFullMap(setsOfOligoSets);
            System.out.println("done with this so. sets of oligo sets size:"+ setsOfOligoSets.size());

        }


        //commenting this as of now because we ont need it since we have already filtered all interactions within region and across regions already 21st Dec 2016
        System.out.println("checking Oligos interaction across SO");
        ArrayList<ArrayList<String>> setOfSetsArr = new SequenceObject().checkOligosInteractAcrossSO(objects, allHetDimerPairsObjectsMap);

        //clearing large hashmapObject.
        allHetDimerPairsObjectsMap.clear();


        System.out.println("sorting oligo sets by minDeltaG");

        //changed sets to array.
        //Set<ArrayList<String>> setOfSets = convertArrayListToSet(setOfSetsArr);
        /*if(setOfSets.size()>0){
            objects = new SequenceObject().sortSetsByMinDeltaG(setOfSets, objects);
        }*/

        if(setOfSetsArr.size()>0){
            objects = new SequenceObject().sortArrByMinDeltaG(setOfSetsArr, objects);
        }

        objects = new SequenceObject().sortSequenceObjectSetsByMinDeltaG(objects);

        System.out.println("writing oligos to file");
        String oligosFilename = writeOligosFinalFile(objects, dataDir, finalOligos, projectId);


        //clear everything!!!
        for(SequenceObject so : objects){
            so.getOligoSetsFullMap().clear();
            //so.getHetDimerDagMap().clear();
            //so.getHetDimerOligosList().clear();
            //so.getHetDimerHashMap().clear();
        }



        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/processOligos.jsp");
        mvObj.addObject("uploadedPath", upFile);
        mvObj.addObject("sequenceObjects", objects);
        mvObj.addObject("optimalOligosFile", oligosFilename);
        mvObj.addObject("projectId", projectId);




        return mvObj;

    }




    /***
     *
     * @param vertex
     * @param filteredhetDimerMapForSO
     * @param dagOligo
     * @param so
     */
    private void traverse(Vertex<OligoObject> vertex, LinkedHashMap<OligoObject, List<OligoObject>> filteredhetDimerMapForSO, Graph<OligoObject> dagOligo, SequenceObject so) {
        OligoObject parentObj = vertex.getData();
        //System.out.println("parentObj:" + parentObj.getInternalPrimerId() + " " + parentObj.getInternalStart());
        /*if(vertex.getName().equalsIgnoreCase("Root")){
            System.out.println("-----------starting traversal from root vertex! Parent Obj is root vertex!----------------\n");
        }
*/

        if(filteredhetDimerMapForSO.get(parentObj)!=null && filteredhetDimerMapForSO.get(parentObj).size()>0){

            List<OligoObject> childrenObj = filteredhetDimerMapForSO.get(parentObj);

            //only return 8 or so children at a time.
            if(childrenObj.size()>=8){
                childrenObj = childrenObj.subList(0,8);
            }

            for(OligoObject childObj : childrenObj){

                Vertex<OligoObject> childVertex = new Vertex<OligoObject>(childObj.getInternalPrimerId(), childObj);
                //System.out.println("iterating through all children for "+vertex.getName()+" Now at childVertex:" + childObj.getInternalPrimerId()+ " Same as:"+ childVertex.getName()+" POS: "+ childObj.getInternalStart());

                //uncomenting today 18thnov 2016
                dagOligo.addVertex(vertex);

                childVertex.setData(childObj);

                dagOligo.addVertex(childVertex);

                boolean result = dagOligo.addEdge(vertex, childVertex, 1);

                List<Edge<OligoObject>> edgesList = dagOligo.getEdges();
                //System.out.println("edges list:"+ edgesList.size());

                if(so.getStop()-Integer.parseInt(childObj.getInternalStart())>=4000){
                    //System.out.println("Traversing from childVertex to its children:" + childObj.getInternalPrimerId() + " " + childObj.getInternalStart());
                    if(filteredhetDimerMapForSO.size()>0){
                        traverse(childVertex, filteredhetDimerMapForSO, dagOligo, so);
                    }
                }

                /*if(so.getStop()-Integer.parseInt(childObj.getInternalStart())<2000){
                    //System.out.println("End of this path at:" + childObj.getInternalPrimerId() + " at " + childObj.getInternalStart() + " with so stop at:" + so.getStop() + " starting at root vertex:"+ dagOligo.getRootVertex().getName());
                }*/
            }
        }
    }


    private String writeOligosFinalFile(ArrayList<SequenceObject> objects, String dataDir, String finalOligos, String projectId) throws Exception {

        String firstOptimalOligosFile = dataDir+finalOligos+projectId+"_primary.txt";
        File firstOligosFile = new File(firstOptimalOligosFile);
        PrintWriter pwFirst = new PrintWriter(firstOligosFile);
        pwFirst.println("Primer Set\tPrimer Id\tPrimerChr\tPrimer Start\tPrimer End\tSequence\tSequence Rev. Complement\tGC\tTm\tSize\tSelf Dimer\tHairpin Tm\tHairpin dG\tBlat");

        for(SequenceObject so : objects){

            String detailFileN=so.getDetailsFile();
            String secondaryOptimalOligosFile = so.getSecondaryFile();

            File detailFile = new File(detailFileN);
            File secondOligosFile = new File(secondaryOptimalOligosFile);

            PrintWriter pwDetail = new PrintWriter(detailFile);
            PrintWriter pwSecond = new PrintWriter(secondOligosFile);

            TreeMap<String, List<OligoObject>> optimalOligosTree = so.getOligoSetsTreeMap();


            //write optimal oligos file
            pwFirst.println("For query region: "+so.getChr()+":"+so.getStart()+"-"+so.getStop());
            System.out.println("For query region: " + so.getChr() + ":" + so.getStart() + "-" + so.getStop());

            if(so.getPrimaryOptimalSetOfOligosForSet()!=null && so.getPrimaryOptimalSetOfOligosForSet().size()>0){
                System.out.println("primary oligos set size:"+so.getPrimaryOptimalSetOfOligosForSet());
                LinkedHashMap<String, List<OligoObject>> primaryOligosSet = so.getPrimaryOptimalSetOfOligosForSet();

                System.out.println("primary oligo keyset size is:"+primaryOligosSet.keySet().size());

                String primarySet = primaryOligosSet.keySet().iterator().next();

                pwFirst.println(primarySet.split("&DelG=")[1]);

                for(OligoObject o : primaryOligosSet.get(primarySet)){

                    DNASequence seq = new DNASequence(o.getInternalSeq());
                    SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                    String revCompSeq = revcomp.getSequenceAsString();
                    pwFirst.println(primarySet.split("&DelG=")[0]+"\t"+ o.getInternalPrimerId() + "\t" + so.getChr() + "\t" + o.getInternalStart() + "\t" + (Integer.parseInt(o.getInternalStart())+o.getInternalLen()) + "\t" + o.getInternalSeq() + "\t"+ revCompSeq +"\t"
                            + o.getInternalGc() + "\t" + o.getInternalTm() + "\t" + o.getInternalLen() + "\t" + o.getHomodimerValue() + "\t-\t" + o.getHairpinValue() + "\t" + o.getInternalPrimerBlatList().size());
                }

            }else{
                pwFirst.println("NO oligos found");
            }


            //write detailed file
            pwDetail.println("<pre>"+so.getReportFile()+"</pre>");


            //remove writing details file for now!

            pwSecond.println("\"Primer Set\tPrimer Id\tPrimerChr\tPrimer Start\tPrimer End\tSequence\tSequence Rev. Complement\tGC\tTm\tSize\tSelf Dimer\tHairpin Tm\tHairpin dG\tBlat");

            if(optimalOligosTree!=null && optimalOligosTree.size()>0){
                for(String set : optimalOligosTree.keySet()){

                    pwSecond.println("\n"+set.split("&DelG=",-1)[1]);

                    //String key = set.split("_")[0];
                    for(OligoObject o : optimalOligosTree.get(set)){

                        DNASequence seq = new DNASequence(o.getInternalSeq());
                        SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                        String revCompSeq = revcomp.getSequenceAsString();

                        pwSecond.println(set.split("&DelG=")[0]+"\t"+ o.getInternalPrimerId() + "\t" + so.getChr() + "\t" + o.getInternalStart() + "\t" + (Integer.parseInt(o.getInternalStart())+o.getInternalLen()) + "\t" + o.getInternalSeq() + "\t"+revCompSeq+"\t"
                                + o.getInternalGc() + "\t" + o.getInternalTm() + "\t" + o.getInternalLen() + "\t" + o.getHomodimerValue() + "\t-\t" + o.getHairpinValue() + "\t" + o.getInternalPrimerBlatList().size());

                    }

                }
            }

            pwSecond.close();
            pwDetail.close();

        }

        pwFirst.close();


        return firstOptimalOligosFile;

    }




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

            //System.out.println(erranswer);

            //System.out.println(answer);

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
