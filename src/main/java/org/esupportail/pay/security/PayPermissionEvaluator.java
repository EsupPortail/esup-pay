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
package org.esupportail.pay.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.esupportail.pay.dao.PayEvtDaoService;
import org.esupportail.pay.dao.PayEvtMontantDaoService;
import org.esupportail.pay.dao.PayTransactionLogDaoService;
import org.esupportail.pay.dao.RespLoginDaoService;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.RespLogin;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class PayPermissionEvaluator implements PermissionEvaluator {

	
	@Resource
	PayEvtDaoService payEvtDaoService;

	@Resource
	PayEvtMontantDaoService payEvtMontantDaoService;
	
	@Resource
	RespLoginDaoService respLoginDaoService;
	
	@Resource
	PayTransactionLogDaoService payTransactionLogDaoService;
	
	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
				
		if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
			return true;

		String permissionKey = (String) permission;

		if(auth == null || auth.getName() == null || "".equals(auth.getName()))
			return false;

		if(!(targetDomainObject instanceof PayEvt || targetDomainObject instanceof PayEvtMontant || targetDomainObject instanceof Long))
			return false;

		if ("view-txlog".equals(permissionKey)) {
		    if(targetDomainObject instanceof Long) {
		        PayTransactionLog txLog = payTransactionLogDaoService.findPayTransactionLog((Long)targetDomainObject);
		        if (txLog != null) {
		            return hasPermission(auth, txLog.getPayEvtMontant().getEvt().getId(), "view");
		        }
		    }
		    return false;
		}

		if ("manage-montant".equals(permissionKey)) {
			if(targetDomainObject instanceof Long) {
				PayEvtMontant payEvtMontant = payEvtMontantDaoService.findPayEvtMontant((Long)targetDomainObject);
				if (payEvtMontant != null) {
					return hasPermission(auth, payEvtMontant.getEvt().getId(), "manage");
				}
			}
			return false;
		}

		Long evtId = null;
		if(targetDomainObject instanceof Long) {
			evtId = (Long)targetDomainObject;
		}
		if(targetDomainObject instanceof PayEvt) {
			PayEvt transientEvt = (PayEvt) targetDomainObject;
			evtId = transientEvt.getId();
		}
		if(targetDomainObject instanceof PayEvtMontant) {
			PayEvt transientEvt = ((PayEvtMontant) targetDomainObject).getEvt();
			evtId = transientEvt.getId();				
		}
		return hasPermissionOnEvt(auth, evtId, permissionKey);
	}

	private boolean hasPermissionOnEvt(Authentication auth, Long evtId, String permissionKey) {
		PayEvt evt = payEvtDaoService.findPayEvt(evtId);
		RespLogin respLogin = respLoginDaoService.findOrCreateRespLogin(auth.getName());
		List<RespLogin> respLoginList = Arrays.asList(new RespLogin[] {respLogin});

		boolean accessAuth = false;

		if("view".equals(permissionKey) || "manage".equals(permissionKey)) {
			accessAuth = payEvtDaoService.findPayEvtsByRespLogins(respLoginList).getResultList().contains(evt);
		}
		
		if("view".equals(permissionKey)) {
			accessAuth = accessAuth 
                || auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ALL_VIEWER"))
                || payEvtDaoService.findPayEvtsByViewerLogins(respLoginList).getResultList().contains(evt);
		}

		return accessAuth;
	}

	@Override
	public boolean hasPermission(Authentication arg0, Serializable arg1,
			String arg2, Object arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
