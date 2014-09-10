package edu.chop.dgd.oligo;

import edu.chop.dgd.dgdObjects.OligoObject;
import edu.chop.dgd.dgdObjects.OligoObjectSubsections;
import edu.chop.dgd.process.primerCreate.AmpliconSeq;
import edu.chop.dgd.process.primerCreate.Variation;
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

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

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


        return mvObj;

    }

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

    }


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


    private String writePrimerInputFile(List<Variation> vList, AmpliconSeq ampliconObj) throws FileNotFoundException {
        Date dt = new Date();
        String fileName="primerInp"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date());
        File primerInputFile = new File(dataDir+oligoInputDir+fileName);
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

}
