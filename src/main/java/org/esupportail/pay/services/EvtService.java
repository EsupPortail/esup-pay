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
package org.esupportail.pay.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.RespLoginDaoService;
import org.esupportail.pay.domain.Label;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EvtService {


    @Resource
    UrlIdService urlIdService;

    @Resource
    LdapService ldapService;
	
	@Resource
	PayEvtDaoService payEvtDaoService;
	
	@Resource
	RespLoginDaoService respLoginDaoService;

    public void updateEvt(PayEvt payEvt) {
        // Hack : don't override logoFile !!
        PayEvt payEvtCurrent = payEvtDaoService.findPayEvt(payEvt.getId());
        payEvt.setLogoFile(payEvtCurrent.getLogoFile());
        // Hack end

        computeLogins(payEvt, payEvt.getLogins(), payEvt.getViewerLogins2Add());
        computeRespLogin(payEvt);
        
        payEvtDaoService.merge(payEvt);
    }

    public void createEvt(PayEvt payEvt, List<String> respLoginIds, List<String> viewerLoginIds) {
    	
        computeLogins(payEvt, respLoginIds, viewerLoginIds);

        if(payEvt.getUrlId() == null || payEvt.getUrlId().isEmpty()) {
            String urlId = urlIdService.generateUrlId4PayEvt(payEvt.getTitle().getTranslation(Label.LOCALE_IDS.en));
            payEvt.setUrlId(urlId);
        }

        payEvtDaoService.persist(payEvt);
    }

	public void computeLogins(PayEvt payEvt, List<String> respLoginIds, List<String> viewerLoginIds) {
		List<RespLogin> respLogins = new ArrayList<RespLogin>();
        if(respLoginIds!=null && !respLoginIds.isEmpty()) {
            for(String login: respLoginIds) {
            	if(login != null && !login.isEmpty() && !respLogins.stream().anyMatch(r -> r.getLogin().equals(login))) {
	                RespLogin respLogin = respLoginDaoService.findOrCreateRespLogin(login);
	                respLogins.add(respLogin);
            	}
            }
        }
        payEvt.setRespLogins(respLogins);

        List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
        if(viewerLoginIds!=null && !viewerLoginIds.isEmpty()) {
            for(String login: viewerLoginIds) {
            	if(login != null && !login.isEmpty() && !viewerLogins.stream().anyMatch(r -> r.getLogin().equals(login))) {
            		RespLogin respLogin = respLoginDaoService.findOrCreateRespLogin(login);
            		viewerLogins.add(respLogin);
            	}
            }
        }
        payEvt.setViewerLogins(viewerLogins);
	}

    public List<RespLogin> listEvt(String currentUser) {
        RespLogin respLogin = respLoginDaoService.findOrCreateRespLogin(currentUser);
        return Arrays.asList(respLogin);
    }

    public void computeRespLogin(List<PayEvt> payEvts) {
    	
    	List<RespLogin> respLogins = new ArrayList<RespLogin>();
    	for(PayEvt payEvt: payEvts) {
    		if(payEvt.getRespLogins() != null) {
    			respLogins.addAll(payEvt.getRespLogins());
    		}
    	}
        ldapService.computeRespLogin(respLogins);
 
    	List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
    	for(PayEvt payEvt: payEvts) {
    		if(payEvt.getViewerLogins() != null) {
    			viewerLogins.addAll(payEvt.getViewerLogins());
    		}
    	}
        ldapService.computeRespLogin(viewerLogins);
        
    }
    
    public void computeRespLogin(PayEvt payEvt) {
        if (payEvt.getRespLogins() != null) {
            ldapService.computeRespLogin(payEvt.getRespLogins());
        }
        if (payEvt.getViewerLogins() != null) {
            ldapService.computeRespLogin(payEvt.getViewerLogins());
        }
    }
 }