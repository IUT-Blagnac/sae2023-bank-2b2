package application.control;

import application.DailyBankApp;
import application.DailyBankState;
import application.view.DailyBankMainFrameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.data.Employe;
import model.orm.Access_BD_Employe;
import model.orm.LogToDatabase;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

import javafx.scene.image.Image;
import java.awt.Taskbar;
import javafx.embed.swing.SwingFXUtils;


/**
 * Classe de controleur de Dialogue de la fenêtre principale.
 *
 */

public class DailyBankMainFrame extends Application {

// Etat courant de l'application //MIS EN STATIC POUR TEST - AUCUNE INCIDENCE SUR LE FONCTIONNEMENT
	private static DailyBankState dailyBankState;

	// Stage de la fenêtre principale construite par DailyBankMainFrame
	private Stage primaryStage;

	/**
	 * Méthode de démarrage (JavaFX).
	 */
	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;

		primaryStage.getIcons().add(new Image(DailyBankMainFrame.class.getResourceAsStream("images/icon.png")));
		//si MACOS est utilisé chagement d'icon pour la barre des taches
		Taskbar taskbar = Taskbar.getTaskbar();
        try {
            java.awt.Image dockIcon = SwingFXUtils.fromFXImage(new Image(getClass().getResourceAsStream("images/icon.png")), null);
            taskbar.setIconImage(dockIcon);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {//le catch ne retourne rien pour en pas perturber l'execution du programme sur les autres OS}
		}

		try {
			// Création de l'état courant de l'application
			this.dailyBankState = new DailyBankState();
			this.dailyBankState.setEmployeActuel(null);

			// Chargement du source fxml
			FXMLLoader loader = new FXMLLoader(DailyBankMainFrameController.class.getResource("dailybankmainframe.fxml"));
			BorderPane root = loader.load();

			// Paramétrage du Stage : feuille de style, titre
			Scene scene = new Scene(root, root.getPrefWidth() + 20, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.setTitle("Fenêtre Principale");

			
			/*
			 // En mise au point : Forcer une connexion existante pour rentrer dansl'appli en mode connecté
			
			//CONNEXION AUTO
			try { Employe e; Access_BD_Employe ae = new Access_BD_Employe();
			  
			e = ae.getEmploye("Tuff", "Lejeune");
			
			if (e == null) { System.out.println("\n\nPB DE CONNEXION\n\n"); } else {
			this.dailyBankState.setEmployeActuel(e); } } catch
			(DatabaseConnexionException e) { ExceptionDialog ed = new
			ExceptionDialog(primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog(); this.dailyBankState.setEmployeActuel(null); } catch
			(ApplicationException ae) { ExceptionDialog ed = new
			ExceptionDialog(primaryStage, this.dailyBankState, ae);
			ed.doExceptionDialog(); this.dailyBankState.setEmployeActuel(null); }
			
			if (this.dailyBankState.getEmployeActuel() != null) {
			this.dailyBankState.setEmployeActuel(this.dailyBankState.getEmployeActuel());
			}
			// fin connexion auto
			*/
			

			// Récupération du contrôleur et initialisation (stage, contrôleur de dialogue,
			// état courant)
			DailyBankMainFrameController dbmfcViewController = loader.getController();
			dbmfcViewController.initContext(primaryStage, this, this.dailyBankState);

			dbmfcViewController.displayDialog();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Méthode principale de lancement de l'application.
	 */
	public static void runApp() {
		Application.launch();
	}

	/**
	 * Réaliser la déconnexion de l'application.
	 */
	public void deconnexionEmploye() {
		this.dailyBankState.setEmployeActuel(null);
		try {
			LogToDatabase.closeConnexion();
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog();
		}
	}

	/**
	 * Lancer la connexion de l'utilisateur (login/mdp employé).
	 */
	public void loginDunEmploye() {
		LoginDialog ld = new LoginDialog(this.primaryStage, this.dailyBankState);
		ld.doLoginDialog();
	}

	/**
	 * Lancer la gestion des clients (liste des clients).
	 */
	public void gestionClients() {
		ClientsManagement cm = new ClientsManagement(this.primaryStage, this.dailyBankState);
		cm.doClientManagementDialog();
	}

	/**
	 * Lancer la gestion des employés (liste des employés).
	 */
	public void gestionEmployes() {
		EmployesManagement em = new EmployesManagement(this.primaryStage, this.dailyBankState);
		em.doEmployeManagementDialog();
	}


//METHODS FOR TESTS - SI VOUS ETES UN PROFESSEUR CETTE METHODE NE RENTRE PAS EN COMPTE DANS LA NOTATION
    public static DailyBankState getDailyBankState() {
        return dailyBankState;
    }
}
