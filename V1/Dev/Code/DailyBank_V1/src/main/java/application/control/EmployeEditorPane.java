package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.EmployeEditorPaneController;
import application.view.EmployesManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Employe;

public class EmployeEditorPane {

	private Stage primaryStage;
	private EmployeEditorPaneController eepcViewController;
	private DailyBankState dailyBankState;

	/**
	 * Constructeur de la classe de EmployeEditorPane.
	 * Cette classe permet de gérer l'affichage d'un dialogue de gestion d'un employé.
	 * 
	 * @param _parentStage
	 * @param _dbstate
	 */
	public EmployeEditorPane(Stage _parentStage, DailyBankState _dbstate) {
		this.dailyBankState = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(EmployesManagementController.class.getResource("employeeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth() + 20, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion d'un employé");
			this.primaryStage.setResizable(false);

			this.eepcViewController = loader.getController();
			this.eepcViewController.initContext(this.primaryStage, this.dailyBankState);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/** 
	 * Cette méthode permet d'afficher le dialogue de gestion d'un employé. <br/>
	 * Que ce soit pour une création, une modification, une suppression ou une consultation <br/> 
	 * il donc faut passer en paramètre l'employé concerné et le mode d'édition qui sera utilisé. <br/>
	 * 
	 * @param employe
	 * @param em
	 * @return Employe
	 */
	public Employe doEmployeEditorDialog(Employe employe, EditionMode em) {
		return this.eepcViewController.displayDialog(employe, em);
	}
}
