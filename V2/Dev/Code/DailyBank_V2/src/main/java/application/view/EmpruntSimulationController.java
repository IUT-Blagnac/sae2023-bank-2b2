package application.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

import javax.swing.text.StyleConstants.FontConstants;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import application.DailyBankState;
import application.control.EmpruntSimulation;
import application.control.PrelevManagement;

public class EmpruntSimulationController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à ComptesManagementController
	private EmpruntSimulation esDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;

	private Client client;
	
	private double montantEmprunt;
	private double tauxEmprunt;
	private int duree;
	private double tauxAssurance;

	@FXML
	private TextField txtMontant;
	@FXML
	private TextField txtDuree;
	@FXML
	private TextField txtTaux;
	@FXML
	private Label lblMontant;
	@FXML
	private Label lblDuree;
	@FXML
	private Label lblTaux;
	@FXML
	private Label lblInfo;
	@FXML
	private Button btnSimuler;
	@FXML
	private Button btnAnnuler;
	@FXML
	private RadioButton rbOui;
	@FXML
	private RadioButton rbNon;
	@FXML
	private Label lblTauxAssurance;
	@FXML
	private TextField txtTauxAssurance;
	private ToggleGroup toggleGroup;

	// Manipulation de la fenêtre

	/**
	 * Initialise le contexte du contrôleur avec les paramètres fournis.
	 * 
	 * @param _containingStage La fenêtre physique contenant la scène
	 * @param _cm              Le contrôleur de dialogue associé
	 * @param _dbstate         L'état courant de l'application
	 * @param client           Le client associé aux comptes
	 */
	public void initContext(Stage _containingStage, DailyBankState _dbstate, Client client) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.client = client;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}

	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	public void initialize() {
		// Créer un groupe pour les boutons radios
		toggleGroup = new ToggleGroup();
		rbOui.setToggleGroup(toggleGroup);
		rbNon.setToggleGroup(toggleGroup);

		// Sélectionner le bouton radio "Non" par défaut
		rbNon.setSelected(true);
		lblTauxAssurance.setVisible(false);
		txtTauxAssurance.setVisible(false);
		txtTauxAssurance.setDisable(true);

		// Ajouter un écouteur de changement de sélection
		toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == rbNon) {
				// Masquer le label et le champ de texte
				lblTauxAssurance.setVisible(false);
				txtTauxAssurance.setVisible(false);
				txtTauxAssurance.setDisable(true);
			} else {
				// Afficher le label et le champ de texte
				lblTauxAssurance.setVisible(true);
				txtTauxAssurance.setVisible(true);
				txtTauxAssurance.setDisable(false);
			}
		});
	}

	// Gestion du stage

	/**
	 * Gère l'événement de fermeture de la fenêtre.
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
	 * Annule et ferme la fenêtre de la simulation de l'emprunt.
	 */
	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void doSimulerEmprunt() {
		
		this.txtMontant.getStyleClass().remove("borderred");
		this.lblMontant.getStyleClass().remove("borderred");
		this.txtDuree.getStyleClass().remove("borderred");
		this.lblDuree.getStyleClass().remove("borderred");
		this.txtTaux.getStyleClass().remove("borderred");
		this.lblTaux.getStyleClass().remove("borderred");
		this.txtTauxAssurance.getStyleClass().remove("borderred");
		this.lblTauxAssurance.getStyleClass().remove("borderred");

		boolean assuranceActive = rbOui.isSelected(); // Vérifie si l'assurance est active

		if (assuranceActive) {
			try {
				this.tauxAssurance = Double.parseDouble(txtTauxAssurance.getText());
				if (this.tauxAssurance < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException nfe) {
				this.txtTauxAssurance.getStyleClass().add("borderred");
				this.lblTauxAssurance.getStyleClass().add("borderred");
				this.txtTauxAssurance.requestFocus();
				return;
			}
		}

		try {
			this.montantEmprunt = Double.parseDouble(txtMontant.getText());
			if (montantEmprunt <= 0)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtMontant.getStyleClass().add("borderred");
			this.lblMontant.getStyleClass().add("borderred");
			this.txtMontant.requestFocus();
			return;
		}
		try {
			this.duree = Integer.parseInt(txtDuree.getText());
			if (duree <= 0)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtDuree.getStyleClass().add("borderred");
			this.lblDuree.getStyleClass().add("borderred");
			this.txtDuree.requestFocus();
			return;
		}
		try {
			this.tauxEmprunt = Double.parseDouble(txtTaux.getText());
			if (tauxEmprunt <= 0)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtTaux.getStyleClass().add("borderred");
			this.lblTaux.getStyleClass().add("borderred");
			this.txtTaux.requestFocus();
			return;
		}

		double mensualite = calculerMensualite(montantEmprunt, duree, tauxEmprunt);
		double capitalRestantDebutPeriode = montantEmprunt;
		double capitalRestantFinPeriode = capitalRestantDebutPeriode;
		double totalPaiement = mensualite * duree * 12 - montantEmprunt;
		double totalAssurance = 0;
		double echeanceMensualite = mensualite;

		DecimalFormat decimalFormat = new DecimalFormat("0.00");

		StringBuilder tableauRemboursementBuilder = new StringBuilder();
		StringBuilder tableauRemboursementAvecAssuranceBuilder = new StringBuilder();

		for (int periode = 1; periode <= duree * 12; periode++) {
			double interets = capitalRestantDebutPeriode * (tauxEmprunt / 100 / 12);
			double mensualiteAssurance = 0; // Initialise à 0

			if (assuranceActive) {
				mensualiteAssurance = montantEmprunt * tauxAssurance / 100 / 12;
				totalAssurance = mensualiteAssurance * duree * 12;
				echeanceMensualite = mensualiteAssurance + mensualite;
			}
			double principal = mensualite - interets;
			capitalRestantFinPeriode = capitalRestantDebutPeriode - principal;

			tableauRemboursementBuilder.append(periode).append("\t")
					.append(decimalFormat.format(capitalRestantDebutPeriode)).append("\t")
					.append(decimalFormat.format(interets)).append("\t").append(decimalFormat.format(principal))
					.append("\t").append(decimalFormat.format(mensualite)).append("\t")
					.append(decimalFormat.format(capitalRestantFinPeriode)).append("\n");

			if (assuranceActive) {
				tableauRemboursementAvecAssuranceBuilder.append(periode).append("\t")
						.append(decimalFormat.format(capitalRestantDebutPeriode)).append("\t")
						.append(decimalFormat.format(interets)).append("\t").append(decimalFormat.format(principal))
						.append("\t").append(decimalFormat.format(mensualite)).append("\t")
						.append(decimalFormat.format(capitalRestantFinPeriode)).append("\t")
						.append(decimalFormat.format(mensualiteAssurance)).append("\n");
			}

			capitalRestantDebutPeriode = capitalRestantFinPeriode;
		}

		String tableauRemboursement = tableauRemboursementBuilder.toString();
		String tableauRemboursementAvecAssurance = tableauRemboursementAvecAssuranceBuilder.toString();

		if (!assuranceActive) {
			generatePDF(tableauRemboursement, "Tableau_amortissement_"+this.client.nom+this.client.prenom+".pdf", assuranceActive, totalPaiement,
					totalAssurance, duree, echeanceMensualite);
		} else {
			generatePDF(tableauRemboursementAvecAssurance, "Tableau_amortissement_avec_assurance_"+this.client.nom+this.client.prenom+".pdf", assuranceActive,
					totalPaiement, totalAssurance, duree, echeanceMensualite);
		}

	}

	/**
	 * Génère un fichier PDF contenant le tableau d'amortissement du prêt.
	 *
	 * @param tableauRemboursement Le tableau d'amortissement au format String
	 */
	private void generatePDF(String tableauRemboursement, String fileName, Boolean assuranceActive,
			Double totalPaiement, Double totalAssurance, int duree, double echanceMensualite) {
		try {
			// Créer un objet FileChooser pour sélectionner le fichier de sauvegarde
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
			fileChooser.getExtensionFilters().add(extFilter);
			fileChooser.setInitialFileName(fileName);

			// Afficher la boîte de dialogue FileChooser
			File file = fileChooser.showSaveDialog(primaryStage);
			if (file != null) {
				// Créer un nouveau document PDF
				PdfWriter writer = new PdfWriter(file.getPath());
				PdfDocument pdf = new PdfDocument(writer);
				Document document = new Document(pdf, PageSize.A4);

				PdfFont font = null;
				PdfFont contentFont = null;
				try {
					URI uri = ComptesManagementController.class.getResource("font/Helvetica-Bold.ttf").toURI();
					File fontFile = new File(uri);
					font = PdfFontFactory.createFont(fontFile.getPath());
					URI uri2 = ComptesManagementController.class.getResource("font/Helvetica-Light.ttf").toURI();
					File fontFile2 = new File(uri2);
					contentFont = PdfFontFactory.createFont(fontFile2.getPath());
				} catch (URISyntaxException e) {
				}

				// Ajouter le titre
				Paragraph title = new Paragraph("Tableau d'amortissement de prêt").setFont(font).setFontSize(18);
				title.setTextAlignment(TextAlignment.CENTER);
				document.add(title);

				// Ajouter la date à droite du titre
				Paragraph date = new Paragraph("Date de la simulation de l'emprunt : " + java.time.LocalDate.now())
						.setFontSize(12);
				date.setTextAlignment(TextAlignment.RIGHT);
				document.add(date);

				Paragraph infosClient = new Paragraph(
						"Client : " + this.client.nom + " " + this.client.prenom + " (ID : " + this.client.idNumCli
								+ ") \n" + this.client.email + "\n" + this.client.adressePostale + "\n"
								+ "De l'agence : " + this.dailyBankState.getAgenceActuelle().nomAg + " (ID : "
								+ this.dailyBankState.getAgenceActuelle().idAg + ")")
						.setFontSize(12);
				infosClient.setTextAlignment(TextAlignment.LEFT);
				document.add(infosClient);
				
				String tauxAssu ="";
				if(assuranceActive) {
					 tauxAssu = (" \n Taux de l'assurance : "+this.tauxAssurance+"%");
				}
				
				Paragraph infosEmprunt = new Paragraph(
						"Montant emprunté : " + this.montantEmprunt +  " euros \n Durée de l'emprunt : " + this.duree +" ans \n" + "Taux de l'emprunt : "+this.tauxEmprunt +"%" +tauxAssu)
						.setFontSize(12);
				infosClient.setTextAlignment(TextAlignment.LEFT);
				document.add(infosEmprunt);

				// Ajouter le tableau d'amortissement
				String[] rows = tableauRemboursement.split("\n");
				String[] headers = rows[0].split("\t");

				Table table = new Table(headers.length);
				table.setWidth(UnitValue.createPercentValue(100));

				// Ajouter les en-têtes de colonne
				table.addCell("Numéro période").setFont(font).setFontSize(10);
				table.addCell("Capital Restant du en début de période").setFont(font).setFontSize(10);
				table.addCell("Montant des intérêts").setFont(font).setFontSize(10);
				table.addCell("Montant du principal").setFont(font).setFontSize(10);
				table.addCell("Montant à rembourser (Mensualité)").setFont(font).setFontSize(10);
				table.addCell("Capital Restant du en fin de période").setFont(font).setFontSize(10);

				if (assuranceActive) {
					table.addCell("Mensualité de l'assurance").setFont(font).setFontSize(10);
				}

				// Ajouter les données du tableau
				for (int i = 0; i < rows.length; i++) {
				    String[] cells = rows[i].split("\t");
				    for (String cell : cells) {
				        if (cell.equals("-0.00")) {
				            cell = "0.00";
				        }
				        table.addCell(cell).setFont(contentFont).setFontSize(10);
				    }
				}


				document.add(table);

				DecimalFormat decimalFormat = new DecimalFormat("0.##");


				// Ajouter le récapitulatif de l'emprunt
				Paragraph recapitulatif = new Paragraph("\nRécapitulatif :\n\n").setFont(font).setFontSize(12);
				document.add(recapitulatif);

				double coutTotalCredit = totalPaiement + totalAssurance;

				if(assuranceActive) {
				Paragraph coutTotal = new Paragraph(
						"Le coût total du crédit s'élève à " + decimalFormat.format(coutTotalCredit) + " euros \n dont "
								+ decimalFormat.format(totalAssurance) + " euros de frais d'assurance.")
						.setFont(contentFont).setFontSize(12);
				document.add(coutTotal);

				Paragraph mensualite = new Paragraph(
						"Les échéances mensuelles s'élèvent à " + decimalFormat.format(echanceMensualite) + " euros \n dont "
								+ decimalFormat.format(totalAssurance / (12 * duree)) +" euros de frais d'assurance.")
						.setFont(contentFont).setFontSize(12);
				document.add(mensualite);

				// Ajouter les informations client
				Paragraph emptyLine = new Paragraph("\n");
				document.add(emptyLine);
				}else {
					Paragraph coutTotal = new Paragraph(
							"Le coût total du crédit s'élève à " + decimalFormat.format(coutTotalCredit)+" euros.")
							.setFont(contentFont).setFontSize(12);
					document.add(coutTotal);

					Paragraph mensualite = new Paragraph(
							"Les échéances mensuelles s'élèvent à " + decimalFormat.format(echanceMensualite)+" euros.")
							.setFont(contentFont).setFontSize(12);
					document.add(mensualite);
				}

				// Fermer le document
				document.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double calculerMensualite(double montant, int duree, double taux) {
		double tauxMensuel = taux / 100 / 12;
		int nombreMois = duree * 12;
		double mensualite = (montant * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -nombreMois));
		return mensualite;
	}

	private double calculerTotalInterets(double mensualite, int duree, double montant) {
		double totalInterets = (mensualite * duree * 12) - montant;
		return totalInterets;
	}
}
