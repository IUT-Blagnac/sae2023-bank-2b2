package app.control;

import java.io.InputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

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
import com.itextpdf.layout.element.Table;
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

public class Batch {
    private Client clientDuCompteActu;
    private CompteCourant compteActu;
    private AgenceBancaire agenceActuelle;

    public void start() {
        System.out.println("COUCOU");
		int nbClients = 0;
		Access_BD_CompteCourant acCourant = new Access_BD_CompteCourant();
		try {
			for(CompteCourant compte : acCourant.getAllCompteCourants() ) {
				System.out.println(compte.toString());
				doPrelev(compte.idNumCompte);
			}
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DatabaseConnexionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
			nbClients = acc.getNbCpt();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 1; i < nbClients-1; i++) {
        	try {
        	    Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
        	    compteActu = acc.getCompteCourant(i);
        	} catch (Exception e) {
        	    e.printStackTrace();
        	}
        	doRel();
		}

       
    }
    
	private void doPrelev(int idNumCompte) {
    	Access_BD_Operation aop = new Access_BD_Operation();
    	Access_BD_Prelevement apa = new Access_BD_Prelevement();
    	Access_BD_CompteCourant acc = new Access_BD_CompteCourant();
    	Date date = Date.valueOf(LocalDate.now());
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	int joursDansLeMois = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    	int jour = calendar.get(Calendar.DAY_OF_MONTH);
    	try {
			for(Prelevement prelevements : apa.getPrelevements(idNumCompte)) {
				if(prelevements.datePrelev == jour) {
					System.out.println("jour bon");
					System.out.println(""+prelevements.debitPrelev);
					try {
						aop.insertDebit(acc.getCompteCourant(idNumCompte), prelevements.debitPrelev, "Prélèvement automatique");
					} catch (ManagementRuleViolation e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RowNotFoundOrTooManyRowsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(prelevements.datePrelev > joursDansLeMois && jour == joursDansLeMois) {
					try {
						aop.insertDebit(acc.getCompteCourant(idNumCompte), prelevements.debitPrelev, "Prélèvement automatique");
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
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DecimalFormat df = new DecimalFormat("0.000");
		ArrayList<Operation> listeOpes;
		Date jdate = Date.valueOf(LocalDate.now());
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(jdate);
    	int jour = calendar.get(Calendar.DAY_OF_MONTH);

		if (jour == 1) {
			
		}

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
			PdfWriter writer = new PdfWriter("Relevé de compte mensuel - " + compteActu.idNumCompte + " - " + java.time.LocalDate.now().toString() + ".pdf");
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

			LocalDate today = LocalDate.now();
			LocalDate lastMonth = today.minusMonths(1);

			String lastMonthName = lastMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
			lastMonthName = "\n Releve de compte du mois de " + lastMonthName.substring(0, 1).toUpperCase() + lastMonthName.substring(1);

			Paragraph date = new Paragraph(lastMonthName + "\nDate de génération du relevé : " + today.format(formatter)).setFontSize(12);
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
				if (currOp.dateOp.toLocalDate().getMonth() ==  lastMonth.getMonth()) {
					oldSolde -= currOp.montant;
				}
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
				if (currOp.dateOp.toLocalDate().getMonth() == java.time.LocalDate.now().getMonth()) {
					table.addCell(new Cell().add(new Paragraph(currOp.idNumCompte+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph(currOp.dateOp.toLocalDate().format(formatter)+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph(currOp.idOperation+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph((currOp.montant+"").replace(".", ",")).setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
					table.addCell(new Cell().add(new Paragraph(currOp.idTypeOp+"").setFontSize(11).setFontColor(ColorConstants.BLACK).setFont(lightFont)));
				}
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
			pdf.close();
			writer.close();
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
