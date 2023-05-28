package application.view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import application.DailyBankState;
import application.control.ComptesManagement;
import application.control.EmpruntSimulation;
import application.control.PrelevManagement;
import application.tools.ConstantesIHM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import model.data.Client;
import model.data.CompteCourant;


/**
 * Cette classe est le contrôleur de la gestion des comptes.
 * Elle gère les interactions avec l'interface graphique et effectue les opérations correspondantes.
 */
public class ComptesManagementController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à ComptesManagementController
	private ComptesManagement cmDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private Client clientDesComptes;
	private ObservableList<CompteCourant> oListCompteCourant;

	// Choix de l'emplacement du fichier à enregistrer
	FileChooser fileChooser = new FileChooser();

	// Manipulation de la fenêtre

	/**
	 * Initialise le contexte du contrôleur avec les paramètres fournis.
	 * @param _containingStage La fenêtre physique contenant la scène
	 * @param _cm Le contrôleur de dialogue associé
	 * @param _dbstate L'état courant de l'application
	 * @param client Le client associé aux comptes
	 */
	public void initContext(Stage _containingStage, ComptesManagement _cm, DailyBankState _dbstate, Client client) {
		this.cmDialogController = _cm;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.clientDesComptes = client;
		this.configure();
	}


	/**
	 * Configure les éléments de l'interface graphique.
	 */
	private void configure() {
		String info;

		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListCompteCourant = FXCollections.observableArrayList();
		this.lvComptes.setItems(this.oListCompteCourant);
		this.lvComptes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvComptes.getFocusModel().focus(-1);
		this.lvComptes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());

		info = this.clientDesComptes.nom + "  " + this.clientDesComptes.prenom + "  (id : "
				+ this.clientDesComptes.idNumCli + ")";
		this.lblInfosClient.setText(info);

		this.loadList();
		this.validateComponentState();
	}

	/**
	 * Affiche la fenêtre de gestion des comptes.
	 */
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}


	/**
	 * Gère l'événement de fermeture de la fenêtre en annulant ce qui était en cours.
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
	private Label lblInfosClient;
	@FXML
	private ListView<CompteCourant> lvComptes;
	@FXML
	private Button btnVoirOpes;
	@FXML
	private Button btnModifierCompte;
	@FXML
	private Button btnSupprCompte;
	@FXML
	private Button btnPrelev;
	@FXML
	private Button btnRel;
	@FXML
	private Button btnEmprunt;

	private ContextMenu contextMenu = new ContextMenu();

	/**
	 * Annule et ferme la fenêtre de gestion des comptes.
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	/**
	 * Affiche les opérations d'un compte sélectionné.
	 * Charge ensuite la liste des comptes et met à jour l'état des composants.
	 */
	@FXML
	private void doVoirOperations() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			CompteCourant cpt = this.oListCompteCourant.get(selectedIndice);
			this.cmDialogController.gererOperationsDUnCompte(cpt);
		}
		this.loadList();
		this.validateComponentState();
	}

	//Modification d'un compte

	/**
	 * @author yannis gibert
	 * Modifie un compte sélectionné.
	 * Charge ensuite la liste des comptes et met à jour l'état des composants.
	 */
	@FXML
	private void doModifierCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			CompteCourant compteMod = this.oListCompteCourant.get(selectedIndice);
			CompteCourant result = this.cmDialogController.modifierCompte(compteMod);
			if (result != null) {
				this.oListCompteCourant.set(selectedIndice, result);
			}
		}
		this.loadList();
		this.validateComponentState();
	}

	//Suppression d'un compte courant

	/**
	 * @author yannis gibert
	 * Supprime un compte courant sélectionné.
	 * Charge ensuite la liste des comptes et met à jour l'état des composants.
	 */
	@FXML
	private void doSupprimerCompte() {
		int selectedItem = this.lvComptes.getSelectionModel().getSelectedIndex();
		if(selectedItem >= 0) {
			CompteCourant compte = this.oListCompteCourant.get(selectedItem);
			this.cmDialogController.supprimerCompte(compte);
		}
		this.loadList();
		this.validateComponentState();
	}

	//Ajout d'un nouveau compte courant

	/**
	 * @author yannis gibert
	 * Ajoute un nouveau compte courant.
	 * Charge ensuite la liste des comptes.
	 */
	@FXML
	private void doNouveauCompte() {
		CompteCourant compte;
		compte = this.cmDialogController.creerNouveauCompte();
		if (compte != null) {
			this.oListCompteCourant.add(compte);
		}
	}



	/**
	 * Méthode exécutée lorsqu'un clic de souris est détecté sur la liste des comptes.
	 * Affiche un menu contextuel en fonction du bouton de la souris utilisé et de l'élément sélectionné.
	 *
	 * @param event L'événement de clic de souris.
	 */
	@FXML
	private void onClicList(MouseEvent event) {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if(this.lvComptes.getItems().size() != 0 && selectedIndice >= 0) {
			CompteCourant compteSelected = this.oListCompteCourant.get(selectedIndice);
			MouseButton mb = event.getButton();
			if(MouseButton.SECONDARY==mb) {
				this.contextMenu.hide();
				this.contextMenu = new ContextMenu();
				MenuItem menuItem1 = new MenuItem("Voir les opérations");
				menuItem1.setOnAction(e -> {
					this.doVoirOperations();
				});
				this.contextMenu.getItems().add(menuItem1);
				if (!compteSelected.isCloture()) {
					MenuItem menuItem2 = new MenuItem("Modifier");
					MenuItem menuItem3 = new MenuItem("Supprimer");
					menuItem2.setOnAction(e -> {
						this.doModifierCompte();
					});
					menuItem3.setOnAction(e -> {
						this.doSupprimerCompte();
					});
					this.contextMenu.getItems().add(menuItem2);
					this.contextMenu.getItems().add(menuItem3);
				}
				this.contextMenu.show(this.lvComptes , event.getScreenX(), event.getScreenY());
			}
			if(MouseButton.PRIMARY==mb) {
				this.contextMenu.hide();
				if(event.getClickCount() > 1) {
					if (!compteSelected.isCloture()) {
						this.doModifierCompte();
					}
				}
			}
		}
	}

	/**
	 * Génère un relevé mensuel d'un compte au format PDF.
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@FXML
	private void doRel() {
		//afficher une alerte qui pemt de sélectionner le mois et l'année
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Relevé mensuel");
		dialog.setHeaderText("Veuillez sélectionner le mois et l'année du relevé");

		// Set the button types.
		ButtonType generateButtonType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

		// Create the month and year ComboBoxes.
		ComboBox<String> monthComboBox = new ComboBox<>();
		monthComboBox.setPromptText("Mois");
		monthComboBox.getItems().addAll("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre");

		ComboBox<String> yearComboBox = new ComboBox<>();
		yearComboBox.setPromptText("Année");
		yearComboBox.getItems().addAll("2021", "2022", "2023", "2024");  // Add as many years as you need

		GridPane grid = new GridPane();
		grid.add(new Label("Mois:"), 0, 0);
		grid.add(monthComboBox, 1, 0);
		grid.add(new Label("Année:"), 0, 1);
		grid.add(yearComboBox, 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == generateButtonType) {
				return new Pair<>(monthComboBox.getValue(), yearComboBox.getValue());
			}
			return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();

		result.ifPresent(monthYear -> {
			if(!this.cmDialogController.genererPDF(monthYear, this.clientDesComptes, this.oListCompteCourant.get(this.lvComptes.getSelectionModel().getSelectedIndex()))) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText("Aucune opération sur ce compte pour ce mois et cette année");
				alert.showAndWait();
			}
		});


	}
	/**
	 * @author yannis gibert
	 * Permet de charger la liste des prélèvements du compte sélectionné
	 */
	
	@FXML
	public void gererPrelevCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			PrelevManagement pm = new PrelevManagement(this.primaryStage, this.dailyBankState, this.oListCompteCourant.get(selectedIndice));
			pm.doPrelevManagementDialog();
		}

	}

	/**
	 * Simule un emprunt
	 * @author Julien Couderc
	 */
	@FXML
	private void doSimulerEmprunt() {
		EmpruntSimulation es = new EmpruntSimulation(this.primaryStage, this.dailyBankState, this.clientDesComptes);
		es.doEmpruntSimulationDialog();
	}


	/**
	 * Charge la liste des comptes du client et l'affiche dans la vue.
	 */
	private void loadList() {
		ArrayList<CompteCourant> listeCpt;
		listeCpt = this.cmDialogController.getComptesDunClient();
		this.oListCompteCourant.clear();
		this.oListCompteCourant.addAll(listeCpt);
	}

	/**
	 * Valide l'état des composants en fonction de la sélection d'un compte.
	 * Active ou désactive les boutons en conséquence.
	 */
	private void validateComponentState() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnVoirOpes.setDisable(false);
			if(!this.oListCompteCourant.get(selectedIndice).isCloture()) {
				this.btnModifierCompte.setDisable(false);
				this.btnSupprCompte.setDisable(false);
				this.btnRel.setDisable(false);
				this.btnPrelev.setDisable(false);
			}
			else {
				this.btnModifierCompte.setDisable(true);
				this.btnSupprCompte.setDisable(true);
				this.btnRel.setDisable(true);
				this.btnPrelev.setDisable(true);
			}
		} else {
			this.btnVoirOpes.setDisable(true);
			this.btnModifierCompte.setDisable(true);
			this.btnSupprCompte.setDisable(true);
			this.btnRel.setDisable(true);
			this.btnPrelev.setDisable(true);
		}
		if (ConstantesIHM.isAdmin(this.dailyBankState.getEmployeActuel())) {
			this.btnEmprunt.setVisible(true);
		}else {
			this.btnEmprunt.setVisible(false);
		}
	}

}
