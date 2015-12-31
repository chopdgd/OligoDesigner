package edu.chop.dgd.dgdObjects;

import edu.chop.dgd.process.primerCreate.AmpliconSeq;
import edu.chop.dgd.process.primerCreate.AmpliconXomAnalyzer;
import edu.chop.dgd.process.primerCreate.PrimerDAO;
import edu.chop.dgd.process.primerCreate.Variation;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by jayaramanp on 6/19/14.
 */
public class SequenceObject{
    String chr;
    int start;
    int stop;
    List<SequenceObjectSubsections> oligoObjectSubsections;
    String reportFile;
    String filename;
    HashMap<OligoObject, List<OligoObject>> hetDimerHashMap;
    HashMap<String, List<OligoObject>> OligoSetsMap;
    TreeMap<String, List<OligoObject>> OligoSetsTreeMap;
    String detailsFile;
    String secondaryFile;

    public List<SequenceObjectSubsections> generateSequenceSubsections(String inputFilename, String dataDir) throws Exception{

        String file = dataDir+inputFilename;
        PrintWriter subsectionWriter = new PrintWriter(file);

        int counter=0;

        for(int i=this.getStart(); i<=this.getStop(); ){

            int subsectStart = i;
            int windowSize = 6000;
            int subsectStop = i+windowSize;
            i+=3000+1;
            counter+=1;

            SequenceObjectSubsections olSubsObj = new SequenceObjectSubsections();
            olSubsObj.setSubSectionChr(this.getChr());
            olSubsObj.setSubSectionStart(subsectStart);
            olSubsObj.setSubSectionStop(subsectStop);
            olSubsObj.setSubSectionWindowNum(counter);

            subsectionWriter.println(olSubsObj.getSubSectionChr() + ":" + olSubsObj.getSubSectionStart() + "-" + olSubsObj.getSubSectionStop());

        }

        subsectionWriter.close();

        String sequenceInfo = retrieveSubsectionSequencesProcessBuilder(inputFilename, dataDir);


        return createSequenceObjSubsections(sequenceInfo);

    }



    private List<SequenceObjectSubsections> createSequenceObjSubsections(String sequenceInfo) throws Exception {

        List<SequenceObjectSubsections> seqObjSubs = new ArrayList<SequenceObjectSubsections>();
        PrimerDAO prDao = new PrimerDAO();
        List<Variation> variantsInSequenceObject = prDao.getVariantsWithinRange(this.getChr(),
                this.getStart(), this.getStop());

        String[] seqArray = sequenceInfo.split(">", -1);
        for(String fastaSeq : seqArray){
            if(fastaSeq.contains("chr")){
                String queryDelims = "[:+ -]+";
                String[] sequence = fastaSeq.split(queryDelims, -1);
                System.out.println(sequence[0]+"\n"+sequence[1]+"\n"+sequence[2]);
                String seq2 = sequence[3].replaceAll("\n", "").toLowerCase();
                String seq = sequence[3].replaceAll("\n", "").toUpperCase();

                SequenceObjectSubsections sosb = new SequenceObjectSubsections();
                sosb.setSubSectionChr(sequence[0]);
                sosb.setSubSectionStart(Integer.parseInt(sequence[1]));
                sosb.setSubSectionStop(Integer.parseInt(sequence[2]));


                AmpliconSeq amplObj = new AmpliconSeq();
                amplObj.setChr(sosb.getSubSectionChr());
                amplObj.setAmpliconStart(sosb.getSubSectionStart());
                amplObj.setAmpliconEnd(sosb.getSubSectionStop());
                amplObj.setBufferUpstream(0);
                amplObj.setBufferDownstream(0);
                amplObj.setSequence(seq);

                String maskedSeq = amplObj.maskAmpliconSequenceForOligoToN(amplObj, variantsInSequenceObject);
                //String maskedlowercase = maskedSeq.replaceAll("N", "n");

                sosb.setSubSectionSequence(maskedSeq);

                seqObjSubs.add(sosb);
            }

        }

        return seqObjSubs;
    }


    private String retrieveSubsectionSequencesProcessBuilder(String inputFilename, String dataDir) throws Exception{


        String answer;
        String errAnswer="NA";

        String inpFile = dataDir+inputFilename;
        ProcessBuilder pb = new ProcessBuilder(dataDir+"RetrieveSequence.sh",inputFilename);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/blat";
        env.put("PATH", path);

        pb.directory(new File(dataDir));

        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the RetrieveSequence.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the RetrieveSequence.sh..");

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
                sb.append(line).append("\n");
            }
            answer = sb.toString();

            System.out.println(erranswer);

            System.out.println(answer);

            System.out.println("should have got an output from twoBitToFasta command...");


            if(errAnswer.length()>2){

                throw new Exception("Exception: Blat server issues. The BLAT server may have " +

                        "not been started. Please start server using gfServer.");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;

    }


    public List<SequenceObjectSubsections> generateSubsections() throws Exception{
        List<SequenceObjectSubsections> subs = new ArrayList<SequenceObjectSubsections>();
        int counter=0;
        PrimerDAO prDao = new PrimerDAO();

        for(int i=this.getStart(); i<=this.getStop(); ){

            int subsectStart = i;
            int windowSize = 6000;
            int subsectStop = i+windowSize;
            i+=3000+1;
            counter+=1;

            SequenceObjectSubsections olSubsObj = new SequenceObjectSubsections();
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

    public List<SequenceObjectSubsections> getOligoObjectSubsections() {
        return oligoObjectSubsections;
    }

    public void setOligoObjectSubsections(List<SequenceObjectSubsections> oligoObjectSubsections) {
        this.oligoObjectSubsections = oligoObjectSubsections;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public HashMap<OligoObject, List<OligoObject>> getHetDimerHashMap() {
        return hetDimerHashMap;
    }

    public void setHetDimerHashMap(HashMap<OligoObject, List<OligoObject>> hetDimerHashMap) {
        this.hetDimerHashMap = hetDimerHashMap;
    }

    public HashMap<String, List<OligoObject>> getOligoSetsMap() {
        return OligoSetsMap;
    }

    public void setOligoSetsMap(HashMap<String, List<OligoObject>> oligoSetsMap) {
        OligoSetsMap = oligoSetsMap;
    }

    public TreeMap<String, List<OligoObject>> getOligoSetsTreeMap() {
        return OligoSetsTreeMap;
    }

    public void setOligoSetsTreeMap(TreeMap<String, List<OligoObject>> oligoSetsTreeMap) {
        OligoSetsTreeMap = oligoSetsTreeMap;
    }

    public String getSecondaryFile() {
        return secondaryFile;
    }

    public void setSecondaryFile(String secondaryFile) {
        this.secondaryFile = secondaryFile;
    }

    public String getDetailsFile() {
        return detailsFile;
    }

    public void setDetailsFile(String detailsFile) {
        this.detailsFile = detailsFile;
    }

}
