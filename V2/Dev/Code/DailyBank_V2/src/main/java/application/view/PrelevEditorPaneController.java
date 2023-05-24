package application.view;

import java.util.Locale;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.CompteCourant;
import model.data.Prelevement;

	/**
	@author yannis gibert
	La classe PrelevementEditorPaneController est un contrôleur qui gère l'affichage et la modification des informations de prelevement automatique pour un compte.
	
	Elle offre des fonctionnalités pour créer, modifier ou supprimer un prelevement automatique.
	*/
public class PrelevEditorPaneController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre
	
	
	private EditionMode editionMode;
	private CompteCourant compteDuPrelev;
	private Prelevement prelevementEditer;
	private Prelevement prelevementResultat;

	// Manipulation de la fenêtre
	
	/**
	 * @author yannis gibert
	 * Initialise le contexte du contrôleur avec la fenêtre principale et l'état de l'application.
	 *
	 * @param _containingStage La fenêtre principale contenant la scène
	 * @param _dbstate L'état courant de l'application
	 */
	public void initContext(Stage _containingStage, DailyBankState _dbstate) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
	}

	/**
	 * @author yannis gibert
	 * Configure la fenêtre en définissant les gestionnaires d'événements pour la fermeture de la fenêtre
	 * et le focus sur les champs de découvert autorisé et de solde.
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.txtDatePrelev.focusedProperty().addListener((t, o, n) -> this.focusDatePrelev(t, o, n));
		this.txtmontant.focusedProperty().addListener((t, o, n) -> this.focusMontantPrelev(t, o, n));
		this.txtbeneficiaire.focusedProperty().addListener((t,o,n) -> this.focusBeneficiaire(t,o,n));
	}

	/**
	@author yannis gibert
	Affiche la boîte de dialogue pour la création, la modification ou la suppression d'un prelevement automatique.

	@param compte le compte associé au prelevement

	@param prlv le prevelement à afficher/modifier/supprimer

	@param mode Le mode d'édition (CREATION, MODIFICATION, SUPPRESSION)

	@return Le prelevement résultant après l'opération ou null si l'opération a été annulée
	*/
	
	public Prelevement displayDialog(CompteCourant compte, Prelevement prlv, EditionMode mode) {
		this.compteDuPrelev = compte;
		this.editionMode = mode;
		if (prlv == null) {
			this.prelevementEditer = new Prelevement(0, 0, this.compteDuPrelev.idNumCompte, 01, null);
		} else {
			this.prelevementEditer = new Prelevement(prlv);
		}
		this.prelevementResultat = null;
		this.txtIdprelev.setDisable(true);
		this.txtIdNumCompte.setDisable(true);
		switch (mode) {
		case CREATION:
			this.txtmontant.setDisable(false);
			this.txtDatePrelev.setDisable(false);
			this.txtbeneficiaire.setDisable(false);
			this.lblMessage.setText("Informations sur le nouveau prélèvement automatique");
			this.btnOk.setText("Ajouter");
			this.btnCancel.setText("Annuler");
			break;
		case MODIFICATION:
			this.txtmontant.setDisable(false);
			this.txtDatePrelev.setDisable(false);
			this.txtbeneficiaire.setDisable(false);
			this.lblMessage.setText("Modification du prélèvement");
			this.btnOk.setText("Modifier");
			this.btnCancel.setText("Annuler");
			break;
		case SUPPRESSION:
			this.txtmontant.setDisable(true);
			this.txtDatePrelev.setDisable(true);
			this.txtbeneficiaire.setDisable(true);
			this.lblMessage.setText("Supression du prélèvement");
			this.btnOk.setText("Supprimer");
			this.btnCancel.setText("Annuler");
			break;
		}

		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dailyBankState.getEmployeActuel())) {
			// rien pour l'instant
		}

		// initialisation du contenu des champs
		this.txtIdprelev.setText("" + this.prelevementEditer.idNumPrelev);
		this.txtIdNumCompte.setText("" + this.prelevementEditer.idNumCompte);
		this.txtmontant.setText(""+this.prelevementEditer.debitPrelev);
		this.txtbeneficiaire.setText("" + this.prelevementEditer.beneficiaire);
		this.txtDatePrelev.setText(""+this.prelevementEditer.datePrelev);

		this.prelevementResultat = null;

		this.primaryStage.showAndWait();
		return this.prelevementResultat;
	}

	// Gestion du stage
	
	/**
	 * @author yannis gibert
	 * Gère l'événement de fermeture de la fenêtre en annulant l'opération en cours.
	 *
	 * @param e L'événement de fermeture de la fenêtre
	 * @return null
	 */
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}
	
	/**
	 * @author yannis gibert
	 * Gère l'événement de focus sur le champ de beneficiaire en mettant à jour le beneficiaire du prelevement automatique.
	 *
	 * @param txtField Le champ de texte du découvert autorisé
	 * @param oldPropertyValue La valeur précédente de la propriété "focused"
	 * @param newPropertyValue La nouvelle valeur de la propriété "focused"
	 * @return null
	 */
	private Object focusBeneficiaire(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				String val;
				val = this.txtbeneficiaire.getText().trim();
				this.prelevementEditer.beneficiaire = val;
			} catch (NumberFormatException nfe) {
				this.txtbeneficiaire.setText("" + this.prelevementEditer.beneficiaire);
			}
		}
		return null;
	}

	/**
	 * @author yannis gibert
	 * Gère l'événement de focus sur le champ de date prélèvement en mettant à jour la valeur du date occurence du prélèvement automatique.
	 *
	 * @param txtField Le champ de texte du découvert autorisé
	 * @param oldPropertyValue La valeur précédente de la propriété "focused"
	 * @param newPropertyValue La nouvelle valeur de la propriété "focused"
	 * @return null
	 */
	private Object focusDatePrelev(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				int val;
				val = Integer.parseInt(this.txtDatePrelev.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.prelevementEditer.datePrelev = val;
			} catch (NumberFormatException nfe) {
				this.txtDatePrelev.setText("" + this.prelevementEditer.datePrelev);
			}
		}
		return null;
	}

	/**
	 * @author yannis gibert
	 * Gère l'événement de focus sur le champ de montant en mettant à jour la valeur du montant du prelevement automatique.
	 *
	 * @param txtField Le champ de texte du solde
	 * @param oldPropertyValue La valeur précédente de la propriété "focused"
	 * @param newPropertyValue La nouvelle valeur de la propriété "focused"
	 * @return null
	 */
	private Object focusMontantPrelev(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				double val;
				val = Double.parseDouble(this.txtmontant.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.prelevementEditer.debitPrelev = val;
			} catch (NumberFormatException nfe) {
				this.txtmontant.setText("" + this.prelevementEditer.debitPrelev);
			}
		}
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private TextField txtIdprelev;
	@FXML
	private TextField txtmontant;
	@FXML
	private TextField txtIdNumCompte;
	@FXML
	private TextField txtbeneficiaire;
	@FXML
	private TextField txtDatePrelev;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;


	
	/**
	 * @author yannis gibert
	 * Gère l'action de l'annulation de l'opération en cours.
	 * Réinitialise le compte résultat et ferme la fenêtre principale.
	 */
	@FXML
	private void doCancel() {
		this.prelevementResultat = null;
		this.primaryStage.close();
	}

	

	/**
	 * @author yannis gibert
	 * Gère l'action du bouton "Ajouter" en fonction du mode d'édition.
	 * Vérifie la validité de la saisie, attribue le compte édité au compte résultat et ferme la fenêtre principale.
	 */
	@FXML
	private void doAjouter() {
		switch (this.editionMode) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.prelevementResultat = this.prelevementEditer;
				this.primaryStage.close();
			}
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.prelevementResultat = this.prelevementEditer;
				this.primaryStage.close();
			}
			break;
		case SUPPRESSION:
			this.prelevementResultat = this.prelevementEditer;
			this.primaryStage.close();
			break;
		}

	}
	
	/**
	 * @author yannis gibert
	 * Vérifie si la saisie des champs est valide.
	 *
	 * @return true si la saisie est valide, false sinon
	 */
	private boolean isSaisieValide() {
		this.prelevementEditer.beneficiaire = this.txtbeneficiaire.getText();
		if (this.prelevementEditer.beneficiaire.equals("null")) {
			AlertUtilities.showAlert(this.primaryStage, "Erreur de saisie", null, "Veuillez saisir un bénéficiaire",
					AlertType.WARNING);
			this.txtbeneficiaire.requestFocus();
			return false;
		}
		return true;
	}
}
