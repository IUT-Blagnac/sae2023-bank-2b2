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
	public void initContext(Stage _containingStage, ComptesManagement _cm, DailyBankState _dbstate, Client client) {
		this.cmDialogController = _cm;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.clientDesComptes = client;
		this.configure();
	}

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

	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage
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

	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

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
	@FXML
	private void doNouveauCompte() {
		CompteCourant compte;
		compte = this.cmDialogController.creerNouveauCompte();
		if (compte != null) {
			this.oListCompteCourant.add(compte);
		}
	}

	@FXML
    private void onClicList(MouseEvent event) {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		CompteCourant compteSelected = this.oListCompteCourant.get(selectedIndice);
        if(lvComptes.getItems().size() != 0 && selectedIndice >= 0) {
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

	private void loadList() {
		ArrayList<CompteCourant> listeCpt;
		listeCpt = this.cmDialogController.getComptesDunClient();
		this.oListCompteCourant.clear();
		this.oListCompteCourant.addAll(listeCpt);
	}

	private void validateComponentState() {
		// Non implémenté => désactivé


		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			this.btnVoirOpes.setDisable(false);
			if(!this.lvComptes.getSelectionModel().getSelectedItem().isCloture()) {
				this.btnModifierCompte.setDisable(false);
				this.btnSupprCompte.setDisable(false);	
			}		
		} else {
			this.btnVoirOpes.setDisable(true);
			this.btnModifierCompte.setDisable(true);
			this.btnSupprCompte.setDisable(true);
		}
	}
}
