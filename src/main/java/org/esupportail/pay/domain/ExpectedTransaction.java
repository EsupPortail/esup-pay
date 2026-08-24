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

public class ExpectedTransaction {

    private final int installmentNumber;
    private final Double amount;
    private final LocalDate expectedDate;

    public ExpectedTransaction(int installmentNumber, Double amount, LocalDate expectedDate) {
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.expectedDate = expectedDate;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }
}
