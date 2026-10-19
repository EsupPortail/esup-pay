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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.junit.Test;
import static org.junit.Assert.*;

public class CsvExportTest {
    @Test
    public void partialBatchesAreWrittenOnceAndPaginationAdvances() throws Exception {
        CsvController controller = new CsvController();
        var separator = CsvController.class.getDeclaredField("separator");
        separator.setAccessible(true);
        separator.set(controller, ";");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServletOutputStream stream = new ServletOutputStream() {
            public void write(int value) { output.write(value); }
            public boolean isReady() { return true; }
            public void setWriteListener(WriteListener listener) {}
        };
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{HttpServletResponse.class},
            (proxy, method, args) -> method.getName().equals("getOutputStream") ? stream : null);
        AtomicInteger calls = new AtomicInteger();
        controller.generateAndReturnCsv(response, pageable -> {
            int page = calls.getAndIncrement();
            assertTrue("CSV must terminate after the last batch", page < 3);
            assertEquals(page, pageable.getPageNumber());
            assertEquals("id", pageable.getSort().stream().toList().get(1).getProperty());
            return page == 0 ? List.of(log("first"), log("second"))
                : page == 1 ? List.of(log("third")) : List.of();
        });
        String csv = output.toString(StandardCharsets.UTF_8);
        assertEquals(3, calls.get());
        assertEquals(4, csv.split("\r\n").length);
        for (String id : List.of("first", "second", "third")) {
            assertEquals(1, csv.lines().filter(line -> line.endsWith(";" + id)).count());
        }
    }

    private PayTransactionLog log(String id) {
        PayEvtMontant amount = new PayEvtMontant();
        amount.setEvt(new PayEvt());
        PayTransactionLog log = new PayTransactionLog();
        log.setPayEvtMontant(amount);
        log.setTransactionDate(LocalDateTime.of(2026, 1, 1, 12, 0));
        log.setIdtrans(id);
        return log;
    }
}
