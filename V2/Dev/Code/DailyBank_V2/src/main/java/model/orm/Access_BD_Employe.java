package model.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.data.Employe;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

/**
 * Classe d'accès aux Employe en BD Oracle.
 *
 * @author Enzo Fournet
 */
public class Access_BD_Employe {

	public Access_BD_Employe() {
	}

	/**
	 * Recherche d'un employé par son login / mot de passe.
	 *
	 * @author Enzo Fournet
	 * @param login    login de l'employé recherché
	 * @param password mot de passe donné
	 * @return un Employe ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException La requête renvoie plus de 1 ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public Employe getEmploye(String login, String password)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {

		Employe employeTrouve;

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM Employe WHERE" + " login = ?" + " AND motPasse = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, login);
			pst.setString(2, password);

			ResultSet rs = pst.executeQuery();

			System.err.println(query);

			if (rs.next()) {
				int idEmployeTrouve = rs.getInt("idEmploye");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				String droitsAccess = rs.getString("droitsAccess");
				String loginTROUVE = rs.getString("login");
				String motPasseTROUVE = rs.getString("motPasse");
				int idAgEmploye = rs.getInt("idAg");

				employeTrouve = new Employe(idEmployeTrouve, nom, prenom, droitsAccess, loginTROUVE, motPasseTROUVE,
						idAgEmploye);
			} else {
				rs.close();
				pst.close();
				// Non trouvé
				return null;
			}

			if (rs.next()) {
				// Trouvé plus de 1 ... bizarre ...
				rs.close();
				pst.close();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return employeTrouve;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 *
	 * Cette méthode permet de récupérer un employé en fonction de son id.
	 *
	 * @author Enzo Fournet
	 * @param _idEmploye
	 * @param _debutNom
	 * @param _debutPrenom
	 * @param _droit
	 * @return
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public ArrayList<Employe> getEmployes(int _idEmploye, String _debutNom, String _debutPrenom, String _droit) throws DataAccessException, DatabaseConnexionException {
		ArrayList<Employe> alResult = new ArrayList<>();
		try {
			Connection con = LogToDatabase.getConnexion();
			boolean first = true;

			PreparedStatement pst;

			String query = "SELECT * FROM Employe";
			if (_idEmploye != -1) {
				if (first) {
					query += " where ";
					first = false;
				}
				query += " idEmploye = ? ";
				if (!_debutNom.equals("") || !_droit.equals("")) {
					query += " and ";
				}
			}
			if(!_debutNom.equals("")) {
				if (first) {
					query += " where ";
					first = false;
				}
				_debutNom = _debutNom.toUpperCase() + "%";
				_debutPrenom = _debutPrenom.toUpperCase() + "%";
				query += " UPPER(nom) like ? ";
				query += " and UPPER(prenom) like ? ";
				if (!_droit.equals("")) {
					query += " and ";
				}
			}
			if(!_droit.equals("")) {
				if (first) {
					query += " where ";
					first = false;
				}
				query += " droitsAccess = ? ";
			}
			query += " order by idEmploye";


			String un = "";
			String deux = "";
			String trois = "";
			String quatre = "";
			pst = con.prepareStatement(query);
			if(_idEmploye != -1) {
				pst.setInt(1, _idEmploye);
				un = _idEmploye + "";
				if (!_debutNom.equals("")) {
					pst.setString(2, _debutNom);
					deux = _debutNom;
					pst.setString(3, _debutPrenom);
					trois = _debutPrenom;
					if (!_droit.equals("")) {
						pst.setString(4, _droit);
						quatre = _droit;
					}
				}else if(!_droit.equals("")) {
					pst.setString(2, _droit);
					deux = _droit;
				}
			} else if(!_debutNom.equals("")) {
				pst.setString(1, _debutNom);
				un = _debutNom;
				pst.setString(2, _debutPrenom);
				deux = _debutPrenom;
				if (!_droit.equals("")) {
					pst.setString(3, _droit);
					trois = _droit;
				}
			} else if(!_droit.equals("")) {
				pst.setString(1, _droit);
				un = _droit;
			}

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idEmploye = rs.getInt("idEmploye");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				String droitsAccess = rs.getString("droitsAccess");
				String login = rs.getString("login");
				String motPasse = rs.getString("motPasse");
				int idAg = rs.getInt("idAg");
				Employe employe = new Employe(idEmploye, nom, prenom, droitsAccess, login, motPasse, idAg);
				alResult.add(employe);
			}
			rs.close();
			pst.close();
		}catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
		}
		return alResult;
	}

	/**
	 *
	 * Cette méthode permet d'insérer un employé en base de données.
	 *
	 * @author Enzo Fournet
	 * @param employe
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public void insertEmploye(Employe employe)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {

			Connection con = LogToDatabase.getConnexion();

			String query = "INSERT INTO EMPLOYE VALUES (" + "seq_id_employe.NEXTVAL" + ", " + "?" + ", " + "?" + ", "+ "?" + ", " + "?" + ", " + "?" + ", " + "?" + ")";
			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, employe.nom);
			pst.setString(2, employe.prenom);
			pst.setString(3, employe.droitsAccess);
			pst.setString(4, employe.login);
			pst.setString(5, employe.motPasse);
			pst.setInt(6, employe.idAg);


			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();

			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.INSERT,
						"Insert anormal (insert de moins ou plus d'une ligne)", null, result);
			}

			query = "SELECT seq_id_employe.CURRVAL from DUAL";

			System.err.println(query);
			PreparedStatement pst2 = con.prepareStatement(query);

			ResultSet rs = pst2.executeQuery();
			rs.next();
			int numEmplBase = rs.getInt(1);

			con.commit();
			rs.close();
			pst2.close();

			employe.idEmploye = numEmplBase;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.INSERT, "Erreur accès", e);
		}
	}

	/**
	 *
	 * Cette méthode permet de mettre à jour un employé en base de données.
	 *
	 * @author Enzo Fournet
	 * @param employe
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public void updateEmploye(Employe employe) throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE EMPLOYE SET " + "nom = " + "? , " + "prenom = " + "? , " + "droitsAccess = " + "? , " + "login = " + "? , " + "motPasse = " + "? " + "WHERE idEmploye = ? ";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, employe.nom);
			pst.setString(2, employe.prenom);
			pst.setString(3, employe.droitsAccess);
			pst.setString(4, employe.login);
			pst.setString(5, employe.motPasse);
			pst.setInt(6, employe.idEmploye);


			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.UPDATE, "Erreur accès", e);
		}
	}

	/**
	 *
	 * Cette méthode permet de supprimer un employé en base de données.
	 *
	 * @author Enzo Fournet
	 * @param employe
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public void deleteEmploye(Employe employe) throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Connection con = LogToDatabase.getConnexion();

			String query = "DELETE FROM EMPLOYE WHERE idEmploye = ? ";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, employe.idEmploye);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Employe, Order.DELETE,
						"Delete anormal (delete de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.DELETE, "Erreur accès", e);
		}
	}
}
