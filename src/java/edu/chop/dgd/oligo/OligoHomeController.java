package edu.chop.dgd.oligo;

import edu.chop.dgd.web.HttpRequestFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        HttpSession session = request.getSession();

        //uncommment later
        //session.setAttribute();

        HttpRequestFacade req = new HttpRequestFacade(request);

        //status.add("oligo Design homepage?");
        String exampleFile = "/data/antholigo_test/antholigo_test.txt";

        return new ModelAndView("/WEB-INF/pages/oligo/home.jsp", "exampleFile", exampleFile);
    }
}
