package edu.chop.dgd.dgdUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by jayaramanp on 3/31/14.
 */


public class FileDownloadController extends HttpServlet{

    /**
     * Size of a byte buffer to read/write file
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Path of the file to be downloaded, relative to application's directory
     */


    private static String dataDir;
    private static String primer3InputDir;
    private static String primer3OpDir;
    private static String blatInpDir;
    private static String blatOpDir;
    private static String insilicoPcrInputDir;
    private static String insilicoPcrOpDir;
    private static String primerProcessScriptDir;


    /**
     * Method for handling file download request from client
     */
    @RequestMapping(method = RequestMethod.GET)
    public void doDownload( HttpServletRequest request,
                           HttpServletResponse response) throws IOException {

        String fileDownload = request.getParameter("file");

        // get absolute path of the application

        ServletContext context = request.getServletContext();

        // construct the complete absolute path of the file
        String fullPath = dataDir+fileDownload;
        File downloadFile = new File(fullPath);
        FileInputStream inputStream = new FileInputStream(downloadFile);

        // get MIME type of the file
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();

    }


    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        FileDownloadController.dataDir = dataDir;
    }

    public String getPrimer3InputDir() {
        return primer3InputDir;
    }

    public void setPrimer3InputDir(String primer3InputDir) {
        FileDownloadController.primer3InputDir = primer3InputDir;
    }

    public String getPrimer3OpDir() {
        return primer3OpDir;
    }

    public void setPrimer3OpDir(String primer3OpDir) {
        FileDownloadController.primer3OpDir = primer3OpDir;
    }

    public String getBlatInpDir() {
        return blatInpDir;
    }

    public  void setBlatInpDir(String blatInpDir) {
        FileDownloadController.blatInpDir = blatInpDir;
    }

    public String getBlatOpDir() {
        return blatOpDir;
    }

    public void setBlatOpDir(String blatOpDir) {
        FileDownloadController.blatOpDir = blatOpDir;
    }

    public String getInsilicoPcrInputDir() {
        return insilicoPcrInputDir;
    }

    public void setInsilicoPcrInputDir(String insilicoPcrInputDir) {
        FileDownloadController.insilicoPcrInputDir = insilicoPcrInputDir;
    }

    public String getInsilicoPcrOpDir() {
        return insilicoPcrOpDir;
    }

    public void setInsilicoPcrOpDir(String insilicoPcrOpDir) {
        FileDownloadController.insilicoPcrOpDir = insilicoPcrOpDir;
    }

    public String getPrimerProcessScriptDir() {
        return primerProcessScriptDir;
    }

    public void setPrimerProcessScriptDir(String primerProcessScriptDir) {
        FileDownloadController.primerProcessScriptDir = primerProcessScriptDir;
    }


}

