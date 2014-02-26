package edu.chop.dgd.primer;

import edu.chop.dgd.process.primerCreate.*;
import edu.chop.dgd.web.HttpRequestFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jayaramanp on 1/29/14.
 */
public class PrimerCreateController implements Controller {
    private String primer3InputDir="/data/primer3Inp/";
    private String primer3OpDir="data/primer3Op/";
    private String blatInpDir="/data/blatInp/";
    private String blatOpDir="/data/blatOp/";
    private String primerProcessScriptDir="/data/";

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        UcscDAS ucDas = new UcscDAS();

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

        AmpliconSeq ampliconObj = new AmpliconSeq().createAmpliconObject(vList.get(0), geneExonVariantMap);

        AmpliconXomAnalyzer xom = new AmpliconXomAnalyzer();
        String sequence = xom.parseRecord(ampliconObj.getSequence());
        System.out.println("here is the sequence:"+sequence);
        ampliconObj.setSequence(sequence);

        Date dt = new Date();
        File primerInputFile = new File(primer3InputDir+"primerInp"+ new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date()));
        PrintWriter pw = new PrintWriter(primerInputFile);
        String relSeq = (vList.get(0).getVstart()-ampliconObj.getBufferUpstream());
        pw.println("SEQUENCE_ID=inpSeq1\nSEQUENCE_TEMPLATE="+sequence.replaceAll("\n","")+"\n=SEQUENCE_TARGET="+);
        pw.flush();
        pw.close();

        String response = runProcessBuilder();

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/primer/primerReport.jsp");
        mvObj.addObject("geneList", geneList);
        mvObj.addObject("geneExonMap", geneExonVariantMap);
        mvObj.addObject("chr", chr);
        mvObj.addObject("start",startPos);
        mvObj.addObject("stop", stopPos);
        mvObj.addObject("variantsList", vList);
        mvObj.addObject("amplicon", ampliconObj);
        mvObj.addObject("primerResults", response);
        return mvObj;

    }

    public String runProcessBuilder() throws Exception {

        String answer = "output:";

        ProcessBuilder pb = new ProcessBuilder(getPrimerProcessScriptDir()+"PrimerProcess.sh");
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
}
