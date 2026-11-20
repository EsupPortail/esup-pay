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
package org.esupportail.pay.web.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PaymentExportToken;
import org.esupportail.pay.repository.PayTransactionLogFilter;
import org.esupportail.pay.repository.PayTransactionLogRepository;
import org.esupportail.pay.repository.PayTransactionLogSpecifications;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PaymentExportController {

    private static final String SETTINGS = "/admin/evts/{id}/fees/payment-export";
    private static final String EXPORT = "/api/payment-exports/{id}/payments.csv";
    private static final Pattern BEARER = Pattern.compile("Bearer +([A-Za-z0-9_-]{43})", Pattern.CASE_INSENSITIVE);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PayTransactionLogRepository transactions;
    private final CsvController csvController;
    private final String applicationUrl;

    @PersistenceContext
    private EntityManager entities;

    public PaymentExportController(PayTransactionLogRepository transactions, CsvController csvController,
                                   @Value("#{environment.getProperty('esup-pay.url', propertyConfigurer['esup-pay.url'])}")
                                   String applicationUrl) {
        this.transactions = transactions;
        this.csvController = csvController;
        this.applicationUrl = applicationUrl.replaceFirst("/+$", "");
    }

    @GetMapping(SETTINGS)
    @PreAuthorize("hasPermission(#id, 'manage')")
    @Transactional(readOnly = true)
    public String settings(@PathVariable Long id, Model model, HttpServletResponse response) {
        PayEvt event = requireEvent(id, LockModeType.NONE);
        return renderSettings(event, entities.find(PaymentExportToken.class, id), model, response);
    }

    @PostMapping(SETTINGS)
    @PreAuthorize("hasPermission(#id, 'manage')")
    @Transactional
    public String generate(@PathVariable Long id, Model model, HttpServletResponse response) {
        PayEvt event = requireEvent(id, LockModeType.PESSIMISTIC_WRITE);
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        var credential = new PaymentExportToken(id, digest(token), Instant.now().plus(90, ChronoUnit.DAYS));
        entities.merge(credential);
        model.addAttribute("authorization", "Bearer " + token);
        return renderSettings(event, credential, model, response);
    }

    @PostMapping(SETTINGS + "/revoke")
    @PreAuthorize("hasPermission(#id, 'manage')")
    @Transactional
    public String revoke(@PathVariable Long id) {
        requireEvent(id, LockModeType.PESSIMISTIC_WRITE);
        PaymentExportToken credential = entities.find(PaymentExportToken.class, id);
        if (credential != null) {
            entities.remove(credential);
        }
        return "redirect:/admin/evts/" + id + "/fees/payment-export";
    }

    @GetMapping(EXPORT)
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public void download(@PathVariable Long id,
                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, defaultValue = "") String authorization,
                         HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        var bearer = BEARER.matcher(authorization);
        PaymentExportToken credential = bearer.matches() ? entities.find(PaymentExportToken.class, id) : null;
        if (credential == null || !credential.getExpiresAt().isAfter(Instant.now())
                || !MessageDigest.isEqual(credential.getDigest().getBytes(StandardCharsets.US_ASCII),
                                          digest(bearer.group(1)).getBytes(StandardCharsets.US_ASCII))) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        PayEvt event = requireEvent(id, LockModeType.NONE);
        var filter = PayTransactionLogFilter.builder().successfulOnly(true).build();
        var specification = PayTransactionLogSpecifications.forEvent(event, null, filter);
        csvController.generateSpreadsheetCsv(response,
            batch -> transactions.findBy(specification, query -> query.slice(batch)).getContent());
    }

    private PayEvt requireEvent(Long id, LockModeType lockMode) {
        PayEvt event = entities.find(PayEvt.class, id, lockMode);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return event;
    }

    private String renderSettings(PayEvt event, PaymentExportToken credential, Model model,
                                  HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        model.addAttribute("payEvt", event);
        model.addAttribute("exportUrl", applicationUrl + EXPORT.replace("{id}", event.getId().toString()));
        if (credential != null && credential.getExpiresAt().isAfter(Instant.now())) {
            model.addAttribute("expiresOn", credential.getExpiresAt().toString());
        }
        return "admin/fees-admin-view/payment-export";
    }

    private static String digest(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
