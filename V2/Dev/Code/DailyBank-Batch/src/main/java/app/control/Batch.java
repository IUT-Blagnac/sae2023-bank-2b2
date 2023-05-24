package app.control;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import java.io.InputStream;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import app.model.data.AgenceBancaire;
import app.model.data.Client;
import app.model.data.CompteCourant;
import app.model.data.Operation;
import app.model.data.Prelevement;
import app.model.orm.Access_BD_AgenceBancaire;
import app.model.orm.Access_BD_Client;
import app.model.orm.Access_BD_CompteCourant;
import app.model.orm.Access_BD_Operation;
import app.model.orm.Access_BD_Prelevement;
import app.model.orm.exception.DataAccessException;
import app.model.orm.exception.DatabaseConnexionException;
import app.model.orm.exception.ManagementRuleViolation;
import app.model.orm.exception.RowNotFoundOrTooManyRowsException;
import app.model.pdf.FooterEventHandler;

import com.itextpdf.layout.element.Table;

public class Batch {
    private Client clientDuCompteActu;
    private CompteCourant compteActu;
    private AgenceBancaire agenceActuelle;

    public void start() {
        System.out.println("COUCOU");
        try {
            Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
            compteActu = acc.getCompteCourant(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        doRel();
        doPrelev();
    }
    
    private void doPrelev() {
    	Access_BD_Operation aop = new Access_BD_Operation();
    	Access_BD_Prelevement apa = new Access_BD_Prelevement();
    	Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
    	Date date = Date.valueOf(LocalDate.now());
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	int jour = calendar.get(Calendar.DAY_OF_MONTH);
    	
    	try {
			for(Prelevement prelevements : apa.getAllPrelevements()) {
				if(prelevements.datePrelev == jour) {
					System.out.println("jour bon");
					try {
						aop.insertDebit(acc.getCompteCourant(prelevements.idNumCompte), jour, "Prélèvement automatique");
					} catch (ManagementRuleViolation e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RowNotFoundOrTooManyRowsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseConnexionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void doRel() {
		DecimalFormat df = new DecimalFormat("0.000");
		ArrayList<Operation> listeOpes;

        int idNumCli = compteActu.idNumCli;
        //récupérer le client
        try {
            Access_BD_Client ac = new Access_BD_Client();
            clientDuCompteActu = ac.getClient(idNumCli);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int idAg = clientDuCompteActu.idAg;
        //récupérer l'agence
        try {
            Access_BD_AgenceBancaire aab = new Access_BD_AgenceBancaire();
            agenceActuelle = aab.getAgenceBancaire(idAg);
        } catch (Exception e) {
            e.printStackTrace();
        }



		try {
			// Initialize PDF writer
			PdfWriter writer = new PdfWriter("Relevé de compte mensuel - " + compteActu.idNumCompte + ".pdf");
			// Initialize PDF document
			PdfDocument pdf = new PdfDocument(writer);
			// Initialize document
			Document document = new Document(pdf);
			PdfFont font = null;
			InputStream fontStream = Batch.class.getResourceAsStream("font/Helvetica.ttf");
			font = PdfFontFactory.createFont(IOUtils.toByteArray(fontStream), PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);

			
			PdfFont boldFont = null;
			InputStream boldFontStream = Batch.class.getResourceAsStream("font/Helvetica-Bold.ttf");
			boldFont = PdfFontFactory.createFont(IOUtils.toByteArray(boldFontStream), PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
			
			PdfFont lightFont = null;
			InputStream lightFontStream = Batch.class.getResourceAsStream("font/Helvetica-Light.ttf");
			lightFont = PdfFontFactory.createFont(IOUtils.toByteArray(lightFontStream), PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);

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
			Paragraph date = new Paragraph("Date de génération du relevé : " + java.time.LocalDate.now()).setFontSize(12);
			date.setTextAlignment(TextAlignment.RIGHT);
			document.add(date);
			Paragraph infosClient = new Paragraph(
				"Client : " + this.clientDuCompteActu.nom + " " + this.clientDuCompteActu.prenom + " (ID : " + this.clientDuCompteActu.idNumCli + ") \n" +
				this.clientDuCompteActu.email + "\n" +
				this.clientDuCompteActu.adressePostale + "\n" +
				"De l'agence : " + this.agenceActuelle.nomAg + " (ID : " + this.clientDuCompteActu.idAg + ")"
				).setFontSize(12);
			infosClient.setTextAlignment(TextAlignment.LEFT);
			document.add(infosClient);
			Paragraph infosCompte = new Paragraph("Compte : " + this.compteActu.idNumCompte).setFontSize(12);
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
			Double oldSolde = this.compteActu.solde;
			listeOpes = getOperationsDunCompte(compteActu);
			for (Operation currOp : listeOpes) {
				oldSolde -= currOp.montant;
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
				table.addCell(new Cell().add(new Paragraph(currOp.dateOp+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
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
						
			rightCell = new Cell().add(new Paragraph(df.format(compteActu.solde))
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
		} catch(Exception e) {
		    // Handle exception here
		    e.printStackTrace();
		}
	}

    public ArrayList<Operation> getOperationsDunCompte(CompteCourant selectedItem) {
		ArrayList<Operation> listeOp = new ArrayList<>();

		try {
			Access_BD_Operation aop = new Access_BD_Operation();
			listeOp = aop.getOperations(selectedItem.idNumCompte);
		} catch (Exception e) {
			listeOp = new ArrayList<>();
        }
		return listeOp;
	}
}