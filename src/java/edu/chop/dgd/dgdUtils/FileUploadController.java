package edu.chop.dgd.dgdUtils;

/**
 * Created by jayaramanp on 10/29/14.
 */


import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUploadController implements Controller{

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

    private static String spacing;
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

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        ArrayList error = new ArrayList();
        ArrayList warning = new ArrayList();
        ArrayList status = new ArrayList();

        String filePath = "/data/downloads/";
        System.out.println("#####"+filePath+"***");
        String fileResponse="";
        String fileName=""; String projectId="proj_id"; String assembly = "hg19";
        String exampleFile = "/data/antholigo_test/antholigo_test.txt";

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {

            if (request instanceof MultipartHttpServletRequest)
            {
                MultipartHttpServletRequest multipartRequest =
                        (MultipartHttpServletRequest) request;
                projectId = multipartRequest.getParameter("proj_id");

                assembly = multipartRequest.getParameter("assembly");

                spacing = multipartRequest.getParameter("oligo_seq_separation");


                String minGC = multipartRequest.getParameter("min_gc");
                if(minGC.length()>0){
                    min_gc = minGC;
                }else{
                    min_gc = "NA";
                }

                String maxGC = multipartRequest.getParameter("max_gc");
                if(maxGC.length()>0){
                    max_gc = maxGC;
                }else{
                    max_gc = "NA";
                }


                String optGC = multipartRequest.getParameter("opt_gc");
                if(optGC.length()>0){
                    opt_gc = optGC;
                }else{
                    opt_gc = "NA";
                }

                String minTm = request.getParameter("min_tm");
                if(optGC.length()>0){
                    min_tm = minTm;
                }else{
                    min_tm = "NA";
                }

                String maxTm = request.getParameter("max_tm");
                if(maxTm.length()>0){
                    max_tm = maxTm;
                }else{
                    max_tm = "NA";
                }

                String optTm = request.getParameter("opt_tm");
                if(optTm.length()>0){
                    opt_tm = optTm;
                }else{
                    opt_tm = "NA";
                }

                String minLen = request.getParameter("min_length");
                if(minLen.length()>0){
                    min_len = minLen;
                }else{
                    min_len = "NA";
                }

                String maxLen = request.getParameter("max_length");
                if(maxLen.length()>0){
                    max_len = maxLen;
                }else{
                    max_len = "NA";
                }

                String optLen = request.getParameter("opt_length");
                if(optLen.length()>0){
                    opt_len = optLen;
                }else{
                    opt_len = "NA";
                }

                String naIon = request.getParameter("na");
                if(naIon.length()>0){
                    na_ion = naIon;
                }else{
                    na_ion = "NA";
                }

                String mgIon = request.getParameter("mg");
                if(mgIon.length()>0){
                    mg_ion = mgIon;
                }else{
                    mg_ion = "NA";
                }

                String selfAny = request.getParameter("self_any");
                if(selfAny.length()>0){
                    self_any = selfAny;
                }else{
                    self_any = "NA";
                }

                String selfEnd = request.getParameter("self_end");
                if(selfEnd.length()>0){
                    self_end = selfEnd;
                }else{
                    self_end = "NA";
                }



                filePath = getDataDir()+getDownloadsDir();
                List<MultipartFile> multipartFiles = multipartRequest.getFiles("file");

                fileResponse+="<html><head><body><div>The following files have been uploaded:</div>";

                if(multipartFiles.size()>0){
                    for(MultipartFile file : multipartFiles){
                        InputStream input = file.getInputStream();
                        File uploadDir = new File(filePath+projectId+"/");
                        if(!uploadDir.exists()){
                            if(uploadDir.mkdir()){
                                System.out.println("new dir created..");
                            }else{
                                System.out.println("mkdir directory didnt work.. something wrong. ");
                                error.add("Cannot seem to create project Directory using 'mkdir'. Permissions issue.");
                            }
                        }else{
                            System.out.println("old dir exists..");
                        }

                        fileName = file.getOriginalFilename();
                        File uploadedFile = new File(uploadDir+"/"+fileName);
                        OutputStream out = new FileOutputStream(uploadedFile);
                        IOUtils.copy(input, out);
                        input.close();
                        out.close();

                        fileResponse+="<div>"+file.getOriginalFilename()+"</div>";
                    }
                }else{
                    fileResponse+="<div>No files found!</div>";
                    error.add("File not found. please check what went wrong");
                }
                // do the input processing
                System.out.println("true");
                fileResponse+="</body></head></html>";
            }

        }

        if(error.size()>0){
            ModelAndView mvErr = new ModelAndView("/WEB-INF/pages/oligo/home.jsp");
            mvErr.addObject("exampleFile", exampleFile);
            mvErr.addObject("error", error);
            return mvErr;
        }
        else{
            ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/fileUpload.jsp");
            mvObj.addObject("proj_id", projectId);
            mvObj.addObject("assembly", assembly);
            mvObj.addObject("uploads", filePath);
            mvObj.addObject("origFilename", fileName);
            mvObj.addObject("fileUploadResponse", fileResponse);
            mvObj.addObject("separation", spacing);
            mvObj.addObject("minGC", min_gc);
            mvObj.addObject("optGC", opt_gc);
            mvObj.addObject("maxGC", max_gc);
            mvObj.addObject("maxTm", max_tm);
            mvObj.addObject("minTm", min_tm);
            mvObj.addObject("optTm", opt_tm);
            mvObj.addObject("maxLen", max_len);
            mvObj.addObject("minLen", min_len);
            mvObj.addObject("optLen", opt_len);
            mvObj.addObject("naIon", na_ion);
            mvObj.addObject("mgIon", mg_ion);
            mvObj.addObject("selfAny", self_any);
            mvObj.addObject("selfEnd", self_end);

        return mvObj;

        }
    }




    public String getBlatInpDir() {
        return blatInpDir;
    }

    public void setBlatInpDir(String blatInpDir) {
        FileUploadController.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        FileUploadController.blatOpDir = blatOpDir;
    }

    public static String getOligoProcessScriptDir() {
        return oligoProcessScriptDir;
    }

    public void setOligoProcessScriptDir(String oligoProcessScriptDir) {
        FileUploadController.oligoProcessScriptDir = oligoProcessScriptDir;
    }

    public static String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        FileUploadController.dataDir = dataDir;
    }

    public static String getOligoInputDir() {
        return oligoInputDir;
    }

    public void setOligoInputDir(String oligoInputDir) {
        FileUploadController.oligoInputDir = oligoInputDir;
    }

    public static String getOligoOutputDir() {
        return oligoOutputDir;
    }

    public void setOligoOutputDir(String oligoOutputDir) {
        FileUploadController.oligoOutputDir = oligoOutputDir;
    }

    public static String getMfoldInpDir() {
        return mfoldInpDir;
    }

    public void setMfoldInpDir(String mfoldInpDir) {
        FileUploadController.mfoldInpDir = mfoldInpDir;
    }

    public static String getMfoldOpDir() {
        return mfoldOpDir;
    }

    public void setMfoldOpDir(String mfoldOpDir) {
        FileUploadController.mfoldOpDir = mfoldOpDir;
    }

    public static String getHomodimerOpDir() {
        return homodimerOpDir;
    }

    public void setHomodimerOpDir(String homodimerOpDir) {
        FileUploadController.homodimerOpDir = homodimerOpDir;
    }

    public static String getHeterodimerInpDir() {
        return heterodimerInpDir;
    }

    public void setHeterodimerInpDir(String heterodimerInpDir) {
        FileUploadController.heterodimerInpDir = heterodimerInpDir;
    }

    public static String getHeterodimerOpDir() {
        return heterodimerOpDir;
    }

    public void setHeterodimerOpDir(String heterodimerOpDir) {
        FileUploadController.heterodimerOpDir = heterodimerOpDir;
    }

    public static String getFinalOligos() {
        return finalOligos;
    }

    public void setFinalOligos(String finalOligos) {
        FileUploadController.finalOligos = finalOligos;
    }

    public static String getDownloadsDir() {
        return downloadsDir;
    }

    public void setDownloadsDir(String downloadsDir) {
        FileUploadController.downloadsDir = downloadsDir;
    }

    public static String getSpacing() {
        return spacing;
    }

    public static void setSpacing(String spacing) {
        FileUploadController.spacing = spacing;
    }

    public static String getMin_gc() {
        return min_gc;
    }

    public static void setMin_gc(String min_gc) {
        FileUploadController.min_gc = min_gc;
    }

    public static String getMax_gc() {
        return max_gc;
    }

    public static void setMax_gc(String max_gc) {
        FileUploadController.max_gc = max_gc;
    }

    public static String getOpt_gc() {
        return opt_gc;
    }

    public static void setOpt_gc(String opt_gc) {
        FileUploadController.opt_gc = opt_gc;
    }

    public static String getMin_tm() {
        return min_tm;
    }

    public static void setMin_tm(String min_tm) {
        FileUploadController.min_tm = min_tm;
    }

    public static String getOpt_tm() {
        return opt_tm;
    }

    public static void setOpt_tm(String opt_tm) {
        FileUploadController.opt_tm = opt_tm;
    }

    public static String getMax_tm() {
        return max_tm;
    }

    public static void setMax_tm(String max_tm) {
        FileUploadController.max_tm = max_tm;
    }

    public static String getMin_len() {
        return min_len;
    }

    public static void setMin_len(String min_len) {
        FileUploadController.min_len = min_len;
    }

    public static String getOpt_len() {
        return opt_len;
    }

    public static void setOpt_len(String opt_len) {
        FileUploadController.opt_len = opt_len;
    }

    public static String getMax_len() {
        return max_len;
    }

    public static void setMax_len(String max_len) {
        FileUploadController.max_len = max_len;
    }

    public static String getNa_ion() {
        return na_ion;
    }

    public static void setNa_ion(String na_ion) {
        FileUploadController.na_ion = na_ion;
    }

    public static String getMg_ion() {
        return mg_ion;
    }

    public static void setMg_ion(String mg_ion) {
        FileUploadController.mg_ion = mg_ion;
    }

    public static String getSelf_any() {
        return self_any;
    }

    public static void setSelf_any(String self_any) {
        FileUploadController.self_any = self_any;
    }

    public static String getSelf_end() {
        return self_end;
    }

    public static void setSelf_end(String self_end) {
        FileUploadController.self_end = self_end;
    }
}
