package application.view;

import java.net.URL;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.control.EmployesManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Employe;

public class EmployesManagementController implements Initializable{
    // Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à EmployesManagementController
	private EmployesManagement cmDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	private ObservableList<Employe> oListEmployes;

    // Manipulation de la fenêtre
	public void initContext(Stage _containingStage, EmployesManagement _em, DailyBankState _dbstate) {
		this.cmDialogController = _em;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
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
	private ListView<Employe> lvEmployes;
	@FXML
	private Button btnDesactClient;
	@FXML
	private Button btnModifClient;
	@FXML
	private Button btnComptesClient;

    @FXML
	private void doCancel() {
		this.primaryStage.close();
	}

    private void validateComponentState() {
		// Non implémenté => désactivé
	}

    @FXML
	private void doRechercher() {
        // Non implémenté => désactivé
	}

	@FXML
	private void doComptesClient() {
        // Non implémenté => désactivé
	}

	@FXML
	private void doModifierClient() {
        // Non implémenté => désactivé
	}

	@FXML
	private void doDesactiverClient() {
        // Non implémenté => désactivé
	}

	@FXML
	private void doNouveauClient() {
        // Non implémenté => désactivé
	}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
}
