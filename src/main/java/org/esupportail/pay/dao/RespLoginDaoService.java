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

	public TypedQuery<RespLogin> findRespLoginsByLoginEquals(String login) {
        if (login == null || login.length() == 0) throw new IllegalArgumentException("The login argument is required");
        
        TypedQuery<RespLogin> q = em.createQuery("SELECT o FROM RespLogin AS o WHERE o.login = :login", RespLogin.class);
        q.setParameter("login", login);
        return q;
    }
	
	@Transactional
	public void persist(RespLogin respLogin) {
		em.persist(respLogin);
	}
}