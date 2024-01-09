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
package org.esupportail.pay.web;

import jakarta.annotation.Resource;

import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.PayEvtMontantDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

@Configurable
/**
 * A central place to register application converters and formatters. 
 */
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {
   
	@Resource
    PayEvtMontantDaoService payEvtMontantDaoService;
	
	@Resource
    PayEvtDaoService payEvtDaoService;
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;
	
	public Converter<PayEvt, String> getPayEvtToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.esupportail.pay.domain.PayEvt, java.lang.String>() {
            public String convert(PayEvt payboxEvt) {
                return new StringBuilder().append(payboxEvt.getPayboxServiceKey()).append(' ').append(payboxEvt.getWebSiteUrl()).append(' ').append(payboxEvt.getUrlId()).append(' ').append(payboxEvt.getManagersEmails()).toString();
            }
        };
    }

	public Converter<Long, PayEvt> getIdToPayEvtConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, org.esupportail.pay.domain.PayEvt>() {
            public org.esupportail.pay.domain.PayEvt convert(java.lang.Long id) {
                return payEvtDaoService.findPayEvt(id);
            }
        };
    }

	public Converter<String, PayEvt> getStringToPayEvtConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.esupportail.pay.domain.PayEvt>() {
            public org.esupportail.pay.domain.PayEvt convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PayEvt.class);
            }
        };
    }

	public Converter<PayEvtMontant, String> getPayEvtMontantToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.esupportail.pay.domain.PayEvtMontant, java.lang.String>() {
            public String convert(PayEvtMontant payboxEvtMontant) {
                return new StringBuilder().append(payboxEvtMontant.getDbleMontant()).append(' ').append(payboxEvtMontant.getUrlId()).append(' ').append(payboxEvtMontant.getAddPrefix()).toString();
            }
        };
    }

	public Converter<Long, PayEvtMontant> getIdToPayEvtMontantConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, org.esupportail.pay.domain.PayEvtMontant>() {
            public org.esupportail.pay.domain.PayEvtMontant convert(java.lang.Long id) {
                return payEvtMontantDaoService.findPayEvtMontant(id);
            }
        };
    }

	public Converter<String, PayEvtMontant> getStringToPayEvtMontantConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.esupportail.pay.domain.PayEvtMontant>() {
            public org.esupportail.pay.domain.PayEvtMontant convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PayEvtMontant.class);
            }
        };
    }

	public Converter<PayTransactionLog, String> getPayTransactionLogToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<org.esupportail.pay.domain.PayTransactionLog, java.lang.String>() {
            public String convert(PayTransactionLog payboxTransactionLog) {
                return new StringBuilder().append(payboxTransactionLog.getTransactionDate()).append(' ').append(payboxTransactionLog.getField1()).append(' ').append(payboxTransactionLog.getField2()).toString();
            }
        };
    }

	public Converter<Long, PayTransactionLog> getIdToPayTransactionLogConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, org.esupportail.pay.domain.PayTransactionLog>() {
            public org.esupportail.pay.domain.PayTransactionLog convert(java.lang.Long id) {
                return payTransactionLogDaoService.findPayTransactionLog(id);
            }
        };
    }

	public Converter<String, PayTransactionLog> getStringToPayTransactionLogConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, org.esupportail.pay.domain.PayTransactionLog>() {
            public org.esupportail.pay.domain.PayTransactionLog convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PayTransactionLog.class);
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getPayEvtToStringConverter());
        registry.addConverter(getIdToPayEvtConverter());
        registry.addConverter(getStringToPayEvtConverter());
        registry.addConverter(getPayEvtMontantToStringConverter());
        registry.addConverter(getIdToPayEvtMontantConverter());
        registry.addConverter(getStringToPayEvtMontantConverter());
        registry.addConverter(getPayTransactionLogToStringConverter());
        registry.addConverter(getIdToPayTransactionLogConverter());
        registry.addConverter(getStringToPayTransactionLogConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
