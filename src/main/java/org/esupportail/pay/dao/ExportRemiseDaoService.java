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

import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.esupportail.pay.domain.ExportRemise;
import org.springframework.stereotype.Service;

@Service
public class ExportRemiseDaoService {
	
	public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("numRemise", "dateRemise", "nbTransactions", "montant", "numContrat");

	@PersistenceContext
    transient EntityManager em;


	public Long countFindExportRemisesByDateRemiseBetween(Date minDateRemise, Date maxDateRemise) {
        if (minDateRemise == null) throw new IllegalArgumentException("The minDateRemise argument is required");
        if (maxDateRemise == null) throw new IllegalArgumentException("The maxDateRemise argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportRemise AS o WHERE o.dateRemise BETWEEN :minDateRemise AND :maxDateRemise", Long.class);
        q.setParameter("minDateRemise", minDateRemise);
        q.setParameter("maxDateRemise", maxDateRemise);
        return ((Long) q.getSingleResult());
    }

	public Long countFindExportRemisesByNumRemiseEqualsAndNumContratEquals(String numRemise, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportRemise AS o WHERE o.numRemise = :numRemise  AND o.numContrat = :numContrat", Long.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("numContrat", numContrat);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<ExportRemise> findExportRemisesByDateRemiseBetween(Date minDateRemise, Date maxDateRemise) {
        if (minDateRemise == null) throw new IllegalArgumentException("The minDateRemise argument is required");
        if (maxDateRemise == null) throw new IllegalArgumentException("The maxDateRemise argument is required");
        TypedQuery<ExportRemise> q = em.createQuery("SELECT o FROM ExportRemise AS o WHERE o.dateRemise BETWEEN :minDateRemise AND :maxDateRemise", ExportRemise.class);
        q.setParameter("minDateRemise", minDateRemise);
        q.setParameter("maxDateRemise", maxDateRemise);
        return q;
    }

	public TypedQuery<ExportRemise> findExportRemisesByDateRemiseBetween(Date minDateRemise, Date maxDateRemise, String sortFieldName, String sortOrder) {
        if (minDateRemise == null) throw new IllegalArgumentException("The minDateRemise argument is required");
        if (maxDateRemise == null) throw new IllegalArgumentException("The maxDateRemise argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ExportRemise AS o WHERE o.dateRemise BETWEEN :minDateRemise AND :maxDateRemise");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ExportRemise> q = em.createQuery(queryBuilder.toString(), ExportRemise.class);
        q.setParameter("minDateRemise", minDateRemise);
        q.setParameter("maxDateRemise", maxDateRemise);
        return q;
    }

	public TypedQuery<ExportRemise> findExportRemisesByNumRemiseEqualsAndNumContratEquals(String numRemise, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        TypedQuery<ExportRemise> q = em.createQuery("SELECT o FROM ExportRemise AS o WHERE o.numRemise = :numRemise  AND o.numContrat = :numContrat", ExportRemise.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public TypedQuery<ExportRemise> findExportRemisesByNumRemiseEqualsAndNumContratEquals(String numRemise, String numContrat, String sortFieldName, String sortOrder) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ExportRemise AS o WHERE o.numRemise = :numRemise  AND o.numContrat = :numContrat");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<ExportRemise> q = em.createQuery(queryBuilder.toString(), ExportRemise.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public long countExportRemises() {
        return em.createQuery("SELECT COUNT(o) FROM ExportRemise o", Long.class).getSingleResult();
    }

	public List<ExportRemise> findAllExportRemises() {
        return em.createQuery("SELECT o FROM ExportRemise o", ExportRemise.class).getResultList();
    }

	public List<ExportRemise> findAllExportRemises(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ExportRemise o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, ExportRemise.class).getResultList();
    }

	public ExportRemise findExportRemise(Long id) {
        if (id == null) return null;
        return em.find(ExportRemise.class, id);
    }

	public List<ExportRemise> findExportRemiseEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM ExportRemise o", ExportRemise.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<ExportRemise> findExportRemiseEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ExportRemise o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, ExportRemise.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public void persist(ExportRemise exportRemise) {
		em.persist(exportRemise);
	}
}