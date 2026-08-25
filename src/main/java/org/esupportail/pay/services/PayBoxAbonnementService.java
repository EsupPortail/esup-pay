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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.ExpectedTransaction;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.SubscriptionTimelineEntry;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PayBoxAbonnementService {

    @Resource
    PayTransactionLogDaoService payTransactionLogDaoService;

    public List<SubscriptionTimelineEntry> computeSubscriptionTimeline(List<PayTransactionLog> paytransactionlogs) {
        List<SubscriptionTimelineEntry> timeline = new ArrayList<>();
        int attemptedTransactionsCount = 0;
        for (PayTransactionLog payTransactionLog : paytransactionlogs) {
            attemptedTransactionsCount++;
            boolean isSuccessful = "00000".equals(payTransactionLog.getErreur());
            if (isSuccessful) {
                timeline.add(SubscriptionTimelineEntry.success(payTransactionLog));
            } else {
                timeline.add(SubscriptionTimelineEntry.failed(payTransactionLog));
            }
        }
        if (!paytransactionlogs.isEmpty()) {
            PayEvtMontant payEvtMontant = paytransactionlogs.get(0).getPayEvtMontant();
            if (payEvtMontant != null && payEvtMontant.getPaiementMultiple_montant2() != null) {
                List<ExpectedTransaction> expectedTransactions = computeExpectedTransactions(payEvtMontant);
                for (ExpectedTransaction expectedTransaction : expectedTransactions) {
                    if (expectedTransaction.getInstallmentNumber() > attemptedTransactionsCount) {
                        timeline.add(SubscriptionTimelineEntry.expected(expectedTransaction));
                    }
                }
            }
        }
        timeline.sort(Comparator.comparing(
            SubscriptionTimelineEntry::getSortDate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return timeline;
    }

    /**
     * Returns the CSS status class ("success", "danger", "info") for a given subscription:
     * - "danger"  if at least one transaction failed
     * - "info"    if no failures but some transactions are still expected
     * - "success" if all expected transactions were completed successfully
     */
    public String computeSubscriptionStatus(String idAbo) {
        List<PayTransactionLog> logs = payTransactionLogDaoService.findPayTransactionLogsByIdAbo(idAbo, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<SubscriptionTimelineEntry> timeline = computeSubscriptionTimeline(logs);
        boolean hasFailed = timeline.stream().anyMatch(e -> "failed".equals(e.getKind()));
        if (hasFailed) return "danger";
        boolean hasExpected = timeline.stream().anyMatch(e -> "expected".equals(e.getKind()));
        if (hasExpected) return "info";
        return "success";
    }

    /**
     * Builds a map of idAbo -> CSS status class for all rows in a page that have a subscription.
     */
    public Map<String, String> buildAboStatusMap(Page<PayTransactionLog> page) {
        Map<String, String> map = new HashMap<>();
        for (PayTransactionLog log : page) {
            String idAbo = log.getIdAbo();
            if (idAbo != null && !"0".equals(idAbo) && !map.containsKey(idAbo)) {
                map.put(idAbo, computeSubscriptionStatus(idAbo));
            }
        }
        return map;
    }

    private List<ExpectedTransaction> computeExpectedTransactions(PayEvtMontant payEvtMontant) {
        if (payEvtMontant.getPaiementMultiple_montant2() == null) {
            return List.of();
        }
        List<ExpectedTransaction> expectedTransactions = new ArrayList<ExpectedTransaction>();
        expectedTransactions.add(new ExpectedTransaction(2, payEvtMontant.getPaiementMultiple_montant2(), payEvtMontant.getOrComputePaiementMultiple_date2()));
        if (payEvtMontant.getPaiementMultiple_montant3() != null) {
            expectedTransactions.add(new ExpectedTransaction(3, payEvtMontant.getPaiementMultiple_montant3(), payEvtMontant.getOrComputePaiementMultiple_date3()));
        }
        if (payEvtMontant.getPaiementMultiple_montant4() != null) {
            expectedTransactions.add(new ExpectedTransaction(4, payEvtMontant.getPaiementMultiple_montant4(), payEvtMontant.getOrComputePaiementMultiple_date4()));
        }
        return expectedTransactions;
    }
}
