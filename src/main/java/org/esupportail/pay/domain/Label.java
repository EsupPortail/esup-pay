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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class Label {
    
	public static enum LOCALE_IDS {fr, en};
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@MapKey(name = "idLocale")
	Map<String, LabelLocale> labelLocales;
	
	
	
	public Label() {
		super();
		labelLocales = new HashMap<String, LabelLocale>();
		labelLocales.put(LOCALE_IDS.en.toString(), new LabelLocale(LOCALE_IDS.en.toString(), ""));
		labelLocales.put(LOCALE_IDS.fr.toString(), new LabelLocale(LOCALE_IDS.fr.toString(), ""));
	}



	public String getTranslation(LOCALE_IDS localeId) {
		return this.getLabelLocales().get(localeId.toString()).getTranslation();
	}
    

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public Map<String, LabelLocale> getLabelLocales() {
        return this.labelLocales;
    }

	public void setLabelLocales(Map<String, LabelLocale> labelLocales) {
        this.labelLocales = labelLocales;
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

	@PersistenceContext
    transient EntityManager entityManager;

	public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("labelLocales");

	public static final EntityManager entityManager() {
        EntityManager em = new Label().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countLabels() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Label o", Long.class).getSingleResult();
    }

	public static List<Label> findAllLabels() {
        return entityManager().createQuery("SELECT o FROM Label o", Label.class).getResultList();
    }

	public static List<Label> findAllLabels(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Label o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Label.class).getResultList();
    }

	public static Label findLabel(Long id) {
        if (id == null) return null;
        return entityManager().find(Label.class, id);
    }

	public static List<Label> findLabelEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Label o", Label.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static List<Label> findLabelEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Label o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Label.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Label attached = Label.findLabel(this.id);
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
    public Label merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Label merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
