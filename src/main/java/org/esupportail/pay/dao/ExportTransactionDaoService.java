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

import org.esupportail.pay.domain.ExportTransaction;
import org.springframework.stereotype.Service;

@Service
public class ExportTransactionDaoService {

	@PersistenceContext
    EntityManager em;

	public Long countFindExportTransactionsByNumTransactionEqualsAndNumContratEquals(String numTransaction, String numContrat) {
        if (numTransaction == null || numTransaction.length() == 0) throw new IllegalArgumentException("The numTransaction argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM ExportTransaction AS o WHERE o.numTransaction = :numTransaction  AND o.numContrat = :numContrat", Long.class);
        q.setParameter("numTransaction", numTransaction);
        q.setParameter("numContrat", numContrat);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<ExportTransaction> findExportTransactionsByNumRemiseAndStatutEqualsAndNumContratEquals(String numRemise, String statut, String numContrat) {
        if (numRemise == null || numRemise.length() == 0) throw new IllegalArgumentException("The numRemise argument is required");
        if (statut == null || statut.length() == 0) throw new IllegalArgumentException("The statut argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        TypedQuery<ExportTransaction> q = em.createQuery("SELECT o FROM ExportTransaction AS o WHERE o.numRemise = :numRemise AND o.statut = :statut  AND o.numContrat = :numContrat", ExportTransaction.class);
        q.setParameter("numRemise", numRemise);
        q.setParameter("statut", statut);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public TypedQuery<ExportTransaction> findExportTransactionsByNumTransactionEqualsAndNumContratEquals(String numTransaction, String numContrat) {
        if (numTransaction == null || numTransaction.length() == 0) throw new IllegalArgumentException("The numTransaction argument is required");
        if (numContrat == null || numContrat.length() == 0) throw new IllegalArgumentException("The numContrat argument is required");
        TypedQuery<ExportTransaction> q = em.createQuery("SELECT o FROM ExportTransaction AS o WHERE o.numTransaction = :numTransaction  AND o.numContrat = :numContrat", ExportTransaction.class);
        q.setParameter("numTransaction", numTransaction);
        q.setParameter("numContrat", numContrat);
        return q;
    }

	public void persist(ExportTransaction exportTransaction) {
		em.persist(exportTransaction);
	}

}