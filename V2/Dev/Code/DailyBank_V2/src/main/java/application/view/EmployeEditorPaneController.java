package application.view;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Employe;

public class EmployeEditorPaneController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre

	private Employe employeEdite;
	private EditionMode editionMode;
	private Employe employeResultat;


	/**
	 * Cette méthode est appelée par le contrôleur de dialogue EmployesManagement <br/>
	 * pour initialiser le contexte de la fenêtre de dialogue de création ou de modification d'un employé <br/>
	 * @author Enzo Fournet
	 * @param _containingStage
	 * @param _dbstate
	 */
	public void initContext(Stage _containingStage, DailyBankState _dbstate) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
	}

	/**
	 * Cette méthode est appelée par le contrôleur de dialogue EmployesManagement <br/>
	 * Elle permet de fermer la fenêtre de dialogue de création ou de modification d'un employé <br/>
	 *
	 * @author Enzo Fournet
	 *
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}


	/**
	 *
	 * Cette méthode est appelée par le contrôleur de dialogue EmployesManagement <br/>
	 * pour afficher la fenêtre de dialogue de création ou de modification d'un employé <br/>
	 *
	 * @author Enzo Fournet
	 * @param employe
	 * @param mode
	 *
	 * @return Employe
	 */
	public Employe displayDialog(Employe employe, EditionMode mode) {

		this.editionMode = mode;
		if (employe == null) {
			//construction d'un employé par défaut
			this.employeEdite = new Employe(0, "", "", "", "", "", this.dailyBankState.getAgenceActuelle().idAg);
		} else {
			this.employeEdite = new Employe(employe);
		}
		this.employeResultat = null;
		switch (mode) {
		case CREATION:
			this.txtIdEmpl.setDisable(true);
			this.txtNom.setDisable(false);
			this.txtPrenom.setDisable(false);
			this.menuBtnDroitAccess.setDisable(false);
			this.txtLogin.setDisable(false);
			this.txtMotPasse.setDisable(false);
			this.txtConfrMotPasse.setDisable(false);
			this.txtIdAgence.setDisable(true);

			this.lblMessage.setText("Informations sur le nouvel employé");
			this.butOk.setText("Ajouter");
			this.butCancel.setText("Annuler");
			break;
		case MODIFICATION:
			this.txtIdEmpl.setDisable(true);
			this.txtNom.setDisable(false);
			this.txtPrenom.setDisable(false);
			this.menuBtnDroitAccess.setDisable(false);
			if (employe.droitsAccess.toString().equals("chefAgence")) {
				this.menuBtnDroitAccess.setText("Chef d'agence");
				this.radioBtnChefAgence.setSelected(true);
			} else {
				this.menuBtnDroitAccess.setText("Guichetier");
				this.radioBtnGuichetier.setSelected(true);
			}

			this.txtLogin.setDisable(false);
			this.txtMotPasse.setDisable(false);
			this.txtConfrMotPasse.setDisable(false);
			this.txtIdAgence.setDisable(true);

			this.lblMessage.setText("Modifications employé");
			this.butOk.setText("Modifier");
			this.butCancel.setText("Annuler");
			break;
		case CONSULTATION:
			this.txtIdEmpl.setDisable(true);
			this.txtNom.setDisable(true);
			this.txtPrenom.setDisable(true);
			this.menuBtnDroitAccess.setDisable(true);
			if (employe.droitsAccess.toString().equals("chefAgence")) {
				this.menuBtnDroitAccess.setText("Chef d'agence");
			} else {
				this.menuBtnDroitAccess.setText("Guichetier");
			}
			this.txtLogin.setDisable(true);
			this.txtMotPasse.setDisable(true);
			this.txtConfrMotPasse.setDisable(true);
			this.txtIdAgence.setDisable(true);

			this.lblMessage.setText("Consultation employé");
			this.butOk.setText("OK");
			this.butCancel.setVisible(false);
			break;
		case SUPPRESSION:
			this.txtIdEmpl.setDisable(true);
			this.txtNom.setDisable(true);
			this.txtPrenom.setDisable(true);
			this.menuBtnDroitAccess.setDisable(true);
			if (employe.droitsAccess.toString().equals("chefAgence")) {
				this.menuBtnDroitAccess.setText("Chef d'agence");
			} else {
				this.menuBtnDroitAccess.setText("Guichetier");
			}
			this.txtLogin.setDisable(true);
			this.txtMotPasse.setDisable(true);
			this.txtConfrMotPasse.setDisable(true);
			this.txtIdAgence.setDisable(true);

			this.lblMessage.setText("Suppression d'un employé");
			this.butOk.setText("Supprimer");
			this.butCancel.setText("Annuler");
			break;
		}
		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dailyBankState.getEmployeActuel())) {
			// rien pour l'instant
		}
		// initialisation du contenu des champs
		this.txtIdEmpl.setText("" + this.employeEdite.idEmploye);
		this.txtNom.setText(this.employeEdite.nom);
		this.txtPrenom.setText(this.employeEdite.prenom);
		this.txtLogin.setText(this.employeEdite.login);
		this.txtMotPasse.setText(this.employeEdite.motPasse);
		this.txtConfrMotPasse.setText(this.employeEdite.motPasse);
		this.txtIdAgence.setText("" + this.employeEdite.idAg);



		this.employeResultat = null;

		this.primaryStage.showAndWait();
		return this.employeResultat;
	}

	/**
	 * Méthodes de gestion des événements <br/>
	 *
	 * @author Enzo Fournet
	 */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions

	@FXML
	private Label lblMessage;
	@FXML
	private TextField txtIdEmpl;
	@FXML
	private TextField txtNom;
	@FXML
	private TextField txtPrenom;
	@FXML
	private MenuButton menuBtnDroitAccess;
	@FXML
	private ToggleGroup toggleGroupDroitAccess;
	@FXML
	private RadioMenuItem radioBtnChefAgence;
	@FXML
	private RadioMenuItem radioBtnGuichetier;
	@FXML
	private TextField txtLogin;
	@FXML
	private PasswordField txtMotPasse;
	@FXML
	private PasswordField txtConfrMotPasse;
	@FXML
	private TextField txtIdAgence;
	@FXML
	private Button butOk;
	@FXML
	private Button butCancel;



	/**
	 * Cette méthode est appelée lorsque la fenêtre est fermée par l'utilisateur <br/>
	 * Elle permet de gérer la fermeture de la fenêtre en renvoyant un employé null <br/>
	 *
	 * @author Enzo Fournet
	 */
	@FXML
	private void doCancel() {
		this.employeResultat = null;
		this.primaryStage.close();
	}


	/**
	 * Cette méthode est appelée lorsque l'utilisateur clique sur le bouton 'Ok' <br/>
	 * Elle permet de gérer la validation de la saisie en renvoyant un employé valide <br/>
	 *
	 * @author Enzo Fournet
	 */
	@FXML
	private void doAjouter() {
		switch (this.editionMode) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.employeResultat = this.employeEdite;
				this.primaryStage.close();
			}
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.employeResultat = this.employeEdite;
				this.primaryStage.close();
			}
			break;
		case SUPPRESSION:
			this.employeResultat = this.employeEdite;
			this.primaryStage.close();
			break;
		case CONSULTATION:
			this.employeResultat = null;
			this.primaryStage.close();
			break;
		}

	}

	/**
	 * Vérifie que les données saisies sont valides <br/>
	 *
	 * @author Enzo Fournet
	 * @return	true si les données sont valides, false si non
	 */
	private boolean isSaisieValide() {
		this.employeEdite.nom = this.txtNom.getText().trim();
		this.employeEdite.prenom = this.txtPrenom.getText().trim();
		if (this.toggleGroupDroitAccess.getSelectedToggle() != null) {
			RadioMenuItem rb = (RadioMenuItem) this.toggleGroupDroitAccess.getSelectedToggle();

			if (rb.getText().equals("Chef d'agence")) {
				this.employeEdite.droitsAccess = "chefAgence";
			} else {
				this.employeEdite.droitsAccess = "guichetier";
			}
		}

		this.employeEdite.login = this.txtLogin.getText().trim();
		this.employeEdite.motPasse = this.txtConfrMotPasse.getText().trim();
		this.employeEdite.idAg = Integer.parseInt(this.txtIdAgence.getText().trim());

		if (this.employeEdite.nom.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le nom ne doit pas être vide",
					AlertType.WARNING);
			this.txtNom.requestFocus();
			return false;
		}
		if (this.employeEdite.prenom.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le prénom ne doit pas être vide",
					AlertType.WARNING);
			this.txtPrenom.requestFocus();
			return false;
		}
		if(this.toggleGroupDroitAccess.getSelectedToggle() == null) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Un droit d'accès doit être sélectionné",
					AlertType.WARNING);
			this.txtPrenom.requestFocus();
			return false;
		}
		if (this.employeEdite.login.isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le login ne doit pas être vide",
					AlertType.WARNING);
			this.txtLogin.requestFocus();
			return false;
		}
		this.employeEdite.login = this.txtLogin.getText().trim();
		if (this.txtMotPasse.getText().trim().isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le mot de passe ne doit pas être vide",
					AlertType.WARNING);
			this.txtMotPasse.requestFocus();
			return false;
		}
		if (!this.txtMotPasse.getText().trim().equals(this.txtConfrMotPasse.getText().trim())) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le mot de passe et sa confirmation doivent être identiques",
					AlertType.WARNING);
			this.txtMotPasse.requestFocus();
			return false;
		}
		if (this.txtMotPasse.getText().trim().length() < 8 || this.txtMotPasse.getText().trim().length() > 16) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le mot de passe doit contenir entre 8 et 16 caractères",
					AlertType.WARNING);
			this.txtMotPasse.requestFocus();
			return false;
		}
		if (!this.isValid(this.txtMotPasse.getText().trim())) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Le mot de passe ne doit contenir que des caractères alphanumériques et les caractères spéciaux suivants : /.?,;:+=%$*()[]&#@!çèàé",
					AlertType.WARNING);
			return false;
		}
		if (this.txtConfrMotPasse.getText().trim().isEmpty()) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "La confirmation du mot de passe ne doit pas être vide, n'y le mot de passe",
					AlertType.WARNING);
			this.txtConfrMotPasse.requestFocus();
			return false;
		}


		return true;
	}

	/**
	 * Vérifie que la chaîne de caractères passée en paramètre ne contient que des caractères alphanumériques <br/>
	 * et les caractères spéciaux autorisés <br/>
	 * @param s
	 * @return
	 */
	public boolean isValid(String s) {
		return s.matches("[a-zA-Z0-9/.?,;:+=%$*()\\[\\]&#@!çèàé]*");
	}

	/**
	 * Est appelée lorsque l'utilisateur sélectionne un droit d'accès en l'occuernce un chef d'agence <br/>
	 * Elle permet de mettre à jour le menu déroulant des droits d'accès pour afficher le droit d'accès sélectionné <br/>
	 */
	@FXML
	private void radioBtnChefAgence() {
		this.menuBtnDroitAccess.setText(this.radioBtnChefAgence.getText());
	}

	/**
	 * Est appelée lorsque l'utilisateur sélectionne un droit d'accès en l'occuernce un guichetier <br/>
	 * Elle permet de mettre à jour le menu déroulant des droits d'accès pour afficher le droit d'accès sélectionné <br/>
	 */
	@FXML
	private void radioBtnGuichetier() {
		this.menuBtnDroitAccess.setText(this.radioBtnGuichetier.getText());
	}

}
