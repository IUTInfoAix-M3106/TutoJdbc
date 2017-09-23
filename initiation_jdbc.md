Introduction
============

L’objectif de ce document est de vous présenter une méthode d’accès à un <span style="font-variant:small-caps;">Sgbd</span> à travers le langage de programmation Java. Pour cela, nous allons dans un premier temps présenter l’API JDBC (Java DataBase Connectivity)[1]. C’est un ensemble de classes permettant d’exécuter des ordres <span style="font-variant:small-caps;">Sql</span> de manière générique. En effet, l’API JDBC est construit autour de pilotes (Driver) interchangeables. Un pilote est un module logiciel dédié à une source de données (un <span style="font-variant:small-caps;">Sgbd-R</span> dans la plupart des cas). Pour utiliser comme source de données MySQL au lieu d’Oracle, il suffit de de remplacer le pilote Oracle par celui de MySQL. Ce changement de pilote peut se faire directement par paramétrage sans même avoir besoin changer une seule ligne de code ni même le recompiler[2].

Mise en place de l’environnement de travail
===========================================

L’API JDBC fait partie de Java mais le pilote propre au <span style="font-variant:small-caps;">Sgbd-R</span> Oracle n’y est pas. Avant de pouvoir se connecter à la base de données située sur *allegro*, il faudra donc ajouter à votre projet le fichier *jar* contenant le pilote adapté. Le prochain paragraphe sera consacré à l’installation de ce fichier à partir de l’IDE Eclipse.

Avant de commencer, récupérez l’un des pilotes dans le repertoire local : [/commun\_iut\_info/profs/nedjar/ojdbc6.jar](/commun_iut_info/profs/nedjar/ojdbc6.jar) ou [/commun\_iut\_info/profs/nedjar/mysql-connector-java-5.1.23-bin.jar](/commun_iut_info/profs/nedjar/mysql-connector-java-5.1.23-bin.jar). Placer le fichier dans répertoire [~/net-home/tp/tpBDA/](~/net-home/tp/tpBDA/). Puis à partir d’Eclipse, lancez l’assistant de création de nouveau projet Java (`File \rightarrow New \rightarrow Java Project`). Après avoir rempli les informations de ce premier écran, validez pour passer au suivant (*cf.* Figure \[capture1\]). Dans la nouvelle fenêtre, cliquez sur l’onglet *Libraries* (*cf.* Figure \[capture2\]) puis sur le bouton *Add External JARs* (*cf.* Figure \[capture3\]), sélectionnez le fichier `ojdbc14.jar` précédemment téléchargé (*cf.* Figure \[capture4\]), validez en cliquant sur *Finish*. Une fois ces étapes validées, vous avez un projet java capable d’utiliser JDBC pour interagir avec Oracle.

Traitement d’un ordre <span style="font-variant:small-caps;">Sql</span> avec JDBC
=================================================================================

L’objectif général de cette partie est de mettre en évidence le schéma de fonctionnement classique de l’API Java d’interaction avec les bases de données relationnelles. Le principe de fonctionnement de cette API est proche de celle de PHP ou de C\#. D’une manière générale, pour traiter un ordre <span style="font-variant:small-caps;">Sql</span> avec JDBC, il faudra suivre les étapes suivantes :

1.  Connexion à la base de données.

2.  Création d’une instruction <span style="font-variant:small-caps;">Sql</span>.

3.  Exécution de la requête.

4.  Traitement de l’ensemble des résultats.

5.  Libération des ressources et fermeture de la connexion.

Étant donné que chacune de ses étapes est susceptible de rencontrer des erreurs, il faudra donc rajouter une étape supplémentaire de gestion des exceptions.

Pour illustrer ce propos, nous utiliserons la base de données « Gestion Pédagogique[3] » que vous avez utilisée lors de vos TP de <span style="font-variant:small-caps;">Pl/Sql</span> en début d’année.

Si vous souhaitez utiliser <span style="font-variant:small-caps;">MySql</span>, vous devez créer votre base de données avec les commandes suivantes :

``` bash
wget https://raw.githubusercontent.com/nedseb/TpJpa/master/gestion_peda_mysql.sql -O ~/net-home/gestion_peda_mysql.sql
mysql --user=root --password=mysql --execute="create database gestionPedaBD"
mysql --user=root --password=mysql --execute="grant all privileges on gestionPedaBD.* to monUser@localhost identified by 'monPassword'"
mysql --user=monUser --password=monPassword gestionPedaBD --execute=" source ~/net-home/gestion_peda_mysql.sql"
```

Le modèle conceptuel des données est rappelé par la figure \[mcd\_gestion\_peda\].

\[node distance=1.96cm, every edge/.style=<span>link</span>\]

(mat) <span> **Module**
Libellé
H\_Cours\_Prev
H\_Cours\_Rea
H\_TP\_Prev
H\_TP\_Rea
Discipline
Coef\_Test
Coef\_CC
</span>;

(etud) \[below right =of mat \] <span> **Etudiant**
Nom\_Et
Prénom\_Et
CP\_Et
Ville\_Et
Année
Groupe
</span>;

(ens) \[above right=of etud \] <span> **Prof**
Nom\_Prof
Prénom\_Prof
Adr\_Prof
CP\_Prof
Ville\_Prof
</span>;

(notation) \[below =of mat\] <span>Notation</span> child <span>node\[attributes\] <span>Moy\_CC
Moy\_Test</span></span>; (mat) – node \[pos=0.15, auto\] <span>(0,n)</span> (notation); (etud.145) – node \[pos=0.35, auto, swap\] <span>(0,n)</span> (notation);

(enseigne) \[above=of etud\] <span>Enseignement</span>; (mat.315) – node \[pos=0.40, auto\] <span>(0,n)</span> (enseigne.west); (ens.208) – node \[pos=0.40, auto, swap\] <span>(0,n)</span> (enseigne.east); (etud) – node \[pos=0.15, auto, swap\] <span>(0,n)</span> (enseigne);

(mat\_spec) \[above=1cm of enseigne\] <span>Spécialiste</span>; (mat.12) – node \[pos=0.35, auto\] <span>(0,n)</span> (mat\_spec); (ens.145) – node \[pos=0.35, auto, swap\] <span>(1,1)</span> (mat\_spec);

(resp) \[above=1cm of mat\_spec\] <span>Responsable</span>; (mat.56) – node \[pos=0.3, auto\] <span>(1,1)</span> (resp); (ens.north) |- node \[pos=0.15, auto, swap\] <span>(0,n)</span> (resp.east);

(mat\_pere) \[left=1cm of mat\] <span>A pour père</span>; (mat.150) -| node \[pos=0.1, auto, swap\] <span>(1,1)</span> (mat\_pere.north); (mat.210) -| node \[pos=0.1, auto\] <span>(0,n)</span> (mat\_pere.south);

Le programme Java ci-dessous[4] va être utilisé pour illustrer le fonctionnement de chacune de ces étapes. L’objectif de ce programme est de récupérer la liste des numéros, noms et prénoms de tous les étudiants habitant à Aix-en-Provence pour l’afficher à l’écran.

```
// Ne pas faire un copier/coller du pdf...
// Fichier recuperable a l'adresse suivante :https://raw.githubusercontent.com/nedseb/TpJpa/master/testJDBC.java


// Importer les classes jdbc
import java.sql.*;

public class testJDBC {
    // Chaine de connexion
    static final String CONNECT_URL = "jdbc:mysql://localhost:3306/gestionPedaBD";
    static final String LOGIN = "monUser";
    static final String PASSWORD = "monPassword";
    // La requete de test
    static final String req = "SELECT NUM_ET, NOM_ET, PRENOM_ET " +
                              "FROM ETUDIANT " +
                              "WHERE VILLE_ET = 'AIX-EN-PROVENCE'";                                     
    public static void main(String[] args) throws SQLException {
        // Objet materialisant la connexion a la base de donnees
        Connection conn = null;
        try {
            // Connexion a la base
            System.out.println("Connexion a " + CONNECT_URL);
            conn = DriverManager.getConnection(CONNECT_URL,LOGIN,PASSWORD);
            System.out.println("Connecte\n");
            // Creation d'une instruction SQL
            Statement stmt = conn.createStatement();
            // Execution de la requete
            System.out.println("Execution de la requete : " + req );
            ResultSet rset = stmt.executeQuery(req);
            // Affichage du resultat
            while (rset.next()){    
                System.out.print(rset.getInt("NUM_ET") + " ");
                System.out.print(rset.getString("NOM_ET") + " ");
                System.out.println(rset.getString("PRENOM_ET"));
            }
            // Fermeture de l'instruction (liberation des ressources)
            stmt.close();
            System.out.println("\nOk.\n");
        } catch (SQLException e) {
            e.printStackTrace();// Arggg!!!
            System.out.println(e.getMessage() + "\n");
        } finally {
            if (conn != null) {
                // Deconnexion de la base de donnees
                conn.close();
            }
        }
    }
}
```

Les différentes étapes détaillées ci-dessous mentionnent de nombreuses classes contenues dans les paquetages `java.sql.*` et `javax.sql.*`. Pour connaître les détails sur chacune de ces classes vous êtes invités à lire la Javadoc que vous trouverez à l’adresse suivante : <http://download.oracle.com/javase/6/docs/api/>.

Connexion à la base de données
------------------------------

La première étape qui permet d’interagir avec une base de données est la connexion. Il faut initialiser un objet du type `Connection` grâce à la méthode `getConnection()` de la classe `DriverManager`.

Création d’une instruction <span style="font-variant:small-caps;">Sql</span>
----------------------------------------------------------------------------

Une fois la connexion établie, il faut créer un objet matérialisant l’ordre <span style="font-variant:small-caps;">Sql</span> à exécuter. Cet objet du type `Statement` est obtenu en appelant la méthode `createStatement()` de notre connexion. Il existe trois types d’ordre :

1.  Les `Statement` : Ils permettent d’exécuter n’importe quelle requête sans paramètre. La requête est interprétée par le <span style="font-variant:small-caps;">Sgbd</span> au moment de son exécution. Ce type d’ordre est à utiliser principalement pour les requêtes à usage unique.

2.  Les `PreparedStatement` : Ils permettent de précompiler un ordre avant son exécution. Ils sont particulièrement importants pour les ordres destinés à être exécutés plusieurs fois comme par exemple les requêtes paramétrées.

3.  Les `CallableStatement` : Ils sont destinés à l’appel des procédures stockées.

Exécution de la requête
-----------------------

Afin d’exécuter une requête, il suffit de faire appel à l’une des méthodes `executeXXXX()` de l’objet `Statement` que l’on vient de créer. Dans l’exemple ci-dessus on utilise la méthode `executeQuery()` en lui passant en paramètre une chaîne de caractères (`string`) contenant la requête (comme <span style="font-variant:small-caps;">Execute Imediate</span> de <span style="font-variant:small-caps;">Pl/Sql</span>). Cette méthode retourne un objet du type `ResultSet` contenant l’ensemble des résultats de la requête. Il faut noter que si l’ordre <span style="font-variant:small-caps;">Sql</span> est une mise à jour des données (<span style="font-variant:small-caps;">Insert</span>, <span style="font-variant:small-caps;">Update</span>, <span style="font-variant:small-caps;">Delete</span>), il faudra alors l’exécuter avec la méthode `executeUpdate()` qui retourne un entier correspondant au nombre de lignes impactées par la mise à jour.

Traitement de l’ensemble des résultats
--------------------------------------

La manipulation du résultat d’une requête se fait à travers un objet du type `ResultSet`. Le résultat se manipule, comme avec les curseurs de <span style="font-variant:small-caps;">Pl/Sql</span>, ligne après ligne. Ainsi, l’objet `ResultSet` maintient un pointeur vers la ligne courante. La manipulation de ce pointeur se fait avec la méthode `next()` qui permet d’avancer le pointeur sur la ligne suivante. Lors de la création du `ResultSet` ce pointeur est positionné sur une ligne spéciale appelée le *gap*. Cette ligne est située une ligne avant la première ligne du résultat. De ce fait, la première ligne n’est pointée qu’après le premier appel à `next()`. Lorsque le pointeur est positionné après la dernière ligne, `next()` retourne la valeur `false`. Pour parcourir linéairement l’intégralité d’un `ResultSet`, on utilise donc une boucle `while` avec `next()` comme prédicat de continuation. Le corps de la boucle est dédié à la manipulation de la ligne (tuple) couramment pointée.

Afin de récupérer les valeurs des attributs du tuple courant, on utilise l’une des différentes méthodes `getXXXX()` (où `XXXX` désigne le type de l’attribut que l’on souhaite récupérer). Par exemple, pour récupérer un entier on utilise `getInt()` et pour récupérer un booléen on utilise `getBoolean()`. Le paramètre passé à cet accesseur permet de choisir l’attribut à récupérer. Il existe deux façons pour désigner un attribut. La première (celle de l’exemple) consiste à utiliser une `string` contenant le nom de la colonne souhaitée. La seconde quant à elle, passe en paramètre un entier (`int`) contenant la position (dans le <span style="font-variant:small-caps;">Select</span>) de l’attribut à récupérer. Attention, contrairement à l’habitude en programmation, les attributs sont numérotés à partir de 1 (et non de 0). Par exemple, si l’on souhaite récupérer la valeur de l’attribut `NUM_ET` (le premier dans notre <span style="font-variant:small-caps;">Select</span>), il faudra faire : `rset.getInt(1)`.

Libération des ressources et fermeture de la connexion
------------------------------------------------------

Tant que l’on utilise un `Statement` ou une `Connection`, le système nous alloue un certain nombre de ressources. Maintenir ces ressources disponibles a un coût non négligeable. Ainsi, comme toujours en informatique, pour éviter le gaspillage (et donc un ralentissement inutile) il faut libérer les ressources dès qu’elles ne sont plus nécessaires. Pour ce faire, il suffit d’appeler la méthode `close()` des objets `Statement` et `Connection`.

Gestion des exceptions
----------------------

La grande majorité des classes de JDBC sont susceptibles de lever des exceptions lorsqu’elles rencontrent des erreurs. C’est pour cela qu’il faut toujours encadrer le code JDBC par un bloc `try/catch`. Les exceptions levées sont toutes des classes filles de `SQLException`.

#### Question  :

Mettre en place un projet TestJDBC pour tester la classe donnée en exemple. N’oubliez pas de configurer votre base de données pour qu’elle contienne les données nécéssaires.

[1] <http://download.oracle.com/javase/6/docs/technotes/guides/jdbc/>

[2] Il faut tout de même pondérer ces avantages car dans la pratique il existe de très nombreuses incompatibilités liées à des implémentations du langage <span style="font-variant:small-caps;">SQL</span> non respectueuses des standards.

[3] Script de régénération disponible à l’adresse suivante : <https://raw.githubusercontent.com/nedseb/TpJpa/master/gestion_peda_oracle.sql> ou <https://raw.githubusercontent.com/nedseb/TpJpa/master/gestion_peda_mysql.sql>

[4] Code source disponible à l’adresse suivante : <https://raw.githubusercontent.com/nedseb/TpJpa/master/testJDBC.java>
