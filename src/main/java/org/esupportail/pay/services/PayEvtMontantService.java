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

import jakarta.annotation.Resource;
import org.esupportail.pay.dao.EmailFieldsMapReferenceDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PayEvtMontantService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Value("${esup-pay.transactionLockSecondsPeriodForLimit:3600}")
    private Long transactionLockSecondsPeriodForLimit;

    @Resource
    PayTransactionLogDaoService payTransactionLogDaoService;

    @Resource
    EmailFieldsMapReferenceDaoService emailFieldsMapReferenceDaoService;

    public boolean checkEvtMontantEnabled(PayEvtMontant payEvtMontant) {
        if(!payEvtMontant.getIsEnabled()) {
            log.info("PayEvtMontant {} found but is not enabled", payEvtMontant);
            return false;
        }
        // check if the mvnt evt reached the max amount or max number of transactions
        if(payEvtMontant.getMontantTotalMax()>-1 || payEvtMontant.getNbTransactionsMax()>-1) {
            List<PayTransactionLog> logs = payTransactionLogDaoService.findPayTransactionLogsByPayEvtMontant(payEvtMontant).getResultList();
            Date today = new Date();
            Date createdAfterDate = new Date(today.getTime() - transactionLockSecondsPeriodForLimit * 1000);
            List<EmailFieldsMapReference> emailFieldsMapReferences = emailFieldsMapReferenceDaoService.findEmailFieldsMapReferencesNotPayedByPayEvtMontantCreatedAfterDate(payEvtMontant, createdAfterDate).getResultList();
            long montantTotal = 0;
            long nbTransactions = 0;
            for(PayTransactionLog log : logs) {
                if("00000".equals(log.getErreur())) {
                    montantTotal += Long.parseLong(log.getMontant());
                    nbTransactions++;
                }
            }
            for(EmailFieldsMapReference emailFieldsMapReference : emailFieldsMapReferences) {
                long montantAsCents = Math.round(Double.valueOf(payEvtMontant.getDbleMontant() * 100));
                montantTotal += montantAsCents;
                nbTransactions++;
            }
            if(payEvtMontant.getMontantTotalMax()>-1 && montantTotal >= payEvtMontant.getMontantTotalMax()) {
                log.info("PayEvtMontant {} found but reached the max amount", payEvtMontant);
                return false;
            }
            if(payEvtMontant.getNbTransactionsMax()>-1 && nbTransactions >= payEvtMontant.getNbTransactionsMax()) {
                log.info("PayEvtMontant {} found but reached the max number of transactions", payEvtMontant);
                return false;
            }
        }
        return true;
    }
}