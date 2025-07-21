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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.esupportail.pay.services.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/admin/stats")
@Controller
@Transactional
public class StatsController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	StatsService statsService;
	
	@RequestMapping
    public String getStats(Model uiModel) {
		uiModel.addAttribute("years", statsService.getDistinctYears());
    	return "admin/stats/index";
    }

	@RequestMapping(value="/montants", headers = "Accept=application/json; charset=utf-8")
	@ResponseBody 
	public String getmontants(@RequestParam(name="year", required = false) String year) {
        String json = "Aucune statistique à récupérer";
		try {
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(statsService.getStats(year));
			
		} catch (Exception e) {
			log.warn("Impossible de récupérer les statistiques", e);
		}
		
    	return json;

	}
}