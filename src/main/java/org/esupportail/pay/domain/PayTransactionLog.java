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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findPayTransactionLogsByIdtransEquals", "findPayTransactionLogsByPayEvtMontant" })
public class PayTransactionLog {

    @NotNull
    @ManyToOne
    private PayEvtMontant payEvtMontant;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MM")
    private Date transactionDate;
    
    private String field1;

    private String field2;

    private String mail;

    private String reference;

    private String montant;

    private String auto;

    private String erreur;

    private String idtrans;

    private String signature;
    
    private Boolean mailSent = null;
    
    /* pour compatibilité ascendante */
    public Boolean getMailSent() {
        return this.mailSent == null || this.mailSent;
    }

    public String getMontantDevise() {
        Double mnt = new Double(montant) / 100.0;
        return mnt.toString();
    }
    
    public Label getEvtTitle() {
    	return this.getPayEvtMontant().getEvt().getTitle();
    }

    public static Long countFindPayTransactionLogsByPayEvt(PayEvt payEvt) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt)", Long.class);
        q.setParameter("payEvt", payEvt);
        return ((Long) q.getSingleResult());
    }

    public static TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvt(PayEvt payEvt) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvt argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt)", PayTransactionLog.class);
        q.setParameter("payEvt", payEvt);
        return q;
    }

    public static TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvt(PayEvt payEvt, String sortFieldName, String sortOrder) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvt argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        String jpaQuery = "SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt)";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PayTransactionLog> q = em.createQuery(jpaQuery, PayTransactionLog.class);
        q.setParameter("payEvt", payEvt);
        return q;
    }
    
    public static List<Object[]> findNbTransactionByYear (){
        EntityManager em = PayEvt.entityManager();
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log GROUP BY year"
        		+ " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static List<Object[]> findMontantByYear (){
        EntityManager em = PayEvt.entityManager();
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log GROUP BY year"
        		+ " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static List<Object[]> findNbTransactionByMonth (){
        EntityManager em = PayEvt.entityManager();
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log GROUP BY year, month"
        		+ " ORDER BY year, month DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static List<Object[]> findMontantByMonth (){
        EntityManager em = PayEvt.entityManager();
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log GROUP BY year, month"
        		+ " ORDER BY year, month DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public static TypedQuery<PayTransactionLog> findAllPayTransactionLogsQuery(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayTransactionLog.class);
    }

	public static List<PayTransactionLog> findOldPayTransactionLogs(long oldDays) {
		Query q = entityManager().createQuery("select log from PayTransactionLog log where log.transactionDate < :oldDate");
		q.setParameter("oldDate", Date.from(Instant.now().minus(Duration.ofDays(oldDays))));
		return q.getResultList();	
	}

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("payEvtMontant", "transactionDate", "field1", "field2", "mail", "reference", "montant", "auto", "erreur", "idtrans", "signature", "mailSent");

	public static final EntityManager entityManager() {
        EntityManager em = new PayTransactionLog().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPayTransactionLogs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PayTransactionLog o", Long.class).getSingleResult();
    }

	public static List<PayTransactionLog> findAllPayTransactionLogs() {
        return entityManager().createQuery("SELECT o FROM PayTransactionLog o", PayTransactionLog.class).getResultList();
    }

	public static List<PayTransactionLog> findAllPayTransactionLogs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayTransactionLog.class).getResultList();
    }

	public static PayTransactionLog findPayTransactionLog(Long id) {
        if (id == null) return null;
        return entityManager().find(PayTransactionLog.class, id);
    }

	public static List<PayTransactionLog> findPayTransactionLogEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PayTransactionLog o", PayTransactionLog.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<PayTransactionLog> findPayTransactionLogEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PayTransactionLog.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            PayTransactionLog attached = PayTransactionLog.findPayTransactionLog(this.id);
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
    public PayTransactionLog merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PayTransactionLog merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public PayEvtMontant getPayEvtMontant() {
        return this.payEvtMontant;
    }

	public void setPayEvtMontant(PayEvtMontant payEvtMontant) {
        this.payEvtMontant = payEvtMontant;
    }

	public Date getTransactionDate() {
        return this.transactionDate;
    }

	public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
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

	public String getMail() {
        return this.mail;
    }

	public void setMail(String mail) {
        this.mail = mail;
    }

	public String getReference() {
        return this.reference;
    }

	public void setReference(String reference) {
        this.reference = reference;
    }

	public String getMontant() {
        return this.montant;
    }

	public void setMontant(String montant) {
        this.montant = montant;
    }

	public String getAuto() {
        return this.auto;
    }

	public void setAuto(String auto) {
        this.auto = auto;
    }

	public String getErreur() {
        return this.erreur;
    }

	public void setErreur(String erreur) {
        this.erreur = erreur;
    }

	public String getIdtrans() {
        return this.idtrans;
    }

	public void setIdtrans(String idtrans) {
        this.idtrans = idtrans;
    }

	public String getSignature() {
        return this.signature;
    }

	public void setSignature(String signature) {
        this.signature = signature;
    }

	public void setMailSent(Boolean mailSent) {
        this.mailSent = mailSent;
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

	public static Long countFindPayTransactionLogsByIdtransEquals(String idtrans) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.idtrans = :idtrans", Long.class);
        q.setParameter("idtrans", idtrans);
        return ((Long) q.getSingleResult());
    }

	public static Long countFindPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant", Long.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return ((Long) q.getSingleResult());
    }

	public static TypedQuery<PayTransactionLog> findPayTransactionLogsByIdtransEquals(String idtrans) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.idtrans = :idtrans", PayTransactionLog.class);
        q.setParameter("idtrans", idtrans);
        return q;
    }

	public static TypedQuery<PayTransactionLog> findPayTransactionLogsByIdtransEquals(String idtrans, String sortFieldName, String sortOrder) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayTransactionLog AS o WHERE o.idtrans = :idtrans");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayTransactionLog> q = em.createQuery(queryBuilder.toString(), PayTransactionLog.class);
        q.setParameter("idtrans", idtrans);
        return q;
    }

	public static TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant", PayTransactionLog.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public static TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant, String sortFieldName, String sortOrder) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        EntityManager em = PayTransactionLog.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayTransactionLog> q = em.createQuery(queryBuilder.toString(), PayTransactionLog.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }
}
