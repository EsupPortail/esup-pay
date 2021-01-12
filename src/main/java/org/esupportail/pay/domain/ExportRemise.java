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
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
public class ExportRemise {

	@Column(unique=true)
	private String numRemise;
	
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MM")
    private Date dateRemise;
    
    private Long nbTransactions;

    private Long montant;
      
    private String numContrat;


	public static Long countFindExportRemisesByDateRemiseBetween(Date minDateRemise, Date maxDateRemise) {
        if (minDateRemise == null) throw new IllegalArgumentException("The minDateRemise argument is required");
        if (maxDateRemise == null) throw new IllegalArgumentException("The maxDateRemise argument is required");
        EntityManager em = ExportRemise.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportRemise AS o WHERE o.dateRemise BETWEEN :minDateRemise AND :maxDateRemise", Long.class);
        q.setParameter("minDateRemise", minDateRemise);
        q.setParameter("maxDateRemise", maxDateRemise);
        return ((Long) q.getSingleResult());
    }

	public static Long countFindExportRemisesByNumRemiseEqualsAndNumContratEquals(String numRemise, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportRemise.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportRemise AS o WHERE o.numRemise = :numRemise  AND o.numContrat = :numContrat", Long.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("numContrat", numContrat);
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<ExportRemise> findExportRemisesByDateRemiseBetween(Date minDateRemise, Date maxDateRemise) {
        if (minDateRemise == null) throw new IllegalArgumentException("The minDateRemise argument is required");
        if (maxDateRemise == null) throw new IllegalArgumentException("The maxDateRemise argument is required");
        EntityManager em = ExportRemise.entityManager();
        TypedQuery<ExportRemise> q = em.createQuery("SELECT o FROM ExportRemise AS o WHERE o.dateRemise BETWEEN :minDateRemise AND :maxDateRemise", ExportRemise.class);
        q.setParameter("minDateRemise", minDateRemise);
        q.setParameter("maxDateRemise", maxDateRemise);
        return q;
    }

	public static TypedQuery<ExportRemise> findExportRemisesByDateRemiseBetween(Date minDateRemise, Date maxDateRemise, String sortFieldName, String sortOrder) {
        if (minDateRemise == null) throw new IllegalArgumentException("The minDateRemise argument is required");
        if (maxDateRemise == null) throw new IllegalArgumentException("The maxDateRemise argument is required");
        EntityManager em = ExportRemise.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ExportRemise AS o WHERE o.dateRemise BETWEEN :minDateRemise AND :maxDateRemise");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ExportRemise> q = em.createQuery(queryBuilder.toString(), ExportRemise.class);
        q.setParameter("minDateRemise", minDateRemise);
        q.setParameter("maxDateRemise", maxDateRemise);
        return q;
    }

	public static TypedQuery<ExportRemise> findExportRemisesByNumRemiseEqualsAndNumContratEquals(String numRemise, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportRemise.entityManager();
        TypedQuery<ExportRemise> q = em.createQuery("SELECT o FROM ExportRemise AS o WHERE o.numRemise = :numRemise  AND o.numContrat = :numContrat", ExportRemise.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public static TypedQuery<ExportRemise> findExportRemisesByNumRemiseEqualsAndNumContratEquals(String numRemise, String numContrat, String sortFieldName, String sortOrder) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportRemise.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ExportRemise AS o WHERE o.numRemise = :numRemise  AND o.numContrat = :numContrat");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ExportRemise> q = em.createQuery(queryBuilder.toString(), ExportRemise.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("numRemise", "dateRemise", "nbTransactions", "montant", "numContrat");

	public static final EntityManager entityManager() {
        EntityManager em = new ExportRemise().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countExportRemises() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ExportRemise o", Long.class).getSingleResult();
    }

	public static List<ExportRemise> findAllExportRemises() {
        return entityManager().createQuery("SELECT o FROM ExportRemise o", ExportRemise.class).getResultList();
    }

	public static List<ExportRemise> findAllExportRemises(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ExportRemise o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ExportRemise.class).getResultList();
    }

	public static ExportRemise findExportRemise(Long id) {
        if (id == null) return null;
        return entityManager().find(ExportRemise.class, id);
    }

	public static List<ExportRemise> findExportRemiseEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ExportRemise o", ExportRemise.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<ExportRemise> findExportRemiseEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ExportRemise o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ExportRemise.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            ExportRemise attached = ExportRemise.findExportRemise(this.id);
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
    public ExportRemise merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ExportRemise merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
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
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getNumRemise() {
        return this.numRemise;
    }

	public void setNumRemise(String numRemise) {
        this.numRemise = numRemise;
    }

	public Date getDateRemise() {
        return this.dateRemise;
    }

	public void setDateRemise(Date dateRemise) {
        this.dateRemise = dateRemise;
    }

	public Long getNbTransactions() {
        return this.nbTransactions;
    }

	public void setNbTransactions(Long nbTransactions) {
        this.nbTransactions = nbTransactions;
    }

	public Long getMontant() {
        return this.montant;
    }

	public void setMontant(Long montant) {
        this.montant = montant;
    }

	public String getNumContrat() {
        return this.numContrat;
    }

	public void setNumContrat(String numContrat) {
        this.numContrat = numContrat;
    }
}
