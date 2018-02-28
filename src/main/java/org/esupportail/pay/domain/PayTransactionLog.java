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

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

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
}
