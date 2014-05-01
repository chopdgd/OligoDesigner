package edu.chop.dgd.utils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by jayaramanp on 3/10/14.
 */
public class InsilicoPCRObject {
    String leftPrimer;
    String rightPrimer;
    String chr;
    int primerSeqStart;
    int primerSeqEnd;
    String strand;
    String size;
    String seq;
    String isPcrId;


    public List<Primer3Object> addInsilicoResultsToPrimers(String inputFileName, String isPcrOpDir, List<Primer3Object> primer3Objects, String dataDir) throws Exception {

        List<InsilicoPCRObject> isPcrObjects = createInsilicoPrimers(inputFileName, isPcrOpDir, dataDir);

        for(Primer3Object prObj : primer3Objects){
            List<InsilicoPCRObject> isPcrObjList = new ArrayList<InsilicoPCRObject>();
            for(InsilicoPCRObject isPcrObj : isPcrObjects){
                String primerIds = prObj.getLeftPrimerId()+"_"+prObj.getRightPrimerId();
                if(isPcrObj.getIsPcrId().equals(primerIds)){
                    isPcrObjList.add(isPcrObj);
                }
            }
            prObj.setInsilicoPCRObjectList(isPcrObjList);
        }

        return primer3Objects;
    }



    public List<InsilicoPCRObject> createInsilicoPrimers(String inputFileName, String isPcrOpDir, String dataDir) throws Exception {

        List<InsilicoPCRObject> isPcrObjectsList = new ArrayList<InsilicoPCRObject>();
        FileReader f = new FileReader(dataDir+isPcrOpDir+inputFileName);
        Scanner s = new Scanner(f);
        InsilicoPCRObject isPcrObj = new InsilicoPCRObject();
        String isPcrSeq = "";

        while(s.hasNextLine()){
            String line = s.nextLine();
            if(line.startsWith(">")){

                if(isPcrSeq.length()>0){
                    isPcrObj.setSeq(isPcrSeq);
                    isPcrObjectsList.add(isPcrObj);
                    isPcrObj = new InsilicoPCRObject();
                }

                String delims = "[ ]+";
                String isPcrObjLine[] = line.split(delims, -1);
                String queryDelims = "[>:+ -]+";
                String query[] = isPcrObjLine[0].split(queryDelims, -1);
                String chr = query[1];
                String primerStart = query[2];
                String primerEnd = query[3];
                //String primerStrand = query[3];
                String insilicoPrimerIds = isPcrObjLine[1];
                String primerSize = isPcrObjLine[2];
                String forwardPrimer = isPcrObjLine[3];
                String reversePrimer = isPcrObjLine[4];

                isPcrObj.setChr(chr);
                isPcrObj.setPrimerSeqStart(Integer.parseInt(primerStart));
                isPcrObj.setPrimerSeqEnd(Integer.parseInt(primerEnd));
                //isPcrObj.setStrand(primerStrand);
                isPcrObj.setSize(primerSize);
                isPcrObj.setIsPcrId(insilicoPrimerIds);
                isPcrObj.setLeftPrimer(forwardPrimer);
                isPcrObj.setRightPrimer(reversePrimer);


            }else if(line.matches("[ATGCNatgcn]+")){

                isPcrSeq+=line;

            }
        }
        if(s.hasNextLine()==false){
            if(isPcrSeq.length()>0){
                isPcrObj.setSeq(isPcrSeq);
                isPcrObjectsList.add(isPcrObj);
            }
        }


        return isPcrObjectsList;

    }



    public String getLeftPrimer() {
        return leftPrimer;
    }

    public void setLeftPrimer(String leftPrimer) {
        this.leftPrimer = leftPrimer;
    }

    public String getRightPrimer() {
        return rightPrimer;
    }

    public void setRightPrimer(String rightPrimer) {
        this.rightPrimer = rightPrimer;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public int getPrimerSeqStart() {
        return primerSeqStart;
    }

    public void setPrimerSeqStart(int primerSeqStart) {
        this.primerSeqStart = primerSeqStart;
    }

    public int getPrimerSeqEnd() {
        return primerSeqEnd;
    }

    public void setPrimerSeqEnd(int primerSeqEnd) {
        this.primerSeqEnd = primerSeqEnd;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getIsPcrId() {
        return isPcrId;
    }

    public void setIsPcrId(String isPcrId) {
        this.isPcrId = isPcrId;
    }


}
