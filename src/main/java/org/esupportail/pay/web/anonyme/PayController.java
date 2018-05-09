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
package org.esupportail.pay.web.anonyme;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayBoxForm;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.ScienceConfReference;
import org.esupportail.pay.exceptions.EntityNotFoundException;
import org.esupportail.pay.services.PayBoxServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.AbstractResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.support.ServletContextResource;

@Controller
@Transactional
public class PayController {

    private final Logger log = Logger.getLogger(getClass());

	@Autowired
	ServletContext servletContext;
	
	@Resource 
	PayBoxServiceManager payBoxServiceManager;
	
    @RequestMapping("/")
    public String index(Model uiModel) {
    	 
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth.isAuthenticated() && 
        		(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) || 
        		auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) ||
         		auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER")))) {
        	return "redirect:/admin";
        }
    	
       return "index";
    }

    @RequestMapping("evts/{evtUrlId}")
    public String indexEvt(@PathVariable("evtUrlId") String evtUrlId, Model uiModel) {
    	log.info("Evt " + evtUrlId + " called");
    	List<PayEvt> evts = PayEvt.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
    	if(evts.size() == 0) {
    		throw new EntityNotFoundException();
    	} 
    	uiModel.addAttribute("payevt", evts.get(0));
        return "evt";
    }

	@RequestMapping(value = "logo/{evtUrlId}")
	public void getLogoFileEvt(@PathVariable("evtUrlId") String evtUrlId, HttpServletRequest request, HttpServletResponse response) {
		try {
	    	List<PayEvt> evts = PayEvt.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
	    	if(evts.size() == 0) {
	    		throw new EntityNotFoundException();
	    	} 
			PayEvt evt = evts.get(0);   
			if(evt.getLogoFile().getBinaryFile() != null) {
				IOUtils.copy(evt.getLogoFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
			} else {
				AbstractResource defaultLogo = new ServletContextResource(servletContext, "images/credit-card.png");
				IOUtils.copy(defaultLogo.getInputStream(), response.getOutputStream());
			}
		} catch(Exception ioe) {
			log.warn("Get Logo of PayEvt with urlId " + evtUrlId + " failed.", ioe);
		}
	}

    @RequestMapping(value="evts/{evtUrlId}/{mntUrlId}", method=RequestMethod.GET)
    public String indexEvtMnt(@PathVariable("evtUrlId") String evtUrlId, @PathVariable("mntUrlId") String mntUrlId, Model uiModel) {
    	log.info("Evt " + evtUrlId + " - mnt " + mntUrlId + " called");
    	List<PayEvt> evts = PayEvt.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
    	if(evts.size() == 0) {
    		log.warn("PayEvt " + evtUrlId + " not found");
    		throw new EntityNotFoundException();
    	} 
    	List<PayEvtMontant> evtsMnts = PayEvtMontant.findPayEvtMontantsByEvtAndUrlIdEquals(evts.get(0), mntUrlId).getResultList();
    	if(evtsMnts.size() == 0) {
    		log.warn("PayEvtMontant " + mntUrlId + "in " + evts.get(0) + " not found");
    		throw new EntityNotFoundException();
    	} 
    	
    	if(evtsMnts.get(0).getSciencesconf()) {
    		log.warn("SciencesConf event montant form must be called from sciencesconf web site.");
    		log.warn("Redirection sur le site sciencesconf en cours ...");
            String forwardUrl = evts.get(0).getWebSiteUrl();
            return "redirect:" + forwardUrl;
    	}
		
    	uiModel.addAttribute("payevt", evts.get(0));
    	uiModel.addAttribute("payevtmontant", evtsMnts.get(0));
    	
    	if(!evtsMnts.get(0).getIsEnabled()) {
    		log.info("PayEvtMontant " + mntUrlId + "in " + evts.get(0) + " found but is not enabled");
    		return "amountFormDisabled";
    	}
	
        return "evtmnt";
    }

    
    
    @RequestMapping(value="evts/{evtUrlId}/{mntUrlId}", method=RequestMethod.POST)
    public String form(Model uiModel, @PathVariable("evtUrlId") String evtUrlId, @PathVariable("mntUrlId") String mntUrlId, 
    		@RequestParam String mail, @RequestParam String field1, @RequestParam String field2, @RequestParam(required=false, value="amount") String amountString) {
    	log.info("Evt " + evtUrlId + " - mnt " + mntUrlId + " form called");
    	
    	if(amountString!=null) {
    		amountString = amountString.trim();
    	}
    	if(mail!=null) {
    		mail = mail.trim();
    	}
    	if(field1!=null) {
    		field1 = field1.trim();
    	}
    	if(field2!=null) {
    		field2 = field2.trim();
    	}
    	
    	List<PayEvt> evts = PayEvt.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
    	if(evts.size() == 0) {
    		log.warn("PayEvt " + evtUrlId + " not found");
    		throw new EntityNotFoundException();
    	} 
    	PayEvt payevt = evts.get(0);
    	
    	List<PayEvtMontant> evtsMnts = PayEvtMontant.findPayEvtMontantsByEvtAndUrlIdEquals(payevt, mntUrlId).getResultList();
    	if(evtsMnts.size() == 0) {
    		log.warn("PayEvtMontant " + mntUrlId + "in " + payevt + " not found");
    		throw new EntityNotFoundException();
    	} 
    	PayEvtMontant payevtmontant = evtsMnts.get(0);
    	
    	uiModel.addAttribute("payevt", payevt);
    	uiModel.addAttribute("payevtmontant", payevtmontant);
    	
    	Double amount = null;
    	// on accepte les , et les . 
    	if(amountString != null) {
    		amountString = amountString.replace(",", ".");
    		amount = Double.valueOf(amountString);
    	}
    	
    	if(!payevtmontant.getFreeAmount()) {
    		amount = payevtmontant.getDbleMontant();
    	}
    	if(amount==null) {
    		uiModel.addAttribute("error", "amount_cant_be_null");
    		 return "evtmnt";
    	}
		if(payevt.getDbleMontantMax() != null && amount > payevt.getDbleMontantMax()) {
			uiModel.addAttribute("error", "dbleMontant_too_high");
			log.warn("Try to pay amount too high : " + mail + ", " + field1 + ", " + field2 + ", " + amount);
			 return "evtmnt";
	    }
    		
    	PayBoxForm payBoxForm = payBoxServiceManager.getPayBoxForm(payevt, mail, field1, field2, amount, payevtmontant);

	    uiModel.addAttribute("payBoxForm", payBoxForm);
        return "evtmntForm";
    }
    
    
    @RequestMapping(value="/", params = "reference")
    public String payboxForward(Model uiModel, @RequestParam String reference, HttpServletResponse response) {
    	EmailFieldsMapReference emailFieldsMapReference = payBoxServiceManager.getEmailFieldsMapReference(reference);
    	if(emailFieldsMapReference!=null && emailFieldsMapReference.getPayEvtMontant().getSciencesconf()) {
    		ScienceConfReference scienceConfReference = ScienceConfReference.findScienceConfReferencesByEmailFieldsMapReference(emailFieldsMapReference).getSingleResult();
    		 uiModel.addAttribute("scienceConfReference", scienceConfReference);
    		 return "scienceconfPostReturn";
    	}
        String forwardUrl = payBoxServiceManager.getWebSite(reference);
        return "redirect:" + forwardUrl;
    }
    
    /**
     * POST transmis par le client émanent de sciencesconf 
			confid : identifiant de la conférence
			uid : identifiant de l’inscrit
			lastname : nom de l’inscrit
			firstname : prénom de l’inscrit
			mail : adresse mail de l’inscrit
			fees : montant de l’inscription (en euros)
			returnurl : url de retour
     */
    @RequestMapping(value="evts/{evtUrlId}/{mntUrlId}", method=RequestMethod.POST, params="confid")
    public String sciencesConfForm(Model uiModel, @PathVariable("evtUrlId") String evtUrlId, @PathVariable("mntUrlId") String mntUrlId, 
    		@RequestParam String confid, @RequestParam String uid, @RequestParam String lastname, @RequestParam String firstname, @RequestParam String mail, @RequestParam String fees, @RequestParam String returnurl) {
    	log.info("Evt " + evtUrlId + " - mnt " + mntUrlId + " called via sciencesconf");
    	log.info("confid " + confid + " - uid : " + uid + " - lastname : " + lastname + " - firstname : " + firstname + " - mail : " + mail + " - fees : " + fees + " - returnurl : " + returnurl );
    	
    	List<PayEvt> evts = PayEvt.findPayEvtsByUrlIdEquals(evtUrlId).getResultList();
    	if(evts.size() == 0) {
    		log.warn("PayEvt " + evtUrlId + " not found");
    		throw new EntityNotFoundException();
    	} 
    	PayEvt payevt = evts.get(0);
    	
    	List<PayEvtMontant> evtsMnts = PayEvtMontant.findPayEvtMontantsByEvtAndUrlIdEquals(payevt, mntUrlId).getResultList();
    	if(evtsMnts.size() == 0) {
    		log.warn("PayEvtMontant " + mntUrlId + "in " + payevt + " not found");
    		throw new EntityNotFoundException();
    	} 
    	PayEvtMontant payevtmontant = evtsMnts.get(0);
    	
    	uiModel.addAttribute("payevt", payevt);
    	uiModel.addAttribute("payevtmontant", payevtmontant);
    	
    	Double amount =  Double.valueOf(fees);

    	PayBoxForm payBoxForm = payBoxServiceManager.getPayBoxForm(payevt, mail, firstname, lastname, amount, payevtmontant);
    	
    	List<EmailFieldsMapReference> emailMapFirstLastNames = EmailFieldsMapReference.findEmailFieldsMapReferencesByReferenceEquals(payBoxForm.getCommande()).getResultList();
        EmailFieldsMapReference emailFieldsMapReference = emailMapFirstLastNames.get(0);
        ScienceConfReference scienceConfReference = new ScienceConfReference();
        scienceConfReference.setConfid(confid);
        scienceConfReference.setUid(uid);
        scienceConfReference.setReturnurl(returnurl);
        scienceConfReference.setEmailFieldsMapReference(emailFieldsMapReference);
        scienceConfReference.setDateCreated(new Date());
        scienceConfReference.persist();

	    uiModel.addAttribute("payBoxForm", payBoxForm);
        return "evtmntForm";
    }
 
}
