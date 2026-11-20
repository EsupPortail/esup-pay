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

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PaymentExportToken;
import org.esupportail.pay.repository.PayTransactionLogRepository;
import org.esupportail.pay.security.PaymentExportCorsFilter;
import org.junit.Test;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

public class PaymentExportControllerTest {

    @Test
    public void exportRequiresTheCurrentUnexpiredBearerForTheRequestedEvent() throws Exception {
        PayEvt firstEvent = new PayEvt();
        firstEvent.setId(1L);
        PayEvt secondEvent = new PayEvt();
        secondEvent.setId(2L);

        Map<Long, PayEvt> events = Map.of(1L, firstEvent, 2L, secondEvent);
        Map<Long, PaymentExportToken> credentials = new HashMap<>();
        EntityManager entities = (EntityManager) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{EntityManager.class}, (proxy, method, args) -> switch (method.getName()) {
                case "find" -> (args[0] == PayEvt.class ? events : credentials).get(args[1]);
                case "merge" -> {
                    credentials.put(1L, (PaymentExportToken) args[0]);
                    yield args[0];
                }
                case "remove" -> credentials.remove(1L);
                default -> throw new UnsupportedOperationException(method.getName());
            });
        PayTransactionLogRepository transactions = (PayTransactionLogRepository) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{PayTransactionLogRepository.class},
            (proxy, method, args) -> new SliceImpl<>(List.of()));
        var controller = new PaymentExportController(transactions, new CsvController(), "https://pay.example.org/");
        ReflectionTestUtils.setField(controller, "entities", entities);
        String pluginOrigin = "https://office.example.org";
        var mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilters(new PaymentExportCorsFilter(pluginOrigin)).build();
        String path = "/api/payment-exports/1/payments.csv";

        var preflight = mvc.perform(options(path).header("Origin", pluginOrigin)
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization")).andReturn().getResponse();
        assertEquals(200, preflight.getStatus());
        assertEquals(pluginOrigin, preflight.getHeader("Access-Control-Allow-Origin"));
        assertEquals(403, mvc.perform(options(path).header("Origin", "https://other.example.org")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization")).andReturn().getResponse().getStatus());

        var first = new ExtendedModelMap();
        controller.generate(1L, first, new MockHttpServletResponse());
        String bearer = (String) first.get("authorization");
        assertEquals("https://pay.example.org" + path, first.get("exportUrl"));
        assertEquals(401, mvc.perform(get(path).param("token", bearer.substring(7))).andReturn().getResponse().getStatus());
        assertEquals(401, mvc.perform(get("/api/payment-exports/2/payments.csv").header("Authorization", bearer))
            .andReturn().getResponse().getStatus());
        var download = mvc.perform(get(path).header("Authorization", bearer).header("Origin", pluginOrigin))
            .andReturn().getResponse();
        assertEquals(200, download.getStatus());
        assertEquals(pluginOrigin, download.getHeader("Access-Control-Allow-Origin"));

        var replacement = new ExtendedModelMap();
        controller.generate(1L, replacement, new MockHttpServletResponse());
        String newBearer = (String) replacement.get("authorization");
        assertNotEquals(bearer, newBearer);
        assertEquals(401, mvc.perform(get(path).header("Authorization", bearer)).andReturn().getResponse().getStatus());

        controller.revoke(1L);
        assertEquals(401, mvc.perform(get(path).header("Authorization", newBearer)).andReturn().getResponse().getStatus());
        controller.generate(1L, replacement, new MockHttpServletResponse());
        credentials.put(1L, new PaymentExportToken(1L, credentials.get(1L).getDigest(), Instant.EPOCH));
        assertEquals(401, mvc.perform(get(path).header("Authorization", replacement.get("authorization")))
            .andReturn().getResponse().getStatus());
    }
}
