package application.view;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.EmployesManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
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

    // Manipulation de la fenêtre
	public void initContext(Stage _containingStage, EmployesManagement _em, DailyBankState _dbstate) {
		this.emDialogController = _em;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
        this.oListEmployes.addAll(emDialogController.getlisteEmployes(-1, "", "", ""));
	}

    private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListEmployes = FXCollections.observableArrayList();
		this.lvEmployes.setItems(this.oListEmployes);
		this.lvEmployes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvEmployes.getFocusModel().focus(-1);
		this.lvEmployes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());
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
	private Button btnNouvelEmploye;
	private ContextMenu contextMenu = new ContextMenu();

    @FXML
	private void doCancel() {
		this.primaryStage.close();
	}

    private void validateComponentState() {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		Employe selectedEmploye = this.lvEmployes.getSelectionModel().getSelectedItem();
		System.out.println(selectedEmploye);
		Employe currentEmploye=  this.dailyBankState.getEmployeActuel();
		System.out.println(currentEmploye);
	
		if (selectedIndice >= 0 && (selectedEmploye.toString().equals(currentEmploye.toString()) || selectedEmploye.droitsAccess.equals("guichetier"))) {	
			this.btnModifEmploye.setDisable(false);
			if (!selectedEmploye.toString().equals(currentEmploye.toString())) {
				this.btnSuprEmploye.setDisable(false);
			}			
		} else {
			System.out.println("true");
			this.btnModifEmploye.setDisable(true);
			this.btnSuprEmploye.setDisable(true);
		}
	}

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

		if(this.droitChef.isSelected()) {
			droit = "chefAgence";
		} else if(this.droitGuichetier.isSelected()) {
			droit = "guichetier";
		} else {
			droit = "";
		}

		ArrayList<Employe> listeEmployes;
		listeEmployes = this.emDialogController.getlisteEmployes(numEmploye, debutNom, debutPrenom, droit);
		this.oListEmployes.clear();
		this.oListEmployes.addAll(listeEmployes);
	}

	@FXML
	private void doModifierEmploye() {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			Employe cliMod = this.oListEmployes.get(selectedIndice);
			Employe result = this.emDialogController.modifierEmploye(cliMod);
			if (result != null) {
				this.oListEmployes.set(selectedIndice, result);
			}
		}
	}

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

	@FXML
	private void doNouvelEmploye() {
		Employe client;
		client = this.emDialogController.nouvelEmploye();
		if (client != null) {
			this.oListEmployes.add(client);
		}
	}

	@FXML
	private void droitChef() {
		menuDroit.setText(this.droitChef.getText());
	} 
	@FXML
	private void droitGuichetier() {
		menuDroit.setText(this.droitGuichetier.getText());
	}
	@FXML
	private void droitAucun() {
		menuDroit.setText(this.droitAucun.getText());
	}

	@FXML
    private void onClicList(MouseEvent event) {
		int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
        if(lvEmployes.getItems().size() != 0 && selectedIndice >= 0) {
            MouseButton mb = event.getButton();
            if(MouseButton.SECONDARY==mb) {
				Employe selectedEmploye = this.lvEmployes.getSelectionModel().getSelectedItem();
				Employe currentEmploye=  this.dailyBankState.getEmployeActuel();
				contextMenu.hide();
                contextMenu = new ContextMenu();
				if (selectedEmploye.toString().equals(currentEmploye.toString()) || selectedEmploye.droitsAccess.equals("guichetier")) {
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
				} else {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information - Vous n'avez pas le droit");
					alert.setHeaderText(null);
					alert.setContentText("Vous n'avez pas le droit de modifier ou supprimer cet employé");
					alert.showAndWait();
				}
                contextMenu.show(lvEmployes , event.getScreenX(), event.getScreenY());
            }
            if(MouseButton.PRIMARY==mb) {
                contextMenu.hide();
                if(event.getClickCount() > 1) {
					Employe selectedEmploye = this.lvEmployes.getSelectionModel().getSelectedItem();
					Employe currentEmploye=  this.dailyBankState.getEmployeActuel();
					if (selectedEmploye.toString().equals(currentEmploye.toString()) || selectedEmploye.droitsAccess.equals("guichetier")) {
						doModifierEmploye();
					}
                    
                }
            }
        }
    }
}
