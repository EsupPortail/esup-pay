package org.esupportail.pay.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DefaultNotFoundController {
	
	private final Logger log = Logger.getLogger(getClass());

    @RequestMapping("/**")
    public ModelAndView defaultNotFound(HttpServletRequest request) {
    	request.getRequestURL();
		ModelAndView mav = new ModelAndView();
		String url = request.getRequestURL().toString();
		if(request.getQueryString()!=null && !request.getQueryString().isEmpty()) {
			url += "?" + request.getQueryString();
		}
		mav.addObject("url", url);
		log.error("Request: " + url + " not found ");
		mav.setViewName("resourceNotFound");
		return mav;
    }
}
