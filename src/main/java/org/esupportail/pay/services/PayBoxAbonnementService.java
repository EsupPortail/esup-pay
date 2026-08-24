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
import java.util.List;

import org.esupportail.pay.domain.ExpectedTransaction;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.SubscriptionTimelineEntry;
import org.springframework.stereotype.Service;

@Service
public class PayBoxAbonnementService {

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
