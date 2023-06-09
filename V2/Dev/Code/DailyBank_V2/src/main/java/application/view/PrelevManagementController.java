package application.view;

import java.util.ArrayList;

import application.DailyBankState;
import application.control.PrelevManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.CompteCourant;
import model.data.Prelevement;


/*
 * @author yannis gibert
 * Cette classe est le contrôleur de la gestion des prelevements.
 * Elle gère les interactions avec l'interface graphique et effectue les opérations correspondantes.
 */
public class PrelevManagementController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à PrelevManagement
	private PrelevManagement cmDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private CompteCourant prelevDuCompte;
	private ObservableList<Prelevement> oListPrelevement;

	// Choix de l'emplacement du fichier à enregistrer
	FileChooser fileChooser = new FileChooser();

	// Manipulation de la fenêtre

	/**
	 * 
	 * Initialise le contexte du contrôleur avec les paramètres fournis.
	 * @param _containingStage La fenêtre physique contenant la scène
	 * @param _cm Le contrôleur de dialogue associé
	 * @param _dbstate L'état courant de l'application
	 * @param compte le compte associé aux prelevements
	 * @author yannis gibert
	 */
	public void initContext(Stage _containingStage, PrelevManagement _cm, DailyBankState _dbstate, CompteCourant compte) {
		this.cmDialogController = _cm;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.prelevDuCompte = compte;
		this.configure();
	}


	/**
	 * 
	 * Configure les éléments de l'interface graphique.
	 * @author yannis gibert
	 */
	private void configure() {
		String info;

		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListPrelevement = FXCollections.observableArrayList();
		this.lvPrelevement.setItems(this.oListPrelevement);
		this.lvPrelevement.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvPrelevement.getFocusModel().focus(-1);
		this.lvPrelevement.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());

		info = this.prelevDuCompte.idNumCompte + "  " + "  (id : "
				+ this.prelevDuCompte.idNumCli + ")";
		this.lblInfosCompte.setText(info);

		this.loadList();
		this.validateComponentState();
	}

	/**
	 * 
	 * Affiche la fenêtre de gestion des comptes.
	 * @author yannis gibert
	 */
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}


	/**
	 * 
	 * Gère l'événement de fermeture de la fenêtre en annulant ce qui était en cours.
	 *
	 * @param e L'événement de fermeture de la fenêtre
	 * @return null
	 * @author yannis gibert
	 */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions

	@FXML
	private Label lblInfosCompte;
	@FXML
	private ListView<Prelevement> lvPrelevement;
	@FXML
	private Button btnModifierPrelev;
	@FXML
	private Button btnSupprPrelev;
	@FXML
	private Button btnAjoutPrelev;
	private ContextMenu contextMenu = new ContextMenu();

	/**
	 * 
	 * Annule et ferme la fenêtre de gestion des prelevements.
	 * @author yannis gibert
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	//Modification d'un prelevement

	/**
	 * 
	 * Modifie un prelevement sélectionné.
	 * Charge ensuite la liste des prelevements et met à jour l'état des composants.
	 * @author yannis gibert
	 */
	@FXML
	private void doModifierPrelev() {
		int selectedIndice = this.lvPrelevement.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Prelevement prelevMod = this.oListPrelevement.get(selectedIndice);
			Prelevement result = this.cmDialogController.modifierPrelevement(prelevMod);
			if (result != null) {
				this.oListPrelevement.set(selectedIndice, result);
			}
		}
		this.loadList();
		this.validateComponentState();
	}

	//Suppression d'un prelevement

	/**
	 * 
	 * Supprime un prelevement sélectionné.
	 * Charge ensuite la liste des prelevements et met à jour l'état des composants.
	 * @author yannis gibert
	 */
	@FXML
	private void doSupprimerPrelev() {
		int selectedItem = this.lvPrelevement.getSelectionModel().getSelectedIndex();
		if(selectedItem >= 0) {
			Prelevement prelev = this.oListPrelevement.get(selectedItem);
			this.cmDialogController.supprimerPrelevement(prelev);
		}
		this.loadList();
		this.validateComponentState();
	}

	//Ajout d'un nouveau prelevement

	/**
	 * 
	 * Ajoute un nouveau prelevement
	 * Charge ensuite la liste des prelevements.
	 * @author yannis gibert
	 */
	@FXML
	private void doNouveauPrelev() {
		Prelevement prelev;
		prelev = this.cmDialogController.creerNouveauPrelev();
		if (prelev != null) {
			this.oListPrelevement.add(prelev);
		}
	}


	/**
	 * 
	 * Charge la liste des prelevements du cmopte et l'affiche dans la vue.
	 * @author yannis gibert
	 */
	private void loadList() {
		ArrayList<Prelevement> listePrelev;
		listePrelev = this.cmDialogController.getPrelevementDunCompte();
		this.oListPrelevement.clear();
		this.oListPrelevement.addAll(listePrelev);
	}

	/**
	 * Valide l'état des composants en fonction de la sélection d'un prelevement.
	 * Active ou désactive les boutons en conséquence.
	 */
	private void validateComponentState() {
		int selectedIndice = this.lvPrelevement.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {

			this.btnModifierPrelev.setDisable(false);
			this.btnSupprPrelev.setDisable(false);

		} else {
			this.btnModifierPrelev.setDisable(true);
			this.btnSupprPrelev.setDisable(true);
		}
	}
}
