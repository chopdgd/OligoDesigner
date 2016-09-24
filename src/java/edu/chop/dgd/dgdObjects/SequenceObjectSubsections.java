package edu.chop.dgd.dgdObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jayaramanp on 6/19/14.
 */
public class SequenceObjectSubsections {

    String subSectionAssembly;
    String subSectionChr;
    int subSectionStart;
    int subSectionStop;
    String subSectionSequence;
    int subSectionWindowNum;
    String subsectionid;
    List<OligoObject> oligoList;


    public List<SequenceObjectSubsections> retrieveResultsFromAnalyses(String fileName, List<SequenceObjectSubsections> osSubsList,
                       String dataDir, String oligoOutputDir, String blatOligoInpDir,
                                       String blatOligoOpDir, String mfoldInpDir, String mfoldOpDir,
                                                          String homodimerOpDir, String heterodimerInpDir, String heterodimeropDir)throws Exception {


        List<OligoObject> oligoObjectsFromPrimer3 = new OligoObject().createOligoObjsList(fileName, dataDir, oligoOutputDir);
        oligoObjectsFromPrimer3 = new OligoObject().addIdsToOligos(fileName, oligoObjectsFromPrimer3, blatOligoInpDir, dataDir, osSubsList);
        List<BlatPsl> blatResults = new BlatPsl().createOligoBlatList(fileName, blatOligoOpDir, dataDir);
        oligoObjectsFromPrimer3 = new BlatPsl().addBlatResultsToOligos(blatResults, oligoObjectsFromPrimer3, osSubsList);
        oligoObjectsFromPrimer3 = new MfoldHairpin().getDeltaGValuesForHairpin(oligoObjectsFromPrimer3, fileName, mfoldOpDir, dataDir);
        oligoObjectsFromPrimer3 = new MfoldDimer().getDeltaGValuesForHomoDimer(oligoObjectsFromPrimer3, fileName, homodimerOpDir, dataDir);

        osSubsList = addOligoListToSubsection(oligoObjectsFromPrimer3, osSubsList);

        return osSubsList;

    }


    private List<SequenceObjectSubsections> addOligoListToSubsection(List<OligoObject> oligoObjectsFromPrimer3, List<SequenceObjectSubsections> osSubsList) {
        for(SequenceObjectSubsections sos : osSubsList){
            for(OligoObject obj : oligoObjectsFromPrimer3){
                String oligoSubsectionId = obj.getInternalPrimerId().split("_", -1)[0]+"_"+
                        obj.getInternalPrimerId().split("_",-1)[1];
                if(oligoSubsectionId.equals(sos.getSubsectionid())){
                    if(sos.getOligoList()!=null && sos.getOligoList().size()>0){
                        List<OligoObject> sosOligosList = sos.getOligoList();
                        sosOligosList.add(obj);
                        sos.setOligoList(sosOligosList);
                    }else{
                        List<OligoObject> sosOligoObjectList = new ArrayList<OligoObject>();
                        sosOligoObjectList.add(obj);
                        sos.setOligoList(sosOligoObjectList);
                    }
                }
            }
        }

        return osSubsList;
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

    public List<OligoObject> getOligoList() {
        return oligoList;
    }

    public void setOligoList(List<OligoObject> oligoList) {
        this.oligoList = oligoList;
    }

    public String getSubsectionid() {
        return subsectionid;
    }

    public void setSubsectionid(String subsectionid) {
        this.subsectionid = subsectionid;
    }

    public String getSubSectionAssembly() {
        return subSectionAssembly;
    }

    public void setSubSectionAssembly(String subSectionAssembly) {
        this.subSectionAssembly = subSectionAssembly;
    }
}
