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
package org.esupportail.pay.dao;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayTransactionLogDaoService {
	
	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("payEvtMontant", "transactionDate", "field1", "field2", "mail", "reference", "montant", "auto", "erreur", "idtrans", "signature", "mailSent");

	@PersistenceContext
    EntityManager em;

    public Long countFindPayTransactionLogsByPayEvt(PayEvt payEvt) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt)", Long.class);
        q.setParameter("payEvt", payEvt);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvt(PayEvt payEvt) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvt argument is required");
        
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt)", PayTransactionLog.class);
        q.setParameter("payEvt", payEvt);
        return q;
    }

    public TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvt(PayEvt payEvt, String sortFieldName, String sortOrder) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvt argument is required");
        
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
    
    public List<Object[]> findNbTransactionByYear (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log GROUP BY year"
        		+ " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findMontantByYear (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log GROUP BY year"
        		+ " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findNbTransactionByMonth (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log GROUP BY year, month"
        		+ " ORDER BY year, month DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findMontantByMonth (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log GROUP BY year, month"
        		+ " ORDER BY year, month DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public TypedQuery<PayTransactionLog> findAllPayTransactionLogsQuery(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayTransactionLog.class);
    }

	public List<PayTransactionLog> findOldPayTransactionLogs(long oldDays) {
		Query q = em.createQuery("select log from PayTransactionLog log where log.transactionDate < :oldDate");
		q.setParameter("oldDate", Date.from(Instant.now().minus(Duration.ofDays(oldDays))));
		return q.getResultList();	
	}

	public long countPayTransactionLogs() {
        return em.createQuery("SELECT COUNT(o) FROM PayTransactionLog o", Long.class).getSingleResult();
    }

	public List<PayTransactionLog> findAllPayTransactionLogs() {
        return em.createQuery("SELECT o FROM PayTransactionLog o", PayTransactionLog.class).getResultList();
    }

	public List<PayTransactionLog> findAllPayTransactionLogs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayTransactionLog.class).getResultList();
    }

	public PayTransactionLog findPayTransactionLog(Long id) {
        if (id == null) return null;
        return em.find(PayTransactionLog.class, id);
    }

	public List<PayTransactionLog> findPayTransactionLogEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM PayTransactionLog o", PayTransactionLog.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<PayTransactionLog> findPayTransactionLogEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayTransactionLog.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public Long countFindPayTransactionLogsByIdtransEquals(String idtrans) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.idtrans = :idtrans", Long.class);
        q.setParameter("idtrans", idtrans);
        return ((Long) q.getSingleResult());
    }

	public Long countFindPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant", Long.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<PayTransactionLog> findPayTransactionLogsByIdtransEquals(String idtrans) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.idtrans = :idtrans", PayTransactionLog.class);
        q.setParameter("idtrans", idtrans);
        return q;
    }

	public TypedQuery<PayTransactionLog> findPayTransactionLogsByIdtransEquals(String idtrans, String sortFieldName, String sortOrder) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        
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

	public TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant", PayTransactionLog.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant, String sortFieldName, String sortOrder) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        
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

	public void persist(PayTransactionLog txLog) {
		em.persist(txLog);
	}
}
