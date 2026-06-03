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
import org.apache.commons.lang3.StringUtils;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
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
		if(!evtMontant.getFreeAmount() && !evtMontant.getSciencesconf()) {
		    validate_amount(evtMontant.getDbleMontant(), "dbleMontant", errors);
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
	    validate_paiementMultiple(evtMontant, errors);
	}

    private void validate_amount(Double amount, String field, Errors errors) {
        if (amount == null) {
            errors.rejectValue(field, "NotEmpty");
        } else if (amount.isNaN()) {
            errors.rejectValue(field, "MustBeInCents");
        } else if (amount <= 0.0) {
            errors.rejectValue(field, "MustBePositive");
        } else if (!amount.toString().matches("[0-9]*((\\.[0-9])?[0-9]?)")) {
            errors.rejectValue(field, "MustBeInCents");
        }
    }

    private void validate_paiementMultiple(PayEvtMontant evtMontant, Errors errors) {
        var kind = evtMontant.getPaiementMultiple_kind();
        if (StringUtils.isEmpty(kind)) return;

        if (evtMontant.getFreeAmount()) {
            errors.rejectValue("paiementMultiple_kind", "paiementMultiple_incompatible_avec_freeAmount");
            return;
        }
        

        if (evtMontant.getPaiementMultiple_montant1() == null) {
            errors.rejectValue("paiementMultiple_montant1", "NotEmpty");
        }
        if (evtMontant.getPaiementMultiple_montant2() == null) {
            errors.rejectValue("paiementMultiple_montant1", "NotEmpty");
        }
        if (evtMontant.getDbleMontant() != null && evtMontant.getPaiementMultiple_montant_total() != evtMontant.getDbleMontant()) {
            errors.reject("paiementMultiple_montant_total_not_equal", 
                new Object[] { evtMontant.getPaiementMultiple_montant_total(), evtMontant.getDbleMontant() },
                "paiementMultiple_montant_total_not_equal");
        }

        if (evtMontant.getEvt().getDbleMontantMax() != null) {
              if(evtMontant.getPaiementMultiple_montant2() > evtMontant.getEvt().getDbleMontantMax()) {
                  errors.rejectValue("paiementMultiple_montant2", "dbleMontant_too_high");
              }
              if(evtMontant.getPaiementMultiple_montant3() != null && evtMontant.getPaiementMultiple_montant3() > evtMontant.getEvt().getDbleMontantMax()) {
                  errors.rejectValue("paiementMultiple_montant3", "dbleMontant_too_high");
              }
              if(evtMontant.getPaiementMultiple_montant4() != null && evtMontant.getPaiementMultiple_montant4() > evtMontant.getEvt().getDbleMontantMax()) {
                  errors.rejectValue("paiementMultiple_montant4", "dbleMontant_too_high");
              }
        }
        if (PayEvtMontant.paiementMultiple_kind_datesPrecise.equals(kind)) {
            if (evtMontant.getPaiementMultiple_date2() == null) {
                 errors.rejectValue("paiementMultiple_date2", "NotEmpty");
            }
            if (evtMontant.getPaiementMultiple_montant3() != null && evtMontant.getPaiementMultiple_date3() == null) {
                 errors.rejectValue("paiementMultiple_date3", "NotEmpty");
            }
            if (evtMontant.getPaiementMultiple_montant4() != null && evtMontant.getPaiementMultiple_date4() == null) {
                 errors.rejectValue("paiementMultiple_date4", "NotEmpty");
            }
        } else if (PayEvtMontant.paiementMultiple_kind_simulateFrequence.equals(kind)) {
            if (evtMontant.getPaiementMultiple_simulateFrequenceEnMois() == null) {
                 errors.rejectValue("paiementMultiple_simulateFrequenceEnMois", "NotEmpty");
            }
        }
    }
}
