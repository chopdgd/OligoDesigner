package edu.chop.dgd.dgdObjects;

import com.google.common.collect.Multimap;
import edu.chop.dgd.dgdUtils.OligoUtils;
import edu.chop.dgd.process.primerCreate.AmpliconSeq;
import edu.chop.dgd.process.primerCreate.AmpliconXomAnalyzer;
import edu.chop.dgd.process.primerCreate.PrimerDAO;
import edu.chop.dgd.process.primerCreate.Variation;
import org.mapdb.HTreeMap;

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
    LinkedHashMap<String, ArrayList<String>> oligoSetsFullMap;
    Multimap<String, String> oligoSetsFullMapMultiMap;
    LinkedHashMap<String, ArrayList<String>> primaryOptimalSetOfOligosForSet;

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







    ////Need to write code!!!!
    public ArrayList<SequenceObject> checkOligosInteractAcrossSO_mapDB(ArrayList<SequenceObject> objects, HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb) {

        ArrayList<ArrayList<String>> oligosAcrossSets = new ArrayList<ArrayList<String>>();
        int count=0;
        if(objects.size()>1){
            getViableSetsOfSets(objects, allHetDimerPairsObjectsMapMapdb, count, oligosAcrossSets);
            System.out.println("should have got viable sets!!!");

        }else{
            //only one sequencxe object in question.
            SequenceObject o = objects.get(0);
            LinkedHashMap<String, ArrayList<String>> oligoSetsFullMap = new LinkedHashMap<String, ArrayList<String>>();
            Random random = new Random();
            Multimap<String, String> setofOligosInObject = o.getOligoSetsFullMapMultiMap();
            List<String> setofOligoskeys = new ArrayList<String>(setofOligosInObject.keySet());
            Collections.shuffle(setofOligoskeys);
            String randomKey = setofOligoskeys.get(random.nextInt(setofOligoskeys.size()));
            oligoSetsFullMap.put(randomKey, new ArrayList<String>(setofOligosInObject.get(randomKey)));
            o.setPrimaryOptimalSetOfOligosForSet(oligoSetsFullMap);
        }

        return objects;

    }






    private void getViableSetsOfSets(ArrayList<SequenceObject> objects, HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb, int count, ArrayList<ArrayList<String>> oligosAcrossSets) {

        Random       random    = new Random();
        ArrayList<String> alloligostotest = new ArrayList<String>();

        //store set of randomkeys so as to put back to SequenceObject
        ArrayList<String> setOfRandomKeys = new ArrayList<String>();

        for(SequenceObject so : objects){
            if(so.getOligoSetsFullMapMultiMap().size()>0){
                Multimap<String, String> setofOligosInObject = so.getOligoSetsFullMapMultiMap();
                List<String> setofOligoskeys = new ArrayList<String>(setofOligosInObject.keySet());
                Collections.shuffle(setofOligoskeys);
                String randomKey = setofOligoskeys.get(random.nextInt(setofOligoskeys.size()));
                ArrayList<String> oligosInSet = new ArrayList<String>(setofOligosInObject.get(randomKey));
                oligosAcrossSets.add(oligosInSet);
                setOfRandomKeys.add(randomKey);
                oligosInSet.clear();
            }else{
                System.out.println("This SO doesnt have viable oligos!!");
            }
        }

        for(ArrayList<String> selectedOligosArr : oligosAcrossSets){
            alloligostotest.addAll(selectedOligosArr);
        }

        alloligostotest = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(alloligostotest);
        //logic to get viable sets of sets. if found a set, increase count. get one viable list of sets, then quit.
        int stopCheckingThisloop=0;
        for(int objinarr=0; objinarr<alloligostotest.size()-1; objinarr++){
            for (int objinsubarr=objinarr+1; objinsubarr<alloligostotest.size(); objinsubarr++){
                String oligokey1 = alloligostotest.get(objinarr);
                String oligokey2 = alloligostotest.get(objinsubarr);
                String keyToCheckInHashMap1 = oligokey1+"&"+oligokey2;
                String keyToCheckInHashMap2 = oligokey2+"&"+oligokey1;
                if(!(allHetDimerPairsObjectsMapMapdb.containsKey(keyToCheckInHashMap1))){
                    stopCheckingThisloop=1;
                    break;
                }
            }

            if(stopCheckingThisloop==1){
                break;
            }
        }

        //if all het dimer paiors are present in the hash, we have found ourselves a viable set of sets.
        if(stopCheckingThisloop==0){
            count+=1;
            for(String randomsetIdInSequenceObj : setOfRandomKeys){
                System.out.println(randomsetIdInSequenceObj);
                for(SequenceObject seqObj : objects){
                    if(seqObj.getOligoSetsFullMapMultiMap().containsKey(randomsetIdInSequenceObj)){
                        Multimap<String, String> setOfOligosMultimap = seqObj.getOligoSetsFullMapMultiMap();
                        LinkedHashMap<String, ArrayList<String>> setOfPrimaryOligosForSeqObj = new LinkedHashMap<String, ArrayList<String>>();
                        setOfPrimaryOligosForSeqObj.put(randomsetIdInSequenceObj, new ArrayList<String>(setOfOligosMultimap.get(randomsetIdInSequenceObj)));
                        seqObj.setPrimaryOptimalSetOfOligosForSet(setOfPrimaryOligosForSeqObj);
                        break;
                    }
                }
            }

        }


        //if count is still 0 i.e not found a viable set yet!
        if(count<1){
            //clear previous set of sets.
            oligosAcrossSets.clear();
            //iterate!!
            getViableSetsOfSets(objects, allHetDimerPairsObjectsMapMapdb, count, oligosAcrossSets);
        }

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

    public LinkedHashMap<String, ArrayList<String>> getOligoSetsFullMap() {
        return oligoSetsFullMap;
    }

    public void setOligoSetsFullMap(LinkedHashMap<String, ArrayList<String>> oligoSetsFullMap) {
        this.oligoSetsFullMap = oligoSetsFullMap;
    }

    public LinkedHashMap<String, ArrayList<String>> getPrimaryOptimalSetOfOligosForSet() {
        return primaryOptimalSetOfOligosForSet;
    }

    public void setPrimaryOptimalSetOfOligosForSet(LinkedHashMap<String, ArrayList<String>> primaryOptimalSetOfOligosForSet) {
        this.primaryOptimalSetOfOligosForSet = primaryOptimalSetOfOligosForSet;
    }

    public List<OligoObject> getHetDimerOligosList() {
        return hetDimerOligosList;
    }

    public void setHetDimerOligosList(List<OligoObject> hetDimerOligosList) {
        this.hetDimerOligosList = hetDimerOligosList;
    }

    public Multimap<String, String> getOligoSetsFullMapMultiMap() {
        return oligoSetsFullMapMultiMap;
    }

    public void setOligoSetsFullMapMultiMap(Multimap<String, String> oligoSetsFullMapMultiMap) {
        this.oligoSetsFullMapMultiMap = oligoSetsFullMapMultiMap;
    }


}
