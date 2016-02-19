package version3;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by robertrambo on 03/02/2016.
 */
public class PDFBuilder extends SwingWorker<Void, Void> {

    private Collection collection;
    private WorkingDirectory workingDirectory;
    private JProgressBar bar;
    private String name;

    public PDFBuilder(Collection collectionInUse, WorkingDirectory workingDirectory, JProgressBar bar){
        this.collection = collectionInUse;
        this.workingDirectory = workingDirectory;
        this.bar = bar;
    }

    // standard plots will be normalized Kratky and qRg*I(q)/I(0) vs qRg and Guinier?
    public void setName(String name){
        this.name = name;
    }

    public void writePDFFile(){

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
            content.drawString("Coming Soon");
            content.endText();
            content.close();

            doc.save(workingDirectory.getWorkingDirectory()+"/PDFWithText.pdf");
            doc.close();

        } catch (Exception e){
            System.out.println(e);
        }

    }

    @Override
    protected Void doInBackground() throws Exception {
        this.bar.setIndeterminate(true);
        System.out.println("Writing PDF");
        this.writePDFFile();
        return null;
    }


    /**
     * @param page
     * @param contentStream
     * @param y the y-coordinate of the first row
     * @param margin the padding on left and right of table
     * @param content a 2d array containing the table data
     * @throws IOException
     */
    public static void drawTable(PDPage page, PDPageContentStream contentStream,
                                 float y, float margin,
                                 String[][] content) throws IOException {

        final int rows = content.length;
        final int cols = content[0].length;
        final float rowHeight = 20f;
        final float tableWidth = page.findMediaBox().getWidth()-(2*margin);
        final float tableHeight = rowHeight * rows;
        final float colWidth = tableWidth/(float)cols;
        final float cellMargin=5f;

        //draw the rows
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
            contentStream.drawLine(margin,nexty,margin+tableWidth,nexty);
            nexty-= rowHeight;
        }

        //draw the columns
        float nextx = margin;
        for (int i = 0; i <= cols; i++) {
            contentStream.drawLine(nextx,y,nextx,y-tableHeight);
            nextx += colWidth;
        }

        //now add the text
        contentStream.setFont(PDType1Font.HELVETICA_BOLD,12);

        float textx = margin+cellMargin;
        float texty = y-15;
        for(int i = 0; i < content.length; i++){
            for(int j = 0 ; j < content[i].length; j++){
                String text = content[i][j];
                contentStream.beginText();
                contentStream.moveTextPositionByAmount(textx,texty);
                contentStream.drawString(text);
                contentStream.endText();
                textx += colWidth;
            }
            texty-=rowHeight;
            textx = margin+cellMargin;
        }
    }
}
