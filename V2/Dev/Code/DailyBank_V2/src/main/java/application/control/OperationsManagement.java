package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.CategorieOperation;
import application.tools.PairsOfValue;
import application.tools.StageManagement;
import application.view.OperationsManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.Access_BD_CompteCourant;
import model.orm.Access_BD_Operation;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class OperationsManagement {

	private Stage primaryStage;
	private DailyBankState dailyBankState;
	private OperationsManagementController omcViewController;
	private Client clientDuCompte;
	private CompteCourant compteConcerne;
	private CompteCourant compteConcerneDest;

	/**
	 * Constructeur de la classe de OperationsManagement. Cette classe permet de gérer l'affichage d'un dialogue de <br/>
	 * gestion des opérations.
	 *
	 * @param _parentStage
	 * @param _dbstate
	 * @param client
	 * @param compte
	 */
	public OperationsManagement(Stage _parentStage, DailyBankState _dbstate, Client client, CompteCourant compte) {

		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.dailyBankState = _dbstate;
		try {
			FXMLLoader loader = new FXMLLoader(
					OperationsManagementController.class.getResource("operationsmanagement.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root, 900 + 20, 350 + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des opérations");
			this.primaryStage.setResizable(false);

			this.omcViewController = loader.getController();
			this.omcViewController.initContext(this.primaryStage, this, _dbstate, client, this.compteConcerne);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode permettant d'afficher le dialogue de gestion des opérations.
	 */
	public void doOperationsManagementDialog() {
		this.omcViewController.displayDialog();
	}


	/**
	 * @author Julien Couderc
	 * Cette méthode permet d'enregistrer une opération de débit.
	 *
	 * @return Operation
	 */
	public Operation enregistrerDebit() {

		OperationEditorPane oep = new OperationEditorPane(this.primaryStage, this.dailyBankState);
		Operation op = oep.doOperationEditorDialog(this.compteConcerne, CategorieOperation.DEBIT);
		if (op != null) {
			try {
				Access_BD_Operation ao = new Access_BD_Operation();

				ao.insertDebit(this.compteConcerne, op.montant, op.idTypeOp,this.dailyBankState);

			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				op = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
				op = null;
			}
		}
		return op;
	}

	/**
	 * @author Julien Couderc
	 * Cette méthode permet d'enregistrer une opération de crédit.
	 *
	 * @return Operation
	 */
	public Operation enregistrerCredit() {

		OperationEditorPane oep = new OperationEditorPane(this.primaryStage, this.dailyBankState);
		Operation op = oep.doOperationEditorDialog(this.compteConcerne, CategorieOperation.CREDIT);
		if (op != null) {
			try {
				Access_BD_Operation ao = new Access_BD_Operation();

				ao.insertCredit(this.compteConcerne.idNumCompte, op.montant, op.idTypeOp);

			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				op = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
				op = null;
			}
		}
		return op;
	}

	/**
	 * @author Julien Couderc
	 *  
	 * Cette méthode permet d'enregistrer un virement.
	 *
	 * @return Operation
	 */
	public Operation enregistrerVirement() {
	    OperationEditorPane oep = new OperationEditorPane(this.primaryStage, this.dailyBankState);
	    Operation op = oep.doOperationEditorDialog(this.compteConcerne, CategorieOperation.VIREMENT);
	    if (op != null) {
	        try {
	            Access_BD_Operation ao = new Access_BD_Operation();
	            // Appeler la procédure de virement SQL
	            ao.insertVirement(this.compteConcerne,op.idNumCompte , op.montant, op.idTypeOp,this.dailyBankState);


	        } catch (DatabaseConnexionException e) {
	            ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
	            ed.doExceptionDialog();
	            this.primaryStage.close();
	            op = null;
	        } catch (ApplicationException ae) {
	            ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
	            ed.doExceptionDialog();
	            op = null;
	        }
	    }
	    return op;
	}


	/*
	 * Cette méthode permet de récupérer la liste des opérations de chause compte <br/>
	 */
	public PairsOfValue<CompteCourant, ArrayList<Operation>> operationsEtSoldeDunCompte() {
		ArrayList<Operation> listeOP = new ArrayList<>();

		try {
			// Relecture BD du solde du compte
			Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
			this.compteConcerne = acc.getCompteCourant(this.compteConcerne.idNumCompte);

			// lecture BD de la liste des opérations du compte de l'utilisateur
			Access_BD_Operation ao = new Access_BD_Operation();
			listeOP = ao.getOperations(this.compteConcerne.idNumCompte);

		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeOP = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
			ed.doExceptionDialog();
			listeOP = new ArrayList<>();
		}
		System.out.println(this.compteConcerne.solde);
		return new PairsOfValue<>(this.compteConcerne, listeOP);
	}
}
