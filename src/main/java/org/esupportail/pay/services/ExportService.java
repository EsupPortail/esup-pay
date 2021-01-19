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
package org.esupportail.pay.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.esupportail.pay.dao.ExportRemiseDaoService;
import org.esupportail.pay.dao.ExportTransactionDaoService;
import org.esupportail.pay.domain.ExportRemise;
import org.esupportail.pay.domain.ExportTransaction;
import org.esupportail.pay.domain.ExportTransaction.TypeTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// 2020-09-28 11:54:20
	SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Resource
	ExportTransactionDaoService exportTransactionDaoService;
	
	@Resource
	ExportRemiseDaoService exportRemiseDaoService;
	
	/*
	Num. transaction	
	Numéro remise banque	
	Date & Heure	
	Référence commande	
	Montant	
	Devise	
	Type de transaction	
	Statut de la transaction	
	Statut acquéreur	
	Moyen de paiement	
	Pays IP	
	Pays Porteur	
	Email porteur	
	Magasin	
	Garantie 3DS	
	Num. appel	
	Code banque	
	Motif refus	
	Num. Contrat	
	Rang	
	Type d'appel	
	Statut 3DS	
	Date & Heure de la remise	
	...
	 */
	public void consumeExportTransactionCsvLine(String line) throws IOException, ParseException {
		String[] fields = line.split(";");
		String numTransaction = fields[0];
		String numRemise = fields[1];
		String dateTransactionAsString = fields[2];
		String reference = fields[3];
		String montantAsString = fields[4];
		String typeTransaction = fields[6];
		String statut = fields[7];
		String email = fields[12];
		String dateRemiseAsString = fields[22];
		String numContrat = fields[18];

		if(!numContrat.isEmpty() && !dateRemiseAsString.isEmpty() && !numRemise.isEmpty()) {
			Date dateTransaction = csvDateFormat.parse(dateTransactionAsString);
			Date dateRemise = csvDateFormat.parse(dateRemiseAsString);
			BigDecimal montantBG = new BigDecimal(montantAsString);
			montantBG = montantBG.multiply( new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
			montantBG = montantBG.setScale(0, RoundingMode.HALF_UP);
			Long montant = montantBG.longValue();
	
			ExportTransaction exportTransaction = new ExportTransaction();	
			
			if(exportTransactionDaoService.countFindExportTransactionsByNumTransactionEqualsAndNumContratEquals(numTransaction, numContrat)>0) {
				exportTransaction = exportTransactionDaoService.findExportTransactionsByNumTransactionEqualsAndNumContratEquals(numTransaction, numContrat).getSingleResult();
			}
	
			exportTransaction.setNumRemise(numRemise);
			exportTransaction.setEmail(email);
			exportTransaction.setMontant(montant);
			exportTransaction.setNumTransaction(numTransaction);
			exportTransaction.setReference(reference);
			exportTransaction.setStatut(statut);
			exportTransaction.setTransactionDate(dateTransaction);
			exportTransaction.setDateRemise(dateRemise);
			exportTransaction.setNumContrat(numContrat);
			if("Débit".equals(typeTransaction)) {
				exportTransaction.setTypeTransaction(TypeTransaction.DEBIT);
			} else if("Remboursement".equals(typeTransaction)) {
				exportTransaction.setTypeTransaction(TypeTransaction.REMBOURSEMENT);
			} else if("Crédit".equals(typeTransaction)) {
				exportTransaction.setTypeTransaction(TypeTransaction.CREDIT);
			} else if("Annulation".equals(typeTransaction)) {
				exportTransaction.setTypeTransaction(TypeTransaction.ANNULATION);
			} 
			if(exportTransaction.getId()==null) {
				exportTransactionDaoService.persist(exportTransaction);
			}
		}
	}

	/*
		Numéro remise banque	
		Date & Heure	
		Num. contrat	
		Rang	
		Statut remise	
		Nb crédits	
		Montant crédits	
		Nb débits	
		Montant débits	
		Nb transactions	
		Montant total	
		Devise	
		Moyen de paiement	
		Magasin
	 */
	public void consumeExportRemiseCsvLine(String line) throws IOException, ParseException {
		String[] fields = line.split(";");
		String numRemise = fields[0];
		String dateRemiseAsString = fields[1];
		String numContrat = fields[2];
		String montantAsString = fields[10];
		String nbTransactions = fields[9];

		if(!numContrat.isEmpty() && !dateRemiseAsString.isEmpty() && !montantAsString.isEmpty()) {
			Date dateRemise = csvDateFormat.parse(dateRemiseAsString);
			BigDecimal montantBG = new BigDecimal(montantAsString);
			montantBG = montantBG.multiply( new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
			montantBG = montantBG.setScale(0, RoundingMode.HALF_UP);
			Long montant = montantBG.longValue();
	
			ExportRemise exportRemise = new ExportRemise();
			
			if(exportRemiseDaoService.countFindExportRemisesByNumRemiseEqualsAndNumContratEquals(numRemise, numContrat)>0) {
				exportRemise = exportRemiseDaoService.findExportRemisesByNumRemiseEqualsAndNumContratEquals(numRemise, numContrat).getSingleResult();
			}
	
			exportRemise.setNumRemise(numRemise);
			exportRemise.setNumContrat(numContrat);
			exportRemise.setMontant(montant);
			exportRemise.setDateRemise(dateRemise);
			exportRemise.setNbTransactions(Long.valueOf(nbTransactions));
			if(exportRemise.getId()==null) {
				exportRemiseDaoService.persist(exportRemise);
			}
		}
	}

	
	//@Scheduled(fixedDelay = 3600000, initialDelay = 1000)
	public synchronized void consumeCsvFolder() throws IOException, ParseException {
		// TODO : chemin en paramètre
		File actual = new File("/opt/paybox-exports/compte-colloque");
		for(File f : actual.listFiles()) {
			log.info(String.format("%s listé", f.getName()));
			Boolean importIsOK = false;
			if(f.getName().matches("Export_transactions_.*\\.csv")) {
				log.info(String.format("CSV de Transaction %s listé dans le répertoire ... début de l'importation", f.getName()));
				consumeExportTransactionCsvFile(new FileInputStream(f));
				importIsOK = true;
			} else if(f.getName().matches("Export_remises_.*\\.csv")) {
				log.info(String.format("CSV de Remise %s listé dans le répertoire ... début de l'importation", f.getName()));
				consumeExportRemiseCsvFile(new FileInputStream(f));
				importIsOK = true;
			}
			if(importIsOK) {
				log.info(String.format("%s importé en base", f.getName()));
				f.delete();
				log.info(String.format("%s supprimé", f.getName()));
			}
		}
	}

	public void consumeExportTransactionCsvFile(InputStream input) throws FileNotFoundException, IOException, ParseException {
		BufferedReader in = new BufferedReader(new InputStreamReader(input, "iso-8859-1"));
		String line;
		in.readLine(); // ignore header line;
		while ((line = in.readLine()) != null) {
			this.consumeExportTransactionCsvLine(line);
		}
	}
	
	public void consumeExportRemiseCsvFile(InputStream input) throws FileNotFoundException, IOException, ParseException {
			BufferedReader in = new BufferedReader(new InputStreamReader(input, "iso-8859-1"));
			String line;
			in.readLine(); // ignore header line;
			while ((line = in.readLine()) != null) {
				this.consumeExportRemiseCsvLine(line);
			}
		}
	
}


