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

import org.esupportail.pay.domain.Label;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LabelDaoService {
	
	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("labelLocales");

	@PersistenceContext
    transient EntityManager em;

	public long countLabels() {
        return em.createQuery("SELECT COUNT(o) FROM Label o", Long.class).getSingleResult();
    }

	public List<Label> findAllLabels() {
        return em.createQuery("SELECT o FROM Label o", Label.class).getResultList();
    }

	public List<Label> findAllLabels(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Label o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, Label.class).getResultList();
    }

	public Label findLabel(Long id) {
        if (id == null) return null;
        return em.find(Label.class, id);
    }

	public List<Label> findLabelEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM Label o", Label.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<Label> findLabelEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Label o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, Label.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
