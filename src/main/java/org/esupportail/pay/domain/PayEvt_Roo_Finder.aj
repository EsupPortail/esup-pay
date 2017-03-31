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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.RespLogin;

privileged aspect PayEvt_Roo_Finder {
    
    public static Long PayEvt.countFindPayEvtsByRespLogins(List<RespLogin> respLogins) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        TypedQuery q = em.createQuery(queryBuilder.toString(), Long.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return ((Long) q.getSingleResult());
    }
    
    public static Long PayEvt.countFindPayEvtsByUrlIdEquals(String urlId) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvt.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PayEvt AS o WHERE o.urlId = :urlId", Long.class);
        q.setParameter("urlId", urlId);
        return ((Long) q.getSingleResult());
    }
    
    public static Long PayEvt.countFindPayEvtsByViewerLogins(List<RespLogin> viewerLogins) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(o) FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        TypedQuery q = em.createQuery(queryBuilder.toString(), Long.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _resplogin: viewerLogins) {
            q.setParameter("viewerLogins_item" + viewerLoginsIndex++, _resplogin);
        }
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<PayEvt> PayEvt.findPayEvtsByRespLogins(List<RespLogin> respLogins) {
        if (respLogins == null) throw new IllegalArgumentException("The respLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < respLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :respLogins_item").append(i).append(" MEMBER OF o.respLogins");
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int respLoginsIndex = 0;
        for (RespLogin _resplogin: respLogins) {
            q.setParameter("respLogins_item" + respLoginsIndex++, _resplogin);
        }
        return q;
    }
    
    public static TypedQuery<PayEvt> PayEvt.findPayEvtsByUrlIdEquals(String urlId) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvt.entityManager();
        TypedQuery<PayEvt> q = em.createQuery("SELECT o FROM PayEvt AS o WHERE o.urlId = :urlId", PayEvt.class);
        q.setParameter("urlId", urlId);
        return q;
    }
    
    public static TypedQuery<PayEvt> PayEvt.findPayEvtsByUrlIdEquals(String urlId, String sortFieldName, String sortOrder) {
        if (urlId == null || urlId.length() == 0) throw new IllegalArgumentException("The urlId argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE o.urlId = :urlId");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        q.setParameter("urlId", urlId);
        return q;
    }
    
    public static TypedQuery<PayEvt> PayEvt.findPayEvtsByViewerLogins(List<RespLogin> viewerLogins) {
        if (viewerLogins == null) throw new IllegalArgumentException("The viewerLogins argument is required");
        EntityManager em = PayEvt.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PayEvt AS o WHERE");
        for (int i = 0; i < viewerLogins.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :viewerLogins_item").append(i).append(" MEMBER OF o.viewerLogins");
        }
        TypedQuery<PayEvt> q = em.createQuery(queryBuilder.toString(), PayEvt.class);
        int viewerLoginsIndex = 0;
        for (RespLogin _resplogin: viewerLogins) {
            q.setParameter("viewerLogins_item" + viewerLoginsIndex++, _resplogin);
        }
        return q;
    }
    
}
