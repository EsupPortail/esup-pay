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
import javax.persistence.Transient;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findRespLoginsByLoginEquals"})
public class RespLogin {

    @Column(unique = true)
    String login;
    
    @Transient
	String displayName;

	public static RespLogin findOrCreateRespLogin(String login2find) {
		RespLogin respLogin = null;
		List<RespLogin> respLogins = findRespLoginsByLoginEquals(login2find).getResultList();
		if(!respLogins.isEmpty()) {
			respLogin = respLogins.get(0);
		} else {
			respLogin = new RespLogin();
			respLogin.setLogin(login2find);
			respLogin.persist();
		}
		return respLogin;
	}

	@Override
	public String toString() {
		return login;
	}
}
