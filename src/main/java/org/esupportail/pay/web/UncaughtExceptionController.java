/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.pay.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.esupportail.pay.exceptions.EntityNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

@Controller
public class UncaughtExceptionController extends AbstractHandlerExceptionResolver {

	private final Logger log = Logger.getLogger(getClass());
	
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		request.getRequestURL();
		ModelAndView mav = new ModelAndView();
		mav.addObject("exception", ex);
		String url = getFullRequestURL(request);
		if(request.getQueryString()!=null && !request.getQueryString().isEmpty()) {
			url += "?" + request.getQueryString();
		}
		mav.addObject("url", url);
		if(ex instanceof DataAccessException) {
			log.error("Request: " + url + " raised " + ex.getMessage());
			mav.setViewName("dataAccessFailure");
		} else if(ex instanceof EntityNotFoundException) {
			log.warn("Request: " + url + " raised " + ex.getMessage());
			mav.setViewName("resourceNotFound");
		} else {
			log.error("Request: " + url + " raised " + ex.getMessage());
			mav.setViewName("uncaughtException");
		}
		log.trace("Request: " + url + " raised " + ex, ex);
		return mav;
	}
	
	@RequestMapping(value = "/uncaughtException")
    public ModelAndView uncaughtException(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getRequestURL();
		ModelAndView mav = new ModelAndView();
		String url = getFullRequestURL(request);
		mav.addObject("url", url);
		log.error("Request: " + url + " raised uncaught exception ");
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		mav.setViewName("uncaughtException");
		return mav;
	}

	private String getFullRequestURL(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		if(request.getQueryString()!=null && !request.getQueryString().isEmpty()) {
			url += "?" + request.getQueryString();
		}
		return url;
	}
	

	@RequestMapping(value = "/resourceNotFound")
    public ModelAndView resourceNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getRequestURL();
		ModelAndView mav = new ModelAndView();
		String url = getFullRequestURL(request);
		mav.addObject("url", url);
		log.warn("Request: " + url + " not found");
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		mav.setViewName("resourceNotFound");
		return mav;
	}
	
	
	
	/**
	 * Workaround : <mvc:view-controller path="/denied"/> ne fonctionne que pour du GET 
	 */
	@RequestMapping(value = "/denied")
    public String handelErrorDenied(final HttpServletResponse response) {
		 response.setContentType("text/html");
		 return "denied";
    }
	
}

