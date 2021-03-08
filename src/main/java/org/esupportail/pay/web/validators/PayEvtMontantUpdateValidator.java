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

import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PayEvtMontantUpdateValidator implements Validator {

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
		PayEvtMontant evtMontant = (PayEvtMontant) target;
		if(!evtMontant.getFreeAmount() && !evtMontant.getSciencesconf() && (evtMontant.getDbleMontant() == null || evtMontant.getDbleMontant() <= 0.0)) {
			errors.rejectValue("dbleMontant", "MustBePositive");
	    }
		if(!evtMontant.getFreeAmount() && !evtMontant.getSciencesconf() && !evtMontant.getDbleMontant().toString().matches("[0-9]*((\\.[0-9])?[0-9]?)")) {
			errors.rejectValue("dbleMontant", "MustBeInCents");
	    }
		if(evtMontant.getTitle().getTranslation(LOCALE_IDS.en) == null || evtMontant.getTitle().getTranslation(LOCALE_IDS.en).isEmpty()) {
			errors.rejectValue("title", "NotEmpty");
	    }
		if(evtMontant.getOptionalAddedParams() != null && !evtMontant.getOptionalAddedParams().isEmpty()) {
	        if(!PayEvtUpdateValidator.optionalAddedParamsPattern.matcher(evtMontant.getOptionalAddedParams()).matches()) {
				errors.rejectValue("optionalAddedParams", "optionalAddedParams_not_well_formed");
	        }
	    }
		if(evtMontant.getEvt().getDbleMontantMax() != null && evtMontant.getDbleMontant() != null && evtMontant.getDbleMontant() > evtMontant.getEvt().getDbleMontantMax()) {
			errors.rejectValue("dbleMontant", "dbleMontant_too_high");
	    }
	}

}
