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

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
	
	private final Logger log = Logger.getLogger(getClass());
	
	@RequestMapping("/admin/su")
	public String su(Model uiModel) {		
		uiModel.addAttribute("active", "su");
		return "admin/su";
	}
	
	@RequestMapping("/admin")
	public String index(Model uiModel) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
				auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) ||
				auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VIEWER"))) {
			return "redirect:/admin/evts?page=0";
		} else if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_VENTILATION"))) {
			return "redirect:/admin/ventilations";
		} else if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAT"))) {
			return "redirect:/admin/stats";
		}
		return "redirect:/admin/evts?page=0";
	}
}
