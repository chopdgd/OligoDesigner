package edu.chop.dgd.oligo;


import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jayaramanp on 6/1/14.
 */
public class FileUploadController  implements Controller{

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {

        String upFile = request.getParameter("uploadFolderPath");
        String projectId = request.getParameter("proj_id");

        ModelAndView mvObj = new ModelAndView("/WEB-INF/pages/oligo/fileUpload.jsp");
        mvObj.addObject("uploadedPath", upFile);
        return mvObj;
    }
}
