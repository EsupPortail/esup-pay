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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.stereotype.Service;

@Service
public class EmailFieldsMapReferenceDaoService {

	@PersistenceContext
    EntityManager em;

	public List<EmailFieldsMapReference> findOldEmailFieldsMapReferences(long oldDays) {
		Query q = em.createQuery("select ref from EmailFieldsMapReference ref where ref.dateCreated is null or ref.dateCreated < :oldDate");
		q.setParameter("oldDate", Date.from(Instant.now().minus(Duration.ofDays(oldDays))));
		return q.getResultList();		
	}


	public Long countFindEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant", Long.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return ((Long) q.getSingleResult());
    }


	public TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByReferenceEquals(String reference) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        TypedQuery<EmailFieldsMapReference> q = em.createQuery("SELECT o FROM EmailFieldsMapReference AS o WHERE o.reference = :reference", EmailFieldsMapReference.class);
        q.setParameter("reference", reference);
        return q;
    }

	public void remove(EmailFieldsMapReference emailFieldsMapReference) {
		em.remove(emailFieldsMapReference);
	}

	public void persist(EmailFieldsMapReference emailMapFirstLastName) {
		em.persist(emailMapFirstLastName);
	}
	
}