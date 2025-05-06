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
package org.esupportail.pay.dao;
import java.io.InputStream;
import java.sql.Blob;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.esupportail.pay.domain.BigFile;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

@Service
public class BigFileDaoService {

	@PersistenceContext
    transient EntityManager em;
	
	// Here we have to use directly Hibernate and no "just" JPA, because we want to use stream to read and write big files
	// @see http://stackoverflow.com/questions/10042766/jpa-analog-of-lobcreator-from-hibernate
    public void setBinaryFileStream(BigFile bigFile, InputStream inputStream, long length) {		
        Session session = (Session) em.getDelegate(); 
        LobHelper helper = session.getLobHelper(); 
        Blob binaryFile = helper.createBlob(inputStream, length);
        bigFile.setBinaryFile(binaryFile);
    }

	public void persist(BigFile bigFile) {
		em.persist(bigFile);
	}
	
}