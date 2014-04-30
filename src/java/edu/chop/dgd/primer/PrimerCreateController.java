package edu.chop.dgd.primer;

import edu.chop.dgd.process.primerCreate.*;
import edu.chop.dgd.utils.BlatPsl;
import edu.chop.dgd.utils.InsilicoPCRObject;
import edu.chop.dgd.utils.Primer3Object;
import edu.chop.dgd.web.HttpRequestFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jayaramanp on 1/29/14.
 */
public class PrimerCreateController implements Controller {


    private static String primer3InputDir;
    private static String primer3OpDir;
    private static String blatInpDir;
    private static String blatOpDir;
    private static String insilicoPcrInputDir;
    private static String insilicoPcrOpDir;
    private static String primerProcessScriptDir;
    private static String dataDir;

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
        int varrelPos = vList.get(0).getVstart()-ampliconObj.getAmpliconStart()+2;
        List<Variation> variantsWithinAmpliconObjList = prDao.getVariantsWithinRange(ampliconObj.getChr(), ampliconObj.getAmpliconStart(), ampliconObj.getAmpliconEnd());
        String inputFileName = writePrimerInputFile(vList, ampliconObj);
        String response = runProcessBuilder(inputFileName);

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/primer/primerReport.jsp");

        if(response.contains("NO PRIMERS FOUND")){

            mvObj.addObject("geneList", geneList);
            mvObj.addObject("geneExonMap", geneExonVariantMap);
            mvObj.addObject("chr", chr);
            mvObj.addObject("start",startPos);
            mvObj.addObject("stop", stopPos);
            mvObj.addObject("variantsList", vList);
            mvObj.addObject("amplicon", ampliconObj);
            mvObj.addObject("variantRelPos", varrelPos);
            mvObj.addObject("outputFiles", "NA");
            mvObj.addObject("stats", "NA");

        }else{
            List<Primer3Object> primer3Primers = new Primer3Object().getPrimer3Objects(inputFileName, primer3OpDir, blatInpDir, blatOpDir, insilicoPcrOpDir, dataDir);
            String prStats = createFiles(primer3Primers, response, chr, startPos, stopPos, dataDir, geneList, geneExonVariantMap, vList, ampliconObj, varrelPos, variantsWithinAmpliconObjList);
            String outputs[] = prStats.split("FILEName\\:", -1);
            String filePath = outputs[1];

            mvObj.addObject("geneList", geneList);
            mvObj.addObject("geneExonMap", geneExonVariantMap);
            mvObj.addObject("chr", chr);
            mvObj.addObject("start",startPos);
            mvObj.addObject("stop", stopPos);
            mvObj.addObject("variantsList", vList);
            mvObj.addObject("amplicon", ampliconObj);
            mvObj.addObject("variantRelPos", varrelPos);
            mvObj.addObject("outputFiles", filePath);
            mvObj.addObject("stats", outputs[0]);
        }

        return mvObj;

    }



    private String createFiles(List<Primer3Object> primer3Primers, String response, String chr, int startPos, int stopPos, String dataDir, List<UcscGene> geneList, Map<UcscGene, UcscGeneExon> geneExonVariantMap, List<Variation> vList, AmpliconSeq ampliconObj, int varrelPos, List<Variation> variantsWithinAmpliconObjList) throws Exception {

        String path=chr+"_"+startPos+"_"+stopPos;
        File filePath = new File(dataDir+path+".detail.html");
        File softFile = new File(dataDir+path+"_soft.xls");
        File secondaryFile = new File(dataDir+path+"_secondary.xls");
        PrimerDAO prDao = new PrimerDAO();

        PrintWriter detailWriter = new PrintWriter(filePath);
        PrintWriter softWriter = new PrintWriter(softFile);
        PrintWriter secondaryFileWriter = new PrintWriter(secondaryFile);

        softWriter.print("Primer Name\tHuman Genome Build\tQuery Condition\tIn-Silico PCR\tprimer ID\tstart\tlen\ttm\tgc%\tany\t3'\tseq\tproductSize\tSNPs found\tChrom\tPrimer Start\tPrimer End\tfolder Id\tsnp check ID\tBLAT Percentage Identity\n");
        secondaryFileWriter.print("Primer Name\tHuman Genome Build\tQuery Condition\tIn-Silico PCR\tprimer ID\tstart\tlen\ttm\tgc%\tany\t3'\tseq\tproductSize\tSNPs found\tChrom\tPrimer Start\tPrimer End\tfolder Id\tsnp check ID\tBLAT Percentage Identity\n");

        String primers="";
        int insilicoFlag = 0;
        int leftPrimerBlatFlag = 0;
        int rightPrimerBlatFlag = 0;
        int softFileFlag = 0; int secondaryFileFlag = 0; int finalFlag = 0;

        for(Primer3Object pr : primer3Primers){

            int isFlag=0; int rtPrFlag=0; int lfPrFlag=0; int leftVarInsFlag=0; int rightVarInsFlag=0;

            if((pr.getInsilicoPCRObjectList()!=null) && (pr.getInsilicoPCRObjectList().size()==1)){

                isFlag=1;
                insilicoFlag+=1;
            }

            if((pr.getLeftPrimerBlatList()!=null) && (pr.getLeftPrimerBlatList().size()==1)){

                if(pr.getLeftPrimerBlatList().get(0).getPercentageIdentity()==100.00){
                    lfPrFlag=1;
                    leftPrimerBlatFlag+=1;
                }
            }


            if((pr.getRightPrimerBlatList()!=null) && (pr.getRightPrimerBlatList().size()==1)){

                if(pr.getRightPrimerBlatList().get(0).getPercentageIdentity()==100.00){
                    rtPrFlag=1;
                    rightPrimerBlatFlag+=1;
                }

            }



            if(insilicoFlag>0 && leftPrimerBlatFlag>0 && rightPrimerBlatFlag>0){

                if(isFlag==1){

                    int leftPrStart = pr.getInsilicoPCRObjectList().get(0).getPrimerSeqStart();
                    int leftPrEnd = pr.getInsilicoPCRObjectList().get(0).getPrimerSeqStart()+pr.getLeftLen();

                    int rightPrStart = pr.getInsilicoPCRObjectList().get(0).getPrimerSeqEnd()-pr.getRightLen();
                    int rightPrEnd = pr.getInsilicoPCRObjectList().get(0).getPrimerSeqEnd();

                    List<Variation> leftVariantsList = findInsertionsInPrimers(chr, leftPrStart, leftPrEnd, variantsWithinAmpliconObjList);
                    List<Variation> rightVariantsList = findInsertionsInPrimers(chr, rightPrStart, rightPrEnd, variantsWithinAmpliconObjList);

                    for(Variation v : leftVariantsList){
                        if(v.getvClass().equals("insertion")){
                            leftVarInsFlag=1;
                            break;
                        }
                    }

                    for(Variation v : rightVariantsList){
                        if(v.getvClass().equals("insertion")){
                            rightVarInsFlag=1;
                            break;
                        }
                    }



                    if(leftVarInsFlag==0 && rightVarInsFlag==0 && lfPrFlag==1 && rtPrFlag==1){

                        if(softFileFlag==0){

                            finalFlag=1;
                            softFileFlag = writeFile(pr, softWriter, chr, startPos, stopPos);

                        }else if(softFileFlag==1){

                            finalFlag=1;
                            secondaryFileFlag = writeFile(pr, secondaryFileWriter, chr, startPos, stopPos);
                        }

                    }else{

                        finalFlag=0;
                        secondaryFileFlag = writeFile(pr, secondaryFileWriter, chr, startPos, stopPos);

                    }
                }else{

                    finalFlag=0;
                    secondaryFileFlag = writeFile(pr, secondaryFileWriter, chr, startPos, stopPos);

                }

            }else{
                finalFlag=0;
                secondaryFileFlag = writeFile(pr, secondaryFileWriter, chr, startPos, stopPos);
            }


            primers+=pr.getLeftPrimerId()+":"+pr.getLeftSeq()+"\t"+pr.getRightPrimerId()+":"+pr.getRightSeq()+"\tinsilicoFlag:"+isFlag+"\tleftBlatFlag:"+lfPrFlag+"\tRightBlatFlag:"+rtPrFlag+
                    "\tInsCheckLtPrFlag:"+leftVarInsFlag+"\tInsCheckRtPrFlag:"+rightVarInsFlag+"\tFINAL:"+finalFlag+"\n";
        }

        primers+="FILEName:"+path+"\n";



        PrimerReport primerReport = new PrimerReport(detailWriter);
        primerReport.setGeneList(geneList);
        primerReport.setVariationList(vList);
        primerReport.setGeneExonMap(geneExonVariantMap);
        primerReport.setAmplicon(ampliconObj);
        primerReport.setVarrelPos(varrelPos);
        primerReport.setResponse(response);
        primerReport.createReport();


        detailWriter.close();
        softWriter.close();
        secondaryFileWriter.close();
        detailWriter.close();


        if(primers.length()>0){
            return primers;
        }else{
            return "NA";
        }


    }

    private List<Variation> findInsertionsInPrimers(String chr, int prStart, int prEnd, List<Variation> variantsWithinAmpliconObjList) {

        List<Variation> varList = new ArrayList<Variation>();
        for(Variation v : variantsWithinAmpliconObjList){
            if(v.getvClass().equals("insertion")){
                if(v.getVstop()>=prStart && v.getVstart()<=prEnd){
                    varList.add(v);
                }
            }
        }

        return varList;
    }


    private int writeFile(Primer3Object pr, PrintWriter fileWriter, String chr, int startPos, int stopPos) {


        fileWriter.print(pr.getLeftPrimerId()+"\thg19\t"+chr+":"+startPos+"_"+stopPos+"\tPASS\t"+chr+":"+startPos+"_"+stopPos+"F\t"+pr.getLeftStart()+"\t"+pr.getLeftLen()+"\t"+pr.getLeftTm()+"\t"+pr.getLeftGc()+"\t"+pr.getLeftAny()+"\t"+pr.getLeft3()+"\t"+pr.getLeftSeq()+"\t");

         if(pr.getInsilicoPCRObjectList()!=null && pr.getInsilicoPCRObjectList().size()>0){

            for(InsilicoPCRObject isPcr : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr.getSize() + "/");
            }
            fileWriter.print("\tN\t");
            for(InsilicoPCRObject isPcr2 : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr2.getChr() + "/");
            }
            fileWriter.print("\t");
            for(InsilicoPCRObject isPcr3 : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr3.getPrimerSeqStart() + "/");
            }
            fileWriter.print("\t");
            for(InsilicoPCRObject isPcr4 : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr4.getPrimerSeqEnd() + "/");
            }

        }else{
                fileWriter.print("NA\tN\tNA\tNA\tNA");
        }

        fileWriter.print("\tNA\tNA\t");

        if(pr.getLeftPrimerBlatList()!=null){

            for(BlatPsl bl : pr.getLeftPrimerBlatList()){
                fileWriter.print(bl.getPercentageIdentity()+"/");
            }

        }else{

            fileWriter.print("\tNA\tNA\tNA");
        }
        fileWriter.print("\n");


        fileWriter.print(pr.getRightPrimerId()+"\thg19\t"+chr+":"+startPos+"_"+stopPos+"\tPASS\t"+chr+":"+startPos+"_"+stopPos+"F\t"+pr.getRightStart()+"\t"+pr.getRightLen()+"\t"+pr.getRightTm()+"\t"+pr.getRightGc()+"\t"+pr.getRightAny()+"\t"+pr.getRight3()+"\t"+pr.getRightSeq()+"\t");

        if(pr.getInsilicoPCRObjectList()!=null && pr.getInsilicoPCRObjectList().size()>0){

            for(InsilicoPCRObject isPcr : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr.getSize() + "/");
            }
            fileWriter.print("\tN\t");
            for(InsilicoPCRObject isPcr2 : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr2.getChr() + "/");
            }
            fileWriter.print("\t");
            for(InsilicoPCRObject isPcr3 : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr3.getPrimerSeqStart() + "/");
            }
            fileWriter.print("\t");
            for(InsilicoPCRObject isPcr4 : pr.getInsilicoPCRObjectList()){
                fileWriter.print(isPcr4.getPrimerSeqEnd() + "/");
            }

        }else{
            fileWriter.print("NA\tN\tNA\tNA\tNA");
        }

        fileWriter.print("\tNA\tNA\t");

        if(pr.getRightPrimerBlatList()!=null){

            for(BlatPsl bl : pr.getRightPrimerBlatList()){
                fileWriter.print(bl.getPercentageIdentity()+"/");
            }


        }else{
            fileWriter.print("\tNA\tNA\tNA");
        }
        fileWriter.print("\n");

        return 1;
    }



    private String writePrimerInputFile(List<Variation> vList, AmpliconSeq ampliconObj) throws FileNotFoundException {
        Date dt = new Date();
        String fileName="primerInp"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
        File primerInputFile = new File(dataDir+primer3InputDir+fileName);
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

        String answer;
        String errAnswer="NA";

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

                if(line.equals("Sorry, the BLAT/iPCR server seems to be down.  Please try again later") ||
                    line.equals("Error in TCP non-blocking connect() 61 - Connection refused") ||
                    line.equals("Couldn't connect to localhost 17779"))
                {
                    errAnswer+=line;
                }else{
                    sb.append(line).append("\n");
                }

            }
            answer = sb.toString();

            System.out.println(erranswer);

            System.out.println(answer);

            System.out.println("should have got an output from Primer3 and BLAT..");


            if(errAnswer.length()>2){
                throw new Exception("Exception: Blat/Insilico PCR server issues. The BLAT/Insilico PCR server may have " +
                        "not been started. Please start server using gfServer.");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;

    }


    public static String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getPrimer3InputDir() {
        return primer3InputDir;
    }

    public void setPrimer3InputDir(String primer3InputDir) {
        PrimerCreateController.primer3InputDir = primer3InputDir;
    }

    public String getPrimer3OpDir() {
        return primer3OpDir;
    }

    public void setPrimer3OpDir(String primer3OpDir) {
        PrimerCreateController.primer3OpDir = primer3OpDir;
    }

    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        PrimerCreateController.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        PrimerCreateController.blatOpDir = blatOpDir;
    }

    public String getInsilicoPcrInputDir() {
        return insilicoPcrInputDir;
    }

    public void setInsilicoPcrInputDir(String insilicoPcrInputDir) {
        PrimerCreateController.insilicoPcrInputDir = insilicoPcrInputDir;
    }

    public String getInsilicoPcrOpDir() {
        return insilicoPcrOpDir;
    }

    public void setInsilicoPcrOpDir(String insilicoPcrOpDir) {
        PrimerCreateController.insilicoPcrOpDir = insilicoPcrOpDir;
    }

    public String getPrimerProcessScriptDir() {
        return primerProcessScriptDir;
    }

    public void setPrimerProcessScriptDir(String primerProcessScriptDir) {
        PrimerCreateController.primerProcessScriptDir = primerProcessScriptDir;
    }


}
