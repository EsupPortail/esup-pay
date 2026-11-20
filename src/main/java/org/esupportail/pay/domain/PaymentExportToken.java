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

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PaymentExportToken {

    @Id
    private Long eventId;

    @Column(nullable = false, length = 64)
    private String digest;

    @Column(nullable = false)
    private Instant expiresAt;

    protected PaymentExportToken() {
    }

    public PaymentExportToken(Long eventId, String digest, Instant expiresAt) {
        this.eventId = eventId;
        this.digest = digest;
        this.expiresAt = expiresAt;
    }

    public String getDigest() {
        return digest;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
