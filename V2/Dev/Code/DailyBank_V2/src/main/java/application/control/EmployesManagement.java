package application.control;

import java.util.ArrayList;

import application.DailyBankApp;
import application.DailyBankState;
import application.tools.EditionMode;
import application.tools.StageManagement;
import application.view.EmployesManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.data.Employe;
import model.orm.Access_BD_Employe;
import model.orm.exception.ApplicationException;
import model.orm.exception.DatabaseConnexionException;

public class EmployesManagement {
    
    private Stage primaryStage;
	private EmployesManagementController emcViewController;
	private DailyBankState dailyBankState;
	private Employe employe;

    /**
	 * Constructeur de la classe de EmployesManagement.
	 * Cette classe permet de gérer l'affichage d'un dialogue de gestion des employes.
	 * 
	 * @author Enzo Fournet
     * @param _parentStage
     * @param _dbstate
     */
    public EmployesManagement(Stage _parentStage, DailyBankState _dbstate) {
        this.dailyBankState = _dbstate;
        try {
            FXMLLoader loader = new FXMLLoader(EmployesManagementController.class.getResource("employesmanagement.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, root.getPrefWidth() + 50, root.getPrefHeight() + 10);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

            this.primaryStage = new Stage();
			this.primaryStage.initModality(Modality.WINDOW_MODAL);
			this.primaryStage.initOwner(_parentStage);
			StageManagement.manageCenteringStage(_parentStage, this.primaryStage);
			this.primaryStage.setScene(scene);
			this.primaryStage.setTitle("Gestion des employes");
			this.primaryStage.setResizable(false);

            System.out.println(emcViewController);
            this.emcViewController = loader.getController();
            this.emcViewController.initContext(this.primaryStage, this, _dbstate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * Appel l'affichage du dialogue d'édition des employes.
	 */
    public void doEmployeManagementDialog() {
		this.emcViewController.displayDialog();
	}

    
	/** 
	 * Cette méthode permet de récupérer la liste des employés en fonction des critères de recherche.
	 * 
	 * @param _numEmploye
	 * @param _debutNom
	 * @param _debutPrenom
	 * @param _droit
	 * @return ArrayList<Employe>
	 */
	public ArrayList<Employe> getlisteEmployes(int _numEmploye, String _debutNom, String _debutPrenom, String _droit){
        ArrayList<Employe> listeEmpl = new ArrayList<>();
		try {
			// Recherche des employés en BD. cf. AccessEmployé > getEmployé(.)
			// numEmployé != -1 => recherche sur numEmployé
			// numEmployé == -1 et debutNom non vide => recherche nom/prenom
			// numEmployé == -1 et debutNom vide => recherche tous les Employé
			Access_BD_Employe ae = new Access_BD_Employe();
			listeEmpl = ae.getEmployes(_numEmploye, _debutNom, _debutPrenom, _droit);
		} catch (DatabaseConnexionException e) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
			ed.doExceptionDialog();
			this.primaryStage.close();
			listeEmpl = new ArrayList<>();
		} catch (ApplicationException ae) {
			ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
			ed.doExceptionDialog();
			listeEmpl = new ArrayList<>();
		}
		return listeEmpl;
    }

	
	/** 
	 * Cette méthode permet de créer un nouvel utilisateur en appelant l'éditeur d'employé.
	 * 
	 * @return Employe
	 */
	public Employe nouvelEmploye() {
		Employe employe;
		EmployeEditorPane eep = new EmployeEditorPane(this.primaryStage, this.dailyBankState);
		employe = eep.doEmployeEditorDialog(null, EditionMode.CREATION);
		if (employe != null) {
			try {
				Access_BD_Employe ae = new Access_BD_Employe();
				ae.insertEmploye(employe); //ICI
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				employe = null;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
				employe = null;
			}
		}
		return employe;
	}

	
	/** 
	 * Cette méthode permet de modifier un employé en appelant l'éditeur d'employé.
	 * 
	 * @param employe
	 * @return Employe
	 */
	public Employe modifierEmploye(Employe employe) {
		EmployeEditorPane eep = new EmployeEditorPane(this.primaryStage, this.dailyBankState);
		Employe result = eep.doEmployeEditorDialog(employe, EditionMode.MODIFICATION);
		if (result != null) {
			try {  
				Access_BD_Employe ec = new Access_BD_Employe();
				ec.updateEmploye(result);
				return result;
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
	 * Cette méthode permet de supprimer un employé en appelant l'éditeur d'employé.
	 * 
	 * @param employe
	 * @return boolean
	 */
	public boolean supprimerEmploye(Employe employe) {
		EmployeEditorPane eep = new EmployeEditorPane(this.primaryStage, this.dailyBankState);
		Employe result = eep.doEmployeEditorDialog(employe, EditionMode.SUPPRESSION);
		System.out.println(result);
		if (result != null) {
			System.out.println("Suppression de l'employé");
			try {
				Access_BD_Employe ec = new Access_BD_Employe();
				ec.deleteEmploye(result);
				return true;
			} catch (DatabaseConnexionException e) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, e);
				ed.doExceptionDialog();
				this.primaryStage.close();
				return false;
			} catch (ApplicationException ae) {
				ExceptionDialog ed = new ExceptionDialog(this.primaryStage, this.dailyBankState, ae);
				ed.doExceptionDialog();
				return false;
			}
		}
		return false;
	}

    /**
	 * Cette méthode permet de consulter un employé en appelant l'éditeur d'employé.
	 * 
     * @param cliCons
     */
    public void consult(Employe cliCons) {
		EmployeEditorPane eep = new EmployeEditorPane(this.primaryStage, this.dailyBankState);
		eep.doEmployeEditorDialog(cliCons, EditionMode.CONSULTATION);
    }
}
