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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;

import org.esupportail.pay.domain.LdapResult;
import org.esupportail.pay.domain.RespLogin;
import org.esupportail.pay.services.LdapService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONSerializer;

@Controller
@PropertySource("classpath:META-INF/spring/esup-pay.properties")
public class LdapPeopleController {

	@Resource
	LdapService ldapService;

    @RequestMapping(value="/admin/searchLoginsJson", headers = "Accept=application/json", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> searchLoginsJson(Model uiModel, @RequestParam("loginPrefix") String loginPrefix) {
    			
    	HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");

        List<LdapResult> ldapResults = ldapService.search(loginPrefix + "*");

        if(ldapResults != null) {
            String loginsJson = new JSONSerializer().serialize(ldapResults);
            return new ResponseEntity<String>(loginsJson, headers, HttpStatus.OK);
        } else {
            LdapResult ldapResult = new LdapResult();
            ldapResult.setUid(loginPrefix);
            ldapResult.setDisplayName(loginPrefix);
            ldapResult.setEmail(loginPrefix);
            String loginsJson = new JSONSerializer().serialize(List.of(ldapResult));
            return new ResponseEntity<String>(loginsJson, headers, HttpStatus.OK);
        }
    }    
}