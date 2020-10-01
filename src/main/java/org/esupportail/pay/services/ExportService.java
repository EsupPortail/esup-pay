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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.esupportail.pay.domain.ExportRemise;
import org.esupportail.pay.domain.ExportTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExportService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// 2020-09-28 11:54:20
	SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
		String dateTransactionAsString = fields[2];
		String reference = fields[3];
		String montantAsString = fields[4];
		String statut = fields[7];
		String email = fields[12];

		Date dateTransaction = csvDateFormat.parse(dateTransactionAsString);
		Number montantNumber = NumberFormat.getInstance().parse(montantAsString);
		Long montant = montantNumber.longValue()*100;

		ExportTransaction exportTransaction = new ExportTransaction();	
		
		if(ExportTransaction.countFindExportTransactionsByNumTransactionEquals(numTransaction)>0) {
			exportTransaction = ExportTransaction.findExportTransactionsByNumTransactionEquals(numTransaction).getSingleResult();
		}

		exportTransaction.setEmail(email);
		exportTransaction.setMontant(montant);
		exportTransaction.setNumTransaction(numTransaction);
		exportTransaction.setReference(reference);
		exportTransaction.setStatut(statut);
		exportTransaction.setTransactionDate(dateTransaction);
		if(exportTransaction.getId()==null) {
			exportTransaction.persist();
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
		String montantAsString = fields[8];
		String nbTransactions = fields[9];

		Date dateRemise = csvDateFormat.parse(dateRemiseAsString);
		Number montantNumber = NumberFormat.getInstance().parse(montantAsString);
		Long montant = montantNumber.longValue()*100;

		ExportRemise exportRemise = new ExportRemise();
		
		if(ExportRemise.countFindExportRemisesByNumRemiseEquals(numRemise)>0) {
			exportRemise = ExportRemise.findExportRemisesByNumRemiseEquals(numRemise).getSingleResult();
		}
		
		exportRemise.setNumRemise(numRemise);
		exportRemise.setMontant(montant);
		exportRemise.setTransactionDate(dateRemise);
		exportRemise.setNbTransactions(Long.valueOf(nbTransactions));
		if(exportRemise.getId()==null) {
			exportRemise.persist();
		}
	}

	
	@Scheduled(fixedDelay = 3600000, initialDelay = 1000)
	public synchronized void consumeCsvFolder() throws IOException, ParseException {
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


