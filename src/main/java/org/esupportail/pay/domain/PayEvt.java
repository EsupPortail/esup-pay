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
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
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
    
    public String getTitleFr() {
    	return this.getTitle().getTranslation(LOCALE_IDS.fr);
    }


	public static Long countFindPayEvtsByRespLogins(List<RespLogin> respLogins) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        TypedQuery q = em.createQuery(queryBuilder.toString(), Long.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return ((Long) q.getSingleResult());
    }

	public static Long countFindPayEvtsByUrlIdEquals(String urlId) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvt.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvt AS o WHERE o.urlId = :urlId", Long.class);
        q.setParameter("urlId", urlId);
        return ((Long) q.getSingleResult());
    }

	public static Long countFindPayEvtsByViewerLogins(List<RespLogin> viewerLogins) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        TypedQuery q = em.createQuery(queryBuilder.toString(), Long.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _resplogin: viewerLogins) {
            q.setParameter("viewerLogins_item" + viewerLoginsIndex++, _resplogin);
        }
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<PayEvt> findPayEvtsByRespLogins(List<RespLogin> respLogins) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return q;
    }

	public static TypedQuery<PayEvt> findPayEvtsByUrlIdEquals(String urlId) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvt.entityManager();
        TypedQuery<PayEvt> q = em.createQuery("SELECT o FROM PayEvt AS o WHERE o.urlId = :urlId", PayEvt.class);
        q.setParameter("urlId", urlId);
        return q;
    }

	public static TypedQuery<PayEvt> findPayEvtsByUrlIdEquals(String urlId, String sortFieldName, String sortOrder) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE o.urlId = :urlId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        q.setParameter("urlId", urlId);
        return q;
    }

	public static TypedQuery<PayEvt> findPayEvtsByViewerLogins(List<RespLogin> viewerLogins) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _resplogin: viewerLogins) {
            q.setParameter("viewerLogins_item" + viewerLoginsIndex++, _resplogin);
        }
        return q;
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("logoFile").toString();
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new PayEvt().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPayEvts() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PayEvt o", Long.class).getSingleResult();
    }

	public static List<PayEvt> findAllPayEvts() {
        return entityManager().createQuery("SELECT o FROM PayEvt o", PayEvt.class).getResultList();
    }

	public static List<PayEvt> findAllPayEvts(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvt o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayEvt.class).getResultList();
    }

	public static PayEvt findPayEvt(Long id) {
        if (id == null) return null;
        return entityManager().find(PayEvt.class, id);
    }

	public static List<PayEvt> findPayEvtEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PayEvt o", PayEvt.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<PayEvt> findPayEvtEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvt o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayEvt.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PayEvt attached = PayEvt.findPayEvt(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public PayEvt merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PayEvt merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public BigFile getLogoFile() {
        return this.logoFile;
    }

	public void setLogoFile(BigFile logoFile) {
        this.logoFile = logoFile;
    }

	public String getPayboxServiceKey() {
        return this.payboxServiceKey;
    }

	public void setPayboxServiceKey(String payboxServiceKey) {
        this.payboxServiceKey = payboxServiceKey;
    }

	public Label getTitle() {
        return this.title;
    }

	public void setTitle(Label title) {
        this.title = title;
    }

	public String getWebSiteUrl() {
        return this.webSiteUrl;
    }

	public void setWebSiteUrl(String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }

	public String getUrlId() {
        return this.urlId;
    }

	public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

	public String getManagersEmail() {
        return this.managersEmail;
    }

	public void setManagersEmail(String managersEmail) {
        this.managersEmail = managersEmail;
    }

	public String getMailSubject() {
        return this.mailSubject;
    }

	public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

	public String getPayboxCommandPrefix() {
        return this.payboxCommandPrefix;
    }

	public void setPayboxCommandPrefix(String payboxCommandPrefix) {
        this.payboxCommandPrefix = payboxCommandPrefix;
    }

	public List<RespLogin> getRespLogins() {
        return this.respLogins;
    }

	public void setRespLogins(List<RespLogin> respLogins) {
        this.respLogins = respLogins;
    }

	public List<RespLogin> getViewerLogins() {
        return this.viewerLogins;
    }

	public void setViewerLogins(List<RespLogin> viewerLogins) {
        this.viewerLogins = viewerLogins;
    }

	public Label getDefaultMntDescription() {
        return this.defaultMntDescription;
    }

	public void setDefaultMntDescription(Label defaultMntDescription) {
        this.defaultMntDescription = defaultMntDescription;
    }

	public String getDefaultOptionalAddedParams() {
        return this.defaultOptionalAddedParams;
    }

	public void setDefaultOptionalAddedParams(String defaultOptionalAddedParams) {
        this.defaultOptionalAddedParams = defaultOptionalAddedParams;
    }

	public Double getDbleMontantMax() {
        return this.dbleMontantMax;
    }

	public void setDbleMontantMax(Double dbleMontantMax) {
        this.dbleMontantMax = dbleMontantMax;
    }

	public void setLogins(List<String> logins) {
        this.logins = logins;
    }

	public void setViewerLogins2Add(List<String> viewerLogins2Add) {
        this.viewerLogins2Add = viewerLogins2Add;
    }
}
