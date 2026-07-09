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

import org.slf4j.Logger;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.*;

class LdapUserContextMapper extends LdapUserDetailsMapper {

    Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    @Override
    public LdapUserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities){
        try {
            return new LdapUserDetails(username, authorities, toMap(ctx));
        } catch (NamingException e) {
            throw new RuntimeException("Unable to map LDAP user for " + username, e);
        }
    }

    Map<String, String> toMap(DirContextOperations ctx) throws NamingException {
        var map = new TreeMap<String, String>();
        if (ctx == null || ctx.getAttributes() == null) {
            return map;
        }
        var attrs = ctx.getAttributes().getAll();
        while (attrs.hasMore()) {
            var attr = attrs.next();
            var value = toStringValue(attr);
            if (value != null) {
                map.put(attr.getID(), value);
            }
        }
        return map;
    }

    String toStringValue(Attribute attr) {
        if (attr == null || attr.size() == 0) {
            return null;
        }
        try {
            if (attr.size() == 1) {
                return toStringValue(attr.get());
            }
            var values = new ArrayList<String>(attr.size());
            var allValues = attr.getAll();
            while (allValues.hasMore()) {
                var value = toStringValue(allValues.next());
                if (value != null) {
                    values.add(value);
                }
            }
            return values.isEmpty() ? null : String.join(", ", values);
        } catch (NamingException e) {
            log.error("Unable to read LDAP attribute {}", attr.getID(), e);
            return null;
        }
    }

    String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) value);
        }
        return Objects.toString(value);
    }
}    
