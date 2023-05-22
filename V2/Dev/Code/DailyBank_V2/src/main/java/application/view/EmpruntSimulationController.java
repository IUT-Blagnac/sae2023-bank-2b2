package application.view;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

	// Données de la fenêtre
	private CompteCourant prelevDuCompte;

	@FXML
	private TextField txtMontant;
	@FXML
	private TextField txtDuree;
	@FXML
	private TextField txtTaux;
	@FXML
	private Button btnSimuler;
	@FXML
	private Button btnAnnuler;
	@FXML
	private Label lblResultat;

	// Manipulation de la fenêtre

	/**
	 * Initialise le contexte du contrôleur avec les paramètres fournis.
	 * 
	 * @param _containingStage La fenêtre physique contenant la scène
	 * @param _cm              Le contrôleur de dialogue associé
	 * @param _dbstate         L'état courant de l'application
	 * @param client           Le client associé aux comptes
	 */
	public void initContext(Stage _containingStage,DailyBankState _dbstate) {
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));
	}
	
	public void displayDialog() {
		this.primaryStage.showAndWait();
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
		double montant = Double.parseDouble(txtMontant.getText());
	    int duree = Integer.parseInt(txtDuree.getText());
	    double taux = Double.parseDouble(txtTaux.getText());

	    double mensualite = calculerMensualite(montant, duree, taux);
	    double capitalRestantDebutPeriode = montant;
	    double capitalRestantFinPeriode = capitalRestantDebutPeriode;

	    DecimalFormat decimalFormat = new DecimalFormat("0.00");

	    // Créer une StringBuilder pour construire le tableau de remboursement
	    StringBuilder tableauRemboursementBuilder = new StringBuilder();
	   
	    for (int periode = 1; periode <= duree * 12; periode++) {
	        double interets = capitalRestantDebutPeriode * (taux / 100 / 12);
	        double principal = mensualite - interets;
	        capitalRestantFinPeriode = capitalRestantDebutPeriode - principal;

	        tableauRemboursementBuilder.append(periode).append("\t")
	                .append(decimalFormat.format(capitalRestantDebutPeriode)).append("\t")
	                .append(decimalFormat.format(interets)).append("\t")
	                .append(decimalFormat.format(principal)).append("\t")
	                .append(decimalFormat.format(mensualite)).append("\t")
	                .append(decimalFormat.format(capitalRestantFinPeriode)).append("\n");

	        capitalRestantDebutPeriode = capitalRestantFinPeriode;
	    }

	    String tableauRemboursement = tableauRemboursementBuilder.toString();

	    generatePDF(tableauRemboursement);
	}
	
	/**
     * Génère un fichier PDF contenant le tableau d'amortissement du prêt.
     *
     * @param tableauRemboursement Le tableau d'amortissement au format String
     */
    private void generatePDF(String tableauRemboursement) {
        try {
            // Créer un objet FileChooser pour sélectionner le fichier de sauvegarde
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName("Tableau_amortissement.pdf");

            // Afficher la boîte de dialogue FileChooser
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                // Créer un nouveau document PDF
                PdfWriter writer = new PdfWriter(file.getPath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4);

                PdfFont font = null;
				try {
					URI uri = ComptesManagementController.class.getResource("font/Helvetica.ttf").toURI();
					File fontFile = new File(uri);
					font = PdfFontFactory.createFont(fontFile.getPath());
				} catch (URISyntaxException e) {}

                // Ajouter le titre
                Paragraph title = new Paragraph("Tableau d'amortissement de prêt").setFont(font).setFontSize(18).setBold();
                title.setTextAlignment(TextAlignment.CENTER);
                document.add(title);

                // Ajouter le tableau d'amortissement
                Table table = new Table(6);
                table.setWidth(UnitValue.createPercentValue(100));
                table.addCell("Numéro période").setFont(font).setFontSize(10).setBold();
                table.addCell("Capital Restant du en début de période").setFont(font).setFontSize(10).setBold();
                table.addCell("Montant des intérêts").setFont(font).setFontSize(10).setBold();
                table.addCell("Montant du principal").setFont(font).setFontSize(10).setBold();
                table.addCell("Montant à rembourser (Mensualité)").setFont(font).setFontSize(10).setBold();
                table.addCell("Capital Restant du en fin de période").setFont(font).setFontSize(10).setBold();

                String[] rows = tableauRemboursement.split("\n");
                for (String row : rows) {
                    String[] cells = row.split("\t");
                    for (String cell : cells) {
                        table.addCell(cell).setFont(font).setFontSize(10);
                    }
                }

                document.add(table);

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
