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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EmailFieldsMapReference {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_evt_seq_gen")
    @SequenceGenerator(name = "pay_evt_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "pay_evt_montant")
    private PayEvtMontant payEvtMontant;

    private String reference;

    private String mail;

    private String field1;

    private String field2;
    
    private Date dateCreated;
    
	private Integer shoppingcartTotalQuantity;
	
	private String billingFirstname;
	
	private String billingLastname;
	
	private String billingAddress1;
	
	private String billingZipCode;
	
	private String billingCity;
	
	private String billingCountryCode;
	
}