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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

public class PayLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
	
	private final Logger log = Logger.getLogger(getClass());
	
	protected Map<String, String> mappingGroupesRoles;
	
	protected LdapUserSearch ldapUserGroupAdminFilterSearch;
	
	protected String groupAdminFilter;
	
	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		// for case insensitive ...
		this.mappingGroupesRoles = new HashMap<String, String>();
		for(String ldapGroup : mappingGroupesRoles.keySet()) {
			this.mappingGroupesRoles.put(ldapGroup.toUpperCase(), mappingGroupesRoles.get(ldapGroup));
		}
	}

	public void setLdapUserGroupAdminFilterSearch(LdapUserSearch ldapUserGroupAdminFilterSearch) {
		this.ldapUserGroupAdminFilterSearch = ldapUserGroupAdminFilterSearch;
	}

	public void setGroupAdminFilter(String groupAdminFilter) {
		this.groupAdminFilter = groupAdminFilter;
	}

	public PayLdapAuthoritiesPopulator(ContextSource contextSource,
			String groupSearchBase) {
		super(contextSource, groupSearchBase);
	}
	
	@Override
	public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String username) {
		if (getGroupSearchBase() == null || getGroupSearchBase().isEmpty() || getGroupSearchFilter() == null || getGroupSearchFilter().isEmpty() ) {
			log.info("Roles from ldap groups not well configured (groupSearchBase, groupSearchFilter) -> no search for this");
			return new HashSet<GrantedAuthority>();
		}
		return super.getGroupMembershipRoles(userDn, username);
	}
	
	@Override
	 protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {

		String userDn = user.getNameInNamespace();

		// search groups with groupFilter usually
		Set<GrantedAuthority> roles = getGroupMembershipRoles(userDn, username);
		log.info("Roles from ldap groups for " + username + " : " + roles);

		Set<GrantedAuthority> extraRoles = new HashSet<GrantedAuthority>();

		for(GrantedAuthority role: roles) {
			log.debug("Group from LDAP : " + role.getAuthority().replaceAll("ROLE_", ""));
			if(mappingGroupesRoles != null && mappingGroupesRoles.containsKey(role.getAuthority())) {
				extraRoles.add(new SimpleGrantedAuthority(mappingGroupesRoles.get(role.getAuthority())));
			}
		}
		
		// add groups found via groupAdminFilter
		if(groupAdminFilter != null && !groupAdminFilter.isEmpty()) {
			try {
				ldapUserGroupAdminFilterSearch.searchForUser(username);
				extraRoles.add(new SimpleGrantedAuthority("ROLE_SU"));
				log.info("Role Admin ok for " + username + " with the filter " + groupAdminFilter);
			} catch(UsernameNotFoundException e) {
				// User is not admin
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
