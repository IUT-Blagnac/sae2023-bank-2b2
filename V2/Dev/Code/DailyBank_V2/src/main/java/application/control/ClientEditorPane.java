package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.ClientEditorPaneController;
import application.view.ClientsManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;

public class ClientEditorPane {

	private Stage primaryStage;
	private ClientEditorPaneController cepcViewController;
	private DailyBankState dailyBankState;

	/**
	 * Constructeur de la classe de ClientEditorPane.
	 * Cette classe permet de gérer l'affichage d'un dialogue de gestion des client.
	 * 
	 * @param _parentStage Fenêtre parente de LoginDialog (sur laquelle se centrer
	 *                     et être modale)
	 * @param _dbstate     Etat courant de l'application
	 */
	public ClientEditorPane(Stage _parentStage, DailyBankState _dbstate) {
		this.dailyBankState = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ClientsManagementController.class.getResource("clienteditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth() + 20, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un client");
			this.primaryStage.setResizable(false);

			this.cepcViewController = loader.getController();
			this.cepcViewController.initContext(this.primaryStage, this.dailyBankState);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Cette méthode permet d'afficher le dialogue de gestion d'un client. <br/>
	 * Que ce soit pour une création, une modification, une suppression ou une consultation <br/> 
	 * il donc faut passer en paramètre le client a qui appartient le compte, le compte concerné et le mode d'édition qui sera utilisé. <br/>
	 * 
	 * @param client le client qu'on souhaite editer
	 * @param em le mode d'édition
	 * 
	 * @return le client edité
	 */
	public Client doClientEditorDialog(Client client, EditionMode em) {
		return this.cepcViewController.displayDialog(client, em);
	}
}
