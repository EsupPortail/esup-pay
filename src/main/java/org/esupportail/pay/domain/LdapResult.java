package org.esupportail.pay.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LdapResult {

    private String uid;
    
    private String displayName;
    
    private String email;

    public LdapResult() {
    }
    
}
