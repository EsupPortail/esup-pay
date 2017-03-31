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
// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.pay.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import org.esupportail.pay.domain.PayEvt;

privileged aspect PayEvt_Roo_Jpa_Entity {
    
    declare @type: PayEvt: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long PayEvt.id;
    
    @Version
    @Column(name = "version")
    private Integer PayEvt.version;
    
    public Long PayEvt.getId() {
        return this.id;
    }
    
    public void PayEvt.setId(Long id) {
        this.id = id;
    }
    
    public Integer PayEvt.getVersion() {
        return this.version;
    }
    
    public void PayEvt.setVersion(Integer version) {
        this.version = version;
    }
    
}
