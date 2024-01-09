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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.Label.LOCALE_IDS;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/csv")
@Controller
@Transactional
public class CsvController {

	private final Logger log = Logger.getLogger(getClass());
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;
	
	@Value("${csv.separator:;}")
	private String separator;
	
	@RequestMapping
    public void getCsv(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
		
		TypedQuery<PayTransactionLog> txLogsQuery = payTransactionLogDaoService.findAllPayTransactionLogsQuery("transactionDate", "asc");
		
		generateAndReturnCsv(response, txLogsQuery);

    }

	public void generateAndReturnCsv(HttpServletResponse response, TypedQuery<PayTransactionLog> txLogsQuery)
			throws UnsupportedEncodingException, IOException {
		StopWatch stopWatch = new StopWatch("Stream - build CSV and send it");
		stopWatch.start();
		
        response.setContentType("text/csv");
        response.setCharacterEncoding("utf-8");   
        response.setHeader("Content-Disposition","attachment; filename=\"esup-pay-transaction-log.csv\"");
        
		Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF8");
		
		List<String> headers = Arrays.asList(new String[] {"Date transaction", "payEvt", "payEvtMontant", "mail", "field1", "field2", "montant", "ID transaction"});
		String csv = StringUtils.join(headers, separator);
		writer.write(csv);

        int offset = 0;
        int nbLine = 0;
        List<PayTransactionLog> txLogs;
        while ((txLogs = txLogsQuery.setFirstResult(offset).setMaxResults(1000).getResultList()).size() > 0) {
        	log.debug("Build CSV Iteration - offset : " + offset);
	    	for(PayTransactionLog txLog : txLogs) {
	    		List<String> entries = new ArrayList<String>();
	    		entries.add(txLog.getTransactionDate().toString());
	    		entries.add(txLog.getPayEvtMontant().getEvt().getTitle().getTranslation(LOCALE_IDS.fr));
	    		entries.add(txLog.getPayEvtMontant().getTitle().getTranslation(LOCALE_IDS.fr));
	    		entries.add(txLog.getMail());
	    		entries.add(txLog.getField1());
	    		entries.add(txLog.getField2());
	    		entries.add(txLog.getMontant());
	    		entries.add(txLog.getIdtrans());
	    		csv = "\r\n" + StringUtils.join(entries, separator);
	    		writer.write(csv);
	    		nbLine++;
	    	} 
	    	offset += txLogs.size();
        }
    	stopWatch.stop();	    	
    	log.info("CSV of " + nbLine + " lines sent in " + stopWatch.getTotalTimeSeconds() + " sec.");

        writer.close();
	}

}
