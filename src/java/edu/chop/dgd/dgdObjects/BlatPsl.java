package edu.chop.dgd.dgdObjects;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by jayaramanp on 3/4/14.
 */
public class BlatPsl{

    int match;
    int misMatch;
    int repMatch;
    int nCount;
    int qNumInsert;
    int qBaseInsert;
    int tNumInsert;
    int tBaseInsert;
    String[] strand;
    String qName;
    int qSize;
    int qStart;
    int qEnd;
    String tName;
    int tSize;
    int tStart;
    int tEnd;
    int blockCount;
    String[] blockSizes;
    String[] qStartsArr;
    String[] tStartsArr;
    int score;
    double percentageIdentity;



    /* Return score for psl. */
    public int calculateScore(BlatPsl pslObject) throws Exception{
        int score = 0;

        // Return score for psl.
        int sizeMul = pslIsProtein(pslObject) ? 3 : 1;
        return sizeMul * pslObject.match + (pslObject.repMatch>>1) - sizeMul * pslObject.misMatch - pslObject.qNumInsert
                - pslObject.tNumInsert;

    }



    /* is psl a protein psl (are it's blockSizes and scores in protein space) */
    public boolean pslIsProtein(BlatPsl psl){
        int lastBlock = psl.blockCount - 1;

        return(
                (
                        (psl.strand[0].equals("+") ) &&
                                (psl.tEnd == (Integer.parseInt(psl.tStartsArr[lastBlock]) + 3*Integer.parseInt(psl.blockSizes[lastBlock])))
                )
                        ||
                        (
                                (psl.strand[0].equals("-")) &&
                                        (psl.tStart == (psl.tSize - (Integer.parseInt(psl.tStartsArr[lastBlock]) + 3*Integer.parseInt(psl.blockSizes[lastBlock]))))
                        )
        );
    }



    /* Calculate badness in parts per thousand. GIVE better name.. */
    public int pslCalcMilliBad(BlatPsl pslObject, boolean isMrna){
        int sizeMul = pslIsProtein(pslObject) ? 3 : 1;
        int qAliSize, tAliSize, aliSize;
        long milliBad = 0;
        int insertFactor;


        qAliSize = sizeMul * (pslObject.qEnd - pslObject.qStart);
        tAliSize = pslObject.tEnd - pslObject.tStart;
        aliSize = Math.min(qAliSize, tAliSize);
        if (aliSize <= 0)
            return 0;
        int sizeDif = qAliSize - tAliSize;
        if (sizeDif < 0)
        {
            if (isMrna)
                sizeDif = 0;
            else
                sizeDif = -sizeDif;
        }
        insertFactor = pslObject.qNumInsert;
        if (!isMrna)
            insertFactor += pslObject.tNumInsert;

        int total = (sizeMul * (pslObject.match + pslObject.repMatch + pslObject.misMatch));
        if (total != 0)
            milliBad = (1000 * (pslObject.misMatch*sizeMul + insertFactor +
                    Math.round(3*Math.log(1+sizeDif)))) / total;

        return Math.round(milliBad);
    }


    public List<Primer3Object> addBlatResultsToPrimers(String blatFile, String blatOpDir, List<Primer3Object> primer3Objects, String dataDir) throws Exception {

        List<BlatPsl> blatResults = createBlatList(blatFile, blatOpDir, dataDir);

        for(BlatPsl blatObj : blatResults){
            for(Primer3Object prObj : primer3Objects){
                if(blatObj.getqName().equals(prObj.getLeftPrimerId())){
                    if(prObj.getLeftPrimerBlatList()==null){
                        List<BlatPsl> leftBlatList = new ArrayList<BlatPsl>();
                        leftBlatList.add(blatObj);
                        prObj.setLeftPrimerBlatList(leftBlatList);
                    }else{
                        List<BlatPsl> leftBlatList = prObj.getLeftPrimerBlatList();
                        leftBlatList.add(blatObj);
                        prObj.setLeftPrimerBlatList(leftBlatList);
                    }
                    break;
                }else if(blatObj.getqName().equals(prObj.getRightPrimerId())){
                    if(prObj.getRightPrimerBlatList()==null){
                        List<BlatPsl> rightBlatList = new ArrayList<BlatPsl>();
                        rightBlatList.add(blatObj);
                        prObj.setRightPrimerBlatList(rightBlatList);
                    }else{
                        List<BlatPsl> rightBlatList = prObj.getRightPrimerBlatList();
                        rightBlatList.add(blatObj);
                        prObj.setRightPrimerBlatList(rightBlatList);
                    }
                    break;
                }
            }
        }

        return primer3Objects;
    }

    public List<BlatPsl> createBlatList(String fileName, String blatOpDir, String dataDir) throws Exception{

        List<BlatPsl> blatResults = new ArrayList<BlatPsl>();

        FileReader f = new FileReader(dataDir+blatOpDir+fileName+"/FINAL.psl");
        Scanner s = new Scanner(f);
        while(s.hasNextLine()){
            String line = s.nextLine();
            if((!(line.contains("psLayout version 3")))&&(!(line.contains("match")))&&(!(line.contains("----------")))){
                String[] lineArr = line.split("\t", -1);
                if(!lineArr[0].equals("")){
                    String match = lineArr[0];
                    String misMatch = lineArr[1];
                    String repMatch = lineArr[2];
                    String nCount = lineArr[3];
                    String qGapCount = lineArr[4];
                    String qGapBases = lineArr[5];
                    String tGapCount = lineArr[6];
                    String tGapBases = lineArr[7];
                    String[] strand = lineArr[8].split(",", -1);
                    String qName = lineArr[9];
                    String qSize = lineArr[10];
                    String qStart = lineArr[11];
                    String qEnd = lineArr[12];
                    String tName = lineArr[13];
                    String tSize = lineArr[14];
                    String tStart = lineArr[15];
                    String tEnd = lineArr[16];
                    String blockCount = lineArr[17];
                    String[] blockSizes = lineArr[18].split(",", -1);
                    String[] qStartsArr = lineArr[19].split(",", -1);
                    String[] tStartsArr = lineArr[20].split(",", -1);
                    String[] querySeqArr = lineArr[21].split(",", -1);

                    BlatPsl newPslObject = new BlatPsl();
                    newPslObject.setMatch(Integer.parseInt(match));
                    newPslObject.setMisMatch(Integer.parseInt(misMatch));
                    newPslObject.setRepMatch(Integer.parseInt(repMatch));
                    newPslObject.setnCount(Integer.parseInt(nCount));
                    newPslObject.setqNumInsert(Integer.parseInt(qGapCount));
                    newPslObject.setqBaseInsert(Integer.parseInt(qGapBases));
                    newPslObject.settNumInsert(Integer.parseInt(tGapCount));
                    newPslObject.settBaseInsert(Integer.parseInt(tGapBases));
                    newPslObject.setStrand(strand);
                    newPslObject.setqName(qName);
                    newPslObject.setqSize(Integer.parseInt(qSize));
                    newPslObject.setqStart(Integer.parseInt(qStart));
                    newPslObject.setqEnd(Integer.parseInt(qEnd));
                    newPslObject.settName(tName);
                    newPslObject.settSize(Integer.parseInt(tSize));
                    newPslObject.settStart(Integer.parseInt(tStart));
                    newPslObject.settEnd(Integer.parseInt(tEnd));
                    newPslObject.setBlockCount(Integer.parseInt(blockCount));
                    newPslObject.setBlockSizes(blockSizes);
                    newPslObject.setqStartsArr(qStartsArr);
                    newPslObject.settStartsArr(tStartsArr);

                    int score = newPslObject.calculateScore(newPslObject);
                    newPslObject.setScore(score);

                    double percentageIdentity = 100.00-newPslObject.pslCalcMilliBad(newPslObject, true);
                    System.out.println("done with row#1\t"+qName+"\t and score is:\t"+score+"\tin chromosome:\t"+newPslObject.gettName()+"\t and percentage identity is:\t"+ percentageIdentity);
                    newPslObject.setPercentageIdentity(percentageIdentity);

                    blatResults.add(newPslObject);

                }
            }
        }

        return blatResults;
    }

    public double getPercentageIdentity() {
        return percentageIdentity;
    }

    public void setPercentageIdentity(double percentageIdentity) {
        this.percentageIdentity = percentageIdentity;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public int getMisMatch() {
        return misMatch;
    }

    public void setMisMatch(int misMatch) {
        this.misMatch = misMatch;
    }

    public int getRepMatch() {
        return repMatch;
    }

    public void setRepMatch(int repMatch) {
        this.repMatch = repMatch;
    }

    public int getnCount() {
        return nCount;
    }

    public void setnCount(int nCount) {
        this.nCount = nCount;
    }

    public int getqNumInsert() {
        return qNumInsert;
    }

    public void setqNumInsert(int qNumInsert) {
        this.qNumInsert = qNumInsert;
    }

    public int getqBaseInsert() {
        return qBaseInsert;
    }

    public void setqBaseInsert(int qBaseInsert) {
        this.qBaseInsert = qBaseInsert;
    }

    public int gettNumInsert() {
        return tNumInsert;
    }

    public void settNumInsert(int tNumInsert) {
        this.tNumInsert = tNumInsert;
    }

    public int gettBaseInsert() {
        return tBaseInsert;
    }

    public void settBaseInsert(int tBaseInsert) {
        this.tBaseInsert = tBaseInsert;
    }

    public String[] getStrand() {
        return strand;
    }

    public void setStrand(String[] strand) {
        this.strand = strand;
    }

    public String getqName() {
        return qName;
    }

    public void setqName(String qName) {
        this.qName = qName;
    }

    public int getqSize() {
        return qSize;
    }

    public void setqSize(int qSize) {
        this.qSize = qSize;
    }

    public int getqStart() {
        return qStart;
    }

    public void setqStart(int qStart) {
        this.qStart = qStart;
    }

    public int getqEnd() {
        return qEnd;
    }

    public void setqEnd(int qEnd) {
        this.qEnd = qEnd;
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public int gettSize() {
        return tSize;
    }

    public void settSize(int tSize) {
        this.tSize = tSize;
    }

    public int gettStart() {
        return tStart;
    }

    public void settStart(int tStart) {
        this.tStart = tStart;
    }

    public int gettEnd() {
        return tEnd;
    }

    public void settEnd(int tEnd) {
        this.tEnd = tEnd;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public String[] getBlockSizes() {
        return blockSizes;
    }

    public void setBlockSizes(String[] blockSizes) {
        this.blockSizes = blockSizes;
    }

    public String[] getqStartsArr() {
        return qStartsArr;
    }

    public void setqStartsArr(String[] qStartsArr) {
        this.qStartsArr = qStartsArr;
    }

    public String[] gettStartsArr() {
        return tStartsArr;
    }

    public void settStartsArr(String[] tStartsArr) {
        this.tStartsArr = tStartsArr;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


}
