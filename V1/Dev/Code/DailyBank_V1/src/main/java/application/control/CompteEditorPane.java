package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.CompteEditorPaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;

public class CompteEditorPane {

	private Stage primaryStage;
	private CompteEditorPaneController cepcViewController;

	/**
	 * Constructeur de la classe de CompteEditorPane.
	 * Cette classe permet de gérer l'affichage d'un dialogue de gestion des comptes.
	 * 
	 * @param _parentStage Fenêtre parente de LoginDialog (sur laquelle se centrer
	 *                     et être modale)
	 * @param _dbstate     Etat courant de l'application
	 */
	public CompteEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(CompteEditorPaneController.class.getResource("compteeditorpane.fxml"));
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

			this.cepcViewController = loader.getController();
			this.cepcViewController.initContext(this.primaryStage, _dbstate);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Cette méthode permet d'afficher le dialogue de gestion d'un compte. <br/>
	 * Que ce soit pour une création, une modification, une suppression ou une consultation <br/> 
	 * il donc faut passer en paramètre le client a qui appartient le compte, le compte concerné et le mode d'édition qui sera utilisé. <br/>
	 * 
	 * @param client le client a qui appartient le compte
	 * @param cpte le compte qu'on souhaite editer
	 * @param em le mode d'édition
	 * 
	 * @return le compte courant edité
	 */
	public CompteCourant doCompteEditorDialog(Client client, CompteCourant cpte, EditionMode em) {
		return this.cepcViewController.displayDialog(client, cpte, em);
	}
}
