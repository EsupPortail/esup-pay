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

import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailFieldsMapReferenceDaoService {

	@PersistenceContext
    EntityManager em;
	
	public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("payEvtMontant", "reference", "mail", "field1", "field2", "dateCreated");

	public List<EmailFieldsMapReference> findOldEmailFieldsMapReferences(long oldDays) {
		Query q = em.createQuery("select ref from EmailFieldsMapReference ref where ref.dateCreated is null or ref.dateCreated < :oldDate");
		q.setParameter("oldDate", Date.from(Instant.now().minus(Duration.ofDays(oldDays))));
		return q.getResultList();		
	}
	
	public long countEmailFieldsMapReferences() {
        return em.createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference o", Long.class).getSingleResult();
    }

	public List<EmailFieldsMapReference> findAllEmailFieldsMapReferences() {
        return em.createQuery("SELECT o FROM EmailFieldsMapReference o", EmailFieldsMapReference.class).getResultList();
    }

	public List<EmailFieldsMapReference> findAllEmailFieldsMapReferences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM EmailFieldsMapReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, EmailFieldsMapReference.class).getResultList();
    }

	public EmailFieldsMapReference findEmailFieldsMapReference(Long id) {
        if (id == null) return null;
        return em.find(EmailFieldsMapReference.class, id);
    }

	public List<EmailFieldsMapReference> findEmailFieldsMapReferenceEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM EmailFieldsMapReference o", EmailFieldsMapReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<EmailFieldsMapReference> findEmailFieldsMapReferenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM EmailFieldsMapReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, EmailFieldsMapReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public Long countFindEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant", Long.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return ((Long) q.getSingleResult());
    }

	public Long countFindEmailFieldsMapReferencesByReferenceEquals(String reference) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM EmailFieldsMapReference AS o WHERE o.reference = :reference", Long.class);
        q.setParameter("reference", reference);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        TypedQuery<EmailFieldsMapReference> q = em.createQuery("SELECT o FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant", EmailFieldsMapReference.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByPayEvtMontant(PayEvtMontant payEvtMontant, String sortFieldName, String sortOrder) {
        if (payEvtMontant == null) throw new IllegalArgumentException("The payEvtMontant argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM EmailFieldsMapReference AS o WHERE o.payEvtMontant = :payEvtMontant");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<EmailFieldsMapReference> q = em.createQuery(queryBuilder.toString(), EmailFieldsMapReference.class);
        q.setParameter("payEvtMontant", payEvtMontant);
        return q;
    }

	public TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByReferenceEquals(String reference) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        TypedQuery<EmailFieldsMapReference> q = em.createQuery("SELECT o FROM EmailFieldsMapReference AS o WHERE o.reference = :reference", EmailFieldsMapReference.class);
        q.setParameter("reference", reference);
        return q;
    }

	public TypedQuery<EmailFieldsMapReference> findEmailFieldsMapReferencesByReferenceEquals(String reference, String sortFieldName, String sortOrder) {
        if (reference == null || reference.length() == 0) throw new IllegalArgumentException("The reference argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM EmailFieldsMapReference AS o WHERE o.reference = :reference");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<EmailFieldsMapReference> q = em.createQuery(queryBuilder.toString(), EmailFieldsMapReference.class);
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
