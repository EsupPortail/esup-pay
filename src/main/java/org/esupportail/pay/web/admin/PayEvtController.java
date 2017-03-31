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
package org.esupportail.pay.web.admin;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.domain.Label;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.RespLogin;
import org.esupportail.pay.domain.UploadFile;
import org.esupportail.pay.services.PayBoxServiceManager;
import org.esupportail.pay.services.UrlIdService;
import org.esupportail.pay.web.validators.PayEvtUpdateValidator;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.AbstractResource;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/admin/evts")
@Controller
@RooWebScaffold(path = "admin/evts", formBackingObject = PayEvt.class)
@Transactional
public class PayEvtController {

	private final Logger log = Logger.getLogger(getClass());

	@Autowired
	ServletContext servletContext;
		
    @Resource
    UrlIdService urlIdService;
    
    @Resource
    PayEvtUpdateValidator payEvtUpdateValidator;
    
    @Resource 
    PayBoxServiceManager payBoxServiceManager;
	
	@RequestMapping(value = "/{id}/addLogoFile", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'manage')")
	public String addLogoFile(@PathVariable("id") Long id, UploadFile uploadFile, BindingResult bindingResult, Model uiModel, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			log.warn(bindingResult.getAllErrors());
			return "redirect:/admin/evts/" + id.toString();
		}
		uiModel.asMap().clear();

		// get PosteCandidature from id                                                                                                                                                                                               
		PayEvt evt = PayEvt.findPayEvt(id);

		MultipartFile file = uploadFile.getLogoFile();

		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?                                                                                     
		if(file != null) {
			Long fileSize = file.getSize();
			//String contentType = file.getContentType();
			//String filename = file.getOriginalFilename();

			InputStream inputStream = file.getInputStream();
			//byte[] bytes = IOUtils.toByteArray(inputStream);                                                                                                                                            

			evt.getLogoFile().setBinaryFileStream(inputStream, fileSize);
			evt.getLogoFile().persist();
		}

		return "redirect:/admin/evts/" + id.toString();
	}

	@RequestMapping(value = "/{id}/logo")
	public void getLogoFile(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
		try {
			PayEvt evt = PayEvt.findPayEvt(id);               
			if(evt.getLogoFile().getBinaryFile() != null) {
				IOUtils.copy(evt.getLogoFile().getBinaryFile().getBinaryStream(), response.getOutputStream());
			} else {
				AbstractResource defaultLogo = new ServletContextResource(servletContext, "images/credit-card.png");
				IOUtils.copy(defaultLogo.getInputStream(), response.getOutputStream());
			}
		} catch(Exception ioe) {
			log.warn("Get Logo of PayEvt " + id + " failed.", ioe);
		}
	}
	
    
    void populateEditForm(Model uiModel, PayEvt payEvt) {
        uiModel.addAttribute("payEvt", payEvt);
        uiModel.addAttribute("payboxServiceKeys", payBoxServiceManager.getPayboxServiceKeys());
    }

    
    // Don't override logoFile !!
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    @PreAuthorize("hasPermission(#payEvt, 'manage')")
    public String update(@Valid PayEvt payEvt, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	payEvtUpdateValidator.validate(payEvt, bindingResult);
    	if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, payEvt);
            return "admin/evts/update";
        }
        uiModel.asMap().clear();
        
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
        return "redirect:/admin/evts/" + encodeUrlPathSegment(payEvt.getId().toString(), httpServletRequest);
    }
    
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'view')")
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	PayEvt evt = PayEvt.findPayEvt(id);
        uiModel.addAttribute("payEvt", evt);
        uiModel.addAttribute("itemId", id);
        
        if(evt!=null) {
        	List<PayEvtMontant> montants = PayEvtMontant.findPayEvtMontantsByEvt(evt).getResultList();
            uiModel.addAttribute("payevtmontants", montants);
        }
        
        return "admin/evts/show";
    }
    
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String create(@Valid PayEvt payEvt, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
        	log.info(bindingResult.getAllErrors());
            populateEditForm(uiModel, payEvt);
            return "admin/evts/create";
        }
        uiModel.asMap().clear();   
        
        List<RespLogin> respLogins = new ArrayList<RespLogin>();
        if(httpServletRequest.getParameterValues("logins") != null) {
	        List<String> logins = Arrays.asList(httpServletRequest.getParameterValues("logins"));
	        for(String login: logins) {
	        	RespLogin respLogin = RespLogin.findOrCreateRespLogin(login);
	        	respLogins.add(respLogin);
	        }
        }
        payEvt.setRespLogins(respLogins);
        
        List<RespLogin> viewerLogins = new ArrayList<RespLogin>();
        if(httpServletRequest.getParameterValues("viewerLogins2Add") != null) {
	        List<String> logins = Arrays.asList(httpServletRequest.getParameterValues("viewerLogins2Add"));
	        for(String login: logins) {
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
        return "redirect:/admin/evts/" + encodeUrlPathSegment(payEvt.getId().toString(), httpServletRequest);
    } 
    
    
    
    @RequestMapping(produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_VIEWER')")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
       
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isManagerOrViewer = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) ||
        		auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER"));
        
        if(sortFieldName == null) {
        	sortFieldName = "id";
        	sortOrder = "desc";
        }
    	
        if(isAdmin) {
	    	if (page != null || size != null) {
	            int sizeNo = size == null ? 10 : size.intValue();
	            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
	            uiModel.addAttribute("payevts", PayEvt.findPayEvtEntries(firstResult, sizeNo, sortFieldName, sortOrder));
	            float nrOfPages = (float) PayEvt.countPayEvts() / sizeNo;
	            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
	        } else {
	            uiModel.addAttribute("payevts", PayEvt.findAllPayEvts(sortFieldName, sortOrder));
	        }
        } else if(isManagerOrViewer) {
    		RespLogin respLogin = RespLogin.findOrCreateRespLogin(auth.getName());
    		List<RespLogin> loginList = Arrays.asList(new RespLogin[] {respLogin});
    		uiModel.addAttribute("payevts", PayEvt.findPayEvtsByRespLoginsOrByViewerLogins(loginList, sortFieldName, sortOrder).getResultList());
        }
        
        return "admin/evts/list";
    }
    
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new PayEvt());
        return "admin/evts/create";
    }
    
    @PreAuthorize("hasPermission(#id, 'manage')")
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, PayEvt.findPayEvt(id));
        return "admin/evts/update";
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        PayEvt payEvt = PayEvt.findPayEvt(id);
        payEvt.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/evts";
    }
    
    @PreAuthorize("hasPermission(#id, 'view')")
    @RequestMapping(value = "/{id}/fees", produces = "text/html")
    public String fees(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if(sortFieldName == null || sortFieldName.isEmpty()) {
    		sortFieldName = "transactionDate";
    		sortOrder = "desc";
    	}
    	PayEvt payEvt = PayEvt.findPayEvt(id);
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("paytransactionlogs", PayTransactionLog.findPayTransactionLogsByPayEvt(payEvt, sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) PayTransactionLog.countFindPayTransactionLogsByPayEvt(payEvt) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("paytransactionlogs", PayTransactionLog.findPayTransactionLogsByPayEvt(payEvt, sortFieldName, sortOrder).getResultList());
        }
        uiModel.addAttribute("payTransactionLog_transactiondate_date_format", DateTimeFormat.patternForStyle("MM", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("evtTitle", payEvt.getTitle());
        return "admin/fees-admin-view/list";
    }
}
