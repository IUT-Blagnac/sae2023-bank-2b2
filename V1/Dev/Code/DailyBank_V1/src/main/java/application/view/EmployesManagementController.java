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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
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

    @FXML
	private void doCancel() {
		this.primaryStage.close();
	}

    private void validateComponentState() {
		// Non implémenté => désactivé
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
		System.out.println("doModifierEmploye");
        // Non implémenté => désactivé
		// int selectedIndice = this.lvEmployes.getSelectionModel().getSelectedIndex();
		// if (selectedIndice >= 0) {
		// 	Employe cliMod = this.oListEmployes.get(selectedIndice);
		// 	Employe result = this.emDialogController.modifier(cliMod);
		// 	if (result != null) {
		// 		this.oListClients.set(selectedIndice, result);
		// 	}
		// }
	}

	@FXML
	private void doSupprimerEmploye() {
		System.out.println("doSuppirmerEmploye");
        // Non implémenté => désactivé
	}

	@FXML
	private void doNouvelEmploye() {
		System.out.println("doNouvelEmploye");
        // Non implémenté => désactivé
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
}
