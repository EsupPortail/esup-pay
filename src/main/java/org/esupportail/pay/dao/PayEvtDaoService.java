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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayEvtDaoService {

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("id", "archived, id", "logoFile", "payboxServiceKey", "title", "webSiteUrl", "urlId", "managersEmail", "mailSubject", "payboxCommandPrefix", "respLogins", "viewerLogins", "defaultMntDescription", "logins", "viewerLogins2Add");
	

	@PersistenceContext
    EntityManager em;
	
	public TypedQuery<PayEvt> findPayEvtsByRespLogins(List<RespLogin> respLogins, String sortFieldName, String sortOrder) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
        	queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" " + sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return q;
    }
    
    
    public TypedQuery<PayEvt> findPayEvtsByViewerLogins(List<RespLogin> viewerLogins, String sortFieldName, String sortOrder) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
        	queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" " + sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _viewerLogin: viewerLogins) {
            q.setParameter("respLogins_item" + viewerLoginsIndex++, _viewerLogin);
        }
        return q;
    }
    
    public TypedQuery<PayEvt> findPayEvtsByRespLoginsOrByViewerLogins(List<RespLogin> logins, String sortFieldName, String sortOrder) {
        if (logins == null) throw new IllegalArgumentException("The logins argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < logins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :logins_item").append(i).append(" MEMBER OF o.respLogins").append(" OR :logins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
        	queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" " + sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _login: logins) {
            q.setParameter("logins_item" + respLoginsIndex++, _login);
        }
        return q;
    }
    
    public List<Object[]> findSumMontantGroupByEvt (){
        
        String sql = "SELECT mail_subject, SUM(cast(montant AS INTEGER)/100), pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id  "
        		+ "GROUP BY pay_evt.id, pay_evt.mail_subject ORDER BY pay_evt.id DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }
    
    public List<Object[]> findNbParticipantsByEvt (){
        
        String sql = "SELECT mail_subject, COUNT(evt) AS count, pay_evt.id FROM pay_transaction_log, pay_evt_montant, pay_evt "
        		+ "WHERE pay_transaction_log.pay_evt_montant = pay_evt_montant.id AND pay_evt_montant.evt = pay_evt.id  "
        		+ "GROUP BY pay_evt.id, pay_evt.mail_subject ORDER BY pay_evt.id DESC";
        Query q = em.createNativeQuery(sql);
        return q.getResultList();
    }

	public Long countFindPayEvtsByRespLogins(List<RespLogin> respLogins) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        TypedQuery q = em.createQuery(queryBuilder.toString(), Long.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return ((Long) q.getSingleResult());
    }

	public Long countFindPayEvtsByUrlIdEquals(String urlId) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvt AS o WHERE o.urlId = :urlId", Long.class);
        q.setParameter("urlId", urlId);
        return ((Long) q.getSingleResult());
    }

	public Long countFindPayEvtsByViewerLogins(List<RespLogin> viewerLogins) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        TypedQuery q = em.createQuery(queryBuilder.toString(), Long.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _resplogin: viewerLogins) {
            q.setParameter("viewerLogins_item" + viewerLoginsIndex++, _resplogin);
        }
        return ((Long) q.getSingleResult());
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

	public TypedQuery<PayEvt> findPayEvtsByUrlIdEquals(String urlId, String sortFieldName, String sortOrder) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE o.urlId = :urlId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
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

	public long countPayEvts() {
        return em.createQuery("SELECT COUNT(o) FROM PayEvt o", Long.class).getSingleResult();
    }

	public List<PayEvt> findAllPayEvts() {
        return em.createQuery("SELECT o FROM PayEvt o", PayEvt.class).getResultList();
    }

	public List<PayEvt> findAllPayEvts(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvt o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayEvt.class).getResultList();
    }

	public PayEvt findPayEvt(Long id) {
        if (id == null) return null;
        return em.find(PayEvt.class, id);
    }

	public List<PayEvt> findPayEvtEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM PayEvt o", PayEvt.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
