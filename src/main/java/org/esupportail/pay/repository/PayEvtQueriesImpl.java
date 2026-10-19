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
package org.esupportail.pay.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/** Event queries requiring translated titles or PostgreSQL aggregation. */
@Transactional(readOnly = true)
public class PayEvtQueriesImpl implements PayEvtQueries {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<PayEvt> search(String title, Pageable pageable) {
        return search(null, title, pageable);
    }

    @Override
    public Page<PayEvt> searchVisible(List<RespLogin> logins, String title, Pageable pageable) {
        Assert.notNull(logins, "The logins argument is required");
        return search(logins, title, pageable);
    }

    private Page<PayEvt> search(List<RespLogin> logins, String title, Pageable pageable) {
        String from = " FROM PayEvt o JOIN o.title.labelLocales titleFr WHERE KEY(titleFr) = 'fr'";
        if (title != null && !title.isEmpty()) from += " AND lower(titleFr.translation) LIKE lower(:title)";
        if (logins != null) from += membership(logins, true, true);
        List<String> orders = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            if (PayEvtRepository.fieldNames4OrderClauseFilter.contains(order.getProperty())) {
                String property = "title".equals(order.getProperty()) ? "titleFr.translation" : "o." + order.getProperty();
                orders.add(property + " " + order.getDirection().name());
            }
        });
        String orderBy = orders.isEmpty() ? "" : " ORDER BY " + String.join(", ", orders);
        var results = em.createQuery("SELECT o" + from + orderBy, PayEvt.class);
        var count = em.createQuery("SELECT COUNT(o)" + from, Long.class);
        if (title != null && !title.isEmpty()) {
            results.setParameter("title", "%" + title + "%");
            count.setParameter("title", "%" + title + "%");
        }
        if (logins != null) {
            for (int i = 0; i < logins.size(); i++) {
                results.setParameter("login" + i, logins.get(i));
                count.setParameter("login" + i, logins.get(i));
            }
        }
        if (pageable.isPaged()) {
            results.setFirstResult(Math.toIntExact(pageable.getOffset()));
            results.setMaxResults(pageable.getPageSize());
        }
        return new PageImpl<>(results.getResultList(), pageable, count.getSingleResult());
    }

    @Override
    public List<PayEvt> findPayEvtsByRespLogins(List<RespLogin> logins) {
        return findByMembership(logins, true, false);
    }

    @Override
    public List<PayEvt> findPayEvtsByViewerLogins(List<RespLogin> logins) {
        return findByMembership(logins, false, true);
    }

    private List<PayEvt> findByMembership(List<RespLogin> logins, boolean managers, boolean viewers) {
        Assert.notNull(logins, "The logins argument is required");
        var query = em.createQuery("SELECT o FROM PayEvt o WHERE 1 = 1" + membership(logins, managers, viewers), PayEvt.class);
        for (int i = 0; i < logins.size(); i++) query.setParameter("login" + i, logins.get(i));
        return query.getResultList();
    }

    private String membership(List<RespLogin> logins, boolean managers, boolean viewers) {
        if (logins.isEmpty()) return " AND 1 = 0";
        List<String> predicates = new ArrayList<>();
        for (int i = 0; i < logins.size(); i++) {
            List<String> alternatives = new ArrayList<>();
            if (managers) alternatives.add(":login" + i + " MEMBER OF o.respLogins");
            if (viewers) alternatives.add(":login" + i + " MEMBER OF o.viewerLogins");
            predicates.add("(" + String.join(" OR ", alternatives) + ")");
        }
        return " AND " + String.join(" AND ", predicates);
    }

    public List<Object[]> findSumMontantGroupByEvt(String year){
        
        String sql = "SELECT mail_subject || '(' || pay_evt.id || ')' as title, SUM(cast(montant AS INTEGER)/100) as total, pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id AND pay_transaction_log.erreur = '00000' ";
        if(StringUtils.isNotEmpty(year)) {
            sql += " AND CAST(date_part('year',transaction_date) AS varchar) = :year ";
        }
        sql += "GROUP BY pay_evt.id ORDER BY total DESC";
        Query q = em.createNativeQuery(sql);
        if(StringUtils.isNotEmpty(year)) {
            q.setParameter("year", year);
        }
        return q.getResultList();
    }
    
    public List<Object[]> findNbParticipantsByEvt(String year){
        
        String sql = "SELECT mail_subject || '(' || pay_evt.id || ')' as title, COUNT(evt) AS totalCount, pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id AND pay_transaction_log.erreur = '00000' ";
        if(StringUtils.isNotEmpty(year)) {
            sql += " AND CAST(date_part('year',transaction_date) AS varchar) = :year ";
        }
        sql += "GROUP BY pay_evt.id ORDER BY totalCount DESC";
        Query q = em.createNativeQuery(sql);
        if(StringUtils.isNotEmpty(year)) {
            q.setParameter("year", year);
        }
        return q.getResultList();
    }

}
