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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayBoxForm;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PayBoxServiceManager {

    private final Logger log = Logger.getLogger(getClass());

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
			PayEvtMontant payboxEvtMontant) {
		return payboxServices.get(payboxevt.getPayboxServiceKey()).getPayBoxForm(mail, field1, field2, montant, payboxEvtMontant);
	}
	
	public String getWebSite(String reference) {
        List<EmailFieldsMapReference> emailMapFirstLastNames = EmailFieldsMapReference.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            PayEvtMontant evtMontant = emailMapFirstLastNames.get(0).getPayEvtMontant();
            PayEvt evt = evtMontant.getEvt();
            return evt.getWebSiteUrl();
        }
        return instituteHref;
	}
	
	public EmailFieldsMapReference getEmailFieldsMapReference(String reference) {
        List<EmailFieldsMapReference> emailMapFirstLastNames = EmailFieldsMapReference.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            return emailMapFirstLastNames.get(0);
        }
        return null;
	}

	public boolean payboxCallback(String montant, String reference,
			String auto, String erreur, String idtrans, String signature,
			String queryString) {
		List<EmailFieldsMapReference> emailMapFirstLastNames = EmailFieldsMapReference.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
        if (!emailMapFirstLastNames.isEmpty()) {
            PayEvtMontant evtMontant = emailMapFirstLastNames.get(0).getPayEvtMontant();
            PayEvt payboxevt = evtMontant.getEvt();
            return payboxServices.get(payboxevt.getPayboxServiceKey()).payboxCallback(montant, reference, auto, erreur, idtrans, signature, queryString);
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
    
}
