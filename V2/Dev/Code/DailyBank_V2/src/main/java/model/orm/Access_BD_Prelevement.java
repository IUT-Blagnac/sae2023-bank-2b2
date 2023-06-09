package model.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.data.Prelevement;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

/*
 *
 * Classe d'accès aux Prelevement en BD Oracle.
 *
 */
public class Access_BD_Prelevement {

	public Access_BD_Prelevement() {
	}

	/**
	 * Recherche des prelevements d'un compte à partir de son id.
	 *
	 * @param idNumCompte id du compte dont on cherche les prelevements
	 * @return Tous les Prelevements de idNumCompte (ou liste vide)
	 * @throws DataAccessException        Erreur d'accès aux données (requête mal
	 *                                    formée ou autre)
	 * @throws DatabaseConnexionException Erreur de connexion
	 * @author yannis gibert
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
	 * Recherche d'un Prelevement à partir de son id (idNumPrelev).
	 *
	 * @param idNumPrelev id du prelevement (clé primaire)
	 * @return Le prelevement ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException La requête renvoie plus de 1 ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 * @author yannis gibert
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
	 * Insertion d'un prelevement.
	 *
	 * @param prelev IN prelev.idNumCompte doit exister
	 * @throws RowNotFoundOrTooManyRowsException La requête insère 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 * @author yannis gibert
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
	 * Suppression d'un Prelevement.
	 *
	 * prelev.idPrelev (clé primaire) doit exister seul
	 * stoppant alors le prélèvement automatique
	 *
	 * @param prelev IN prelev.idPrelev (clé primaire) doit exister seul
	 * @throws RowNotFoundOrTooManyRowsException La requête modifie 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 * @author yannis gibert
	 */
	public void deletePrelevement(Prelevement prelev) throws RowNotFoundOrTooManyRowsException, DataAccessException,
			DatabaseConnexionException{
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
	 * modification d'un Prelevement.
	 *
	 * prelev.idPrelev (clé primaire) doit exister seul
	 *
	 * @param prelev IN prelev.idPrelev (clé primaire) doit exister seul
	 * @throws RowNotFoundOrTooManyRowsException La requête modifie 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 * @author yannis gibert
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

