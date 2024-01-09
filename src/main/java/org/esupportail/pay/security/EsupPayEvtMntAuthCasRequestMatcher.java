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

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.PayEvtMontantDaoService;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

@Service
public class EsupPayEvtMntAuthCasRequestMatcher implements RequestMatcher {

	private final Logger log = Logger.getLogger(getClass());
	  
	@Resource
    PayEvtMontantDaoService payEvtMontantDaoService;
	
	@Resource
	PayEvtDaoService payEvtDaoService;
    
	@Override
	public boolean matches(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        
        String evtUrlId = path.replaceFirst("/evts/([^/]+)/.*", "$1");        
        String mntUrlId = path.replaceFirst("/evts/([^/]+)/([^/]+)", "$2");
        
        if(StringUtils.isNoneEmpty(evtUrlId) && StringUtils.isNoneEmpty(mntUrlId)) {
	        log.debug("Evt " + evtUrlId + " - mnt " + mntUrlId);
	    	List<PayEvt> evts = payEvtDaoService.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
	    	if(evts.size() == 0) {
	    		 return false;
	    	} 
	    	List<PayEvtMontant> evtsMnts = payEvtMontantDaoService.findPayEvtMontantsByEvtAndUrlIdEquals(evts.get(0), mntUrlId).getResultList();
	    	if(evtsMnts.size() == 0) {
	    		 return false;
	    	} 
	    	return evtsMnts.get(0).getAuthCas();
        }
        
        return false;
	}

}
