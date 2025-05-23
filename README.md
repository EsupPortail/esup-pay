![EsupPay](https://github.com/EsupPortail/esup-pay/raw/master/src/main/webapp/images/credit-card.png)

Application de dématérialisation de paiements en ligne.
============================

Cette application permet aux personnels d'un établissement de créer des formulaires de paiement en lignes.

Ces formulaires sont à destination de tout internaute, et peuvent être utilisés dans le cadre de règlement d'inscriptions à des colloques, de réglement de factures, de formulaires de dons, etc. 

Cette application utilise Paybox - http://www1.paybox.com - pour réaliser effectivement le paiement.

__Paybox est un service de paiement en ligne (commercial) permettant de se décharger de la procédure de paiement en ligne via cartes bancaires, e-carte bleues, comptes paypal et autres.__

Cette application utilise CAS+LDAP pour autoriser uniquement certains membres de l'établissement à créer des formulaires de paiement en ligne.

## Prérequis

* un CAS
* un LDAP avec un groupe regroupant les administrateurs de l'application esup-pay qui pourront créer des évènements esup-pay - ce groupe peut-être un posixGroup ou un groupOfNames
* un compte Paybox - esup-pay utilise l'intégration paybox version hmac (sans module cgi) - il faut donc un compte paybox avec un hmac de configuré. 
* une base de données PostgreSQL
* JDK 17
* Tomcat 10

Précisions supplémentaires : 
* esup-pay est complètement indépendant de l'ENT esup-uportal
* esup-pay peut être intégré à sciencesconf.org (https://www.sciencesconf.org)

## Configurations 

Logs : 
* src/main/resources/log4j.properties

Base de données : 
* src/main/resources/META-INF/spring/database.properties pour paramètres de connexion
* src/main/resources/META-INF/persistence.xml pour passage de create à update après premier lancement (création + initialisation de la base de données)

Mails : 
* src/main/resources/META-INF/spring/email.properties

Logo, Titre, CAS, LDAP, Paybox :
* src/main/resources/META-INF/spring/esup-pay.properties

Pour des configurations avancées, il est également possible de modifier les fichiers xml Spring tels que par exemple src/main/resources/META-INF/spring/applicationContextPaybox.xml (pour définir plusieurs comptes paybox par exemple).


## Installation 

### Pré-requis
* Java OpenJDK 17 : le mieux est de l'installer via le système de paquets de votre linux.
* Maven : le mieux est de l'installer via le système de paquets de votre linux.
* Postgresql (15, ...) : le mieux est de l'installer via le système de paquets de votre linux.
* Tomcat (Tomcat 10)

### PostgreSQL
* pg_hba.conf : ajout de 

``` 
host    all             all             127.0.0.1/32            password
``` 

* redémarrage de postgresql
* psql

```
create database esuppay;
create USER esuppay with password 'esup';
grant ALL ON DATABASE esuppay to esuppay;
ALTER DATABASE esuppay OWNER TO esuppay;
```

### Paramétrage mémoire JVM :

Pensez à paramétrer les espaces mémoire JVM : 
```
export JAVA_OPTS="-Xms512m -Xmx512m"
```

Pour maven :
```
export MAVEN_OPTS="-Xms512m -Xmx512m"
```

### Lancement simple avec jetty :
```
mvn jetty:run
```
Puis firefox http://localhost:8080


### Obtention du war pour déploiement sur tomcat ou autre :
```
mvn clean package
```


## POSTGRESQL

Pour une bonne gestion des blob de cette application (utilisés pour stocker les logos des formulaires de paiement), il faut ajouter dans PostgreSQL un trigger sur la base de données sur la table big_file.
La fonction lo_manage est nécessaire ici.

Sous debian : 
```
apt-get install postgresql-contrib
```

Puis la création de l'extension lo se fait via un super-user:

```
psql
\c esuppay
CREATE EXTENSION lo;
```
--

Et enfin ajout du trigger* : 
```
CREATE TRIGGER t_big_file BEFORE UPDATE OR DELETE ON big_file  FOR EACH ROW EXECUTE PROCEDURE lo_manage(binary_file);
```

CF https://doc.postgresql.fr/15/lo.html

\* afin que les tables soient préalablement créées, notamment la table big_file sur lequel on souhaite mettre le trigger lo_manage, vous devez démarrer l'application une fois ; en n'oubliant pas ensuite, pour ne pas écraser la base au redémarrage, de __modifier src/main/resources/META-INF/persistence.xml : create-> update__ - cf ci-dessous.



## Test / Environnement de développement avec DOCKER

Pour faciliter le développement, un docker-compose est livré avec esup-pay. Celui-ci permet de monter les services requis suivants :
* un PostgreSQL
* un LDAP
* un CAS

Vous pouvez ainsi lancer depuis le répertoire __docker4dev__ :
```
docker compose up
```

Puis depuis le répertoire principal, vous lancez le jetty ainsi :
```
mvn clean jetty:run -Djetty.systemPropertiesFile=docker4dev/esup-pay4docker.properties
```

Avec les fichiers de configuration par défaut, un esup-pay est alors opérationnel.
Vous pouvez alors accéder à http://localhost:8080 et vous authentifier avec un des comptes suivants :
* admin/esup
* ventil/esup
* stat/esup


