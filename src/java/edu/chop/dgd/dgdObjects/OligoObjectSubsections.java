package edu.chop.dgd.dgdObjects;

import java.util.List;

/**
 * Created by jayaramanp on 6/19/14.
 */
public class OligoObjectSubsections {

    String subSectionChr;
    int subSectionStart;
    int subSectionStop;
    String subSectionSequence;
    int subSectionWindowNum;
    String subsectionid;
    List<Primer3Object> oligoList;
    List<BlatPsl> blatResults;
    List<MfoldHairpin> mfoldHairpinResults;


    public List<OligoObjectSubsections> retrieveResultsFromAnalyses(String fileName, List<OligoObjectSubsections> osSubsList, String dataDir, String oligoOutputDir, String blatOligoInpDir, String blatOligoOpDir)throws Exception {

        List<BlatPsl> blatResults = new BlatPsl().createOligoBlatList(fileName, blatOligoOpDir, dataDir);

        for(OligoObjectSubsections oss : osSubsList){
            Primer3Object p3Obj = new Primer3Object();
            List<Primer3Object> oligoObjectsFromPrimer3 = p3Obj.createOligoObjsList(fileName, dataDir, oligoOutputDir, oss);
            oligoObjectsFromPrimer3 = p3Obj.addIdsToOligos(fileName, oligoObjectsFromPrimer3, blatOligoInpDir, dataDir, oss);
            oligoObjectsFromPrimer3 = new BlatPsl().addBlatResultsToOligos(blatResults, oligoObjectsFromPrimer3, dataDir, oss);
        }

        return null;

    }




    public String getSubSectionChr() {
        return subSectionChr;
    }

    public void setSubSectionChr(String subSectionChr) {
        this.subSectionChr = subSectionChr;
    }

    public String getSubSectionSequence() {
        return subSectionSequence;
    }

    public void setSubSectionSequence(String subSectionSequence) {
        this.subSectionSequence = subSectionSequence;
    }

    public int getSubSectionStart() {
        return subSectionStart;
    }

    public void setSubSectionStart(int subSectionStart) {
        this.subSectionStart = subSectionStart;
    }

    public int getSubSectionStop() {
        return subSectionStop;
    }

    public void setSubSectionStop(int subSectionStop) {
        this.subSectionStop = subSectionStop;
    }

    public int getSubSectionWindowNum() {
        return subSectionWindowNum;
    }

    public void setSubSectionWindowNum(int subSectionWindowNum) {
        this.subSectionWindowNum = subSectionWindowNum;
    }

    public List<Primer3Object> getOligoList() {
        return oligoList;
    }

    public void setOligoList(List<Primer3Object> oligoList) {
        this.oligoList = oligoList;
    }

    public List<BlatPsl> getBlatResults() {
        return blatResults;
    }

    public void setBlatResults(List<BlatPsl> blatResults) {
        this.blatResults = blatResults;
    }

    public String getSubsectionid() {
        return subsectionid;
    }

    public void setSubsectionid(String subsectionid) {
        this.subsectionid = subsectionid;
    }

    public List<MfoldHairpin> getMfoldHairpinResults() {
        return mfoldHairpinResults;
    }

    public void setMfoldHairpinResults(List<MfoldHairpin> mfoldHairpinResults) {
        this.mfoldHairpinResults = mfoldHairpinResults;
    }
}
