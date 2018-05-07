package org.esupportail.pay.services;

import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.PayTransactionLog;
import org.esupportail.pay.domain.ScienceConfReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArchiveService {
	
	private final Logger log = Logger.getLogger(getClass());
	
	@Value("${archive.enabled:false}")
	private Boolean enabled;
	
	@Value("${archive.oldDays.emailFieldsMapReference:15}")
	private Long oldDays4emailFieldsMapReference;
	
	@Value("${archive.oldDays.transactionsLogs:1825}")
	private Long oldDays4transactionsLogs;

	@Scheduled(fixedDelay=3600000)
	public void removeOldTmpEmailFieldsMapReference() {		
		if(enabled) {
			log.debug("removeOldTmpEmailFieldsMapReference called");
			List<EmailFieldsMapReference> emailFieldsMapReferences2remove = EmailFieldsMapReference.findOldEmailFieldsMapReferences(oldDays4emailFieldsMapReference);
			for(EmailFieldsMapReference emailFieldsMapReference : emailFieldsMapReferences2remove) {
				TypedQuery<ScienceConfReference> q = ScienceConfReference.findScienceConfReferencesByEmailFieldsMapReference(emailFieldsMapReference);
				if(!q.getResultList().isEmpty()) {
					q.getSingleResult().remove();
				}
				emailFieldsMapReference.remove();
			}
			if(emailFieldsMapReferences2remove.size()>0) {
				log.info(emailFieldsMapReferences2remove.size() + " old emailFieldsMapReferences removed");
			}
		}
	}
	
	@Scheduled(fixedDelay=3600000)
	public void anonymiseOldTransactions() {	
		if(enabled) {
			log.debug("anonymiseOldTransactions called");
			List<PayTransactionLog> transactionLogs = PayTransactionLog.findOldPayTransactionLogs(oldDays4transactionsLogs);
			for(PayTransactionLog transactionLog : transactionLogs) {
				anonymise(transactionLog);
			}
			if(transactionLogs.size()>0) {
				log.info(transactionLogs.size() + " old transactionLogs anonymised");
			}
		}
	}

	private void anonymise(PayTransactionLog transactionLog) {
		transactionLog.setField1("archived");
		transactionLog.setField2("archived");
		transactionLog.setMail("archived");
		transactionLog.setReference("archived");
	}
	
}

