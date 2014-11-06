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

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        String filePath = "/data/downloads/";
        System.out.println("#####"+filePath+"***");
        String fileResponse="";
        String fileName=""; String projectId="proj_id";

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {

            if (request instanceof MultipartHttpServletRequest)
            {
                MultipartHttpServletRequest multipartRequest =
                        (MultipartHttpServletRequest) request;
                projectId = multipartRequest.getParameter("proj_id");
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
                }
                // do the input processing
                System.out.println("true");
                fileResponse+="</body></head></html>";
            }

        }

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/fileUpload.jsp");
        mvObj.addObject("proj_id", projectId);
        mvObj.addObject("uploads", filePath);
        mvObj.addObject("origFilename", fileName);
        mvObj.addObject("fileUploadResponse", fileResponse);

        return mvObj;
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

}
