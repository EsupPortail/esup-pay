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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findEmailFieldsMapReferencesByReferenceEquals", "findEmailFieldsMapReferencesByPayEvtMontant" })
public class EmailFieldsMapReference {

    @NotNull
    @ManyToOne
    private PayEvtMontant payEvtMontant;

    private String reference;

    private String mail;

    private String field1;

    private String field2;
    
    private Date dateCreated;

	public static List<EmailFieldsMapReference> findOldEmailFieldsMapReferences(long oldDays) {
		Query q = entityManager().createQuery("select ref from EmailFieldsMapReference ref where ref.dateCreated is null or ref.dateCreated < :oldDate");
		q.setParameter("oldDate", Date.from(Instant.now().minus(Duration.ofDays(oldDays))));
		return q.getResultList();		
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

	public PayEvtMontant getPayEvtMontant() {
        return this.payEvtMontant;
    }

	public void setPayEvtMontant(PayEvtMontant payEvtMontant) {
        this.payEvtMontant = payEvtMontant;
    }

	public String getReference() {
        return this.reference;
    }

	public void setReference(String reference) {
        this.reference = reference;
    }

	public String getMail() {
        return this.mail;
    }

	public void setMail(String mail) {
        this.mail = mail;
    }

	public String getField1() {
        return this.field1;
    }

	public void setField1(String field1) {
        this.field1 = field1;
    }

	public String getField2() {
        return this.field2;
    }

	public void setField2(String field2) {
        this.field2 = field2;
    }

	public Date getDateCreated() {
        return this.dateCreated;
    }

	public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("payEvtMontant", "reference", "mail", "field1", "field2", "dateCreated");

	public static final EntityManager entityManager() {
        EntityManager em = new EmailFieldsMapReference().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countEmailFieldsMapReferences() {
        return entityManager().createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference o", Long.class).getSingleResult();
    }

	public static List<EmailFieldsMapReference> findAllEmailFieldsMapReferences() {
        return entityManager().createQuery("SELECT o FROM EmailFieldsMapReference o", EmailFieldsMapReference.class).getResultList();
    }

	public static List<EmailFieldsMapReference> findAllEmailFieldsMapReferences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM EmailFieldsMapReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, EmailFieldsMapReference.class).getResultList();
    }

	public static EmailFieldsMapReference findEmailFieldsMapReference(Long id) {
        if (id == null) return null;
        return entityManager().find(EmailFieldsMapReference.class, id);
    }

	public static List<EmailFieldsMapReference> findEmailFieldsMapReferenceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM EmailFieldsMapReference o", EmailFieldsMapReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<EmailFieldsMapReference> findEmailFieldsMapReferenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM EmailFieldsMapReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, EmailFieldsMapReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            EmailFieldsMapReference attached = EmailFieldsMapReference.findEmailFieldsMapReference(this.id);
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
    public EmailFieldsMapReference merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        EmailFieldsMapReference merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public static Long countFindEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = EmailFieldsMapReference.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant", Long.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return ((Long) q.getSingleResult());
    }

	public static Long countFindEmailFieldsMapReferencesByReferenceEquals(String reference) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        EntityManager em = EmailFieldsMapReference.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference AS o WHERE o.reference = :reference", Long.class);
        q.setParameter("reference", reference);
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = EmailFieldsMapReference.entityManager();
        TypedQuery<EmailFieldsMapReference> q = em.createQuery("SELECT o FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant", EmailFieldsMapReference.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public static TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant, String sortFieldName, String sortOrder) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = EmailFieldsMapReference.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<EmailFieldsMapReference> q = em.createQuery(queryBuilder.toString(), EmailFieldsMapReference.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public static TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByReferenceEquals(String reference) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        EntityManager em = EmailFieldsMapReference.entityManager();
        TypedQuery<EmailFieldsMapReference> q = em.createQuery("SELECT o FROM EmailFieldsMapReference AS o WHERE o.reference = :reference", EmailFieldsMapReference.class);
        q.setParameter("reference", reference);
        return q;
    }

	public static TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByReferenceEquals(String reference, String sortFieldName, String sortOrder) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        EntityManager em = EmailFieldsMapReference.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM EmailFieldsMapReference AS o WHERE o.reference = :reference");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<EmailFieldsMapReference> q = em.createQuery(queryBuilder.toString(), EmailFieldsMapReference.class);
        q.setParameter("reference", reference);
        return q;
    }
}
