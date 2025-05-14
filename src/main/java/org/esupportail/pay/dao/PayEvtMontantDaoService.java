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

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.Valid;

import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PayEvtMontantDaoService {

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

	public TypedQuery<PayEvtMontant> findPayEvtMontantsByEvtAndUrlIdEquals(PayEvt evt, String urlId) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        TypedQuery<PayEvtMontant> q = em.createQuery("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt AND o.urlId = :urlId", PayEvtMontant.class);
        q.setParameter("evt", evt);
        q.setParameter("urlId", urlId);
        return q;
    }

    public Page<PayEvtMontant> findPagePayEvtMontantsByEvt(PayEvt evt, Pageable pageable) {
        if (evt == null) throw new IllegalArgumentException("The evt argument is required");
        TypedQuery<PayEvtMontant> q = em.createQuery("SELECT o FROM PayEvtMontant AS o WHERE o.evt = :evt", PayEvtMontant.class);
        q.setParameter("evt", evt);
        return new PageImpl<>(q.getResultList(), pageable, q.getResultList().size());
    }

	public PayEvtMontant findPayEvtMontant(Long id) {
        if (id == null) return null;
        return em.find(PayEvtMontant.class, id);
    }

	public void persist(@Valid PayEvtMontant payEvtMontant) {
		em.persist(payEvtMontant);
	}

	public void remove(PayEvtMontant payEvtMontant) {
		em.remove(payEvtMontant);
	}

	public void merge(@Valid PayEvtMontant payEvtMontant) {
		em.merge(payEvtMontant);
	}

}