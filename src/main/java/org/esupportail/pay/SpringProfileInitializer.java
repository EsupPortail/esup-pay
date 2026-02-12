package org.esupportail.pay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
/*
  Permet de charger le fichier esup-pay.properties et d'activer le profil Spring défini dans ce fichier avant que le contexte Spring ne soit initialisé. *
  Utile pour éviter de devoir définir le profil via une variable d'environnement ou un argument de ligne de commande.
  Les profiles disponibles sont :
   * cas pour activer l'authentification via CAS
   * shib pour activer l'authentification via Shibboleth
 */
public class SpringProfileInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            ResourcePropertySource propertySource =
                    new ResourcePropertySource("classpath:META-INF/spring/esup-pay.properties");
            environment.getPropertySources().addFirst(propertySource);

            String activeProfile = environment.getProperty("spring.profiles.active");
            if (activeProfile == null || activeProfile.isEmpty()) {
                log.info("No active Spring profile defined in esup-pay.properties. Defaulting to 'cas' profile.");
                activeProfile = "cas";
            }
            environment.setActiveProfiles(activeProfile.split(","));
            log.info("Active Spring profile(s) set to: {}", activeProfile);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load esup-pay.properties", e);
        }
    }
}
