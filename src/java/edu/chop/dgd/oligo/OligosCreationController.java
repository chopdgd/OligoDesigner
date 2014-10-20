package edu.chop.dgd.oligo;

import edu.chop.dgd.dgdObjects.MfoldDimer;
import edu.chop.dgd.dgdObjects.OligoObject;
import edu.chop.dgd.dgdObjects.SequenceObject;
import edu.chop.dgd.dgdObjects.SequenceObjectSubsections;
import org.apache.commons.io.FileUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private static String homodimerOpDir;
    private static String heterodimerInpDir;
    private static String heterodimerOpDir;
    private static String finalOligos;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        String upFile = request.getParameter("uploadFolderPath");
        String projectId = request.getParameter("proj_id");
        File newFile = new File(upFile+"/"+projectId);
        File uploadedFiles[] = newFile.listFiles();
        File fileToParse = uploadedFiles[0];
        ArrayList<SequenceObject> objects =  getObjectsFromFile(fileToParse);
        SequenceObjectSubsections soss = new SequenceObjectSubsections();
        String reportFile="";String heterodimerReport="";

        for(SequenceObject so : objects){
            //List<SequenceObjectSubsections> sosSubsList = so.generateSubsections();  //set subsection ID in the next section..
            //String fullReportFileName = generateReportSetSubsectionIds(sosSubsList, projectId);

            //testing new method
            String inpFilename = projectId+"_"+so.getChr()+":"+so.getStart()+"-"+so.getStop()+".txt";
            List<SequenceObjectSubsections> sosSubsList = so.generateSequenceSubsections(inpFilename, dataDir);
            String fullReportFileName = generateReportSetSubsectionIds(sosSubsList, projectId);


            String[] reportAndName = fullReportFileName.split("&", -1);
            reportFile = reportAndName[0];
            String fileName = reportAndName[1];
            List<SequenceObjectSubsections> oligosSubsectionList = soss.retrieveResultsFromAnalyses(fileName, sosSubsList, dataDir, oligoOutputDir, blatInpDir, blatOpDir, mfoldInpDir, mfoldOpDir, homodimerOpDir, heterodimerInpDir, heterodimerOpDir);
            HashMap<OligoObject,List<OligoObject>> heterodimerOligosHashMap = new MfoldDimer().FilterOligosRetrieveHeteroDimers(oligosSubsectionList, fileName, heterodimerInpDir, heterodimerOpDir, dataDir);
            HashMap<String, List<OligoObject>> hetDimerSets = new MfoldDimer().createMapSetsOfHets(heterodimerOligosHashMap, so);
            TreeMap<String, List<OligoObject>> hetDimerTreeMap = new MfoldDimer().sortOligosHetSetMinDeltaG(hetDimerSets);


            heterodimerReport = FileUtils.readFileToString(new File(dataDir + heterodimerOpDir + fileName + "_1_" + fileName + "_2.out"));
            reportFile+=heterodimerReport;

            String detailFile = dataDir+finalOligos+fileName+"_detail.html";
            String secondaryFile = dataDir+finalOligos+fileName+"_secondary.txt";

            so.setDetailsFile(detailFile);
            so.setSecondaryFile(secondaryFile);

            so.setOligoObjectSubsections(oligosSubsectionList);

            so.setReportFile(reportFile);
            so.setFilename(fileName);
            so.setHetDimerHashMap(heterodimerOligosHashMap);
            so.setOligoSetsMap(hetDimerSets);
            so.setOligoSetsTreeMap(hetDimerTreeMap);

        }

        String oligosFilename = writeOligosFinalFile(objects, dataDir, finalOligos, projectId);

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/fileUpload.jsp");
        mvObj.addObject("uploadedPath", upFile);
        mvObj.addObject("sequenceObjects", objects);
        mvObj.addObject("optimalOligosFile", oligosFilename);
        mvObj.addObject("projectId", projectId);

        return mvObj;

    }

    private String writeOligosFinalFile(ArrayList<SequenceObject> objects, String dataDir, String finalOligos, String projectId) throws Exception {


        String firstOptimalOligosFile = dataDir+finalOligos+projectId+"_primary.txt";
        File firstOligosFile = new File(firstOptimalOligosFile);
        PrintWriter pwFirst = new PrintWriter(firstOligosFile);
        pwFirst.println("Primer Set\tPrimer Id\tPrimerChr\tPrimer Start\tPrimer End\tSequence\tSequence Rev. Complement\tGC\tTm\tSize\tSelf Dimer\tHairpin Tm\tHairpin dG\tBlat");

        for(SequenceObject so : objects){

            String detailFileN=so.getDetailsFile();
            String secondaryOptimalOligosFile = so.getSecondaryFile();

            File detailFile = new File(detailFileN);
            File secondOligosFile = new File(secondaryOptimalOligosFile);

            PrintWriter pwDetail = new PrintWriter(detailFile);
            PrintWriter pwSecond = new PrintWriter(secondOligosFile);

            HashMap<String, List<OligoObject>> optimalOligos = so.getOligoSetsMap();
            TreeMap<String, List<OligoObject>> optimalOligosTree = so.getOligoSetsTreeMap();


            //write optimal oligos file

            for(OligoObject o : optimalOligos.get(optimalOligosTree.firstEntry().getKey().split("_")[0])){

                pwFirst.println(optimalOligosTree.firstEntry().getKey().split("_")[0] + "\t" + o.getInternalPrimerId() + "\t" + so.getChr() + "\t" + o.getInternalStart() + "\t" + Integer.parseInt(o.getInternalStart()) + o.getInternalLen() + "\t" + o.getInternalSeq() + "\t-\t"
                        + o.getInternalGc() + "\t" + o.getInternalTm() + "\t" + o.getInternalLen() + "\t" + o.getHomodimerValue() + "\t-\t" + o.getHairpinValue() + "\t" + o.getInternalPrimerBlatList().size());

            }



            //write detailed file
            pwDetail.println("<pre>"+so.getReportFile()+"</pre>");

            //write secondary file
            pwSecond.println("\"Primer Set\tPrimer Id\tPrimerChr\tPrimer Start\tPrimer End\tSequence\tSequence Rev. Complement\tGC\tTm\tSize\tSelf Dimer\tHairpin Tm\tHairpin dG\tBlat");
            for(String set : optimalOligosTree.keySet()){

                pwSecond.println("\n"+set);
                String key = set.split("_")[0];
                for(OligoObject o : optimalOligos.get(key)){

                    pwSecond.println(o.getInternalPrimerId() + "\t" + so.getChr() + "\t" + o.getInternalStart() + "\t" + Integer.parseInt(o.getInternalStart()) + o.getInternalLen() + "\t" + o.getInternalSeq() + "\t-\t"
                            + o.getInternalGc() + "\t" + o.getInternalTm() + "\t" + o.getInternalLen() + "\t" + o.getHomodimerValue() + "\t-\t" + o.getHairpinValue() + "\t" + o.getInternalPrimerBlatList().size());

                }

            }

            pwSecond.close();
            pwDetail.close();

        }

        pwFirst.close();
        

        return firstOptimalOligosFile;

    }




    private String generateReportSetSubsectionIds(List<SequenceObjectSubsections> ampliconObjList, String projectId) throws Exception{

        Date dt = new Date();
        String fileN="oligoInp_"+projectId+"_"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());

        File oligoInputFile = new File(dataDir+oligoInputDir+fileN);
        PrintWriter pw = new PrintWriter(oligoInputFile);
        int counter = 0;

        for(SequenceObjectSubsections oss : ampliconObjList){
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


        return resultPrimer3Blat+"&"+fileN;

    }


    private ArrayList<SequenceObject> getObjectsFromFile(File fileToParse) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToParse)));
        ArrayList<SequenceObject> oligoList = new ArrayList<SequenceObject>();

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                String lineArr[] = line.split("\t", -1);
                System.out.println(line);
                SequenceObject obj = new SequenceObject();
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

        String answer;
        String errAnswer="NA";

        ProcessBuilder pb = new ProcessBuilder(getOligoProcessScriptDir()+"OligoProcess.sh",inputFileName);
        //System.out.println( "environment before addition:"+pb.environment());
        Map<String, String> env = pb.environment();
        env.put("SHELL", "/bin/bash");
        String path = env.get("PATH");
        path += ":/usr/local/primer3";
        path += ":/usr/local/blat";
        path += ":/usr/local/mfold/bin";
        path += ":/usr/local/mfold/share";
        env.put("PATH", path);

        pb.directory(new File(oligoProcessScriptDir));

        pb.redirectErrorStream(true);
        System.out.println(pb.directory());
        System.out.println(pb.command());
        System.out.println(pb.environment());
        System.out.println("should've initiated the oligoProcess.sh..");
        try{
            Process p = pb.start();
            System.out.println("should be running the oligoProcess.sh..");

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

                throw new Exception("Exception: Blat server issues. The BLAT server may have " +

                        "not been started. Please start server using gfServer.");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return answer;

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

    public static String getHomodimerOpDir() {
        return homodimerOpDir;
    }

    public void setHomodimerOpDir(String homodimerOpDir) {
        OligosCreationController.homodimerOpDir = homodimerOpDir;
    }

    public static String getHeterodimerInpDir() {
        return heterodimerInpDir;
    }

    public void setHeterodimerInpDir(String heterodimerInpDir) {
        OligosCreationController.heterodimerInpDir = heterodimerInpDir;
    }

    public static String getHeterodimerOpDir() {
        return heterodimerOpDir;
    }

    public void setHeterodimerOpDir(String heterodimerOpDir) {
        OligosCreationController.heterodimerOpDir = heterodimerOpDir;
    }

    public static String getFinalOligos() {
        return finalOligos;
    }

    public void setFinalOligos(String finalOligos) {
        OligosCreationController.finalOligos = finalOligos;
    }
}
