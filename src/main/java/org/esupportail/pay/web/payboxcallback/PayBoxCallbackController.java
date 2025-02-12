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
package org.esupportail.pay.web.payboxcallback;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.esupportail.pay.services.PayBoxServiceManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayBoxCallbackController {

    private final Logger log = Logger.getLogger(getClass());

	@Resource 
	PayBoxServiceManager payBoxServiceManager;
	
    @RequestMapping("/payboxcallback")
    @ResponseBody
    public ResponseEntity<java.lang.String> index(@RequestParam String montant, @RequestParam String reference, @RequestParam(required = false) String auto, 
    		@RequestParam String erreur, @RequestParam String idtrans, 
    		@RequestParam(required = false) String securevers, @RequestParam(required = false) String softdecline, 
    		@RequestParam(required = false) String secureauth, @RequestParam(required = false) String securegarantie,
    		@RequestParam String signature, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String queryString = request.getQueryString();
        synchronized (idtrans.intern()) {
             if (payBoxServiceManager.payboxCallback(montant, reference, auto, erreur, idtrans, securevers, softdecline, secureauth, securegarantie, signature, queryString)) {
                 HttpHeaders headers = new HttpHeaders();
                 headers.add("Content-Type", "text/html; charset=utf-8");
                 return new ResponseEntity<String>("", headers, HttpStatus.OK);
             } else {
                 HttpHeaders headers = new HttpHeaders();
                 headers.add("Content-Type", "text/html; charset=utf-8");
                 return new ResponseEntity<String>("", headers, HttpStatus.INTERNAL_SERVER_ERROR);
             }
        }
    }
    
}