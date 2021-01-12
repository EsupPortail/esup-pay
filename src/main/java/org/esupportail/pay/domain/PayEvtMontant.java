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
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
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
    
    Boolean isEnabled = true;
    
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

	public boolean isDeletable() {
    	return PayTransactionLog.countFindPayTransactionLogsByPayEvtMontant(this) < 1 
    			&& EmailFieldsMapReference.countFindEmailFieldsMapReferencesByPayEvtMontant(this) < 1;
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

	public void setEvt(PayEvt evt) {
		this.evt = evt;
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

	public static Long countFindPayEvtMontantsByEvt(PayEvt evt) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        EntityManager em = PayEvtMontant.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvtMontant AS o WHERE o.evt = :evt", Long.class);
        q.setParameter("evt", evt);
        return ((Long) q.getSingleResult());
    }

	public static Long countFindPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvtMontant.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId", Long.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<PayEvtMontant> findPayEvtMontantsByEvt(PayEvt evt) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        EntityManager em = PayEvtMontant.entityManager();
        TypedQuery<PayEvtMontant> q = em.createQuery("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt", PayEvtMontant.class);
        q.setParameter("evt", evt);
        return q;
    }

	public static TypedQuery<PayEvtMontant> findPayEvtMontantsByEvt(PayEvt evt, String sortFieldName, String sortOrder) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        EntityManager em = PayEvtMontant.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvtMontant> q = em.createQuery(queryBuilder.toString(), PayEvtMontant.class);
        q.setParameter("evt", evt);
        return q;
    }

	public static TypedQuery<PayEvtMontant> findPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvtMontant.entityManager();
        TypedQuery<PayEvtMontant> q = em.createQuery("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId", PayEvtMontant.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return q;
    }

	public static TypedQuery<PayEvtMontant> findPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId, String sortFieldName, String sortOrder) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvtMontant.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvtMontant> q = em.createQuery(queryBuilder.toString(), PayEvtMontant.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return q;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("evt", "title", "description", "help", "field1Label", "field2Label", "dbleMontant", "urlId", "freeAmount", "sciencesconf", "addPrefix", "optionalAddedParams", "isEnabled");

	public static final EntityManager entityManager() {
        EntityManager em = new PayEvtMontant().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPayEvtMontants() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PayEvtMontant o", Long.class).getSingleResult();
    }

	public static List<PayEvtMontant> findAllPayEvtMontants() {
        return entityManager().createQuery("SELECT o FROM PayEvtMontant o", PayEvtMontant.class).getResultList();
    }

	public static List<PayEvtMontant> findAllPayEvtMontants(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvtMontant o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayEvtMontant.class).getResultList();
    }

	public static PayEvtMontant findPayEvtMontant(Long id) {
        if (id == null) return null;
        return entityManager().find(PayEvtMontant.class, id);
    }

	public static List<PayEvtMontant> findPayEvtMontantEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PayEvtMontant o", PayEvtMontant.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<PayEvtMontant> findPayEvtMontantEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvtMontant o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayEvtMontant.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            PayEvtMontant attached = PayEvtMontant.findPayEvtMontant(this.id);
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
    public PayEvtMontant merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PayEvtMontant merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public PayEvt getEvt() {
        return this.evt;
    }

	public Label getTitle() {
        return this.title;
    }

	public void setTitle(Label title) {
        this.title = title;
    }

	public Label getDescription() {
        return this.description;
    }

	public void setDescription(Label description) {
        this.description = description;
    }

	public Label getHelp() {
        return this.help;
    }

	public void setHelp(Label help) {
        this.help = help;
    }

	public Label getField1Label() {
        return this.field1Label;
    }

	public void setField1Label(Label field1Label) {
        this.field1Label = field1Label;
    }

	public Label getField2Label() {
        return this.field2Label;
    }

	public void setField2Label(Label field2Label) {
        this.field2Label = field2Label;
    }

	public Double getDbleMontant() {
        return this.dbleMontant;
    }

	public void setDbleMontant(Double dbleMontant) {
        this.dbleMontant = dbleMontant;
    }

	public String getUrlId() {
        return this.urlId;
    }

	public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

	public Boolean getFreeAmount() {
        return this.freeAmount;
    }

	public Boolean getSciencesconf() {
        return this.sciencesconf;
    }

	public String getAddPrefix() {
        return this.addPrefix;
    }

	public void setAddPrefix(String addPrefix) {
        this.addPrefix = addPrefix;
    }

	public String getOptionalAddedParams() {
        return this.optionalAddedParams;
    }

	public void setOptionalAddedParams(String optionalAddedParams) {
        this.optionalAddedParams = optionalAddedParams;
    }

	public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
