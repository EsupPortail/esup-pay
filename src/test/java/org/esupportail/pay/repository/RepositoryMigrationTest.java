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
import org.h2.jdbcx.JdbcDataSource;
import org.junit.*;
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
    @Import({PayEvtMontantService.class, RespLoginService.class})
    static class Config {
        @Bean
        DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:repository-migration;DB_CLOSE_DELAY=-1");
            return ds;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("org.esupportail.pay.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
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
}
