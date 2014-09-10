package edu.chop.dgd.oligo;

import edu.chop.dgd.web.HttpRequestFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * Created by jayaramanp on 1/22/14.
 */
public class OligoHomeController implements Controller{


    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
        ArrayList error = new ArrayList();
        ArrayList warning = new ArrayList();
        ArrayList status = new ArrayList();

        String uploadFilePath = "/data/downloads";

        HttpRequestFacade req = new HttpRequestFacade(request);
        //status.add("oligo Design homepage?");

        ModelAndView mv = new ModelAndView("/WEB-INF/pages/oligo/home.jsp", "message", "Design oligos for given positions.");
        mv.addObject("uploads", uploadFilePath);

        return mv;
    }
}
