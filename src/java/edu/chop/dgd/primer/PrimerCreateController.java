package edu.chop.dgd.primer;

import edu.chop.dgd.process.primerCreate.*;
import edu.chop.dgd.utils.Primer3Object;
import edu.chop.dgd.web.HttpRequestFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jayaramanp on 1/29/14.
 */
public class PrimerCreateController implements Controller {

    private String primer3InputDir="/data/primer3Inp/";
    private String primer3OpDir="/data/primer3out/";
    private String blatInpDir="/data/blatInp/";
    private String blatOpDir="/data/blatOp/";
    private String insilicoPcrInputDir="/data/isPcrInp/";
    private String insilicoPcrOpDir="/data/isPcrOp/";
    private String primerProcessScriptDir="/data/";


    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        HttpRequestFacade req = new HttpRequestFacade(request);
        String chr = "chr"+req.getParameter("selectObject");
        if(chr.equals("23")){
            chr="chrX";
        }else if(chr.equals("24")){
            chr="chrY";
        }


        int startPos = Integer.parseInt(req.getParameter("start"));
        int stopPos = Integer.parseInt(req.getParameter("stop"));



        PrimerDAO prDao = new PrimerDAO();
        List<UcscGene> geneList = prDao.getGenesInRange(chr, startPos, stopPos);
        List<Variation> vList = prDao.getVariantsInQuery(chr, startPos, stopPos);

        Map<UcscGene, UcscGeneExon> geneExonVariantMap = prDao.getGeneExonVariantMap(geneList, vList);
        AmpliconSeq ampliconObj = new AmpliconSeq().createAmpliconObject(vList.get(0));
        String inputFileName = writePrimerInputFile(vList, ampliconObj);
        String response = runProcessBuilder(inputFileName);

        List<Primer3Object> primer3Primers = new Primer3Object().getPrimer3Objects(inputFileName, primer3OpDir, blatInpDir, blatOpDir, insilicoPcrInputDir, insilicoPcrOpDir);

        String prStats = createFiles(primer3Primers, response, chr, startPos, stopPos, primerProcessScriptDir);

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/primer/primerReport.jsp");
        mvObj.addObject("geneList", geneList);
        mvObj.addObject("geneExonMap", geneExonVariantMap);
        mvObj.addObject("chr", chr);
        mvObj.addObject("start",startPos);
        mvObj.addObject("stop", stopPos);
        mvObj.addObject("variantsList", vList);
        mvObj.addObject("amplicon", ampliconObj);
        mvObj.addObject("primerResults", response);
        mvObj.addObject("stats", prStats);
        return mvObj;

    }

    private String createFiles(List<Primer3Object> primer3Primers, String response, String chr, int startPos, int stopPos, String primerProcessScriptDir) throws Exception {

        String path=primerProcessScriptDir+chr+"_"+startPos+"_"+stopPos;
        File filePath = new File(path+".detail");
        File softFile = new File(path+"_soft.xls");
        File secondaryFile = new File(path+"_secondary.xls");

        PrintWriter detailWriter = new PrintWriter(filePath);
        PrintWriter softWriter = new PrintWriter(softFile);
        PrintWriter secondaryFileWriter = new PrintWriter(secondaryFile);

        String softPrimers=""; String secondaryprimers="";

        int insilicoFlag = 0;
        int leftPrimerBlatFlag = 0;
        int rightPrimerBlatFlag = 0;
        int softFileFlag = 0;

        for(Primer3Object pr : primer3Primers){
            if((pr.getInsilicoPCRObjectList()!=null) && (pr.getInsilicoPCRObjectList().size()==1)){
                insilicoFlag+=1;
            }
            if((pr.getLeftPrimerBlatList()!=null) && (pr.getLeftPrimerBlatList().size()==1)){
                if(pr.getLeftPrimerBlatList().get(0).getPercentageIdentity()==100.00){
                    leftPrimerBlatFlag+=1;
                }
            }
            if((pr.getRightPrimerBlatList()!=null) && (pr.getRightPrimerBlatList().size()==1)){
                if(pr.getRightPrimerBlatList().get(0).getPercentageIdentity()==100.00){
                    rightPrimerBlatFlag+=1;
                }
            }

            if(insilicoFlag==1 && leftPrimerBlatFlag==1 && rightPrimerBlatFlag==1){

               softPrimers+=pr.getLeftPrimerId()+" "+pr.getLeftSeq()+"\t"+pr.getRightPrimerId()+" "+pr.getRightSeq()+"\n";
               softFileFlag = writeFile(pr, softWriter, chr, startPos, stopPos);


            }else{
                secondaryprimers+=pr.getLeftPrimerId()+" "+pr.getLeftSeq()+"\t"+pr.getRightPrimerId()+" "+pr.getRightSeq()+"\n";
                softFileFlag = writeFile(pr, secondaryFileWriter, chr, startPos, stopPos);

            }
        }



        detailWriter.print(response);

        softWriter.close();
        secondaryFileWriter.close();
        detailWriter.close();


        if((softPrimers.length()>0) && (softFileFlag ==1)){
            return softPrimers;
        }else{
            return "NA";
        }

    }

    private int writeFile(Primer3Object pr, PrintWriter fileWriter, String chr, int startPos, int stopPos) {

        fileWriter.print("Primer Name\tHuman Genome Build\tQuery Condition\tIn-Silico PCR\tprimer ID\tstart\tlen\ttm\tgc%\tany\t3'\tseq\tproductSize\tSNPs found\tChrom\tPrimer Start\tPrimer End\tfolder Id\tsnp check ID\n");
        fileWriter.print(pr.getLeftPrimerId()+"\thg19\t"+chr+":"+startPos+"_"+stopPos+"\tPASS\t"+chr+":"+startPos+"_"+stopPos+"F\t"+pr.getLeftStart()+"\t"+pr.getLeftLen()+"\t"+pr.getLeftTm()+"\t"+pr.getLeftGc()+"\t"+pr.getLeftAny()+"\t"+pr.getLeft3()+"\t"+pr.getLeftSeq()+"\t"+pr.getInsilicoPCRObjectList().get(0).getSize()+"\tN\t"+pr.getInsilicoPCRObjectList().get(0).getChr()+"\t"+pr.getInsilicoPCRObjectList().get(0).getPrimerSeqStart()+"\t"+pr.getInsilicoPCRObjectList().get(0).getPrimerSeqEnd()+"\tNA\tNA\n");
        fileWriter.print(pr.getRightPrimerId()+"\thg19\t"+chr+":"+startPos+"_"+stopPos+"\tPASS\t"+chr+":"+startPos+"_"+stopPos+"F\t"+pr.getRightStart()+"\t"+pr.getRightLen()+"\t"+pr.getRightTm()+"\t"+pr.getRightGc()+"\t"+pr.getRightAny()+"\t"+pr.getRight3()+"\t"+pr.getRightSeq()+"\t"+pr.getInsilicoPCRObjectList().get(0).getSize()+"\tN\t"+pr.getInsilicoPCRObjectList().get(0).getChr()+"\t"+pr.getInsilicoPCRObjectList().get(0).getPrimerSeqStart()+"\t"+pr.getInsilicoPCRObjectList().get(0).getPrimerSeqEnd()+"\tNA\tNA\n");

        return 1;
    }



    private String writePrimerInputFile(List<Variation> vList, AmpliconSeq ampliconObj) throws FileNotFoundException {
        Date dt = new Date();
        String fileName="primerInp"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
        File primerInputFile = new File(primer3InputDir+fileName);
        PrintWriter pw = new PrintWriter(primerInputFile);
        int bufferUpstreamPos = vList.get(0).getVstart()-ampliconObj.getBufferUpstream();
        int relPosStart = bufferUpstreamPos-ampliconObj.getAmpliconStart();
        int relPosLength = ampliconObj.getBufferUpstream()+vList.get(0).getVstop()-vList.get(0).getVstart()+ampliconObj.getBufferDownstream();
        pw.println("SEQUENCE_ID=inpSeq1\nSEQUENCE_TEMPLATE="+ampliconObj.getMaskedSeq()+"\nSEQUENCE_TARGET="+relPosStart+","+relPosLength+"\n=");
        System.out.println("SEQUENCE_ID=inpSeq1\nSEQUENCE_TEMPLATE="+ampliconObj.getMaskedSeq()+"\nSEQUENCE_TARGET="+relPosStart+","+relPosLength+"\n=");
        pw.flush();
        pw.close();

        return fileName;
    }



    public String runProcessBuilder(String inputFileName) throws Exception {

        String answer = "output:";

        ProcessBuilder pb = new ProcessBuilder(getPrimerProcessScriptDir()+"PrimerProcess.sh",inputFileName);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
        env.put("PATH", path);

        pb.directory(new File(primerProcessScriptDir));
        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the primerProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the primerprocess.sh..");
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
            System.out.println(answer);
            System.out.println("should have got an output from Primer3 and BLAT..");
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;

    }



    public void setPrimer3InputDir(String primer3InputDir) {
        this.primer3InputDir = primer3InputDir;
    }

    public String getPrimer3InputDir() {
        return primer3InputDir;
    }

    public void setPrimer3OpDir(String primer3OpDir) {
        this.primer3OpDir = primer3OpDir;
    }

    public String getPrimer3OpDir() {
        return primer3OpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        this.blatInpDir = blatInpDir;
    }

    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        this.blatOpDir = blatOpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setPrimerProcessScriptDir(String primerProcessScriptDir) {
        this.primerProcessScriptDir = primerProcessScriptDir;
    }

    public String getPrimerProcessScriptDir() {
        return primerProcessScriptDir;
    }

    public String getInsilicoPcrInputDir() {
        return insilicoPcrInputDir;
    }

    public void setInsilicoPcrInputDir(String insilicoPcrInputDir) {
        this.insilicoPcrInputDir = insilicoPcrInputDir;
    }

    public String getInsilicoPcrOpDir() {
        return insilicoPcrOpDir;
    }

    public void setInsilicoPcrOpDir(String insilicoPcrOpDir) {
        this.insilicoPcrOpDir = insilicoPcrOpDir;
    }
}
