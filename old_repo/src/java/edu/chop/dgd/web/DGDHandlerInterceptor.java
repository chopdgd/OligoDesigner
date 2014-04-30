package edu.chop.dgd.web;

/**
 * Created by jayaramanp on 1/23/14.
 */

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *  DGD web application HandlerInterceptorAdaptor.  All dgd requests execute this class.
 */

public class DGDHandlerInterceptor extends HandlerInterceptorAdapter {

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        System.out.println(request.getRequestURI());
        System.out.println(request.getRemoteAddr());
        if (request.getRequestURI().indexOf(".jsp") != -1) {
            response.getWriter().println("Can't call jsp");
            return;
        }
        System.out.println("in interceptor");
    }

}

