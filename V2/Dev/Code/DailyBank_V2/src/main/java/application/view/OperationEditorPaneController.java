package application.view;

import java.util.ArrayList;
import java.util.Locale;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.CategorieOperation;
import application.tools.ConstantesIHM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.Access_BD_CompteCourant;
import model.orm.Access_BD_Operation;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.RowNotFoundOrTooManyRowsException;

/**
 * Contrôleur de la fenêtre de l'éditeur d'opérations.
 */
public class OperationEditorPaneController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private CategorieOperation categorieOperation;
	private CompteCourant compteEdite;
	private Operation operationResultat;

	// Manipulation de la fenêtre
	
	 /**
     * Initialise le contexte du contrôleur.
     *
     * @param _containingStage La fenêtre physique contenant la scène
     * @param _dbstate         L'état courant de l'application
     */
	public void initContext(Stage _containingStage, DailyBankState _dbstate) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
	}

	 /**
     * Configure la gestion de l'événement de fermeture de la fenêtre primaryStage.
     */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	
	/**
     * Affiche la boîte de dialogue de l'éditeur d'opérations.
     *
     * @param cpte Le compte courant concerné
     * @param mode Le mode de l'opération (DEBIT, CREDIT, VIREMENT)
     * @return L'opération résultat ou null si annulé
     */
	public Operation displayDialog(CompteCourant cpte, CategorieOperation mode) {
		this.categorieOperation = mode;
		this.compteEdite = cpte;

		switch (mode) {
		case DEBIT:

			String info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);
			this.lblCompte.setVisible(false);
			this.cbTypeCompte.setVisible(false);

			this.btnOk.setText("Effectuer Débit");
			this.btnCancel.setText("Annuler débit");

			ObservableList<String> listTypesOpesPossibles = FXCollections.observableArrayList();
			listTypesOpesPossibles.addAll(ConstantesIHM.OPERATIONS_DEBIT_GUICHET);

			this.cbTypeOpe.setItems(listTypesOpesPossibles);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;
		case CREDIT:
			info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);
			this.lblCompte.setVisible(false);
			this.cbTypeCompte.setVisible(false);

			this.btnOk.setText("Effectuer Crédit");
			this.btnCancel.setText("Annuler Crédit");

			listTypesOpesPossibles = FXCollections.observableArrayList();
			listTypesOpesPossibles.addAll(ConstantesIHM.OPERATIONS_CREDIT_GUICHET);

			this.cbTypeOpe.setItems(listTypesOpesPossibles);
			this.cbTypeOpe.getSelectionModel().select(0);
			break;

		case VIREMENT:
			info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			this.btnOk.setText("Effectuer Virement");
			this.btnCancel.setText("Annuler Virement");




			listTypesOpesPossibles = FXCollections.observableArrayList();
			listTypesOpesPossibles.addAll(ConstantesIHM.OPERATIONS_VIREMENT_GUICHET);


			ObservableList<CompteCourant> listTypesComptesPossibles = FXCollections.observableArrayList();
			ObservableList<String> listTypesComptesPossiblesString = FXCollections.observableArrayList();
			ArrayList<CompteCourant> al = new ArrayList<>();
			int numClient = cpte.idNumCli;

			Access_BD_CompteCourant ao = new Access_BD_CompteCourant();

			try {
				al = ao.getCompteCourants(numClient);

				for (CompteCourant compte : al) {
					if (compte.idNumCompte != this.compteEdite.idNumCompte && !compte.isCloture()) {
						listTypesComptesPossibles.add(compte);
						String compteString = compte.toStringVirement();
						listTypesComptesPossiblesString.add(compteString);

					}
				}

			} catch (DataAccessException e) {
				e.printStackTrace();
			} catch (DatabaseConnexionException e) {
				e.printStackTrace();
			}

			this.cbTypeOpe.setItems(listTypesOpesPossibles);
			this.cbTypeOpe.getSelectionModel().select(0);

			this.cbTypeCompte.setItems(listTypesComptesPossiblesString);
			if(listTypesComptesPossiblesString.size() !=0) {
				this.cbTypeCompte.getSelectionModel().select(0);
			}else {
				cbTypeCompte.setDisable(true);
				btnOk.setDisable(true);
			}


			this.cbTypeCompte.setItems(listTypesComptesPossiblesString);
			if (listTypesComptesPossiblesString.size() != 0) {
				this.cbTypeCompte.getSelectionModel().select(0);
			} else {
				cbTypeCompte.setDisable(true);
			}


			break;

		}

		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dailyBankState.getEmployeActuel())) {
			// rien pour l'instant
		}

		this.operationResultat = null;
		this.cbTypeOpe.requestFocus();

		this.primaryStage.showAndWait();
		return this.operationResultat;
	}

	// Gestion du stage
	
	/**
     * Gère l'événement de fermeture de la fenêtre.
     *
     * @param e L'événement de fermeture de la fenêtre
     * @return null
     */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions

	@FXML
	private Label lblMessage;
	@FXML
	private Label lblMontant;
	@FXML
	private Label lblCompte;
	@FXML
	private ComboBox<String> cbTypeOpe;
	@FXML
	private ComboBox<String> cbTypeCompte;
	@FXML
	private TextField txtMontant;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	
	/**
     * Annule l'opération en cours et ferme la fenêtre.
     */
	@FXML
	private void doCancel() {
		this.operationResultat = null;
		this.primaryStage.close();
	}

	
	/**
     * Ajoute l'opération en cours et ferme la fenêtre.
     *
     */
	@FXML
	private void doAjouter() {

		switch (this.categorieOperation) {
		case DEBIT:
			// règles de validation d'un débit :
			// - le montant doit être un nombre valide
			// - et si l'utilisateur n'est pas chef d'agence,
			// - le débit ne doit pas amener le compte en dessous de son découvert autorisé
			double montant;

			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");


			String info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);

			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if (this.compteEdite.solde - montant < this.compteEdite.debitAutorise) {
				info = "Dépassement du découvert ! - Cpt. : " + this.compteEdite.idNumCompte + "  "
						+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
						+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			String typeOp = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montant, null, null, this.compteEdite.idNumCli, typeOp);
			this.primaryStage.close();
			break;
		case CREDIT:
			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");



			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if (montant > 999999) {
				info = "Dépassement du montant du crédit dépassé (>1M) ";
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}

			typeOp = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montant, null, null, this.compteEdite.idNumCli, typeOp);
			this.primaryStage.close();
			break;

		case VIREMENT:
			this.txtMontant.getStyleClass().remove("borderred");
			this.lblMontant.getStyleClass().remove("borderred");
			this.lblMessage.getStyleClass().remove("borderred");


			try {
				montant = Double.parseDouble(this.txtMontant.getText().trim());
				if (montant <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if (montant > 999999) {
				info = "Dépassement du montant du crédit dépassé (>1M) ";
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}
			if (this.compteEdite.solde - montant < this.compteEdite.debitAutorise) {
				info = "Dépassement du découvert ! - Cpt. : " + this.compteEdite.idNumCompte + "  "
						+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
						+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
				this.lblMessage.setText(info);
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}


			String idCompteDest = this.cbTypeCompte.getSelectionModel().getSelectedItem();
			String numIdCompteDest = idCompteDest.substring(0, 5);
			String numIdCompteDest1 = numIdCompteDest.replaceFirst("^0+(?!$)", "");
			int numId = Integer.parseInt(numIdCompteDest1);			 

			typeOp = this.cbTypeOpe.getValue();
			this.operationResultat = new Operation(-1, montant, null, null, numId, typeOp);


			this.primaryStage.close();
			break;
		}

	}

}


