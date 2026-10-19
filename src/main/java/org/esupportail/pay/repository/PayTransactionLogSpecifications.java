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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.esupportail.pay.domain.Label;
import org.esupportail.pay.domain.LabelLocale;
import org.esupportail.pay.domain.PayEvt;
import org.esupportail.pay.domain.PayEvtMontant;
import org.esupportail.pay.domain.PayTransactionLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

public final class PayTransactionLogSpecifications {
    private PayTransactionLogSpecifications() {}

    public static Specification<PayTransactionLog> forEvent(PayEvt event, String idAbo,
                                                           PayTransactionLogFilter filter) {
        Assert.notNull(event, "The event argument is required");
        return matching(filter).and((root, query, cb) -> {
            Predicate scope = cb.equal(root.get("payEvtMontant").get("evt"), event);
            return idAbo == null ? scope : cb.and(scope, cb.equal(root.get("idAbo"), idAbo));
        });
    }

    public static Specification<PayTransactionLog> matching(PayTransactionLogFilter filter) {
        Assert.notNull(filter, "The filter argument is required");
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getSuccessfulOnly() != null) {
                predicates.add(filter.getSuccessfulOnly()
                    ? cb.equal(root.get("erreur"), "00000")
                    : cb.notEqual(root.get("erreur"), "00000"));
            }
            contains(predicates, cb, root.get("field1"), filter.getField1());
            contains(predicates, cb, root.get("field2"), filter.getField2());
            contains(predicates, cb, root.get("mail"), filter.getMail());
            if (filter.getHasAbo() != null) {
                Path<String> idAbo = root.get("idAbo");
                predicates.add(filter.getHasAbo()
                    ? cb.and(cb.isNotNull(idAbo), cb.notEqual(idAbo, "0"))
                    : cb.or(cb.isNull(idAbo), cb.equal(idAbo, "0")));
            }
            if (hasText(filter.getEvtMnt())) {
                Subquery<Long> titleQuery = query.subquery(Long.class);
                Root<PayEvtMontant> montant = titleQuery.from(PayEvtMontant.class);
                MapJoin<Label, String, LabelLocale> titles = montant.join("title").joinMap("labelLocales");
                titleQuery.select(montant.get("id")).where(
                    cb.equal(montant, root.get("payEvtMontant")),
                    cb.equal(titles.key(), "fr"),
                    cb.like(cb.lower(titles.get("translation")), pattern(filter.getEvtMnt())));
                predicates.add(cb.exists(titleQuery));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void contains(List<Predicate> predicates, CriteriaBuilder cb,
                                 Expression<String> path, String value) {
        if (hasText(value)) predicates.add(cb.like(cb.lower(path), pattern(value)));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isEmpty();
    }

    private static String pattern(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }
}
