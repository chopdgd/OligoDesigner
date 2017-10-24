package edu.chop.dgd.dgdUtils;

import edu.chop.dgd.dgdObjects.OligoObject;
import edu.chop.dgd.dgdObjects.SequenceObjectSubsections;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jayaramanp on 10/16/17.
 */
public class OligoUtils {

    /**
     *
     * @param line
     * @param tag
     * @param pr
     * @return
     * @throws Exception
     */
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


    /**
     *
     * @param inputFileName
     * @param folder
     * @param oligoOutputDir
     * @return
     * @throws Exception
     */
    public List<OligoObject> createOligoObjsList(String inputFileName, String folder, String oligoOutputDir) throws Exception{
        List<OligoObject> primer3Objects = new ArrayList<OligoObject>();
        String fileName=inputFileName;
        File primerInputFile = new File(folder+oligoOutputDir+fileName);
        InputStream fileStream = new FileInputStream(primerInputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

        String line;

        while((line = reader.readLine())!=null){
            if(line.contains("INTERNAL_OLIGO")){
                //prObj = createOligoObject(line, "internal", prObj);
                String delims = "[  ]+";
                String primerStr[] = line.split("INTERNAL", -1);
                String prObjarr[] = primerStr[1].split(delims, -1);
                String start = prObjarr[1];
                String len = prObjarr[2];
                String tm = prObjarr[3];
                String gc = prObjarr[4];
                String any = prObjarr[5];
                String dash3 = prObjarr[6];
                String seq = prObjarr[7];
                String internalPrimerId = "";
                String chr = "";
                String assembly = "";
                Float homodimerValue = Float.parseFloat("0.00");
                Float hairpinValue= Float.parseFloat("0.00");

                OligoObject prObj = new OligoObject(chr, assembly, internalPrimerId, seq, start, Integer.parseInt(len), dash3, tm, any, gc, hairpinValue, homodimerValue);
                primer3Objects.add(prObj);
            }

        }

        return primer3Objects;
    }


    /**
     *
     * @param inputFileName
     * @param oligoObjects
     * @param blatInpDir
     * @param dataDir
     * @param ossSublist
     * @return
     * @throws Exception
     */
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
                                prObj.setChr(String.valueOf(oss.getSubSectionChr()));
                                prObj.setAssembly(oss.getSubSectionAssembly());

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


    /**
     *
     * @param oligoList
     * @return
     */
    public List<OligoObject> sortOligosBySubsectionAndSerialNum(List<OligoObject> oligoList) {

        Collections.sort(oligoList, new Comparator<OligoObject>() {
            @Override
            public int compare(OligoObject o1, OligoObject o2) {
                //return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1])));
                int compChr = (Integer.valueOf(o1.getChr().split("chr", -1)[1]).compareTo(Integer.valueOf(o2.getChr().split("chr", -1)[1])));

                if(compChr==0){
                    int compSeqObjStart = (Integer.valueOf(o1.getInternalPrimerId().split("_", -1)[0].split(":",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_", -1)[0].split(":",-1)[1])));

                    if(compSeqObjStart == 0){
                        int compSeqEnd = (Integer.valueOf(o1.getInternalPrimerId().split("_", -1)[0].split(":",-1)[2]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_", -1)[0].split(":",-1)[2])));

                        if(compSeqEnd == 0){
                            int compSubsect = (Integer.valueOf(o1.getInternalPrimerId().split("_",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_",-1)[1])));

                            if(compSubsect ==0){
                                return (Integer.valueOf(o1.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_",-1)[2].split("O",-1)[1])));

                            }else{
                                return compSubsect;
                            }
                        }else{
                            return compSeqEnd;
                        }
                    }else {
                        return compSeqObjStart;
                    }
                }else{
                    return compChr;
                }
            }
        });

        return oligoList;
    }


    /**
     *
     * @param oligoIdsList
     * @return
     */
    public ArrayList<String> sortOligoIdListBySubsectionAndSerialNum(ArrayList<String> oligoIdsList) {


        Collections.sort(oligoIdsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                //return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1])));
                int compChr = (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1])));

                if(compChr==0){
                    int compSeqObjStart = (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[1])));

                    if(compSeqObjStart == 0){
                        int compSeqEnd = (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[2]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[2])));

                        if(compSeqEnd == 0){
                            int compSubsect = (Integer.valueOf(o1.split("_",-1)[1]).compareTo(Integer.valueOf(o2.split("_",-1)[1])));

                            if(compSubsect ==0){
                                return (Integer.valueOf(o1.split("_",-1)[2].split("O",-1)[1]).compareTo(Integer.valueOf(o2.split("_",-1)[2].split("O",-1)[1])));

                            }else{
                                return compSubsect;
                            }
                        }else{
                            return compSeqEnd;
                        }
                    }else {
                        return compSeqObjStart;
                    }
                }else{
                    return compChr;
                }
            }
        });




        /*Collections.sort(oligoIdsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[0].split("chr", -1)[1])));
            }
        });

        Collections.sort(oligoIdsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[1])));
            }
        });

        Collections.sort(oligoIdsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[2]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[2])));
            }
        });

        Collections.sort(oligoIdsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (Integer.valueOf(o1.split("_", -1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[1])));
            }
        });

        Collections.sort(oligoIdsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (Integer.valueOf(o1.split("_", -1)[2].split("O",-1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[2].split("O",-1)[1])));
            }
        });
*/
        return oligoIdsList;
    }



    public void compareOligos(List<OligoObject> oligoList){
        Collections.sort(oligoList, new Comparator<OligoObject>() {
            @Override
            public int compare(OligoObject o1, OligoObject o2) {
                //return (Integer.valueOf(o1.split("_", -1)[0].split(":",-1)[1]).compareTo(Integer.valueOf(o2.split("_", -1)[0].split(":",-1)[1])));
                return (Integer.valueOf(o1.getInternalPrimerId().split("_", -1)[0].split(":",-1)[1]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_", -1)[0].split(":",-1)[1])));
            }
        });

        Collections.sort(oligoList, new Comparator<OligoObject>() {
            @Override
            public int compare(OligoObject o1, OligoObject o2) {
                return (Integer.valueOf(o1.getInternalPrimerId().split("_", -1)[0].split(":",-1)[2]).compareTo(Integer.valueOf(o2.getInternalPrimerId().split("_", -1)[0].split(":",-1)[2])));
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

    }
}
