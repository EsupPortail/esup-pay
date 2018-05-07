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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findEmailFieldsMapReferencesByReferenceEquals", "findEmailFieldsMapReferencesByPayEvtMontant" })
public class EmailFieldsMapReference {

    @NotNull
    @ManyToOne
    private PayEvtMontant payEvtMontant;

    private String reference;

    private String mail;

    private String field1;

    private String field2;
    
    private Date dateCreated;

	public static List<EmailFieldsMapReference> findOldEmailFieldsMapReferences() {
		Query q = entityManager().createQuery("select ref from EmailFieldsMapReference ref where ref.dateCreated is null or ref.dateCreated > :oldDate");
		q.setParameter("oldDate", Date.from(Instant.now().minus(Duration.ofDays(15))));
		return q.getResultList();		
	}
}
