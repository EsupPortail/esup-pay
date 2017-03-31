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

import java.util.Date;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;

privileged aspect PayTransactionLog_Roo_JavaBean {
    
    public PayEvtMontant PayTransactionLog.getPayEvtMontant() {
        return this.payEvtMontant;
    }
    
    public void PayTransactionLog.setPayEvtMontant(PayEvtMontant payEvtMontant) {
        this.payEvtMontant = payEvtMontant;
    }
    
    public Date PayTransactionLog.getTransactionDate() {
        return this.transactionDate;
    }
    
    public void PayTransactionLog.setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public String PayTransactionLog.getUid() {
        return this.uid;
    }
    
    public void PayTransactionLog.setUid(String uid) {
        this.uid = uid;
    }
    
    public String PayTransactionLog.getField1() {
        return this.field1;
    }
    
    public void PayTransactionLog.setField1(String field1) {
        this.field1 = field1;
    }
    
    public String PayTransactionLog.getField2() {
        return this.field2;
    }
    
    public void PayTransactionLog.setField2(String field2) {
        this.field2 = field2;
    }
    
    public String PayTransactionLog.getMail() {
        return this.mail;
    }
    
    public void PayTransactionLog.setMail(String mail) {
        this.mail = mail;
    }
    
    public String PayTransactionLog.getReference() {
        return this.reference;
    }
    
    public void PayTransactionLog.setReference(String reference) {
        this.reference = reference;
    }
    
    public String PayTransactionLog.getMontant() {
        return this.montant;
    }
    
    public void PayTransactionLog.setMontant(String montant) {
        this.montant = montant;
    }
    
    public String PayTransactionLog.getAuto() {
        return this.auto;
    }
    
    public void PayTransactionLog.setAuto(String auto) {
        this.auto = auto;
    }
    
    public String PayTransactionLog.getErreur() {
        return this.erreur;
    }
    
    public void PayTransactionLog.setErreur(String erreur) {
        this.erreur = erreur;
    }
    
    public String PayTransactionLog.getIdtrans() {
        return this.idtrans;
    }
    
    public void PayTransactionLog.setIdtrans(String idtrans) {
        this.idtrans = idtrans;
    }
    
    public String PayTransactionLog.getSignature() {
        return this.signature;
    }
    
    public void PayTransactionLog.setSignature(String signature) {
        this.signature = signature;
    }
    
}
