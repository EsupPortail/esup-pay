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

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.esupportail.pay.services.PayBoxService;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "pay_evt", indexes = {
                @Index(name = "pay_evt_archived_id", columnList = "archived, id desc")
})
public class PayEvt {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_evt_seq_gen")
    @SequenceGenerator(name = "pay_evt_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;
	
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "logo_file")
    private BigFile logoFile = new BigFile();

    @NotEmpty
    String payboxServiceKey;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "title")
    Label title;

    String webSiteUrl;

    @Column(unique = true)
    String urlId;

    @ElementCollection
    @CollectionTable(name = "managers_emails", joinColumns = @JoinColumn(name = "pay_evt"))
    @Column(name = "manager_email")
    List<String> managersEmails;

    @NotEmpty
    String mailSubject;

    @NotEmpty
    String payboxCommandPrefix;

    @Column(columnDefinition = "boolean default false")
    Boolean archived = false;

    @ManyToMany
    @JoinTable(name = "pay_evt_resp_logins",
    	joinColumns = @JoinColumn(
            name = "pay_evt",
            referencedColumnName = "id"
    	),
    	inverseJoinColumns = @JoinColumn(
    		name = "resp_logins",
    		referencedColumnName = "id"
    	)
    )
    List<RespLogin> respLogins;

    @ManyToMany
    @JoinTable(name = "pay_evt_viewer_logins",
        	joinColumns = @JoinColumn(
                    name = "pay_evt",
                    referencedColumnName = "id"
            	),
            	inverseJoinColumns = @JoinColumn(
            		name = "viewer_logins",
            		referencedColumnName = "id"
            	)
            )
    List<RespLogin> viewerLogins;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "default_mnt_description")
    Label defaultMntDescription;
    
    String defaultOptionalAddedParams = "";
    
    Double dbleMontantMax;
    
    /**
     * pour formulaire web
     */
    @Transient
    List<String> logins;
    
    
    /**
     * pour formulaire web
     */
    @Transient
    List<String> viewerLogins2Add;
    
    public PayEvt() {
		super();
		title = new Label();
		defaultMntDescription = new Label();
		defaultMntDescription.getLabelLocales().get(LOCALE_IDS.fr.toString()).setTranslation("Droits d'inscriptions");
		defaultMntDescription.getLabelLocales().get(LOCALE_IDS.en.toString()).setTranslation("Registration fees");
	}

	public String getRespLoginsStr() {
        List<String> displayNames = new ArrayList<>();
        for (RespLogin respLogin : respLogins) {
        	if(respLogin.getDisplayName() != null) {
        		displayNames.add(respLogin.getDisplayName());
        	} else {
        		displayNames.add(respLogin.getLogin());
        	}
        }
        return StringUtils.join(displayNames, " ; ");
    }
    
    public String getViewerLoginsStr() {
        List<String> displayNames = new ArrayList<>();
        for (RespLogin viewerLogin : viewerLogins) {
        	if(viewerLogin.getDisplayName() != null) {
        		displayNames.add(viewerLogin.getDisplayName());
        	} else {
        		displayNames.add(viewerLogin.getLogin());
        	}
        }
        return StringUtils.join(displayNames, " ; ");
    }
 
    public List<String> getLogins() {
		if(logins==null && respLogins!=null) {
			this.logins = new ArrayList<String>();
			for(RespLogin respLogin: respLogins) {
				this.logins.add(respLogin.getLogin());
			}
		}
		return this.logins;
	}

	public List<String> getViewerLogins2Add() {
		if(viewerLogins2Add==null && viewerLogins!=null) {
			this.viewerLogins2Add = new ArrayList<String>();
			for(RespLogin viewerLogin: viewerLogins) {
				this.viewerLogins2Add.add(viewerLogin.getLogin());
			}
		}
		return viewerLogins2Add;
	}
    
    public String getTitleFr() {
    	return this.getTitle().getTranslation(LOCALE_IDS.fr);
    }
    
    public String getDbleMontantMaxDisplay() {
    	return String.format("%,.2f€", dbleMontantMax);
    }

    public void setPayboxCommandPrefix(String payboxCommandPrefix) {
        this.payboxCommandPrefix = StringUtils.stripAccents(payboxCommandPrefix).replaceAll(PayBoxService.NUM_COMMANDE_CHARS_NOT_AUTHORIZED_REGEX, "");
    }
}