package application.view;

import java.util.ArrayList;
import java.util.Locale;

import application.DailyBankState;
import application.tools.CategorieOperation;
import application.tools.ConstantesIHM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;
import model.orm.Access_BD_Client;
import model.orm.Access_BD_CompteCourant;
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


	/**
	 * 
	 * Initialise le contexte du contrôleur.
	 *
	 * @param _containingStage La fenêtre physique contenant la scène
	 * @param _dbstate         L'état courant de l'application
	 * @author Julien Couderc
	 */
	public void initContext(Stage _containingStage, DailyBankState _dbstate) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
	}

	/**
	 * 
	 * Configure la gestion de l'événement de fermeture de la fenêtre primaryStage.
	 * @author Julien Couderc
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	/**
	 * 
	 * 
	 * Affiche la boîte de dialogue de l'éditeur d'opérations.
	 *
	 * @param cpte Le compte courant concerné
	 * @param mode Le mode de l'opération (DEBIT, CREDIT, VIREMENT)
	 * @return L'opération résultat ou null si annulé
	 * @author Julien Couderc
	 */
	public Operation displayDialog(CompteCourant cpte, CategorieOperation mode) {
		this.categorieOperation = mode;
		this.compteEdite = cpte;
		this.btnExceptionnel.setDisable(true);
		this.btnExceptionnel.setVisible(false);
		this.btnExceptionnel.setSelected(false);
		this.lblExceptionnel.setVisible(false);
		switch (mode) {
		case DEBIT:

			String info = "Cpt. : " + this.compteEdite.idNumCompte + "  "
					+ String.format(Locale.ENGLISH, "%12.02f", this.compteEdite.solde) + "  /  "
					+ String.format(Locale.ENGLISH, "%8d", this.compteEdite.debitAutorise);
			this.lblMessage.setText(info);
			this.lblCompte.setVisible(false);
			this.txtNumCompte.setVisible(false);
			if(this.dailyBankState.isChefDAgence()) {
				this.btnExceptionnel.setDisable(false);
				this.btnExceptionnel.setVisible(true);
				this.lblExceptionnel.setVisible(true);
			}
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
			this.txtNumCompte.setVisible(false);
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
			this.cbTypeOpe.setItems(listTypesOpesPossibles);
			this.cbTypeOpe.getSelectionModel().select(0);

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


	/**
	 * 
	 * 
	 * Gère l'événement de fermeture de la fenêtre.
	 *
	 * @param e L'événement de fermeture de la fenêtre
	 * @return null
	 * @author Julien Couderc
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
	private Label lblExceptionnel;
	@FXML
	private ComboBox<String> cbTypeOpe;
	@FXML
	private TextField txtNumCompte;
	@FXML
	private TextField txtMontant;
	@FXML
	private RadioButton btnExceptionnel;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	/**
	 * 
	 * 
	 * Annule l'opération en cours et ferme la fenêtre.
	 * @author Julien Couderc
	 */
	@FXML
	private void doCancel() {
		this.operationResultat = null;
		this.primaryStage.close();
	}

	/**
	 * 
	 * 
	 * 
	 * Ajoute l'opération en cours et ferme la fenêtre.
	 * 
	 * Cette méthode est appelée lorsque l'utilisateur souhaite ajouter une opération.
	 * Elle effectue les vérifications nécessaires en fonction de la catégorie de l'opération (débit, crédit, virement)
	 * et des règles spécifiques à chaque catégorie.
	 * 
	 * Pour la catégorie "Débit" :
	 *   - Le montant doit être un nombre valide et supérieur à zéro.
	 *   - Si l'utilisateur n'est pas chef d'agence, le débit ne doit pas amener le compte en dessous de son découvert autorisé.
	 * 
	 * Pour la catégorie "Crédit" :
	 *   - Le montant doit être un nombre valide et supérieur à zéro.
	 *   - Le montant du crédit ne doit pas dépasser 1 million.
	 * 
	 * Pour la catégorie "Virement" :
	 *   - Le montant doit être un nombre valide et supérieur à zéro.
	 *   - Le montant du virement ne doit pas dépasser 1 million.
	 *   - Le compte cible du virement doit exister parmi les comptes courants non clôturés des clients de la même agence.
	 *   - Il est impossible de s'auto-virer (virement vers le même compte).
	 *   - Si toutes les vérifications sont réussies, l'opération de virement est créée avec les informations nécessaires.
	 * 
	 * Une fois l'opération ajoutée, la fenêtre est fermée.
	 * @author Julien Couderc
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
			if (this.compteEdite.solde - montant < this.compteEdite.debitAutorise && !this.btnExceptionnel.isSelected()) {
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
				if (montant <= 0) {
					throw new NumberFormatException();
				}
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

			int numCompte = Integer.parseInt(this.txtNumCompte.getText());

			int idAg = 0;
			CompteCourant compteC;
			int idNumCli = 0;
			ArrayList<Client> alClient = new ArrayList<>();
			ArrayList<CompteCourant> alComptesCourants = new ArrayList<>();

			Access_BD_CompteCourant acc = new Access_BD_CompteCourant();

			try {

				compteC = acc.getCompteCourant(this.compteEdite.idNumCompte);
				idNumCli = compteC.idNumCli;
				Access_BD_Client ac = new Access_BD_Client();

				Client c = ac.getClient(idNumCli);
				idAg = c.idAg;

				alClient = ac.getClients(idAg, -1, "", "");

				for (int i = 0; i < alClient.size(); i++) {
					Client client = alClient.get(i);
					ArrayList<CompteCourant> comptesClient = acc.getCompteCourants(client.idNumCli);
					for (CompteCourant compteCourant : comptesClient) {
						if (!compteCourant.isCloture()) {
							alComptesCourants.add(compteCourant);
						}
					}
				}

			} catch (RowNotFoundOrTooManyRowsException e) {
				e.printStackTrace();
			} catch (DataAccessException e) {
				e.printStackTrace();
			} catch (DatabaseConnexionException e) {
				e.printStackTrace();
			}

			boolean existe = false;
			for (CompteCourant alComptesCourant : alComptesCourants) {
				if (numCompte == alComptesCourant.idNumCompte) {
					existe = true;
					break;
				}
			}


			if (existe) {
				typeOp = this.cbTypeOpe.getValue();

				if(numCompte != this.compteEdite.idNumCompte) {
					this.operationResultat = new Operation(-1, montant, null, null, numCompte, typeOp);
				}else {
						this.lblMessage.setText("IMPOSSIBLE DE S'AUTO VIRER");
						this.txtMontant.getStyleClass().add("borderred");
						this.lblMontant.getStyleClass().add("borderred");
						this.lblMessage.getStyleClass().add("borderred");
						this.txtMontant.requestFocus();
						return;
				}
			} else {
				this.lblMessage.setText("COMPTE INEXISTANT");
				this.txtMontant.getStyleClass().add("borderred");
				this.lblMontant.getStyleClass().add("borderred");
				this.lblMessage.getStyleClass().add("borderred");
				this.txtMontant.requestFocus();
				return;
			}

			this.primaryStage.close();
			break;
		}

	}
}
