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

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.Valid;

import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.stereotype.Service;

@Service
public class PayEvtMontantDaoService {

	public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("evt", "title", "description", "help", "field1Label", "field2Label", "dbleMontant", "urlId", "freeAmount", "sciencesconf", "addPrefix", "optionalAddedParams", "isEnabled");

	@PersistenceContext
    EntityManager em;	
	
	@Resource
	EmailFieldsMapReferenceDaoService emailFieldsMapReferenceDaoService;
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;
	
	public boolean isDeletable(PayEvtMontant payEvtMontant) {
    	return payTransactionLogDaoService.countFindPayTransactionLogsByPayEvtMontant(payEvtMontant) < 1 
    			&& emailFieldsMapReferenceDaoService.countFindEmailFieldsMapReferencesByPayEvtMontant(payEvtMontant) < 1;
    }
	
	public Long countFindPayEvtMontantsByEvt(PayEvt evt) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvtMontant AS o WHERE o.evt = :evt", Long.class);
        q.setParameter("evt", evt);
        return ((Long) q.getSingleResult());
    }

	public Long countFindPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId", Long.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<PayEvtMontant> findPayEvtMontantsByEvt(PayEvt evt) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        TypedQuery<PayEvtMontant> q = em.createQuery("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt", PayEvtMontant.class);
        q.setParameter("evt", evt);
        return q;
    }

	public TypedQuery<PayEvtMontant> findPayEvtMontantsByEvt(PayEvt evt, String sortFieldName, String sortOrder) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvtMontant> q = em.createQuery(queryBuilder.toString(), PayEvtMontant.class);
        q.setParameter("evt", evt);
        return q;
    }

	public TypedQuery<PayEvtMontant> findPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        TypedQuery<PayEvtMontant> q = em.createQuery("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId", PayEvtMontant.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return q;
    }

	public TypedQuery<PayEvtMontant> findPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId, String sortFieldName, String sortOrder) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvtMontant> q = em.createQuery(queryBuilder.toString(), PayEvtMontant.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return q;
    }

	public long countPayEvtMontants() {
        return em.createQuery("SELECT COUNT(o) FROM PayEvtMontant o", Long.class).getSingleResult();
    }

	public List<PayEvtMontant> findAllPayEvtMontants() {
        return em.createQuery("SELECT o FROM PayEvtMontant o", PayEvtMontant.class).getResultList();
    }

	public List<PayEvtMontant> findAllPayEvtMontants(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvtMontant o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayEvtMontant.class).getResultList();
    }

	public PayEvtMontant findPayEvtMontant(Long id) {
        if (id == null) return null;
        return em.find(PayEvtMontant.class, id);
    }

	public List<PayEvtMontant> findPayEvtMontantEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM PayEvtMontant o", PayEvtMontant.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<PayEvtMontant> findPayEvtMontantEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PayEvtMontant o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, PayEvtMontant.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public void persist(@Valid PayEvtMontant payEvtMontant) {
		em.persist(payEvtMontant);
	}

	public void remove(PayEvtMontant payEvtMontant) {
		em.remove(payEvtMontant);
	}

}
