package org.esupportail.pay.services;

import org.esupportail.pay.domain.Label;
import org.esupportail.pay.domain.LdapResult;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvtService {


    @Resource
    UrlIdService urlIdService;

    @Resource
    LdapService ldapService;

    @Value("${ldap.displayName}")
    private String loginDisplayName;

    public void updateEvt(PayEvt payEvt) {
        // Hack : don't override logoFile !!
        PayEvt payEvtCurrent = PayEvt.findPayEvt(payEvt.getId());
        payEvt.setLogoFile(payEvtCurrent.getLogoFile());
        // Hack end

        computeLogins(payEvt, payEvt.getLogins(), payEvt.getViewerLogins2Add());
        computeRespLogin(payEvt);
        
        payEvt.merge();
    }

    public void createEvt(PayEvt payEvt, List<String> respLoginIds, List<String> viewerLoginIds) {
    	
        computeLogins(payEvt, respLoginIds, viewerLoginIds);

        if(payEvt.getUrlId() == null || payEvt.getUrlId().isEmpty()) {
            String urlId = urlIdService.generateUrlId4PayEvt(payEvt.getTitle().getTranslation(Label.LOCALE_IDS.en));
            payEvt.setUrlId(urlId);
        }

        payEvt.persist();
    }

	public void computeLogins(PayEvt payEvt, List<String> respLoginIds, List<String> viewerLoginIds) {
		List<RespLogin> respLogins = new ArrayList<RespLogin>();
        if(!respLoginIds.isEmpty()) {
            for(String login: respLoginIds) {
            	if(login != null && !login.isEmpty()) {
	                RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
	                respLogins.add(respLogin);
            	}
            }
        }
        payEvt.setRespLogins(respLogins);

        List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
        if(viewerLoginIds!=null && !viewerLoginIds.isEmpty()) {
            for(String login: viewerLoginIds) {
            	if(login != null && !login.isEmpty()) {
            		RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
            		viewerLogins.add(respLogin);
            	}
            }
        }
        payEvt.setViewerLogins(viewerLogins);
	}

    public List<RespLogin> listEvt(String currentUser) {
        RespLogin respLogin = RespLogin.findOrCreateRespLogin(currentUser);
        return Arrays.asList(new RespLogin[] {respLogin});
    }

    public void computeRespLogin(List<PayEvt> payEvts) {
    	
    	List<RespLogin> respLogins = new ArrayList<RespLogin>();
    	for(PayEvt payEvt: payEvts) {
    		if(payEvt.getRespLogins() != null) {
    			respLogins.addAll(payEvt.getRespLogins());
    		}
    	}
        ldapService.computeRespLogin(respLogins, loginDisplayName);
 
    	List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
    	for(PayEvt payEvt: payEvts) {
    		if(payEvt.getViewerLogins() != null) {
    			viewerLogins.addAll(payEvt.getViewerLogins());
    		}
    	}
        ldapService.computeRespLogin(viewerLogins, loginDisplayName);
        
    }
    
    public void computeRespLogin(PayEvt payEvt) {
        if (payEvt.getRespLogins() != null) {
            ldapService.computeRespLogin(payEvt.getRespLogins(), loginDisplayName);
        }
        if (payEvt.getViewerLogins() != null) {
            ldapService.computeRespLogin(payEvt.getViewerLogins(), loginDisplayName);
        }
    }
 }










