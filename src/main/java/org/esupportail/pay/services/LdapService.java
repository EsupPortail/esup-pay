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
package org.esupportail.pay.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.esupportail.pay.domain.LdapResult;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.stereotype.Service;

@Service
public class LdapService {

	@Autowired(required = false)
	LdapTemplate ldapTemplate;
	
	@Value("${ldap.uid.attribute:uid}")
	String ldapUidAttr;

	@Value("${ldap.displayName:displayName}")
	private String loginDisplayName;

	@Value("${ldap.mail:mail}")
	private String loginMail;

	private List<String> ldapSearchAttrs;
	private List<String> ldapSearchEqAttrs;

	@Value("${ldap.searchLikeAttrs}")
	public void setLdapSearchAttr(String ldapSearchAttr) {
		this.ldapSearchAttrs = Arrays.asList(ldapSearchAttr.split(","));
	}

	@Value("${ldap.searchEqAttrs}")
	public void setLdapSearchEqAttr(String ldapSearchEqAttr) {
		this.ldapSearchEqAttrs = Arrays.asList(ldapSearchEqAttr.split(","));
	}

	public List<LdapResult> search(String login) {
		if(ldapTemplate == null) {
			return null;
		}
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		OrFilter orFilter = new OrFilter();
		if (ldapSearchAttrs != null) {
			for (String ldapSearchAttr : ldapSearchAttrs) {
			    orFilter.or(new LikeFilter(ldapSearchAttr, login));
			}
		}
		for (String ldapSearchAttr : ldapSearchEqAttrs) {
			orFilter.or(new EqualsFilter(ldapSearchAttr, login));
		}
		filter.and(orFilter);
		return ldapTemplate.search("", filter.encode(), SearchControls.SUBTREE_SCOPE, new String [] {loginDisplayName, ldapUidAttr, loginMail}, new SimpleLoginAttributMapper(loginDisplayName, loginMail));
	}

	public void computeRespLogin(List<RespLogin> respLogins) {
		if(respLogins==null || respLogins.isEmpty()) {
			return;
		}
		if( ldapTemplate == null) {
			for (RespLogin respLogin : respLogins) {
				respLogin.setDisplayName(respLogin.getLogin());
			}
			return;
		}

		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		OrFilter orFilter = new OrFilter();
		for (RespLogin respLogin : respLogins) {
			orFilter.or(new EqualsFilter(ldapUidAttr, respLogin.getLogin()));
		}
		filter.and(orFilter);
		List<LdapResult> ldapResults = ldapTemplate.search("", filter.encode(), SearchControls.SUBTREE_SCOPE, new String [] {loginDisplayName, ldapUidAttr, loginMail}, new SimpleLoginAttributMapper(loginDisplayName, loginMail));
		for (RespLogin respLogin : respLogins) {
			for (LdapResult ldapResult : ldapResults) {
				if (respLogin.getLogin().equals(ldapResult.getUid())) {
					respLogin.setDisplayName(ldapResult.getDisplayName());
				}

			}
		}
	}
	
	class SimpleLoginAttributMapper  implements AttributesMapper {
		
		String loginDisplayName;
		String loginMail;

		public SimpleLoginAttributMapper(String loginDisplayName, String loginMail) {
			this.loginDisplayName = loginDisplayName;
			this.loginMail = loginMail;
		}

		public LdapResult mapFromAttributes(Attributes attrs)
				throws javax.naming.NamingException {
			LdapResult ldapResult = new LdapResult();
			if(attrs.get(loginDisplayName) != null) {
				ldapResult.setDisplayName(attrs.get(loginDisplayName).get().toString());
			}
			if(attrs.get(ldapUidAttr) != null) {
				ldapResult.setUid(attrs.get(ldapUidAttr).get().toString());
			}
			if(attrs.get(loginMail) != null) {
				ldapResult.setEmail(attrs.get(loginMail).get().toString());
			}
			return ldapResult;
		}
	}
}
