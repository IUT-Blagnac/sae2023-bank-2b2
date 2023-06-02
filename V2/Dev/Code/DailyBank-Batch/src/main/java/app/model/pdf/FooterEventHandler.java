package app.model.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.*;

/**
 * Classe permettant de gérer les pieds de page des documents PDF en ajoutant un numéro de page.
 * @author Enzo Fournet
 */
public class FooterEventHandler implements IEventHandler {
    protected Document doc;
    public FooterEventHandler(Document doc) {
        this.doc = doc;
    }
    @Override
    public void handleEvent(Event currentEvent) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) currentEvent;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), doc.getPdfDocument());
        Canvas canvas = new Canvas(pdfCanvas, pageSize);

        int pageNumber = pdfDoc.getPageNumber(page);

        Paragraph footer = new Paragraph(""+pageNumber).setFontSize(10).setFontColor(ColorConstants.GRAY);
        canvas.showTextAligned(footer, pageSize.getWidth() / 2, pageSize.getBottom() + 20, TextAlignment.CENTER);
    }
}
