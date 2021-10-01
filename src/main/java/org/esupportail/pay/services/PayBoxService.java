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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.dao.EmailFieldsMapReferenceDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.esupportail.pay.domain.PayBoxForm;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSender;

public class PayBoxService {

    private final Logger log = Logger.getLogger(getClass());

    private static final String RETOUR_VARIABLES = "montant:M;reference:R;auto:A;erreur:E;idtrans:S;securevers:v;softdecline:e;secureauth:F;securegarantie:G;signature:K";
    
    private static final String DELIMITER_REF = "@@";
    
    @Resource
    EmailFieldsMapReferenceDaoService emailFieldsMapReferenceDaoService;
    
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;
	
    private HashService hashService;

    private String site;

    private String rang;

    private String identifiant;

    private String devise;

    private List<String> payboxActionUrls;

    private PublicKey payboxPublicKey;

    private String reponseServerUrl;
    
    private String mailFrom;

    private static final String NUM_COMMANDE_CHARS_NOT_AUTHORIZED_REGEX = "[^a-zA-Z0-9:@,.:_\\-]";

    @Autowired
    private transient MailSender mailTemplate;

    public void setHashService(HashService hashService) {
        this.hashService = hashService;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public void setPayboxActionUrls(List<java.lang.String> payboxActionUrls) {
        this.payboxActionUrls = payboxActionUrls;
    }

    public void setDerPayboxPublicKeyFile(String derPayboxPublicKeyFile) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        org.springframework.core.io.Resource derPayboxPublicKeyRessource = new ClassPathResource(derPayboxPublicKeyFile);
        InputStream fis = derPayboxPublicKeyRessource.getInputStream();
        DataInputStream dis = new DataInputStream(fis);
        byte[] pubKeyBytes = new byte[fis.available()];
        dis.readFully(pubKeyBytes);
        fis.close();
        dis.close();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.payboxPublicKey = kf.generatePublic(x509EncodedKeySpec);
    }

    public void setReponseServerUrl(String reponseServerUrl) {
        this.reponseServerUrl = reponseServerUrl;
    }

    public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public PayBoxForm getPayBoxForm(String mail, String field1, String field2, double montant, PayEvtMontant payEvtMontant, 
			String billingFirstname, String billingLastname, String billingAddress1, String billingZipCode, String billingCity, String billingCountryCode) {
        String montantAsCents = Long.toString(Math.round(new Double(montant * 100)));
        PayBoxForm payBoxForm = new PayBoxForm();
        payBoxForm.setActionUrl(getPayBoxActionUrl());
        payBoxForm.setClientEmail(mail);
        if(payEvtMontant.getAddPrefix() == null || payEvtMontant.getAddPrefix().isEmpty()) {
                payBoxForm.setCommande(getNumCommande(payEvtMontant.getEvt().getPayboxCommandPrefix(), mail, montantAsCents));
        } else {
        	String addPrefix = "";
        	if("field1".equals(payEvtMontant.getAddPrefix())) {
        		addPrefix = field1;
        	} else if("field2".equals(payEvtMontant.getAddPrefix())) {
        		addPrefix = field2;
        	}
                payBoxForm.setCommande(getNumCommande(payEvtMontant.getEvt().getPayboxCommandPrefix(), addPrefix, mail, montantAsCents));
        }
        payBoxForm.setDevise(devise);
        payBoxForm.setHash(hashService.getHash());
        payBoxForm.setIdentifiant(identifiant);
        payBoxForm.setRang(rang);
        payBoxForm.setRetourVariables(RETOUR_VARIABLES);
        payBoxForm.setSite(site);
        payBoxForm.setTime(getCurrentTime());
        payBoxForm.setTotal(montantAsCents);
        String callbackUrl = reponseServerUrl + "/payboxcallback";
        payBoxForm.setCallbackUrl(callbackUrl);
        String forwardUrl = reponseServerUrl;
        payBoxForm.setForwardAnnuleUrl(forwardUrl);
        payBoxForm.setForwardEffectueUrl(forwardUrl);
        payBoxForm.setForwardRefuseUrl(forwardUrl);
        
        payBoxForm.setShoppingcartTotalQuantity(payEvtMontant.getShoppingcartTotalQuantity());
        
        payBoxForm.setBillingFirstname(billingFirstname);
        payBoxForm.setBillingLastname(billingLastname);
        payBoxForm.setBillingAddress1(billingAddress1);
        payBoxForm.setBillingZipCode(billingZipCode);
        payBoxForm.setBillingCity(billingCity);
        payBoxForm.setBillingCountryCode(billingCountryCode);
        
        payBoxForm.setOptionalAddedParams(payEvtMontant.getOptionalAddedParams());

        String hMac = hashService.getHMac(payBoxForm.getParamsAsString());
        payBoxForm.setHmac(hMac);
        
        EmailFieldsMapReference emailMapFirstLastName = new EmailFieldsMapReference();
        emailMapFirstLastName.setReference(payBoxForm.getCommande());
        emailMapFirstLastName.setField1(field1);
        emailMapFirstLastName.setField2(field2);
        emailMapFirstLastName.setMail(mail);
        emailMapFirstLastName.setPayEvtMontant(payEvtMontant);
        emailMapFirstLastName.setDateCreated(new Date());
        emailFieldsMapReferenceDaoService.persist(emailMapFirstLastName);
        
        return payBoxForm;
    }

	private String getNumCommande(String numCommandePrefix, String mail, String montantAsCents) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S");
        String numCommande = numCommandePrefix + mail + DELIMITER_REF + montantAsCents + "-" + df.format(new Date());
    	// on supprime les #, &, ? & co  :  caractères spéciaux dans une url (la reference == numCommande étant reprises dans l'url callback paybox)
        numCommande = StringUtils.stripAccents(numCommande).replaceAll(NUM_COMMANDE_CHARS_NOT_AUTHORIZED_REGEX, "");
        return numCommande;
    }

    private String getNumCommande(String numCommandePrefix, String addPrefix, String mail, String montantAsCents) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S");
        String numCommande = numCommandePrefix + addPrefix + DELIMITER_REF + mail + DELIMITER_REF + montantAsCents + "-" + df.format(new Date());
    	// on supprime les #, &, ? & co  :  caractères spéciaux dans une url (la reference == numCommande étant reprises dans l'url callback paybox)
        numCommande = StringUtils.stripAccents(numCommande).replaceAll(NUM_COMMANDE_CHARS_NOT_AUTHORIZED_REGEX, "");
        return numCommande;
	}

    protected String getPayBoxActionUrl() {
        for (String payboxActionUrl : payboxActionUrls) {
            try {
                URL url = new URL(payboxActionUrl);
                URL url2test = new URL(String.format("%s://%s", url.getProtocol(), url.getHost()));
                URLConnection connection = url2test.openConnection();
                connection.connect();
                connection.getInputStream().read();
                return payboxActionUrl;
            } catch (Exception e) {
                log.warn("Pb with " + payboxActionUrl, e);
            }
        }
        throw new RuntimeException("No paybox action url is available at the moment !");
    }

    protected String getCurrentTime() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        return nowAsISO;
    }

    public boolean checkPayboxSignature(String queryString, String signature) {
        String sData = queryString.substring(0, queryString.lastIndexOf("&"));
        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            byte[] sigBytes = Base64.decodeBase64(signature.getBytes());
            sig.initVerify(payboxPublicKey);
            sig.update(sData.getBytes());
            boolean signatureOk = sig.verify(sigBytes);
            if (!signatureOk) {
                log.error("Erreur lors de la vérification de la signature, les données ne correspondent pas.");
                log.error(sData);
                log.error(signature);
            }
            return signatureOk;
        } catch (Exception e) {
            log.warn("Pb when checking SSL signature of Paybox", e);
            return false;
        }
    }

    public boolean payboxCallback(String montant, String reference, String auto, String erreur, String idtrans, String securevers, String softdecline, String secureauth, String securegarantie, String signature, String queryString) {
        synchronized (idtrans.intern()) {
	    	List<PayTransactionLog> txLogs = payTransactionLogDaoService.findPayTransactionLogsByIdtransEquals(idtrans).getResultList();
	        boolean newTxLog = txLogs.size() == 0;
	        PayTransactionLog txLog = txLogs.size() > 0 ? txLogs.get(0) : null;
	        if (txLog == null) {
	            txLog = new PayTransactionLog();
	        } else {
	            if ("00000".equals(txLog.getErreur())) {
	                log.info("This transaction + " + idtrans + " is already OK");
	                return true;
	            }
	        }
	        txLog.setMontant(montant);
	        txLog.setReference(reference);
	        txLog.setAuto(auto);
	        txLog.setErreur(erreur);
	        txLog.setIdtrans(idtrans);
	        txLog.setSecurevers(securevers);
	        txLog.setSoftdecline(softdecline);
	        txLog.setSecureauth(secureauth);
	        txLog.setSecuregarantie(securegarantie);
	        txLog.setSignature(signature);
	        txLog.setTransactionDate(new Date());
	
	            List<EmailFieldsMapReference> emailMapFirstLastNames = emailFieldsMapReferenceDaoService.findEmailFieldsMapReferencesByReferenceEquals(reference).getResultList();
	            if (!emailMapFirstLastNames.isEmpty()) {
	                txLog.setField1(emailMapFirstLastNames.get(0).getField1());
	                txLog.setField2(emailMapFirstLastNames.get(0).getField2());
	                txLog.setMail(emailMapFirstLastNames.get(0).getMail());
	                PayEvtMontant evtMontant = emailMapFirstLastNames.get(0).getPayEvtMontant();
	                PayEvt evt = evtMontant.getEvt();
	                txLog.setPayEvtMontant(emailMapFirstLastNames.get(0).getPayEvtMontant());
	            if (this.checkPayboxSignature(queryString, signature)) {
	                if ("00000".equals(erreur)) {
	                        log.info("Transaction : " + reference + " pour un montant de " + montant + " OK !");
	                        
	                        String subject = evt.getMailSubject() + txLog.getMail() + " - "  + txLog.getMontantDevise() + " Euros.";
	                        String mailTo = evt.getManagersEmail();
	                        String message = "Email : " + txLog.getMail() + "\n";
	                        message += txLog.getPayEvtMontant().getField1Label().getTranslation(LOCALE_IDS.fr) + " : " + txLog.getField1() + "\n";
	                        message += txLog.getPayEvtMontant().getField2Label().getTranslation(LOCALE_IDS.fr) + " : " + txLog.getField2() + "\n";
	                        message += "Montant : " + txLog.getMontantDevise() + " Euros\n";
	                        message += "Evt : " + txLog.getPayEvtMontant().getEvt().getTitle().getLabelLocales().get(LOCALE_IDS.fr.toString()).getTranslation() + " \n";
	                        message += "Titre du Montant : " + txLog.getPayEvtMontant().getTitle().getLabelLocales().get(LOCALE_IDS.fr.toString()).getTranslation() + " \n";
	                        message += "Transaction Paybox : " + idtrans + "\n";
	                        message += "Reference : " + reference + "\n";
	                        
	                        try {
	                        	this.sendMessage(mailFrom, subject, mailTo, message);
	                        	txLog.setMailSent(true);
	                        } catch (Exception ex) {
		                        log.error("Exception during sending email to : " + mailTo , ex);
		                        txLog.setMailSent(false);
		                    }
	                        
	                        if (newTxLog) {
	                        	payTransactionLogDaoService.persist(txLog);
	                        } 
	                } else {
	                    log.info("'Erreur' " + erreur + "  (annulation) lors de la transaction paybox : " + reference + " pour un montant de " + montant);
	                }
	            } else {
	                log.error("signature checking of paybox failed, transaction " + txLog + " canceled.");
	            }
	            return true;
	        }    
	        return false;
        }
    }

    public void sendMessage(String mailFrom, String subject, String mailTo, String message) {
        org.springframework.mail.SimpleMailMessage mailMessage = new org.springframework.mail.SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setSubject(subject);
        mailMessage.setTo(mailTo);
        mailMessage.setText(message);
        mailTemplate.send(mailMessage);
    }
}
