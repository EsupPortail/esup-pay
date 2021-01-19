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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PayTransactionLog {
	  
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

    @NotNull
    @ManyToOne
    private PayEvtMontant payEvtMontant;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MM")
    private Date transactionDate;
    
    private String field1;

    private String field2;

    private String mail;

    private String reference;

    private String montant;

    private String auto;

    private String erreur;

    private String idtrans;

    private String signature;
    
    private Boolean mailSent = null;
    
    /* pour compatibilité ascendante */
    public Boolean getMailSent() {
        return this.mailSent == null || this.mailSent;
    }

    public String getMontantDevise() {
        Double mnt = new Double(montant) / 100.0;
        return mnt.toString();
    }
    
    public Label getEvtTitle() {
    	return this.getPayEvtMontant().getEvt().getTitle();
    }
    
}

