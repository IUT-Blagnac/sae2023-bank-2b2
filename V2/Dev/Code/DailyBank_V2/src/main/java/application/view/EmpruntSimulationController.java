package application.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;

import java.text.DecimalFormat;

import application.DailyBankState;
import application.control.EmpruntSimulation;
import application.control.PrelevManagement;

public class EmpruntSimulationController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à ComptesManagementController
	private EmpruntSimulation esDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private CompteCourant prelevDuCompte;

	@FXML
	private TextField txtMontant;
	@FXML
	private TextField txtDuree;
	@FXML
	private TextField txtTaux;
	@FXML
	private Button btnSimuler;
	@FXML
	private Label lblResultat;

	// Manipulation de la fenêtre

	/**
	 * Initialise le contexte du contrôleur avec les paramètres fournis.
	 * 
	 * @param _containingStage La fenêtre physique contenant la scène
	 * @param _cm              Le contrôleur de dialogue associé
	 * @param _dbstate         L'état courant de l'application
	 * @param client           Le client associé aux comptes
	 */
	public void initContext(Stage _containingStage,DailyBankState _dbstate) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}
	
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage

	/**
	 * Gère l'événement de fermeture de la fenêtre.
	 *
	 * @param e L'événement de fermeture de la fenêtre
	 * @return null
	 */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	/**
	 * Annule et ferme la fenêtre de la simulation de l'emprunt.
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	private void simulerEmprunt() {
		double montant = Double.parseDouble(txtMontant.getText());
		int duree = Integer.parseInt(txtDuree.getText());
		double taux = Double.parseDouble(txtTaux.getText());

		double mensualite = calculerMensualite(montant, duree, taux);
		double totalInterets = calculerTotalInterets(mensualite, duree, montant);

		DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
		String resultat = "Mensualité : " + decimalFormat.format(mensualite) + " €\n" + "Total des intérêts : "
				+ decimalFormat.format(totalInterets) + " €";

		lblResultat.setText(resultat);
	}

	private double calculerMensualite(double montant, int duree, double taux) {
		double tauxMensuel = taux / 100 / 12;
		int nombreMois = duree * 12;
		double mensualite = (montant * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -nombreMois));
		return mensualite;
	}

	private double calculerTotalInterets(double mensualite, int duree, double montant) {
		double totalInterets = (mensualite * duree * 12) - montant;
		return totalInterets;
	}
}
