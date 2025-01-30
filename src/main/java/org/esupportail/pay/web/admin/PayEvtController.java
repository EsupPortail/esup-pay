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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.dao.BigFileDaoService;
import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.PayEvtMontantDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.RespLogin;
import org.esupportail.pay.domain.UploadFile;
import org.esupportail.pay.security.PayPermissionEvaluator;
import org.esupportail.pay.services.EvtService;
import org.esupportail.pay.services.PayBoxServiceManager;
import org.esupportail.pay.web.validators.PayEvtUpdateValidator;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.AbstractResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
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
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/admin/evts")
@Controller
@Transactional
public class PayEvtController {

	private final Logger log = Logger.getLogger(getClass());

	@Autowired
	ServletContext servletContext;

    @Resource
    PayEvtUpdateValidator payEvtUpdateValidator;
    
    @Resource 
    PayBoxServiceManager payBoxServiceManager;
    
    @Resource
    CsvController csvController;

    @Resource
    EvtService evtService;
    
    @Resource
    BigFileDaoService bigFileDaoService;
    
    @Resource
    PayEvtMontantDaoService payEvtMontantDaoService;
    
	@Resource
    PayEvtDaoService payEvtDaoService;
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;

    @Resource
    PayPermissionEvaluator payPermissionEvaluator;
	
    Double defaultDbleMontantMax;
    
    @Value("${esup-pay.defaultDbleMontantMax:}")
    public void setDefaultDbleMontantMax(String defaultDbleMontantMaxAsString) {
    	if(defaultDbleMontantMaxAsString != null && !defaultDbleMontantMaxAsString.isEmpty()) {
    		defaultDbleMontantMax = Double.valueOf(defaultDbleMontantMaxAsString);
    	}
    }
	
	@RequestMapping(value = "/{id}/addLogoFile", method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'manage')")
	public String addLogoFile(@PathVariable("id") Long id, UploadFile uploadFile, BindingResult bindingResult, Model uiModel, HttpServletRequest request) throws IOException {
		if (bindingResult.hasErrors()) {
			log.warn(bindingResult.getAllErrors());
			return "redirect:/admin/evts/" + id.toString();
		}
		uiModel.asMap().clear();

		// get PosteCandidature from id                                                                                                                                                                                               
		PayEvt evt = payEvtDaoService.findPayEvt(id);

		MultipartFile file = uploadFile.getLogoFile();

		// sometimes file is null here, but I don't know how to reproduce this issue ... maybe that can occur only with some specifics browsers ?                                                                                     
		if(file != null) {
			Long fileSize = file.getSize();
			//String contentType = file.getContentType();
			//String filename = file.getOriginalFilename();

			InputStream inputStream = file.getInputStream();
			//byte[] bytes = IOUtils.toByteArray(inputStream);                                                                                                                                            

			bigFileDaoService.setBinaryFileStream(evt.getLogoFile(), inputStream, fileSize);
			bigFileDaoService.persist(evt.getLogoFile());
		}

		return "redirect:/admin/evts/" + id.toString();
	}

	@RequestMapping(value = "/{id}/logo")
	public void getLogoFile(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
		try {
			PayEvt evt = payEvtDaoService.findPayEvt(id);               
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
    		log.debug(bindingResult.getAllErrors());
            List<String> respLoginIds= Arrays.asList();
            if(httpServletRequest.getParameterValues("logins") != null) {
    	        respLoginIds = Arrays.asList(httpServletRequest.getParameterValues("logins"));
            }
            List<String> viewerLoginIds= Arrays.asList();;
            if(httpServletRequest.getParameterValues("viewerLogins2Add") != null) {
    	        viewerLoginIds = Arrays.asList(httpServletRequest.getParameterValues("viewerLogins2Add"));
            }
            evtService.computeLogins(payEvt, respLoginIds, viewerLoginIds);
            evtService.computeRespLogin(payEvt);
            populateEditForm(uiModel, payEvt);
            return "admin/evts/update";
        }
        uiModel.asMap().clear();        
        evtService.updateEvt(payEvt);
        return "redirect:/admin/evts/" + encodeUrlPathSegment(payEvt.getId().toString(), httpServletRequest);
    }
    
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    @PreAuthorize("hasPermission(#id, 'view')")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    	PayEvt evt = payEvtDaoService.findPayEvt(id);
    	evtService.computeRespLogin(evt);
        uiModel.addAttribute("payEvt", evt);
        uiModel.addAttribute("itemId", id);    
        if(evt!=null) {
        	List<PayEvtMontant> montants = payEvtMontantDaoService.findPayEvtMontantsByEvt(evt).getResultList();
            uiModel.addAttribute("payevtmontants", montants);
        }
        uiModel.addAttribute("canUpdate", payPermissionEvaluator.hasPermission(auth, evt, "manage"));
        uiModel.addAttribute("isAdmin", isAdmin);
        return "admin/evts/show";
    }
    
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String create(@Valid PayEvt payEvt, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            List<String> respLoginIds= Arrays.asList();
            if(httpServletRequest.getParameterValues("logins") != null) {
    	        respLoginIds = Arrays.asList(httpServletRequest.getParameterValues("logins"));
            }
            List<String> viewerLoginIds= Arrays.asList();;
            if(httpServletRequest.getParameterValues("viewerLogins2Add") != null) {
    	        viewerLoginIds = Arrays.asList(httpServletRequest.getParameterValues("viewerLogins2Add"));
            }
            evtService.computeLogins(payEvt, respLoginIds, viewerLoginIds);
            evtService.computeRespLogin(payEvt);
            populateEditForm(uiModel, payEvt);
            return "admin/evts/create.html";
        }
        uiModel.asMap().clear();

        List<String> respLoginIds= Arrays.asList();
        if(httpServletRequest.getParameterValues("logins") != null) {
	        respLoginIds = Arrays.asList(httpServletRequest.getParameterValues("logins"));
        }

        List<String> viewerLoginIds= Arrays.asList();
        if(httpServletRequest.getParameterValues("viewerLogins2Add") != null) {
	        viewerLoginIds = Arrays.asList(httpServletRequest.getParameterValues("viewerLogins2Add"));
        }

        evtService.createEvt(payEvt, respLoginIds, viewerLoginIds);

        return "redirect:/admin/evts/" + encodeUrlPathSegment(payEvt.getId().toString(), httpServletRequest);
    } 
    
    
    
    @RequestMapping(produces = "text/html")
    @PreAuthorize("hasRole('ROLE_ALL_VIEWER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_VIEWER')")
    public String list(Model uiModel, @PageableDefault(size = 10, sort = {"archived", "id"}, direction =  Sort.Direction.DESC) Pageable pageable) {
       
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isAllViewer = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ALL_VIEWER"));
        boolean isManager = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"));
        boolean isViewer = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER"));

        String currentUser = auth.getName();

        if(isAllViewer) {
            Page<PayEvt> payEvtPage = payEvtDaoService.findPagePayEvts(pageable);
            List<PayEvt> payEvts = payEvtPage.getContent();
            evtService.computeRespLogin(payEvts);
        	uiModel.addAttribute("payevts", payEvts);
            uiModel.addAttribute("page", payEvtPage);
        } else if(isManager || isViewer) {
            List<RespLogin> loginList = evtService.listEvt(currentUser);
            Page<PayEvt> payEvtPage = payEvtDaoService.findPagePayEvtsByRespLoginsOrByViewerLogins(
                    loginList,
                    pageable
            );
            List<PayEvt> payEvts = payEvtPage.getContent();
            evtService.computeRespLogin(payEvts);
    		uiModel.addAttribute("payevts", payEvts);
            uiModel.addAttribute("page", payEvtPage);

        }
        uiModel.addAttribute("isAdmin", isAdmin);
        
        return "admin/evts/list.html";
    }
    
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
    	PayEvt payEvt = new PayEvt();
    	payEvt.setDbleMontantMax(defaultDbleMontantMax);
        populateEditForm(uiModel, payEvt);
        return "admin/evts/create.html";
    }
    
    @PreAuthorize("hasPermission(#id, 'manage')")
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        PayEvt payEvt = payEvtDaoService.findPayEvt(id);
        evtService.computeRespLogin(payEvt);
        populateEditForm(uiModel, payEvt);
        return "admin/evts/update";
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        PayEvt payEvt = payEvtDaoService.findPayEvt(id);
        payEvtDaoService.remove(payEvt);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/evts";
    }
    
    @PreAuthorize("hasPermission(#id, 'view')")
    @RequestMapping(value = "/{id}/fees", produces = "text/html")
    public String fees(@PathVariable("id") Long id, @RequestParam(defaultValue = "transactionDate") String sortFieldName, @RequestParam(defaultValue = "desc") String sortOrder, Model uiModel) {
    	PayEvt payEvt = payEvtDaoService.findPayEvt(id);
        List<PayTransactionLog> paytransactionlogs = payTransactionLogDaoService.findPayTransactionLogsByPayEvt(payEvt, sortFieldName, sortOrder).getResultList();
        long total = 0L;
        for(PayTransactionLog ptl : paytransactionlogs) {
            total += Long.valueOf(ptl.getMontant());
        }
        uiModel.addAttribute("total", String.format("%,.2f€", Double.valueOf(total) / 100.0));
        uiModel.addAttribute("paytransactionlogs", paytransactionlogs);
        uiModel.addAttribute("payTransactionLog_transactiondate_date_format", DateTimeFormat.patternForStyle("MM", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("payEvt", payEvt);
        return "admin/fees-admin-view/list";
    }
    
    @PreAuthorize("hasPermission(#id, 'view')")
    @RequestMapping(value = "/{id}/fees/csv", produces = "text/html")
    public void csvFees(@PathVariable("id") Long id, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
    	PayEvt payEvt = payEvtDaoService.findPayEvt(id);
    	TypedQuery<PayTransactionLog> txLogsQuery = payTransactionLogDaoService.findPayTransactionLogsByPayEvt(payEvt, "transactionDate", "asc");
    	csvController.generateAndReturnCsv(response, txLogsQuery);
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (Exception uee) {}
        return pathSegment;
    }
}
