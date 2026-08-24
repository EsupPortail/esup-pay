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
package org.esupportail.pay.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SubscriptionTimelineEntry {
    private final Long logId;
    private final String kind;
    private final String cssClass;
    private final LocalDateTime transactionDate;
    private final LocalDate expectedDate;
    private final LocalDateTime sortDate;
    private final String amountDisplay;
    private final String idtrans;
    private final String reference;
    private final String errorCode;
    private final Integer installmentNumber;

    private SubscriptionTimelineEntry(
        Long logId,
        String kind,
        String cssClass,
        LocalDateTime transactionDate,
        LocalDate expectedDate,
        String amountDisplay,
        String idtrans,
        String reference,
        String errorCode,
        Integer installmentNumber
    ) {
        this.logId = logId;
        this.kind = kind;
        this.cssClass = cssClass;
        this.transactionDate = transactionDate;
        this.expectedDate = expectedDate;
        this.sortDate = transactionDate != null ? transactionDate : (expectedDate != null ? expectedDate.atStartOfDay() : null);
        this.amountDisplay = amountDisplay;
        this.idtrans = idtrans;
        this.reference = reference;
        this.errorCode = errorCode;
        this.installmentNumber = installmentNumber;
    }

    public static SubscriptionTimelineEntry success(PayTransactionLog log) {
        return new SubscriptionTimelineEntry(
            log.getId(),
            "done",
            "success",
            log.getTransactionDate(),
            null,
            log.getMontantDevise(),
            log.getIdtrans(),
            log.getReference(),
            null,
            null
        );
    }

    public static SubscriptionTimelineEntry failed(PayTransactionLog log) {
        return new SubscriptionTimelineEntry(
            log.getId(),
            "failed",
            "danger",
            log.getTransactionDate(),
            null,
            log.getMontantDevise(),
            log.getIdtrans(),
            log.getReference(),
            log.getErreur(),
            null
        );
    }

    public static SubscriptionTimelineEntry expected(ExpectedTransaction tx) {
        return new SubscriptionTimelineEntry(
            null,
            "expected",
            "info",
            null,
            tx.getExpectedDate(),
            String.format("%,.2f€", tx.getAmount()),
            null,
            null,
            null,
            tx.getInstallmentNumber()
        );
    }

    public Long getLogId() {
        return logId;
    }

    public String getKind() {
        return kind;
    }

    public String getCssClass() {
        return cssClass;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public LocalDateTime getSortDate() {
        return sortDate;
    }

    public String getAmountDisplay() {
        return amountDisplay;
    }

    public String getIdtrans() {
        return idtrans;
    }

    public String getReference() {
        return reference;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }
}
