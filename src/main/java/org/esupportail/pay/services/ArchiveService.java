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

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.persistence.TypedQuery;

import org.esupportail.pay.domain.PayEvtMontant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.esupportail.pay.dao.EmailFieldsMapReferenceDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.dao.ScienceConfReferenceDaoService;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.ScienceConfReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArchiveService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	static final String ARCHIVED = "archived";
	
	@Resource
	EmailFieldsMapReferenceDaoService emailFieldsMapReferenceDaoService;
	
	@Value("${archive.enabled:false}")
	private Boolean enabled;
	
	@Value("${archive.oldDays.emailFieldsMapReference:15}")
	private Long oldDays4emailFieldsMapReference;
	
	@Value("${archive.oldDays.transactionsLogs:1825}")
	private Long oldDays4transactionsLogs;
	
	@Resource
	ScienceConfReferenceDaoService scienceConfReferenceDaoService;
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;

	@Scheduled(fixedDelay=3600000)
	public void removeOldTmpEmailFieldsMapReference() {		
		if(enabled) {
			log.debug("removeOldTmpEmailFieldsMapReference called");
			List<EmailFieldsMapReference> emailFieldsMapReferences2remove = emailFieldsMapReferenceDaoService.findOldEmailFieldsMapReferences(oldDays4emailFieldsMapReference);
			Set<EmailFieldsMapReference> emailFieldsMapReferences2notRemoveBecausePaimentMultiple = new HashSet<>();
			for(EmailFieldsMapReference emailFieldsMapReference : emailFieldsMapReferences2remove) {
				PayEvtMontant payEvtMontant = emailFieldsMapReference.getPayEvtMontant();
				if(payEvtMontant.getPaiementMultiple_kind().isEmpty()) {
					TypedQuery<ScienceConfReference> q = scienceConfReferenceDaoService.findScienceConfReferencesByEmailFieldsMapReference(emailFieldsMapReference);
					if (!q.getResultList().isEmpty()) {
						scienceConfReferenceDaoService.remove(q.getSingleResult());
					}
					emailFieldsMapReferenceDaoService.remove(emailFieldsMapReference);
				} else {
					// multiple paiement : emailFieldsMapReference should not be removed before all paiement is OK
					LocalDate lastDate = payEvtMontant.getPaiementMultiple_last_date();
					if(lastDate.plusDays(oldDays4emailFieldsMapReference).isAfter(LocalDate.now())) {
						emailFieldsMapReferenceDaoService.remove(emailFieldsMapReference);
					}  else {
						emailFieldsMapReferences2notRemoveBecausePaimentMultiple.add(emailFieldsMapReference);
					}
				}
			}
			if(emailFieldsMapReferences2notRemoveBecausePaimentMultiple.size()>0) {
				log.debug(emailFieldsMapReferences2notRemoveBecausePaimentMultiple.size() + " old emailFieldsMapReferences not removed because used in multiple paiements");
			}
			emailFieldsMapReferences2remove.removeAll(emailFieldsMapReferences2notRemoveBecausePaimentMultiple);
			if(emailFieldsMapReferences2remove.size()>0) {
				log.info(emailFieldsMapReferences2remove.size() + " old emailFieldsMapReferences removed");
			}
		}
	}
	
	@Scheduled(fixedDelay=3600000)
	public void anonymiseOldTransactions() {	
		if(enabled) {
			log.debug("anonymiseOldTransactions called");
			List<PayTransactionLog> transactionLogs = payTransactionLogDaoService.findOldPayTransactionLogs(oldDays4transactionsLogs);
			for(PayTransactionLog transactionLog : transactionLogs) {
				anonymise(transactionLog);
			}
			if(transactionLogs.size()>0) {
				log.info(transactionLogs.size() + " old transactionLogs anonymised");
			}
		}
	}

	private void anonymise(PayTransactionLog transactionLog) {
		transactionLog.setField1(ARCHIVED);
		transactionLog.setField2(ARCHIVED);
		transactionLog.setMail(ARCHIVED);
		transactionLog.setReference(ARCHIVED);
		transactionLog.setBillingFirstname(ARCHIVED);
		transactionLog.setBillingLastname(ARCHIVED);
		transactionLog.setBillingAddress1(ARCHIVED);
		transactionLog.setBillingZipCode(ARCHIVED);
		transactionLog.setBillingCity(ARCHIVED);
		transactionLog.setBillingCountryCode(ARCHIVED);
	}
	
}