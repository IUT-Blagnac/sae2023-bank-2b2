package application.control;
import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ComptesManagementController;
import application.view.PrelevManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Prelevement;
import model.orm.Access_BD_CompteCourant;
import model.orm.Access_BD_Prelevement;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.Table;

public class PrelevManagement {

	private Stage primaryStage;
	private PrelevManagementController pmcViewController;
	private DailyBankState dailyBankState;
	private CompteCourant prelevDesComptes;

	/**
	 * Création d'un page de gestion des comptes.<BR />
	 *
	 * @param _parentStage Fenêtre parente de LoginDialog (sur laquelle se centrer
	 *                     et être modale)
	 * @param _dbstate     Etat courant de l'application
	 * @param client le client dont on souhaite voir les comptes
	 */
	public PrelevManagement(Stage _parentStage, DailyBankState _dbstate, CompteCourant compte) {

		this.prelevDesComptes = compte;
		this.dailyBankState = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ComptesManagementController.class.getResource("comptesmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth() + 50, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des prélèvements");
			this.primaryStage.setResizable(false);

			this.pmcViewController = loader.getController();
			this.pmcViewController.initContext(this.primaryStage, this, _dbstate, compte);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Affiche la page des comptes d'un client
	 */
	public void doPrelevManagementDialog() {
		this.pmcViewController.displayDialog();
	}	

	/**
	 * Permet de récupérer tout les comptes courant d'un client
	 * 
	 * @return une liste de compte courant
	 */
	public ArrayList<Prelevement> getPrelevementDunCompte() {
		ArrayList<Prelevement> listePrelev = new ArrayList<>();

		try {
			Access_BD_Prelevement ap = new Access_BD_Prelevement();
			listePrelev = ap.getPrelevements(this.prelevDesComptes.idNumCompte);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listePrelev = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
			ed.doExceptionDialog();
			listePrelev = new ArrayList<>();
		}
		return listePrelev;
	}
}
