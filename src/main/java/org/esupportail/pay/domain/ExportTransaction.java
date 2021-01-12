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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class ExportTransaction {

	public enum TypeTransaction {DEBIT, CREDIT, REMBOURSEMENT, ANNULATION}
	
    @Column(unique = true)
    private String numTransaction;
    
    private String numRemise;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MM")
    private Date transactionDate;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MM")
    private Date dateRemise;
    
    private String numContrat;
    
    private String reference;

    private Long montant;

    private String statut;

    private String email;
    
    @Enumerated(EnumType.STRING)
    private TypeTransaction typeTransaction;
    
    

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

	public String getNumTransaction() {
        return this.numTransaction;
    }

	public void setNumTransaction(String numTransaction) {
        this.numTransaction = numTransaction;
    }

	public String getNumRemise() {
        return this.numRemise;
    }

	public void setNumRemise(String numRemise) {
        this.numRemise = numRemise;
    }

	public Date getTransactionDate() {
        return this.transactionDate;
    }

	public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

	public Date getDateRemise() {
        return this.dateRemise;
    }

	public void setDateRemise(Date dateRemise) {
        this.dateRemise = dateRemise;
    }

	public String getNumContrat() {
        return this.numContrat;
    }

	public void setNumContrat(String numContrat) {
        this.numContrat = numContrat;
    }

	public String getReference() {
        return this.reference;
    }

	public void setReference(String reference) {
        this.reference = reference;
    }

	public Long getMontant() {
        return this.montant;
    }

	public void setMontant(Long montant) {
        this.montant = montant;
    }

	public String getStatut() {
        return this.statut;
    }

	public void setStatut(String statut) {
        this.statut = statut;
    }

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public TypeTransaction getTypeTransaction() {
        return this.typeTransaction;
    }

	public void setTypeTransaction(TypeTransaction typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("numTransaction", "numRemise", "transactionDate", "dateRemise", "numContrat", "reference", "montant", "statut", "email", "typeTransaction");

	public static final EntityManager entityManager() {
        EntityManager em = new ExportTransaction().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countExportTransactions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ExportTransaction o", Long.class).getSingleResult();
    }

	public static List<ExportTransaction> findAllExportTransactions() {
        return entityManager().createQuery("SELECT o FROM ExportTransaction o", ExportTransaction.class).getResultList();
    }

	public static List<ExportTransaction> findAllExportTransactions(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ExportTransaction o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ExportTransaction.class).getResultList();
    }

	public static ExportTransaction findExportTransaction(Long id) {
        if (id == null) return null;
        return entityManager().find(ExportTransaction.class, id);
    }

	public static List<ExportTransaction> findExportTransactionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ExportTransaction o", ExportTransaction.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<ExportTransaction> findExportTransactionEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ExportTransaction o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ExportTransaction.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            ExportTransaction attached = ExportTransaction.findExportTransaction(this.id);
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
    public ExportTransaction merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ExportTransaction merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static Long countFindExportTransactionsByNumRemiseAndStatutEqualsAndNumContratEquals(String numRemise, String statut, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (statut == null || statut.length() == 0) throw new IllegalArgumentException("The statut argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportTransaction.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportTransaction AS o WHERE o.numRemise = :numRemise AND o.statut = :statut  AND o.numContrat = :numContrat", Long.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("statut", statut);
        q.setParameter("numContrat", numContrat);
        return ((Long) q.getSingleResult());
    }

	public static Long countFindExportTransactionsByNumTransactionEqualsAndNumContratEquals(String numTransaction, String numContrat) {
        if (numTransaction == null || numTransaction.length() == 0) throw new IllegalArgumentException("The numTransaction argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportTransaction.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportTransaction AS o WHERE o.numTransaction = :numTransaction  AND o.numContrat = :numContrat", Long.class);
        q.setParameter("numTransaction", numTransaction);
        q.setParameter("numContrat", numContrat);
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<ExportTransaction> findExportTransactionsByNumRemiseAndStatutEqualsAndNumContratEquals(String numRemise, String statut, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (statut == null || statut.length() == 0) throw new IllegalArgumentException("The statut argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportTransaction.entityManager();
        TypedQuery<ExportTransaction> q = em.createQuery("SELECT o FROM ExportTransaction AS o WHERE o.numRemise = :numRemise AND o.statut = :statut  AND o.numContrat = :numContrat", ExportTransaction.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("statut", statut);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public static TypedQuery<ExportTransaction> findExportTransactionsByNumRemiseAndStatutEqualsAndNumContratEquals(String numRemise, String statut, String numContrat, String sortFieldName, String sortOrder) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (statut == null || statut.length() == 0) throw new IllegalArgumentException("The statut argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportTransaction.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ExportTransaction AS o WHERE o.numRemise = :numRemise AND o.statut = :statut  AND o.numContrat = :numContrat");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ExportTransaction> q = em.createQuery(queryBuilder.toString(), ExportTransaction.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("statut", statut);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public static TypedQuery<ExportTransaction> findExportTransactionsByNumTransactionEqualsAndNumContratEquals(String numTransaction, String numContrat) {
        if (numTransaction == null || numTransaction.length() == 0) throw new IllegalArgumentException("The numTransaction argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportTransaction.entityManager();
        TypedQuery<ExportTransaction> q = em.createQuery("SELECT o FROM ExportTransaction AS o WHERE o.numTransaction = :numTransaction  AND o.numContrat = :numContrat", ExportTransaction.class);
        q.setParameter("numTransaction", numTransaction);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public static TypedQuery<ExportTransaction> findExportTransactionsByNumTransactionEqualsAndNumContratEquals(String numTransaction, String numContrat, String sortFieldName, String sortOrder) {
        if (numTransaction == null || numTransaction.length() == 0) throw new IllegalArgumentException("The numTransaction argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        EntityManager em = ExportTransaction.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ExportTransaction AS o WHERE o.numTransaction = :numTransaction  AND o.numContrat = :numContrat");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ExportTransaction> q = em.createQuery(queryBuilder.toString(), ExportTransaction.class);
        q.setParameter("numTransaction", numTransaction);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
