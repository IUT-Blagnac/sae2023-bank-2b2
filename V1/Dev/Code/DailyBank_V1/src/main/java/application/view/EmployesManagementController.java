package application.view;

import java.util.ArrayList;

import application.DailyBankState;
import application.control.EmployesManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Employe;

public class EmployesManagementController {
	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à EmployesManagementController
	private EmployesManagement emDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Employe> oListEmployes;

	/**
	 * Initialisation du contexte de la fenêtre de gestion des employés <br/>
	 * Cette méthode est appelée par le contrôleur de dialogue EmployesManagement
	 * lors de la création de la fenêtre <br/>
	 * ELle permet de passer les références nécessaires à la gestion des employés
	 * <br/>
	 * 
	 * @param _containingStage
	 * @param _em
	 * @param _dbstate
	 *
	 */
	public void initContext(Stage _containingStage, EmployesManagement _em, DailyBankState _dbstate) {
		this.emDialogController = _em;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
		this.oListEmployes.addAll(emDialogController.getlisteEmployes(-1, "", "", ""));
	}

	/**
	 * 
	 * Cette méthode est appelée par le contrôleur de dialogue EmployesManagement
	 * lors de la création de la fenêtre <br/>
	 * ELle permet de passer les références nécessaires à la gestion des employés et
	 * de lancer la configuration de la fenêtre <br/>
	 * 
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListEmployes = FXCollections.observableArrayList();
		this.lvEmployes.setItems(this.oListEmployes);
		this.lvEmployes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvEmployes.getFocusModel().focus(-1);
		this.lvEmployes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
		this.validateComponentState();
	}

	/**
	 * 
	 * Cette méthode est appelée par le contrôleur de dialogue EmployesManagement
	 * lors de la création de la fenêtre <br/>
	 * Elle permet d'afficher la fenêtre de gestion des employés <br/>
	 */
	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	/**
	 * 
	 * @param e
	 * @return null
	 */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	@FXML
	private TextField txtNum;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private MenuButton menuDroit;
	@FXML
	private RadioMenuItem droitChef;
	@FXML
	private RadioMenuItem droitGuichetier;
	@FXML
	private RadioMenuItem droitAucun;
	@FXML
	private ListView<Employe> lvEmployes;
	@FXML
	private Button btnSuprEmploye;
	@FXML
	private Button btnModifEmploye;
	@FXML
	private Button btnConsultEmploye;
	@FXML
	private Button btnNouvelEmploye;
	private ContextMenu contextMenu = new ContextMenu();

	/**
	 * Annule et ferme la fenêtre.
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	/**
	 * Valide l'état des composants de la fenêtre. Active ou désactive les boutons
	 * en fonction de l'employé sélectionné.
	 */
	private void validateComponentState() {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		Employe selectedEmploye = this.lvEmployes.getSelectionModel().getSelectedItem();
		Employe currentEmploye = this.dailyBankState.getEmployeActuel();

		if (selectedIndice >= 0) {
			if (selectedEmploye.droitsAccess.equals("chefAgence")) {
				if (selectedEmploye.toString().equals(currentEmploye.toString())) {
					this.btnConsultEmploye.setDisable(false);
					this.btnModifEmploye.setDisable(false);
					this.btnSuprEmploye.setDisable(true);
				} else {
					this.btnConsultEmploye.setDisable(false);
					this.btnModifEmploye.setDisable(true);
					this.btnSuprEmploye.setDisable(true);
				}
			} else {
				this.btnConsultEmploye.setDisable(false);
				this.btnModifEmploye.setDisable(false);
				this.btnSuprEmploye.setDisable(false);
			}
		} else {
			this.btnConsultEmploye.setDisable(true);
			this.btnModifEmploye.setDisable(true);
			this.btnSuprEmploye.setDisable(true);
		}
	}

	/**
	 * Recherche des employés en fonction des critères fournis. Met à jour la liste
	 * des employés dans la vue avec les résultats de la recherche.
	 */
	@FXML
	private void doRechercher() {
		int numEmploye;
		try {
			String nc = this.txtNum.getText();
			if (nc.equals("")) {
				numEmploye = -1;
			} else {
				numEmploye = Integer.parseInt(nc);
				if (numEmploye < 0) {
					this.txtNum.setText("");
					numEmploye = -1;
				}
			}
		} catch (NumberFormatException nfe) {
			this.txtNum.setText("");
			numEmploye = -1;
		}

		String debutNom = this.txtNom.getText();
		String debutPrenom = this.txtPrenom.getText();

		if (numEmploye != -1) {
			this.txtNom.setText("");
			this.txtPrenom.setText("");
		} else {
			if (debutNom.equals("") && !debutPrenom.equals("")) {
				this.txtPrenom.setText("");
			}
		}

		String droit;

		if (this.droitChef.isSelected()) {
			droit = "chefAgence";
		} else if (this.droitGuichetier.isSelected()) {
			droit = "guichetier";
		} else {
			droit = "";
		}

		ArrayList<Employe> listeEmployes;
		listeEmployes = this.emDialogController.getlisteEmployes(numEmploye, debutNom, debutPrenom, droit);
		this.oListEmployes.clear();
		this.oListEmployes.addAll(listeEmployes);
	}

	/**
	 * Modifie l'employé sélectionné.
	 */
	@FXML
	private void doModifierEmploye() {
		System.out.println("doModifierEmploye");
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe cliMod = this.oListEmployes.get(selectedIndice);
			Employe result = this.emDialogController.modifierEmploye(cliMod);
			if (result != null) {
				this.oListEmployes.set(selectedIndice, result);
			}
		}
	}

	/**
	 * Supprime l'employé sélectionné.
	 */
	@FXML
	private void doSupprimerEmploye() {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe cliSup = this.oListEmployes.get(selectedIndice);
			if (this.emDialogController.supprimerEmploye(cliSup)) {
				this.oListEmployes.remove(selectedIndice);
			}
		}
	}

	/**
	 * Consulte les détails de l'employé sélectionné.
	 */
	@FXML
	public void doConsulterEmploye() {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe cliCons = this.oListEmployes.get(selectedIndice);
			if (cliCons != null) {
				this.emDialogController.consult(cliCons);
			}
		}
	}

	/**
	 * Cette méthode est appelée lorsqu'un nouvel employé est créé. Elle affiche un
	 * dialogue pour créer un nouvel employé et l'ajoute à la liste des employés si
	 * celui-ci n'est pas nul.
	 */
	@FXML
	private void doNouvelEmploye() {
		Employe employe;
		employe = this.emDialogController.nouvelEmploye();
		if (employe != null) {
			this.oListEmployes.add(employe);
		}
	}

	/**
	 * Cette méthode est appelée lorsqu'on sélectionne l'option "Droit Chef". Elle
	 * met à jour le libellé du menu de droite avec le texte de l'option "Droit
	 * Chef".
	 */
	@FXML
	private void droitChef() {
		menuDroit.setText(this.droitChef.getText());
	}

	/**
	 * Cette méthode est appelée lorsqu'on sélectionne l'option "Droit Guichetier".
	 * Elle met à jour le libellé du menu de droite avec le texte de l'option "Droit
	 * Guichetier".
	 */
	@FXML
	private void droitGuichetier() {
		menuDroit.setText(this.droitGuichetier.getText());
	}

	/**
	 * Cette méthode est appelée lorsqu'on sélectionne l'option "Droit Aucun". Elle
	 * met à jour le libellé du menu de droite avec le texte de l'option "Droit
	 * Aucun".
	 */
	@FXML
	private void droitAucun() {
		menuDroit.setText(this.droitAucun.getText());
	}

	/**
	 * Cette méthode est appelée lorsqu'on clique sur la liste des employés. Elle
	 * affiche un menu contextuel avec des options en fonction de l'employé
	 * sélectionné et de l'employé actuel.
	 *
	 * @param event L'événement de la souris associé au clic sur la liste des
	 *              employés.
	 */
	@FXML
	private void onClicList(MouseEvent event) {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (lvEmployes.getItems().size() != 0 && selectedIndice >= 0) {
			MouseButton mb = event.getButton();
			if (MouseButton.SECONDARY == mb) {
				Employe selectedEmploye = this.lvEmployes.getSelectionModel().getSelectedItem();
				Employe currentEmploye = this.dailyBankState.getEmployeActuel();
				contextMenu.hide();
				contextMenu = new ContextMenu();
				if (selectedEmploye.toString().equals(currentEmploye.toString())
						|| selectedEmploye.droitsAccess.equals("guichetier")) {
					MenuItem menuItem1 = new MenuItem("Modifier");
					menuItem1.setOnAction(e -> {
						doModifierEmploye();
					});
					contextMenu.getItems().add(menuItem1);
					if (!selectedEmploye.toString().equals(currentEmploye.toString())) {
						MenuItem menuItem2 = new MenuItem("Supprimer");
						menuItem2.setOnAction(e -> {
							doSupprimerEmploye();
						});
						contextMenu.getItems().add(menuItem2);
					}
					MenuItem menuItem3 = new MenuItem("Consulter");
					menuItem3.setOnAction(e -> {
						doConsulterEmploye();
					});
					contextMenu.getItems().add(menuItem3);
				} else {
					MenuItem menuItem = new MenuItem("Consulter");
					menuItem.setOnAction(e -> {
						doConsulterEmploye();
					});
					contextMenu.getItems().add(menuItem);
				}
				contextMenu.show(lvEmployes, event.getScreenX(), event.getScreenY());
			}
			if (MouseButton.PRIMARY == mb) {
				contextMenu.hide();
				if (event.getClickCount() > 1) {
					Employe selectedEmploye = this.lvEmployes.getSelectionModel().getSelectedItem();
					Employe currentEmploye = this.dailyBankState.getEmployeActuel();
					if (selectedEmploye.toString().equals(currentEmploye.toString())
							|| selectedEmploye.droitsAccess.equals("guichetier")) {
						doModifierEmploye();
					} else {
						doConsulterEmploye();
					}
				}
			}
		}
	}
}
