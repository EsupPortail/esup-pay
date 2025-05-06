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
import java.util.Locale;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.esupportail.pay.domain.Label.LOCALE_IDS;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PayEvtMontant {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_evt_seq_gen")
    @SequenceGenerator(name = "pay_evt_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;
	
    @ManyToOne
    @NotNull
    @JoinColumn(name = "evt")
    private PayEvt evt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "title")
    Label title;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "description")
    Label description;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "help")
    Label help; 
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "field1label")
    Label field1Label;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "field2label")
    Label field2Label; 
    
    Double dbleMontant;

    String urlId;
    
    Boolean freeAmount = false;
       
    Boolean sciencesconf = false;
    
    Boolean authCas = false;
 
    String addPrefix = "";
    
    String optionalAddedParams = "";
    
    Boolean isEnabled = true;
    
    @Min(value = 1)
    @Max(value = 99)
    Integer shoppingcartTotalQuantity = 1;
    
    Boolean isBillingAddressRequired = false;

    Long montantTotalMax = -1L;

    Long nbTransactionsMax = -1L;
    
    public String getDbleMontantDisplay() {
    	return String.format(Locale.FRANCE, "%,.2f€", dbleMontant);
    }
    
    public Boolean getIsEnabled() {
        return this.isEnabled == null || this.isEnabled;
    }
    
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

	public void setEvtWithDefaultParametersIfNeeded(PayEvt evt) {
		this.setEvt(evt);
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
	
	public Boolean getAuthCas() {
		return authCas != null && authCas;
	}

    public Long getMontantTotalMax() {
        if (montantTotalMax == null || montantTotalMax < 0) {
            return -1L;
        }
        return montantTotalMax;
    }

    public Long getNbTransactionsMax() {
        if (nbTransactionsMax == null || nbTransactionsMax < 0) {
            return -1L;
        }
        return nbTransactionsMax;
    }

}