package edu.chop.dgd.dgdObjects;

import edu.chop.dgd.process.primerCreate.AmpliconSeq;
import edu.chop.dgd.process.primerCreate.AmpliconXomAnalyzer;
import edu.chop.dgd.process.primerCreate.PrimerDAO;
import edu.chop.dgd.process.primerCreate.Variation;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by jayaramanp on 6/19/14.
 */
public class SequenceObject{
    String assembly;
    String chr;
    int start;
    int stop;
    List<SequenceObjectSubsections> oligoObjectSubsections;
    String reportFile;
    String filename;
    HashMap<OligoObject, List<OligoObject>> hetDimerHashMap;
    HashMap<String, List<OligoObject>> OligoSetsMap;
    List<OligoObject> hetDimerOligosList;
    TreeMap<String, List<OligoObject>> OligoSetsTreeMap;
    LinkedHashMap<String, Graph<OligoObject>> hetDimerDagMap;
    LinkedHashMap<String, ArrayList<OligoObject>> oligoSetsFullMap;
    LinkedHashMap<String, List<OligoObject>> primaryOptimalSetOfOligosForSet;

    String detailsFile;
    String secondaryFile;

    public List<SequenceObjectSubsections> generateSequenceSubsections(String inputFilename, String dataDir) throws Exception{

        String file = dataDir+inputFilename;
        PrintWriter subsectionWriter = new PrintWriter(file);

        int counter=0;

        for(int i=this.getStart(); i<=this.getStop(); ){

            int subsectStart = i;
            int windowSize = 6000;
            int subsectStop = i+windowSize;
            if(subsectStop>this.getStop()){
                subsectStop = this.getStop();
            }
            //i+=1000+1;
            i = subsectStop - 1000 + 1;
            counter+=1;

            SequenceObjectSubsections olSubsObj = new SequenceObjectSubsections();
            olSubsObj.setSubSectionAssembly(this.getAssembly());
            olSubsObj.setSubSectionChr(this.getChr());
            olSubsObj.setSubSectionStart(subsectStart);
            olSubsObj.setSubSectionStop(subsectStop);
            olSubsObj.setSubSectionWindowNum(counter);

            subsectionWriter.println(olSubsObj.getSubSectionChr() + ":" + olSubsObj.getSubSectionStart() + "-" + olSubsObj.getSubSectionStop());
            System.out.println(olSubsObj.getSubSectionChr() + ":" + olSubsObj.getSubSectionStart() + "-" + olSubsObj.getSubSectionStop());
            if(subsectStop == this.getStop()){
                break;
            }

        }

        subsectionWriter.close();

        String sequenceInfo = retrieveSubsectionSequencesProcessBuilder(inputFilename, dataDir);


        return createSequenceObjSubsections(sequenceInfo);

    }



    private List<SequenceObjectSubsections> createSequenceObjSubsections(String sequenceInfo) throws Exception {

        List<SequenceObjectSubsections> seqObjSubs = new ArrayList<SequenceObjectSubsections>();
        PrimerDAO prDao = new PrimerDAO();


        List<Variation> variantsInSequenceObject = prDao.getVariantsWithinRange(this.getChr(),
                this.getStart(), this.getStop(), this.getAssembly());

        String[] seqArray = sequenceInfo.split("\\>", -1);
        for(String fastaSeq : seqArray){
            if((fastaSeq.contains("chr")) && (!fastaSeq.contains("twoBitReadSeqFrag"))){
                String queryDelims = "[:+ -]+";
                String[] sequence = fastaSeq.split(queryDelims, -1);
                System.out.println(sequence[0]+"\n"+sequence[1]+"\n"+sequence[2]);
                String seq2 = sequence[3].replaceAll("\n", "").toLowerCase();
                String seq = sequence[3].replaceAll("\n", "").toUpperCase();

                SequenceObjectSubsections sosb = new SequenceObjectSubsections();
                sosb.setSubSectionChr(sequence[0]);
                sosb.setSubSectionStart(Integer.parseInt(sequence[1]));
                sosb.setSubSectionStop(Integer.parseInt(sequence[2]));
                sosb.setSubSectionAssembly(this.getAssembly());


                AmpliconSeq amplObj = new AmpliconSeq();
                amplObj.setChr(sosb.getSubSectionChr());
                amplObj.setAmpliconStart(sosb.getSubSectionStart());
                amplObj.setAmpliconEnd(sosb.getSubSectionStop());
                amplObj.setBufferUpstream(0);
                amplObj.setBufferDownstream(0);
                amplObj.setSequence(seq);

                String maskedSeq = amplObj.maskAmpliconSequenceForOligoToN(amplObj, variantsInSequenceObject);
                //String maskedlowercase = maskedSeq.replaceAll("N", "n");

                sosb.setSubSectionSequence(maskedSeq);

                seqObjSubs.add(sosb);
            }

        }

        return seqObjSubs;
    }


    private String retrieveSubsectionSequencesProcessBuilder(String inputFilename, String dataDir) throws Exception{


        String answer;
        String errAnswer="NA";

        String inpFile = dataDir+inputFilename;
        ProcessBuilder pb = new ProcessBuilder(dataDir+"RetrieveSequence.sh",inputFilename, this.getAssembly());
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/blat";
        env.put("PATH", path);

        pb.directory(new File(dataDir));

        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the RetrieveSequence.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the RetrieveSequence.sh..");

            BufferedReader bErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder errsb = new StringBuilder();
            String errline;
            while ((errline = bErr.readLine()) != null) {
                errsb.append(errline).append("\n");
            }
            String erranswer = errsb.toString();
            System.out.println(erranswer);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            answer = sb.toString();

            System.out.println(erranswer);

            System.out.println(answer);

            System.out.println("should have got an output from twoBitToFasta command...");


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


    public List<SequenceObjectSubsections> generateSubsections() throws Exception{
        List<SequenceObjectSubsections> subs = new ArrayList<SequenceObjectSubsections>();
        int counter=0;
        PrimerDAO prDao = new PrimerDAO();

        for(int i=this.getStart(); i<=this.getStop(); ){

            int subsectStart = i;
            int windowSize = 6000;
            int subsectStop = i+windowSize;
            i+=3000+1;
            counter+=1;

            SequenceObjectSubsections olSubsObj = new SequenceObjectSubsections();
            olSubsObj.setSubSectionChr(this.getChr());
            olSubsObj.setSubSectionStart(subsectStart);
            olSubsObj.setSubSectionStop(subsectStop);
            olSubsObj.setSubSectionWindowNum(counter);
            olSubsObj.setSubSectionAssembly(this.getAssembly());

            String dasSequence = prDao.getAmpliconSequence(olSubsObj.getSubSectionChr(), olSubsObj.getSubSectionStart(), olSubsObj.getSubSectionStop(), this.getAssembly());
            AmpliconXomAnalyzer xom = new AmpliconXomAnalyzer();
            String sequence = xom.parseRecord(dasSequence).replaceAll("\n","");

            System.out.println("here is the sequence:"+sequence);

            AmpliconSeq amplObj = new AmpliconSeq();
            amplObj.setChr(olSubsObj.getSubSectionChr());
            amplObj.setAmpliconStart(olSubsObj.getSubSectionStart());
            amplObj.setAmpliconEnd(olSubsObj.getSubSectionStop());
            amplObj.setBufferUpstream(0);
            amplObj.setBufferDownstream(0);
            amplObj.setSequence(sequence);

            String maskedSeq = amplObj.maskAmpliconSequence(amplObj, this.getAssembly());

            olSubsObj.setSubSectionSequence(maskedSeq);

            subs.add(olSubsObj);

        }

        return subs;
    }



    public Set<ArrayList<String>> checkOligosInteractAcrossSO(ArrayList<SequenceObject> objects) {



        ArrayList<ArrayList<String>> listofSets = new ArrayList<ArrayList<String>>();

        System.out.println("creating listofSets");

        for(int i=0; i<objects.size(); i++){

            if(objects.get(i).getOligoSetsFullMap()!=null && objects.get(i).getOligoSetsFullMap().size()>0){
                LinkedHashMap<String, ArrayList<OligoObject>> mapOfOligoObjectSetsForROI = objects.get(i).getOligoSetsFullMap();
                Set<String> mapOligoKeyset = mapOfOligoObjectSetsForROI.keySet();
                listofSets.add(new ArrayList<String>(mapOligoKeyset));
            }

        }

        System.out.println("create SetsOfArraysAcross SO");

        Set<ArrayList<String>> setsOfArraySetsAcrossSO = getCombinationsFilterSets(listofSets, objects);

        //see if you can merge the two methods into one.. so as to save memory.
        //Set<ArrayList<String>> setsOfArraySetsAcrossSO = getCombinations(listofSets);
        //Set<ArrayList<String>> filteredSetOfArraySetsAcrossSO = filterSetsBasedOnInteractions(setsOfArraySetsAcrossSO, objects);
        //return filteredSetOfArraySetsAcrossSO;

        return setsOfArraySetsAcrossSO;

    }



    private Set<ArrayList<String>> filterSetsBasedOnInteractions(Set<ArrayList<String>> setsOfArraySetsAcrossSO, ArrayList<SequenceObject> objects) {

        Set<ArrayList<String>> filteredSetOfArraySetsAcrossSO = new LinkedHashSet<ArrayList<String>>();
        Iterator<ArrayList<String>> arrayIt = setsOfArraySetsAcrossSO.iterator();
        while(arrayIt.hasNext()){
            ArrayList<String> arrayOfSets = arrayIt.next();

            int flagRemoveArrayListOfSets=0;

            for(int i=0; i<arrayOfSets.size(); i++){
                String setId = arrayOfSets.get(i);
                ArrayList<OligoObject> oligoObjArrInSet = new ArrayList<OligoObject>();

                for(SequenceObject obj : objects){
                    if(obj.getOligoSetsFullMap().get(setId)!=null && obj.getOligoSetsFullMap().get(setId).size()>0){
                        oligoObjArrInSet =  obj.getOligoSetsFullMap().get(setId);
                        break;
                    }
                }

                for(int j=i+1; j<arrayOfSets.size(); j++){

                    String nextSetId = arrayOfSets.get(j);
                    ArrayList<OligoObject> oligoObjArrInNextSet = new ArrayList<OligoObject>();

                    for(SequenceObject obj : objects){
                        if(obj.getOligoSetsFullMap().get(nextSetId)!=null && obj.getOligoSetsFullMap().get(nextSetId).size()>0){
                            oligoObjArrInNextSet =  obj.getOligoSetsFullMap().get(nextSetId);
                            break;
                        }
                    }


                    for(OligoObject o : oligoObjArrInSet){
                        for(OligoObject objInNextSet : oligoObjArrInNextSet){
                            if(o.getHeterodimerValues().get(objInNextSet.getInternalPrimerId())<-10.00){
                                flagRemoveArrayListOfSets=1;
                                break;
                            }

                        }

                        if(flagRemoveArrayListOfSets==1){
                            break;
                        }
                    }

                    if(flagRemoveArrayListOfSets==1){
                        break;
                    }
                }

                if(flagRemoveArrayListOfSets==1){
                    break;
                }
            }



            if(flagRemoveArrayListOfSets==0){
                filteredSetOfArraySetsAcrossSO.add(arrayOfSets);
            }

        }

        return filteredSetOfArraySetsAcrossSO;
    }



    private ArrayList<String> filterEachSetOfOligosBasedOnInteractions(ArrayList<String> arraySetToCheck, String setIdToCheckInteractionWith, ArrayList<SequenceObject> objects) {



        ArrayList<String> filteredArraySettoReturn = new ArrayList<String>();

        for(int i=0; i<arraySetToCheck.size(); i++){

            int flagRemoveArrayListOfSets=0;

            String setId = arraySetToCheck.get(i);
            ArrayList<OligoObject> oligoObjArrInSet = new ArrayList<OligoObject>();

            for(SequenceObject obj : objects){
                if(obj.getOligoSetsFullMap().get(setId)!=null && obj.getOligoSetsFullMap().get(setId).size()>0){
                    oligoObjArrInSet =  obj.getOligoSetsFullMap().get(setId);
                    break;
                }
            }

            String nextSetId = setIdToCheckInteractionWith;
            ArrayList<OligoObject> oligoObjArrInNextSet = new ArrayList<OligoObject>();

            for(SequenceObject obj : objects){
                if(obj.getOligoSetsFullMap().get(nextSetId)!=null && obj.getOligoSetsFullMap().get(nextSetId).size()>0){
                    oligoObjArrInNextSet =  obj.getOligoSetsFullMap().get(nextSetId);
                    break;
                }
            }


            for(OligoObject o : oligoObjArrInSet){
                for(OligoObject objInNextSet : oligoObjArrInNextSet){
                    if(o.getHeterodimerValues().get(objInNextSet.getInternalPrimerId())<-10.00){
                        flagRemoveArrayListOfSets=1;
                        break;
                    }

                }

                if(flagRemoveArrayListOfSets==1){
                    break;
                }
            }

            if(flagRemoveArrayListOfSets==0){
                filteredArraySettoReturn.add(setId);
            }
        }

        return filteredArraySettoReturn;
    }



    private Set<ArrayList<String>> getCombinations(ArrayList<ArrayList<String>> listofSets) {

        //Set<ArrayList<String>> setsOfArraySetsAcrossSO = new LinkedHashSet<ArrayList<String>>();

        Set<ArrayList<String>> combinations = new HashSet<ArrayList<String>>();
        Set<ArrayList<String>> newCombinations;

        int index = 0;

        // extract each of the integers in the first list
        // and add each to ints as a new list
        for(String i: listofSets.get(0)) {
            ArrayList<String> newList = new ArrayList<String>();
            newList.add(i);
            combinations.add(newList);
        }
        index++;
        while(index < listofSets.size()) {
            List<String> nextList = listofSets.get(index);
            newCombinations = new HashSet<ArrayList<String>>();
            for(ArrayList<String> first: combinations) {
                for(String second: nextList) {
                    ArrayList<String> newList = new ArrayList<String>();


                    newList.addAll(first);
                    newList.add(second);


                    newCombinations.add(newList);
                }
            }
            combinations = newCombinations;

            index++;
        }

        return combinations;

        //setsOfArraySetsAcrossSO = combinations;
        //return setsOfArraySetsAcrossSO;
    }


    private Set<ArrayList<String>> getCombinationsFilterSets(ArrayList<ArrayList<String>> listofSets, ArrayList<SequenceObject> objects) {

        //Set<ArrayList<String>> setsOfArraySetsAcrossSO = new LinkedHashSet<ArrayList<String>>();

        Set<ArrayList<String>> combinations = new HashSet<ArrayList<String>>();
        Set<ArrayList<String>> newCombinations;

        int index = 0;

        // extract each of the integers in the first list
        // and add each to ints as a new list
        for(String i: listofSets.get(0)) {
            ArrayList<String> newList = new ArrayList<String>();
            newList.add(i);
            combinations.add(newList);
        }
        index++;
        while(index < listofSets.size()) {
            List<String> nextList = listofSets.get(index);
            newCombinations = new HashSet<ArrayList<String>>();
            for(ArrayList<String> first: combinations) {
                for(String second: nextList) {
                    ArrayList<String> newList = new ArrayList<String>();

                    ArrayList<String> filteredFirst = filterEachSetOfOligosBasedOnInteractions(first, second, objects);

                    if(filteredFirst.size()>0){
                        newList.addAll(first);
                        newList.add(second);

                        newCombinations.add(newList);
                    }
                }
            }
            combinations = newCombinations;

            index++;
        }

        return combinations;

        //setsOfArraySetsAcrossSO = combinations;
        //return setsOfArraySetsAcrossSO;
    }


    public ArrayList<SequenceObject> sortSetsByMinDeltaG(Set<ArrayList<String>> setOfSets, ArrayList<SequenceObject> objects) {

        Iterator<ArrayList<String>> arrayIt = setOfSets.iterator();
        Float minSumDeltaGAcrosssets = Float.parseFloat("0.00");
        System.out.println("sorting sets by mindelta G. sets of sets size:"+ setOfSets.size());

        int counter =0;
        while(arrayIt.hasNext()){
            ArrayList<String> setArray = arrayIt.next();

            Float sumDeltaGAcrossSets = Float.parseFloat("0.00");

            for(String setid : setArray){

                for(SequenceObject obj :objects){

                    if(obj.getOligoSetsFullMap().get(setid)!=null && obj.getOligoSetsFullMap().containsKey(setid)){

                        Float delGForArrayOfoligosInEachSet = Float.parseFloat(setid.split("&DelG=", -1)[1]);
                        sumDeltaGAcrossSets += delGForArrayOfoligosInEachSet;

                        if(obj.getOligoSetsTreeMap()!=null){
                            TreeMap<String, List<OligoObject>> oligoHashmapSet = obj.getOligoSetsTreeMap();
                            oligoHashmapSet.put(setid, obj.getOligoSetsFullMap().get(setid));
                            //obj.setOligoSetsTreeMap(oligoHashmapSet);

                        }else{
                            TreeMap<String, List<OligoObject>> oligoHashmapSet = new TreeMap<String, List<OligoObject>>();
                            oligoHashmapSet.put(setid, obj.getOligoSetsFullMap().get(setid));
                            //obj.setOligoSetsTreeMap(oligoHashmapSet);

                        }

                        break;
                    }

                }

            }

           // System.out.println("sum deltaG across sets:" + sumDeltaGAcrossSets);
           // System.out.println("Min sum deltaG across sets:" + minSumDeltaGAcrosssets);

            if(minSumDeltaGAcrosssets>sumDeltaGAcrossSets){
                minSumDeltaGAcrosssets = sumDeltaGAcrossSets;
                //System.out.println("this set has min full set deltag"+setArray.toString());
                for(String setid : setArray){


                    for(SequenceObject obj :objects){
                        if(obj.getOligoSetsFullMap().get(setid)!=null && obj.getOligoSetsFullMap().containsKey(setid)){
                            LinkedHashMap<String, List<OligoObject>> primaryDelGOligoHashmapSet = new LinkedHashMap<String, List<OligoObject>>();
                            primaryDelGOligoHashmapSet.put(setid, obj.getOligoSetsFullMap().get(setid));
                            obj.setPrimaryOptimalSetOfOligosForSet(primaryDelGOligoHashmapSet);
                        }
                    }
                }
            }
        }

        return objects;
    }


    public ArrayList<SequenceObject> sortObjectFullSetMapByDelG(ArrayList<SequenceObject> objects) {

        for(SequenceObject so : objects){
            HashMap<String, ArrayList<OligoObject>> oligoSetMap = so.getOligoSetsFullMap();
            TreeMap<String, List<OligoObject>> oligoSetssortedByDelGMap = so.getOligoSetsTreeMap();

            Set<String> oligoSetKeys = oligoSetMap.keySet();
            Set<String> sorted = new TreeSet<String>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {

                    String[] o1Arr = o1.split("&DelG=", -1);
                    Float o1DegG = Float.parseFloat(o1Arr[1]);
                    String[] o2Arr = o1.split("&DelG=", -1);
                    Float o2DelG = Float.parseFloat(o2Arr[1]);

                    if(o1DegG<o2DelG){
                        return 1;
                    }else{
                        return -1;
                    }
                }
            });

            Iterator<String> sorterit = sorted.iterator();

            while(sorterit.hasNext()){
                String sortedKey = sorterit.next();
                oligoSetssortedByDelGMap.put(sortedKey, oligoSetMap.get(sortedKey));
            }

            so.setOligoSetsTreeMap(oligoSetssortedByDelGMap);
        }

        return objects;
    }




    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public List<SequenceObjectSubsections> getOligoObjectSubsections() {
        return oligoObjectSubsections;
    }

    public void setOligoObjectSubsections(List<SequenceObjectSubsections> oligoObjectSubsections) {
        this.oligoObjectSubsections = oligoObjectSubsections;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public HashMap<OligoObject, List<OligoObject>> getHetDimerHashMap() {
        return hetDimerHashMap;
    }

    public void setHetDimerHashMap(HashMap<OligoObject, List<OligoObject>> hetDimerHashMap) {
        this.hetDimerHashMap = hetDimerHashMap;
    }

    public HashMap<String, List<OligoObject>> getOligoSetsMap() {
        return OligoSetsMap;
    }

    public void setOligoSetsMap(HashMap<String, List<OligoObject>> oligoSetsMap) {
        OligoSetsMap = oligoSetsMap;
    }

    public TreeMap<String, List<OligoObject>> getOligoSetsTreeMap() {
        return OligoSetsTreeMap;
    }

    public void setOligoSetsTreeMap(TreeMap<String, List<OligoObject>> oligoSetsTreeMap) {
        OligoSetsTreeMap = oligoSetsTreeMap;
    }

    public String getSecondaryFile() {
        return secondaryFile;
    }

    public void setSecondaryFile(String secondaryFile) {
        this.secondaryFile = secondaryFile;
    }

    public String getDetailsFile() {
        return detailsFile;
    }

    public void setDetailsFile(String detailsFile) {
        this.detailsFile = detailsFile;
    }

    public LinkedHashMap<String, Graph<OligoObject>> getHetDimerDagMap() {
        return hetDimerDagMap;
    }

    public void setHetDimerDagMap(LinkedHashMap<String, Graph<OligoObject>> hetDimerDagMap) {
        this.hetDimerDagMap = hetDimerDagMap;
    }

    public LinkedHashMap<String, ArrayList<OligoObject>> getOligoSetsFullMap() {
        return oligoSetsFullMap;
    }

    public void setOligoSetsFullMap(LinkedHashMap<String, ArrayList<OligoObject>> oligoSetsFullMap) {
        this.oligoSetsFullMap = oligoSetsFullMap;
    }

    public LinkedHashMap<String, List<OligoObject>> getPrimaryOptimalSetOfOligosForSet() {
        return primaryOptimalSetOfOligosForSet;
    }

    public void setPrimaryOptimalSetOfOligosForSet(LinkedHashMap<String, List<OligoObject>> primaryOptimalSetOfOligosForSet) {
        this.primaryOptimalSetOfOligosForSet = primaryOptimalSetOfOligosForSet;
    }

    public List<OligoObject> getHetDimerOligosList() {
        return hetDimerOligosList;
    }

    public void setHetDimerOligosList(List<OligoObject> hetDimerOligosList) {
        this.hetDimerOligosList = hetDimerOligosList;
    }
}
