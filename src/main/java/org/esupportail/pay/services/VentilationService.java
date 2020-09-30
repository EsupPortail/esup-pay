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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.esupportail.pay.domain.ExportRemise;
import org.esupportail.pay.domain.ExportTransaction;
import org.esupportail.pay.domain.Ventilation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VentilationService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public List<Ventilation> getVentilations() {
		
		List<Ventilation> ventilations = new ArrayList<Ventilation>();
		
		for(ExportRemise remise : ExportRemise.findAllExportRemises("transactionDate", "DESC")) {
			Ventilation ventilation = new Ventilation();
			ventilation.setRemise(remise);
			ventilation.setDate(remise.getTransactionDate());
			Date dayBefore = DateUtils.addDays(remise.getTransactionDate(),-1);
			List<ExportTransaction> transactions = ExportTransaction.findExportTransactionsByTransactionDateBetweenAndStatutEquals(dayBefore, remise.getTransactionDate(), "Acceptée").getResultList();
			ventilation.setTransactions(transactions);
			ventilations.add(ventilation);
		}
		
		return ventilations;
	}
	
	
}


