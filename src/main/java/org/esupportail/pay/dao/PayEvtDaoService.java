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
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayEvtDaoService {

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("id", "archived", "archived, id", "logoFile", "payboxServiceKey", "title", "webSiteUrl", "urlId", "managersEmail", "mailSubject", "payboxCommandPrefix", "respLogins", "viewerLogins", "defaultMntDescription", "logins", "viewerLogins2Add");

	@PersistenceContext
    EntityManager em;

    /**
     * Select PayEvts for logins in RespLogins or ViewerLogins in a Pageable.
     * Not just a wrapper to findPayEvtsByRespLoginsOrByViewerLogins, because it
     * only selects the current page of Pageable.
     */
    public Page<PayEvt> findPagePayEvtsByRespLoginsOrByViewerLogins(List<RespLogin> logins, Pageable pageable, List<Sort.Order> orders) {
        if (logins == null) throw new IllegalArgumentException("The logins argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        StringBuilder countBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");

        StringBuilder whereBuilder = new StringBuilder();
        for (int i = 0; i < logins.size(); i++) {
            if (i > 0) whereBuilder.append(" AND");
            whereBuilder.append(" :logins_item").append(i).append(" MEMBER OF o.respLogins")
                        .append(" OR :logins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        queryBuilder.append(whereBuilder);
        countBuilder.append(whereBuilder);

        if (orders != null && !orders.isEmpty()) {
            queryBuilder.append(" ORDER BY ");
            for (int i = 0; i<orders.size();  i++) {
                String sortOrder = orders.get(i).getDirection().name();
                if (fieldNames4OrderClauseFilter.contains(orders.get(i).getProperty())) {
                    queryBuilder.append(orders.get(i).getProperty());
                    if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                        queryBuilder.append(" ").append(sortOrder);
                    }
                }
                if (i < orders.size() - 1) queryBuilder.append(", ");
            }
        }

        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        TypedQuery<Long> qCount = em.createQuery(countBuilder.toString(), Long.class);

        int respLoginsIndex = 0;
        for (RespLogin _login: logins) {
            q.setParameter("logins_item" + respLoginsIndex, _login);
            qCount.setParameter("logins_item" + respLoginsIndex, _login);
            respLoginsIndex++;
        }

        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        List<PayEvt> results = q.getResultList();
        Long total = qCount.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
    
    public List<Object[]> findSumMontantGroupByEvt(String year){
        
        String sql = "SELECT mail_subject || '(' || pay_evt.id || ')' as title, SUM(cast(montant AS INTEGER)/100) as total, pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id  ";
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
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id  ";
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

	public TypedQuery<PayEvt> findPayEvtsByRespLogins(List<RespLogin> respLogins) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return q;
    }

	public TypedQuery<PayEvt> findPayEvtsByUrlIdEquals(String urlId) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        
        TypedQuery<PayEvt> q = em.createQuery("SELECT o FROM PayEvt AS o WHERE o.urlId = :urlId", PayEvt.class);
        q.setParameter("urlId", urlId);
        return q;
    }

	public TypedQuery<PayEvt> findPayEvtsByViewerLogins(List<RespLogin> viewerLogins) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _resplogin: viewerLogins) {
            q.setParameter("viewerLogins_item" + viewerLoginsIndex++, _resplogin);
        }
        return q;
    }

	public List<PayEvt> findAllPayEvts() {
        return em.createQuery("SELECT o FROM PayEvt o", PayEvt.class).getResultList();
    }

    public Page<PayEvt> findPagePayEvts(Pageable pageable, List<Sort.Order> orders) {
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o");
        StringBuilder countBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o");

        if (orders != null && !orders.isEmpty()) {
            queryBuilder.append(" ORDER BY ");
            for (int i = 0; i<orders.size();  i++) {
                String sortOrder = orders.get(i).getDirection().name();
                if (fieldNames4OrderClauseFilter.contains(orders.get(i).getProperty())) {
                    queryBuilder.append(orders.get(i).getProperty());
                    if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                        queryBuilder.append(" ").append(sortOrder);
                    }
                }
                if (i < orders.size() - 1) queryBuilder.append(", ");
            }
        }

        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        TypedQuery<Long> qCount = em.createQuery(countBuilder.toString(), Long.class);

        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        List<PayEvt> results = q.getResultList();
        Long total = qCount.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

	public PayEvt findPayEvt(Long id) {
        if (id == null) return null;
        return em.find(PayEvt.class, id);
    }

	public List<PayEvt> findPayEvtEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvt o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayEvt.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
	public void persist(PayEvt payEvt) {
		em.persist(payEvt);
	}


	public void remove(PayEvt payEvt) {
		em.remove(payEvt);
	}


	public void merge(PayEvt payEvt) {
		em.merge(payEvt);
	}

}