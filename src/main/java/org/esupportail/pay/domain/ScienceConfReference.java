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
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceContext;
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

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findScienceConfReferencesByEmailFieldsMapReference" })
public class ScienceConfReference {

    @NotNull
    @OneToOne
    private EmailFieldsMapReference emailFieldsMapReference;

    private String uid;

    private String confid;

    private String returnurl;
    
    private Date dateCreated;

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("emailFieldsMapReference", "uid", "confid", "returnurl", "dateCreated");

	public static final EntityManager entityManager() {
        EntityManager em = new ScienceConfReference().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countScienceConfReferences() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ScienceConfReference o", Long.class).getSingleResult();
    }

	public static List<ScienceConfReference> findAllScienceConfReferences() {
        return entityManager().createQuery("SELECT o FROM ScienceConfReference o", ScienceConfReference.class).getResultList();
    }

	public static List<ScienceConfReference> findAllScienceConfReferences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ScienceConfReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ScienceConfReference.class).getResultList();
    }

	public static ScienceConfReference findScienceConfReference(Long id) {
        if (id == null) return null;
        return entityManager().find(ScienceConfReference.class, id);
    }

	public static List<ScienceConfReference> findScienceConfReferenceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ScienceConfReference o", ScienceConfReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<ScienceConfReference> findScienceConfReferenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ScienceConfReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ScienceConfReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            ScienceConfReference attached = ScienceConfReference.findScienceConfReference(this.id);
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
    public ScienceConfReference merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ScienceConfReference merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public EmailFieldsMapReference getEmailFieldsMapReference() {
        return this.emailFieldsMapReference;
    }

	public void setEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference) {
        this.emailFieldsMapReference = emailFieldsMapReference;
    }

	public String getUid() {
        return this.uid;
    }

	public void setUid(String uid) {
        this.uid = uid;
    }

	public String getConfid() {
        return this.confid;
    }

	public void setConfid(String confid) {
        this.confid = confid;
    }

	public String getReturnurl() {
        return this.returnurl;
    }

	public void setReturnurl(String returnurl) {
        this.returnurl = returnurl;
    }

	public Date getDateCreated() {
        return this.dateCreated;
    }

	public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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

	public static Long countFindScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        EntityManager em = ScienceConfReference.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference", Long.class);
        q.setParameter("emailFieldsMapReference", emailFieldsMapReference);
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<ScienceConfReference> findScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        EntityManager em = ScienceConfReference.entityManager();
        TypedQuery<ScienceConfReference> q = em.createQuery("SELECT o FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference", ScienceConfReference.class);
        q.setParameter("emailFieldsMapReference", emailFieldsMapReference);
        return q;
    }

	public static TypedQuery<ScienceConfReference> findScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference, String sortFieldName, String sortOrder) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        EntityManager em = ScienceConfReference.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ScienceConfReference> q = em.createQuery(queryBuilder.toString(), ScienceConfReference.class);
        q.setParameter("emailFieldsMapReference", emailFieldsMapReference);
        return q;
    }
}
