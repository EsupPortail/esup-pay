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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.stereotype.Service;

@Service
public class LdapService {

	@Resource
	LdapTemplate ldapTemplate;

	private String ldapAttr;

	public List<String> searchLogins(String login, String ldapAttr) {
		this.ldapAttr = ldapAttr;
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		filter.and(new LikeFilter(this.ldapAttr, login));
		return ldapTemplate.search("", filter.encode(), SearchControls.SUBTREE_SCOPE, new String [] {this.ldapAttr}, new SimpleLoginAttributMapper());
	}
	
	class SimpleLoginAttributMapper  implements AttributesMapper {

		public String mapFromAttributes(Attributes attrs)
				throws javax.naming.NamingException {
			return attrs.get(ldapAttr).get().toString();
		}
	}
}
