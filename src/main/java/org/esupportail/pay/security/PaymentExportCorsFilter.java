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
package org.esupportail.pay.security;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Component
public class PaymentExportCorsFilter extends CorsFilter {

    private static final String EXPORT_PATH = "/api/payment-exports/*/payments.csv";
    private static final PathPatternRequestMatcher EXPORT =
        PathPatternRequestMatcher.withDefaults().matcher(EXPORT_PATH);

    public PaymentExportCorsFilter(@Value("${esup-pay.onlyoffice-origins:}") String origins) {
        super(exportCorsConfiguration(origins));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !EXPORT.matches(request);
    }

    private static UrlBasedCorsConfigurationSource exportCorsConfiguration(String origins) {
        var source = new UrlBasedCorsConfigurationSource();
        var allowedOrigins = Arrays.stream(origins.split(",")).map(String::strip)
            .filter(origin -> !origin.isEmpty()).toList();
        for (String origin : allowedOrigins) {
            URI uri = URI.create(origin);
            if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                || !uri.getRawPath().isEmpty() || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("esup-pay.onlyoffice-origins requires HTTPS origins without paths");
            }
        }
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(allowedOrigins);
        cors.setAllowedMethods(List.of("GET"));
        cors.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION));
        source.registerCorsConfiguration(EXPORT_PATH, cors);
        return source;
    }
}
