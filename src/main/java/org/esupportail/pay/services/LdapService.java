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

import java.util.List;

import javax.annotation.Resource;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import jdk.nashorn.internal.ir.CallNode;
import org.esupportail.pay.domain.LdapResult;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.stereotype.Service;

@Service
public class LdapService {

	@Resource
	LdapTemplate ldapTemplate;

	private String loginDisplayName;

	public List<LdapResult> search(String login, List<String> ldapSearchAttrs, String loginDisplayName) {
		this.loginDisplayName = loginDisplayName;
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		OrFilter orFilter = new OrFilter();
		for (String ldapSearchAttr : ldapSearchAttrs) {
			orFilter.or(new LikeFilter(ldapSearchAttr, login));
		}
		filter.and(orFilter);
		return ldapTemplate.search("", filter.encode(), SearchControls.SUBTREE_SCOPE, new String [] {loginDisplayName, "uid"}, new SimpleLoginAttributMapper());
	}

	public void computeRespLogin(List<RespLogin> respLogins, String loginDisplayName) {
		this.loginDisplayName = loginDisplayName;
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		OrFilter orFilter = new OrFilter();
		for (RespLogin respLogin : respLogins) {
			orFilter.or(new LikeFilter("uid", respLogin.getLogin()+'*'));
		}
		filter.and(orFilter);
		List<LdapResult> ldapResults = ldapTemplate.search("", filter.encode(), SearchControls.SUBTREE_SCOPE, new String [] {loginDisplayName, "uid"}, new SimpleLoginAttributMapper());
		for (RespLogin respLogin : respLogins) {
			for (LdapResult ldapResult : ldapResults) {
				if (respLogin.getLogin().equals(ldapResult.getUid())) {
					respLogin.setDisplayName(ldapResult.getDisplayName());
				}

			}
		}
	}
	
	class SimpleLoginAttributMapper  implements AttributesMapper {

		public LdapResult mapFromAttributes(Attributes attrs)
				throws javax.naming.NamingException {
			LdapResult ldapResult = new LdapResult();
			ldapResult.setDisplayName(attrs.get(loginDisplayName).get().toString());
			ldapResult.setUid(attrs.get("uid").get().toString());
			return ldapResult;
		}
	}
}
