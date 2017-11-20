package edu.chop.dgd.dgdObjects;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import edu.chop.dgd.dgdUtils.OligoUtils;
import org.mapdb.HTreeMap;

import java.io.*;
import java.util.*;

/**
 * Created by jayaramanp on 9/25/14.
 */
public class MfoldDimer {
    String dimer1Id;
    String dimer2Id;
    Float dimerDeltaG;

    /**
     *
     * @param oligoObjectsFromPrimer3
     * @param fileName
     * @param homodimerOpDir
     * @param dataDir
     * @return
     * @throws Exception
     */
    public List<OligoObject> getDeltaGValuesForHomoDimer(List<OligoObject> oligoObjectsFromPrimer3, String fileName, String homodimerOpDir, String dataDir) throws Exception {

        File mfoldFile = new File(dataDir+homodimerOpDir+"/"+fileName+"_"+fileName+".ct");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mfoldFile)));

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String oligoHeader = lineArr[3].split("-", -1)[0];
                    for(OligoObject o:oligoObjectsFromPrimer3){
                        if(o.getInternalPrimerId().equals(oligoHeader)){
                            String homodimerValue = lineArr[1].split(" = ", -1)[1];
                            o.setHomodimerValue(Float.parseFloat(homodimerValue));
                        }
                    }
                }
            }

        }finally {
            reader.close();
        }

        //delete mfold homodimer file when done;
        mfoldFile.delete();

        return oligoObjectsFromPrimer3;
    }


    /**
     *
     * @param oligoKeysList
     * @param spacing
     * @param hetDimerMapForSO
     * @return
     * @throws Exception
     */
    public LinkedHashMap<OligoObject, List<OligoObject>> filterMapCreateOnlyHetsWithinDistanceMap(List<OligoObject> oligoKeysList, int spacing, LinkedHashMap<String, List<OligoObject>> hetDimerMapForSO) throws Exception{

        LinkedHashMap<OligoObject, List<OligoObject>> filteredoligoObjectsMap = new LinkedHashMap<OligoObject, List<OligoObject>>();
        for(OligoObject oligoObj : oligoKeysList){
            List<OligoObject> nextBinOligosWithinSpacing = getNext8_10KBOligoObjs(oligoObj, oligoKeysList, spacing, hetDimerMapForSO);
            if(nextBinOligosWithinSpacing.size()>0){
                filteredoligoObjectsMap.put(oligoObj, nextBinOligosWithinSpacing);
            }
        }

        System.out.println("returning hashmap of hets");

        return filteredoligoObjectsMap;


    }

    /**
     *
     * @param oligoObjectsMap
     * @param dataDir
     * @param heterodimerOpDir
     * @param fileName
     * @param subpartnum
     * @return
     * @throws Exception
     */
    public LinkedHashMap<OligoObject, List<OligoObject>> getDeltaGValuesForHetDimerPairs_new(LinkedHashMap<OligoObject, List<OligoObject>> oligoObjectsMap, String dataDir, String heterodimerOpDir, String fileName, int subpartnum) throws Exception {

        String hetOpFilename = dataDir+heterodimerOpDir+fileName+"_"+subpartnum+"_1_"+fileName+"_"+subpartnum+"_2.out";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(hetOpFilename)));
        //Set<OligoObject> hashMapKeys = oligoObjectsMap.keySet();
        //HashMap<String, Float> hetDimerDeltaGValuesMap = new HashMap<String, Float>();

        Set<OligoObject> oligoKeys = oligoObjectsMap.keySet();
        List<OligoObject> oligoKeysList = new ArrayList<OligoObject>();
        for(OligoObject o : oligoKeys){
            oligoKeysList.add(o);
        }

        oligoKeysList = new OligoUtils().sortOligosBySubsectionAndSerialNum(oligoKeysList);

        try{
            String line;
            while((line=reader.readLine()) != null){
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String[] oligoHeaderArr = lineArr[3].split("-", -1);
                    String hetOligoHeader1 = oligoHeaderArr[0];
                    String hetOligoHeader2 = oligoHeaderArr[1];

                    for(OligoObject o:oligoKeysList){
                        if(o.getInternalPrimerId().equals(hetOligoHeader1)){
                            List<OligoObject> hetObjects = oligoObjectsMap.get(o);
                            for(OligoObject oObj : hetObjects){
                                if(oObj.getInternalPrimerId().equals(hetOligoHeader2)){
                                    String hetdimerValue = lineArr[1].split(" = ", -1)[1];
                                    if(Float.parseFloat(hetdimerValue) >= -10.00){
                                        oObj.setHetdimerValue(Float.parseFloat(hetdimerValue));
                                    }else{
                                        hetObjects.remove(hetObjects.indexOf(oObj));
                                    }
                                    break;
                                }
                            }

                            oligoObjectsMap.put(o, hetObjects);
                            break;
                        }
                    }
                }
            }

        }finally {
            reader.close();
        }

        return oligoObjectsMap;
    }


    /**
     *
     * @param heteroDimerObjectsList
     * @param heterodimerInpDir
     * @param dataDir
     * @param fileName
     * @param serialNum
     * @return
     * @throws Exception
     */
    public LinkedHashMap<OligoObject, List<OligoObject>> mapOligosRunHeterodimerAnalysis(List<OligoObject> heteroDimerObjectsList,
                                                                                    String heterodimerInpDir, String dataDir, String fileName, int serialNum) throws Exception {
        String file1 = fileName+"_"+serialNum+"_1";
        String file2 = fileName+"_"+serialNum+"_2";
        File hetInpFile1 = new File(dataDir+heterodimerInpDir+file1);
        File hetInpFile2 = new File(dataDir+heterodimerInpDir+file2);
        PrintWriter pw1 = new PrintWriter(hetInpFile1);
        PrintWriter pw2 = new PrintWriter(hetInpFile2);

        LinkedHashMap<OligoObject, List<OligoObject>> hetDimerObjMap = new LinkedHashMap<OligoObject, List<OligoObject>>();

        for(OligoObject o : heteroDimerObjectsList){
                List<OligoObject> hetObjectList = new ArrayList<OligoObject>();
            for(OligoObject valueObj : heteroDimerObjectsList){
                if(!valueObj.getInternalPrimerId().equals(o.getInternalPrimerId())){
                    hetObjectList.add(valueObj);
                    pw1.println(">"+o.getInternalPrimerId());
                    pw1.println(o.getInternalSeq());
                    pw2.println(">"+valueObj.getInternalPrimerId());
                    pw2.println(valueObj.getInternalSeq());
                }
            }
            hetDimerObjMap.put(o, hetObjectList);
        }

        pw1.close();
        pw2.close();

        String resultHeterodimerString = runHeterodimerAnalysisProcessBuilder(file1, file2, dataDir);
        //System.out.println(resultHeterodimerString);

        return hetDimerObjMap;
    }

    /**
     *
     * @param heteroDimerObjectsList
     * @return
     * @throws Exception
     */
    public LinkedHashMap<OligoObject, List<OligoObject>> mapOligosCreateHetDimerInpSections(List<OligoObject> heteroDimerObjectsList) throws Exception {

        LinkedHashMap<OligoObject, List<OligoObject>> hetDimerObjMap = new LinkedHashMap<OligoObject, List<OligoObject>>();
        for(OligoObject o : heteroDimerObjectsList){
            List<OligoObject> hetObjectList = new ArrayList<OligoObject>();
            for(OligoObject valueObj : heteroDimerObjectsList){
                if(!valueObj.getInternalPrimerId().equals(o.getInternalPrimerId())){
                    hetObjectList.add(valueObj);
                }
            }
            hetDimerObjMap.put(o, hetObjectList);
        }
        return hetDimerObjMap;
    }

    /**
     *
     * @param heteroDimerObjectsList
     * @return
     * @throws Exception
     */
    public LinkedHashMap<OligoObject, List<OligoObject>> mapOligosCreateHetDimerInpSections_new(List<OligoObject> heteroDimerObjectsList) throws Exception {

        LinkedHashMap<OligoObject, List<OligoObject>> hetDimerObjMap = new LinkedHashMap<OligoObject, List<OligoObject>>();
        for(int i=0; i< heteroDimerObjectsList.size(); i++){

            OligoObject o = heteroDimerObjectsList.get(i);
            List<OligoObject> hetObjectList = new ArrayList<OligoObject>();

            for(int j=i+1; j<heteroDimerObjectsList.size(); j++){
                hetObjectList.add(heteroDimerObjectsList.get(j));
            }

            hetDimerObjMap.put(o, hetObjectList);
        }

        return hetDimerObjMap;
    }

    /**
     *
     * @param heteroDimerObjectsList
     * @return
     * @throws Exception
     */
    public Multimap<String, String> mapOligosCreateHetDimerInpSections_newMapDB(List<OligoObject> heteroDimerObjectsList) throws Exception {

        //LinkedHashMap<String, List<String>> hetDimerObjMap = new LinkedHashMap<String, List<String>>();
        Multimap<String, String> hetDimerObjIdsMap = LinkedListMultimap.create();
        for(int i=0; i< heteroDimerObjectsList.size(); i++){

            OligoObject o = heteroDimerObjectsList.get(i);
            List<OligoObject> hetObjectList = new ArrayList<OligoObject>();

            for(int j=i+1; j<heteroDimerObjectsList.size(); j++){
                hetObjectList.add(heteroDimerObjectsList.get(j));
                hetDimerObjIdsMap.put(o.getInternalPrimerId(), heteroDimerObjectsList.get(j).getInternalPrimerId());
            }
        }

        return hetDimerObjIdsMap;
    }


    /**
     *
     * @param hetFile1
     * @param hetFile2
     * @param dataDir
     * @return
     * @throws Exception
     */
    public String runHeterodimerAnalysisProcessBuilder(String hetFile1, String hetFile2, String dataDir) throws Exception{

        String answer;
        String errAnswer="NA";

        ProcessBuilder pb = new ProcessBuilder(dataDir+"HeteroDimerAnalyzeProcess.sh",hetFile1, hetFile2);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
        path += ":/usr/local/mfold/bin";
        path += ":/usr/local/mfold/share";
        env.put("PATH", path);

        pb.directory(new File(dataDir));
        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the HeteroDimerAnalyzeProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the HeteroDimerAnalyzeProcess.sh..");

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
            System.out.println(erranswer);
            System.out.println(answer);
            System.out.println("should have got an output from hybrid-min..");

            if(errAnswer.length()>2){
                throw new Exception("Exception: check program params!");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;
    }


    /**
     *
     * @param sequenceObjectSubsectionsList
     * @return
     */
    public List<OligoObject> filterOligosCreateHeterodimers(List<SequenceObjectSubsections> sequenceObjectSubsectionsList) {

        List<OligoObject> hetDimerInputs = new ArrayList<OligoObject>();
        for(SequenceObjectSubsections s : sequenceObjectSubsectionsList){
            List<OligoObject> oligoList = s.getOligoList();
            if(s.getOligoList()!=null){
                for(OligoObject o : oligoList){
                    if((o.getInternalPrimerBlatList()!=null) && (o.getInternalPrimerBlatList().size()==1) &&
                            (Double.compare(o.getInternalPrimerBlatList().get(0).getPercentageIdentity(), Double.parseDouble("99.00"))>=0)){
                        if(o.getHairpinValue()>-2.00 && o.getHomodimerValue()>-10.00){
                            hetDimerInputs.add(o);
                        }
                    }
                }
            }
        }

        return hetDimerInputs;
    }


    /**
     *
     * @param heterodimerOligosHashMap
     * @param sObj
     * @param spacingKB
     * @return
     * @throws Exception
     */
    public HashMap<String,List<OligoObject>> createMapSetsOfHets(LinkedHashMap<OligoObject, List<OligoObject>> heterodimerOligosHashMap,
                                                                 SequenceObject sObj, int spacingKB) throws Exception {

        LinkedHashMap<String, List<OligoObject>> hashSetOfHetPrimers = new LinkedHashMap<String, List<OligoObject>>();

        Set<OligoObject> oligoKeys = heterodimerOligosHashMap.keySet();
        List<OligoObject> oligoKeysList = new ArrayList<OligoObject>();
        for(OligoObject o : oligoKeys){
            oligoKeysList.add(o);
        }

        oligoKeysList = new OligoUtils().sortOligosBySubsectionAndSerialNum(oligoKeysList);

        int counter=1;

        System.out.println("getting hash map sets of hets");
        hashSetOfHetPrimers = getHashMapOfhetSets(oligoKeysList, oligoKeysList.get(0), hashSetOfHetPrimers, sObj, counter, spacingKB);

        return hashSetOfHetPrimers;

    }


    /***
     *
     * @param oligoKeysList
     * @param startingOligoHetObject
     * @param hashSetOfHetPrimers
     * @param sObj
     * @param counter
     * @param spacingKB
     * @return
     * @throws Exception
     */
    public LinkedHashMap<String, List<OligoObject>> getHashMapOfhetSets(List<OligoObject> oligoKeysList,
                        OligoObject startingOligoHetObject, LinkedHashMap<String, List<OligoObject>> hashSetOfHetPrimers,
                        SequenceObject sObj, int counter, int spacingKB) throws Exception {

        String set = "set"+counter;

        while(startingOligoHetObject.getInternalStart()-sObj.getStart()<=4000){

            int i=oligoKeysList.indexOf(startingOligoHetObject);

            for(; i<oligoKeysList.size(); ){

                if(hashSetOfHetPrimers.size()==0){
                    if(startingOligoHetObject.getInternalStart()-sObj.getStart()>=2000){
                        List<OligoObject> setOfHets = new ArrayList<OligoObject>();
                        setOfHets.add(oligoKeysList.get(i));
                        hashSetOfHetPrimers.put(set, setOfHets);
                    }else{
                        i++;
                        startingOligoHetObject = oligoKeysList.get(i);
                    }

                }else if(hashSetOfHetPrimers.size()>0){
                    if(sObj.getStop()-oligoKeysList.get(i).getInternalStart()>=2000){
                        List<OligoObject> setOfhets =  new ArrayList<OligoObject>();
                        if(hashSetOfHetPrimers.get(set)!=null){
                            setOfhets = hashSetOfHetPrimers.get(set);
                        }

                        OligoObject nextHetObj = getNext8KBOligoObj(oligoKeysList.get(i), oligoKeysList, spacingKB);
                        if(nextHetObj!=null){
                            i = oligoKeysList.indexOf(nextHetObj);
                            setOfhets.add(nextHetObj);
                            hashSetOfHetPrimers.put(set, setOfhets);
                        }else{
                            hashSetOfHetPrimers.put(set, setOfhets);

                            //increase Counter start new set
                            counter+=1;
                            set = "set"+counter;
                            i=oligoKeysList.indexOf(startingOligoHetObject);
                            i+=1;
                            startingOligoHetObject=oligoKeysList.get(i);
                            break;
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

        return hashSetOfHetPrimers;
    }



    /***
     *
     * @param objInQuestion
     * @param oligosList
     * @param spacingKB
     * @return
     * @throws Exception
     */

    public OligoObject getNext8KBOligoObj(OligoObject objInQuestion, List<OligoObject> oligosList, int spacingKB) throws Exception{

        int diffLessThan1 = (spacingKB*1000)+2000;
        int diffGreaterThan1 = (spacingKB*1000)-2000;

        int diffLessThan2 = (spacingKB*1000)+4000;
        int diffGreaterThan2 = (spacingKB*1000)-4000;

        int diffLessThan3 = (spacingKB*1000)+6000;
        int diffGreaterThan3 = (spacingKB*1000)-6000;

        for(OligoObject o : oligosList){
            int oligoStartDiff = o.getInternalStart()-objInQuestion.getInternalStart();
            if(diffLessThan1>oligoStartDiff && oligoStartDiff>diffGreaterThan1){
                return o;
            }
        }

        for(OligoObject o : oligosList){
            int oligoStartDiff = o.getInternalStart()-objInQuestion.getInternalStart();
            if(diffLessThan2>oligoStartDiff && oligoStartDiff>diffGreaterThan2){
                return o;
            }
        }

        for(OligoObject o : oligosList){
            int oligoStartDiff = o.getInternalStart()-objInQuestion.getInternalStart();
            if(diffLessThan3>oligoStartDiff && oligoStartDiff>diffGreaterThan3){
                return o;
            }
        }

        return null;
    }



    /***
     *
     *
     * @param objInQuestion
     * @param oligosList
     * @param spacingKB
     * @param hetDimerMapForSO
     * @return
     * @throws Exception
     */

    public List<OligoObject> getNext8_10KBOligoObjs(OligoObject objInQuestion, List<OligoObject> oligosList, int spacingKB, LinkedHashMap<String, List<OligoObject>> hetDimerMapForSO) throws Exception{

        int diffLessThan0 = (spacingKB*1000)+500;
        int diffGreaterThan0 = (spacingKB*1000)-500;

        int diffLessThan1 = (spacingKB*1000)+2000;
        int diffGreaterThan1 = (spacingKB*1000)-2000;

        int diffLessThan2 = (spacingKB*1000)+4000;
        int diffGreaterThan2 = (spacingKB*1000)-4000;

        int diffLessThan3 = (spacingKB*1000)+6000;
        int diffGreaterThan3 = (spacingKB*1000)-6000;

        ArrayList<OligoObject> oligosReturned = new ArrayList<OligoObject>();

        for(OligoObject o : oligosList){
            int oligoStartDiff = o.getInternalStart()-objInQuestion.getInternalStart();
            if(diffLessThan1>oligoStartDiff && oligoStartDiff>diffGreaterThan1){

                //check if obj is present in hetDimerMapForSO.
                if(hetDimerMapForSO.containsKey(objInQuestion.getInternalPrimerId())){
                    List<OligoObject> hetDimerInteractionsList = hetDimerMapForSO.get(objInQuestion.getInternalPrimerId());
                    for(OligoObject hetDimerInteractionObj : hetDimerInteractionsList){
                        if(hetDimerInteractionObj.getInternalPrimerId().equalsIgnoreCase(o.getInternalPrimerId())){
                            oligosReturned.add(o);
                            break;
                        }
                    }
                }

            }
        }

        return oligosReturned;
    }


    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MfoldDimer that = (MfoldDimer) o;

        if (!dimer1Id.equals(that.dimer1Id)) return false;
        if (!dimer2Id.equals(that.dimer2Id)) return false;
        if (!dimerDeltaG.equals(that.dimerDeltaG)) return false;

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = dimer1Id.hashCode();
        result = 31 * result + dimer2Id.hashCode();
        result = 31 * result + dimerDeltaG.hashCode();
        return result;
    }

    /**
     *
     * @param allHetDimerPairsObjectsMap
     * @param so
     * @return
     */
    public LinkedHashMap<String, List<OligoObject>> getHetDimersForRegion(LinkedHashMap<String, List<OligoObject>> allHetDimerPairsObjectsMap, SequenceObject so) {


        LinkedHashMap<String, List<OligoObject>> hetDimersForGivenRegion = new LinkedHashMap<String, List<OligoObject>>();
        Set<String> hetDimermapKeys = allHetDimerPairsObjectsMap.keySet();

        for(String hetDimerObj : hetDimermapKeys){
            String hetDimerOligoid = hetDimerObj.split("_", -1)[0];
            String chr = hetDimerOligoid.split(":", -1)[0].replaceAll("inpSeq", "");
            int sostart = Integer.parseInt(hetDimerOligoid.split(":", -1)[1]);
            int soend = Integer.parseInt(hetDimerOligoid.split(":", -1)[2]);

            ArrayList<OligoObject> hetDimerObjectsInteractedForThisregion = new ArrayList<OligoObject>();

            for(OligoObject obj : allHetDimerPairsObjectsMap.get(hetDimerObj)){
                String objchr = obj.getInternalPrimerId().split("_", -1)[0].split(":", -1)[0].replaceAll("inpSeq", "");
                int objsostart = Integer.parseInt(obj.getInternalPrimerId().split("_", -1)[0].split(":", -1)[1]);
                int objsoend = Integer.parseInt(obj.getInternalPrimerId().split("_", -1)[0].split(":", -1)[2]);

                if( so.getChr().equalsIgnoreCase(objchr) && objsostart==so.getStart() && objsoend==so.getStop() ){
                    hetDimerObjectsInteractedForThisregion.add(obj);
                }
            }
            List<OligoObject> hetDimerObjectsInteractedForThisregionList = hetDimerObjectsInteractedForThisregion;

            if( so.getChr().equalsIgnoreCase(chr) && sostart==so.getStart() && soend==so.getStop() ){
                //hetDimersForGivenRegion.put(hetDimerObj, allHetDimerPairsObjectsMap.get(hetDimerObj));
                hetDimersForGivenRegion.put(hetDimerObj, hetDimerObjectsInteractedForThisregionList);
            }
        }

        return hetDimersForGivenRegion;
    }

    /**
     *
     * @param heteroDimerObjectsMap
     * @param heterodimerInpDir
     * @param dataDir
     * @param hetdimerFilename
     * @param serialNum
     * @param numlinesInFile
     * @param oligoIdStoppedAt
     * @param oligoIdIndexInArrayOfMapValuesArray
     * @return
     * @throws Exception
     */
    public String createSubsetRunHeterodimerAnalysis(LinkedHashMap<OligoObject, List<OligoObject>> heteroDimerObjectsMap, String heterodimerInpDir, String dataDir, String hetdimerFilename, int serialNum, int numlinesInFile, int oligoIdStoppedAt, int oligoIdIndexInArrayOfMapValuesArray ) throws Exception {

        String file1 = hetdimerFilename+"_"+serialNum+"_1";
        String file2 = hetdimerFilename+"_"+serialNum+"_2";
        File hetInpFile1 = new File(dataDir+heterodimerInpDir+file1);
        File hetInpFile2 = new File(dataDir+heterodimerInpDir+file2);
        PrintWriter pw1 = new PrintWriter(hetInpFile1);
        PrintWriter pw2 = new PrintWriter(hetInpFile2);

        Set<OligoObject> oligoIdsSet = heteroDimerObjectsMap.keySet();
        ArrayList<OligoObject> oligoIdsArray = new ArrayList<OligoObject>();
        oligoIdsArray.addAll(oligoIdsSet);

        int counter=0;

        //making allhetdimerhashmap ordered instead of two way.. two way is unnecessary and hugely memory dependent.
        for(int olig=oligoIdStoppedAt; olig<oligoIdsArray.size(); olig++){

            OligoObject oligoObj = oligoIdsArray.get(olig);

            List<OligoObject> oligoArrays = heteroDimerObjectsMap.get(oligoObj);

            //int i=0;
            int i=olig+1;
            if(olig==oligoIdStoppedAt){
                i=oligoIdIndexInArrayOfMapValuesArray;
            }

            for( ;i<oligoArrays.size(); i++){

                OligoObject o = oligoArrays.get(i);

                //make sure you're not comparing the same oligoObject with the same oligoObject.
                if(!o.getInternalPrimerId().equals(oligoObj.getInternalPrimerId())){
                    if(counter>numlinesInFile){
                        pw1.close();
                        pw2.close();

                        String resultHeterodimerString = runHeterodimerAnalysisProcessBuilder(file1, file2, dataDir);
                        return olig+"&"+i;
                    }

                    pw1.println(">"+oligoObj.getInternalPrimerId());
                    pw1.println(oligoObj.getInternalSeq());
                    pw2.println(">"+o.getInternalPrimerId());
                    pw2.println(o.getInternalSeq());
                    System.out.println(oligoObj.getInternalPrimerId()+"\t"+o.getInternalPrimerId());
                    counter+=1;
                }
            }
        }

        System.out.println("last oligoId is:"+ oligoIdsArray.get(oligoIdsArray.size()-1).getInternalPrimerId() + " and second last oligoId is: "+ oligoIdsArray.get(oligoIdsArray.size()-2).getInternalPrimerId());

        if(counter <= numlinesInFile){
            //supposedly last set of the file will have this..
            pw1.close();
            pw2.close();
            String resultHeterodimerString = runHeterodimerAnalysisProcessBuilder(file1, file2, dataDir);
            //System.out.println(resultHeterodimerString);
        }
        return oligoIdStoppedAt+"&"+oligoIdIndexInArrayOfMapValuesArray;
    }

    /**
     *
     * @param oligoObjectsMap
     * @return
     */
    public ArrayList<String[]> createSubsetofhetDimers(LinkedHashMap<OligoObject, List<OligoObject>> oligoObjectsMap) {

        Set<OligoObject> oligoIdsSet = oligoObjectsMap.keySet();
        ArrayList<OligoObject> oligoIdsArray = new ArrayList<OligoObject>();
        oligoIdsArray.addAll(oligoIdsSet);

        ArrayList<String[]> inputlinesArr = new ArrayList<String[]>();

        for(int o=0;o<oligoIdsArray.size(); o++){
            OligoObject oligo = oligoIdsArray.get(o);

            for(int p=o+1; p<oligoIdsArray.size(); p++){
                OligoObject secondfileOLigo = oligoIdsArray.get(p);
                String p1 = ">"+oligo.getInternalPrimerId()+"\n"+ oligo.getInternalSeq()+"\n";
                String p2 = ">"+secondfileOLigo.getInternalPrimerId()+"\n"+secondfileOLigo.getInternalSeq()+"\n";
                String inputArr[]  = new String[2];
                inputArr[0] = p1;
                inputArr[1] = p2;

                inputlinesArr.add(inputArr);
            }
        }

        return inputlinesArr;
    }


    /***
     *
     * @param inputlistforHetDimerAnalysis
     * @param heterodimerInpDir
     * @param dataDir
     * @param hetdimerFilename
     * @param serialNum
     * @param oligoidStoppedAt
     * @param numlines
     * @return
     * @throws Exception
     */
    public String createFileRunHeterodimerAnalysis(ArrayList<String[]> inputlistforHetDimerAnalysis, String heterodimerInpDir, String dataDir, String hetdimerFilename, int serialNum, int oligoidStoppedAt, int numlines) throws Exception {

        String file1 = hetdimerFilename+"_"+serialNum+"_1";
        String file2 = hetdimerFilename+"_"+serialNum+"_2";
        File hetInpFile1 = new File(dataDir+heterodimerInpDir+file1);
        File hetInpFile2 = new File(dataDir+heterodimerInpDir+file2);
        PrintWriter pw1 = new PrintWriter(hetInpFile1);
        PrintWriter pw2 = new PrintWriter(hetInpFile2);

        if(inputlistforHetDimerAnalysis.size()-oligoidStoppedAt<=numlines){
            //numlines = inputlistforHetDimerAnalysis.size()-oligoidStoppedAt;
            numlines = inputlistforHetDimerAnalysis.size();
        }else{
            numlines = oligoidStoppedAt+numlines;
        }

        int counter = oligoidStoppedAt;
        for(int i=oligoidStoppedAt; i<numlines; i++){
            String[] inputarr = inputlistforHetDimerAnalysis.get(i);

            String fileLine1 = inputarr[0];
            String fileLine2 = inputarr[1];

            pw1.print(fileLine1);
            pw2.print(fileLine2);

            counter += 1;
        }

        pw1.close();
        pw2.close();

        String resultHeterodimerString = runHeterodimerAnalysisProcessBuilder(file1, file2, dataDir);

        return counter+"&"+resultHeterodimerString;
    }

    /***
     *
     * @param inputlistforHetDimerAnalysis
     * @param heterodimerInpDir
     * @param dataDir
     * @param hetdimerFilename
     * @param serialNum
     * @param oligoidStoppedAt
     * @param numlines
     * @return
     * @throws Exception
     */
    public int createFileForHeterodimerAnalysis(ArrayList<String[]> inputlistforHetDimerAnalysis, String heterodimerInpDir, String dataDir, String hetdimerFilename, int serialNum, int oligoidStoppedAt, int numlines) throws Exception {

        String file1 = hetdimerFilename+"_"+serialNum+"_1";
        String file2 = hetdimerFilename+"_"+serialNum+"_2";
        File hetInpFile1 = new File(dataDir+heterodimerInpDir+file1);
        File hetInpFile2 = new File(dataDir+heterodimerInpDir+file2);
        PrintWriter pw1 = new PrintWriter(hetInpFile1);
        PrintWriter pw2 = new PrintWriter(hetInpFile2);

        if(inputlistforHetDimerAnalysis.size()-oligoidStoppedAt<=numlines){
            //numlines = inputlistforHetDimerAnalysis.size()-oligoidStoppedAt;
            numlines = inputlistforHetDimerAnalysis.size();
        }else{
            numlines = oligoidStoppedAt+numlines;
        }

        int counter = oligoidStoppedAt;
        for(int i=oligoidStoppedAt; i<numlines; i++){
            String[] inputarr = inputlistforHetDimerAnalysis.get(i);

            String fileLine1 = inputarr[0];
            String fileLine2 = inputarr[1];

            pw1.print(fileLine1);
            pw2.print(fileLine2);

            counter += 1;
        }

        pw1.close();
        pw2.close();


        return counter;
    }



    /**
     *
     * @param allHetDimerPairsObjectsMapMapdb
     * @param dataDir
     * @param heterodimerOpDir
     * @param fileName
     * @param subpartnum
     * @return
     * @throws Exception
     */
    public HTreeMap<String, Float> getDeltaGValuesForHetDimerPairs_createMapDBHash(HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb, String dataDir, String heterodimerOpDir, String fileName, int subpartnum) throws Exception{

        String hetOpFilename = dataDir+heterodimerOpDir+fileName+"_"+subpartnum+"_1_"+fileName+"_"+subpartnum+"_2.out";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(hetOpFilename)));

        try{

            String line;
            while((line=reader.readLine()) != null){
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String[] oligoHeaderArr = lineArr[3].split("-", -1);
                    String hetOligoHeader1 = oligoHeaderArr[0];
                    String hetOligoHeader2 = oligoHeaderArr[1];
                    String hetdimerValue = lineArr[1].split(" = ", -1)[1];

                    Float hetDimerValueToCompare = Float.parseFloat(hetdimerValue);
                    Float hetDimerValueToCompareTo = Float.parseFloat("-10.00");

                    int comparison = hetDimerValueToCompare.compareTo(hetDimerValueToCompareTo);
                    if (comparison < 0) {
                        System.out.println("f1 is less than f2");
                    }
                    else if (comparison == 0) {
                        System.out.println("f1 is equal to f2");
                    }
                    else {
                        System.out.println("f1 is greater than f2");
                    }

                    if(Float.parseFloat(hetdimerValue) >= -10.00){
                        allHetDimerPairsObjectsMapMapdb.put(hetOligoHeader1 + "&" + hetOligoHeader2, Float.parseFloat(hetdimerValue));
                    }
                }
            }
        }finally {
            reader.close();
        }

        return allHetDimerPairsObjectsMapMapdb;
    }


    /**
     *
     * @param allHetDimerPairsObjectsMapMapdb
     * @param so
     * @param hetDimerHashMapMAPDB
     * @param hetDimerMapForSO_mapDB
     * @return
     * @throws Exception
     */
    public HTreeMap<String, OligoObject> getHetDimersForRegion_mapDB(HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb, SequenceObject so, Map<String, OligoObject> hetDimerHashMapMAPDB, HTreeMap<String, OligoObject> hetDimerMapForSO_mapDB) throws Exception{

        for(String htreeKey : allHetDimerPairsObjectsMapMapdb.getKeys()){
            String[] hetdimerids = htreeKey.split("&", -1);
            OligoObject hetDimerOligoObj1 = hetDimerHashMapMAPDB.get(hetdimerids[0]);
            int sostart1 = Integer.parseInt(hetdimerids[0].split(":", -1)[1]);
            int soend1 = Integer.parseInt(hetdimerids[0].split(":", -1)[2]);
            OligoObject hetDimerOligoObj2 = hetDimerHashMapMAPDB.get(hetdimerids[1]);
            int sostart2 = Integer.parseInt(hetdimerids[1].split(":", -1)[1]);
            int soend2 = Integer.parseInt(hetdimerids[1].split(":", -1)[2]);


            if(hetDimerOligoObj1.getChr().equalsIgnoreCase(so.getChr()) && hetDimerOligoObj2.getChr().equalsIgnoreCase(so.getChr())){
                if( sostart1==so.getStart() && soend1==so.getStop() && sostart2==so.getStart() && soend2==so.getStop()){
                    hetDimerMapForSO_mapDB.put(hetDimerOligoObj1.getInternalPrimerId(), hetDimerOligoObj1);
                    hetDimerMapForSO_mapDB.put(hetDimerOligoObj2.getInternalPrimerId(), hetDimerOligoObj2);
                }
            }
        }

        return hetDimerMapForSO_mapDB;
    }


    /**
     *
     *
     * @param allHetDimerPairsObjectsMapMapdb
     * @param so
     * @param hetDimerHashMapMAPDB
     * @return
     * @throws Exception
     */
    public ArrayList<String> getHetDimersIdsForRegion(HTreeMap<String, Float> allHetDimerPairsObjectsMapMapdb, SequenceObject so, HTreeMap<String, Object> hetDimerHashMapMAPDB) throws Exception{

        ArrayList<String> hetDimersList = new ArrayList<String>();

        for(String htreeKey : allHetDimerPairsObjectsMapMapdb.getKeys()){
            String[] hetdimerids = htreeKey.split("&", -1);
            OligoObject hetDimerOligoObj1 = (OligoObject) hetDimerHashMapMAPDB.get(hetdimerids[0]);
            int sostart1 = Integer.parseInt(hetdimerids[0].split("_", -1)[0].split(":", -1)[1]);
            int soend1 = Integer.parseInt(hetdimerids[0].split("_", -1)[0].split(":", -1)[2]);
            OligoObject hetDimerOligoObj2 = (OligoObject) hetDimerHashMapMAPDB.get(hetdimerids[1]);
            int sostart2 = Integer.parseInt(hetdimerids[1].split("_", -1)[0].split(":", -1)[1]);
            int soend2 = Integer.parseInt(hetdimerids[1].split("_", -1)[0].split(":", -1)[2]);


            if(hetDimerOligoObj1.getChr().equalsIgnoreCase(so.getChr()) && hetDimerOligoObj2.getChr().equalsIgnoreCase(so.getChr())){
                if( sostart1==so.getStart() && soend1==so.getStop() && sostart2==so.getStart() && soend2==so.getStop()){
                    hetDimersList.add(hetDimerOligoObj1.getInternalPrimerId());
                    hetDimersList.add(hetDimerOligoObj2.getInternalPrimerId());
                }
            }
        }

        Set<String> hetdimersIdSet = new HashSet<String>(hetDimersList);
        hetDimersList.clear();

        hetDimersList = new ArrayList<String>(hetdimersIdSet);
        return hetDimersList;
    }


    /**
     *
     *
     * @param hetDimerIdListForSO
     * @param spacing
     * @param hetDimerMapForSO_mapDB
     * @param filteredHetDimerMapForSO_multimap
     * @return
     */
    public Multimap<String, String> filterMapCreateOnlyHetsWithinDistanceMap_MapDB(ArrayList<String> hetDimerIdListForSO, int spacing, HTreeMap<String, Object> hetDimerMapForSO_mapDB, Multimap<String, String> filteredHetDimerMapForSO_multimap) throws Exception {

        for(String oligoid : hetDimerIdListForSO){
            List<String> nextBinOligosWithinSpacing = getNext8_10KBOligoObjs_mapDB(hetDimerMapForSO_mapDB.get(oligoid), hetDimerIdListForSO, hetDimerMapForSO_mapDB, spacing, hetDimerIdListForSO.indexOf(oligoid));
            if(nextBinOligosWithinSpacing.size()>0){
                for(String nextoligo : nextBinOligosWithinSpacing){
                    //filteredHetDimerMapForSO_multimap.add(new Object[]{oligoid,nextoligo});
                    filteredHetDimerMapForSO_multimap.put(oligoid, nextoligo);
                }
            }
        }

        System.out.println("returning filtered hets sorted by distance");
        return filteredHetDimerMapForSO_multimap;
    }


    /**
     *
     *
     * @param objInQuestion
     * @param hetDimerIdListForSO
     * @param hetDimerMapForSO_mapDB
     * @param spacing
     * @param i   @return
     * */
    public List<String> getNext8_10KBOligoObjs_mapDB(Object objInQuestion, ArrayList<String> hetDimerIdListForSO, HTreeMap<String, Object> hetDimerMapForSO_mapDB, int spacing, int i) throws Exception{

        OligoObject oligoObjectinQuestion = (OligoObject) objInQuestion;
        int diffLessThan0 = (spacing*1000)+500;
        int diffGreaterThan0 = (spacing*1000)-500;

        int diffLessThan1 = (spacing*1000)+2000;
        int diffGreaterThan1 = (spacing*1000)-2000;

        int diffLessThan2 = (spacing*1000)+4000;
        int diffGreaterThan2 = (spacing*1000)-4000;

        int diffLessThan3 = (spacing*1000)+6000;
        int diffGreaterThan3 = (spacing*1000)-6000;

        ArrayList<String> oligosReturned = new ArrayList<String>();

        for(String oligoid : hetDimerIdListForSO){
            OligoObject o = (OligoObject) hetDimerMapForSO_mapDB.get(oligoid);
            int oligoStartDiff = o.getInternalStart()-oligoObjectinQuestion.getInternalStart();
            if(diffLessThan1>oligoStartDiff && oligoStartDiff>diffGreaterThan1){
                //check if obj is present in hetDimerMapForSO.
                oligosReturned.add(oligoid);
            }
        }

        oligosReturned = new OligoUtils().sortOligoIdListBySubsectionAndSerialNum(oligosReturned);
        Collections.shuffle(oligosReturned);
        return oligosReturned;

    }


    public String getDimer1Id() {
        return dimer1Id;
    }

    public void setDimer1Id(String dimer1Id) {
        this.dimer1Id = dimer1Id;
    }

    public String getDimer2Id() {
        return dimer2Id;
    }

    public void setDimer2Id(String dimer2Id) {
        this.dimer2Id = dimer2Id;
    }

    public Float getDimerDeltaG() {
        return dimerDeltaG;
    }

    public void setDimerDeltaG(Float dimerDeltaG) {
        this.dimerDeltaG = dimerDeltaG;
    }
}