package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.CompteEditorPaneController;
import application.view.PrelevEditorPaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Prelevement;

public class PrelevementEditorPane {

	private Stage primaryStage;
	private PrelevEditorPaneController pepcViewController;

	/**
	 * Constructeur de la classe de CompteEditorPane.
	 * Cette classe permet de gérer l'affichage d'un dialogue de gestion des comptes.
	 * 
	 * @param _parentStage Fenêtre parente de LoginDialog (sur laquelle se centrer
	 *                     et être modale)
	 * @param _dbstate     Etat courant de l'application
	 */
	public PrelevementEditorPane(Stage _parentStage, DailyBankState _dbstate) {

		try {
			FXMLLoader loader = new FXMLLoader(PrelevEditorPaneController.class.getResource("prelevementeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth() + 20, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un prélèvement");
			this.primaryStage.setResizable(false);

			this.pepcViewController = loader.getController();
			this.pepcViewController.initContext(this.primaryStage, _dbstate);

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
	public Prelevement doPrelevementEditorDialog(CompteCourant compte, Prelevement prlv, EditionMode em) {
		return this.pepcViewController.displayDialog(compte, prlv, em);
	}
}
