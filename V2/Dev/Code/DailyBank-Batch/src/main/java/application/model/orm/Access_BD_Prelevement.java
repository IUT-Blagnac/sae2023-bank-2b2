package application.model.orm;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import application.model.data.CompteCourant;
import application.model.data.Prelevement;
import application.model.orm.exception.DataAccessException;
import application.model.orm.exception.DatabaseConnexionException;
import application.model.orm.exception.ManagementRuleViolation;
import application.model.orm.exception.Order;
import application.model.orm.exception.RowNotFoundOrTooManyRowsException;
import application.model.orm.exception.Table;

/**
 *
 * Classe d'accès aux CompteCourant en BD Oracle.
 *
 */
public class Access_BD_Prelevement {

	public Access_BD_Prelevement() {
	}

	/**
	 * Recherche des CompteCourant d'un client à partir de son id.
	 *
	 * @param idNumCli id du client dont on cherche les comptes
	 * @return Tous les CompteCourant de idNumCli (ou liste vide)
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

	/**
	 * Recherche d'un CompteCourant à partir de son id (idNumCompte).
	 *
	 * @param idNumCompte id du compte (clé primaire)
	 * @return Le compte ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException La requête renvoie plus de 1 ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public Prelevement getPrelevement(int idNumPrelev)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Prelevement pr;

			Connection con = LogToDatabase.getConnexion();

			String query = "SELECT * FROM PRELEVEMENTAUTOMATIQUE where" + " idPrelev = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumPrelev);

			System.err.println(query);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				int idNumPrelevTROUVE = rs.getInt("idPrelev");
				double debitPrelev = rs.getInt("montant");
				int datePrelev = rs.getInt("dateRecurrente");
				String beneficiaire = rs.getString("beneficiaire");
				int idNumCompte = rs.getInt("idNumCompte");

				pr = new Prelevement(idNumPrelevTROUVE, debitPrelev, idNumCompte, datePrelev, beneficiaire);
			} else {
				rs.close();
				pst.close();
				return null;
			}

			if (rs.next()) {
				throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return pr;
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.SELECT, "Erreur accès", e);
		}
	}
	
	/**
	 * Insertion d'un compte.
	 *
	 * @param compte IN/OUT Tous les attributs IN sauf idNumCompte en OUT
	 * @throws RowNotFoundOrTooManyRowsException La requête insère 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public void insertPrelevement(Prelevement prelev)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {

			Connection con = LogToDatabase.getConnexion();

			String query = "INSERT INTO PRELEVEMENTAUTOMATIQUE VALUES (" + "seq_id_prelevAuto.NEXTVAL" + ", " + "?" + ", " + "?" + ", "
					+ "?" + ", " + "?" + ")";
			PreparedStatement pst = con.prepareStatement(query);
			pst.setDouble(1, prelev.debitPrelev);
			pst.setInt(2,prelev.datePrelev);
			pst.setString(3, "" + prelev.beneficiaire);
			pst.setInt(4, prelev.idNumCompte);
			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();

			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.INSERT,
						"Insert anormal (insert de moins ou plus d'une ligne)", null, result);
			}

			query = "SELECT seq_id_prelevAuto.CURRVAL from DUAL";

			System.err.println(query);
			PreparedStatement pst4 = con.prepareStatement(query);

			ResultSet rs = pst4.executeQuery();
			rs.next();
			int numPrelevBase = rs.getInt(1);

			con.commit();
			rs.close();
			pst4.close();

			prelev.idNumPrelev = numPrelevBase;
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.INSERT, "Erreur accès", e);
		}
	}
	

	/**
	 * Suppression d'un CompteCourant.
	 *
	 * cc.idNumCompte (clé primaire) doit exister seul, le compte n'est pas supprimé mais seulement désactivé 
	 * empêchant alors les opérations
	 * cc.idNumCli non mis à jour (un cc ne change pas de client)
	 *
	 * @param cc IN cc.idNumCompte (clé primaire) doit exister seul
	 * @throws RowNotFoundOrTooManyRowsException La requête modifie 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 * @throws ManagementRuleViolation           Erreur sur le solde courant par
	 *                                           rapport au débitAutorisé (solde <
	 *                                           débitAutorisé)
	 */
	public void deletePrelevement(Prelevement prelev) throws RowNotFoundOrTooManyRowsException, DataAccessException,
			DatabaseConnexionException, ManagementRuleViolation {
		try {

			Connection con = LogToDatabase.getConnexion();

			String query = "DELETE FROM PrelevementAutomatique WHERE idPrelev = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, prelev.idNumPrelev);
			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.DELETE,
						"Delete anormal (delete de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.DELETE, "Erreur accès", e);
		}
	}

	/**
	 * Mise à jour d'un CompteCourant.
	 *
	 * cc.idNumCompte (clé primaire) doit exister seul cc.debitAutorise est mis à
	 * jour cc.solde non mis à jour (ne peut se faire que par une opération)
	 * cc.idNumCli non mis à jour (un cc ne change pas de client)
	 *
	 * @param cc IN cc.idNumCompte (clé primaire) doit exister seul
	 * @throws RowNotFoundOrTooManyRowsException La requête modifie 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 * @throws ManagementRuleViolation           Erreur sur le solde courant par
	 *                                           rapport au débitAutorisé (solde <
	 *                                           débitAutorisé)
	 */
	public void updatePrelevement(Prelevement prelev) throws RowNotFoundOrTooManyRowsException, DataAccessException,
			DatabaseConnexionException, ManagementRuleViolation {
		try {

			if (prelev.debitPrelev <= 0) {
				throw new ManagementRuleViolation(Table.PrelevementAutomatique, Order.UPDATE,
						"Erreur de règle de gestion : impossible de faire un prélèvement négatif ou égal à 0", null);
			}
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE PrelevementAutomatique SET " + "montant = ?, dateRecurrente = ?, beneficiaire = ? " + "WHERE idPrelev = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setDouble(1, prelev.debitPrelev);
			pst.setInt(2, prelev.datePrelev);
			pst.setString(3, ""+prelev.beneficiaire);
			pst.setInt(4, prelev.idNumPrelev);
			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.PrelevementAutomatique, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.PrelevementAutomatique, Order.UPDATE, "Erreur accès", e);
		}
	}
}

