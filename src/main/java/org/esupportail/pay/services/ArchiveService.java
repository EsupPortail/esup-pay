package org.esupportail.pay.services;

import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.esupportail.pay.domain.EmailFieldsMapReference;
import org.esupportail.pay.domain.ScienceConfReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArchiveService {
	
	private final Logger log = Logger.getLogger(getClass());
	
	@Scheduled(fixedDelay=3600000)
	public void removeOldTmpEmailFieldsMapReference() {		
		log.debug("removeOldTmpEmailFieldsMapReference called");
		List<EmailFieldsMapReference> emailFieldsMapReferences2remove = EmailFieldsMapReference.findOldEmailFieldsMapReferences();
		for(EmailFieldsMapReference emailFieldsMapReference : emailFieldsMapReferences2remove) {
			TypedQuery<ScienceConfReference> q = ScienceConfReference.findScienceConfReferencesByEmailFieldsMapReference(emailFieldsMapReference);
			if(!q.getResultList().isEmpty()) {
				q.getSingleResult().remove();
			}
			emailFieldsMapReference.remove();
		}
		if(emailFieldsMapReferences2remove.size()>0) {
			log.info(emailFieldsMapReferences2remove.size() + " old emailFieldsMapReference removed");
		}
	}
	
}
