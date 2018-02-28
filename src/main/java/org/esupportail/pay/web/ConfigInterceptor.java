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
package org.esupportail.pay.web;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class ConfigInterceptor extends HandlerInterceptorAdapter {
	
	final static SortedMap<String, String> subTitles = new TreeMap<String, String>() {{
	    put("/admin/evts", "Liste des évènements Paybox");
	    put("/admin/fees", "Liste des transactions Paybox");
	    put("/admin/evtmnts", "Montant transaction Paybox");
	    put("/admin/stats", "Statistiques Paybox");
	}};
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		
		// we want to add usual model in modelAndView only when needed, ie with
		// direct html view :
		// not for download response (for example) because we don't need it
		// not for redirect view because we don't need it and we don't want that
		// they appears in the url

		if (modelAndView != null && modelAndView.hasView()) {

			boolean isViewObject = modelAndView.getView() == null;

			boolean isRedirectView = !isViewObject && modelAndView.getView() instanceof RedirectView;

			boolean viewNameStartsWithRedirect = isViewObject && modelAndView.getViewName().startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX);

			if (!isRedirectView && !viewNameStartsWithRedirect) {

				String path = request.getServletPath();
				String subTitle = subTitles.get(path);
				String activeMenu = path.replaceAll("/", "");

				if (subTitle == null) {
					List<String> keys = new Vector<String>(subTitles.keySet());
					Collections.reverse(keys);
					for (String key : keys) {
						if (path.startsWith(key)) {
							subTitle = subTitles.get(key);
							;
							activeMenu = key.replaceAll("/", "");
							break;
						}
					}
				}

				modelAndView.addObject("subTitle", subTitle);
				modelAndView.addObject("activeMenu", activeMenu);
			}
		} 
	}

}
