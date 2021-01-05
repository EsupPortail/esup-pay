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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.esupportail.pay.domain.ExportTransaction.TypeTransaction;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class Ventilation {

	private Date date;

	private ExportRemise remise;

	Map<PayEvt, List<ExportTransaction>> transactions = new TreeMap<PayEvt, List<ExportTransaction>>(
			new Comparator<PayEvt>() {
				@Override
				public int compare(PayEvt evt1, PayEvt evt2) {
					return evt1.getTitleFr().compareTo(evt2.getTitleFr());
				}
			}
	); 

	Map<PayEvt, Long> montantsEvts = new TreeMap<PayEvt, Long>(
			new Comparator<PayEvt>() {
				@Override
				public int compare(PayEvt evt1, PayEvt evt2) {
					return evt1.getTitleFr().compareTo(evt2.getTitleFr());
				}
			}
	); 

	long totalMontantTransactions;

	public void setTransactions(Map<PayEvt, List<ExportTransaction>> transactions) {
		this.transactions = transactions;
		updateMontants();
	}

	public void updateMontants() {
		totalMontantTransactions = 0;
		for(PayEvt evt : transactions.keySet()) {
			long s = 0;
			for(ExportTransaction t: transactions.get(evt)) {
				if(TypeTransaction.DEBIT.equals(t.getTypeTransaction())) {
					s += t.getMontant();
				} else if(TypeTransaction.REMBOURSEMENT.equals(t.getTypeTransaction()) || TypeTransaction.CREDIT.equals(t.getTypeTransaction())) {
					s -= t.getMontant();
				} else if(TypeTransaction.ANNULATION.equals(t.getTypeTransaction())) {
					// operation neutre
				}
			}
			montantsEvts.put(evt, s);
			totalMontantTransactions += s;
		}
	}

	public long getNbTransactions() {
		long s = 0;
		for(PayEvt evt : transactions.keySet()) {
			s += transactions.get(evt).stream().filter(t->!TypeTransaction.ANNULATION.equals(t.getTypeTransaction())).count();
		}
		return s;
	}
	
	public long getNbTransactionsCreditRemboursement() {
		long s = 0;
		for(PayEvt evt : transactions.keySet()) {
			s += transactions.get(evt).stream().filter(t->TypeTransaction.CREDIT.equals(t.getTypeTransaction()) || TypeTransaction.REMBOURSEMENT.equals(t.getTypeTransaction())).count();
		}
		return s;
	}
	

	public boolean isConsistentMontant() {
		return remise.getMontant().equals(this.totalMontantTransactions) && remise.getNbTransactions().equals(this.getNbTransactions());
	}
}
