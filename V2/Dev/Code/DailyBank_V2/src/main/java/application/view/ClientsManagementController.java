package application.view;

import java.util.ArrayList;

import application.DailyBankState;
import application.control.ClientsManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;

/**
 * Classe responsable de la gestion des clients dans l'application.
 * Elle contrôle la fenêtre physique contenant la scène liée au fichier XML gérée par cette classe.
 * Elle gère les interactions avec les utilisateurs et les manipulations des données des clients.
 */
public class ClientsManagementController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à ClientsManagementController
	private ClientsManagement cmDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Client> oListClients;

	// Manipulation de la fenêtre

	/**
     * Initialise le contexte du contrôleur.
     *
     * @param _containingStage La fenêtre contenant la scène associée.
     * @param _cm              Le contrôleur de dialogue associé.
     * @param _dbstate         L'état courant de la banque.
     */
	public void initContext(Stage _containingStage, ClientsManagement _cm, DailyBankState _dbstate) {
		this.cmDialogController = _cm;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
		this.oListClients.addAll(this.cmDialogController.getlisteComptes(-1, "", ""));
	}

	/**
	 * Configure les éléments de la fenêtre et les actions associées.
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListClients = FXCollections.observableArrayList();
		this.lvClients.setItems(this.oListClients);
		this.lvClients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvClients.getFocusModel().focus(-1);
		this.lvClients.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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
	 * Ferme la fenêtre en appelant la méthode "doCancel()" et consomme l'événement de fermeture.
	 *
	 * @param e L'événement de fermeture de la fenêtre.
	 * @return null
	 */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions

	@FXML
	private TextField txtNum;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private ListView<Client> lvClients;
	@FXML
	private Button btnDesactClient;
	@FXML
	private Button btnModifClient;
	@FXML
	private Button btnComptesClient;
	private ContextMenu contextMenu = new ContextMenu();


	/**
     * Ferme la fenêtre de dialogue.
     */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}


	/**
     * Effectue une recherche de clients en fonction des critères spécifiés.
     */
	@FXML
	private void doRechercher() {
		int numCompte;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				numCompte = -1;
			} else {
				numCompte = Integer.parseInt(nc);
				if (numCompte < 0) {
					this.txtNum.setText("");
					numCompte = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			numCompte = -1;
		}

		String debutNom = this.txtNom.getText();
		String debutPrenom = this.txtPrenom.getText();

		if (numCompte != -1) {
			this.txtNom.setText("");
			this.txtPrenom.setText("");
		} else {
			if (debutNom.equals("") && !debutPrenom.equals("")) {
				this.txtPrenom.setText("");
			}
		}

		// Recherche des clients en BD. cf. AccessClient > getClients(.)
		// numCompte != -1 => recherche sur numCompte
		// numCompte != -1 et debutNom non vide => recherche nom/prenom
		// numCompte != -1 et debutNom vide => recherche tous les clients
		ArrayList<Client> listeCli;
		listeCli = this.cmDialogController.getlisteComptes(numCompte, debutNom, debutPrenom);

		this.oListClients.clear();
		this.oListClients.addAll(listeCli);
		this.validateComponentState();
	}

    /**
     * Ouvre la fenêtre de gestion des comptes pour le client sélectionné.
     */

	@FXML
	private void doComptesClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client client = this.oListClients.get(selectedIndice);
			this.cmDialogController.gererComptesClient(client);
		}
	}


	/**
     * Modifie le client sélectionné.
     */
	@FXML
	private void doModifierClient() {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Client cliMod = this.oListClients.get(selectedIndice);
			Client result = this.cmDialogController.modifierClient(cliMod);
			if (result != null) {
				this.oListClients.set(selectedIndice, result);
			}
		}
	}


	/**
     * Désactive le client sélectionné.
     */
	@FXML
	private void doDesactiverClient() {
	}


	 /**
     * Crée un nouveau client et l'ajoute à la liste des clients.
     */
	@FXML
	private void doNouveauClient() {
		Client client;
		client = this.cmDialogController.nouveauClient();
		if (client != null) {
			this.oListClients.add(client);
		}
	}


	/**
     * Valide l'état des composants de la fenêtre et active/désactive les boutons en conséquence.
     */
	private void validateComponentState() {
		this.btnDesactClient.setDisable(true);
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnModifClient.setDisable(false);
			this.btnComptesClient.setDisable(false);
		} else {
			this.btnModifClient.setDisable(true);
			this.btnComptesClient.setDisable(true);
		}
	}


	 /**
     * Gère les événements de clic sur la liste des clients pour afficher le menu contextuel et effectuer des actions.
     *
     * @param event L'événement de clic de la souris.
     */
	@FXML
    private void onClicList(MouseEvent event) {
		int selectedIndice = this.lvClients.getSelectionModel().getSelectedIndex();
        if(this.lvClients.getItems().size() != 0 && selectedIndice >= 0) {
            MouseButton mb = event.getButton();
            if(MouseButton.SECONDARY==mb) {
				this.contextMenu.hide();
                this.contextMenu = new ContextMenu();
				MenuItem menuItem1 = new MenuItem("Modifier");
				MenuItem menuItem2 = new MenuItem("Comptes client");
				menuItem1.setOnAction(e -> {
					this.doModifierClient();
				});
				menuItem2.setOnAction(e -> {
					this.doComptesClient();
				});
				this.contextMenu.getItems().addAll(menuItem1 ,menuItem2);
                this.contextMenu.show(this.lvClients , event.getScreenX(), event.getScreenY());
            }
            if(MouseButton.PRIMARY==mb) {
                this.contextMenu.hide();
                if(event.getClickCount() > 1) {
						this.doModifierClient();
                }
            }
        }
    }
}
