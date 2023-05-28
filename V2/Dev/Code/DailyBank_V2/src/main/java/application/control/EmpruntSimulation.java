/*
 * @author Julien Couderc
 */

package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.StageManagement;
import application.view.EmpruntSimulationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;

public class EmpruntSimulation {

	private Stage primaryStage;
	private EmpruntSimulationController esViewController;

	/**
	 * @author Julien Couderc
	 * 
	 * Constructeur de la classe de SimulerAssuranceEditorPane. Cette classe permet
	 * de gérer l'affichage d'un dialogue de gestion des comptes.
	 *
	 * @param _parentStage Fenêtre parente de LoginDialog (sur laquelle se centrer
	 *                     et être modale)
	 * @param _dbstate     Etat courant de l'application
	 * 
	 * @param client	   Client courant
	 *
	 * @author Julien Couderc
	 */
	public EmpruntSimulation(Stage _parentStage, DailyBankState _dbstate, Client client) {

		try {
			FXMLLoader loader = new FXMLLoader(EmpruntSimulationController.class.getResource("emprunt.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth() + 20, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un compte");
			this.primaryStage.setResizable(false);

			this.esViewController = loader.getController();
			this.esViewController.initContext(this.primaryStage, _dbstate, client);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author Julien Couderc
	 * 
	 * Cette méthode permet d'afficher le dialogue de la simulation d'un emprunt
	 *
	 * @return le compte courant edité
	 */
	public void doEmpruntSimulationDialog() {
		 this.esViewController.displayDialog();
	}
}
