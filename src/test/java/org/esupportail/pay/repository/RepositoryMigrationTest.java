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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.esupportail.pay.domain.*;
import org.esupportail.pay.services.PayEvtMontantService;
import org.esupportail.pay.services.RespLoginService;
import org.junit.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;
import static org.junit.Assert.*;

/** Boots every repository against an isolated database; never uses the application datasource. */
public class RepositoryMigrationTest {
    @ClassRule
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");

    private static AnnotationConfigApplicationContext context;
    private PayEvtRepository events;
    private PayEvtMontantRepository amounts;
    private PayTransactionLogRepository logs;

    /** Exercises Spring Data's Hibernate-specific native parameter binding path. */
    public interface NativeQueryProbe extends org.springframework.data.repository.Repository<PayTransactionLog, Long> {
        @org.springframework.data.jpa.repository.Query(value = "select cast(:value as varchar)", nativeQuery = true)
        String echo(@org.springframework.data.repository.query.Param("value") String value);
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = PayEvtRepository.class, considerNestedRepositories = true)
    @Import({PayEvtMontantService.class, RespLoginService.class,
        org.esupportail.pay.security.PayPermissionEvaluator.class, org.esupportail.pay.services.StatsService.class, org.esupportail.pay.services.PayBoxAbonnementService.class})
    static class Config {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("org.esupportail.pay.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            properties.setProperty("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
            factory.setJpaProperties(properties);
            factory.getJpaPropertyMap().put("jakarta.persistence.validation.factory",
                jakarta.validation.Validation.byDefaultProvider().configure()
                    .messageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator())
                    .buildValidatorFactory());
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    @BeforeClass
    public static void start() {
        context = new AnnotationConfigApplicationContext(Config.class);
    }

    @AfterClass
    public static void stop() {
        if (context != null) context.close();
    }

    @Before
    public void repositories() {
        events = context.getBean(PayEvtRepository.class);
        amounts = context.getBean(PayEvtMontantRepository.class);
        logs = context.getBean(PayTransactionLogRepository.class);
    }

    private void inTransaction(Runnable assertions) {
        new TransactionTemplate(context.getBean(PlatformTransactionManager.class)).execute(status -> {
            status.setRollbackOnly();
            assertions.run();
            return null;
        });
    }

    private PayEvt event(String title) {
        PayEvt event = new PayEvt();
        event.setMailSubject("subject");
        event.setPayboxServiceKey("test");
        event.setPayboxCommandPrefix("test");
        event.setRespLogins(List.of());
        event.setViewerLogins(List.of());
        event.getTitle().getLabelLocales().get("fr").setTranslation(title);
        return events.save(event);
    }

    private PayEvtMontant amount(PayEvt event, String title) {
        PayEvtMontant amount = new PayEvtMontant();
        amount.setEvt(event);
        amount.setUrlId("fee-" + title);
        amount.getTitle().getLabelLocales().get("fr").setTranslation(title);
        amount.getTitle().getLabelLocales().get("en").setTranslation("English " + title);
        return amounts.save(amount);
    }

    private PayTransactionLog log(PayEvtMontant amount, String error, String abo) {
        PayTransactionLog log = new PayTransactionLog();
        log.setPayEvtMontant(amount);
        log.setErreur(error);
        log.setIdAbo(abo);
        log.setMail("Alice@Example.org");
        log.setField1("Alpha");
        log.setField2("Beta");
        log.setMontant("1000");
        log.setTransactionDate(LocalDateTime.now());
        return logs.save(log);
    }

    @Test
    public void amountsArePagedSortedAndMerged() {
        inTransaction(() -> {
            PayEvt event = event("Conference");
            PayEvtMontant first = amount(event, "A");
            PayEvtMontant second = amount(event, "B");
            amount(event("Other"), "C");
            Page<PayEvtMontant> page = amounts.findByEvt(event, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));
            assertEquals(2, page.getTotalElements());
            assertEquals(List.of(second), page.getContent());
            assertEquals(List.of(first), amounts.findByEvtAndUrlId(event, first.getUrlId()));
            first.setUrlId("changed");
            amounts.saveAndFlush(first);
            assertEquals(List.of(first), amounts.findByEvtAndUrlId(event, "changed"));
        });
    }

    @Test
    public void filtersKeepTriStateBooleansAndEventScope() {
        inTransaction(() -> {
            PayEvt event = event("Conference");
            PayEvtMontant fee = amount(event, "Tarif Etudiant");
            PayTransactionLog success = log(fee, "00000", "subscription");
            log(fee, "00001", "0");
            log(fee, null, null);
            log(amount(event("Other"), "Tarif Etudiant"), "00000", "subscription");
            var filter = new PayTransactionLogFilter();
            assertEquals(3, logs.findAll(PayTransactionLogSpecifications.forEvent(event, null, filter)).size());
            filter.setSuccessfulOnly(true);
            filter.setHasAbo(true);
            filter.setMail("ALICE@");
            filter.setField1("LPH");
            filter.setField2("ETA");
            filter.setEvtMnt("ETUDIANT");
            var spec = PayTransactionLogSpecifications.forEvent(event, "subscription", filter);
            assertEquals(List.of(success), logs.findAll(spec));
            assertEquals(1, logs.findAll(spec, PageRequest.of(0, 1)).getTotalElements());
            assertEquals(List.of(success), logs.findBy(spec, q -> q.slice(PageRequest.of(0, 10))).getContent());
            filter.setSuccessfulOnly(false);
            filter.setHasAbo(false);
            assertEquals(1, logs.findAll(PayTransactionLogSpecifications.forEvent(event, null, filter)).size());
            filter.setSuccessfulOnly(null);
            assertEquals(2, logs.findAll(PayTransactionLogSpecifications.forEvent(event, null, filter)).size());
            filter.setEvtMnt("English");
            assertTrue(logs.findAll(PayTransactionLogSpecifications.forEvent(event, null, filter)).isEmpty());
        });
    }

    @Test
    public void subscriptionFinderHonorsPagination() {
        inTransaction(() -> {
            var amount = amount(event("Event"), "Fee");
            log(amount, "00000", "sub");
            var latest = log(amount, "00000", "sub");
            var page = logs.findByIdAbo("sub", PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));
            assertEquals(2, page.getTotalElements());
            assertEquals(List.of(latest), page.getContent());
            assertEquals(2, logs.findByIdAbo("sub", Pageable.unpaged()).getContent().size());
        });
    }

    @Test
    public void deletionPolicyChecksBothKindsOfReference() {
        inTransaction(() -> {
            var amount = amount(event("Event"), "Fee");
            var service = context.getBean(PayEvtMontantService.class);
            var references = context.getBean(EmailFieldsMapReferenceRepository.class);
            assertTrue(service.isDeletable(amount));
            var ref = new EmailFieldsMapReference();
            ref.setPayEvtMontant(amount);
            ref.setDateCreated(new Date());
            references.saveAndFlush(ref);
            assertFalse(service.isDeletable(amount));
            assertEquals(List.of(ref), references.findUnpaidAfter(amount, new Date(0)));
            ref.setIsPayed(true);
            references.flush();
            assertTrue(references.findUnpaidAfter(amount, new Date(0)).isEmpty());
            references.delete(ref);
            references.flush();
            assertTrue(service.isDeletable(amount));
            log(amount, "00000", null);
            assertFalse(service.isDeletable(amount));
        });
    }

    @Test
    public void eventVisibilityAndTranslatedSortingRemainScoped() {
        inTransaction(() -> {
            var loginService = context.getBean(RespLoginService.class);
            var manager = loginService.findOrCreateRespLogin("manager");
            var viewer = loginService.findOrCreateRespLogin("viewer");
            assertEquals(manager.getId(), loginService.findOrCreateRespLogin("manager").getId());
            var a = event("Alpha");
            a.setRespLogins(List.of(manager));
            a.setViewerLogins(List.of(viewer));
            var b = event("Beta");
            b.setViewerLogins(List.of(manager));
            event("Hidden");
            events.flush();
            var page = events.searchVisible(List.of(manager), null, PageRequest.of(0, 1, Sort.by("title")));
            assertEquals(2, page.getTotalElements());
            assertEquals(List.of(a), page.getContent());
            assertEquals(List.of(b), events.searchVisible(List.of(manager), "ETA", Pageable.unpaged()).getContent());
            assertEquals(List.of(a), events.findPayEvtsByRespLogins(List.of(manager)));
            assertEquals(List.of(a), events.findPayEvtsByViewerLogins(List.of(viewer)));
            assertTrue(events.searchVisible(List.of(), null, Pageable.unpaged()).isEmpty());
            assertTrue(events.findPayEvtsByRespLogins(List.of()).isEmpty());
            assertEquals(List.of(a), events.searchVisible(List.of(manager, viewer), null, Pageable.unpaged()).getContent());
            assertEquals(3, events.search(null, Pageable.unpaged()).getTotalElements());
        });
    }

    @Test
    public void detachedUpdatesAndCascadedDeletesUseRepositorySemantics() {
        inTransaction(() -> {
            var amount = amount(event("Event"), "Fee");
            amounts.flush();
            Long id = amount.getId();
            Integer version = amount.getVersion();
            var em = SharedEntityManagerCreator.createSharedEntityManager(context.getBean(EntityManagerFactory.class));
            em.clear();
            amount.setUrlId("updated");
            var managed = amounts.saveAndFlush(amount);
            assertEquals(id, managed.getId());
            assertTrue(managed.getVersion() > version);
            assertEquals("updated", amounts.findById(id).orElseThrow().getUrlId());
            amounts.delete(managed);
            amounts.flush();
            assertTrue(amounts.findById(id).isEmpty());
        });
    }

    @Test
    public void importAndReferenceQueriesFindExistingRows() {
        inTransaction(() -> {
            var remises = context.getBean(ExportRemiseRepository.class);
            var transactions = context.getBean(ExportTransactionRepository.class);
            var remise = new ExportRemise();
            remise.setNumRemise("R1");
            remise.setNumContrat("C1");
            remise.setDateRemise(LocalDateTime.now());
            remises.save(remise);
            var transaction = new ExportTransaction();
            transaction.setNumTransaction("T1");
            transaction.setNumRemise("R1");
            transaction.setNumContrat("C1");
            transaction.setStatut("Acceptée");
            transactions.save(transaction);
            assertEquals(1, remises.countByNumRemiseAndNumContrat("R1", "C1"));
            assertEquals(remise, remises.findByNumRemiseAndNumContrat("R1", "C1"));
            assertEquals(List.of(remise), remises.findByDateRemiseBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), Sort.by("dateRemise")));
            assertEquals(1, transactions.countByNumTransactionAndNumContrat("T1", "C1"));
            assertEquals(transaction, transactions.findByNumTransactionAndNumContrat("T1", "C1"));
            assertEquals(List.of(transaction), transactions.findByNumRemiseAndStatutAndNumContrat("R1", "Acceptée", "C1"));
            var references = context.getBean(EmailFieldsMapReferenceRepository.class);
            var ref = new EmailFieldsMapReference();
            ref.setPayEvtMontant(amount(event("Event"), "Fee"));
            ref.setReference("reference");
            ref.setDateCreated(new Date(0));
            references.save(ref);
            var science = new ScienceConfReference();
            science.setEmailFieldsMapReference(ref);
            var sciences = context.getBean(ScienceConfReferenceRepository.class);
            sciences.save(science);
            assertEquals(science, sciences.findByEmailFieldsMapReference(ref));
            assertEquals(List.of(ref), references.findByReference("reference"));
            assertTrue(references.findOlderThan(new Date()).isEmpty());
        });
    }

    @Test
    public void nativeQueriesBindNonNullAndNullParameters() {
        inTransaction(() -> {
            var probe = context.getBean(NativeQueryProbe.class);
            assertEquals("native-query", probe.echo("native-query"));
            assertNull(probe.echo(null));
        });
    }

    @Test
    public void requestBindingKeepsExistingParameterNamesAndDefaults() {
        var filter = PayTransactionLogFilter.builder().successfulOnly(true).build();
        WebDataBinder binder = new WebDataBinder(filter);
        binder.bind(new MutablePropertyValues(java.util.Map.of("successfulOnly", "false", "hasAbo", "true", "evtMnt", "Tarif")));
        assertFalse(binder.getBindingResult().hasErrors());
        assertEquals(Boolean.FALSE, filter.getSuccessfulOnly());
        assertEquals(Boolean.TRUE, filter.getHasAbo());
        assertEquals("Tarif", filter.getEvtMnt());
        var defaults = new org.esupportail.pay.web.admin.CsvController().transactionFilter();
        new WebDataBinder(defaults).bind(new MutablePropertyValues());
        assertEquals(Boolean.TRUE, defaults.getSuccessfulOnly());
    }
    @Test
    public void nativeStatisticsCountOnlySuccessfulPaymentsAndKeepYearScope() {
        inTransaction(() -> {
            var first = event("First");
            var fee = amount(first, "Fee");
            var january = log(fee, "00000", null);
            january.setTransactionDate(LocalDateTime.of(2025, 1, 10, 12, 0));
            var february = log(fee, "00000", null);
            february.setTransactionDate(LocalDateTime.of(2026, 2, 10, 12, 0));
            var failed = log(fee, "00001", null);
            failed.setTransactionDate(LocalDateTime.of(2026, 2, 11, 12, 0));
            log(amount(event("Second"), "Fee"), "00000", null)
                .setTransactionDate(LocalDateTime.of(2026, 2, 12, 12, 0));
            logs.flush();
            assertEquals(List.of("2026", "2025"), logs.findDistinctYears());
            assertEquals(2, ((Number) logs.findNbTransactionByYear().get(0)[1]).intValue());
            assertEquals(20, ((Number) logs.findMontantByYear().get(0)[1]).intValue());
            assertEquals(2, logs.findNbTransactionByMonth().size());
            assertEquals(10, ((Number) logs.findMontantByMonth().get(0)[2]).intValue());
            var totals = events.findSumMontantGroupByEvt("2025");
            assertEquals(1, totals.size());
            assertEquals(first.getId().longValue(), ((Number) totals.get(0)[2]).longValue());
            assertEquals(10, ((Number) totals.get(0)[1]).intValue());
            assertEquals(1, ((Number) events.findNbParticipantsByEvt("2025").get(0)[1]).intValue());
            assertEquals(2, events.findSumMontantGroupByEvt(null).size());
            assertEquals(2, events.findNbParticipantsByEvt("").size());
            assertTrue(events.findSumMontantGroupByEvt("1900").isEmpty());
            var stats = context.getBean(org.esupportail.pay.services.StatsService.class).getStats("2026");
            assertEquals(6, stats.size());
            assertEquals("2", ((java.util.Map<?, ?>) stats.get("transactions")).get("2026"));
        });
    }

    @Test
    public void searchTreatsQuotesAsDataAndKeepsStablePages() {
        inTransaction(() -> {
            event("L'été à Paris");
            event("Other");
            assertEquals(1, events.search("L'ÉTÉ", Pageable.unpaged()).getTotalElements());
            assertTrue(events.search("' OR 1=1 --", Pageable.unpaged()).isEmpty());
            var sort = Sort.by("archived").ascending().and(Sort.by("id").descending());
            var first = events.search(null, PageRequest.of(0, 1, sort));
            var second = events.search(null, PageRequest.of(1, 1, sort));
            assertEquals(2, first.getTotalElements());
            assertNotEquals(first.getContent(), second.getContent());
            assertTrue(events.search(null, PageRequest.of(2, 1, sort)).isEmpty());
        });
    }

    @Test
    public void eventUrlsAndAmountUrlsRemainScoped() {
        inTransaction(() -> {
            var a = event("A");
            a.setUrlId("event-a");
            var b = event("B");
            var first = amount(a, "Shared");
            var second = amount(b, "Shared");
            assertEquals(List.of(a), events.findByUrlId("event-a"));
            assertTrue(events.findByUrlId("missing").isEmpty());
            assertEquals(List.of(first), amounts.findByEvtAndUrlId(a, second.getUrlId()));
            assertTrue(amounts.findByEvtAndUrlId(a, "missing").isEmpty());
        });
    }

    @Test
    public void transactionFiltersAreIndependentAndCaseInsensitive() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "ÉTUDIANT");
            var match = log(fee, "00000", "sub");
            log(fee, "00001", "0").setField1("Different");
            for (String field : List.of("mail", "field1", "field2", "evtMnt")) {
                var filter = new PayTransactionLogFilter();
                switch (field) {
                    case "mail" -> filter.setMail("example.ORG");
                    case "field1" -> filter.setField1("lPh");
                    case "field2" -> filter.setField2("eTa");
                    case "evtMnt" -> filter.setEvtMnt("étudiant");
                }
                int expected = field.equals("field1") ? 1 : 2;
                assertEquals(field, expected, logs.findAll(PayTransactionLogSpecifications.matching(filter)).size());
            }
            var filter = PayTransactionLogFilter.builder().mail("' OR 1=1 --").build();
            assertTrue(logs.findAll(PayTransactionLogSpecifications.matching(filter)).isEmpty());
            filter.setMail("");
            filter.setHasAbo(true);
            assertEquals(List.of(match), logs.findAll(PayTransactionLogSpecifications.matching(filter)));
        });
    }

    @Test
    public void subscriptionScopeCannotLeakAcrossEvents() {
        inTransaction(() -> {
            var a = event("A");
            var own = log(amount(a, "Fee"), "00000", "shared");
            log(amount(event("B"), "Fee"), "00000", "shared");
            log(own.getPayEvtMontant(), "00000", "other");
            assertEquals(List.of(own), logs.findAll(PayTransactionLogSpecifications.forEvent(
                a, "shared", new PayTransactionLogFilter())));
        });
    }

    @Test
    public void archiveQueryExcludesRecentAndAlreadyArchivedTransactions() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            var cutoff = LocalDateTime.of(2026, 1, 1, 0, 0);
            var old = log(fee, "00000", null);
            old.setTransactionDate(cutoff.minusSeconds(1));
            log(fee, "00000", null).setTransactionDate(cutoff);
            var archived = log(fee, "00000", null);
            archived.setTransactionDate(cutoff.minusDays(1));
            archived.setMail("archived");
            assertEquals(List.of(old), logs.findOlderThan(cutoff));
        });
    }

    private EmailFieldsMapReference reference(PayEvtMontant fee, Date date, Boolean paid) {
        var ref = new EmailFieldsMapReference();
        ref.setPayEvtMontant(fee);
        ref.setDateCreated(date);
        ref.setIsPayed(paid);
        return context.getBean(EmailFieldsMapReferenceRepository.class).save(ref);
    }

    @Test
    public void reservationQueryExcludesExpiredPaidAndOtherAmounts() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            Date cutoff = new Date(100000);
            var pending = reference(fee, new Date(100001), false);
            var unknown = reference(fee, new Date(100002), null);
            reference(fee, cutoff, false);
            reference(fee, new Date(99999), false);
            reference(fee, new Date(100003), true);
            reference(amount(fee.getEvt(), "Other"), new Date(100003), false);
            assertEquals(java.util.Set.of(pending, unknown), new java.util.HashSet<>(
                context.getBean(EmailFieldsMapReferenceRepository.class).findUnpaidAfter(fee, cutoff)));
        });
    }

    @Test
    public void expiredReferencesExcludeScienceConfAndRecentRows() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            var expired = reference(fee, new Date(1), false);
            reference(fee, new Date(100000), false);
            var protectedRef = reference(fee, new Date(2), false);
            var science = new ScienceConfReference();
            science.setEmailFieldsMapReference(protectedRef);
            context.getBean(ScienceConfReferenceRepository.class).save(science);
            assertEquals(List.of(expired), context.getBean(EmailFieldsMapReferenceRepository.class)
                .findOlderThan(new Date(100000)));
        });
    }

    @Test
    public void transactionLookupFindsCallbackAndReturnReferences() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            var payment = log(fee, "00000", null);
            payment.setIdtrans("callback-id");
            payment.setReference("return-ref");
            assertEquals(List.of(payment), logs.findByIdtrans("callback-id"));
            assertEquals(payment, logs.findFirstByReference("return-ref"));
            assertTrue(logs.findByIdtrans("missing").isEmpty());
            assertNull(logs.findFirstByReference("missing"));
        });
    }

    @Test
    public void logoBlobSurvivesFlushAndReload() {
        inTransaction(() -> {
            var evt = event("Logo");
            byte[] bytes = new byte[]{0, 1, 2, -1, 42};
            var files = context.getBean(BigFileRepository.class);
            files.setBinaryFileStream(evt.getLogoFile(), new java.io.ByteArrayInputStream(bytes), bytes.length);
            events.flush();
            var em = SharedEntityManagerCreator.createSharedEntityManager(context.getBean(EntityManagerFactory.class));
            em.clear();
            try {
                var blob = events.findById(evt.getId()).orElseThrow().getLogoFile().getBinaryFile();
                assertArrayEquals(bytes, blob.getBytes(1, (int) blob.length()));
            } catch (java.sql.SQLException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void staleDetachedUpdateIsRejected() {
        var id = new Long[1];
        var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        PayEvtMontant stale = tx.execute(status -> {
            var fee = amount(event("Concurrent"), "Fee");
            amounts.flush();
            id[0] = fee.getId();
            return fee;
        });
        try {
            tx.execute(status -> {
                amounts.findById(id[0]).orElseThrow().setUrlId("newer");
                return null;
            });
            assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                () -> tx.execute(status -> amounts.saveAndFlush(stale)));
        } finally {
            tx.execute(status -> {
                var fee = amounts.findById(id[0]).orElseThrow();
                var evt = fee.getEvt();
                amounts.delete(fee);
                amounts.flush();
                events.delete(evt);
                return null;
            });
        }
    }

    @Test
    public void permissionsDistinguishManagerViewerAndUnrelatedUsers() {
        inTransaction(() -> {
            var evt = event("Restricted");
            var service = context.getBean(RespLoginService.class);
            evt.setRespLogins(List.of(service.findOrCreateRespLogin("owner")));
            evt.setViewerLogins(List.of(service.findOrCreateRespLogin("reader")));
            var fee = amount(evt, "Fee");
            var payment = log(fee, "00000", null);
            var permissions = context.getBean(org.esupportail.pay.security.PayPermissionEvaluator.class);
            var owner = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated("owner", "", List.of());
            var reader = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated("reader", "", List.of());
            var outsider = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated("outsider", "", List.of());
            assertTrue(permissions.hasPermission(owner, evt.getId(), "manage"));
            assertTrue(permissions.hasPermission(owner, fee.getId(), "manage-montant"));
            assertTrue(permissions.hasPermission(reader, payment.getId(), "view-txlog"));
            assertFalse(permissions.hasPermission(reader, evt.getId(), "manage"));
            assertFalse(permissions.hasPermission(outsider, evt.getId(), "view"));
            assertFalse(permissions.hasPermission(outsider, payment.getId(), "view-txlog"));
            assertFalse(permissions.hasPermission(outsider, Long.MAX_VALUE, "manage-montant"));
        });
    }

    @Test
    public void paymentCapacityCountsSuccessfulPaymentsAndActiveReservations() {
        inTransaction(() -> {
            var fee = amount(event("Capacity"), "Fee");
            fee.setDbleMontant(10.0);
            fee.setNbTransactionsMax(2L);
            var service = context.getBean(PayEvtMontantService.class);
            assertTrue(service.checkEvtMontantEnabled(fee));
            log(fee, "00001", null);
            assertTrue(service.checkEvtMontantEnabled(fee));
            log(fee, "00000", null);
            assertTrue(service.checkEvtMontantEnabled(fee));
            reference(fee, new Date(), false);
            assertFalse(service.checkEvtMontantEnabled(fee));
        });
    }

    @Test
    public void amountCapacityAcceptsExactLimitAndRejectsNextPayment() {
        inTransaction(() -> {
            var fee = amount(event("Capacity"), "Fee");
            fee.setDbleMontant(10.0);
            fee.setMontantTotalMax(2000L);
            var service = context.getBean(PayEvtMontantService.class);
            log(fee, "00000", null);
            assertTrue(service.checkEvtMontantEnabled(fee));
            log(fee, "00000", null);
            assertFalse(service.checkEvtMontantEnabled(fee));
            fee.setMontantTotalMax(-1L);
            fee.setIsEnabled(false);
            assertFalse(service.checkEvtMontantEnabled(fee));
        });
    }

    private org.springframework.test.web.servlet.MockMvc csvEndpoint() {
        var controller = new org.esupportail.pay.web.admin.CsvController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "payTransactionLogRepository", logs);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "separator", ";");
        // Standalone MVC exercises routing and binding; authorization is tested separately.
        return org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
    }

    private String export(org.springframework.test.web.servlet.MockMvc mvc, String... parameters) {
        try {
            var request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/csv");
            for (int i = 0; i < parameters.length; i += 2) request.param(parameters[i], parameters[i + 1]);
            var response = mvc.perform(request).andReturn().getResponse();
            assertEquals(200, response.getStatus());
            assertEquals("text/csv;charset=utf-8", response.getContentType());
            assertEquals("attachment; filename=\"esup-pay-transaction-log.csv\"", response.getHeader("Content-Disposition"));
            return response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void csvHttpDefaultsExportOnlySuccessfulPayments() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            log(fee, "00000", null).setIdtrans("success");
            log(fee, "00001", null).setIdtrans("failure");
            String csv = export(csvEndpoint());
            assertEquals(2, csv.split("\r\n").length);
            assertTrue(csv.contains(";success"));
            assertFalse(csv.contains(";failure"));
        });
    }

    @Test
    public void csvHttpBindsCombinedFiltersAndErrorStatus() {
        inTransaction(() -> {
            var fee = amount(event("Événement"), "Étudiant");
            log(fee, "00001", "sub").setIdtrans("selected");
            log(fee, "00000", "sub").setIdtrans("success");
            log(fee, "00001", "0").setIdtrans("no-subscription");
            log(amount(event("Other"), "Other"), "00001", "sub").setIdtrans("other-fee");
            String csv = export(csvEndpoint(), "successfulOnly", "false", "hasAbo", "true",
                "mail", "ALICE@", "field1", "LPH", "field2", "ETA", "evtMnt", "étudiant");
            assertEquals(2, csv.split("\r\n").length);
            assertTrue(csv.contains(";selected"));
            assertTrue(csv.contains("Événement;Étudiant"));
        });
    }

    @Test
    public void csvHttpEmptyOptionalFiltersDoNotDiscardRowsAndOrderIsDeterministic() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            var first = log(fee, "00000", null);
            first.setIdtrans("first");
            var second = log(fee, "00000", "0");
            second.setIdtrans("second");
            second.setTransactionDate(first.getTransactionDate());
            String csv = export(csvEndpoint(), "hasAbo", "", "mail", "", "evtMnt", "");
            assertEquals(3, csv.split("\r\n").length);
            assertTrue(csv.indexOf(";first") < csv.indexOf(";second"));
            assertEquals(1, export(csvEndpoint(), "mail", "missing").split("\r\n").length);
        });
    }

    @Test
    public void csvHttpInvalidBooleanReturnsClientError() {
        inTransaction(() -> {
            try {
                var response = csvEndpoint().perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .get("/admin/csv").param("hasAbo", "invalid")).andReturn().getResponse();
                assertEquals(400, response.getStatus());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void expiredAndPaidReservationsDoNotBlockCapacity() {
        inTransaction(() -> {
            var fee = amount(event("Capacity"), "Fee");
            fee.setDbleMontant(10.0);
            fee.setNbTransactionsMax(1L);
            reference(fee, new Date(0), false);
            reference(fee, new Date(), true);
            reference(amount(fee.getEvt(), "Other"), new Date(), false);
            assertTrue(context.getBean(PayEvtMontantService.class).checkEvtMontantEnabled(fee));
        });
    }

    @Test
    public void globalViewerCanReadButCannotManageAndAdminCanManage() {
        inTransaction(() -> {
            var evt = event("Event");
            var permissions = context.getBean(org.esupportail.pay.security.PayPermissionEvaluator.class);
            var viewer = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                "global", "", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ALL_VIEWER")));
            var admin = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                "admin", "", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
            assertTrue(permissions.hasPermission(viewer, evt.getId(), "view"));
            assertFalse(permissions.hasPermission(viewer, evt.getId(), "manage"));
            assertTrue(permissions.hasPermission(admin, evt.getId(), "manage"));
        });
    }

    @Test
    public void eventFeesTotalUsesAllFilteredPagesAndExcludesOtherEvents() {
        inTransaction(() -> {
            var evt = event("Event");
            var fee = amount(evt, "Fee");
            log(fee, "00000", null);
            log(fee, "00000", "0");
            log(fee, "00001", null).setMontant("9000");
            log(amount(event("Other"), "Fee"), "00000", null).setMontant("8000");
            var controller = new org.esupportail.pay.web.admin.PayEvtController();
            org.springframework.test.util.ReflectionTestUtils.setField(controller, "payEvtRepository", events);
            org.springframework.test.util.ReflectionTestUtils.setField(controller, "payTransactionLogRepository", logs);
            org.springframework.test.util.ReflectionTestUtils.setField(controller, "payBoxAbonnementService",
                context.getBean(org.esupportail.pay.services.PayBoxAbonnementService.class));
            var model = new org.springframework.ui.ExtendedModelMap();
            var filter = controller.transactionFilter();
            assertEquals("admin/fees-admin-view/list", controller.fees(evt.getId(), model, filter,
                PageRequest.of(0, 1, Sort.by("id"))));
            var page = (Page<?>) model.get("page");
            assertEquals(1, page.getContent().size());
            assertEquals(2, page.getTotalElements());
            assertEquals(String.format("%,.2f€", 20.0), model.get("total"));
            assertEquals(Boolean.TRUE, model.get("successfulOnly"));
        });
    }

    @Test
    public void subscriptionStatusIncludesFailuresOutsideDisplayedPage() {
        inTransaction(() -> {
            var fee = amount(event("Event"), "Fee");
            log(fee, "00001", "sub");
            var latest = log(fee, "00000", "sub");
            var page = logs.findByIdAbo("sub", PageRequest.of(0, 1, Sort.by("id").descending()));
            assertEquals(List.of(latest), page.getContent());
            var service = context.getBean(org.esupportail.pay.services.PayBoxAbonnementService.class);
            assertEquals("danger", service.buildAboStatusMap(page).get("sub"));
            log(fee, "00000", "complete");
            assertEquals("success", service.computeSubscriptionStatus("complete"));
            assertTrue(service.buildAboStatusMap(Page.empty()).isEmpty());
        });
    }

    @Test
    public void nativeStatisticsHandleEmptyDatabase() {
        inTransaction(() -> {
            assertTrue(logs.findNbTransactionByYear().isEmpty());
            assertTrue(logs.findMontantByYear().isEmpty());
            assertTrue(logs.findNbTransactionByMonth().isEmpty());
            assertTrue(logs.findMontantByMonth().isEmpty());
            assertTrue(logs.findDistinctYears().isEmpty());
            assertTrue(events.findSumMontantGroupByEvt(null).isEmpty());
            assertTrue(events.findNbParticipantsByEvt(null).isEmpty());
        });
    }

}
