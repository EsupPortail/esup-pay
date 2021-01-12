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
 package org.esupportail.pay.domain;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class LabelLocale {
    
    String idLocale;
    
    String translation;
    
	public LabelLocale() {
		super();
	}
	
	public LabelLocale(String idLocale, String translation) {
		super();
		this.idLocale = idLocale;
		this.translation = translation;
	}

	@Override
	public String toString() {
		return translation ;
	}


	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("idLocale", "translation");

	public static final EntityManager entityManager() {
        EntityManager em = new LabelLocale().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countLabelLocales() {
        return entityManager().createQuery("SELECT COUNT(o) FROM LabelLocale o", Long.class).getSingleResult();
    }

	public static List<LabelLocale> findAllLabelLocales() {
        return entityManager().createQuery("SELECT o FROM LabelLocale o", LabelLocale.class).getResultList();
    }

	public static List<LabelLocale> findAllLabelLocales(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM LabelLocale o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, LabelLocale.class).getResultList();
    }

	public static LabelLocale findLabelLocale(Long id) {
        if (id == null) return null;
        return entityManager().find(LabelLocale.class, id);
    }

	public static List<LabelLocale> findLabelLocaleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM LabelLocale o", LabelLocale.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<LabelLocale> findLabelLocaleEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM LabelLocale o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, LabelLocale.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            LabelLocale attached = LabelLocale.findLabelLocale(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public LabelLocale merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        LabelLocale merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String getIdLocale() {
        return this.idLocale;
    }

	public void setIdLocale(String idLocale) {
        this.idLocale = idLocale;
    }

	public String getTranslation() {
        return this.translation;
    }

	public void setTranslation(String translation) {
        this.translation = translation;
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }
}
