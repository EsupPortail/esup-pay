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
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Label {
    
	public enum LOCALE_IDS {fr, en}

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_evt_seq_gen")
	@SequenceGenerator(name = "pay_evt_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@MapKey(name = "idLocale")
    @JoinTable(name = "label_label_locales",
	    joinColumns = @JoinColumn( name = "label" ),
	    inverseJoinColumns = @JoinColumn( name = "label_locales" ) )
	Map<String, LabelLocale> labelLocales;
	
	public Label() {
		super();
		labelLocales = new HashMap<String, LabelLocale>();
		labelLocales.put(LOCALE_IDS.en.toString(), new LabelLocale(LOCALE_IDS.en.toString(), ""));
		labelLocales.put(LOCALE_IDS.fr.toString(), new LabelLocale(LOCALE_IDS.fr.toString(), ""));
	}

	public String getTranslation(LOCALE_IDS localeId) {
		return this.getLabelLocales().get(localeId.toString()).getTranslation();
	}

	public String getTranslation(String localeId) {
		return this.getTranslation(localeId.equals("fr") ? LOCALE_IDS.fr : LOCALE_IDS.en);
	}
}