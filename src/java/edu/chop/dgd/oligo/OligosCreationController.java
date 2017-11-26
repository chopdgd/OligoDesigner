package edu.chop.dgd.oligo;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import edu.chop.dgd.Process.*;
import edu.chop.dgd.dgdObjects.*;
import edu.chop.dgd.dgdUtils.OSValidator;
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


        String primer3OligoadditionalParams = "PRIMER_INTERNAL_MAX_TM="+max_tm+"\nPRIMER_INTERNAL_OPT_TM="+opt_tm+"\nPRIMER_INTERNAL_MIN_TM="+min_tm+"\n"+
                "PRIMER_INTERNAL_MAX_GC="+max_gc+"\nPRIMER_INTERNAL_OPT_GC_PERCENT="+opt_gc+"\nPRIMER_INTERNAL_MIN_GC="+min_gc+"\n"+
                "PRIMER_INTERNAL_MAX_SIZE="+max_len+"\nPRIMER_INTERNAL_OPT_SIZE="+opt_len+"\nPRIMER_INTERNAL_MIN_SIZE="+min_len+"\n"+
                "PRIMER_INTERNAL_SALT_MONOVALENT="+na_ion+"\nPRIMER_INTERNAL_SALT_DIVALENT="+mg_ion+"\nPRIMER_INTERNAL_MAX_SELF_ANY="+self_any+"\n"+
                "PRIMER_INTERNAL_MAX_SELF_END="+self_end+"\n=";


        File fileToParse = new File(upFile+projectId+"/"+origFileName);
        ArrayList<SequenceObject> objects =  getObjectsFromFile(fileToParse, error, assembly);
        SequenceObjectSubsections soss = new SequenceObjectSubsections();
        String heterodimerReport="";

        //commenting because it will be generated as arrays combine to create the big array.
        /*List<OligoObject> heteroDimerObjectsList = new ArrayList<OligoObject>();*/
        MfoldDimer mfd = new MfoldDimer();

        //create new MapDB database to store all hetdimer objects:
        Serializer<OligoObject> serializer = new OligoSerializer();
        DB hetdimerdb = DBMaker.fileDB("/data/"+heterodimerInpDir+"/"+assembly+"_"+projectId+".db")
                .closeOnJvmShutdown().transactionEnable()
                .fileDeleteAfterClose().make();
        //stores all hetdimer object info. so that only storing the ids for the other maps, are enough further down.
        HTreeMap<String, Object> hetDimerHashMapMAPDB = hetdimerdb.hashMap("hetDimerHashMap"+assembly+"_"+projectId).keySerializer(Serializer.STRING).
                valueSerializer(new SerializerJava()).createOrOpen();


        int numthreads=2;
        int numcores = getNumberOfCPUCores();
        System.out.println(numcores);

        if(numcores>numthreads){
            numthreads = numcores;
        }
        //OligoSeedDaemon daemon = new OligoSeedDaemon(this.threads);
        OligoSeedDaemon daemon = new OligoSeedDaemon(objects.size(), numthreads);
        daemon.start(); int threadcount=0;

        //for (int i=0;i<fileList.size();i++) {
        for(SequenceObject so : objects){
            threadcount+=1;
            //OligoSeedThread jobThread = new OligoSeedThread(fileList.get(i), i+1, seedFinder, extendScore);
            OligoSeedThread jobThread = new OligoSeedThread(so, threadcount, projectId, assembly, primer3OligoadditionalParams);
            jobThread.setDataDir(dataDir);
            jobThread.setOligoInputDir(oligoInputDir);
            jobThread.setOligoOutputDir(oligoOutputDir);
            jobThread.setBlatInpDir(blatInpDir);
            jobThread.setBlatOpDir(blatOpDir);
            jobThread.setMfoldInpDir(mfoldInpDir);
            jobThread.setMfoldOpDir(mfoldOpDir);
            jobThread.setHomodimerOpDir(homodimerOpDir);
            jobThread.setHeterodimerInpDir(heterodimerInpDir);
            jobThread.setHeterodimerOpDir(heterodimerOpDir);
            jobThread.setOligoProcessScriptDir(oligoProcessScriptDir);
            jobThread.setFinalOligos(finalOligos);
            daemon.addJob(jobThread);
        }

        try {
            daemon.join();
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            System.out.println("Daemon interrupted, exiting");
            System.exit(1);
        }

        List<OligoObject> heteroDimerObjectsList = daemon.getCombinedResult();
        //daemon.destroy();
        //PrintWriter preHetdimerOligosBedWriter = new PrintWriter(dataDir+heterodimerInpDir+projectId+"_hetdimer_bedfile.bed");


        for(OligoObject hetdimerObj : heteroDimerObjectsList){
            //Using MAPDB: Creates a hashmap of all hetdimer objects instead of list.
            hetDimerHashMapMAPDB.put(hetdimerObj.getInternalPrimerId(), hetdimerObj);
            //preHetdimerOligosBedWriter.println(hetdimerObj.getChr()+"\t"+hetdimerObj.getInternalStart()+"\t"+hetdimerObj.getInternalStop()+"\t"+hetdimerObj.getInternalPrimerId());
        }

        //preHetdimerOligosBedWriter.close();

        System.out.println("sorting hetdimerobjectslist");
        heteroDimerObjectsList = new OligoUtils().sortOligosBySubsectionAndSerialNum(heteroDimerObjectsList);

        //Oct9th 2017 need to change this to: https://github.com/harishreedharan/MapDB/blob/master/src/test/java/examples/MultiMap.java
        System.out.println("Mapping Oligos and creating hetdimerInp sections");
        //LinkedHashMap<OligoObject, List<OligoObject>> oligoObjectsMap = mfd.mapOligosCreateHetDimerInpSections_new(heteroDimerObjectsList);
        //ArrayList<String[]> inputlistforHetDimerAnalysis = mfd.createSubsetofhetDimers(oligoObjectsMap);
        //oligoObjectsMap.clear();

        Multimap<String, String> oligoObjectsMap_multimap = mfd.mapOligosCreateHetDimerInpSections_newMapDB(heteroDimerObjectsList);
        ArrayList<String[]> inputlistforHetDimerAnalysis = mfd.createSubsetofhetDimers_multimap(oligoObjectsMap_multimap, hetDimerHashMapMAPDB);
        System.out.println("deleting oligoobjectsmap");
        oligoObjectsMap_multimap.clear();

        System.out.println("creating numfiles for hetdimer");
        int numfiles = 1; int numlines = 10000; //int numlinescopy = numlines;
        double temp = Math.ceil((inputlistforHetDimerAnalysis.size())/(double)(numlines));
        int temp1= (int) temp;
        if(temp1 != 0){
            numfiles = temp1;
        }else{
            numfiles = temp1 + 1;
        }


        System.out.println("starting parallel processing for hetdimer analysis");
        OligoHetDimerDaemon daemon_hetdimerMap = new OligoHetDimerDaemon(numfiles, numthreads);
        daemon_hetdimerMap.start();
        int hetdimer_threadcount=0;

        String hetdimerFilename = "oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
        //using MapDB, it will only add the two ids as a string and a float value as the value.
        DB db2 = DBMaker.fileDB(dataDir+heterodimerInpDir+hetdimerFilename+"allHetDimerPairsObjectsMap.db").closeOnJvmShutdown().transactionEnable().fileDeleteAfterClose().make();
        HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb = db2.hashMap("allHetDimerPairsObjectsMapMapdb").keySerializer(Serializer.STRING).valueSerializer(Serializer.FLOAT).createOrOpen();

        //getting first oligoId to begin with.
        int oligoIdStoppedAt = 0;
        //creating hetdimer input files upfront.
        for(int n=1; n<=numfiles; n++){
            System.out.println("starting hetdimer run in subset of file");
            oligoIdStoppedAt = mfd.createFileForHeterodimerAnalysis(inputlistforHetDimerAnalysis, heterodimerInpDir, dataDir, hetdimerFilename, n, oligoIdStoppedAt, numlines);
            System.out.println("OligoIdstoppedat:" + oligoIdStoppedAt);
        }

        System.out.println("Running Heterodimer analysis now");
        //running hetdimer analysis and deltaG filteration, only, in parallel.
        for(int n=1; n<=numfiles; n++){
            hetdimer_threadcount = n-1;
            OligoHetDimerThread jobThread_hetDimer = new OligoHetDimerThread(n, numlines, hetdimer_threadcount, hetdimerFilename, dataDir, heterodimerOpDir);
            daemon_hetdimerMap.addJob(jobThread_hetDimer);
            /*System.out.println("starting hetdimer run in subset of file");
            String hetDimerSubsectionIndexes = mfd.createFileRunHeterodimerAnalysis(inputlistforHetDimerAnalysis, heterodimerInpDir, dataDir, hetdimerFilename, n, oligoIdStoppedAt, numlines);
            oligoIdStoppedAt = Integer.parseInt(hetDimerSubsectionIndexes.split("&", -1)[0]);
            System.out.println("getting deltaG values for HetDimer Pairs");
            allHetDimerPairsObjectsMapMapdb = mfd.getDeltaGValuesForHetDimerPairs_createMapDBHash(allHetDimerPairsObjectsMapMapdb, dataDir, heterodimerOpDir, hetdimerFilename, n);*/

        }

        try {
            daemon_hetdimerMap.join();
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            System.out.println("Daemon interrupted, exiting");
            System.exit(1);
        }

        System.out.println("Combining all hetdimer results");
        allHetDimerPairsObjectsMapMapdb = daemon_hetdimerMap.getCombinedResultMap();


        //PrintWriter postHetdimerOligosBedWriter = new PrintWriter(dataDir+heterodimerOpDir+projectId+"_posthetdimer_bedfile.bed");
        //PrintWriter setsOfOligosBedWriter = new PrintWriter(dataDir+finalOligos+projectId+"_setsofoligos_bedfile.bed");

        for(SequenceObject so : objects){

            ArrayList<String> hetDimerIdListForSO = mfd.getHetDimersIdsForRegion(allHetDimerPairsObjectsMapMapdb, so, hetDimerHashMapMAPDB);
            hetDimerIdListForSO = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(hetDimerIdListForSO);

            DB db = DBMaker.memoryDB().closeOnJvmShutdown().transactionEnable().fileDeleteAfterClose().make();
            HTreeMap<String, Object> hetDimerMapForSO_mapDB_sorted = db.hashMap("hetDimerMapOnlySO_sorted"+so.getChr()+"_"+so.getStart()+"_"+so.getStop()).keySerializer(Serializer.STRING).valueSerializer(new SerializerJava()).createOrOpen();

            for(String id : hetDimerIdListForSO){
                OligoObject hetDimerOligoObj_sorted = (OligoObject) hetDimerHashMapMAPDB.get(id);
                hetDimerMapForSO_mapDB_sorted.put(id, hetDimerHashMapMAPDB.get(id));
                //postHetdimerOligosBedWriter.println(hetDimerOligoObj_sorted.getChr()+"\t"+hetDimerOligoObj_sorted.getInternalStart()+"\t"+hetDimerOligoObj_sorted.getInternalStop()+"\t"+hetDimerOligoObj_sorted.getInternalPrimerId());
            }

            db.commit();

            System.out.println("creating filterMapCreateOnlyHetsWithinDistanceMap_MapDB multimap for SO");
            //Create NavigableSet. set tuple serializer
            Multimap<String, String> filteredHetDimerMapForSO_multimap = ArrayListMultimap.create();
            filteredHetDimerMapForSO_multimap = mfd.filterMapCreateOnlyHetsWithinDistanceMap_MapDB(hetDimerIdListForSO, spacing, hetDimerMapForSO_mapDB_sorted, filteredHetDimerMapForSO_multimap);


            //now create tree.
            Set<String> filteredHetDimerMapForSO_multimap_keys = filteredHetDimerMapForSO_multimap.keySet();

            System.out.println("sorting oligoIDs ListbySubsectionAndSerial number for SO");
            ArrayList<String> filteredHetDimerMapForSO_multimap_keys_sorted = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(new ArrayList<String>(filteredHetDimerMapForSO_multimap_keys));


            //Creating a multi-map instead
            Multimap<String, String> setsOfOligoSets_mapDB = LinkedListMultimap.create();

            ArrayList<String> seedOligoslist = new ArrayList<String>();
            ArrayList<String> seedOligoslist_short = new ArrayList<String>();

            System.out.println("getting seed oligos");
            //if seed oligos found within 3kb, dont do 6kb.
            for(String oligoobjid : filteredHetDimerMapForSO_multimap_keys_sorted){
                OligoObject obj = (OligoObject) hetDimerMapForSO_mapDB_sorted.get(oligoobjid);
                if((obj.getInternalStart()-so.getStart()<=3000) && (obj.getInternalStart()-so.getStart()>=1)){
                    seedOligoslist.add(oligoobjid);
                }
            }


            //if no seed oligos found.. do 6kb
            if(seedOligoslist.size()==0){
                for(String oligoobjid : filteredHetDimerMapForSO_multimap_keys_sorted){
                    OligoObject obj = (OligoObject) hetDimerMapForSO_mapDB_sorted.get(oligoobjid);
                    if((obj.getInternalStart()-so.getStart()<=6000) && (obj.getInternalStart()-so.getStart()>=1)){
                        seedOligoslist.add(oligoobjid);
                    }
                }
            }

            //sorting the keys.
            //for(String oligoobjid : filteredHetDimerMapForSO_multimap_keys_sorted){
            if(seedOligoslist.size()>0){

                System.out.println("Num seed oligos:" + seedOligoslist.size());

                //only return 5-10 or so children at a time.Subject to change.
                if(so.getStop()-so.getStart()>=100000){
                    //more seed oligos but only 2 children per parent node. more of a binary tree.
                    if(seedOligoslist.size()>=5){
                        for(int s=0; s<5; s++){
                            seedOligoslist_short.add(seedOligoslist.get(s));
                        }
                        seedOligoslist.clear();
                        seedOligoslist = seedOligoslist_short;
                    }
                }else if(so.getStop()-so.getStart()<100000){
                    //feer seed oligos but 3 children per parent node. wider graph.
                    if(seedOligoslist.size()>=3){
                        for(int s=0; s<3; s++){
                            seedOligoslist_short.add(seedOligoslist.get(s));
                        }
                        seedOligoslist.clear();
                        seedOligoslist = seedOligoslist_short;
                    }
                }

                /*if(seedOligoslist.size()>=3){
                    for(int s=0; s<3; s++){
                        seedOligoslist_short.add(seedOligoslist.get(s));
                    }
                    seedOligoslist.clear();
                    seedOligoslist = seedOligoslist_short;
                }*/

                //Start GraphDaemon so as to parallelize graphs generation.
                OligoGraphDaemon graphDaemon = new OligoGraphDaemon(seedOligoslist.size(), numthreads);
                graphDaemon.start();

                int jobcount=0;
                int startingcounter = 1;
                for(String oligoobjid : seedOligoslist){

                    OligoGraphThread graphjobThread = new OligoGraphThread(oligoobjid, hetDimerMapForSO_mapDB_sorted, filteredHetDimerMapForSO_multimap, so, jobcount, startingcounter, allHetDimerPairsObjectsMapMapdb, spacing);
                    graphDaemon.addJob(graphjobThread);
                    jobcount+=1;
                    startingcounter++;
                }

                try {
                    graphDaemon.join();
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    System.out.println("Graph Daemon interrupted, exiting");
                    System.exit(1);
                }

                setsOfOligoSets_mapDB = graphDaemon.getCombinedResultMap();


            }else{
                System.out.println("SO has no seed oligos within 6Kb to create graphs");
            }

            so.setOligoSetsFullMapMultiMap(setsOfOligoSets_mapDB);

            db.close();
            System.out.println("done with this so. sets of oligo sets keys are:"+ setsOfOligoSets_mapDB.keySet());

        }

        //postHetdimerOligosBedWriter.close();
        //setsOfOligosBedWriter.close();

        System.out.println("checking Oligos interaction across SO");
        objects = new SequenceObject().checkOligosInteractAcrossSO_mapDB(objects, allHetDimerPairsObjectsMapMapdb);
        System.out.println("sorting oligo sets by minDeltaG");
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
        allHetDimerPairsObjectsMapMapdb.close();
        hetDimerHashMapMAPDB.close();
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

            //LinkedHashMap<String, ArrayList<String>> optimalOligosLinkedHashmap = so.getPrimaryOptimalSetOfOligosForSet();
            //write optimal oligos file
            pwFirst.println("For query region: "+so.getChr()+":"+so.getStart()+"-"+so.getStop());
            System.out.println("For query region: " + so.getChr() + ":" + so.getStart() + "-" + so.getStop());

            if(so.getPrimaryOptimalSetOfOligosForSet()!=null && so.getPrimaryOptimalSetOfOligosForSet().size()>0){
                System.out.println("primary oligos set size:"+so.getPrimaryOptimalSetOfOligosForSet());
                LinkedHashMap<String, ArrayList<String>> primaryOligosSet = so.getPrimaryOptimalSetOfOligosForSet();

                System.out.println("primary oligo keyset size is:"+primaryOligosSet.keySet().size());
                //String primarySet = primaryOligosSet.keySet().iterator().next();
                Iterator<String> primarySetKeySetit = primaryOligosSet.keySet().iterator();

                /*for(String o : primaryOligosSet.get(primarySet)){
                    OligoObject oligoobj = (OligoObject) hetDimerHashMapMAPDB.get(o);
                    DNASequence seq = new DNASequence(oligoobj.getInternalSeq());
                    SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                    String revCompSeq = revcomp.getSequenceAsString();
                    pwFirst.println(primarySet.split("inpSeq")[0]+"\t"+ oligoobj.getInternalPrimerId() + "\t" + so.getChr() + "\t" + oligoobj.getInternalStart()
                            + "\t" + (Integer.parseInt(oligoobj.getInternalStart())+oligoobj.getInternalLen())
                            + "\t" + oligoobj.getInternalSeq() + "\t"+ revCompSeq +"\t"
                            + oligoobj.getInternalGc() + "\t" + oligoobj.getInternalTm() + "\t" + oligoobj.getInternalLen()
                            + "\t" + oligoobj.getHomodimerValue() + "\t-\t" + oligoobj.getHairpinValue() + "\t" + oligoobj.getInternalPrimerBlatList().size());
                }*/


                while(primarySetKeySetit.hasNext()){
                    String primarySet = primarySetKeySetit.next();
                    for(String o : primaryOligosSet.get(primarySet)){
                        OligoObject oligoobj = (OligoObject) hetDimerHashMapMAPDB.get(o);
                        DNASequence seq = new DNASequence(oligoobj.getInternalSeq());
                        SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                        String revCompSeq = revcomp.getSequenceAsString();
                        pwFirst.println(primarySet.split("inpSeq")[0]+"\t"+ oligoobj.getInternalPrimerId() + "\t" + so.getChr() + "\t" + oligoobj.getInternalStart()
                                + "\t" + oligoobj.getInternalStop()
                                + "\t" + oligoobj.getInternalSeq() + "\t"+ revCompSeq +"\t"
                                + oligoobj.getInternalGc() + "\t" + oligoobj.getInternalTm() + "\t" + oligoobj.getInternalLen()
                                + "\t" + oligoobj.getHomodimerValue() + "\t-\t" + oligoobj.getHairpinValue() + "\t" + oligoobj.getInternalPrimerBlatList().size());
                    }
                }

            }else{
                pwFirst.println("NO oligos found");
            }
        }

        pwFirst.close();

        return firstOptimalOligosFile;

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
                //padding it by 3kb each end
                obj.setStart(Integer.parseInt(lineArr[1])-2000);
                obj.setStop(Integer.parseInt(lineArr[2])+2000);

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

    private int getNumberOfCPUCores() {
        OSValidator osValidator = new OSValidator();
        String command = "";
        if(osValidator.isMac()){
            command = "sysctl -n machdep.cpu.core_count";
        }else if(osValidator.isUnix()){
            command = "lscpu";
        }else if(osValidator.isWindows()){
            command = "cmd /C WMIC CPU Get /Format:List";
        }
        Process process = null;
        int numberOfCores = 0;
        int sockets = 0;
        try {
            if(osValidator.isMac()){
                String[] cmd = { "/bin/sh", "-c", command};
                process = Runtime.getRuntime().exec(cmd);
            }else{
                process = Runtime.getRuntime().exec(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if(osValidator.isMac()){
                    numberOfCores = line.length() > 0 ? Integer.parseInt(line) : 0;
                }else if (osValidator.isUnix()) {
                    if (line.contains("Core(s) per socket:")) {
                        numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                    }
                    if(line.contains("Socket(s):")){
                        sockets = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                    }
                } else if (osValidator.isWindows()) {
                    if (line.contains("NumberOfCores")) {
                        numberOfCores = Integer.parseInt(line.split("=")[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(osValidator.isUnix()){
            return numberOfCores * sockets;
        }
        return numberOfCores;
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