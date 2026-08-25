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
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PayTransactionLogDaoService {
    private static final String SUCCESS_ERROR_CODE = "00000";

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("payEvtMontant", "transactionDate", "field1", "field2", "mail", "reference", "montant", "auto", "erreur", "idtrans", "signature", "mailSent");

	@PersistenceContext
    EntityManager em;

    public TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvt(PayEvt payEvt, String sortFieldName, String sortOrder, Boolean successfulOnly) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvt argument is required");
        
        String jpaQuery = "SELECT o FROM PayTransactionLog AS o WHERE o.erreur " + (successfulOnly ? "=" : "!=") + " :successErrorCode AND o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt)" +
            toOrderBy(sortFieldName, sortOrder);
        
        TypedQuery<PayTransactionLog> q = em.createQuery(jpaQuery, PayTransactionLog.class);
        q.setParameter("payEvt", payEvt);
        q.setParameter("successErrorCode", SUCCESS_ERROR_CODE);
        return q;
    }

    public Page<PayTransactionLog> findPayTransactionLogsByIdAbo(String idAbo, Pageable pageable) {
        if (idAbo == null) throw new IllegalArgumentException("The idAbo argument is required");
        
        String jpaQuery = "SELECT o FROM PayTransactionLog AS o WHERE o.idAbo = :idAbo" +
            (pageable.getSort().iterator().hasNext() ? toOrderBy(pageable.getSort().iterator().next()) : "");
        TypedQuery<PayTransactionLog> q = em.createQuery(jpaQuery, PayTransactionLog.class);
        q.setParameter("idAbo", idAbo);
        return new PageImpl<>(q.getResultList());
    }

    public Page<PayTransactionLog> findPagePayTransactionLogsByPayEvt(PayEvt payEvt, String idAbo, Pageable pageable) {
        return findPagePayTransactionLogsByPayEvt(payEvt, idAbo, pageable, null);
    }

    public Page<PayTransactionLog> findPagePayTransactionLogsByPayEvt(PayEvt payEvt, String idAbo, Pageable pageable, Boolean successfulOnly) {
        return findPagePayTransactionLogsByPayEvt(payEvt, idAbo, pageable, successfulOnly, null, null, null, null);
    }

    public Page<PayTransactionLog> findPagePayTransactionLogsByPayEvt(PayEvt payEvt, String idAbo, Pageable pageable, Boolean successfulOnly, String field1, String field2, String mail, Boolean hasAbo) {
        if (payEvt == null) throw new IllegalArgumentException("The payEvt argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt) ");
        String queryCount = "SELECT COUNT(o) from PayTransactionLog AS o WHERE o.payEvtMontant in (select m FROM PayEvtMontant AS m WHERE m.evt = :payEvt) ";

        if (idAbo != null) {
            queryBuilder.append("AND o.idAbo = :idAbo ");
            queryCount += "AND o.idAbo = :idAbo ";
        }

        if (successfulOnly != null) {
            queryBuilder.append(successfulOnly ? "AND o.erreur = :successErrorCode " : "AND o.erreur <> :successErrorCode ");
            queryCount += successfulOnly ? "AND o.erreur = :successErrorCode " : "AND o.erreur <> :successErrorCode ";
        }

        if (field1 != null && !field1.isEmpty()) {
            queryBuilder.append("AND lower(o.field1) LIKE lower(:field1) ");
            queryCount += "AND lower(o.field1) LIKE lower(:field1) ";
        }

        if (field2 != null && !field2.isEmpty()) {
            queryBuilder.append("AND lower(o.field2) LIKE lower(:field2) ");
            queryCount += "AND lower(o.field2) LIKE lower(:field2) ";
        }

        if (mail != null && !mail.isEmpty()) {
            queryBuilder.append("AND lower(o.mail) LIKE lower(:mail) ");
            queryCount += "AND lower(o.mail) LIKE lower(:mail) ";
        }

        if (hasAbo != null) {
            if (hasAbo) {
                queryBuilder.append("AND (o.idAbo IS NOT NULL AND o.idAbo <> '0') ");
                queryCount += "AND (o.idAbo IS NOT NULL AND o.idAbo <> '0') ";
            } else {
                queryBuilder.append("AND (o.idAbo IS NULL OR o.idAbo = '0') ");
                queryCount += "AND (o.idAbo IS NULL OR o.idAbo = '0') ";
            }
        }

        Sort.Order sortFieldName = pageable.getSort().iterator().next();
        queryBuilder.append(toOrderBy(sortFieldName));

        TypedQuery<PayTransactionLog> q = em.createQuery(queryBuilder.toString(), PayTransactionLog.class);
        TypedQuery<Long> qCount = em.createQuery(queryCount, Long.class);
        q.setParameter("payEvt", payEvt);
        qCount.setParameter("payEvt", payEvt);
        if (idAbo != null) {
            q.setParameter("idAbo", idAbo);
            qCount.setParameter("idAbo", idAbo);
        }
        if (successfulOnly != null) {
            q.setParameter("successErrorCode", SUCCESS_ERROR_CODE);
            qCount.setParameter("successErrorCode", SUCCESS_ERROR_CODE);
        }
        if (field1 != null && !field1.isEmpty()) {
            q.setParameter("field1", "%" + field1 + "%");
            qCount.setParameter("field1", "%" + field1 + "%");
        }
        if (field2 != null && !field2.isEmpty()) {
            q.setParameter("field2", "%" + field2 + "%");
            qCount.setParameter("field2", "%" + field2 + "%");
        }
        if (mail != null && !mail.isEmpty()) {
            q.setParameter("mail", "%" + mail + "%");
            qCount.setParameter("mail", "%" + mail + "%");
        }
        if(pageable.isPaged()) {
            q.setFirstResult((int) pageable.getOffset());
            q.setMaxResults(pageable.getPageSize());
        }
        return new PageImpl<>(q.getResultList(), pageable, qCount.getSingleResult());
    }

    public List<Object[]> findNbTransactionByYear (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year"
        		+ " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findMontantByYear (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year"
        		+ " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findNbTransactionByMonth (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year, month"
        		+ " ORDER BY year, month DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findMontantByMonth (){
        
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year, month"
        		+ " ORDER BY year, month DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public TypedQuery<PayTransactionLog> findAllPayTransactionLogsQuery(String sortFieldName, String sortOrder, Boolean successfulOnly) {
        String jpaQuery = "SELECT o FROM PayTransactionLog o WHERE o.erreur" + (successfulOnly ? "=" : "!=") + " :successErrorCode" + toOrderBy(sortFieldName, sortOrder);
        return em.createQuery(jpaQuery, PayTransactionLog.class).setParameter("successErrorCode", SUCCESS_ERROR_CODE);
    }

	public List<PayTransactionLog> findOldPayTransactionLogs(long oldDays) {
		Query q = em.createQuery("select log from PayTransactionLog log where log.transactionDate < :oldDate and log.mail <> 'archived'");
    q.setParameter("oldDate", LocalDateTime.ofInstant(Instant.now().minus(Duration.ofDays(oldDays)), java.time.ZoneId.systemDefault()));
		return q.getResultList();
	}

    public Page<PayTransactionLog> findPageAllPayTransactionLogs(Pageable pageable) {
        return findPageAllPayTransactionLogs(pageable, null);
    }

    public Page<PayTransactionLog> findPageAllPayTransactionLogs(Pageable pageable, Boolean successfulOnly) {
        return findPageAllPayTransactionLogs(pageable, successfulOnly, null, null, null, null);
    }

    public Page<PayTransactionLog> findPageAllPayTransactionLogs(Pageable pageable, Boolean successfulOnly, String field1, String field2, String mail, Boolean hasAbo) {
        StringBuilder conditions = new StringBuilder();

        if (successfulOnly != null) {
            conditions.append(successfulOnly ? "o.erreur = :successErrorCode " : "o.erreur <> :successErrorCode ");
        }
        if (field1 != null && !field1.isEmpty()) {
            if (conditions.length() > 0) conditions.append("AND ");
            conditions.append("lower(o.field1) LIKE lower(:field1) ");
        }
        if (field2 != null && !field2.isEmpty()) {
            if (conditions.length() > 0) conditions.append("AND ");
            conditions.append("lower(o.field2) LIKE lower(:field2) ");
        }
        if (mail != null && !mail.isEmpty()) {
            if (conditions.length() > 0) conditions.append("AND ");
            conditions.append("lower(o.mail) LIKE lower(:mail) ");
        }
        if (hasAbo != null) {
            if (conditions.length() > 0) conditions.append("AND ");
            if (hasAbo) {
                conditions.append("(o.idAbo IS NOT NULL AND o.idAbo <> '0') ");
            } else {
                conditions.append("(o.idAbo IS NULL OR o.idAbo = '0') ");
            }
        }

        String whereClause = conditions.length() > 0 ? "WHERE " + conditions : "";
        String queryStr = "FROM PayTransactionLog o " + whereClause;
        String queryCount = "SELECT COUNT(o) FROM PayTransactionLog o " + whereClause;

        Sort.Order sortOrder = pageable.getSort().iterator().next();
        queryStr += toOrderBy(sortOrder);

        TypedQuery<PayTransactionLog> q = em.createQuery(queryStr, PayTransactionLog.class);
        TypedQuery<Long> qCount = em.createQuery(queryCount, Long.class);

        if (successfulOnly != null) {
            q.setParameter("successErrorCode", SUCCESS_ERROR_CODE);
            qCount.setParameter("successErrorCode", SUCCESS_ERROR_CODE);
        }
        if (field1 != null && !field1.isEmpty()) {
            q.setParameter("field1", "%" + field1 + "%");
            qCount.setParameter("field1", "%" + field1 + "%");
        }
        if (field2 != null && !field2.isEmpty()) {
            q.setParameter("field2", "%" + field2 + "%");
            qCount.setParameter("field2", "%" + field2 + "%");
        }
        if (mail != null && !mail.isEmpty()) {
            q.setParameter("mail", "%" + mail + "%");
            qCount.setParameter("mail", "%" + mail + "%");
        }
        if (pageable.isPaged()) {
            q.setFirstResult((int) pageable.getOffset());
            q.setMaxResults(pageable.getPageSize());
        }
        return new PageImpl<>(q.getResultList(), pageable, qCount.getSingleResult());
    }


    public PayTransactionLog findPayTransactionLog(Long id) {
        if (id == null) return null;
        return em.find(PayTransactionLog.class, id);
    }


	public Long countFindPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant", Long.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return ((Long) q.getSingleResult());
    }

    public TypedQuery<PayTransactionLog> findPayTransactionLogsByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        TypedQuery q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.payEvtMontant = :payEvtMontant", PayTransactionLog.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public TypedQuery<PayTransactionLog> findPayTransactionLogsByIdtransEquals(String idtrans) {
        if (idtrans == null || idtrans.length() == 0) throw new IllegalArgumentException("The idtrans argument is required");
        
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.idtrans = :idtrans", PayTransactionLog.class);
        q.setParameter("idtrans", idtrans);
        return q;
    }

	public void persist(PayTransactionLog txLog) {
		em.persist(txLog);
	}

    public List<String> findDistinctYears() {
        String sql = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year FROM pay_transaction_log GROUP BY year"
                + " ORDER BY year DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

    public PayTransactionLog findPayTransactionLogByReference(String reference) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        TypedQuery<PayTransactionLog> q = em.createQuery("SELECT o FROM PayTransactionLog AS o WHERE o.reference = :reference", PayTransactionLog.class);
        q.setParameter("reference", reference);
        List<PayTransactionLog> resultList = q.getResultList();
        if (resultList.isEmpty()) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

    private String toOrderBy(String sortFieldName, String sortOrder) {
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                return " ORDER BY " + sortFieldName + " " + sortOrder;
            } else {
                return " ORDER BY " + sortFieldName;
            }
        }
        return "";
    }

    private String toOrderBy(Sort.Order sort) {
        return toOrderBy(sort.getProperty(), sort.getDirection().name());
    }
}