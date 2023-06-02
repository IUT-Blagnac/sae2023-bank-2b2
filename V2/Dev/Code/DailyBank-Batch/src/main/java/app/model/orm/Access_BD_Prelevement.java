package app.model.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import app.model.data.Prelevement;
import app.model.orm.exception.DataAccessException;
import app.model.orm.exception.DatabaseConnexionException;
import app.model.orm.exception.ManagementRuleViolation;
import app.model.orm.exception.Order;
import app.model.orm.exception.RowNotFoundOrTooManyRowsException;
import app.model.orm.exception.Table;

/**
 *
 * Classe d'accès aux CompteCourant en BD Oracle.
 *
 */
public class Access_BD_Prelevement {

	public Access_BD_Prelevement() {
	}

	/**
	 * Recherche des Prélèvements d'un compte courant à partir de son id.
	 *
	 * @param idNumCompte id du compte dont on cherche les prélèvements
	 * @return Tous les Prélèvements de idNumCompte (ou liste vide)
	 * @throws DataAccessException        Erreur d'accès aux données (requête mal
	 *                                    formée ou autre)
	 * @throws DatabaseConnexionException Erreur de connexion
	 */
	public ArrayList<Prelevement> getPrelevements(int idNumCompte)
			throws DataAccessException, DatabaseConnexionException {

		ArrayList<Prelevement> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM PrelevementAutomatique where idNumCompte = ?";
			query += " ORDER BY idNumCompte";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCompte);
			System.err.println(query);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				double debitPrelev = rs.getInt("montant");
				int datePrelev = rs.getInt("dateRecurrente");
				String beneficiaire = rs.getString("beneficiaire");
				int idPrelev = rs.getInt("idPrelev");

				alResult.add(new Prelevement(idPrelev, debitPrelev, idNumCompte, datePrelev, beneficiaire));
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.SELECT, "Erreur accès", e);
		}

		return alResult;
	}
}

