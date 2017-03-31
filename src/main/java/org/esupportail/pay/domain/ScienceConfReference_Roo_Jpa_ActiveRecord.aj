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
// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.pay.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.esupportail.pay.domain.ScienceConfReference;
import org.springframework.transaction.annotation.Transactional;

privileged aspect ScienceConfReference_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager ScienceConfReference.entityManager;
    
    public static final List<String> ScienceConfReference.fieldNames4OrderClauseFilter = java.util.Arrays.asList("emailFieldsMapReference", "uid", "confid", "returnurl");
    
    public static final EntityManager ScienceConfReference.entityManager() {
        EntityManager em = new ScienceConfReference().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long ScienceConfReference.countScienceConfReferences() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ScienceConfReference o", Long.class).getSingleResult();
    }
    
    public static List<ScienceConfReference> ScienceConfReference.findAllScienceConfReferences() {
        return entityManager().createQuery("SELECT o FROM ScienceConfReference o", ScienceConfReference.class).getResultList();
    }
    
    public static List<ScienceConfReference> ScienceConfReference.findAllScienceConfReferences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ScienceConfReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ScienceConfReference.class).getResultList();
    }
    
    public static ScienceConfReference ScienceConfReference.findScienceConfReference(Long id) {
        if (id == null) return null;
        return entityManager().find(ScienceConfReference.class, id);
    }
    
    public static List<ScienceConfReference> ScienceConfReference.findScienceConfReferenceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ScienceConfReference o", ScienceConfReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<ScienceConfReference> ScienceConfReference.findScienceConfReferenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ScienceConfReference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ScienceConfReference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void ScienceConfReference.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void ScienceConfReference.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            ScienceConfReference attached = ScienceConfReference.findScienceConfReference(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void ScienceConfReference.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void ScienceConfReference.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public ScienceConfReference ScienceConfReference.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ScienceConfReference merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
