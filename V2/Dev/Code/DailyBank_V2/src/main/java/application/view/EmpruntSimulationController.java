package application.view;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import application.DailyBankState;
import application.control.EmpruntSimulation;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;

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
	private RadioButton rbMois;
	@FXML
	private RadioButton rbAnnee;
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
		this.toggleGroup = new ToggleGroup();
		this.rbOui.setToggleGroup(this.toggleGroup);
		this.rbNon.setToggleGroup(this.toggleGroup);

		// Sélectionner le bouton radio "Non" par défaut
		this.rbNon.setSelected(true);
		this.lblTauxAssurance.setVisible(false);
		this.txtTauxAssurance.setVisible(false);
		this.txtTauxAssurance.setDisable(true);

		// Ajouter un écouteur de changement de sélection
		this.toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == this.rbNon) {
				// Masquer le label et le champ de texte
				this.lblTauxAssurance.setVisible(false);
				this.txtTauxAssurance.setVisible(false);
				this.txtTauxAssurance.setDisable(true);
			} else {
				// Afficher le label et le champ de texte
				this.lblTauxAssurance.setVisible(true);
				this.txtTauxAssurance.setVisible(true);
				this.txtTauxAssurance.setDisable(false);
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

		boolean assuranceActive = this.rbOui.isSelected(); // Vérifie si l'assurance est active
		boolean remboursementAnnee = this.rbAnnee.isSelected();

		if (assuranceActive) {
			try {
				this.tauxAssurance = Double.parseDouble(this.txtTauxAssurance.getText());
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
			this.montantEmprunt = Double.parseDouble(this.txtMontant.getText());
			if (this.montantEmprunt <= 0)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtMontant.getStyleClass().add("borderred");
			this.lblMontant.getStyleClass().add("borderred");
			this.txtMontant.requestFocus();
			return;
		}
		try {
			this.duree = Integer.parseInt(this.txtDuree.getText());
			if (this.duree <= 0 || this.duree > 75)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtDuree.getStyleClass().add("borderred");
			this.lblDuree.getStyleClass().add("borderred");
			this.txtDuree.requestFocus();
			return;
		}
		try {
			this.tauxEmprunt = Double.parseDouble(this.txtTaux.getText());
			if (this.tauxEmprunt <= 0 || this.tauxEmprunt >= 100)
				throw new NumberFormatException();
		} catch (NumberFormatException nfe) {
			this.txtTaux.getStyleClass().add("borderred");
			this.lblTaux.getStyleClass().add("borderred");
			this.txtTaux.requestFocus();
			return;
		}

		double rembourser;
		double totalPaiement = 0;

		if (remboursementAnnee) {
			rembourser = this.calculerAnnuité();
			 totalPaiement = rembourser * this.duree - this.montantEmprunt;
		} else {
			rembourser = this.calculerMensualite();
			 totalPaiement = rembourser * this.duree * 12 - this.montantEmprunt;
		}

		double capitalRestantDebutPeriode = this.montantEmprunt;
		double capitalRestantFinPeriode = capitalRestantDebutPeriode;

		double totalAssurance = 0;
		double echeance = rembourser;

		DecimalFormat decimalFormat = new DecimalFormat("0.00");

		StringBuilder tableauRemboursementBuilder = new StringBuilder();
		StringBuilder tableauRemboursementAvecAssuranceBuilder = new StringBuilder();
		StringBuilder tableauAssuranceBuilder = new StringBuilder();


		
		int nbMoisAnnee; //permet de pouvoir automatiquement faire l'entrée des valeurs dans le tableau
		
		if(remboursementAnnee) {
			nbMoisAnnee = 1;
		}else {
			nbMoisAnnee = 12;
		}
		
		double echeanceAssurance = 0; // Initialise à 0
				
		for (int periode = 1; periode <= this.duree * nbMoisAnnee; periode++) {
			double interets = capitalRestantDebutPeriode * (this.tauxEmprunt / 100 / nbMoisAnnee);
	

			if (assuranceActive) {
				if (this.rbAnnee.isSelected()) {
					echeanceAssurance = this.montantEmprunt * this.tauxAssurance / 100;
					totalAssurance = echeanceAssurance * this.duree;
				} else {
					echeanceAssurance = this.montantEmprunt * this.tauxAssurance / 100 / nbMoisAnnee;
					totalAssurance = echeanceAssurance * this.duree * nbMoisAnnee;

				}

				echeance = echeanceAssurance + rembourser;
			}
			double principal = rembourser - interets;
			capitalRestantFinPeriode = capitalRestantDebutPeriode - principal;

			tableauRemboursementBuilder.append(periode).append("\t")
			.append(decimalFormat.format(capitalRestantDebutPeriode)).append("\t")
			.append(decimalFormat.format(interets)).append("\t").append(decimalFormat.format(principal))
			.append("\t").append(decimalFormat.format(rembourser)).append("\t")
			.append(decimalFormat.format(capitalRestantFinPeriode)).append("\n");

			if (assuranceActive) {
				tableauRemboursementAvecAssuranceBuilder.append(periode).append("\t")
				.append(decimalFormat.format(capitalRestantDebutPeriode)).append("\t")
				.append(decimalFormat.format(interets)).append("\t").append(decimalFormat.format(principal))
				.append("\t").append(decimalFormat.format(rembourser)).append("\t")
				.append(decimalFormat.format(capitalRestantFinPeriode)).append("\t")
				.append(decimalFormat.format(echeanceAssurance)).append("\t")
				.append(decimalFormat.format(echeanceAssurance + rembourser)).append("\n");
				
				
			}

			capitalRestantDebutPeriode = capitalRestantFinPeriode;
		}

		if(assuranceActive) {
		tableauAssuranceBuilder.append(echeanceAssurance).append("\t")
		.append(decimalFormat.format(rembourser)).append("\t")
		.append(decimalFormat.format(echeanceAssurance + rembourser)).append("\n");
		}
		
		
		String tableauRemboursement = tableauRemboursementBuilder.toString();
		String tableauRemboursementAvecAssurance = tableauRemboursementAvecAssuranceBuilder.toString();
		String tableauAssurance = tableauAssuranceBuilder.toString();

		if (!assuranceActive) {
			this.generatePDF(tableauRemboursement,
					"Tableau_amortissement_" + this.client.nom + "_" + this.client.prenom + ".pdf", assuranceActive, remboursementAnnee,
					totalPaiement, totalAssurance, echeance, "", "");
		} else {
			this.generatePDF(tableauRemboursement,
					"Tableau_amortissement_avec_assurance_" + this.client.nom + "_" + this.client.prenom + ".pdf",
					assuranceActive, remboursementAnnee, totalPaiement, totalAssurance, echeance, tableauRemboursementAvecAssurance, tableauAssurance);
		}

	}

	/**
	 * Génère un fichier PDF contenant le tableau d'amortissement du prêt.
	 *
	 * @param tableauRemboursement Le tableau d'amortissement au format String
	 * @param fileName             Le nom du fichier PDF à générer
	 * @param assuranceActive      Indique si l'assurance est active ou non
	 * @param totalPaiement        Le montant total des paiements
	 * @param totalAssurance       Le montant total de l'assurance
	 * @param echanceMensualite    L'échéance de la mensualité
	 */
	private void generatePDF(String tableauRemboursement, String fileName, Boolean assuranceActive, Boolean remboursementAnnee,
			Double totalPaiement, Double totalAssurance, double echance, String tableauRemboursementAssurance, String tableauAssurance) {
		try {
			// Créer un objet FileChooser pour sélectionner le fichier de sauvegarde
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
			fileChooser.getExtensionFilters().add(extFilter);
			fileChooser.setInitialFileName(fileName);

			// Afficher la boîte de dialogue FileChooser
			File file = fileChooser.showSaveDialog(this.primaryStage);
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
				Paragraph title = new Paragraph("Tableau d'amortissement d'emprunt").setFont(font).setFontSize(18);
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

				String tauxAssu = "";
				if (assuranceActive) {
					tauxAssu = (" \n Taux de l'assurance : " + this.tauxAssurance + "%");
				}
				String typeRemboursement ="";
				
				if(remboursementAnnee) {
					typeRemboursement = "Année";
				}else {
					typeRemboursement = "Mois";
				}

				Paragraph infosEmprunt = new Paragraph(
						"Montant emprunté : " + this.montantEmprunt + " euros \n Durée de l'emprunt : " + this.duree
						+ " ans \n" + "Taux de l'emprunt : " + this.tauxEmprunt + "%" + tauxAssu + "\n Type de remboursement : " +typeRemboursement)
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
				table.addCell("Montant à rembourser").setFont(font).setFontSize(10);
				table.addCell("Capital Restant du en fin de période").setFont(font).setFontSize(10);


				for (String row : rows) {
					String[] cells = row.split("\t");
					for (String cell : cells) {
						if (cell.equals("-0.00")) {
							cell = "0.00";
						} else if (cell.startsWith("-")) {
							cell = cell.substring(1); // Supprimer le signe négatif
						}
						table.addCell(cell).setFont(contentFont).setFontSize(10);
					}
				}

				document.add(table);
				document.add(new AreaBreak());
				
				if(assuranceActive) {
					// Ajouter le titre
					Paragraph title2 = new Paragraph("Tableau des frais d'assurances").setFont(font).setFontSize(18);
					title.setTextAlignment(TextAlignment.CENTER);
					document.add(title2);
					
					// Ajouter le tableau d'amortissement
					String[] rowsAssu = tableauAssurance.split("\n");
					String[] headersAssu = rowsAssu[0].split("\t");

					Table tableAssu = new Table(headersAssu.length);
					tableAssu.setWidth(UnitValue.createPercentValue(100));

					// Ajouter les en-têtes de colonne
					tableAssu.addCell("Echeance de l'assurance").setFont(font).setFontSize(10);
					tableAssu.addCell("Montant à rembourser hors assurance").setFont(font).setFontSize(10);
					tableAssu.addCell("Montant à rembourser hors assurance").setFont(font).setFontSize(10);
				

					for (String row : rowsAssu) {
						String[] cells = row.split("\t");
						for (String cell : cells) {
							if (cell.equals("-0.00")) {
								cell = "0.00";
							} else if (cell.startsWith("-")) {
								cell = cell.substring(1); // Supprimer le signe négatif
							}
							tableAssu.addCell(cell).setFont(contentFont).setFontSize(10);
						}
					}
					
					document.add(tableAssu);
					document.add(new AreaBreak());
					
					
					// Ajouter le titre
					Paragraph title3 = new Paragraph("Tableau d'amortissement d'emprunt avec assurance").setFont(font).setFontSize(18);
					title.setTextAlignment(TextAlignment.CENTER);
					document.add(title3);
					
					// Ajouter le tableau d'amortissement
					String[] rowsEmpruntAssu = tableauRemboursementAssurance.split("\n");
					String[] headersEmpruntAssu = rowsEmpruntAssu[0].split("\t");

					Table tableEmpruntAssu = new Table(headersEmpruntAssu.length);
					tableEmpruntAssu.setWidth(UnitValue.createPercentValue(100));

					tableEmpruntAssu.addCell("Numéro période").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Capital Restant du en début de période").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Montant des intérêts").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Montant du principal").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Montant à rembourser").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Capital Restant du en fin de période").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Montant de l'assurance").setFont(font).setFontSize(10);
					tableEmpruntAssu.addCell("Montant à rembourser avec l'assurance").setFont(font).setFontSize(10);
				

					for (String row : rowsEmpruntAssu) {
						String[] cells = row.split("\t");
						for (String cell : cells) {
							if (cell.equals("-0.00")) {
								cell = "0.00";
							} else if (cell.startsWith("-")) {
								cell = cell.substring(1); // Supprimer le signe négatif
							}
							tableEmpruntAssu.addCell(cell).setFont(contentFont).setFontSize(10);
						}
					}
					
					document.add(tableEmpruntAssu);
					
				}

				DecimalFormat decimalFormat = new DecimalFormat("0.##");

				Paragraph recapitulatif = new Paragraph("\nRécapitulatif :\n").setFont(font).setFontSize(12);
				document.add(recapitulatif);

				double coutTotalCredit = totalPaiement + totalAssurance;

				if (assuranceActive) {
				    Paragraph coutTotal = new Paragraph("Le coût total du crédit s'élève à "
				            + decimalFormat.format(coutTotalCredit) + " euros, dont "
				            + decimalFormat.format(totalAssurance) + " euros de frais d'assurance.")
				            .setFont(contentFont).setFontSize(12);
				    document.add(coutTotal);

				    String typeRemboursementLabel = remboursementAnnee ? "annuelle" : "mensuelle";
				    String assuranceLabel = remboursementAnnee ? decimalFormat.format(totalAssurance / duree) : decimalFormat.format(totalAssurance / duree / 12);
				    Paragraph mensualite = new Paragraph("Les échéances " + typeRemboursementLabel + "s s'élèvent à "
				            + decimalFormat.format(echance) + " euros, dont "
				            + assuranceLabel + " euros de frais d'assurance par " + (remboursementAnnee ? "an" : "mois") + ".")
				            .setFont(contentFont).setFontSize(12);
				    document.add(mensualite);
				} else {
				    Paragraph coutTotal = new Paragraph(
				            "Le coût total du crédit s'élève à " + decimalFormat.format(coutTotalCredit) + " euros.")
				            .setFont(contentFont).setFontSize(12);
				    document.add(coutTotal);

				    String typeRemboursementLabel = remboursementAnnee ? "annuels" : "mensuels";
				    String mensualiteLabel = remboursementAnnee ? decimalFormat.format(echance) : decimalFormat.format(echance * 12);
				    Paragraph mensualite = new Paragraph("Les échéances " + typeRemboursementLabel + " s'élèvent à "
				            + mensualiteLabel + " euros.").setFont(contentFont).setFontSize(12);
				    document.add(mensualite);
				}






				// Fermer le document
				document.close();
				// Afficher une boîte de dialogue de confirmation
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Génération du fichier PDF");
				alert.setHeaderText(null);
				alert.setContentText("Le fichier PDF a été généré avec succès !");
				alert.showAndWait();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double calculerMensualite() {
		double tauxMensuel = tauxEmprunt / 100 / 12;
		int nombreMois = duree * 12;
		double mensualite = (montantEmprunt * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -nombreMois));
		return mensualite;
	}

	private double calculerAnnuité() {
		double taux = tauxEmprunt / 100;
		double mensualite = (montantEmprunt * taux) / (1 - Math.pow(1 + taux, -duree));
		return mensualite;
	}
}
