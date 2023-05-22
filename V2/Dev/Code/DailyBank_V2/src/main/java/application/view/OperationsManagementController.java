package application.view;

import java.util.ArrayList;
import java.util.Locale;

import application.DailyBankState;
import application.control.OperationsManagement;
import application.tools.NoSelectionModel;
import application.tools.PairsOfValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;

/**
 * Contrôleur de gestion des opérations.
 * Cette classe gère la logique métier et l'interaction avec l'interface graphique
 * pour les opérations bancaires d'un compte courant.
 */
public class OperationsManagementController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à OperationsManagementController
	private OperationsManagement omDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private Client clientDuCompte;
	private CompteCourant compteConcerne;
	private ObservableList<Operation> oListOperations;

	// Manipulation de la fenêtre
	
	/**
     * Initialise le contexte du contrôleur.
     *
     * @param _containingStage La fenêtre contenant la scène.
     * @param _om              Le contrôleur de dialogue associé.
     * @param _dbstate         L'état courant de l'application.
     * @param client           Le client associé au compte courant.
     * @param compte           Le compte courant concerné.
     */
	public void initContext(Stage _containingStage, OperationsManagement _om, DailyBankState _dbstate, Client client,
			CompteCourant compte) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.omDialogController = _om;
		this.clientDuCompte = client;
		this.compteConcerne = compte;
		this.configure();
	}

	
	/**
	 * Configure les paramètres et les éléments de l'interface graphique.
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListOperations = FXCollections.observableArrayList();
		this.lvOperations.setItems(this.oListOperations);
		this.lvOperations.setSelectionModel(new NoSelectionModel<Operation>());
		this.updateInfoCompteClient();
		this.lvOperations.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
		this.validateComponentState();
	}

	/**
     * Affiche la fenêtre de dialogue.
     */
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage
	
	 /**
     * Ferme la fenêtre et effectue les actions appropriées.
     *
     * @param e L'événement de fermeture de la fenêtre.
     * @return {@code null}.
     */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions

	@FXML
	private Label lblInfosClient;
	@FXML
	private Label lblInfosCompte;
	@FXML
	private ListView<Operation> lvOperations;
	@FXML
	private Button btnDebit;
	@FXML
	private Button btnCredit;
	@FXML
	private Button btnVirement;

	
	/**
     * Annule l'action en cours et ferme la fenêtre.
     */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	/**
     * Effectue l'opération de débit.
     */
	@FXML
	private void doDebit() {

		Operation op = this.omDialogController.enregistrerDebit();
		if (op != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	
	 /**
     * Effectue l'opération de crédit.
     */
	@FXML
	private void doCredit() {
		Operation op = this.omDialogController.enregistrerCredit();
		if (op != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	
	 /**
     * Effectue l'opération de virement.
     */
	@FXML
	private void doAutre() {
		Operation op = this.omDialogController.enregistrerVirement();
		if (op != null) {
			this.updateInfoCompteClient();
			this.validateComponentState();
		}
	}

	/**
     * Valide l'état des composants en fonction du compte courant.
     */
	private void validateComponentState() {
		if (!compteConcerne.isCloture() ) {
			this.btnCredit.setDisable(false);
			this.btnDebit.setDisable(false);
			this.btnVirement.setDisable(false);
		}else {
			this.btnCredit.setDisable(true);
			this.btnDebit.setDisable(true);
			this.btnVirement.setDisable(true);
		}
	}
	
	/**
     * Met à jour les informations du compte et du client affichées dans l'interface graphique.
     */
	private void updateInfoCompteClient() {

		PairsOfValue<CompteCourant, ArrayList<Operation>> opesEtCompte;

		opesEtCompte = this.omDialogController.operationsEtSoldeDunCompte();

		ArrayList<Operation> listeOP;
		this.compteConcerne = opesEtCompte.getLeft();
		listeOP = opesEtCompte.getRight();

		String info;
		info = this.clientDuCompte.nom + "  " + this.clientDuCompte.prenom + "  (id : " + this.clientDuCompte.idNumCli
				+ ")";
		this.lblInfosClient.setText(info);

		info = "Cpt. : " + this.compteConcerne.idNumCompte + "  "
				+ String.format(Locale.ENGLISH, "%12.02f", this.compteConcerne.solde) + "  /  "
				+ String.format(Locale.ENGLISH, "%8d", this.compteConcerne.debitAutorise);
		this.lblInfosCompte.setText(info);

		this.oListOperations.clear();
		this.oListOperations.addAll(listeOP);

		this.validateComponentState();
	}
}
