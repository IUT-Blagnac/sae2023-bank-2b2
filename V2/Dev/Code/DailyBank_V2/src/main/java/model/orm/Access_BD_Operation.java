package model.orm;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import application.DailyBankState;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

/**
 * Classe d'accès aux Operation en BD Oracle.
 */
public class Access_BD_Operation {

	public Access_BD_Operation() {
	}

	/**
	 * Recherche de toutes les opérations d'un compte.
	 *
	 * @author Enzo Fournet
	 * 
	 * @param idNumCompte id du compte dont on cherche toutes les opérations
	 * @param month       mois de l'opération
	 * @param year        année de l'opération
	 * 
	 * @return Toutes les opérations du compte au mos et à l'année donnée, liste vide si pas d'opération.
	 * @throws DataAccessException        Erreur d'accès aux données (requête mal
	 *                                    formée ou autre)
	 * @throws DatabaseConnexionException Erreur de connexion
	 */
	public ArrayList<Operation> getOperations(int idNumCompte, String month, String year) throws DataAccessException, DatabaseConnexionException {
		DateTimeFormatter parser = new DateTimeFormatterBuilder()
    		.parseCaseInsensitive()
    		.appendPattern("MMMM")
    		.toFormatter(Locale.FRENCH);
		ArrayList<Operation> alResult = new ArrayList<>();

		if (month.isEmpty() || year.isEmpty()) {
			return alResult;
		}
		
		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM Operation where idNumCompte = ? AND EXTRACT(MONTH FROM dateOp) = ? AND EXTRACT(YEAR FROM dateOp) = ?";
			query += " ORDER BY dateOp, idOperation";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCompte);
			pst.setInt(2, Month.from(parser.parse(month)).getValue());
			System.out.println(Month.from(parser.parse(month)).getValue());
			pst.setInt(3, Integer.parseInt(year));


			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idOperation = rs.getInt("idOperation");
				double montant = rs.getDouble("montant");
				Date dateOp = rs.getDate("dateOp");
				Date dateValeur = rs.getDate("dateValeur");
				int idNumCompteTrouve = rs.getInt("idNumCompte");
				String idTypeOp = rs.getString("idTypeOp");

				alResult.add(new Operation(idOperation, montant, dateOp, dateValeur, idNumCompteTrouve, idTypeOp));
			}
			rs.close();
			pst.close();
			return alResult;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 * Recherche de toutes les opérations d'un compte.
	 *
	 * @author Enzo Fournet
	 * 
	 * @param idNumCompte id du compte dont on cherche toutes les opérations
	 * 
	 * @return Toutes les opérations du compte, liste vide si pas d'opération.
	 * @throws DataAccessException        Erreur d'accès aux données (requête mal
	 *                                    formée ou autre)
	 * @throws DatabaseConnexionException Erreur de connexion
	 */
	public ArrayList<Operation> getOperations(int idNumCompte) throws DataAccessException, DatabaseConnexionException {

		ArrayList<Operation> alResult = new ArrayList<>();
		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM Operation where idNumCompte = ?";
			query += " ORDER BY dateOp, idOperation";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCompte);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idOperation = rs.getInt("idOperation");
				double montant = rs.getDouble("montant");
				Date dateOp = rs.getDate("dateOp");
				Date dateValeur = rs.getDate("dateValeur");
				int idNumCompteTrouve = rs.getInt("idNumCompte");
				String idTypeOp = rs.getString("idTypeOp");

				alResult.add(new Operation(idOperation, montant, dateOp, dateValeur, idNumCompteTrouve, idTypeOp));
			}
			rs.close();
			pst.close();
			return alResult;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 * Recherche d'une opération par son id.
	 *
	 * @param idOperation id de l'opération recherchée (clé primaire)
	 * @return une Operation ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException La requête renvoie plus de 1 ligne
	 * @throws DataAccessException               Erreur d'accès aux données (requête
	 *                                           mal formée ou autre)
	 * @throws DatabaseConnexionException        Erreur de connexion
	 */
	public Operation getOperation(int idOperation)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {

		Operation operationTrouvee;

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM Operation  where" + " idOperation = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idOperation);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				int idOperationTrouve = rs.getInt("idOperation");
				double montant = rs.getDouble("montant");
				Date dateOp = rs.getDate("dateOp");
				Date dateValeur = rs.getDate("dateValeur");
				int idNumCompteTrouve = rs.getInt("idNumCompte");
				String idTypeOp = rs.getString("idTypeOp");

				operationTrouvee = new Operation(idOperationTrouve, montant, dateOp, dateValeur, idNumCompteTrouve,
						idTypeOp);
			} else {
				rs.close();
				pst.close();
				return null;
			}

			if (rs.next()) {
				rs.close();
				pst.close();
				throw new RowNotFoundOrTooManyRowsException(Table.Operation, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return operationTrouvee;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.SELECT, "Erreur accès", e);
		}
	}

	/**
	 * Enregistrement d'un débit.
	 *
	 * Se fait par procédure stockée : - Vérifie que le débitAutorisé n'est pas
	 * dépassé <BR />
	 * - Enregistre l'opération <BR />
	 * - Met à jour le solde du compte. <BR />
	 *
	 * @param idNumCompte compte débité
	 * @param montant     montant débité
	 * @param typeOp      libellé de l'opération effectuée (cf TypeOperation)
	 * @throws DataAccessException        Erreur d'accès aux données (requête mal
	 *                                    formée ou autre)
	 * @throws DatabaseConnexionException Erreur de connexion
	 * @throws ManagementRuleViolation    Si dépassement découvert autorisé
	 */
	public void insertDebit(CompteCourant compte, double montant, String typeOp,DailyBankState dailyBankState)
			throws DatabaseConnexionException, ManagementRuleViolation, DataAccessException {
		try {
			Connection con = LogToDatabase.getConnexion();
			CallableStatement call;

			String q = "{call Debiter (?, ?, ?, ?)}";
			// les ? correspondent aux paramètres : cf. déf procédure (4 paramètres)
			call = con.prepareCall(q);
			// Paramètres in
			call.setInt(1, compte.idNumCompte);
			// 1 -> valeur du premier paramètre, cf. déf procédure
			call.setDouble(2, montant);
			call.setString(3, typeOp);
			// Paramètres out
			call.registerOutParameter(4, java.sql.Types.INTEGER);
			// 4 type du quatrième paramètre qui est déclaré en OUT, cf. déf procédure

			call.execute();

			int res = call.getInt(4);
			if (res != 0 && !dailyBankState.isChefDAgence()) { // Erreur applicative
				throw new ManagementRuleViolation(Table.Operation, Order.INSERT,
						"Erreur de règle de gestion : découvert autorisé dépassé", null);
			}
			else if (dailyBankState.isChefDAgence() && res !=0)  {
				String query = "INSERT INTO Operation VALUES (" + "seq_id_operation.NEXTVAL" + ", " + "?" + ", " + "?" + ", "
						+ "?" + ", " + "?" + "," +"?" + ")";
				PreparedStatement pst = con.prepareStatement(query);
				pst.setDouble(1, montant);
				pst.setDate(2, Date.valueOf(LocalDate.now()));
				pst.setDate(3,Date.valueOf(LocalDate.now().plusDays(2)));
				pst.setInt(4, compte.idNumCompte);
				pst.setString(5,typeOp);
				System.err.println(query);
				int result = pst.executeUpdate();
				pst.close();
				if (result != 1) {
					con.rollback();
					throw new RowNotFoundOrTooManyRowsException(Table.Operation, Order.INSERT,
							"Insert anormal (insert de moins ou plus d'une ligne)", null, result);
				}

				query = "SELECT seq_id_operation.CURRVAL from DUAL";
				System.err.println(query);
				PreparedStatement pst4 = con.prepareStatement(query);
				ResultSet rs = pst4.executeQuery();
				rs.next();
				String queryy = "UPDATE CompteCourant SET " + "solde = ? " + "WHERE idNumCompte = ?";

				PreparedStatement pstt = con.prepareStatement(queryy);
				pstt.setDouble(1, compte.solde - montant);
				pstt.setInt(2, compte.idNumCompte);
				System.err.println(queryy);

				int result1 = pstt.executeUpdate();
				pstt.close();
				if (result1 != 1) {
					con.rollback();
					throw new RowNotFoundOrTooManyRowsException(Table.CompteCourant, Order.UPDATE,
							"Update anormal (update de moins ou plus d'une ligne)", null, result1);
				}
				
				con.commit();
				rs.close();
				pst4.close();
			}
			
		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.INSERT, "Erreur accès", e);
		} catch (RowNotFoundOrTooManyRowsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertCredit(int idNumCompte, double montant, String typeOp)
			throws DatabaseConnexionException, ManagementRuleViolation, DataAccessException {
		try {
			Connection con = LogToDatabase.getConnexion();
			CallableStatement call;

			String q = "{call Crediter (?, ?, ?, ?)}";
			// les ? correspondent aux paramètres : cf. déf procédure (4 paramètres)
			call = con.prepareCall(q);
			// Paramètres in
			call.setInt(1, idNumCompte);
			// 1 -> valeur du premier paramètre, cf. déf procédure
			call.setDouble(2, montant);
			call.setString(3, typeOp);
			// Paramètres out
			call.registerOutParameter(4, java.sql.Types.INTEGER);
			// 4 type du quatrième paramètre qui est déclaré en OUT, cf. déf procédure

			call.execute();

		} catch (SQLException e) {
			throw new DataAccessException(Table.Operation, Order.INSERT, "Erreur accès", e);
		}
	}
	
	public void insertVirement(CompteCourant compte, int idNumCompteDest, double montant, String typeOp, DailyBankState dailyBankState)
	        throws DatabaseConnexionException, ManagementRuleViolation, DataAccessException {
	    try {
	        Access_BD_Operation ao = new Access_BD_Operation();
	        
	        // Débiter le compte source
	        ao.insertDebit(compte, montant, typeOp,dailyBankState);
	        
	        // Créditer le compte destination
	        ao.insertCredit(idNumCompteDest, montant, typeOp);
	        
	    } catch (DatabaseConnexionException e) {
	        throw e;
	    } catch (ManagementRuleViolation | DataAccessException e) {
	        throw e;
	    }
	}



	/*
	 * Fonction utilitaire qui retourne un ordre sql "to_date" pour mettre une date
	 * dans une requête sql
	 *
	 * @param d Date (java.sql) à transformer
	 *
	 * @return Une chaine : TO_DATE ('j/m/a', 'DD/MM/YYYY') 'j/m/a' : jour mois an
	 * de d ex : TO_DATE ('25/01/2019', 'DD/MM/YYYY')
	 */
	private String dateToString(Date d) {
		String sd;
		Calendar cal;
		cal = Calendar.getInstance();
		cal.setTime(d);
		sd = "" + cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
		sd = "TO_DATE( '" + sd + "' , 'DD/MM/YYYY')";
		return sd;
	}

}
