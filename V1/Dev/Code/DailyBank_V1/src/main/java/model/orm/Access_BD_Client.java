package model.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.data.Client;
import model.data.Employe;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

/**
 * Classe d'accès aux Client en BD Oracle.
 */
public class Access_BD_Client {

	public Access_BD_Client() {
	}

	/**
	 * Recherche des clients paramétrée (tous/un seul par id/par nom-prénom).
	 *
	 * On recherche : <BR/>
	 * - un client précis si idNumCli <> -1 <BR />
	 * - des clients par début nom/prénom si debutNom donné <BR />
	 * - tous les clients de idAg sinon <BR/>
	 *
	 * @param idAg        : id de l'agence dont on cherche les clients
	 * @param idNumCli    : vaut -1 si il n'est pas spécifié sinon numéro recherché
	 * @param debutNom    : vaut "" si il n'est pas spécifié sinon sera le
	 *                    nom/prenom recherchés
	 * @param debutPrenom cf. @param debutNom
	 * @return Le ou les clients recherchés, liste vide si non trouvé
	 * @throws DataAccessException        Erreur d'accès aux données (requête mal
	 *                                    formée ou autre)
	 * @throws DatabaseConnexionException Erreur de connexion
	 */
	public ArrayList<Client> getClients(int idAg, int idNumCli, String debutNom, String debutPrenom) throws DataAccessException, DatabaseConnexionException {

		ArrayList<Client> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();

			PreparedStatement pst;

			String query;
			if (idNumCli != -1) {
				query = "SELECT * FROM Client where idAg = ?";
				query += " AND idNumCli = ?";
				query += " ORDER BY nom";
				pst = con.prepareStatement(query);
				pst.setInt(1, idAg);
				pst.setInt(2, idNumCli);

			} else if (!debutNom.equals("")) {
				debutNom = debutNom.toUpperCase() + "%";
				debutPrenom = debutPrenom.toUpperCase() + "%";
				query = "SELECT * FROM Client where idAg = ?";
				query += " AND UPPER(nom) like ?" + " AND UPPER(prenom) like ?";
				query += " ORDER BY nom";
				pst = con.prepareStatement(query);
				pst.setInt(1, idAg);
				pst.setString(2, debutNom);
				pst.setString(3, debutPrenom);
			} else {
				query = "SELECT * FROM Client where idAg = ?";
				query += " ORDER BY nom";
				pst = con.prepareStatement(query);
				pst.setInt(1, idAg);
			}
			System.err.println(query + " nom : " + debutNom + " prenom : " + debutPrenom + "#");

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idNumCliTR = rs.getInt("idNumCli");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				String adressePostale = rs.getString("adressePostale");
				adressePostale = (adressePostale == null ? "" : adressePostale);
				String email = rs.getString("email");
				email = (email == null ? "" : email);
				String telephone = rs.getString("telephone");
				telephone = (telephone == null ? "" : telephone);
				String estInactif = rs.getString("estInactif");
				int idAgCli = rs.getInt("idAg");

				alResult.add(
						new Client(idNumCliTR, nom, prenom, adressePostale, email, telephone, estInactif, idAgCli));
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.SELECT, "Erreur accès", e);
		}

		return alResult;
	}

	/**
	 * Recherche de client par son id.
	 *
	 * @return un Client ou null si non trouvé
	 * @param idCli id du client recherché (clé primaire)
	 * @throws RowNotFoundOrTooManyRowsException La requête renvoie plus de 1 ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public Client getClient(int idCli)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {

		Client clientTrouve;

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM Client where" + " idNumCli = ?";
			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idCli);
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				// Trouvé ...
				int idNumCli = rs.getInt("idNumCli");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				String adressePostale = rs.getString("adressePostale");
				adressePostale = (adressePostale == null ? "" : adressePostale);
				String email = rs.getString("email");
				email = (email == null ? "" : email);
				String telephone = rs.getString("telephone");
				telephone = (telephone == null ? "" : telephone);
				String estInactif = rs.getString("estInactif");
				int idAgCli = rs.getInt("idAg");

				clientTrouve = new Client(idNumCli, nom, prenom, adressePostale, email, telephone, estInactif, idAgCli);
			} else {
				// Non trouvé ...
				rs.close();
				pst.close();
				return null;
			}

			if (rs.next()) {
				// Plus de 2 ? Bizarre ...
				rs.close();
				pst.close();
				throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return clientTrouve;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 * Insertion d'un client.
	 *
	 * @param client IN/OUT Tous les attributs IN sauf idNumCli en OUT
	 * @throws RowNotFoundOrTooManyRowsException La requête insère 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public void insertClient(Client client)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {

			Connection con = LogToDatabase.getConnexion();

			String query = "INSERT INTO CLIENT VALUES (" + "seq_id_client.NEXTVAL" + ", " + "?" + ", " + "?" + ", "
					+ "?" + ", " + "?" + ", " + "?" + ", " + "?" + ", " + "?" + ")";
			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, client.nom);
			pst.setString(2, client.prenom);
			pst.setString(3, client.adressePostale);
			pst.setString(4, client.email);
			pst.setString(5, client.telephone);
			pst.setString(6, "" + client.estInactif.charAt(0));
			pst.setInt(7, client.idAg);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();

			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.INSERT,
						"Insert anormal (insert de moins ou plus d'une ligne)", null, result);
			}

			query = "SELECT seq_id_client.CURRVAL from DUAL";

			System.err.println(query);
			PreparedStatement pst2 = con.prepareStatement(query);

			ResultSet rs = pst2.executeQuery();
			rs.next();
			int numCliBase = rs.getInt(1);

			con.commit();
			rs.close();
			pst2.close();

			client.idNumCli = numCliBase;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.INSERT, "Erreur accès", e);
		}
	}

	/**
	 * Mise à jour d'un Client.
	 *
	 * client.idNumCli est la clé primaire et doit exister tous les autres champs
	 * sont des mises à jour. client.idAg non mis à jour (un client ne change
	 * d'agence que par delete/insert)
	 *
	 * @param client IN client.idNumCli (clé primaire) doit exister
	 * @throws RowNotFoundOrTooManyRowsException La requête modifie 0 ou plus de 1
	 *                                           ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public void updateClient(Client client)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE CLIENT SET " + "nom = " + "? , " + "prenom = " + "? , " + "adressePostale = "
					+ "? , " + "email = " + "? , " + "telephone = " + "? , " + "estInactif = " + "? " + " "
					+ "WHERE idNumCli = ? ";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setString(1, client.nom);
			pst.setString(2, client.prenom);
			pst.setString(3, client.adressePostale);
			pst.setString(4, client.email);
			pst.setString(5, client.telephone);
			pst.setString(6, "" + client.estInactif.charAt(0));
			pst.setInt(7, client.idNumCli);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.UPDATE, "Erreur accès", e);
		}
	}

	public ArrayList<Employe> getEmployes(int _idEmploye, String _debutNom, String _debutPrenom, String _droit) throws DataAccessException, DatabaseConnexionException {
		ArrayList<Employe> alResult = new ArrayList<Employe>();
		try {
			Connection con = LogToDatabase.getConnexion();
			Boolean first = true;

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
			System.out.println(query);


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

			System.out.println("un =" + un + " deux =" + deux + " trois =" + trois + " quatre =" + quatre);
			
		

			System.out.println("coucou");
			System.out.println(query);
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
				System.out.println(employe);
				alResult.add(employe);
			}
			rs.close();
			pst.close();
		}catch (SQLException e) {
			throw new DataAccessException(Table.Client, Order.SELECT, "Erreur accès", e);
		}
		return alResult;
	}

	public ArrayList<Employe> getAllEmployes() throws DataAccessException, DatabaseConnexionException {
		ArrayList<Employe> employes = new ArrayList<Employe>();
		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM EMPLOYE";
			PreparedStatement pst = con.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idEmploye = rs.getInt("idEmploye");
				String nom = rs.getString("nom");
				String prenom = rs.getString("prenom");
				String droitsAccess = rs.getString("droitsAccess");
				String login = rs.getString("login");
				String motPasse = rs.getString("motPasse");
				int idAg = rs.getInt("idAg");

				Employe employe = new Employe(idEmploye,nom, prenom, droitsAccess, login, motPasse, idAg);
				employes.add(employe);
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(employes.size());
		return employes;
	}
}
