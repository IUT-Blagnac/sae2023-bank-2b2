package application.view;

import java.util.ArrayList;

import application.DailyBankState;
import application.control.ComptesManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
        if(lvComptes.getItems().size() != 0 && selectedIndice >= 0) {
			CompteCourant compteSelected = this.oListCompteCourant.get(selectedIndice);
            MouseButton mb = event.getButton();
            if(MouseButton.SECONDARY==mb) {
				contextMenu.hide();
                contextMenu = new ContextMenu();
				MenuItem menuItem1 = new MenuItem("Voir les opérations");
				menuItem1.setOnAction(e -> {
					doVoirOperations();
				});
				contextMenu.getItems().add(menuItem1);
				if (!compteSelected.isCloture()) {
					MenuItem menuItem2 = new MenuItem("Modifier");
					MenuItem menuItem3 = new MenuItem("Supprimer");
					menuItem2.setOnAction(e -> {
						doModifierCompte();
					});
					menuItem3.setOnAction(e -> {
						doSupprimerCompte();
					});
					contextMenu.getItems().add(menuItem2);
					contextMenu.getItems().add(menuItem3);
				}
                contextMenu.show(lvComptes , event.getScreenX(), event.getScreenY());
            }
            if(MouseButton.PRIMARY==mb) {
                contextMenu.hide();
                if(event.getClickCount() > 1) {
					if (!compteSelected.isCloture()) {
						doModifierCompte();
					}
                }
            }
        }
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
			}	
			else {
				this.btnModifierCompte.setDisable(true);
				this.btnSupprCompte.setDisable(true);
			}
		} else {
			this.btnVoirOpes.setDisable(true);
			this.btnModifierCompte.setDisable(true);
			this.btnSupprCompte.setDisable(true);
		}
	}
}
