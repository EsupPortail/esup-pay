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

import jakarta.annotation.Resource;
import org.esupportail.pay.dao.EmailFieldsMapReferenceDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PayBoxServiceManager {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Resource
    EmailFieldsMapReferenceDaoService emailFieldsMapReferenceDaoService;

    @Resource
    PayTransactionLogDaoService payTransactionLogDaoService;
    
    @Value("${institute.href}")
    String instituteHref;
    
    @Resource
    Map<String,PayBoxService> payboxServices;

    public List<String> getPayboxServiceKeys() {
    	List<String> serviceKeys = new ArrayList<String>(payboxServices.keySet());
    	Collections.sort(serviceKeys);
    	return serviceKeys;
    }

	public PayBoxForm getPayBoxForm(PayEvt payboxevt, String mail,
			String field1, String field2, Double montant,
			PayEvtMontant payboxEvtMontant, String billingFirstname, String billingLastname, 
			String billingAddress1, String billingZipCode, String billingCity, String billingCountryCode) {
		if(!payboxServices.containsKey(payboxevt.getPayboxServiceKey())) {
			String errorMessage = String.format("Pas de compte paybox associé à %s en configuration d'esup-pay.", payboxevt.getPayboxServiceKey());
			log.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}

		return payboxServices.get(payboxevt.getPayboxServiceKey()).getPayBoxForm(mail, field1, field2, montant, payboxEvtMontant,
				billingFirstname, billingLastname, billingAddress1, billingZipCode, billingCity, billingCountryCode);
	}

    @Transactional(readOnly = true)
	public String getWebSite(String reference) {
        String webSite = instituteHref;
        List<EmailFieldsMapReference> emailMapFirstLastNames = emailFieldsMapReferenceDaoService.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            PayEvtMontant evtMontant = emailMapFirstLastNames.get(0).getPayEvtMontant();
            PayEvt evt = evtMontant.getEvt();
            webSite = evt.getWebSiteUrl();
        } else {
            // Paiement déjà ok par paybox -> transactionLog
            PayTransactionLog transactionLog = payTransactionLogDaoService.findPayTransactionLogByReference(reference);
            if (transactionLog != null) {
                PayEvt evt = transactionLog.getPayEvtMontant().getEvt();
                webSite = evt.getWebSiteUrl();
            }
        }
        log.info("website for reference {} : {}", reference, webSite);
        return webSite;
	}
	
	public EmailFieldsMapReference getEmailFieldsMapReference(String reference) {
        List<EmailFieldsMapReference> emailMapFirstLastNames = emailFieldsMapReferenceDaoService.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            return emailMapFirstLastNames.get(0);
        }
        return null;
	}

    @Transactional
	public boolean payboxCallback(String montant, String reference,
			String auto, String erreur, String idtrans,
			String securevers, String softdecline, String secureauth, String securegarantie,
			String signature, String queryString) {
		List<EmailFieldsMapReference> emailMapFirstLastNames = emailFieldsMapReferenceDaoService.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            PayEvtMontant evtMontant = emailMapFirstLastNames.get(0).getPayEvtMontant();
            PayEvt payboxevt = evtMontant.getEvt();
            PayBoxService  payBoxService = payboxServices.get(payboxevt.getPayboxServiceKey());
            if(payBoxService==null) {
                log.error("Pas de compte paybox associé à " + payboxevt.getPayboxServiceKey() + " en configuration d'esup-pay !");
                return false;
            }
            return payboxServices.get(payboxevt.getPayboxServiceKey()).payboxCallback(montant, reference, auto, erreur, idtrans, securevers, softdecline, secureauth, securegarantie, signature, queryString);
        }
        log.error("reference ne correspond pas à un montant/evt et donc à un service paybox !? reference : " + reference);
        return false;
	}

	public PayBoxForm getPayBoxForm(PayEvt payboxevt, String mail,
			String field1, String field2, Double dbleMontant,
			PayEvtMontant payboxevtmontant, String amount) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean checkPayboxSignature(String reference, String queryString, String signature) {
		List<EmailFieldsMapReference> emailMapFirstLastNames = emailFieldsMapReferenceDaoService.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            PayEvtMontant evtMontant = emailMapFirstLastNames.get(0).getPayEvtMontant();
            PayEvt payboxevt = evtMontant.getEvt();
            return payboxServices.get(payboxevt.getPayboxServiceKey()).checkPayboxSignature(queryString, signature);
        }
        log.error("reference ne correspond pas à un montant/evt et donc à un service paybox !? reference : " + reference);
        return false;
	}
    
}