package model.data;

/*
 * Attributs mis en public car cette classe ne fait que "véhiculer" des données.
 */

public class Employe {

	public int idEmploye;
	public String nom, prenom, droitsAccess;
	public String login, motPasse;

	public int idAg;

	public Employe(int idEmploye, String nom, String prenom, String droitsAccess, String login, String motPasse, int idAg) {
		super();
		this.idEmploye = idEmploye;
		this.nom = nom;
		this.prenom = prenom;
		this.droitsAccess = droitsAccess;
		this.login = login;
		this.motPasse = motPasse;
		this.idAg = idAg;
	}

	public Employe(Employe e) {
		this(e.idEmploye, e.nom, e.prenom, e.droitsAccess, e.login, e.motPasse, e.idAg);
	}

	public Employe() {
		this(-1000, null, null, null, null, null, -1000);
	}

	/**
	 * Nouveau toString() pour afficher les employés dans la liste déroulante correctement.
	 * @author Enzo Fournet
	 */
	@Override
	public String toString() {
		return "[" + this.idEmploye + "]  " + this.nom.toUpperCase() + " " + this.prenom + "(" + this.login + ")  {"
				+ this.droitsAccess + "}";
	}

}
