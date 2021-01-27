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

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayBoxForm {

	private String actionUrl;

	private String site;	

	private String rang;

	private String identifiant;

	private String total;

	private String devise;
	
	private String commande;
	
	private String clientEmail;
	
	private String retourVariables;
	
	private String hash;
	
	private String time;
	
	private String callbackUrl;
	
	private String forwardEffectueUrl;
	
	private String forwardRefuseUrl;
	
	private String forwardAnnuleUrl;
	
	private String hmac;
	
	SortedMap<String, String> optionalAddedParams = new TreeMap<String, String>();
	
	public SortedMap<String, String> getOrderedParams() {
		SortedMap<String, String> params = new TreeMap<String, String>();
		params.put("PBX_SITE", site);
		params.put("PBX_RANG", rang);
		params.put("PBX_IDENTIFIANT", identifiant);
		params.put("PBX_TOTAL", total);
		params.put("PBX_DEVISE", devise);
		params.put("PBX_CMD", commande);
		params.put("PBX_PORTEUR", clientEmail);
		params.put("PBX_RETOUR", retourVariables);
		params.put("PBX_HASH", hash);
		params.put("PBX_TIME", time);
		params.put("PBX_REPONDRE_A", callbackUrl);
		params.put("PBX_EFFECTUE", forwardEffectueUrl);
		params.put("PBX_REFUSE", forwardRefuseUrl);
		params.put("PBX_ANNULE", forwardAnnuleUrl);
		
		params.putAll(optionalAddedParams);
		// params.put("PBX_HMAC", hmac);
		
		return params;
	}
	
	public String getParamsAsString() {
		String paramsAsString = "";
		 SortedMap<String, String> params = getOrderedParams();
		 for(String key : params.keySet()) {
			 paramsAsString = paramsAsString + key + "=" + params.get(key) + "&";
		 }
		 paramsAsString = paramsAsString.subSequence(0, paramsAsString.length()-1).toString();
		 try {
			 // paramsAsString = URLEncoder.encode(paramsAsString, "utf8");
			 System.out.println(paramsAsString);
			 return paramsAsString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getMontant() {
		return Double.valueOf((Double.valueOf(total)/100.0)).toString();
	}
	
    public String getMontantDisplay() {
    	return String.format("%,.2f€", Double.valueOf(total)/100.0);
    }

	public void setOptionalAddedParams(String optionalAddedParams2) {
		if(optionalAddedParams2!=null && !optionalAddedParams2.isEmpty()) {
			List<String> params = Arrays.asList(optionalAddedParams2.split("&"));
			for(String param : params) {
				List<String> paramNameAndValue = Arrays.asList(param.split("="));
				String paramName = paramNameAndValue.get(0);
				String paramValue = paramNameAndValue.get(1);
				this.optionalAddedParams.put(paramName, paramValue);
			}
			
		}
	}
	
}

