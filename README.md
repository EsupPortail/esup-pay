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


### Export des paiements pour ONLYOFFICE

Dans la liste des paiements d’un événement, un gestionnaire peut ouvrir **Export ONLYOFFICE** et générer un jeton. L’écran affiche l’URL et la valeur de l’en-tête `Authorization` à configurer dans le connecteur. L’URL est construite à partir de la valeur publique HTTPS de `esup-pay.url`.

Le connecteur appelle l’URL par GET avec l’en-tête `Authorization: Bearer <jeton>`. La réponse contient les paiements réussis de l’événement en CSV UTF-8 séparé par des points-virgules. Importez la colonne `ID transaction` comme du texte pour conserver les identifiants tels quels. Un jeton manquant, invalide ou expiré donne une réponse HTTP 401.

Chaque événement dispose d’un jeton valable 90 jours. Le jeton est affiché à sa création et peut être remplacé ou révoqué depuis le même écran. Le proxy doit transmettre l’en-tête `Authorization` jusqu’à esup-pay et laisser la route `/api/payment-exports/*/payments.csv` atteindre l’application sans imposer de session SSO. Un connecteur côté serveur peut appeler directement cette route et y conserver le jeton.

#### Plugin ONLYOFFICE Docs

Le plugin esup-pay est dans `src/main/onlyoffice` et cible ONLYOFFICE Docs 9.1 ou ultérieur. Depuis la racine du dépôt, générer l’archive d’installation avec les commandes suivantes.

```sh
mkdir -p target
(cd src/main/onlyoffice && zip -j ../../../target/esup-pay-onlyoffice.plugin config.json index.html payments.js plugin.js)
```

L’administrateur ONLYOFFICE Docs peut déployer les fichiers dans un sous-dossier de `sdkjs-plugins` ou déclarer l’URL du manifeste dans `editorConfig.plugins.pluginsData`. Voir le [guide d’installation ONLYOFFICE Docs](https://api.onlyoffice.com/docs/plugins/development-workflow/installing-and-testing/docs-on-premises-installation).

Si le plugin et esup-pay ont des origines différentes, configurer `esup-pay.onlyoffice-origins` dans `esup-pay.properties` avec l’origine HTTPS de la page du plugin, sans chemin ni barre oblique finale. Plusieurs origines peuvent être séparées par des virgules. Le proxy doit aussi transmettre la requête `OPTIONS` de vérification CORS sur la route d’export.

```properties
esup-pay.onlyoffice-origins=https://onlyoffice.example.org
```

Dans un XLSX ouvert en édition, sélectionner **Plugins → esup-pay**, coller l’URL et le jeton, puis cliquer sur **Actualiser les paiements**. Le plugin efface le jeton du formulaire après la mise à jour et enregistre l’URL de la source dans le XLSX. Il crée deux feuilles.

- **Paiements esup-pay** contient l’export courant. Les identifiants sont importés comme texte et les montants comme nombres en centimes.
- **Suivi esup-pay** contient les ID déjà importés et une liste déroulante **Vérifié** avec les choix ☐ et ☑. L’actualisation ajoute les ID nouveaux et préserve les coches existantes même si un paiement disparaît de l’export puis revient. Si vous triez le suivi, triez les deux colonnes ensemble.

**Actualisation à l’ouverture.** Ajouter le GUID du plugin à `editorConfig.plugins.autostart` dans l’intégration ONLYOFFICE, pour les classeurs concernés. Si l’intégration gère aussi le chargement du plugin, déclarer le manifeste dans `pluginsData`.

```json
{
  "editorConfig": {
    "plugins": {
      "autostart": ["asc.{D6A6E8CB-8351-4A58-9C00-F08E2FC43AB2}"],
      "pluginsData": ["https://onlyoffice.example.org/sdkjs-plugins/esup-pay/config.json"]
    }
  }
}
```

Au premier import, cocher **Garder le jeton dans ce navigateur pour actualiser à l’ouverture**. Aux ouvertures suivantes en édition, le plugin lit l’URL du classeur et récupère dans le stockage local du navigateur le jeton associé à cette URL et à l’identifiant aléatoire du classeur. Utiliser cette option sur un profil de navigateur de confiance. **Oublier le jeton enregistré** efface la copie locale et désactive l’actualisation automatique pour cette source sur ce profil. Après rotation du jeton esup-pay, saisir le nouveau jeton lors d’une actualisation manuelle.

En coédition, activer cette option sur un seul profil chargé des actualisations. Lorsque le navigateur fournit les Web Locks, le plugin sérialise les actualisations entre les onglets de ce profil. Les collaborateurs peuvent continuer à modifier les coches depuis leur propre session.

**Tableaux croisés dynamiques.** Dès le premier import qui contient des paiements, le plugin crée la feuille **Sommes esup-pay** si elle est disponible. Le TCD regroupe par événement et par tarif (`payEvtMontant`) et additionne les montants effectivement payés, en centimes. Pour créer d’autres TCD, utiliser une feuille de synthèse distincte et prendre comme source `'Paiements esup-pay'!A1:H<n>`, avec les huit en-têtes et au moins une ligne dessous. Après chaque import, le plugin ajuste cette plage à la taille du CSV et rafraîchit le TCD. Une plage personnalisée sur cette feuille est rafraîchie à taille fixe. Pour un export vide, le plugin conserve les en-têtes et une ligne vide comme source du TCD.

Utiliser un classeur par événement et réserver les deux feuilles au plugin et au suivi.

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


### Tests automatisés

Exécuter `mvn test` avec un démon Docker accessible. Les tests des dépôts utilisent
Testcontainers pour démarrer un PostgreSQL 14 isolé sur un port disponible, puis
le supprimer en fin de suite. L'image `postgres:14` est téléchargée au premier
lancement si nécessaire. La base configurée pour l'application n'est pas utilisée.

La suite couvre les filtres, la pagination, le cloisonnement par événement, les
permissions métier, les statistiques SQL PostgreSQL, les réservations et limites
de paiement, les requêtes d'archivage, les logos BLOB et les conflits de version.
Les tests MockMvc vérifient les paramètres HTTP et le contenu des exports CSV ;
ils ne chargent pas la chaîne de sécurité et ne rendent pas les vues Thymeleaf.
Le rendu navigateur, le JavaScript, CAS et les échanges réels avec Paybox restent
à valider séparément.

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

### Développement

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

### Test

Si vous souhaitez tester esup-pay avec docker (sans lancer esup-pay manuellement depuis une jvm en local), vous pouvez lancer le docker compose avec le profile 'with-esup-pay' :

Depuis le répertoire __docker4dev__ :
```
docker compose --profile with-esup-pay up
```
Voir ci-dessus pour les comptes de test à utiliser.


### Récupération des transactions en erreur

À partir de la 2.3.0, esup-pay conserve également les transactions marquées en erreur par paybox, cela est notamment intéressant pour les paiements en n fois (notion d'abonnement).

Si vous souhaitez récupérer ces erreurs non persistées avant cette 2.3.0 en reprenant vos fichiers logs apache/nginx,
vous pouvez rejouer les appels de paybox via des commandes curl.

Exemple qui rejoue uniquement les transsactions liées à des abonnements et qui sont tombés en erreur : 
```
bzgrep 'GET /payboxcallback' /var/log/archives/esup-pay-access.log-202*.bz2  | fgrep idAbo | fgrep -v 'idAbo=0' | fgrep -v 'erreur=00000' | sed 's$.*GET $curl "http://localhost:8080$' | sed 's/ HTTP.*/"/' | sh 
```

