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
package org.esupportail.pay.repository;

import java.util.List;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayTransactionLogRepository extends JpaRepository<PayTransactionLog, Long>, JpaSpecificationExecutor<PayTransactionLog> {
    List<PayTransactionLog> findByPayEvtMontant(PayEvtMontant montant);
    boolean existsByPayEvtMontant(PayEvtMontant montant);
    List<PayTransactionLog> findByIdtrans(String idtrans);
    PayTransactionLog findFirstByReference(String reference);
    Page<PayTransactionLog> findByIdAbo(String idAbo, Pageable pageable);

    @Query("select log from PayTransactionLog log where log.transactionDate < :oldDate and log.mail <> 'archived'")
    List<PayTransactionLog> findOlderThan(@Param("oldDate") java.time.LocalDateTime oldDate);

    @Query(value = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year ORDER BY year DESC", nativeQuery = true)
    List<Object[]> findNbTransactionByYear();

    @Query(value = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year ORDER BY year DESC", nativeQuery = true)
    List<Object[]> findMontantByYear();

    @Query(value = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, COUNT(TO_CHAR(transaction_date, 'YYYY')) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year, month ORDER BY year, month DESC", nativeQuery = true)
    List<Object[]> findNbTransactionByMonth();

    @Query(value = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year, CAST(date_part('month',transaction_date) AS integer) as month, SUM(cast(montant AS INTEGER)/100) FROM pay_transaction_log WHERE erreur = '00000' GROUP BY year, month ORDER BY year, month DESC", nativeQuery = true)
    List<Object[]> findMontantByMonth();

    @Query(value = "SELECT CAST(date_part('year',transaction_date) AS integer) AS year FROM pay_transaction_log GROUP BY year ORDER BY year DESC", nativeQuery = true)
    List<String> findDistinctYears();

}
