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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.ScienceConfReference;
import org.springframework.stereotype.Service;

@Service
public class ScienceConfReferenceDaoService {

	@PersistenceContext
    EntityManager em;

	public TypedQuery<ScienceConfReference> findScienceConfReferencesByEmailFieldsMapReference(EmailFieldsMapReference emailFieldsMapReference) {
        if (emailFieldsMapReference == null) throw new IllegalArgumentException("The emailFieldsMapReference argument is required");
        
        TypedQuery<ScienceConfReference> q = em.createQuery("SELECT o FROM ScienceConfReference AS o WHERE o.emailFieldsMapReference = :emailFieldsMapReference", ScienceConfReference.class);
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