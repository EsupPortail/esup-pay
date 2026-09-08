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

import java.util.LinkedHashMap;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente le rejeu (retry) d'un paiement Paybox vers le site secondaire (tpeweb <-> tpeweb1),
 * suite à une erreur "00001" (échec de connexion au centre d'autorisation) ou "00003" (erreur Paybox)
 * renvoyée à l'utilisateur.
 *
 * Le payload (paramètres + PBX_HMAC) est strictement identique à celui de la 1ère tentative :
 * seule l'URL cible du POST (actionUrl) change.
 */
@Getter
@Setter
public class PayBoxRetryForm {

	private String actionUrl;

	private LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();

}
