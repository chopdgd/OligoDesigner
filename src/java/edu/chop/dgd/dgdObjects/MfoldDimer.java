package edu.chop.dgd.dgdObjects;

import java.io.*;
import java.util.*;

/**
 * Created by jayaramanp on 9/25/14.
 */
public class MfoldDimer {
    String dimer1Id;
    String dimer2Id;
    Float dimerDeltaG;

    public List<OligoObject> getDeltaGValuesForHomoDimer(List<OligoObject> oligoObjectsFromPrimer3, String fileName, String homodimerOpDir, String dataDir) throws Exception {

        File mfoldFile = new File(dataDir+homodimerOpDir+"/"+fileName+"_"+fileName+".ct");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mfoldFile)));

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String oligoHeader = lineArr[2];
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



    public HashMap<OligoObject, List<OligoObject>> FilterOligosRetrieveHeteroDimers(List<SequenceObjectSubsections> sequenceSubsectionsList, String fileName, String heterodimerInpDir, String heterodimerOpDir, String dataDir) throws Exception {

        List<OligoObject> heteroDimerObjectsList = filterOligosCreateHeterodimers(sequenceSubsectionsList, dataDir, fileName);
        HashMap<OligoObject, List<OligoObject>> oligoObjectsMap = mapOligosRunHeterodimerAnalysis(heteroDimerObjectsList, heterodimerInpDir, dataDir, fileName);
        oligoObjectsMap = getDeltaGValuesForHetDimerPairs(oligoObjectsMap, dataDir, heterodimerOpDir, fileName);
        oligoObjectsMap = filterMapCreateOnlyViablehetsMap(oligoObjectsMap);
        return oligoObjectsMap;
    }

    private HashMap<OligoObject, List<OligoObject>> filterMapCreateOnlyViablehetsMap(HashMap<OligoObject, List<OligoObject>> oligoObjectsMap) {


        for(OligoObject oligoObj : oligoObjectsMap.keySet()){

            HashMap<String, Float> hetScoreOligomap = oligoObj.getHeterodimerValues();

            //trying to optimize code 23rd Oct 254pm.
            //HashMap<String, Float> hetScoreOligomap_new = hetScoreOligomap;
            HashMap<String, Float> hetScoreOligomap_new = new HashMap<String, Float>();

            for(String oligoId : hetScoreOligomap.keySet()){

                //trying to optimize code. 23rd oct 254pm
                /*if(hetScoreOligomap.get(oligoId)<-15.00){
                    hetScoreOligomap_new.remove(oligoId);
                }*/

                if(hetScoreOligomap.get(oligoId)>=-15.00){
                    hetScoreOligomap_new.put(oligoId, hetScoreOligomap.get(oligoId));
                }
            }

            oligoObj.setHeterodimerValues(hetScoreOligomap_new);
            oligoObjectsMap.put(oligoObj, oligoObjectsMap.get(oligoObj));

        }

        return oligoObjectsMap;

    }


    private HashMap<OligoObject, List<OligoObject>> getDeltaGValuesForHetDimerPairs(HashMap<OligoObject, List<OligoObject>> oligoObjectsMap, String dataDir, String heterodimerOpDir, String fileName) throws Exception {

        String hetOpFilename = dataDir+heterodimerOpDir+fileName+"_1_"+fileName+"_2.out";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(hetOpFilename)));
        Set<OligoObject> hashMapKeys = oligoObjectsMap.keySet();
        //HashMap<String, Float> hetDimerDeltaGValuesMap = new HashMap<String, Float>();

        try{
            String line;
            while((line=reader.readLine()) != null){
                if(line.contains("dG")){
                    String lineArr[] = line.split("\t", -1);
                    String[] oligoHeaderArr = lineArr[3].split("-", -1);
                    String hetOligoHeader1 = oligoHeaderArr[0];
                    String hetOligoHeader2 = oligoHeaderArr[1];

                    for(OligoObject o:hashMapKeys){
                        if(o.getInternalPrimerId().equals(hetOligoHeader1)){
                            List<OligoObject> hetObjects = oligoObjectsMap.get(o);
                            HashMap<String, Float> hetDimerDeltaGValuesMap = o.getHeterodimerValues();

                            for(OligoObject oObj : hetObjects){
                                if(oObj.getInternalPrimerId().equals(hetOligoHeader2)){
                                    String hetdimerValue = lineArr[1].split(" = ", -1)[1];
                                    if(hetDimerDeltaGValuesMap!=null){
                                        hetDimerDeltaGValuesMap.put(hetOligoHeader2, Float.parseFloat(hetdimerValue));
                                    }else{
                                        hetDimerDeltaGValuesMap = new HashMap<String, Float>();
                                        hetDimerDeltaGValuesMap.put(hetOligoHeader2, Float.parseFloat(hetdimerValue));
                                    }
                                }
                            }

                            o.setHeterodimerValues(hetDimerDeltaGValuesMap);
                            oligoObjectsMap.put(o, oligoObjectsMap.get(o));
                        }
                    }
                }
            }

        }finally {

            reader.close();

        }

        return oligoObjectsMap;
    }


    private HashMap<OligoObject, List<OligoObject>> mapOligosRunHeterodimerAnalysis(List<OligoObject> heteroDimerObjectsList,
                                                                                    String heterodimerInpDir, String dataDir, String fileName) throws Exception {
        String file1 = fileName+"_1";
        String file2 = fileName+"_2";
        File hetInpFile1 = new File(dataDir+heterodimerInpDir+file1);
        File hetInpFile2 = new File(dataDir+heterodimerInpDir+file2);
        PrintWriter pw1 = new PrintWriter(hetInpFile1);
        PrintWriter pw2 = new PrintWriter(hetInpFile2);

        HashMap<OligoObject, List<OligoObject>> hetDimerObjMap = new HashMap<OligoObject, List<OligoObject>>();

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

        return hetDimerObjMap;
    }




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




    private List<OligoObject> filterOligosCreateHeterodimers(List<SequenceObjectSubsections> sequenceObjectSubsectionsList,
                                                             String dataDir, String fileName) {

        List<OligoObject> hetDimerInputs = new ArrayList<OligoObject>();
        for(SequenceObjectSubsections s : sequenceObjectSubsectionsList){
            List<OligoObject> oligoList = s.getOligoList();
            for(OligoObject o : oligoList){
                if((o.getInternalPrimerBlatList()!=null) && (o.getInternalPrimerBlatList().size()==1) &&
                        (Double.compare(o.getInternalPrimerBlatList().get(0).getPercentageIdentity(), Double.parseDouble("99.00"))>=0)){
                    if(o.getHairpinValue()>-2.00 && o.getHomodimerValue()>-10.00){
                        hetDimerInputs.add(o);
                    }
                }
            }
        }


        return hetDimerInputs;

    }



    public HashMap<String,List<OligoObject>> createMapSetsOfHets(HashMap<OligoObject, List<OligoObject>> heterodimerOligosHashMap,
                                                                 SequenceObject sObj, int spacingKB) throws Exception {

        HashMap<String, List<OligoObject>> hashSetOfHetPrimers = new HashMap<String, List<OligoObject>>();

        Set<OligoObject> oligoKeys = heterodimerOligosHashMap.keySet();
        List<OligoObject> oligoKeysList = new ArrayList<OligoObject>();
        for(OligoObject o : oligoKeys){
            oligoKeysList.add(o);
        }

        oligoKeysList = new OligoObject().sortOligosBySubsectionAndSerialNum(oligoKeysList);

        int counter=1;

        hashSetOfHetPrimers = getHashMapOfhetSets(oligoKeysList, oligoKeysList.get(0), hashSetOfHetPrimers, sObj, counter, spacingKB);

        return hashSetOfHetPrimers;
    }



    public HashMap<String, List<OligoObject>> getHashMapOfhetSets(List<OligoObject> oligoKeysList,
                        OligoObject startingOligoHetObject, HashMap<String, List<OligoObject>> hashSetOfHetPrimers,
                        SequenceObject sObj, int counter, int spacingKB) throws Exception {

        String set = "set"+counter;

        while(Integer.parseInt(startingOligoHetObject.getInternalStart())-sObj.getStart()<=10000){
            int i=oligoKeysList.indexOf(startingOligoHetObject);

            for(; i<oligoKeysList.size(); ){

                if(hashSetOfHetPrimers.size()==0){
                    if(Integer.parseInt(startingOligoHetObject.getInternalStart())-sObj.getStart()>=2000){
                        List<OligoObject> setOfHets = new ArrayList<OligoObject>();
                        setOfHets.add(oligoKeysList.get(i));
                        hashSetOfHetPrimers.put(set, setOfHets);
                        //i++;
                    }else{
                        i++;
                        startingOligoHetObject = oligoKeysList.get(i);
                    }

                }else if(hashSetOfHetPrimers.size()>0){
                    if(sObj.getStop()-Integer.parseInt(oligoKeysList.get(i).getInternalStart())>=3000){
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
                            OligoObject nullObj = new OligoObject();
                            nullObj.setInternalPrimerId("NULL");
                            setOfhets.add(nullObj);
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

        return hashSetOfHetPrimers;
    }




    public OligoObject getNext8KBOligoObj(OligoObject objInQuestion, List<OligoObject> oligosList, int spacingKB) throws Exception{

        int diffLessThan1 = (spacingKB*1000)+2000;
        int diffGreaterThan1 = (spacingKB*1000)-2000;

        int diffLessThan2 = (spacingKB*1000)+4000;
        int diffGreaterThan2 = (spacingKB*1000)-4000;

        for(OligoObject o : oligosList){
            int oligoStartDiff = Integer.parseInt(o.getInternalStart())-Integer.parseInt(objInQuestion.getInternalStart());
            if(diffLessThan1>oligoStartDiff && oligoStartDiff>diffGreaterThan1){
                return o;
            }
        }

        for(OligoObject o : oligosList){
            int oligoStartDiff = Integer.parseInt(o.getInternalStart())-Integer.parseInt(objInQuestion.getInternalStart());
            if(diffLessThan2>oligoStartDiff && oligoStartDiff>diffGreaterThan2){
                return o;
            }
        }

        return null;
    }


    public TreeMap<String,List<OligoObject>> sortOligosHetSetMinDeltaG(HashMap<String,List<OligoObject>> hetDimerSets) throws Exception{

        TreeMap<String, List<OligoObject>> treeMapSetsWithDeltaG = new TreeMap<String, List<OligoObject>>(new DeltaGComprator());

        for(String set : hetDimerSets.keySet()){

            List<OligoObject> oligosInSet = hetDimerSets.get(set);
            Float deltaGValue = Float.parseFloat("0.00");

            for(int i=0; i<oligosInSet.size(); i++){
                for(int j=i+1; j<oligosInSet.size(); j++){
                    deltaGValue+=oligosInSet.get(i).getHeterodimerValues().get(oligosInSet.get(j).getInternalPrimerId());
                }
            }

            //get avg deltaG Value.
            deltaGValue = deltaGValue/oligosInSet.size();
            set=set+"_"+deltaGValue;
            treeMapSetsWithDeltaG.put(set, oligosInSet);
        }

        return treeMapSetsWithDeltaG;
    }



    private class DeltaGComprator implements Comparator<String>{

        @Override
        public int compare(String e1, String e2) {
            if(Float.parseFloat(e2.split("_")[1])>=Float.parseFloat(e1.split("_")[1])){
                return 1;
            }else{
                return -1;
            }
        }

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
