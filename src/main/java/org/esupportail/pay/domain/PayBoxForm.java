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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

@Getter
@Setter
public class PayBoxForm {

	private final static Logger log = Logger.getLogger(PayBoxForm.class);

	private static String PBX_SHOPPINGCART_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<shoppingcart>"
			+ "<total>"
			+ "<totalQuantity>%s</totalQuantity>"
			+ "</total>"
			+ "</shoppingcart>";
	
	private static String PBX_BILLING_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<Billing>"
			+ "<Address>"
			+ "<FirstName>%s</FirstName>"
			+ "<LastName>%s</LastName>"
			+ "<Address1>%s</Address1>"
			+ "<ZipCode>%s</ZipCode>"
			+ "<City>%s</City>"
			+ "<CountryCode>%s</CountryCode>"
			+ "</Address>"
			+ "</Billing>";

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
	
	private Integer shoppingcartTotalQuantity;
	
	private String billingFirstname;
	
	private String billingLastname;
	
	private String billingAddress1;
	
	private String billingZipCode;
	
	private String billingCity;
	
	private String billingCountryCode;
	
	private SortedMap<String, String> optionalAddedParams = new TreeMap<String, String>();	
	
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
		
		if(shoppingcartTotalQuantity != null) {
			String pbxShoppingcardXml = String.format(PBX_SHOPPINGCART_XML, shoppingcartTotalQuantity);
			params.put("PBX_SHOPPINGCART", pbxShoppingcardXml);
		}
		
		if(billingFirstname != null) {
			String pbxBillingXml = String.format(PBX_BILLING_XML,
					billingFirstname,
					billingLastname,
					billingAddress1,
					billingZipCode,
					billingCity,
					billingCountryCode);
			params.put("PBX_BILLING", pbxBillingXml);
		}
		
		params.putAll(optionalAddedParams);
		
		params.put("PBX_HASH", "SHA512");
		
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
				if(paramNameAndValue.size()<2) {
					log.warn(String.format("OptionalAddedParam %s not well formed : it doesn't contain '='", param));
				} else {
					String paramName = paramNameAndValue.get(0);
					String paramValue = paramNameAndValue.get(1);
					this.optionalAddedParams.put(paramName, paramValue);
				}
			}
			
		}
	}

	private String removeNotUsualCharacters(String s) {
		String cleanupString =  s;
		if(cleanupString != null) {
			cleanupString = StringUtils.stripAccents(cleanupString);
			cleanupString = cleanupString.replaceAll("[^a-zA-Z0-9\\s]", "");
		}
		return cleanupString;
	}
	
	public void setBillingFirstname(String billingFirstname) {
		this.billingFirstname = removeNotUsualCharacters(billingFirstname);
	}

	public void setBillingLastname(String billingLastname) {
		this.billingLastname = removeNotUsualCharacters(billingLastname);
	}

	public void setBillingAddress1(String billingAddress1) {
		this.billingAddress1 = removeNotUsualCharacters(billingAddress1);
	}

	public void setBillingZipCode(String billingZipCode) {
		this.billingZipCode = removeNotUsualCharacters(billingZipCode);
	}

	public void setBillingCity(String billingCity) {
		this.billingCity = removeNotUsualCharacters(billingCity);
	}

	public void setBillingCountryCode(String billingCountryCode) {
		this.billingCountryCode = removeNotUsualCharacters(billingCountryCode);
	}
	
}

