package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.tools.pdf.GenPDF;
import application.view.ComptesManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.Access_BD_CompteCourant;
import model.orm.Access_BD_Operation;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.Order;
import model.orm.exception.Table;

public class ComptesManagement {

	private Stage primaryStage;
	private ComptesManagementController cmcViewController;
	private DailyBankState dailyBankState;
	private Client clientDesComptes;

	/**
	 * Création d'un page de gestion des comptes.<BR />
	 *
	 * @param _parentStage Fenêtre parente de LoginDialog (sur laquelle se centrer
	 *                     et être modale)
	 * @param _dbstate     Etat courant de l'application
	 * @param client le client dont on souhaite voir les comptes
	 */
	public ComptesManagement(Stage _parentStage, DailyBankState _dbstate, Client client) {

		this.clientDesComptes = client;
		this.dailyBankState = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(ComptesManagementController.class.getResource("comptesmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, root.getPrefWidth() + 50, root.getPrefHeight() + 100);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des comptes");
			this.primaryStage.setResizable(false);

			this.cmcViewController = loader.getController();
			this.cmcViewController.initContext(this.primaryStage, this, _dbstate, client);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Affiche la page des comptes d'un client
	 */
	public void doComptesManagementDialog() {
		this.cmcViewController.displayDialog();
	}

	/**
	 * Permet d'ouvrir la page de gestion des opérations d'un compte
	 *
	 * @param cpt le compte qu'on souhaite récupérer ses opérations
	 */
	public void gererOperationsDUnCompte(CompteCourant cpt) {
		OperationsManagement om = new OperationsManagement(this.primaryStage, this.dailyBankState,
				this.clientDesComptes, cpt);
		om.doOperationsManagementDialog();
	}

	/**
	 * Permet de cloturer un compte
	 *
	 * @param compte le compte que l'on souhaite supprimer
	 * @return le compte courant
	 * @author yannis gibert
	 */
	public CompteCourant supprimerCompte(CompteCourant compte) {
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dailyBankState);
		CompteCourant result = cep.doCompteEditorDialog(this.clientDesComptes,compte, EditionMode.SUPPRESSION);
		if (result != null) {
			try {
				Access_BD_CompteCourant ac = new Access_BD_CompteCourant();
				if(compte.solde !=  0) {
					AlertUtilities.showAlert(this.primaryStage,"Cloturation du compte" ,"Impossible de cloturer le compte" , "Il n'est pas possible de cloturer un compte si le solde de ce dernier n'est pas égal à 0", AlertType.ERROR);
					return null;
				}
				compte.setCloture("O");
				ac.deleteCompteCourant(compte);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				result = null;
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		return result;
	}

	/**
	 * Permet de modifier les informations d'un compte
	 *
	 * @param c le compte qu'on souhaite modifier
	 * @return le compte modifié
	 * @author yannis gibert
	 */
	public CompteCourant modifierCompte(CompteCourant c) {
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dailyBankState);
		CompteCourant result = cep.doCompteEditorDialog(this.clientDesComptes,c, EditionMode.MODIFICATION);
		if (result != null) {
			try {
				Access_BD_CompteCourant ac = new Access_BD_CompteCourant();
				ac.updateCompteCourant(result);
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				result = null;
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
				result = null;
			}
		}
		return result;
	}

	/**
	 * Permet de créer un nouveau compte courant
	 *
	 * @return le nouveau compte courant
	 * @author yannis gibert
	 */
	public CompteCourant creerNouveauCompte() {
		CompteCourant compte;
		CompteEditorPane cep = new CompteEditorPane(this.primaryStage, this.dailyBankState);
		compte = cep.doCompteEditorDialog(this.clientDesComptes, null, EditionMode.CREATION);
		if (compte != null) {
			try {
				Access_BD_CompteCourant acCC = new Access_BD_CompteCourant();
				Access_BD_Operation acOp = new Access_BD_Operation();
				acCC.insertCompte(compte);
				acCC.updateCompteCourant(compte);
				acOp.insertPremierDepot(compte.idNumCompte, compte.solde, "Premier depot");
				if (Math.random() < -1) {
					throw new ApplicationException(Table.CompteCourant, Order.INSERT, "todo : test exceptions", null);
				}
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
			}
		}
		return compte;
	}

	/**
	 * Permet de récupérer tout les comptes courant d'un client
	 *
	 * @return une liste de compte courant
	 */
	public ArrayList<CompteCourant> getComptesDunClient() {
		ArrayList<CompteCourant> listeCpt = new ArrayList<>();

		try {
			Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
			listeCpt = acc.getCompteCourants(this.clientDesComptes.idNumCli);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeCpt = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
			ed.doExceptionDialog();
			listeCpt = new ArrayList<>();
		}
		return listeCpt;
	}

	/**
	 * Permet de récupérer toutes les opérations d'un compte 
	 * 
	 * @param selectedItem le compte courant sélectionné
	 * @param month le mois de l'operation
	 * @param year l'annee de l'operation
	 *
	 * @return une array liste d'opération
	 */
	public ArrayList<Operation> getOperationsDunCompte(CompteCourant selectedItem, String month, String year) {
		ArrayList<Operation> listeOp = new ArrayList<>();

		try {
			Access_BD_Operation aop = new Access_BD_Operation();
			listeOp = aop.getOperations(selectedItem.idNumCompte, month, year);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeOp = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
			ed.doExceptionDialog();
			listeOp = new ArrayList<>();
		}

		return listeOp;
	}

    public boolean genererPDF(Pair<String, String> monthYear, Client clientDesComptes, CompteCourant compteCourant) {
		GenPDF pdf = new GenPDF();
		pdf.initContext(this.primaryStage, this, this.dailyBankState, clientDesComptes);
		return pdf.genererPDF(monthYear, clientDesComptes, compteCourant);
    }
}
