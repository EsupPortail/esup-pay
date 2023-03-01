package org.esupportail.pay.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShibAuthenticatedUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Map<String, String> userRoles;

    public void setUserRoles(Map<String, String> userRoles) {
        this.userRoles = userRoles;
    }

    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws AuthenticationException {
        Set<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>();

        for(String role : userRoles.keySet()) {
            if(Arrays.asList(userRoles.get(role).split(";")).contains(token.getName())) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }
        log.info("Shib & Ldap Credentials for " + token.getName() + " -> " + authorities);

        return createUserDetails(token, authorities);
    }

    protected UserDetails createUserDetails(Authentication token, Collection<? extends GrantedAuthority> authorities) {
        return new User(token.getName(), "N/A", true, true, true, true, authorities);
    }

}
