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
package org.esupportail.pay.web.validators;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.annotation.Resource;

import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.services.LdapService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@PropertySource("classpath:META-INF/spring/esup-pay.properties")
public class PayEvtUpdateValidator implements Validator {

	public static Pattern optionalAddedParamsPattern = Pattern.compile("[^=]+=[^\\&]+(\\&[^=]+=[^\\&]+)*");
	
	@Resource
	LdapService ldapService;

	@Override
	public boolean supports(Class<?> clazz) {
		return PayEvt.class.equals(clazz);
	}

	/* 
	 * @see PayboxEvtController.update and PayboxEvt : title and payboxCommandPrefix can't already not be empty
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {

		PayEvt evt = (PayEvt) target;
		if(evt.getUrlId() == null || evt.getUrlId().isEmpty()) {
			errors.rejectValue("urlId", "NotEmpty.urlId");
	    }

		if(evt.getManagersEmails() == null || evt.getManagersEmails().isEmpty()) {
			errors.rejectValue("managersEmails", "NotEmpty.managersEmails");
	    }

		if(evt.getMailSubject() == null || evt.getMailSubject().isEmpty()) {
			errors.rejectValue("mailSubject", "NotEmpty.mailSubject");
	    }

		if(evt.getLogins() != null) {
			for(String login : evt.getLogins()) {
				if(ldapService.search(login) != null && ldapService.search(login).size()<1) {
					errors.rejectValue("logins", "login_not_found");
				}
			}
		}
		if(evt.getViewerLogins2Add() != null) {
			for(String login : evt.getViewerLogins2Add()) {
				if(ldapService.search(login) != null && ldapService.search(login).size()<1) {
					errors.rejectValue("viewerLogins2Add", "login_not_found");
				}
			}
		}
		if(evt.getDefaultOptionalAddedParams() != null && !evt.getDefaultOptionalAddedParams().isEmpty()) {
	        if(!optionalAddedParamsPattern.matcher(evt.getDefaultOptionalAddedParams()).matches()) {
				errors.rejectValue("defaultOptionalAddedParams", "optionalAddedParams_not_well_formed");
	        }
	    }
	}

}
