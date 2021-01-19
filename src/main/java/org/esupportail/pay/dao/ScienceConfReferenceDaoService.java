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
import javax.persistence.TypedQuery;

import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.ScienceConfReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScienceConfReferenceDaoService {

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("emailFieldsMapReference", "uid", "confid", "returnurl", "dateCreated");

	@PersistenceContext
    EntityManager em;

	public long countScienceConfReferences() {
        return em.createQuery("SELECT COUNT(o) FROM ScienceConfReference o", Long.class).getSingleResult();
    }

	public List<ScienceConfReference> findAllScienceConfReferences() {
        return em.createQuery("SELECT o FROM ScienceConfReference o", ScienceConfReference.class).getResultList();
    }

	public List<ScienceConfReference> findAllScienceConfReferences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ScienceConfReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, ScienceConfReference.class).getResultList();
    }

	public ScienceConfReference findScienceConfReference(Long id) {
        if (id == null) return null;
        return em.find(ScienceConfReference.class, id);
    }

	public List<ScienceConfReference> findScienceConfReferenceEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM ScienceConfReference o", ScienceConfReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<ScienceConfReference> findScienceConfReferenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ScienceConfReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, ScienceConfReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public Long countFindScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference", Long.class);
        q.setParameter("emailFieldsMapReference", emailFieldsMapReference);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<ScienceConfReference> findScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        
        TypedQuery<ScienceConfReference> q = em.createQuery("SELECT o FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference", ScienceConfReference.class);
        q.setParameter("emailFieldsMapReference", emailFieldsMapReference);
        return q;
    }

	public TypedQuery<ScienceConfReference> findScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference, String sortFieldName, String sortOrder) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ScienceConfReference> q = em.createQuery(queryBuilder.toString(), ScienceConfReference.class);
        q.setParameter("emailFieldsMapReference", emailFieldsMapReference);
        return q;
    }

	public void remove(ScienceConfReference scienceConfReference) {
		em.remove(scienceConfReference);
	}

	public void persist(ScienceConfReference scienceConfReference) {
		em.persist(scienceConfReference);
	}
}
