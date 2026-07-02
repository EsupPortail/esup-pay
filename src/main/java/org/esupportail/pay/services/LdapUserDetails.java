package org.esupportail.pay.services;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Map;

public class LdapUserDetails extends User {
    private final Map<String, String> attrs;

    public LdapUserDetails(
            String username,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, String> attrs) {

        super(username, "", authorities);
        this.attrs = attrs;
    }

    public String getAttr(String name) {
        return attrs.get(name);
    }

}