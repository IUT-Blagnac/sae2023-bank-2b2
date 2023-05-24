package application.tools.pdf;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.itextpdf.commons.bouncycastle.pkcs.IPKCS8EncryptedPrivateKeyInfo;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import application.DailyBankState;
import application.control.ComptesManagement;
import application.view.ComptesManagementController;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Operation;

public class GenPDF {
    private Client clientDesComptes;
    private CompteCourant compteActuel;
    private ComptesManagement cmDialogController;
    private Stage primaryStage;
    private DailyBankState dailyBankState;

    public void initContext(Stage _containingStage, ComptesManagement _cm, DailyBankState _dbstate, Client client) {
		this.cmDialogController = _cm;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.clientDesComptes = client;
	}

    public void GenPDF() {}

    public boolean genererPDF(Pair<String, String> monthYear, Client clientDesComptes, CompteCourant compteCourant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DecimalFormat df = new DecimalFormat("0.000");
        this.clientDesComptes = clientDesComptes;
        this.compteActuel = compteCourant;

		ArrayList<Operation> listeOpes = this.cmDialogController.getOperationsDunCompte(compteActuel, monthYear.getKey(), monthYear.getValue());
        
		if (listeOpes.isEmpty()) {
            return false;
        }




		FileChooser fileChooser = new FileChooser();

		// Définissez l'extension de filtrage
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
		//Afficher tout les fichiers
		FileChooser.ExtensionFilter exttFilter = new FileChooser.ExtensionFilter("Tout les fichiers", "*.*");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.getExtensionFilters().add(exttFilter);

		//définisser le nom par défaut du fichier
		fileChooser.setInitialFileName("Relevé de compte " + this.compteActuel.idNumCompte + " .pdf");

		// Affichez la boîte de dialogue FileChooser
		File file = fileChooser.showSaveDialog(new Stage());

		if(file != null) {
			try {
				// Initialize PDF writer
				PdfWriter writer = new PdfWriter(file.getPath());
				// Initialize PDF document
				PdfDocument pdf = new PdfDocument(writer);

				// Initialize document
				Document document = new Document(pdf);

				PdfFont font = null;
				try {
					URI uri = ComptesManagementController.class.getResource("font/Helvetica.ttf").toURI();
					File fontFile = new File(uri);
					font = PdfFontFactory.createFont(fontFile.getPath());
				} catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }

				PdfFont boldFont = null;
				try {
					URI boldUri = ComptesManagementController.class.getResource("font/Helvetica-Bold.ttf").toURI();
					File boldFontFile = new File(boldUri);
					boldFont = PdfFontFactory.createFont(boldFontFile.getPath());
				} catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }

				PdfFont lightFont = null;
				try {
					URI lightUri = ComptesManagementController.class.getResource("font/Helvetica-Light.ttf").toURI();
					File lightFontFile = new File(lightUri);
					lightFont = PdfFontFactory.createFont(lightFontFile.getPath());
				} catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }

				document.setFont(font);
				//ajouter les metadata
				pdf.getDocumentInfo().setTitle("Relevé mensuel");
				pdf.getDocumentInfo().setAuthor("DailyBank");
				pdf.getDocumentInfo().setCreator("DailyBank");
				pdf.getDocumentInfo().setSubject("Relevé mensuel");
				pdf.getDocumentInfo().setKeywords("DailyBank, Relevé mensuel");

				// Ajouter un titre centré en haut du pdf avec une police de taille 18
				Paragraph title = new Paragraph("Relevé de compte").setFontSize(18).setFont(boldFont);
				title.setTextAlignment(TextAlignment.CENTER);
				document.add(title);
				//Ajouter la date à droite du titre
				Paragraph date = new Paragraph("Date de génération du relevé : " + java.time.LocalDate.now().format(formatter)).setFontSize(12);
				date.setTextAlignment(TextAlignment.RIGHT);
				document.add(date);

				Paragraph infosClient = new Paragraph(
						"Client : " + this.clientDesComptes.nom + " " + this.clientDesComptes.prenom + " (ID : " + this.clientDesComptes.idNumCli + ") \n" +
								this.clientDesComptes.email + "\n" +
								this.clientDesComptes.adressePostale + "\n" +
								"De l'agence : " + this.dailyBankState.getAgenceActuelle().nomAg + " (ID : " + this.dailyBankState.getAgenceActuelle().idAg + ")"
						).setFontSize(12);
				infosClient.setTextAlignment(TextAlignment.LEFT);
				document.add(infosClient);

				Paragraph infosCompte = new Paragraph("Compte : " + this.compteActuel.idNumCompte).setFontSize(12);
				infosCompte.setTextAlignment(TextAlignment.LEFT);
				document.add(infosCompte);

				document.add(new Paragraph("Bonjour, vous retrouverez ci dessous votre relevé de compte :"));

				//make space between paragraphs
				document.add(new Paragraph("\n").setFontSize(30));

				//ajouter un tableau
				float[] pointColumnWidths = {150F, 150F, 150F, 150F, 250F};
				Table table = new Table(pointColumnWidths);				
				table.addHeaderCell(new Cell().add(new Paragraph("ID Compte").setFontSize(10).setFontColor(ColorConstants.GRAY).setFont(boldFont)));
				table.addHeaderCell(new Cell().add(new Paragraph("Date").setFontSize(10).setFontColor(ColorConstants.GRAY).setFont(boldFont)));
				table.addHeaderCell(new Cell().add(new Paragraph("ID Opération").setFontSize(10).setFontColor(ColorConstants.GRAY).setFont(boldFont)));
				table.addHeaderCell(new Cell().add(new Paragraph("Montant").setFontSize(10).setFontColor(ColorConstants.GRAY).setFont(boldFont)));
				table.addHeaderCell(new Cell().add(new Paragraph("Type").setFontSize(10).setFontColor(ColorConstants.GRAY).setFont(boldFont)));

				Table innerTable = new Table(2)
						.setWidth(UnitValue.createPercentValue(100))
						.setBorder(Border.NO_BORDER);

				Cell leftCell = new Cell().add(new Paragraph("Solde avant transactions : ")
						.setFontSize(11)
						.setFontColor(ColorConstants.BLACK)
						.setFont(lightFont)
						.setTextAlignment(TextAlignment.LEFT))
						.setBorder(Border.NO_BORDER)
						.setFont(boldFont);

				Double oldSolde = compteActuel.solde;
				System.out.println("oldSolde : " + oldSolde);
				
				for (Operation currOp : listeOpes) {
					System.out.println("id : " + currOp.idOperation + "| Montant :" + currOp.montant);
					oldSolde -= currOp.montant;
					System.out.println("oldSolde : " + oldSolde);
				}

				Cell rightCell = new Cell().add(new Paragraph(df.format(oldSolde))
						.setFontSize(11)
						.setFontColor(ColorConstants.BLACK)
						.setFont(lightFont)
						.setTextAlignment(TextAlignment.RIGHT))
						.setBorder(Border.NO_BORDER)
						.setFont(boldFont);

				innerTable.addCell(leftCell);
				innerTable.addCell(rightCell);

				Cell outerCell = new Cell(1,5);
				outerCell.add(innerTable)
				.setPadding(0)
				.setFont(boldFont);
				table.addCell(outerCell);

				for (Operation currOp : listeOpes) {
					table.addCell(new Cell().add(new Paragraph(currOp.idNumCompte+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph(currOp.dateOp.toLocalDate().format(formatter)+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph(currOp.idOperation+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph((currOp.montant+"").replace(".", ",")).setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph(currOp.idTypeOp+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
				}

				innerTable = new Table(2)
						.setWidth(UnitValue.createPercentValue(100))
						.setBorder(Border.NO_BORDER);

				leftCell = new Cell().add(new Paragraph("Solde aprés transactions : ")
						.setFontSize(11)
						.setFontColor(ColorConstants.BLACK)
						.setFont(lightFont)
						.setTextAlignment(TextAlignment.LEFT))
						.setBorder(Border.NO_BORDER)
						.setFont(boldFont);

				rightCell = new Cell().add(new Paragraph(df.format(this.compteActuel.solde))
						.setFontSize(11)
						.setFontColor(ColorConstants.BLACK)
						.setFont(lightFont)
						.setTextAlignment(TextAlignment.RIGHT))
						.setBorder(Border.NO_BORDER)
						.setFont(boldFont);

				innerTable.addCell(leftCell);
				innerTable.addCell(rightCell);

				outerCell = new Cell(1,5);
				outerCell.add(innerTable)
				.setPadding(0)
				.setFont(boldFont);
				table.addCell(outerCell.setFont(boldFont));

				document.add(table);

				pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler(document));

				// Close document
				document.close();
				pdf.close();
				writer.close();
                return true;
			} catch(Exception e) {
				// Handle exception here
				e.printStackTrace();
                return false;
			}
		}
        return false;
    }
}
