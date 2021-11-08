package fr.univ_amu.iut;

// Importer les classes jdbc
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestJDBC {
	// Chaine de connexion
	static final String CONNECT_URL = "jdbc:mysql://localhost:3306/GestionPedaBD";
	static final String LOGIN = "monUser";
	static final String PASSWORD = "monPassword";
	// La requete de test
	static final String QUERY = "SELECT NUM_ET, NOM_ET, PRENOM_ET " + "FROM ETUDIANT "
			+ "WHERE VILLE_ET = 'AIX-EN-PROVENCE'";

	private static final Logger logger = Logger.getLogger("TestJDBC");

	public static void main(String[] args) throws SQLException {
		// Connexion a la base de données
		logger.log(Level.INFO, "Connexion a " + CONNECT_URL);
		try (Connection connection = DriverManager.getConnection(CONNECT_URL, LOGIN, PASSWORD)) {
			logger.log(Level.INFO, "Création instruction SQL");
			try (Statement statement = connection.createStatement()) {
				logger.log(Level.INFO, "Execution de la requete : " + QUERY);
				try (ResultSet resultSet = statement.executeQuery(QUERY)) {
					logger.log(Level.INFO, "Affichage du resultat");
					while (resultSet.next()) {
						logger.log(Level.INFO, "{0}",resultSet.getInt("NUM_ET") + " " +resultSet.getString("NOM_ET") +" "+ resultSet.getString("PRENOM_ET"));
					}
				}
			}
			logger.log(Level.INFO, "Fin de l\'exemple");
		} catch (SQLException e) {
			e.printStackTrace();// Arggg!!!
			logger.log(Level.INFO, e.getMessage());
		}
	}
}
