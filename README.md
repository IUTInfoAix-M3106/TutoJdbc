# ![Logo Iut Aix-Marseille](https://raw.githubusercontent.com/IUTInfoAix-M2105/Syllabus/master/assets/logo.png) Bases de données avancées

## IUT d’Aix-Marseille – Département Informatique Aix-en-Provence

- **Cours:** [M3106](http://cache.media.enseignementsup-recherche.gouv.fr/file/25/09/7/PPN_INFORMATIQUE_256097.pdf)
- **Responsable:** [Sébastien NEDJAR](mailto:sebastien.nedjar@univ-amu.fr)
- **Enseignants:** [Sébastien NEDJAR](mailto:sebastien.nedjar@univ-amu.fr)
- **Besoin d'aide ?**
  - Consulter et/ou créer des [issues](https://github.com/IUTInfoAix-M3106/TutoJdbc/issues).
  - [Email](mailto:sebastien.nedjar@univ-amu.fr) pour une question d'ordre privée, ou pour convenir d'un rendez-vous physique.

## Tutoriel de découverte de JDBC [![Java CI](https://github.com/IUTInfoAix-M3106/TutoJdbc/actions/workflows/mvn_build.yml/badge.svg)](https://github.com/IUTInfoAix-M3106/TutoJdbc/actions/workflows/mvn_build.yml)

L’objectif de ce document est de vous présenter une méthode d’accès à un SGBD Relationnel à travers le langage de programmation `Java`. Pour cela, nous allons dans un premier temps présenter l’API JDBC ([Java DataBase Connectivity](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/package-summary.html)). C’est un ensemble de classes permettant d’exécuter des ordres SQL de manière générique. En effet, l’API JDBC est construit autour de pilotes (Driver) interchangeables. Un pilote est un module logiciel dédié à une source de données tabulaire (un SGBD-R dans la plupart des cas). Pour utiliser comme source de données MySQL au lieu d’Oracle, il suffit de de remplacer le pilote Oracle par celui de MySQL. Ce changement de pilote peut se faire directement par paramétrage sans avoir besoin changer une seule ligne de code ni le recompiler (Il faut tout de même pondérer ces avantages car dans la pratique il existe de très nombreuses incompatibilités liées à des implémentations du langage SQL non respectueuses des standards).

### Mise en place de l’environnement de travail

L’API JDBC fait partie de Java mais le pilote propre au SGBD-R n’y est pas. Avant de pouvoir se connecter à une base de données, il faudra donc ajouter à votre projet le fichier _jar_ contenant le pilote adapté. Si vous utilisez un projet Maven, l'ajout se fera par l'insertion d'une nouvelle dépendance dans le fichier `pom.xml` de votre projet.

Par exemple pour pouvoir accéder à une base de donnée MySQL, vous devrez rajouter les lignes suivantes entre dans le bloc `<dependencies> </dependencies>` :

```xml
<dependency>
   <groupId>mysql</groupId>
   <artifactId>mysql-connector-java</artifactId>
   <version>8.0.26</version>
</dependency>
```

### Traitement d’un ordre SQL avec JDBC

L’objectif général de cette partie est de mettre en évidence le schéma de fonctionnement classique de l’API Java d’interaction avec les bases de données relationnelles. Le principe de fonctionnement de cette API est proche de celle d'autres langages objets comme PHP ou C\#. D’une manière générale, pour traiter un ordre SQL avec JDBC, il faudra suivre les étapes suivantes :

1. Connexion à la base de données.

2. Création d’une instruction SQL.

3. Exécution de la requête.

4. Traitement de l’ensemble des résultats.

5. Libération des ressources et fermeture de la connexion.

Étant donné que chacune de ses étapes est susceptible de rencontrer des erreurs, il faudra donc rajouter une étape supplémentaire de gestion des exceptions.

Pour illustrer ce propos, nous utiliserons la base de données « Gestion Pédagogique » que vous avez utilisée lors de vos TP de PL/SQL en début d’année. Dans le présent dépôt, vous pourrez trouver un script de génération des tables adapté à MySQL ou Oracle.

Si vous souhaitez utiliser MySQL, vous devez créer votre base de données. Pour pouvoir travailler avec votre propre base de données, il est conseillé de créer une base de donnée MySQL chez [Always Data](https://www.alwaysdata.com/fr/). Une fois votre base créée, il faudra créer un utilisateur avec les droit de modification et remplir votre base à partir de l'interface PhpMyadmin fournie.

Le programme Java ci-dessous va être utilisé pour illustrer le fonctionnement de chacune de ces étapes. L’objectif de ce programme est de récupérer la liste des numéros, noms et prénoms de tous les étudiants habitant à Aix-en-Provence pour l’afficher à l’écran.

```java
// Ne pas faire un copier/coller de ce document. 
// Importez plutôt directement le dépôt dans l'IDE


// Importer les classes jdbc
import java.sql.*;

public class testJDBC {
    // Chaîne de connexion
    static final String CONNECT_URL = "jdbc:mysql://localhost:3306/gestionPedaBD";
    static final String LOGIN = "monUser";
    static final String PASSWORD = "monPassword";
    
    // La requête de test
    static final String req = "SELECT NUM_ET, NOM_ET, PRENOM_ET " +
                              "FROM ETUDIANT " +
                              "WHERE VILLE_ET = 'AIX-EN-PROVENCE'";

    public static void main(String[] args) throws SQLException {
        // Connexion a la base
        System.out.println("Connexion a " + CONNECT_URL);

        try (Connection conn = DriverManager.getConnection(CONNECT_URL,LOGIN,PASSWORD)){
            System.out.println("Connecte\n");
            // Creation d'une instruction SQL
            Statement stmt = conn.createStatement();
            
            // Execution de la requête
            System.out.println("Execution de la requête : " + req );
            ResultSet rset = stmt.executeQuery(req);
            
            // Affichage du résultat
            while (rset.next()){    
                System.out.print(rset.getInt("NUM_ET") + " ");
                System.out.print(rset.getString("NOM_ET") + " ");
                System.out.println(rset.getString("PRENOM_ET"));
            }
            
            // Fermeture de l'instruction (libération des ressources)
            stmt.close();
            System.out.println("\nOk.\n");
        } catch (SQLException e) {
            //Ceci n'est pas une gestion réaliste des erreurs
            e.printStackTrace();// Arggg!!!
            System.out.println(e.getMessage() + "\n");
        }
    }
}
```

Les différentes étapes détaillées ci-dessous mentionnent de nombreuses classes contenues dans les paquetages `java.sql.*` et `javax.sql.*`. Pour connaître les détails sur chacune de ces classes vous êtes invités à lire la Javadoc que vous trouverez à l’adresse suivante : <https://docs.oracle.com/en/java/javase/11/docs/api/index.html>.

#### Connexion à la base de données

La première étape qui permet d’interagir avec une base de données est la connexion. Il faut initialiser un objet du type `Connection` grâce à la méthode `getConnection()` de la classe `DriverManager`.

#### Création d’une instruction SQL

Une fois la connexion établie, il faut créer un objet matérialisant l’ordre SQL à exécuter. Cet objet du type `Statement` est obtenu en appelant la méthode `createStatement()` de notre connexion. Il existe trois types d’ordre :

1. Les `Statement` : Ils permettent d’exécuter n’importe quelle requête sans paramètre. La requête est interprétée par le SGBD au moment de son exécution. Ce type d’ordre est à utiliser principalement pour les requêtes à usage unique.

2. Les `PreparedStatement` : Ils permettent de précompiler un ordre avant son exécution. Ils sont particulièrement importants pour les ordres destinés à être exécutés plusieurs fois comme par exemple les requêtes paramétrées.

3. Les `CallableStatement` : Ils sont destinés à l’appel des procédures stockées.

#### Exécution de la requête

Afin d’exécuter une requête, il suffit de faire appel à l’une des méthodes `executeXXXX()` de l’objet `Statement` que l’on vient de créer. Dans l’exemple ci-dessus on utilise la méthode `executeQuery()` en lui passant en paramètre une chaîne de caractères (`string`) contenant la requête (comme `Execute Imediate` de PL/SQL). Cette méthode retourne un objet du type `ResultSet` contenant l’ensemble des résultats de la requête. Il faut noter que si l’ordre SQL est une mise à jour des données (`Insert`, `Update`, `Delete`), il faudra alors l’exécuter avec la méthode `executeUpdate()` qui retourne un entier correspondant au nombre de lignes impactées par la mise à jour.

#### Traitement de l’ensemble des résultats

La manipulation du résultat d’une requête se fait à travers un objet du type `ResultSet`. Le résultat se manipule, comme avec les curseurs de PL/SQL, ligne après ligne. Ainsi, l’objet `ResultSet` maintient un pointeur vers la ligne courante. La manipulation de ce pointeur se fait principalement avec la méthode `next()` qui permet d’avancer le pointeur sur la ligne suivante. Lors de la création du `ResultSet` ce pointeur est positionné sur une ligne spéciale appelée le _gap_. Cette ligne est située une ligne avant la première ligne du résultat. De ce fait, la première ligne n’est pointée qu’après le premier appel à `next()`. Lorsque le pointeur est positionné après la dernière ligne, `next()` retourne la valeur `false`. Pour parcourir linéairement l’intégralité d’un `ResultSet`, on utilise donc une boucle `while` avec `next()` comme prédicat de continuation. Le corps de la boucle est dédié à la manipulation de la ligne (tuple) couramment pointée.

Afin de récupérer les valeurs des attributs du tuple courant, on utilise l’une des différentes méthodes `getXXXX()` (où `XXXX` désigne le type de l’attribut que l’on souhaite récupérer). Par exemple, pour récupérer un entier on utilise `getInt()` et pour récupérer un booléen on utilise `getBoolean()`. Le paramètre passé à cet accesseur permet de choisir l’attribut à récupérer. Il existe deux façons pour désigner un attribut. La première (celle de l’exemple) consiste à utiliser une `string` contenant le nom de la colonne souhaitée. La seconde quant à elle, passe en paramètre un entier (`int`) contenant la position (dans le `Select`) de l’attribut à récupérer. Attention, contrairement à l’habitude en programmation, les attributs sont numérotés à partir de 1 (et non de 0). Par exemple, si l’on souhaite récupérer la valeur de l’attribut `NUM_ET` (le premier dans notre `Select`), il faudra faire : `rset.getInt(1)`.

#### Libération des ressources et fermeture de la connexion

Tant que l’on utilise un `Statement` ou une `Connection`, le système nous alloue un certain nombre de ressources. Maintenir ces ressources disponibles a un coût non négligeable. Ainsi, comme toujours en informatique, pour éviter le gaspillage (et donc un ralentissement inutile) il faut libérer les ressources dès qu’elles ne sont plus nécessaires. Pour ce faire, il suffit d’appeler la méthode `close()` des objets `Statement` et `Connection`.

#### Gestion des exceptions

La grande majorité des classes de JDBC sont susceptibles de lever des exceptions lorsqu’elles rencontrent des erreurs. C’est pour cela qu’il faut toujours encadrer le code JDBC par un bloc `try/catch`. Les exceptions levées sont toutes des classes filles de `SQLException`.

## Travail à faire

Cloner le dépôt `IUTInfoAix-M3106/TutoJdbc` et l'importer dans votre IDE. Le fichier java donné en exemple devra être adapté avec les informations vers votre base de données personnelle (nom d'hôte, login, mot de passe).
Lancer la classe `TestJDBC` pour vérifier que tout fonctionne. N’oubliez pas de configurer votre base de données pour qu’elle contienne des données accessible à un utilisateur lambda.

## Ouvrir votre projet avec Gitpod

Si vous n'êtes pas certain de pouvoir disposer d'un environnement correctement configuré, vous pouvez ouvrir le dépôt dans l'IDE en ligne mis à disposition avec l'outil Gitpod.

Gitpod est un outil permettant de créer des environnements de développement éphémères. Ces environnements permettent aux développeurs de disposer à chaque instant, d'un IDE prêts avec tous les outils et dépendances pré-paramétrés.

Pour ouvrir ce tutoriel avec Gitpod, vous pouvez simplement cliquer sur le bouton suivant et vous laisser guider après avoir cliqué sur le bouton "Continue with Github" :

[![Open In Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/IUTInfoAix-M3106/TutoJdbc)

## Configurer Gitpod

Pour disposer de Gitpod sur tous vos projets hébergés sur Github, suivez les étapes suivantes :

- Créez un compte Gitpod en vous rendant sur la page [Get started](https://www.gitpod.io/#get-started). Identifiez-vous avec votre compte github en cliquant sur le bouton "Continue with Github". Si vous avez déjà obtenu votre pack [Github Education](https://education.github.com/pack), vous pouvez bénéficier de l'offre 100h/mois. N'oubliez pas d'en faire la demande dans les [réglages de votre compte Gitpod](https://gitpod.io/plans).

- Installez l'[application Gitpod](https://github.com/apps/gitpod-io/installations/new). L'application GitHub de Gitpod est similaire à un serveur CI et préparera en permanence des pré-constructions pour toutes vos branches et demandes d'extraction - vous n'avez donc pas à attendre que Maven ou NPM télécharge Internet lorsque vous souhaitez commencer à travailler.

- Démarrez votre premier espace de travail avec une préconstruction en préfixant l'URL du référentiel par [https://gitpod.io#prebuild/](https://gitpod.io#prebuild/). Gitpod affiche l'état d'avancement de la pré-construction en exécutant les commandes `init` du fichier `.gitpod.yml` avant de démarrer un espace de travail. Plus tard, lorsque vous créez un nouvel espace de travail sur une branche, ou une Pull Request, l'espace de travail se charge beaucoup plus rapidement, car toutes les dépendances sont déjà téléchargées et le code est compilé.

- Installez de l'extension navigateur Gitpod sur tous les navigateurs basés sur [Chromium](https://chrome.google.com/webstore/detail/gitpod-always-ready-to-co/dodmmooeoklaejobgleioelladacbeki) (tels que Microsoft Edge, Brave, Chrome, ...) ou sur [Firefox](https://addons.mozilla.org/fr/firefox/addon/gitpod/). L'extension ajoute simplement un bouton Gitpod sur chaque projet et branche sur GitHub, et Bitbucket qui préfixe l'URL avec [http://gitpod.io/#](http://gitpod.io/#) afin que vous puissiez facilement ouvrir un nouvel espace de travail à partir de n'importe quel contexte Git.

- Personnalisez le thème par défaut en ouvrant le panneau de commande (avec le raccourci clavier `Ctrl+Shift+P`) et en sélectionnant `Preferences: Color Theme`. Si vous préférez par exemple les couleur sombre pour reposer vos yeux, le thème *gitpod dark* devrait vous convenir. Vous pouvez rajouter de nouveaux thème directement en recherchant dans les extensions (avec le raccourci clavier `Ctrl+Shift+X`).

Vous pouvez maintenant commencer à traiter les environnements de développement comme des ressources automatisées que vous lancez lorsque vous en avez besoin et fermez (et oubliez) lorsque vous avez terminé votre tâche. Les environnements de développement deviennent totalement éphémères. Attention avec votre offre Github éducation vous ne disposez que de 100h mensuels, donc il faut penser à fermer vos espaces de travail quand vous avez terminé de vous en servir (dans tous les cas ils seront fermés automatiquement après 30 minutes d'inactivité).

Vous pouvez commencer à basculer entre les espaces de travail ou ouvrir plusieurs espaces de travail sur le même contexte. Par exemple, vous pouvez en ouvrir quatre  (un pour une fonctionnalité en cours de développement, un pour réviser une PR, un pour une issue et un pour une autre PR).
