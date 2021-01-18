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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.PayEvtMontantDaoService;
import org.esupportail.pay.domain.PayEvt;
import org.springframework.stereotype.Service;

@Service
public class UrlIdService {

    private final Logger log = Logger.getLogger(getClass());
	
    @Resource
	PayEvtDaoService payEvtDaoService;
	
    @Resource
    PayEvtMontantDaoService payEvtMontantDaoService;

	public String generateUrlId4PayEvt(String title) {
		String urlId = title.replaceAll("[^\\p{L}\\p{Nd}]+", "");
		urlId = StringUtils.stripAccents(urlId);
		try {
			urlId = URLEncoder.encode(urlId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.warn("UnsupportedEncodingException encoding " + urlId, e);
		}
		int i = 1;
		while(!payEvtDaoService.findPayEvtsByUrlIdEquals(urlId).getResultList().isEmpty()) {
			urlId = urlId + i++;
		}
		return urlId;
	}
	
	public String generateUrlId4PayEvtMontant(PayEvt payboxEvt, String title) {
		String urlId = title.replaceAll("[^\\p{L}\\p{Nd}]+", "");
		urlId = StringUtils.stripAccents(urlId);
		try {
			urlId = URLEncoder.encode(urlId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.warn("UnsupportedEncodingException encoding " + urlId, e);
		}
		int i = 1;
		while(!payEvtMontantDaoService.findPayEvtMontantsByEvtAndUrlIdEquals(payboxEvt, urlId).getResultList().isEmpty()) {
			urlId = urlId + i++;
		}
		return urlId;
	}

}
