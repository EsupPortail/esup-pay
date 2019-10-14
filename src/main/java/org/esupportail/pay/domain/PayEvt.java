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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString(excludeFields = "logoFile")
@RooJpaActiveRecord(finders = { "findPayEvtsByUrlIdEquals", "findPayEvtsByRespLogins", "findPayEvtsByViewerLogins" })
public class PayEvt {

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("id", "logoFile", "payboxServiceKey", "title", "webSiteUrl", "urlId", "managersEmail", "mailSubject", "payboxCommandPrefix", "respLogins", "viewerLogins", "defaultMntDescription", "logins", "viewerLogins2Add");
	
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private BigFile logoFile = new BigFile();

    @NotEmpty
    String payboxServiceKey;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Label title;

    String webSiteUrl;

    @Column(unique = true)
    String urlId;

    @NotEmpty
    String managersEmail;
    
    @NotEmpty
    String mailSubject;

    @NotEmpty
    String payboxCommandPrefix;

    @ManyToMany
    List<RespLogin> respLogins;

    @ManyToMany
    List<RespLogin> viewerLogins;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
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


	public static TypedQuery<PayEvt> findPayEvtsByRespLogins(List<RespLogin> respLogins, String sortFieldName, String sortOrder) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
        	queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" " + sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return q;
    }
    
    
    public static TypedQuery<PayEvt> findPayEvtsByViewerLogins(List<RespLogin> viewerLogins, String sortFieldName, String sortOrder) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
        	queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" " + sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _viewerLogin: viewerLogins) {
            q.setParameter("respLogins_item" + viewerLoginsIndex++, _viewerLogin);
        }
        return q;
    }
    
    public static TypedQuery<PayEvt> findPayEvtsByRespLoginsOrByViewerLogins(List<RespLogin> logins, String sortFieldName, String sortOrder) {
        if (logins == null) throw new IllegalArgumentException("The logins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < logins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :logins_item").append(i).append(" MEMBER OF o.respLogins").append(" OR :logins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
        	queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" " + sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _login: logins) {
            q.setParameter("logins_item" + respLoginsIndex++, _login);
        }
        return q;
    }
    
    public static List<Object[]> findSumMontantGroupByEvt (){
        EntityManager em = PayEvt.entityManager();
        String sql = "SELECT mail_subject, SUM(cast(montant AS INTEGER)/100), pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id  "
        		+ "GROUP BY pay_evt.id, pay_evt.mail_subject ORDER BY pay_evt.id DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static List<Object[]> findNbParticipantsByEvt (){
        EntityManager em = PayEvt.entityManager();
        String sql = "SELECT mail_subject, COUNT(evt) AS count, pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id  "
        		+ "GROUP BY pay_evt.id, pay_evt.mail_subject ORDER BY pay_evt.id DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
}
