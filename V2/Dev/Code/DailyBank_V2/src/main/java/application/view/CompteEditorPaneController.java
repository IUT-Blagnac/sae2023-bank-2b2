package application.view;

import java.util.Locale;

import application.DailyBankState;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;

	/**

	La classe CompteEditorPaneController est un contrôleur qui gère l'affichage et la modification des informations de compte bancaire pour un client.

	Elle offre des fonctionnalités pour créer, modifier ou supprimer un compte bancaire.
	*/
public class CompteEditorPaneController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	// Données de la fenêtre


	private EditionMode editionMode;
	private Client clientDuCompte;
	private CompteCourant compteEdite;
	private CompteCourant compteResultat;

	// Manipulation de la fenêtre

	/**
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
	 * Configure la fenêtre en définissant les gestionnaires d'événements pour la fermeture de la fenêtre
	 * et le focus sur les champs de découvert autorisé et de solde.
	 */
	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.txtDecAutorise.focusedProperty().addListener((t, o, n) -> this.focusDecouvert(t, o, n));
		this.txtSolde.focusedProperty().addListener((t, o, n) -> this.focusSolde(t, o, n));
	}

	/**
	
	Affiche la boîte de dialogue pour la création, la modification ou la suppression d'un compte courant.

	@param client Le client associé au compte

	@param cpte Le compte courant à afficher/modifier/supprimer

	@param mode Le mode d'édition (CREATION, MODIFICATION, SUPPRESSION)

	@return Le compte courant résultant après l'opération ou null si l'opération a été annulée
	@author yannis gibert
	*/

	public CompteCourant displayDialog(Client client, CompteCourant cpte, EditionMode mode) {
		this.clientDuCompte = client;
		this.editionMode = mode;
		if (cpte == null) {
			this.compteEdite = new CompteCourant(0, 200, 0, "N", this.clientDuCompte.idNumCli);
		} else {
			this.compteEdite = new CompteCourant(cpte);
		}
		this.compteResultat = null;
		this.txtIdclient.setDisable(true);
		this.txtIdAgence.setDisable(true);
		this.txtIdNumCompte.setDisable(true);
		switch (mode) {
		case CREATION:
			this.txtDecAutorise.setDisable(false);
			this.txtSolde.setDisable(false);
			this.lblMessage.setText("Informations sur le nouveau compte");
			this.lblSolde.setText("Solde (premier dépôt)");
			this.btnOk.setText("Ajouter");
			this.btnCancel.setText("Annuler");
			break;
		case MODIFICATION:
			this.txtDecAutorise.setDisable(false);
			this.txtSolde.setDisable(true);
			this.lblMessage.setText("Modification du compte");
			this.lblSolde.setText("Solde");
			this.btnOk.setText("Modifier");
			this.btnCancel.setText("Annuler");
			break;
		case SUPPRESSION:
			this.txtDecAutorise.setDisable(true);
			this.txtSolde.setDisable(true);
			this.lblMessage.setText("Supression du compte");
			this.lblSolde.setText("Solde ");
			this.btnOk.setText("Supprimer");
			this.btnCancel.setText("Annuler");
			break;
		}

		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dailyBankState.getEmployeActuel())) {
			// rien pour l'instant
		}

		// initialisation du contenu des champs
		this.txtIdclient.setText("" + this.compteEdite.idNumCli);
		this.txtIdNumCompte.setText("" + this.compteEdite.idNumCompte);
		this.txtIdAgence.setText("" + this.dailyBankState.getEmployeActuel().idAg);
		this.txtDecAutorise.setText("" + this.compteEdite.debitAutorise);
		this.txtSolde.setText(""+this.compteEdite.solde);

		this.compteResultat = null;

		this.primaryStage.showAndWait();
		return this.compteResultat;
	}

	// Gestion du stage

	/**
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
	 * Gère l'événement de focus sur le champ de découvert autorisé en mettant à jour la valeur du découvert autorisé du compte courant.
	 *
	 * @param txtField Le champ de texte du découvert autorisé
	 * @param oldPropertyValue La valeur précédente de la propriété "focused"
	 * @param newPropertyValue La nouvelle valeur de la propriété "focused"
	 * @return null
	 */
	private Object focusDecouvert(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				int val;
				val = Integer.parseInt(this.txtDecAutorise.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.compteEdite.debitAutorise = val;
			} catch (NumberFormatException nfe) {
				this.txtDecAutorise.setText("" + this.compteEdite.debitAutorise);
			}
		}
		return null;
	}

	/**
	 * Gère l'événement de focus sur le champ de solde en mettant à jour la valeur du solde du compte courant.
	 *
	 * @param txtField Le champ de texte du solde
	 * @param oldPropertyValue La valeur précédente de la propriété "focused"
	 * @param newPropertyValue La nouvelle valeur de la propriété "focused"
	 * @return null
	 */
	private Object focusSolde(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				double val;
				val = Double.parseDouble(this.txtSolde.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.compteEdite.solde = val;
			} catch (NumberFormatException nfe) {
				this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde));
			}
		}
		this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde));
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private Label lblSolde;
	@FXML
	private TextField txtIdclient;
	@FXML
	private TextField txtIdAgence;
	@FXML
	private TextField txtIdNumCompte;
	@FXML
	private TextField txtDecAutorise;
	@FXML
	private TextField txtSolde;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;



	/**
	 * Gère l'action de l'annulation de l'opération en cours.
	 * Réinitialise le compte résultat et ferme la fenêtre principale.
	 */
	@FXML
	private void doCancel() {
		this.compteResultat = null;
		this.primaryStage.close();
	}



	/**
	 * Gère l'action du bouton "Ajouter" en fonction du mode d'édition.
	 * Vérifie la validité de la saisie, attribue le compte édité au compte résultat et ferme la fenêtre principale.
	 */
	@FXML
	private void doAjouter() {
		switch (this.editionMode) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.compteResultat = this.compteEdite;
				this.primaryStage.close();
			}
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.compteResultat = this.compteEdite;
				this.primaryStage.close();
			}
			break;
		case SUPPRESSION:
			this.compteResultat = this.compteEdite;
			this.primaryStage.close();
			break;
		}

	}

	/**
	 * Vérifie si la saisie des champs est valide.
	 *
	 * @return true si la saisie est valide, false sinon
	 */
	private boolean isSaisieValide() {
		return true;
	}
}
