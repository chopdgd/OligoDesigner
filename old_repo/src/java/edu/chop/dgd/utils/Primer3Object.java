package edu.chop.dgd.utils;

import edu.chop.dgd.process.primerCreate.Variation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jayaramanp on 3/10/14.
 */

public class Primer3Object {
    String leftPrimerId;
    String rightPrimerId;
    String leftSeq;
    String rightSeq;
    int leftLen;
    int rightLen;
    String leftStart;
    String rightStart;
    String leftTm;
    String rightTm;
    String leftGc;
    String rightGc;
    String leftAny;
    String rightAny;
    String left3;
    String right3;
    List<BlatPsl> leftPrimerBlatList;
    List<BlatPsl> rightPrimerBlatList;
    List<InsilicoPCRObject> insilicoPCRObjectList;
    List<Variation> insertionOverlaps;

    public Primer3Object(){
        super();
    }


    public Primer3Object createPrimer3Object(String line, String tag, Primer3Object pr) throws Exception{
        String delims = "[  ]+";
        String primerStr[] = line.split("PRIMER", -1);
        String prObj[] = primerStr[1].split(delims, -1);
        String start = prObj[1];
        String len = prObj[2];
        String tm = prObj[3];
        String gc = prObj[4];
        String any = prObj[5];
        String dash3 = prObj[6];
        String seq = prObj[7];

        System.out.println(prObj);

        if(tag.equals("left")){
            pr.setLeftSeq(seq);
            pr.setLeft3(dash3);
            pr.setLeftAny(any);
            pr.setLeftGc(gc);
            pr.setLeftLen(Integer.parseInt(len));
            pr.setLeftTm(tm);
            pr.setLeftStart(start);
        }else if(tag.equals("right")){
            pr.setRightSeq(seq);
            pr.setRight3(dash3);
            pr.setRightAny(any);
            pr.setRightGc(gc);
            pr.setRightLen(Integer.parseInt(len));
            pr.setRightTm(tm);
            pr.setRightStart(start);
        }

        return pr;
    }


    public List<Primer3Object> getPrimer3Objects(String inputFileName, String primer3OpDir, String blatInpDir, String blatOpDir, String isPcrOpDir, String dataDir) throws Exception{

        List<Primer3Object> primer3Objects = createPrimerObjsList(inputFileName, dataDir, primer3OpDir);
        primer3Objects = addIdsToPrimers(inputFileName, primer3Objects, blatInpDir, dataDir);
        primer3Objects = new BlatPsl().addBlatResultsToPrimers(inputFileName, blatOpDir, primer3Objects, dataDir);
        primer3Objects = new InsilicoPCRObject().addInsilicoResultsToPrimers(inputFileName, isPcrOpDir, primer3Objects, dataDir);

        return primer3Objects;
    }


    private List<Primer3Object> createPrimerObjsList(String inputFileName, String folder, String primer3OpDir) throws Exception{
        List<Primer3Object> primer3Objects = new ArrayList<Primer3Object>();
        String fileName=inputFileName;
        File primerInputFile = new File(folder+primer3OpDir+fileName);
        InputStream fileStream = new FileInputStream(primerInputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

        String line;
        Primer3Object prObj = new Primer3Object();

        while((line = reader.readLine())!=null){
            if(line.contains("LEFT PRIMER")){
                prObj = new Primer3Object().createPrimer3Object(line, "left", prObj);
                //prObj = createPrimer3Object(line, "left", prObj);
            }else if((line.contains("RIGHT PRIMER")) && (prObj.getLeftSeq().length()>0)){
                prObj = new Primer3Object().createPrimer3Object(line, "right", prObj);
                //prObj = createPrimer3Object(line, "right", prObj);
            }

            if(prObj.getRightSeq()!=null && prObj.getLeftSeq().length()>0 && prObj.getRightSeq().length()>0){
                primer3Objects.add(prObj);
                //after adding primer Obj to array, create new instance of object for next primer.
                prObj = new Primer3Object();
            }
        }

        return primer3Objects;
    }



    private List<Primer3Object> addIdsToPrimers(String inputFileName, List<Primer3Object> primer3Objects, String blatInpDir, String dataDir) throws Exception {

        List<Primer3Object> newPrimerObjects = new ArrayList<Primer3Object>();
        String fileName=inputFileName;
        File blatInputFile = new File(dataDir+blatInpDir+fileName);
        InputStream fileStream = new FileInputStream(blatInputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

        String line; String primerId="NA"; String counter = "0";
        int i=1;
        while((line = reader.readLine())!=null){
            if(line.startsWith(">")){
                primerId=line.replaceAll(">","");
                counter = primerId.split("_", -1)[1];
            }else{
                String seq = line;
                for(; i<=primer3Objects.size();){
                    Primer3Object prObj = primer3Objects.get(i-1);

                    if(counter.equals("L"+i)){
                        prObj.setLeftPrimerId(primerId);
                        primerId="NA";
                        break;

                    }else if(counter.equals("R"+i)){
                        prObj.setRightPrimerId(primerId);
                        primerId="NA";
                        if((!prObj.getLeftPrimerId().equals("NA")) && (!prObj.getRightPrimerId().equals("NA"))){
                            newPrimerObjects.add(prObj);
                        }
                        i+=1;
                        break;

                    }

                }
            }
        }

        return newPrimerObjects;
    }


    public String getLeftPrimerId() {
        return leftPrimerId;
    }

    public void setLeftPrimerId(String leftPrimerId) {
        this.leftPrimerId = leftPrimerId;
    }

    public String getRightPrimerId() {
        return rightPrimerId;
    }

    public void setRightPrimerId(String rightPrimerId) {
        this.rightPrimerId = rightPrimerId;
    }

    public String getLeftSeq() {
        return leftSeq;
    }

    public void setLeftSeq(String leftSeq) {
        this.leftSeq = leftSeq;
    }

    public String getRightSeq() {
        return rightSeq;
    }

    public void setRightSeq(String rightSeq) {
        this.rightSeq = rightSeq;
    }

    public int getLeftLen() {
        return leftLen;
    }

    public void setLeftLen(int leftLen) {
        this.leftLen = leftLen;
    }

    public int getRightLen() {
        return rightLen;
    }

    public void setRightLen(int rightLen) {
        this.rightLen = rightLen;
    }

    public String getLeftStart() {
        return leftStart;
    }

    public void setLeftStart(String leftStart) {
        this.leftStart = leftStart;
    }

    public String getRightStart() {
        return rightStart;
    }

    public void setRightStart(String rightStart) {
        this.rightStart = rightStart;
    }

    public String getLeftTm() {
        return leftTm;
    }

    public void setLeftTm(String leftTm) {
        this.leftTm = leftTm;
    }

    public String getRightTm() {
        return rightTm;
    }

    public void setRightTm(String rightTm) {
        this.rightTm = rightTm;
    }

    public String getLeftGc() {
        return leftGc;
    }

    public void setLeftGc(String leftGc) {
        this.leftGc = leftGc;
    }

    public String getRightGc() {
        return rightGc;
    }

    public void setRightGc(String rightGc) {
        this.rightGc = rightGc;
    }

    public String getLeftAny() {
        return leftAny;
    }

    public void setLeftAny(String leftAny) {
        this.leftAny = leftAny;
    }

    public String getRightAny() {
        return rightAny;
    }

    public void setRightAny(String rightAny) {
        this.rightAny = rightAny;
    }

    public String getLeft3() {
        return left3;
    }

    public void setLeft3(String left3) {
        this.left3 = left3;
    }

    public String getRight3() {
        return right3;
    }

    public void setRight3(String right3) {
        this.right3 = right3;
    }

    public List<BlatPsl> getLeftPrimerBlatList() {
        return leftPrimerBlatList;
    }

    public void setLeftPrimerBlatList(List<BlatPsl> leftPrimerBlatList) {
        this.leftPrimerBlatList = leftPrimerBlatList;
    }

    public List<BlatPsl> getRightPrimerBlatList() {
        return rightPrimerBlatList;
    }

    public void setRightPrimerBlatList(List<BlatPsl> rightPrimerBlatList) {
        this.rightPrimerBlatList = rightPrimerBlatList;
    }

    public List<InsilicoPCRObject> getInsilicoPCRObjectList() {
        return insilicoPCRObjectList;
    }

    public void setInsilicoPCRObjectList(List<InsilicoPCRObject> insilicoPCRObjectList) {
        this.insilicoPCRObjectList = insilicoPCRObjectList;
    }

    public List<Variation> getInsertionOverlaps() {
        return insertionOverlaps;
    }

    public void setInsertionOverlaps(List<Variation> insertionOverlaps) {
        this.insertionOverlaps = insertionOverlaps;
    }
}
