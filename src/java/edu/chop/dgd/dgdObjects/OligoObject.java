package edu.chop.dgd.dgdObjects;

import java.io.*;
import java.util.*;

/**
 * Created by jayaramanp on 9/26/14.
 */

public class OligoObject extends Primer3Object{

    float hairpinValue;
    float homodimerValue;
    LinkedHashMap<String, Float> heterodimerValues;



    public OligoObject createOligoObject(String line, String tag, OligoObject pr) throws Exception{
        String delims = "[  ]+";
        String primerStr[] = line.split("INTERNAL", -1);
        String prObj[] = primerStr[1].split(delims, -1);
        String start = prObj[1];
        String len = prObj[2];
        String tm = prObj[3];
        String gc = prObj[4];
        String any = prObj[5];
        String dash3 = prObj[6];
        String seq = prObj[7];

        System.out.println(prObj);

        if(tag.equals("internal")){
            pr.setInternalSeq(seq);
            pr.setInternal3(dash3);
            pr.setInternalAny(any);
            pr.setInternalGc(gc);
            pr.setInternalLen(Integer.parseInt(len));
            pr.setInternalTm(tm);
            pr.setInternalStart(start);

        }

        return pr;
    }



    public List<OligoObject> createOligoObjsList(String inputFileName, String folder, String oligoOutputDir) throws Exception{
        List<OligoObject> primer3Objects = new ArrayList<OligoObject>();
        String fileName=inputFileName;
        File primerInputFile = new File(folder+oligoOutputDir+fileName);
        InputStream fileStream = new FileInputStream(primerInputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

        String line;
        OligoObject prObj = new OligoObject();

        while((line = reader.readLine())!=null){
            if(line.contains("INTERNAL_OLIGO")){
                prObj = createOligoObject(line, "internal", prObj);
                //prObj = createPrimer3Object(line, "right", prObj);
            }

            if(prObj.getInternalSeq()!=null && prObj.getInternalSeq().length()>0){
                primer3Objects.add(prObj);
                //after adding primer Obj to array, create new instance of object for next primer.
                prObj = new OligoObject();
            }
        }

        return primer3Objects;
    }



    public List<OligoObject> addIdsToOligos(String inputFileName, List<OligoObject> oligoObjects, String blatInpDir,
                                              String dataDir, List<SequenceObjectSubsections> ossSublist) throws Exception {

        List<OligoObject> newOligoObjects = new ArrayList<OligoObject>();
        String fileName=inputFileName;
        File blatInputFile = new File(dataDir+blatInpDir+fileName);
        InputStream fileStream = new FileInputStream(blatInputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

        String line; String primerId="NA"; String primerSubsectionId="NA"; String counter = "0";
        int i=1;
        while((line = reader.readLine())!=null){
            if(line.startsWith(">")){
                primerId=line.replaceAll(">","");
                counter = primerId.split("_", -1)[2];
                primerSubsectionId = primerId.split("_", -1)[0]+"_"+primerId.split("_", -1)[1];
            }else{
                String seq = line;

                for(SequenceObjectSubsections oss : ossSublist){

                    if(oss.getSubsectionid().equals(primerSubsectionId)){

                        for(; i<=oligoObjects.size();){

                            OligoObject prObj = oligoObjects.get(i-1);
                            if(counter.equals("O"+i)){

                                prObj.setInternalPrimerId(primerId);
                                prObj.setInternalStart(String.valueOf(oss.getSubSectionStart()+Integer.parseInt(prObj.getInternalStart())));

                                primerId="NA";
                                if(!prObj.getInternalPrimerId().equals("NA")){
                                    newOligoObjects.add(prObj);
                                }
                                i+=1;
                                break;

                            }

                        }
                        break;

                    }

                }
            }
        }

        return newOligoObjects;
    }


    public List<OligoObject> sortOligosBySubsectionAndSerialNum(List<OligoObject> oligoList) {

        Collections.sort(oligoList, new Comparator<OligoObject>() {
            @Override
            public int compare(OligoObject o1, OligoObject o2) {
                return (o1.getInternalPrimerId().split("_",-1)[0].compareTo(o2.getInternalPrimerId().split("_",-1)[0]));
            }
        });

        Collections.sort(oligoList, new Comparator<OligoObject>() {
            @Override
            public int compare(OligoObject o1, OligoObject o2) {
                return (Integer.valueOf(o1.getInternalPrimerId().split("_",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_",-1)[1])));
            }
        });

        Collections.sort(oligoList, new Comparator<OligoObject>() {
            @Override
            public int compare(OligoObject o1, OligoObject o2) {
                return (Integer.valueOf(o1.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1])));
            }
        });

        return oligoList;

    }

    public float getHairpinValue() {
        return hairpinValue;
    }

    public void setHairpinValue(float hairpinValue) {
        this.hairpinValue = hairpinValue;
    }

    public float getHomodimerValue() {
        return homodimerValue;
    }

    public void setHomodimerValue(float homodimerValue) {
        this.homodimerValue = homodimerValue;
    }

    public LinkedHashMap<String, Float> getHeterodimerValues() {
        return heterodimerValues;
    }

    public void setHeterodimerValues(LinkedHashMap<String, Float> heterodimerValues) {
        this.heterodimerValues = heterodimerValues;
    }

}
