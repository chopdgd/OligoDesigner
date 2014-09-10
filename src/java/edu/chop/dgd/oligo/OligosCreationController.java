package edu.chop.dgd.oligo;

<<<<<<< HEAD

import edu.chop.dgd.dgdObjects.OligoObject;
import edu.chop.dgd.dgdObjects.OligoObjectSubsections;
=======
import edu.chop.dgd.primer.PrimerReport;
import edu.chop.dgd.process.primerCreate.*;
import edu.chop.dgd.utils.BlatPsl;
import edu.chop.dgd.utils.InsilicoPCRObject;
import edu.chop.dgd.utils.Primer3Object;
import edu.chop.dgd.web.HttpRequestFacade;
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
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
<<<<<<< HEAD
 * Created by jayaramanp on 6/1/14.
 */
public class OligosCreationController implements Controller{

    private static String blatInpDir;
    private static String blatOpDir;
    private static String oligoProcessScriptDir;
    private static String dataDir;
    private static String oligoInputDir;
    private static String oligoOutputDir;
    private static String mfoldInpDir;
    private static String mfoldOpDir;

=======
 * Created by jayaramanp on 1/29/14.
 */
public class OligosCreationController implements Controller {


    private static String primer3InputDir;
    private static String primer3OpDir;
    private static String blatInpDir;
    private static String blatOpDir;
    private static String insilicoPcrInputDir;
    private static String insilicoPcrOpDir;
    private static String primerProcessScriptDir;
    private static String dataDir;
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

<<<<<<< HEAD
        String upFile = request.getParameter("uploadFolderPath");
        String projectId = request.getParameter("proj_id");
        File newFile = new File(upFile+"/"+projectId);
        File uploadedFiles[] = newFile.listFiles();
        File fileToParse = uploadedFiles[0];
        ArrayList<OligoObject> objects =  getObjectsFromFile(fileToParse);
        OligoObjectSubsections oss = new OligoObjectSubsections();
        String reportFile="";

        for(OligoObject o : objects){
            List<OligoObjectSubsections> osSubsList = o.generateSubsections();  //set subsection ID in the next section..
            String fullReportFileName = generateReport(osSubsList, projectId);
            String[] reportAndName = fullReportFileName.split("_", -1);
            reportFile = reportAndName[0];
            String fileName = reportAndName[1];
            List<OligoObjectSubsections> oligosSubsectionList = oss.retrieveResultsFromAnalyses(fileName, osSubsList, dataDir, oligoOutputDir);
            o.setOligoObjectSubsections(oligosSubsectionList);

        }

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/fileUpload.jsp");
        mvObj.addObject("uploadedPath", upFile);
        mvObj.addObject("reportFile", reportFile);
=======
        HttpRequestFacade req = new HttpRequestFacade(request);

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/fileUpload.jsp");


>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6

        return mvObj;

    }

<<<<<<< HEAD
    private String generateReport(List<OligoObjectSubsections> ampliconObjList, String projectId) throws Exception{

        Date dt = new Date();
        String fileN="oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());

        File oligoInputFile = new File(dataDir+oligoInputDir+fileN);
        PrintWriter pw = new PrintWriter(oligoInputFile);
        int counter = 0;

        for(OligoObjectSubsections oss : ampliconObjList){
            counter+=1;
            String subsectionId = "inpSeq_"+counter;
            oss.setSubsectionid(subsectionId);
            pw.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n=");
            System.out.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n=");
        }
        pw.flush();
        pw.close();

        String resultPrimer3Blat = runOligoProcessBuilder(fileN);
        System.out.println(resultPrimer3Blat);


        return resultPrimer3Blat+"_"+fileN;
=======


    private String createFiles(List<Primer3Object> primer3Primers, String response, String chr, int startPos, int stopPos, String dataDir, List<UcscGene> geneList, Map<UcscGene, UcscGeneExon> geneExonVariantMap, List<Variation> vList, AmpliconSeq ampliconObj, int varrelPos, List<Variation> variantsWithinAmpliconObjList) throws Exception {

        String path=chr+"_"+startPos+"_"+stopPos;
        File filePath = new File(dataDir+path+".detail.html");
        File softFile = new File(dataDir+path+"_soft.xls");
        File secondaryFile = new File(dataDir+path+"_secondary.xls");
        PrimerDAO prDao = new PrimerDAO();

        PrintWriter detailWriter = new PrintWriter(filePath);
        PrintWriter softWriter = new PrintWriter(softFile);
        PrintWriter secondaryFileWriter = new PrintWriter(secondaryFile);

        softWriter.print("Primer Name\tHuman Genome Build\tQuery Condition\tIn-Silico PCR\tprimer ID\tstart\tlen\ttm\tgc%\tany\t3'\tseq\tproductSize\tSNPs found\tChrom\tAmplicon Start\tAmplicon End\tfolder Id\tsnp check ID\tBLAT Percentage Identity\n");
        secondaryFileWriter.print("Primer Name\tHuman Genome Build\tQuery Condition\tIn-Silico PCR\tprimer ID\tstart\tlen\ttm\tgc%\tany\t3'\tseq\tproductSize\tSNPs found\tChrom\tAmplicon Start\tAmplicon End\tfolder Id\tsnp check ID\tBLAT Percentage Identity\n");

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
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6


    }

<<<<<<< HEAD


    private ArrayList<OligoObject> getObjectsFromFile(File fileToParse) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToParse)));
        ArrayList<OligoObject> oligoList = new ArrayList<OligoObject>();

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                String lineArr[] = line.split("\t", -1);
                System.out.println(line);
                OligoObject obj = new OligoObject();
                obj.setChr(lineArr[0]);
                obj.setStart(Integer.parseInt(lineArr[1]));
                obj.setStop(Integer.parseInt(lineArr[2]));

                oligoList.add(obj);

            }

        }finally {

            reader.close();

        }

        return oligoList;
    }


    public String runOligoProcessBuilder(String inputFileName) throws Exception {
=======
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


    private int writeFile(Primer3Object pr, PrintWriter fileWriter, String chr, int startPos, int stopPos) throws Exception {


        fileWriter.print(pr.getLeftPrimerId()+"\thg19\t"+chr+":"+startPos+"_"+stopPos+"\tPASS\t"+chr+":"+startPos+"_"+stopPos+"F\t"+pr.getLeftStart()+"\t"+pr.getLeftLen()+"\t"+pr.getLeftTm()+"\t"+pr.getLeftGc()+"\t"+pr.getLeftAny()+"\t"+pr.getLeft3()+"\t"+pr.getLeftSeq()+"\t");

        writeInsilicoPCRResultsInFile(fileWriter, pr);

        fileWriter.print("\tNA\tNA\t");

        if(pr.getLeftPrimerBlatList()!=null){

            String bls="";
            for(BlatPsl bl : pr.getLeftPrimerBlatList()){
                bls+=bl.getPercentageIdentity()+"/";
            }
            fileWriter.print(bls.substring(0, (bls.length()-1)));

        }else{

            fileWriter.print("\tNA\tNA\tNA");
        }
        fileWriter.print("\n");





        fileWriter.print(pr.getRightPrimerId()+"\thg19\t"+chr+":"+startPos+"_"+stopPos+"\tPASS\t"+chr+":"+startPos+"_"+stopPos+"R\t"+pr.getRightStart()+"\t"+pr.getRightLen()+"\t"+pr.getRightTm()+"\t"+pr.getRightGc()+"\t"+pr.getRightAny()+"\t"+pr.getRight3()+"\t"+pr.getRightSeq()+"\t");

        writeInsilicoPCRResultsInFile(fileWriter, pr);

        fileWriter.print("\tNA\tNA\t");

        if(pr.getRightPrimerBlatList()!=null){

            String bls="";
            for(BlatPsl bl : pr.getRightPrimerBlatList()){
                bls+=bl.getPercentageIdentity()+"/";
            }
            fileWriter.print(bls.substring(0, (bls.length()-1)));

        }else{
            fileWriter.print("\tNA\tNA\tNA");
        }
        fileWriter.print("\n");



        return 1;

    }




    public void writeInsilicoPCRResultsInFile(PrintWriter fileWriter, Primer3Object pr) throws Exception{



        if(pr.getInsilicoPCRObjectList()!=null && pr.getInsilicoPCRObjectList().size()>0){

            String isPcrs="";
            for(InsilicoPCRObject isPcr : pr.getInsilicoPCRObjectList()){
                isPcrs+=isPcr.getSize().replace("bp","") + "/";

            }
            fileWriter.print(isPcrs.substring(0, (isPcrs.length()-1)));
            fileWriter.print("\tN\t");

            String isPcr2s="";
            for(InsilicoPCRObject isPcr2 : pr.getInsilicoPCRObjectList()){
                isPcr2s+=isPcr2.getChr().replace("chr","") + "/";
            }
            fileWriter.print(isPcr2s.substring(0, (isPcr2s.length()-1)));
            fileWriter.print("\t");

            String isPcr3s="";
            for(InsilicoPCRObject isPcr3 : pr.getInsilicoPCRObjectList()){
                isPcr3s+=isPcr3.getPrimerSeqStart() + "/";
            }
            fileWriter.print(isPcr3s.substring(0, (isPcr3s.length()-1)));
            fileWriter.print("\t");

            String isPcr4s="";
            for(InsilicoPCRObject isPcr4 : pr.getInsilicoPCRObjectList()){
                isPcr4s+=isPcr4.getPrimerSeqEnd() + "/";
            }
            fileWriter.print(isPcr4s.substring(0, (isPcr4s.length()-1)));

        }else{
            fileWriter.print("NA\tN\tNA\tNA\tNA");
        }

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
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6

        String answer;
        String errAnswer="NA";

<<<<<<< HEAD
        ProcessBuilder pb = new ProcessBuilder(getOligoProcessScriptDir()+"OligoProcess.sh",inputFileName);
=======
        ProcessBuilder pb = new ProcessBuilder(getPrimerProcessScriptDir()+"PrimerProcess.sh",inputFileName);
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
<<<<<<< HEAD
        path += ":/usr/local/mfold/bin";
        path += ":/usr/local/mfold/share";
        env.put("PATH", path);

        pb.directory(new File(oligoProcessScriptDir));
=======
        env.put("PATH", path);

        pb.directory(new File(primerProcessScriptDir));
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
<<<<<<< HEAD
        System.out.println("should've initiated the oligoProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the oligoProcess.sh..");
=======
        System.out.println("should've initiated the primerProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the primerprocess.sh..");
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
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
<<<<<<< HEAD
                        line.equals("Error in TCP non-blocking connect() 61 - Connection refused") ||
                        line.equals("Couldn't connect to localhost 17779"))
=======
                    line.equals("Error in TCP non-blocking connect() 61 - Connection refused") ||
                    line.equals("Couldn't connect to localhost 17779"))
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
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
<<<<<<< HEAD
                throw new Exception("Exception: Blat server issues. The BLAT server may have " +
=======
                throw new Exception("Exception: Blat/Insilico PCR server issues. The BLAT/Insilico PCR server may have " +
>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
                        "not been started. Please start server using gfServer.");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;

    }


<<<<<<< HEAD

    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        OligosCreationController.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        OligosCreationController.blatOpDir = blatOpDir;
    }

    public static String getOligoProcessScriptDir() {
        return oligoProcessScriptDir;
    }

    public void setOligoProcessScriptDir(String oligoProcessScriptDir) {
        OligosCreationController.oligoProcessScriptDir = oligoProcessScriptDir;
    }

    public static String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        OligosCreationController.dataDir = dataDir;
    }

    public static String getOligoInputDir() {
        return oligoInputDir;
    }

    public void setOligoInputDir(String oligoInputDir) {
        OligosCreationController.oligoInputDir = oligoInputDir;
    }

    public static String getOligoOutputDir() {
        return oligoOutputDir;
    }

    public void setOligoOutputDir(String oligoOutputDir) {
        OligosCreationController.oligoOutputDir = oligoOutputDir;
    }

    public static String getMfoldInpDir() {
        return mfoldInpDir;
    }

    public void setMfoldInpDir(String mfoldInpDir) {
        OligosCreationController.mfoldInpDir = mfoldInpDir;
    }

    public static String getMfoldOpDir() {
        return mfoldOpDir;
    }

    public void setMfoldOpDir(String mfoldOpDir) {
        OligosCreationController.mfoldOpDir = mfoldOpDir;
    }
=======
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
        OligosCreationController.primer3InputDir = primer3InputDir;
    }

    public String getPrimer3OpDir() {
        return primer3OpDir;
    }

    public void setPrimer3OpDir(String primer3OpDir) {
        OligosCreationController.primer3OpDir = primer3OpDir;
    }

    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        OligosCreationController.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        OligosCreationController.blatOpDir = blatOpDir;
    }

    public String getInsilicoPcrInputDir() {
        return insilicoPcrInputDir;
    }

    public void setInsilicoPcrInputDir(String insilicoPcrInputDir) {
        OligosCreationController.insilicoPcrInputDir = insilicoPcrInputDir;
    }

    public String getInsilicoPcrOpDir() {
        return insilicoPcrOpDir;
    }

    public void setInsilicoPcrOpDir(String insilicoPcrOpDir) {
        OligosCreationController.insilicoPcrOpDir = insilicoPcrOpDir;
    }

    public String getPrimerProcessScriptDir() {
        return primerProcessScriptDir;
    }

    public void setPrimerProcessScriptDir(String primerProcessScriptDir) {
        OligosCreationController.primerProcessScriptDir = primerProcessScriptDir;
    }


>>>>>>> 9f9004643b5f328a04c5f4a8ee73dc1c0003e9d6
}
