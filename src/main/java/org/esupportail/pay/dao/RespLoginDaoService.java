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

import org.esupportail.pay.domain.RespLogin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RespLoginDaoService {
	
	public final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("login");
	
	@PersistenceContext
    EntityManager em;

	@Transactional
	public RespLogin findOrCreateRespLogin(String login2find) {
		RespLogin respLogin = null;
		List<RespLogin> respLogins = findRespLoginsByLoginEquals(login2find).getResultList();
		if(!respLogins.isEmpty()) {
			respLogin = respLogins.get(0);
		} else {
			respLogin = new RespLogin();
			respLogin.setLogin(login2find);
			this.persist(respLogin);
		}
		return respLogin;
	}

	public Long countFindRespLoginsByLoginEquals(String login) {
        if (login == null || login.length() == 0) throw new IllegalArgumentException("The login argument is required");
        
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM RespLogin AS o WHERE o.login = :login", Long.class);
        q.setParameter("login", login);
        return ((Long) q.getSingleResult());
    }

	public TypedQuery<RespLogin> findRespLoginsByLoginEquals(String login) {
        if (login == null || login.length() == 0) throw new IllegalArgumentException("The login argument is required");
        
        TypedQuery<RespLogin> q = em.createQuery("SELECT o FROM RespLogin AS o WHERE o.login = :login", RespLogin.class);
        q.setParameter("login", login);
        return q;
    }

	public TypedQuery<RespLogin> findRespLoginsByLoginEquals(String login, String sortFieldName, String sortOrder) {
        if (login == null || login.length() == 0) throw new IllegalArgumentException("The login argument is required");
        
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM RespLogin AS o WHERE o.login = :login");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<RespLogin> q = em.createQuery(queryBuilder.toString(), RespLogin.class);
        q.setParameter("login", login);
        return q;
    }

	public long countRespLogins() {
        return em.createQuery("SELECT COUNT(o) FROM RespLogin o", Long.class).getSingleResult();
    }

	public List<RespLogin> findAllRespLogins() {
        return em.createQuery("SELECT o FROM RespLogin o", RespLogin.class).getResultList();
    }

	public List<RespLogin> findAllRespLogins(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM RespLogin o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, RespLogin.class).getResultList();
    }

	public RespLogin findRespLogin(Long id) {
        if (id == null) return null;
        return em.find(RespLogin.class, id);
    }

	public List<RespLogin> findRespLoginEntries(int firstResult, int maxResults) {
        return em.createQuery("SELECT o FROM RespLogin o", RespLogin.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public List<RespLogin> findRespLoginEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM RespLogin o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return em.createQuery(jpaQuery, RespLogin.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
	
	@Transactional
	public void persist(RespLogin respLogin) {
		em.persist(respLogin);
	}
}