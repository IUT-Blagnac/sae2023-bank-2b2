package model.orm;

import java.sql.*;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

import model.data.Employe;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;

/**
 * Classe d'accès aux Employe en BD Oracle pour les tests.
 * 
 * @author Enzo Fournet
 * 
 */
public class Access_BD_Test {

    public int getSeqEmplCurrVal() throws DataAccessException, DatabaseConnexionException {
        int seqEmplGetCurrVal = 0;
        try {
            Connection con = LogToDatabase.getConnexion();
            String query = "SELECT seq_id_employe.CURRVAL  FROM DUAL";

            PreparedStatement pst = con.prepareStatement(query);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                seqEmplGetCurrVal = rs.getInt("currval");
            }

            rs.close();
            pst.close();
            return seqEmplGetCurrVal;
        } catch (SQLException e) {
            throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
        }
    }

    public int getNumberEmploye() throws DataAccessException, DatabaseConnexionException {
        int nbEmploye = 0;
        try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT count(*) as nbEmploye FROM EMPLOYE";

			PreparedStatement pst = con.prepareStatement(query);

			ResultSet rs = pst.executeQuery();

            if(rs.next()) {
			    nbEmploye = rs.getInt("nbEmploye");
			}

			rs.close();
			pst.close();
			return nbEmploye;
		} catch (SQLException e) {
			throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
		}
    }

    public ArrayList<Employe> getAllEmploye() throws DataAccessException, DatabaseConnexionException{
        ArrayList<Employe> alResult = new ArrayList<>();
        try {
            Connection con = LogToDatabase.getConnexion();
            String query = "SELECT * FROM EMPLOYE";

            PreparedStatement pst = con.prepareStatement(query);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int idEmploye = rs.getInt("IDEMPLOYE");
                String nom = rs.getString("NOM");
                String prenom = rs.getString("PRENOM");
                String droitacces = rs.getString("DROITSACCESS");
                String login = rs.getString("LOGIN");
                String password = rs.getString("MOTPASSE");
                int idAg = rs.getInt("IDAG");

                alResult.add(new Employe(idEmploye, nom, prenom, droitacces ,login, password, idAg));
            }
            rs.close();
            pst.close();
            return alResult;
        } catch (SQLException e) {
            throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
        }
    }

    public ArrayList<Employe> getAllGuichetier() throws DataAccessException, DatabaseConnexionException{
        ArrayList<Employe> alResult = new ArrayList<>();
        try {
            Connection con = LogToDatabase.getConnexion();
            String query = "SELECT * FROM EMPLOYE WHERE DROITSACCESS = 'guichetier'";

            PreparedStatement pst = con.prepareStatement(query);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int idEmploye = rs.getInt("IDEMPLOYE");
                String nom = rs.getString("NOM");
                String prenom = rs.getString("PRENOM");
                String droitacces = rs.getString("DROITSACCESS");
                String login = rs.getString("LOGIN");
                String password = rs.getString("MOTPASSE");
                int idAg = rs.getInt("IDAG");

                alResult.add(new Employe(idEmploye, nom, prenom, droitacces ,login, password, idAg));
            }
            rs.close();
            pst.close();
            return alResult;
        } catch (SQLException e) {
            throw new DataAccessException(Table.Employe, Order.SELECT, "Erreur accès", e);
        }
    }

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

    public void resestBD() throws DataAccessException, DatabaseConnexionException{
        InputStream inputStream = Access_BD_Test.class.getResourceAsStream("sql/scriptResetBase.sql");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + " ");
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Connection con = LogToDatabase.getConnexion();
        String[] inst = sb.toString().split("/ ");
        for (int i = 0; i < inst.length; i++) {
            try {
                if (!inst[i].trim().equals("")) {
                    System.out.println(">>" + inst[i]);
                    if(inst[i].contains("EXECUTE Debiter(")) {
                        System.out.println("debiter>>" + inst[i]);
                        int debit = debiter(inst[i]);
                        System.out.println("debit>>" + debit);
                    } else if(inst[i].contains("EXECUTE Debiter(")) {
                        System.out.println("crediter>>" + inst[i]);
                        int credit = crediter(inst[i]);
                        System.out.println("credit>>" + credit);

                    } else if(inst[i].contains("EXECUTE CreerOperation(")) {
                        System.out.println("creerOperation>>" + inst[i]);
                        int creerOperation = creerOperation(inst[i]);
                        System.out.println("creerOperation>>" + creerOperation);
                    } else if(inst[i].contains("EXECUTE CreerCompte(")) { 
                        System.out.println("creerCompte>>" + inst[i]);
                        int creerCompte = creerCompte(inst[i]);
                        System.out.println("creerCompte>>" + creerCompte);
                    } else if (inst[i].contains("EXECUTE Virer(")) {
                        System.out.println("virer>>" + inst[i]);
                        int virer = virer(inst[i]);
                        System.out.println("virer>>" + virer);
                    }else {
                        Statement st = con.createStatement();
                        st.executeUpdate(inst[i]);
                        st.close();
                    }                    
                }
                System.out.println(i + ">>" + inst[i]);
            } catch (SQLException e) {
                if (e.getMessage().contains("Connexion interrompue")) {
                    System.out.println("BUG DE CONNEXION - RECONNECTION - RELANCEMENT DE LA REQUETE");
                    i --;
                    con = LogToDatabase.getConnexion();
                } else if (e.getMessage().contains("Table ou vue inexistante")) {
                    System.out.println("BUG DROP INEXSIATNT");
                    //ici inutile d'afficher le message d'erreur
                } else {
                    e.printStackTrace();
                    System.out.println("*** Erreur Reset BD ***");
                    System.out.println(e.getMessage());
                    System.out.println("************************");
                }
                
                
            }
                
        }
        try {
            con.commit();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int virer(String requeteSQL) throws SQLException, DataAccessException, DatabaseConnexionException {
        //EXECUTE Virer(2, 1, TO_NUMBER('97,23'), :ret)
        requeteSQL = requeteSQL.replace("VARIABLE ret NUMBER; EXECUTE Virer(", "");
        requeteSQL = requeteSQL.replace("EXECUTE Virer(","");
        requeteSQL = requeteSQL.replace(", :ret)", "");
        System.out.println("requeteSQL>>" + requeteSQL);

        int NumCompteDeb;
        int NumCompteCred;
        float montantOp;

        String[] param = requeteSQL.split(", ");

        param[0] = param[0].replace(" ", "");
        NumCompteDeb = Integer.parseInt(param[0]);

        param[1] = param[1].replace(" ", "");
        NumCompteCred = Integer.parseInt(param[1]);

        param[2] = param[2].replace("TO_NUMBER('", "");
        param[2] = param[2].replace("')", "");
        param[2] = param[2].replace(",", ".");
        montantOp = Float.parseFloat(param[2]);

        System.out.println("param>>" + NumCompteDeb + " " + NumCompteCred + " " + montantOp);

        String procedureName = "{ CALL Virer(?, ?, ?, ?) }";
        Connection con = LogToDatabase.getConnexion();
        CallableStatement cs = con.prepareCall(procedureName);

        cs.setInt(1, NumCompteDeb);
        cs.setInt(2, NumCompteCred);
        cs.setFloat(3, montantOp);
        cs.registerOutParameter(4, Types.INTEGER);

        cs.execute();

        int returnn = cs.getInt(4);

        cs.close();
        con.close();

        return returnn;
    }

    private int creerCompte(String requeteSQL) throws SQLException, DataAccessException, DatabaseConnexionException {
        //EXECUTE CreerCompte(TO_NUMBER('-300'), TO_NUMBER('120'), 2, :ret)
        requeteSQL = requeteSQL.replace("VARIABLE ret NUMBER; EXECUTE CreerCompte(", "");
        requeteSQL = requeteSQL.replace("EXECUTE CreerCompte(","");
        requeteSQL = requeteSQL.replace(", :ret)", "");
        System.out.println("requeteSQL>>" + requeteSQL);


        float debitAutorise;
        float montantInitial;
        int idNumCli;

        String[] param = requeteSQL.split(", ");

        param[0] = param[0].replace("TO_NUMBER('", "");
        param[0] = param[0].replace("')", "");
        param[0] = param[0].replace(",", ".");
        debitAutorise = Float.parseFloat(param[0]);

        param[1] = param[1].replace("TO_NUMBER('", "");
        param[1] = param[1].replace("')", "");
        param[1] = param[1].replace(",", ".");
        montantInitial = Float.parseFloat(param[1]);

        param[2] = param[2].replace(" ", "");
        idNumCli = Integer.parseInt(param[2]);

        System.out.println("param>>" + debitAutorise + " " + montantInitial + " " + idNumCli);

        String procedureName = "{ CALL CreerCompte(?, ?, ?, ?) }";
        Connection con = LogToDatabase.getConnexion();
        CallableStatement cstmt = con.prepareCall(procedureName);

        cstmt.setFloat(1, debitAutorise);
        cstmt.setFloat(2, montantInitial);
        cstmt.setInt(3, idNumCli);
        cstmt.registerOutParameter(4, Types.INTEGER);

        cstmt.execute();

        int returnn = cstmt.getInt(4);

        cstmt.close();
        con.close();

        return returnn;
    }

    private int creerOperation(String requeteSQL) throws SQLException, DataAccessException, DatabaseConnexionException {
        //EXECUTE CreerOperation(1, TO_NUMBER('-25'), 'Retrait Espèces', :ret)
        requeteSQL = requeteSQL.replace("VARIABLE ret NUMBER; EXECUTE CreerOperation(", "");
        requeteSQL = requeteSQL.replace("EXECUTE CreerOperation(","");
        requeteSQL = requeteSQL.replace(", :ret)", "");
        System.out.println("requeteSQL>>" + requeteSQL);

        int idNumCompte;
        float montantOp;
        String typeOperation;

        String[] param = requeteSQL.split(", ");
        param[0] = param[0].replace(" ", "");
        idNumCompte = Integer.parseInt(param[0]);

        param[1] = param[1].replace("TO_NUMBER('", "");
        param[1] = param[1].replace("')", "");
        param[1] = param[1].replace(",", ".");
        montantOp = Float.parseFloat(param[1]);

        param[2] = param[2].replace("'", "");
        typeOperation = param[2];

        System.out.println("param>>" + idNumCompte + " " + montantOp + " " + typeOperation);

        String procedureName = "{ CALL CreerOperation(?, ?, ?, ?) }";
        Connection con = LogToDatabase.getConnexion();
        CallableStatement cstmt = con.prepareCall(procedureName);

        cstmt.setInt(1, idNumCompte);
        cstmt.setFloat(2, montantOp);
        cstmt.setString(3, typeOperation);
        cstmt.registerOutParameter(4, Types.INTEGER);

        cstmt.execute();

        int returnn = cstmt.getInt(4);

        cstmt.close();
        con.close();

        return returnn;
    }

    private int crediter(String requeteSQL) throws SQLException, DataAccessException, DatabaseConnexionException {
        //EXECUTE Crediter(1, TO_NUMBER('15,2'), 'Retrait Espèces', :ret);
        requeteSQL = requeteSQL.replace("VARIABLE ret NUMBER; EXECUTE Crediter(", "");
        requeteSQL = requeteSQL.replace("EXECUTE Crediter(","");
        requeteSQL = requeteSQL.replace(", :ret)", "");
        System.out.println("requeteSQL>>" + requeteSQL);

        int idNumCompte;
        float montant;
        String typeOperation;

        String[] param = requeteSQL.split(", ");
        param[0] = param[0].replace(" ", "");
        System.out.println("param>>" + param[0] + " " + param[1] + " " + param[2]);
        idNumCompte = Integer.parseInt(param[0]);
        System.out.println("idNumCompte>>" + idNumCompte);
        param[1] = param[1].replace("TO_NUMBER('", "");
        param[1] = param[1].replace("')", "");
        param[1] = param[1].replace(",", ".");
        montant = Float.parseFloat(param[1]);
        System.out.println("montant>>" + montant);
        typeOperation = param[2].replaceAll("'", "");
        System.out.println("typeOperation>>" + typeOperation);

        String procedureName = "{ CALL CreerOperation(?, ?, ?, ?) }";
        Connection con = LogToDatabase.getConnexion();
        CallableStatement cstmt = con.prepareCall(procedureName);
        cstmt.setInt(1, idNumCompte);
        cstmt.setFloat(2, montant);
        cstmt.setString(3, typeOperation);
        cstmt.registerOutParameter(4, Types.INTEGER);
        cstmt.execute();
        int nb = cstmt.getInt(4);
        cstmt.close();
        con.close();
        return nb;
    }

    public int debiter(String requeteSQL) throws SQLException, DataAccessException, DatabaseConnexionException {
        //EXECUTE Debiter(1, TO_NUMBER('15,2'), 'Retrait Espèces', :ret);
        requeteSQL = requeteSQL.replace("VARIABLE ret NUMBER; EXECUTE Debiter(", "");
        requeteSQL = requeteSQL.replace(", :ret)", "");
        System.out.println("requeteSQL>>" + requeteSQL);

        int idNumCompte;
        float montant;
        String typeOperation;

        String[] param = requeteSQL.split(", ");
        param[0] = param[0].replace(" ", "");
        System.out.println("param>>" + param[0] + " " + param[1] + " " + param[2]);
        idNumCompte = Integer.parseInt(param[0].replace("EXECUTEDebiter(",""));
        System.out.println("idNumCompte>>" + idNumCompte);
        param[1] = param[1].replace("TO_NUMBER('", "");
        param[1] = param[1].replace("')", "");
        param[1] = param[1].replace(",", ".");
        montant = Float.parseFloat(param[1]);
        System.out.println("montant>>" + montant);
        typeOperation = param[2].replaceAll("'", "");
        System.out.println("typeOperation>>" + typeOperation);

        String procedureName = "{ CALL Debiter(?, ?, ?, ?) }";
        Connection con = LogToDatabase.getConnexion();
        CallableStatement stmt = con.prepareCall(procedureName);
            
        // Passer les paramètres IN à la procédure stockée
        stmt.setInt(1, idNumCompte);
        stmt.setFloat(2, montant);
        stmt.setString(3, typeOperation);
            
        // Enregistrer le paramètre OUT de la procédure stockée
        stmt.registerOutParameter(4, java.sql.Types.NUMERIC);
    
        // Exécuter la procédure stockée
        stmt.execute();
            
        // Récupérer la valeur du paramètre OUT
        int result = stmt.getInt(4);
            
        stmt.close();
        con.close();

        System.out.println("result>>" + result);
        return result;
    }
    
}
