package edu.chop.dgd.dgdObjects;

import edu.chop.dgd.process.primerCreate.AmpliconSeq;
import edu.chop.dgd.process.primerCreate.AmpliconXomAnalyzer;
import edu.chop.dgd.process.primerCreate.PrimerDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jayaramanp on 6/19/14.
 */
public class OligoObject{
    String chr;
    int start;
    int stop;
    List<OligoObjectSubsections> oligoObjectSubsections;



    public List<OligoObjectSubsections> generateSubsections() throws Exception{
        List<OligoObjectSubsections> subs = new ArrayList<OligoObjectSubsections>();
        int counter=0;
        PrimerDAO prDao = new PrimerDAO();

        for(int i=this.getStart(); i<=this.getStop(); ){

            int subsectStart = i;
            int windowSize = 6000;
            int subsectStop = i+windowSize;
            i+=3000+1;
            counter+=1;

            OligoObjectSubsections olSubsObj = new OligoObjectSubsections();
            olSubsObj.setSubSectionChr(this.getChr());
            olSubsObj.setSubSectionStart(subsectStart);
            olSubsObj.setSubSectionStop(subsectStop);
            olSubsObj.setSubSectionWindowNum(counter);

            String dasSequence = prDao.getAmpliconSequence(olSubsObj.getSubSectionChr(), olSubsObj.getSubSectionStart(), olSubsObj.getSubSectionStop());
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

            String maskedSeq = amplObj.maskAmpliconSequence(amplObj);

            olSubsObj.setSubSectionSequence(maskedSeq);

            subs.add(olSubsObj);

        }

        return subs;
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

    public List<OligoObjectSubsections> getOligoObjectSubsections() {
        return oligoObjectSubsections;
    }

    public void setOligoObjectSubsections(List<OligoObjectSubsections> oligoObjectSubsections) {
        this.oligoObjectSubsections = oligoObjectSubsections;
    }

}
