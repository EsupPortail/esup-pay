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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.services.UrlIdService;
import org.esupportail.pay.web.validators.PayEvtMontantUpdateValidator;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/admin/evtmnts")
@Controller
@RooWebScaffold(path = "admin/evtmnts", formBackingObject = PayEvtMontant.class)
public class PayEvtMontantController {
	
    @Resource
    UrlIdService urlIdService;
    
    @ModelAttribute("addPrefixList")
    public List<String> getAddPrefixList() {
    	return Arrays.asList(new String[]{"", "field1", "field2"});
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    @PreAuthorize("hasPermission(#evtId, 'manage')")
    public String createForm(Model uiModel, @RequestParam(required=true) Long evtId) {
    	PayEvtMontant payEvtMontant = new PayEvtMontant();
    	PayEvt evt =  PayEvt.findPayEvt(evtId);
    	payEvtMontant.setEvtWithDefaultParametersIfNeeded(evt);
        uiModel.addAttribute("payEvtMontant", payEvtMontant);
        return "admin/evtmnts/create";
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html", value="/addMontant")
    @PreAuthorize("hasPermission(#payEvtMontant, 'manage')")
    public String addMontant(@Valid PayEvtMontant payEvtMontant, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	PayEvtMontantUpdateValidator payEvtMontantValidator = new PayEvtMontantUpdateValidator();
    	payEvtMontantValidator.validate(payEvtMontant, bindingResult);
    	if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, payEvtMontant);
            return "admin/evtmnts/create";
        }
        uiModel.asMap().clear();
        PayEvt evt = PayEvt.findPayEvt(payEvtMontant.getEvt().getId());
        payEvtMontant.setEvtWithDefaultParametersIfNeeded(evt);
        
        if(payEvtMontant.getUrlId() == null || payEvtMontant.getUrlId().isEmpty()) {
        	String urlId = urlIdService.generateUrlId4PayEvtMontant(evt, payEvtMontant.getTitle().getTranslation(LOCALE_IDS.en));
        	payEvtMontant.setUrlId(urlId);
        }
        
        payEvtMontant.persist();
        return "redirect:/admin/evts/" + encodeUrlPathSegment(evt.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid PayEvtMontant payEvtMontant, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	PayEvtMontantUpdateValidator payEvtMontantValidator = new PayEvtMontantUpdateValidator();
    	payEvtMontantValidator.validate(payEvtMontant, bindingResult);
    	if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, payEvtMontant);
            return "admin/evtmnts/update";
        }
        uiModel.asMap().clear();
        payEvtMontant.merge();
        return "redirect:/admin/evtmnts/" + encodeUrlPathSegment(payEvtMontant.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        return "redirect:/admin/evtmnts/" + id  + "?form";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        PayEvtMontant payEvtMontant = PayEvtMontant.findPayEvtMontant(id);
    	Long evtId = payEvtMontant.getEvt().getId();
    	if(payEvtMontant.isDeletable()) {
    		payEvtMontant.remove();
    	} else {
    		uiModel.addAttribute("alert_msg", "pay_admin_evtmontant_delete_abort");
    	}
        return "redirect:/admin/evts/" + evtId;
    }
    
}
