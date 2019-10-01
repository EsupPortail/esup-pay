package org.esupportail.pay.services;

import org.esupportail.pay.domain.Label;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EvtService {


    @Resource
    UrlIdService urlIdService;

    public void updateEvt(PayEvt payEvt) {
        // Hack : don't override logoFile !!
        PayEvt payEvtCurrent = PayEvt.findPayEvt(payEvt.getId());
        payEvt.setLogoFile(payEvtCurrent.getLogoFile());
        // Hack end

        List<RespLogin> respLogins = new ArrayList<RespLogin>();
        if(payEvt.getLogins() != null) {
            for(String login: payEvt.getLogins()) {
                RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
                respLogins.add(respLogin);
            }
            payEvt.setRespLogins(respLogins);
        }

        List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
        if(payEvt.getViewerLogins2Add() != null) {
            for(String login: payEvt.getViewerLogins2Add()) {
                RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
                viewerLogins.add(respLogin);
            }
        }
        payEvt.setViewerLogins(viewerLogins);

        payEvt.merge();
    }

    public void createEvt(PayEvt payEvt, List<String> respLoginIds, List<String> viewerLoginIds) {
        List<RespLogin> respLogins = new ArrayList<RespLogin>();
        if(!respLogins.isEmpty()) {
            for(String login: respLoginIds) {
                RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
                respLogins.add(respLogin);
            }
        }
        payEvt.setRespLogins(respLogins);

        List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
        if(!viewerLoginIds.isEmpty()) {
            for(String login: viewerLoginIds) {
                RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
                viewerLogins.add(respLogin);
            }
        }
        payEvt.setViewerLogins(viewerLogins);

        if(payEvt.getUrlId() == null || payEvt.getUrlId().isEmpty()) {
            String urlId = urlIdService.generateUrlId4PayEvt(payEvt.getTitle().getTranslation(Label.LOCALE_IDS.en));
            payEvt.setUrlId(urlId);
        }

        payEvt.persist();
    }

    public List<RespLogin> listEvt(String currentUser) {
        RespLogin respLogin = RespLogin.findOrCreateRespLogin(currentUser);
        return Arrays.asList(new RespLogin[] {respLogin});
    }

}
