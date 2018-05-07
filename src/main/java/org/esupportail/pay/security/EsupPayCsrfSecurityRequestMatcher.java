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
package org.esupportail.pay.security;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

@Service
public class EsupPayCsrfSecurityRequestMatcher implements RequestMatcher {

	private final Logger log = Logger.getLogger(getClass());
	  
	private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
	
	@Override
	public boolean matches(HttpServletRequest request) {
		
        if(allowedMethods.matcher(request.getMethod()).matches()){
            return false;
        }
        
        if(request.getParameter("confid") != null) {
        	try {
	        	String servletPath = request.getServletPath();
	        	List<String> pathes = Arrays.asList(servletPath.split("/"));
	        	String mntUrlId = pathes.get(pathes.size()-1);
	        	String evtUrlId = pathes.get(pathes.size()-2);
	        	List<PayEvt> evts = PayEvt.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
	        	PayEvt payevt = evts.get(0);
	        	List<PayEvtMontant> evtsMnts = PayEvtMontant.findPayEvtMontantsByEvtAndUrlIdEquals(payevt, mntUrlId).getResultList();
	        	PayEvtMontant payevtmontant = evtsMnts.get(0);
	        	if(payevtmontant.getSciencesconf()) {
	        		log.info("sciencesconf 'redirect post' request -> we don't use csrf here");      	
	        		return false;
	        	}
        	} catch(Exception e) {
        		log.warn("it seems not to be sciencesconf 'redirect post' request even if there is a confid param ... we don't use csrf here");
        	}
        }
        
        return true;
	}

}
