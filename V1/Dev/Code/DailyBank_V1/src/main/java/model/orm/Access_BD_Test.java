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
import model.orm.exception.Table;

public class Access_BD_Test {
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
}
