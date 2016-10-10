package edu.chop.dgd.oligo;

import edu.chop.dgd.dgdObjects.MfoldDimer;
import edu.chop.dgd.dgdObjects.OligoObject;
import edu.chop.dgd.dgdObjects.SequenceObject;
import edu.chop.dgd.dgdObjects.SequenceObjectSubsections;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.template.SequenceView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**

 * Created by jayaramanp on 6/1/14.
 */
public class OligosCreationController implements Controller{

    private static String blatInpDir;
    private static String blatOpDir;
    private static String oligoProcessScriptDir;
    private static String dataDir;
    private static String downloadsDir;
    private static String oligoInputDir;
    private static String oligoOutputDir;
    private static String mfoldInpDir;
    private static String mfoldOpDir;
    private static String homodimerOpDir;
    private static String heterodimerInpDir;
    private static String heterodimerOpDir;
    private static String finalOligos;

    private static String min_gc;
    private static String max_gc;
    private static String opt_gc;
    private static String min_tm;
    private static String opt_tm;
    private static String max_tm;
    private static String min_len;
    private static String opt_len;
    private static String max_len;
    private static String na_ion;
    private static String mg_ion;
    private static String self_any;
    private static String self_end;
    private static int spacing=0;





    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        ArrayList error = new ArrayList();
        ArrayList warning = new ArrayList();
        ArrayList status = new ArrayList();
        String exampleFile = "/data/antholigo_test/antholigo_test.txt";

        String upFile = request.getParameter("uploadFolderPath");
        String projectId = request.getParameter("proj_id");
        String assembly = request.getParameter("assembly");

        String origFileName = request.getParameter("origFile");

        String spacing2 = request.getParameter("separation");
        spacing = Integer.parseInt(spacing2);

        String minGC = request.getParameter("minGC");
        if(minGC.length()>0){
            min_gc = minGC;
        }else{
            min_gc = "NA";
        }

        String maxGC = request.getParameter("maxGC");
        if(maxGC.length()>0){
            max_gc = maxGC;
        }else{
            max_gc = "NA";
        }


        String optGC = request.getParameter("optGC");
        if(optGC.length()>0){
            opt_gc = optGC;
        }else{
            opt_gc = "NA";
        }

        String minTm = request.getParameter("minTm");
        if(minTm.length()>0){
            min_tm = minTm;
        }else{
            min_tm = "NA";
        }

        String maxTm = request.getParameter("maxTm");
        if(maxTm.length()>0){
            max_tm = maxTm;
        }else{
            max_tm = "NA";
        }

        String optTm = request.getParameter("optTm");
        if(optTm.length()>0){
            opt_tm = optTm;
        }else{
            opt_tm = "NA";
        }

        String minLen = request.getParameter("minLen");
        if(minLen.length()>0){
            min_len = minLen;
        }else{
            min_len = "NA";
        }

        String maxLen = request.getParameter("maxLen");
        if(maxLen.length()>0){
            max_len = maxLen;
        }else{
            max_len = "NA";
        }

        String optLen = request.getParameter("optLen");
        if(optLen.length()>0){
            opt_len = optLen;
        }else{
            opt_len = "NA";
        }

        String naIon = request.getParameter("Na");
        if(naIon.length()>0){
            na_ion = naIon;
        }else{
            na_ion = "NA";
        }

        String mgIon = request.getParameter("Mg");
        if(mgIon.length()>0){
            mg_ion = mgIon;
        }else{
            mg_ion = "NA";
        }

        String selfAny = request.getParameter("selfAny");
        if(selfAny.length()>0){
            self_any = selfAny;
        }else{
            self_any = "NA";
        }

        String selfEnd = request.getParameter("selfEnd");
        if(selfEnd.length()>0){
            self_end = selfEnd;
        }else{
            self_end = "NA";
        }


        //File uploadedFiles[] = newFile.listFiles();
        //File fileToParse = uploadedFiles[0];
        File fileToParse = new File(upFile+projectId+"/"+origFileName);
        ArrayList<SequenceObject> objects =  getObjectsFromFile(fileToParse, error, assembly);
        SequenceObjectSubsections soss = new SequenceObjectSubsections();
        String reportFile="";String heterodimerReport="";

        for(SequenceObject so : objects){
            //List<SequenceObjectSubsections> sosSubsList = so.generateSubsections();  //set subsection ID in the next section..
            //String fullReportFileName = generateReportSetSubsectionIds(sosSubsList, projectId);

            //testing new method
            String inpFilename = assembly+"_"+projectId+"_"+so.getChr()+":"+so.getStart()+"-"+so.getStop()+".txt";
            List<SequenceObjectSubsections> sosSubsList = so.generateSequenceSubsections(inpFilename, dataDir);
            String fullReportFileName = generateReportSetSubsectionIds(sosSubsList, projectId);


            String[] reportAndName = fullReportFileName.split("&", -1);
            reportFile = reportAndName[0];
            String fileName = reportAndName[1];
            List<SequenceObjectSubsections> oligosSubsectionList = soss.retrieveResultsFromAnalyses(fileName, sosSubsList, dataDir, oligoOutputDir, blatInpDir, blatOpDir, mfoldInpDir, mfoldOpDir, homodimerOpDir, heterodimerInpDir, heterodimerOpDir);
            System.out.println("filtering oligos retrieving results");
            HashMap<OligoObject,List<OligoObject>> heterodimerOligosHashMap = new MfoldDimer().FilterOligosRetrieveHeteroDimers(oligosSubsectionList, fileName, heterodimerInpDir, heterodimerOpDir, dataDir);
            System.out.println("creating map sets of hets");
            HashMap<String, List<OligoObject>> hetDimerSets = new MfoldDimer().createMapSetsOfHets(heterodimerOligosHashMap, so, spacing);
            System.out.println("sorting the oligos");
            TreeMap<String, List<OligoObject>> hetDimerTreeMap = new MfoldDimer().sortOligosHetSetMinDeltaG(hetDimerSets);

            //heterodimerReport = FileUtils.readFileToString(new File(dataDir + heterodimerOpDir + fileName + "_1_" + fileName + "_2.out"));
            /*LineIterator it = FileUtils.lineIterator(new File(dataDir + heterodimerOpDir + fileName + "_1_" + fileName + "_2.out"), "UTF-8");
            try {
                while (it.hasNext()) {
                    String line = it.nextLine();
                    // do something with line
                    reportFile+=line;
                }
            } finally {
                LineIterator.closeQuietly(it);
            }*/



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

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/processOligos.jsp");
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
            pwFirst.println("For query region: "+so.getChr()+":"+so.getStart()+"-"+so.getStop());

            for(OligoObject o : optimalOligos.get(optimalOligosTree.firstEntry().getKey().split("_")[0])){

                DNASequence seq = new DNASequence(o.getInternalSeq());
                SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                String revCompSeq = revcomp.getSequenceAsString();

                pwFirst.println(optimalOligosTree.firstEntry().getKey().split("_")[0] + "\t" + o.getInternalPrimerId() + "\t" + so.getChr() + "\t" + o.getInternalStart() + "\t" + (Integer.parseInt(o.getInternalStart())+o.getInternalLen()) + "\t" + o.getInternalSeq() + "\t"+ revCompSeq +"\t"
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

                    DNASequence seq = new DNASequence(o.getInternalSeq());
                    SequenceView<NucleotideCompound> revcomp = seq.getReverseComplement();
                    String revCompSeq = revcomp.getSequenceAsString();

                    pwSecond.println(o.getInternalPrimerId() + "\t" + so.getChr() + "\t" + o.getInternalStart() + "\t" + (Integer.parseInt(o.getInternalStart())+o.getInternalLen()) + "\t" + o.getInternalSeq() + "\t"+revCompSeq+"\t"
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
            pw.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n"+
                    "PRIMER_INTERNAL_MAX_TM="+max_tm+"\nPRIMER_INTERNAL_OPT_TM="+opt_tm+"\nPRIMER_INTERNAL_MIN_TM="+min_tm+"\n"+
                    "PRIMER_INTERNAL_MAX_GC="+max_gc+"\nPRIMER_INTERNAL_OPT_GC_PERCENT="+opt_gc+"\nPRIMER_INTERNAL_MIN_GC="+min_gc+"\n"+
                    "PRIMER_INTERNAL_MAX_SIZE="+max_len+"\nPRIMER_INTERNAL_OPT_SIZE="+opt_len+"\nPRIMER_INTERNAL_MIN_SIZE="+min_len+"\n"+
                    "PRIMER_INTERNAL_SALT_MONOVALENT="+na_ion+"\nPRIMER_INTERNAL_SALT_DIVALENT="+mg_ion+"\nPRIMER_INTERNAL_MAX_SELF_ANY="+self_any+"\n"+
                    "PRIMER_INTERNAL_MAX_SELF_END="+self_end+"\n=");
            System.out.println("SEQUENCE_ID="+subsectionId+"\nSEQUENCE_TEMPLATE="+oss.getSubSectionSequence()+"\n"+
                    "PRIMER_INTERNAL_MAX_TM="+max_tm+"\nPRIMER_INTERNAL_OPT_TM="+opt_tm+"\nPRIMER_INTERNAL_MIN_TM="+min_tm+"\n"+
                    "PRIMER_INTERNAL_MAX_GC="+max_gc+"\nPRIMER_INTERNAL_OPT_GC=PERCENT"+opt_gc+"\nPRIMER_INTERNAL_MIN_GC="+min_gc+"\n"+
                 "PRIMER_INTERNAL_MAX_SIZE="+max_len+"\nPRIMER_INTERNAL_OPT_SIZE="+opt_len+"\nPRIMER_INTERNAL_MIN_SIZE="+min_len+"\n"+
                "PRIMER_INTERNAL_SALT_MONOVALENT="+na_ion+"\nPRIMER_INTERNAL_SALT_DIVALENT="+mg_ion+"\nPRIMER_INTERNAL_MAX_SELF_ANY="+self_any+"\n"+
                "PRIMER_INTERNAL_MAX_SELF_END="+self_end+"\n=");
        }

        pw.flush();
        pw.close();

        String resultPrimer3Blat = runOligoProcessBuilder(fileN);
        System.out.println(resultPrimer3Blat);

        return resultPrimer3Blat+"&"+fileN;

    }


    private ArrayList<SequenceObject> getObjectsFromFile(File fileToParse, ArrayList error, String assembly) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToParse)));
        ArrayList<SequenceObject> oligoList = new ArrayList<SequenceObject>();

        try{
            String line;
            while((line=reader.readLine()) != null){
                //line = reader.readLine();
                String lineArr[] = line.split("\t", -1);
                System.out.println(line);
                SequenceObject obj = new SequenceObject();
                obj.setAssembly(assembly);
                obj.setChr(lineArr[0]);
                obj.setStart(Integer.parseInt(lineArr[1]));
                obj.setStop(Integer.parseInt(lineArr[2]));

                oligoList.add(obj);

            }

        }catch (Exception e){
            error.add("Something went wrong wile parsing file");
            error.add(e.getStackTrace());
        }
        finally {

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

    public static String getDownloadsDir() {
        return downloadsDir;
    }

    public void setDownloadsDir(String downloadsDir) {
        OligosCreationController.downloadsDir = downloadsDir;
    }

    public static String getMin_gc() {
        return min_gc;
    }

    public static void setMin_gc(String min_gc) {
        OligosCreationController.min_gc = min_gc;
    }

    public static String getMax_gc() {
        return max_gc;
    }

    public static void setMax_gc(String max_gc) {
        OligosCreationController.max_gc = max_gc;
    }

    public static String getOpt_gc() {
        return opt_gc;
    }

    public static void setOpt_gc(String opt_gc) {
        OligosCreationController.opt_gc = opt_gc;
    }

    public static String getMin_tm() {
        return min_tm;
    }

    public static void setMin_tm(String min_tm) {
        OligosCreationController.min_tm = min_tm;
    }

    public static String getOpt_tm() {
        return opt_tm;
    }

    public static void setOpt_tm(String opt_tm) {
        OligosCreationController.opt_tm = opt_tm;
    }

    public static String getMax_tm() {
        return max_tm;
    }

    public static void setMax_tm(String max_tm) {
        OligosCreationController.max_tm = max_tm;
    }

    public static String getMin_len() {
        return min_len;
    }

    public static void setMin_len(String min_len) {
        OligosCreationController.min_len = min_len;
    }

    public static String getOpt_len() {
        return opt_len;
    }

    public static void setOpt_len(String opt_len) {
        OligosCreationController.opt_len = opt_len;
    }

    public static String getMax_len() {
        return max_len;
    }

    public static void setMax_len(String max_len) {
        OligosCreationController.max_len = max_len;
    }

    public static String getNa_ion() {
        return na_ion;
    }

    public static void setNa_ion(String na_ion) {
        OligosCreationController.na_ion = na_ion;
    }

    public static String getMg_ion() {
        return mg_ion;
    }

    public static void setMg_ion(String mg_ion) {
        OligosCreationController.mg_ion = mg_ion;
    }

    public static String getSelf_any() {
        return self_any;
    }

    public static void setSelf_any(String self_any) {
        OligosCreationController.self_any = self_any;
    }

    public static String getSelf_end() {
        return self_end;
    }

    public static void setSelf_end(String self_end) {
        OligosCreationController.self_end = self_end;
    }

    public static int getSpacing() {
        return spacing;
    }

    public static void setSpacing(int spacing) {
        OligosCreationController.spacing = spacing;
    }
}
