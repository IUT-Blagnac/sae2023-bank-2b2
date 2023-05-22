package application.model.data;

import java.sql.Date;

public class Prelevement {

	public int idNumPrelev;
	public double debitPrelev;
	public int idNumCompte;
	public int datePrelev;
	public String beneficiaire;

	public Prelevement(int idNumPrelev, double debitPrelev, int idNumCompte, int datePrelev,String beneficiaire) {
		super();
		this.idNumPrelev = idNumPrelev;
		this.debitPrelev = debitPrelev;
		this.idNumCompte = idNumCompte;
		this.datePrelev = datePrelev;
		this.beneficiaire = beneficiaire;
	}

	public Prelevement(Prelevement prelev) {
		this(prelev.idNumPrelev, prelev.debitPrelev, prelev.idNumCompte, prelev.datePrelev,prelev.beneficiaire);
	}

	@Override
	public String toString() {
		String s = "" + String.format("%05d", this.idNumPrelev) + " : montant prélèvement =" + String.format("%12.02f", this.debitPrelev)
				+ "  ,  date de prélèvement =" + this.datePrelev + ", beneficiaire : " + this.beneficiaire + " ,idNumCompte : " + String.format("%05d", this.idNumCompte);
		return s;
	}

	
}
