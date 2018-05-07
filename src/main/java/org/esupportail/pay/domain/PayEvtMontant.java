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
 package org.esupportail.pay.domain;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findPayEvtMontantsByEvtAndUrlIdEquals", "findPayEvtMontantsByEvt" })
public class PayEvtMontant {

    @ManyToOne
    @NotNull
    private PayEvt evt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Label title;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Label description;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Label help; 
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Label field1Label;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Label field2Label; 
    
    Double dbleMontant;

    String urlId;
    
    Boolean freeAmount = false;
       
    Boolean sciencesconf = false;
    
    String addPrefix = "";
    
    String optionalAddedParams = "";
       
    public PayEvtMontant() {
		super();
		
		title = new Label();
		
		description = new Label();

		help = new Label(); 
		help.getLabelLocales().get(LOCALE_IDS.fr.toString()).setTranslation("Merci de renseigner une adresse mail valide ainsi que votre nom et votre prénom.");
		help.getLabelLocales().get(LOCALE_IDS.en.toString()).setTranslation("Be careful to fill in a valid email, name and firstname !");
		
		field1Label = new Label();	
		field1Label.getLabelLocales().get(LOCALE_IDS.fr.toString()).setTranslation("Nom de famille");
		field1Label.getLabelLocales().get(LOCALE_IDS.en.toString()).setTranslation("Last Name");
		
		field2Label = new Label(); 
		field2Label.getLabelLocales().get(LOCALE_IDS.fr.toString()).setTranslation("Prénom");
		field2Label.getLabelLocales().get(LOCALE_IDS.en.toString()).setTranslation("First Name");
	}

	public boolean isDeletable() {
    	return PayTransactionLog.countFindPayTransactionLogsByPayEvtMontant(this) < 1 
    			&& EmailFieldsMapReference.countFindEmailFieldsMapReferencesByPayEvtMontant(this) < 1;
    }

	public void setEvt(PayEvt evt) {
		this.evt = evt;
		if(description.getTranslation(LOCALE_IDS.fr).isEmpty()) {
			description.getLabelLocales().get(LOCALE_IDS.fr.toString()).setTranslation(evt.getDefaultMntDescription().getTranslation(LOCALE_IDS.fr));
		}
		if(description.getTranslation(LOCALE_IDS.en).isEmpty()) {
			description.getLabelLocales().get(LOCALE_IDS.en.toString()).setTranslation(evt.getDefaultMntDescription().getTranslation(LOCALE_IDS.en));
		}
		if(optionalAddedParams.isEmpty()) {
			optionalAddedParams = evt.getDefaultOptionalAddedParams();
		}
	}

	public void setFreeAmount(Boolean freeAmount) {
		this.freeAmount = freeAmount;
		if(freeAmount) {
			this.dbleMontant = null;
		}
	}
	
	public void setSciencesconf(Boolean sciencesconf) {
		this.sciencesconf = sciencesconf;
		if(sciencesconf) {
			this.dbleMontant = null;
		}
	}


}
