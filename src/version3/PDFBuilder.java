package version3;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Created by robertrambo on 03/02/2016.
 */
public class PDFBuilder {

    private Collection collection;
    private WorkingDirectory workingDirectory;

    public PDFBuilder(Collection collectionInUse, WorkingDirectory workingDirectory){
        this.collection = collectionInUse;
        this.workingDirectory = workingDirectory;

    }

    // standard plots will be normalized Kratky and qRg*I(q)/I(0) vs qRg and Guinier?
    public void writePDFFile(String name){

        PDDocument doc = null;
        PDPage page = null;

        try{
            doc = new PDDocument();
            page = new PDPage();

            doc.addPage(page);
            PDFont font = PDType1Font.HELVETICA_BOLD;

            PDPageContentStream content = new PDPageContentStream(doc, page);
            content.beginText();
            content.setFont( font, 12 );
            content.moveTextPositionByAmount( 100, 700 );
            content.drawString("Hello from www.printmyfolders.com");

            content.endText();
            content.close();
            doc.save("PDFWithText.pdf");
            doc.close();
        } catch (Exception e){
            System.out.println(e);
        }

    }



}
