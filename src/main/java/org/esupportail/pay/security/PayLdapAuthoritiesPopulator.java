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
package org.esupportail.pay.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

public class PayLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
	
	private final Logger log = Logger.getLogger(getClass());
	
	protected Map<String, String> mappingGroupesRoles;
	
	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		this.mappingGroupesRoles = mappingGroupesRoles;
	}
	
	public PayLdapAuthoritiesPopulator(ContextSource contextSource,
			String groupSearchBase) {
		super(contextSource, groupSearchBase);
	}

	@Override
	 protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {

		String userDn = user.getNameInNamespace();

		Set<GrantedAuthority> roles = getGroupMembershipRoles(userDn, username);

		Set<GrantedAuthority> extraRoles = new HashSet<GrantedAuthority>();

		for(GrantedAuthority role: roles) {
			log.debug("Group from LDAP : " + role.getAuthority().replaceAll("ROLE_", ""));
			if(mappingGroupesRoles != null && mappingGroupesRoles.containsKey(role.getAuthority())) {
				extraRoles.add(new SimpleGrantedAuthority(mappingGroupesRoles.get(role.getAuthority())));
			}
		}
		
		// role SU -> role ADMIN
		if(extraRoles.contains(new SimpleGrantedAuthority("ROLE_SU"))) {
			extraRoles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		
		RespLogin respLogin = RespLogin.findOrCreateRespLogin(username);
		List<RespLogin> respLoginList = Arrays.asList(new RespLogin[] {respLogin});
		if(!PayEvt.findPayEvtsByRespLogins(respLoginList).getResultList().isEmpty()) {
			extraRoles.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
		}
		
		RespLogin viewerLogin = RespLogin.findOrCreateRespLogin(username);
		List<RespLogin> viewerLoginList = Arrays.asList(new RespLogin[] {viewerLogin});
		if(!PayEvt.findPayEvtsByViewerLogins(viewerLoginList).getResultList().isEmpty()) {
			extraRoles.add(new SimpleGrantedAuthority("ROLE_VIEWER"));
		}
		
		if(log.isInfoEnabled()) {
			for(GrantedAuthority role: extraRoles) {
				log.info("-> Role : " + role.getAuthority().replaceAll("ROLE_", ""));
			}
		}

		return extraRoles;
	}

}
