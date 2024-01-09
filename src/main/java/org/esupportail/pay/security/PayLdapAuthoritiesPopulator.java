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

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.RespLoginDaoService;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

public class PayLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
	
	private final Logger log = Logger.getLogger(getClass());
	
	protected Map<String, String> mappingGroupesRoles;
	
	protected Map<String, LdapUserSearch> ldapUserGroupFilterSearch = new HashMap<>();
	
	protected Map<String, String> groupFilters;
	
	@Resource
	PayEvtDaoService payEvtDaoService;
	
	@Resource
	RespLoginDaoService respLoginDaoService;
	
	public void setMappingGroupesRoles(Map<String, String> mappingGroupesRoles) {
		// for case insensitive ...
		this.mappingGroupesRoles = new HashMap<String, String>();
		for(String ldapGroup : mappingGroupesRoles.keySet()) {
			this.mappingGroupesRoles.put(ldapGroup.toUpperCase(), mappingGroupesRoles.get(ldapGroup));
		}
	}

	public PayLdapAuthoritiesPopulator(BaseLdapPathContextSource contextSource,
									   String peopleSearchBase, String peopleSearchFilter,
									   String groupSearchBase, String groupSearchFilter, Map<String, String> mappingGroupesRoles, Map<String, String> groupFilters) {
		super(contextSource, groupSearchBase);
		this.setGroupSearchFilter(groupSearchFilter);
		this.mappingGroupesRoles = mappingGroupesRoles;
		for(String role : groupFilters.keySet()) {
			String groupFilter = groupFilters.get(role);
			if(!groupFilter.isBlank()) {
				String searchFilter = String.format("(&(%s)(%s))", peopleSearchFilter, groupFilter);
				LdapUserSearch ldapUserSearch = new FilterBasedLdapUserSearch(peopleSearchBase, searchFilter, contextSource);
				ldapUserGroupFilterSearch.put(role, ldapUserSearch);
				log.debug(String.format("%s -> %s", searchFilter, role));
			}
		}
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
		Set<GrantedAuthority> groups = getGroupMembershipRoles(userDn, username);
		log.info("Groups from ldap (with ROLE_ prefix) for " + username + " : " + groups);

		Set<GrantedAuthority> extraRoles = new HashSet<GrantedAuthority>();

		for(GrantedAuthority group: groups) {
			String groupname =  group.getAuthority().replaceAll("ROLE_", "");
			if(!groupname.isBlank()) {
				log.debug("Group from LDAP : " + groupname);
				for (String role : mappingGroupesRoles.keySet()) {
					if (groupname.equalsIgnoreCase(mappingGroupesRoles.get(role))) {
						extraRoles.add(new SimpleGrantedAuthority(role));
						log.debug(String.format("Role %s ok for %s", role, username));
					}
				}
			}
		}
		
		// add groups found via group filters
		for(String role : ldapUserGroupFilterSearch.keySet()) {
			LdapUserSearch ldapUserSearch = ldapUserGroupFilterSearch.get(role);
			try {
				ldapUserSearch.searchForUser(username);
				extraRoles.add(new SimpleGrantedAuthority(role));
				log.debug(String.format("Role %s ok for %s", role, username));
			} catch(UsernameNotFoundException e) {
				// User is not admin
			}
		}
		
		// role SU -> role ADMIN
		if(extraRoles.contains(new SimpleGrantedAuthority("ROLE_SU"))) {
			extraRoles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			extraRoles.add(new SimpleGrantedAuthority("ROLE_STAT"));
			extraRoles.add(new SimpleGrantedAuthority("ROLE_VENTILATION"));
		}
		
		RespLogin respLogin = respLoginDaoService.findOrCreateRespLogin(username);
		List<RespLogin> respLoginList = Arrays.asList(new RespLogin[] {respLogin});
		if(!payEvtDaoService.findPayEvtsByRespLogins(respLoginList).getResultList().isEmpty()) {
			extraRoles.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
		}
		
		RespLogin viewerLogin = respLoginDaoService.findOrCreateRespLogin(username);
		List<RespLogin> viewerLoginList = Arrays.asList(new RespLogin[] {viewerLogin});
		if(!payEvtDaoService.findPayEvtsByViewerLogins(viewerLoginList).getResultList().isEmpty()) {
			extraRoles.add(new SimpleGrantedAuthority("ROLE_VIEWER"));
		}
		
		if(log.isInfoEnabled()) {
			for(GrantedAuthority role: extraRoles) {
				log.info(String.format("%s -> Role : %s ", username, role.getAuthority().replaceAll("ROLE_", "")));
			}
		}

		return extraRoles;
	}

}
