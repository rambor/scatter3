package version3.ReportPDF;


import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;

import rst.pdfbox.layout.elements.*;
import rst.pdfbox.layout.elements.Frame;
import rst.pdfbox.layout.elements.render.*;
import rst.pdfbox.layout.shape.Rect;

import rst.pdfbox.layout.shape.Stroke;

import rst.pdfbox.layout.text.*;
import version3.*;
import version3.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class Report {

    private Collection inUseCollection;
    private int totalInCollection;
    private final float imageWidth = 270;
    private final float imageHeight = 240;
    private PDFont pdfont = PDType1Font.COURIER;
    /**
     * create report using selected files in Collection
     * @param collection
     * @param workingDirectory
     */
    public Report(Collection collection, WorkingDirectory workingDirectory, String titleOf){
        inUseCollection = collection;
        totalInCollection = inUseCollection.getDatasetCount();

        // make scaled log10 plot

        // make dimensionless kratky plot

        // make q*I(q) plot

        // make P(r)-plot
        Document document = buildBasePlotsFromCollection(titleOf, true);
        //Document document = new Document(30, 30, 40, 60);

        // Add more details to Document

        //document.
        document.add(new VerticalSpacer(40));
//        ImageElement izerorg = new ImageElement(createIZeroRgPlotFromCollection());
//        izerorg.setWidth(imageWidth);
//        izerorg.setHeight(imageHeight);
//        document.add(izerorg, new VerticalLayoutHint(Alignment.Left, 0, 0 , 0, 0, true));

        // add note

        final OutputStream outputStream;

        try {
            outputStream = new FileOutputStream("letter.pdf");
            document.save(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * create report using single selected Dataset
     * @param dataset
     * @param workingDirectory
     */
    public Report(Dataset dataset, WorkingDirectory workingDirectory){
        inUseCollection = new Collection();
        inUseCollection.addDataset(dataset);
        inUseCollection.getDataset(0).setInUse(true);
        totalInCollection = 1;

        Document document = buildBasePlotsFromCollection(dataset.getFileName(), false);

        // Add Pr plot and Guinier with residuals if available
        // provide completed table
        document.add(new VerticalSpacer(40));
        ImageElement residualsGuinier = new ImageElement(createGuinierResidualsPlotFromDataset(dataset));
        residualsGuinier.setWidth(imageWidth);
        residualsGuinier.setHeight(imageHeight);
        document.add(residualsGuinier, new VerticalLayoutHint(Alignment.Right, 0, 0 , 0, 0, true));


        ImageElement guinier = new ImageElement(createGuinierPlotFromDataset(dataset));
        guinier.setWidth(imageWidth);
        guinier.setHeight(imageHeight);
        document.add(guinier, new VerticalLayoutHint(Alignment.Left, 0, 0 , 0, 0, true));

        // add table
        document.add(new VerticalSpacer(300));

        try {
            Paragraph tempparagraph = new Paragraph();
            tempparagraph.addMarkup(getDataSummaryTopHeader(), 10, BaseFont.Courier);
            Frame headerFrame = new Frame(tempparagraph, (float)(document.getPageWidth()/1.2), 12f);
            headerFrame.setShape(new Rect());
            headerFrame.setBorder(Color.WHITE, new Stroke(0.5f));
            headerFrame.setPadding(0, 0, 0, 0);
            document.add(headerFrame, VerticalLayoutHint.CENTER);

            Frame lineFrame = new Frame(new Paragraph(), (float)(document.getPageWidth()/1.2), 1f);
            lineFrame.setShape(new Rect());
            lineFrame.setBorder(Color.black, new Stroke(0.5f));
            lineFrame.setPadding(0, 0, 0, 0);
            document.add(lineFrame, VerticalLayoutHint.CENTER);


            ArrayList<String> rows = this.getSummaryTableRowsFromDataset(dataset);
            int totalrows = rows.size();
            for(int i=0; i<totalrows; i++){
                Paragraph tempparag = new Paragraph();
                tempparag.addMarkup(rows.get(i), 10, BaseFont.Courier);
                Frame frame = new Frame(tempparag, (float)(document.getPageWidth()/1.2), 12f);
                frame.setShape(new Rect());
                frame.setBorder(Color.white, new Stroke(0.5f));
                frame.setPadding(0, 0, 0, 0);
                document.add(frame, VerticalLayoutHint.CENTER);
            }

            document.add(lineFrame, VerticalLayoutHint.CENTER);
            String legend = "All values are reported in non-SI units of Angstroms or inverse Angstroms.";
            Paragraph legendP = new Paragraph();
            legendP.addMarkup(legend,12, BaseFont.Times);
            document.add(legendP, VerticalLayoutHint.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }



        final OutputStream outputStream;

        try {
            outputStream = new FileOutputStream(dataset.getFileName()+".pdf");
            document.save(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Document buildBasePlotsFromCollection(String titleOf, boolean addIzeroRgPlot){
        // make scaled log10 plot

        // make dimensionless kratky plot

        // make q*I(q) plot

        // make P(r)-plot

        Document document = new Document(30, 30, 40, 60);
        document.addRenderListener(new RenderListener() {

            @Override
            public void beforePage(RenderContext renderContext) {}

            @Override
            public void afterPage(RenderContext renderContext) throws IOException {
                String content = String.format("%s", renderContext.getPageIndex() + 1);
                TextFlow text = TextFlowUtil.createTextFlow(content, 11, PDType1Font.TIMES_ROMAN);

                float offset = renderContext.getPageFormat().getMarginLeft() + TextSequenceUtil.getOffset(text, renderContext.getWidth(), Alignment.Right);
                text.drawText(renderContext.getContentStream(), new Position(offset, 30), Alignment.Right, null);
            }
        });


        try {
            Paragraph title = new Paragraph();
            title.addMarkup(escape(titleOf), 10, BaseFont.Times);
            document.add(title, VerticalLayoutHint.CENTER);
            document.add(new VerticalSpacer(5));
        } catch (IOException e) {
            e.printStackTrace();
        }


        BufferedImage log10 = createLog10ChartFromCollection();
        BufferedImage kratky = createKratklyPlotFromCollection();
        BufferedImage qIq = createqIqPlotFromCollection();

        ImageElement imageLog10 = new ImageElement(log10);
        imageLog10.setWidth(imageWidth);
        imageLog10.setHeight(imageHeight);
        document.add(imageLog10, new VerticalLayoutHint(Alignment.Left, 0, 0 , 0, 0, true));

        ImageElement imageKratky = new ImageElement(kratky);
        imageKratky.setWidth(imageWidth);
        imageKratky.setHeight(imageHeight);
        document.add(imageKratky, new VerticalLayoutHint(Alignment.Right, 0, 0 , 0, 0, true));


        document.add(new VerticalSpacer(240));
        ImageElement imageqIqPlot = new ImageElement(qIq);
        imageqIqPlot.setWidth(imageWidth);
        imageqIqPlot.setHeight(imageHeight);
        document.add(imageqIqPlot, new VerticalLayoutHint(Alignment.Left, 0, 0 , 0, 0, true));

        if (addIzeroRgPlot){
            ImageElement izerorg = new ImageElement(createIZeroRgPlotFromCollection());
            izerorg.setWidth(imageWidth);
            izerorg.setHeight(imageHeight);
            document.add(izerorg, new VerticalLayoutHint(Alignment.Right, 0, 0 , 0, 0, true));
        } // add Pr distribution plot if single


        // add table of results (color text by data color)
        document.add(new VerticalSpacer(240));


        ArrayList<String> header = getHeader();
        document.add(new ColumnLayout(1, 5));

        try {
            Paragraph tempparagraph = new Paragraph();
            tempparagraph.addMarkup(getHeaderString(), 8, BaseFont.Courier);
            Frame headerFrame = new Frame(tempparagraph, document.getPageWidth(), 10f);
            headerFrame.setShape(new Rect());
            headerFrame.setBorder(Color.WHITE, new Stroke(0.5f));
            headerFrame.setPadding(0, 0, 0, 0);
            //headerFrame.setMargin(40, 40, 5, 0);
            document.add(headerFrame, VerticalLayoutHint.CENTER);

            Frame lineFrame = new Frame(new Paragraph(), (float)(document.getPageWidth()), 1f);
            lineFrame.setShape(new Rect());
            lineFrame.setBorder(Color.black, new Stroke(0.5f));
            lineFrame.setPadding(0, 0, 0, 0);
            document.add(lineFrame, VerticalLayoutHint.CENTER);

            for(int i=0;i<totalInCollection; i++){
                if (inUseCollection.getDataset(i).getInUse()){
                    document.add(this.makeRow(inUseCollection.getDataset(i), document.getPageWidth()), VerticalLayoutHint.CENTER);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        try {
            document.add(new VerticalSpacer(20));
            Paragraph paragraph = new Paragraph();
            paragraph.addText("Additional Details", 10, PDType1Font.TIMES_ROMAN);
            document.add(paragraph);

            Paragraph tempparagraph = new Paragraph();
            tempparagraph.addMarkup(getAdditionalDetailsString(), 8, BaseFont.Courier);
            Frame headerFrame = new Frame(tempparagraph, document.getPageWidth(), 10f);
            headerFrame.setShape(new Rect());
            headerFrame.setBorder(Color.WHITE, new Stroke(0.5f));
            headerFrame.setPadding(0, 0, 0, 0);
            //headerFrame.setMargin(40, 40, 5, 0);
            document.add(headerFrame, VerticalLayoutHint.CENTER);

            Frame lineFrame = new Frame(new Paragraph(), (float)(document.getPageWidth()), 1f);
            lineFrame.setShape(new Rect());
            lineFrame.setBorder(Color.black, new Stroke(0.5f));
            lineFrame.setPadding(0, 0, 0, 0);
            document.add(lineFrame, VerticalLayoutHint.CENTER);

            for(int i=0;i<totalInCollection; i++){
                if (inUseCollection.getDataset(i).getInUse()){
                    document.add(this.makeAdditionalDetailsRow(inUseCollection.getDataset(i), document.getPageWidth()), VerticalLayoutHint.CENTER);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    private BufferedImage createGuinierPlotFromDataset(Dataset dataset){
        XYSeriesCollection guinierCollection = new XYSeriesCollection();
        guinierCollection.addSeries(new XYSeries("GUINIER MODEL LINE"));
        // create residual series and line fits
        XYSeries dataInUse = new XYSeries("DataInuse");
        XYSeries tempG = dataset.getGuinierData();
        for(int i=dataset.getIndexOfLowerGuinierFit(); i<dataset.getIndexOfUpperGuinierFit(); i++){
            dataInUse.add(tempG.getDataItem(i));
        }
        double rg = dataset.getGuinierRg();
        double slope = -rg*rg/3.0;
        double intercept = Math.log(dataset.getGuinierIzero());

        guinierCollection.getSeries(0).add(dataInUse.getMinX(), slope*dataInUse.getMinX()+intercept);
        guinierCollection.getSeries(0).add(dataInUse.getMaxX(), slope*dataInUse.getMaxX()+intercept);
        guinierCollection.addSeries(dataInUse);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Guinier fit",                // chart title
                "",                       // domain axis label
                "ln I(q)",                // range axis label
                guinierCollection,               // data
                PlotOrientation.VERTICAL,
                false,                     // include legend
                true,
                false
        );


        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("");
        final NumberAxis rangeAxis = new NumberAxis("ln [I(q)]");

        Font fnt = new Font("SansSerif", Font.BOLD, 24);
        domainAxis.setLabelFont(fnt);
        rangeAxis.setLabelFont(fnt);
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 20));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 20));
        domainAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRangeStickyZero(false);
        String quote = "q\u00B2 (\u212B \u207B\u00B2)";
        domainAxis.setLabel(quote);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setBackgroundPaint(null);

        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(false);
        renderer1.setSeriesLinesVisible(1, false);
        renderer1.setSeriesShapesVisible(0, true);
        renderer1.setSeriesShapesVisible(0, false);
        renderer1.setSeriesPaint(0, Color.red);
        renderer1.setSeriesStroke(0, dataset.getStroke());

        renderer1.setSeriesPaint(1, dataset.getColor());
        renderer1.setSeriesShape(1, new Ellipse2D.Double(-4, -4, 8.0, 8.0));
        renderer1.setSeriesOutlinePaint(1, dataset.getColor());
        renderer1.setSeriesOutlineStroke(1, dataset.getStroke());

        return chart.createBufferedImage((int)imageWidth*3,(int)imageHeight*3);
    }

    private BufferedImage createGuinierResidualsPlotFromDataset(Dataset dataset){

        // rebuild residuals dataset
        XYSeriesCollection residualsDataset = new XYSeriesCollection();
        residualsDataset.addSeries(new XYSeries("residuals"));
        residualsDataset.addSeries(new XYSeries("line"));

        XYSeries dataInUse = new XYSeries("DataInuse");
        XYSeries tempG = dataset.getGuinierData();
        for(int i=dataset.getIndexOfLowerGuinierFit(); i<dataset.getIndexOfUpperGuinierFit(); i++){
            dataInUse.add(tempG.getDataItem(i));
        }

        int itemCount = dataInUse.getItemCount();

        double rg = dataset.getGuinierRg();
        double slope = -rg*rg/3.0;
        double intercept = Math.log(dataset.getGuinierIzero());

        for (int v=0; v< itemCount; v++) {
            XYDataItem item = dataInUse.getDataItem(v);
            residualsDataset.getSeries(0).add(item.getX(),item.getY().doubleValue()-(slope*item.getX().doubleValue()+intercept));
        }

        residualsDataset.getSeries(1).add(dataInUse.getMinX(), 0);
        residualsDataset.getSeries(1).add(dataInUse.getMaxX(), 0);

        // add to chart

        JFreeChart residualsChart = ChartFactory.createXYLineChart(
                "Residuals",                // chart title
                "",                    // domain axis label
                "residuals",                  // range axis label
                residualsDataset,               // data
                PlotOrientation.VERTICAL,
                false,                     // include legend
                true,
                false
        );

        final XYPlot residuals = residualsChart.getXYPlot();

        final NumberAxis domainAxis = new NumberAxis("");
        final NumberAxis rangeAxis = new NumberAxis("");

        Font fnt = new Font("SansSerif", Font.BOLD, 24);
        domainAxis.setLabelFont(fnt);
        rangeAxis.setLabelFont(fnt);

        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 20));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 20));

        domainAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRangeStickyZero(false);

        String quote = "q\u00B2 (\u212B \u207B\u00B2)";
        domainAxis.setLabel(quote);
        residuals.setDomainAxis(domainAxis);
        residuals.setBackgroundPaint(null);

        XYLineAndShapeRenderer renderer2 = (XYLineAndShapeRenderer) residuals.getRenderer();
        renderer2.setBaseShapesVisible(true);
        renderer2.setBaseShapesFilled(false);
        renderer2.setSeriesLinesVisible(0, false);
        renderer2.setSeriesShapesVisible(1, true);
        renderer2.setSeriesShapesVisible(1, false);
        renderer2.setSeriesPaint(1, Color.red);
        renderer2.setSeriesStroke(1, dataset.getStroke());
        renderer2.setSeriesPaint(0, Color.BLACK);
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8.0, 8.0));


        return residualsChart.createBufferedImage((int)imageWidth*3,(int)imageHeight*3);
    }


    private BufferedImage createIZeroRgPlotFromCollection(){

        //
        // previously miniCollection was being plotted
        // this is really the active datasets,
        // in the constructor for collection, miniCollection is derived from dataset (same pointer)
        //

        XYSeriesCollection izerosCollection = new XYSeriesCollection();
        XYSeriesCollection rgsCollection = new XYSeriesCollection();
        XYSeries izeros = new XYSeries("I Zero");
        XYSeries rgs = new XYSeries("Rg");


        double tempUpper;
        double upperLeft = 0;
        double lowerLeft = 10000000;
        double upperRight = 0;
        double lowerRight = 10000000;

        for (int i=0; i<totalInCollection; i++){
            Dataset tempData = inUseCollection.getDataset(i);

            if (tempData.getInUse() && tempData.getGuinierRg() > 0){
                tempUpper = tempData.getGuinierIzero();
                izeros.add(i+1, tempUpper); // follows index of the files selected

                if (tempUpper > upperLeft){
                    upperLeft = tempUpper;
                }

                if (tempUpper < lowerLeft){
                    lowerLeft = tempUpper;
                }

                tempUpper = tempData.getGuinierRg();
                rgs.add(i+1, tempUpper);

                if (tempUpper > upperRight){
                    upperRight = tempUpper;
                }

                if (tempUpper < lowerRight){
                    lowerRight = tempUpper;
                }
            }
        }

        izerosCollection.addSeries(izeros);
        rgsCollection.addSeries(rgs);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "",                // chart title
                "Number",                        // domain axis label
                "I(0)",                // range axis label
                izerosCollection,           // data
                PlotOrientation.VERTICAL,
                false,                      // include legend
                true,
                false
        );

        chart.setTitle("I(zero) Rg Plot");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 24));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);

        XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("File Number");
        final NumberAxis rangeAxisLeft = new NumberAxis("Left");
        final NumberAxis rangeAxisRight = new NumberAxis("Right");


        String quote = "Number";
        domainAxis.setLabel(quote);
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setLabelFont(new Font("Times", Font.BOLD, 28));
        domainAxis.setTickLabelFont(new Font("Times", Font.BOLD, 22));

        quote = "I(0)";

        rangeAxisLeft.setLabel(quote);
        rangeAxisLeft.setLabelFont(new Font("Times", Font.BOLD, 30));
        rangeAxisLeft.setTickLabelFont(new Font("Times", Font.BOLD, 22));
        rangeAxisLeft.setLabelPaint(new Color(255, 153, 51));
        rangeAxisLeft.setAutoRange(false);
        rangeAxisLeft.setRange(lowerLeft-lowerLeft*0.03, upperLeft+0.1*upperLeft);
        rangeAxisLeft.setAutoRangeStickyZero(false);

        String quoteR = "Rg Å";
        rangeAxisRight.setLabel(quoteR);
        rangeAxisRight.setLabelFont(new Font("Times", Font.BOLD, 30));
        rangeAxisRight.setTickLabelFont(new Font("Times", Font.BOLD, 22));
        rangeAxisRight.setLabelPaint(new Color(51, 153, 255));
        rangeAxisRight.setAutoRange(false);
        rangeAxisRight.setRange(lowerRight-lowerRight*0.03, upperRight+0.1*upperRight);
        rangeAxisRight.setAutoRangeStickyZero(false);

        domainAxis.setAutoRangeStickyZero(false);

        plot.setDomainAxis(0, domainAxis);
        plot.setRangeAxis(0, rangeAxisLeft);
        plot.setRangeAxis(1, rangeAxisRight);

        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setOutlineVisible(false);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYLineAndShapeRenderer leftRenderer = (XYLineAndShapeRenderer) plot.getRenderer();
        XYLineAndShapeRenderer rightRenderer = new XYLineAndShapeRenderer();

        plot.setDataset(0, izerosCollection);
        plot.setRenderer(0,leftRenderer);
        plot.setDataset(1,rgsCollection);
        plot.setRenderer(1, rightRenderer);       //render as a line

        plot.mapDatasetToRangeAxis(0, 0);//1st dataset to 1st y-axis
        plot.mapDatasetToRangeAxis(1, 1); //2nd dataset to 2nd y-axis

        leftRenderer.setBaseShapesVisible(true);
        leftRenderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));
        leftRenderer.setSeriesLinesVisible(0, false);
        leftRenderer.setSeriesPaint(0, new Color(255, 153, 51));
        leftRenderer.setSeriesVisible(0, true);
        leftRenderer.setSeriesOutlineStroke(0, new BasicStroke(2));
        leftRenderer.setSeriesOutlinePaint(0, Color.BLACK);

        rightRenderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));
        rightRenderer.setSeriesLinesVisible(0, false);
        rightRenderer.setSeriesPaint(0, new Color(51, 153, 255));
        rightRenderer.setSeriesShapesFilled(0, true);
        rightRenderer.setSeriesVisible(0, true);
        rightRenderer.setSeriesOutlineStroke(0, new BasicStroke(2));

        plot.setDomainZeroBaselineVisible(false);
        plot.setShadowGenerator(null);
        chart.getRenderingHints().put(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, Boolean.TRUE);
        return chart.createBufferedImage((int)imageWidth*3,(int)imageHeight*3);
    }


    private BufferedImage createqIqPlotFromCollection(){
        int totalSets = inUseCollection.getDatasetCount();

        XYSeriesCollection plottedDatasets = new XYSeriesCollection();
        double qlower=10, qupper=-1, ilower = 10, iupper = -1;

        for (int i=0; i < totalSets; i++){
            Dataset tempData = inUseCollection.getDataset(i);
            if (tempData.getGuinierRg() > 0 && tempData.getInUse()){
                tempData.scalePlottedQIQData();
                plottedDatasets.addSeries(tempData.getPlottedQIQDataSeries()); // positive only data
            }
        }

        ilower = plottedDatasets.getRangeLowerBound(true);
        iupper = plottedDatasets.getRangeUpperBound(true);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "q",                             // domain axis label
                "",                     // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.getXYPlot().setDomainCrosshairVisible(false);
        chart.getXYPlot().setRangeCrosshairVisible(false);

        String xaxisLabel = "q (\u212B\u207B\u00B9)";
        String yaxisLabel = "q\u00D7 I(q)";

        chart.getTitle().setVisible(false);
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 10));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);


        XYPlot plot = chart.getXYPlot();

        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setOutlineVisible(false);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        ValueMarker marker = new ValueMarker(0);
        marker.setLabelAnchor(RectangleAnchor.RIGHT);
        marker.setLabelBackgroundColor(Color.white);
        marker.setLabelOffset(new RectangleInsets(0,0,0,-40));

        marker.setLabel("0");
        marker.setLabelFont(Constants.BOLD_18);
        marker.setStroke(new BasicStroke(1.8f));
        plot.addRangeMarker(marker);


        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        //set dot size for all series
        double offset;
        int counted = 0;

        for (int i=0; i < totalSets; i++){
            Dataset tempData = inUseCollection.getDataset(i);
            if (tempData.getInUse()){
                offset = -0.5*tempData.getPointSize();
                renderer1.setSeriesShape(counted, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
                renderer1.setSeriesLinesVisible(counted, false);
                renderer1.setSeriesPaint(counted, tempData.getColor());
                renderer1.setSeriesShapesFilled(counted, tempData.getBaseShapeFilled());
                renderer1.setSeriesVisible(counted, tempData.getInUse());
                renderer1.setSeriesOutlineStroke(counted, tempData.getStroke());
                counted++;
            }
        }

        plot.setDomainAxis(getDomainAxis(xaxisLabel));
        plot.setRangeAxis(getRangeAxis(yaxisLabel, ilower, iupper));

//        NumberAxis tempAxis = (NumberAxis) plot.getRangeAxis();
//        tempAxis.setNumberFormatOverride(new DecimalFormat() {
//            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
//                if (number == 0){
//                    return toAppendTo.append(0);
//                }
//                return toAppendTo.append("1");
//            }
//        });



        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setDomainZeroBaselineVisible(false);
        plot.setShadowGenerator(null);
        chart.getRenderingHints().put(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, Boolean.TRUE);
        return chart.createBufferedImage((int)imageWidth*3,(int)imageHeight*3);
    }

    private BufferedImage createKratklyPlotFromCollection(){

        int totalSets = inUseCollection.getDatasetCount();

        XYSeriesCollection plottedDatasets = new XYSeriesCollection();
        double qlower=10, qupper=-1, ilower = 10, iupper = -1;

        int rgCount = 0;
        int inUseCount =0;

        for (int i=0; i < totalSets; i++){
            if (inUseCollection.getDataset(i).getInUse()){
                double rg = inUseCollection.getDataset(i).getGuinierRg();
                if (rg > 0){
                    rgCount+=1;
                }
                inUseCount+=1;
            }
        }

        boolean useNormalized = false;
        if (((double)rgCount)/(double)inUseCount > 0.45){
            useNormalized = true;
        }

        if (useNormalized){
            for (int i=0; i < totalSets; i++){
                Dataset tempData = inUseCollection.getDataset(i);
                if (tempData.getGuinierRg() > 0 && tempData.getInUse()){
                    tempData.createNormalizedKratkyReciRgData();
                    plottedDatasets.addSeries(tempData.getNormalizedKratkyReciRgData()); // positive only data

                }
            }
        } else {
            for (int i=0; i < totalSets; i++){
                Dataset tempData = inUseCollection.getDataset(i);
                if (tempData.getInUse()){
                    tempData.scalePlottedKratkyData();
                    plottedDatasets.addSeries(tempData.getPlottedKratkyDataSeries()); // positive only data
                }
            }
        }

        ilower = plottedDatasets.getRangeLowerBound(true);
        iupper = plottedDatasets.getRangeUpperBound(true);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "q",                             // domain axis label
                "log[I(q)]",                     // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.getXYPlot().setDomainCrosshairVisible(false);
        chart.getXYPlot().setRangeCrosshairVisible(false);

        String xaxisLabel, yaxisLabel;
        if (useNormalized){
            chart.setTitle("Dimensionless Kratky Plot");
            xaxisLabel = "q\u2217Rg";
            yaxisLabel = "I(q)/I(0)\u2217(q\u2217Rg)\u00B2";
        } else {
            chart.setTitle("Kratky Plot");
            xaxisLabel = "q, \u212B \u207B\u00B9";
            yaxisLabel = "q\u00B2 \u00D7 I(q)";
        }

        chart.getTitle().setVisible(false);
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 10));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);


        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setOutlineVisible(false);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        //set dot size for all series
        double offset;
        int counted = 0;

        if (useNormalized){
            for (int i=0; i < totalSets; i++){
                Dataset tempData = inUseCollection.getDataset(i);
                if (tempData.getGuinierRg() > 0 && tempData.getInUse()){
                    offset = -0.5*tempData.getPointSize();
                    renderer1.setSeriesShape(counted, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
                    renderer1.setSeriesLinesVisible(counted, false);
                    renderer1.setSeriesPaint(counted, tempData.getColor());
                    renderer1.setSeriesShapesFilled(counted, tempData.getBaseShapeFilled());
                    renderer1.setSeriesVisible(counted, tempData.getInUse());
                    renderer1.setSeriesOutlineStroke(counted, tempData.getStroke());
                    counted++;
                }
            }
            plot.addDomainMarker(new ValueMarker(1.7320508));
            plot.addRangeMarker(new ValueMarker(1.1));

        } else {
            for (int i=0; i < totalSets; i++){
                Dataset tempData = inUseCollection.getDataset(i);
                if (tempData.getInUse()){
                    offset = -0.5*tempData.getPointSize();
                    renderer1.setSeriesShape(counted, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
                    renderer1.setSeriesLinesVisible(counted, false);
                    renderer1.setSeriesPaint(counted, tempData.getColor());
                    renderer1.setSeriesShapesFilled(counted, tempData.getBaseShapeFilled());
                    renderer1.setSeriesVisible(counted, tempData.getInUse());
                    renderer1.setSeriesOutlineStroke(counted, tempData.getStroke());
                    counted++;
                }
            }
        }

        plot.setDomainAxis(getDomainAxis(xaxisLabel));
        plot.setRangeAxis(getRangeAxis(yaxisLabel, ilower, iupper));

        ValueMarker marker = new ValueMarker(0);
        marker.setLabelAnchor(RectangleAnchor.RIGHT);
        marker.setLabelBackgroundColor(Color.white);
        marker.setLabelOffset(new RectangleInsets(0,0,0,0));
        marker.setLabel("0");
        marker.setLabelFont(Constants.BOLD_18);
        marker.setStroke(new BasicStroke(1.8f));
        plot.addRangeMarker(marker);

        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setDomainZeroBaselineVisible(false);

        return chart.createBufferedImage((int)imageWidth*3,(int)imageHeight*3);
    }


    private BufferedImage createLog10ChartFromCollection() {

        int totalSets = inUseCollection.getDatasetCount();
        //plottedDatasets = new XYSeriesCollection();  // spinners will always modify the plottedDataset series
        XYSeriesCollection plottedDatasets = new XYSeriesCollection();
        double qlower=10, qupper=-1, ilower = 10, iupper = -1;

        for (int i=0; i < totalSets; i++){
            if (inUseCollection.getDataset(i).getInUse()){
                plottedDatasets.addSeries(inUseCollection.getDataset(i).getData()); // positive only data

                Dataset tempData = inUseCollection.getDataset(i);

                if (tempData.getAllData().getMinX() < qlower){
                    qlower = tempData.getAllData().getMinX();
                }

                if (tempData.getAllData().getMaxX() > qupper){
                    qupper = tempData.getAllData().getMaxX();
                }

                if (tempData.getData().getMinY() < ilower){
                    ilower = tempData.getData().getMinY();
                }

                if (tempData.getData().getMaxY() > iupper){
                    iupper = tempData.getData().getMaxY();
                }
            }
        }


        JFreeChart chart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "q",                             // domain axis label
                "log[I(q)]",                     // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.getXYPlot().setDomainCrosshairVisible(false);
        chart.getXYPlot().setRangeCrosshairVisible(false);
        chart.setTitle("Intensity Plot");
        chart.getTitle().setVisible(false);
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 12));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);


        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setOutlineVisible(false);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        //set dot size for all series
        double offset;
        int counted = 0;

        for (int i=0; i < totalSets; i++){

            if (inUseCollection.getDataset(i).getInUse()){
                Dataset tempData = inUseCollection.getDataset(i);

                offset = -0.5*tempData.getPointSize();
                renderer1.setSeriesShape(counted, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
                renderer1.setSeriesLinesVisible(counted, false);
                renderer1.setSeriesPaint(counted, tempData.getColor());
                renderer1.setSeriesShapesFilled(counted, tempData.getBaseShapeFilled());
                renderer1.setSeriesVisible(counted, tempData.getInUse());
                renderer1.setSeriesOutlineStroke(counted, tempData.getStroke());
                counted++;
            }
        }

        plot.setDomainAxis(getDomainAxis("q (\u212B\u207B\u00B9)"));
        plot.setRangeAxis(getRangeAxis("log10[I(q)]", ilower, iupper));
        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setDomainZeroBaselineVisible(false);

        return chart.createBufferedImage((int)imageWidth*3,(int)imageHeight*3);
    }



    private JFreeChart createLog10ChartFromDataset(Dataset dataset) {

        XYSeriesCollection plottedDatasets = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "q",                             // domain axis label
                "log[I(q)]",                     // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.getXYPlot().setDomainCrosshairVisible(false);
        chart.getXYPlot().setRangeCrosshairVisible(false);
        chart.setTitle("Intensity Plot");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);


        XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("Log Intensity");
        String quote = "q (\u212B\u207B\u00B9)";
        domainAxis.setLabelFont(Constants.FONT_BOLD_20);
        domainAxis.setTickLabelFont(Constants.FONT_BOLD_20);
        domainAxis.setLabel(quote);
        domainAxis.setAxisLineStroke(new BasicStroke(3.0f));
        quote = "log[I(q)]";

        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRange(false);
        rangeAxis.setLabelFont(Constants.FONT_BOLD_28);
        rangeAxis.setTickLabelFont(Constants.FONT_BOLD_28);
        rangeAxis.setTickLabelsVisible(false);
        rangeAxis.setAxisLineStroke(new BasicStroke(3.0f));
        rangeAxis.setTickLabelPaint(Color.BLACK);

        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setOutlineVisible(false);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        plot.setDataset(0,plottedDatasets);
        plot.setRenderer(0,renderer1);

        //set dot size for all series
        plottedDatasets.addSeries(dataset.getData()); // positive only data

        double lower = dataset.getAllData().getMinX();
        double upper = dataset.getAllData().getMaxX();
        double dlower = dataset.getData().getMinY();
        double dupper = dataset.getData().getMaxY();

        double offset = -0.5*dataset.getPointSize();
        renderer1.setSeriesShape(0, new Ellipse2D.Double(offset, offset, dataset.getPointSize(), dataset.getPointSize()));
        renderer1.setSeriesLinesVisible(0, false);
        renderer1.setSeriesPaint(0, dataset.getColor());
        renderer1.setSeriesShapesFilled(0, dataset.getBaseShapeFilled());
        renderer1.setSeriesVisible(0, dataset.getInUse());
        renderer1.setSeriesOutlineStroke(0, dataset.getStroke());


        rangeAxis.setRange(lower-lower*0.03, upper+0.1*upper);
        rangeAxis.setAutoRangeStickyZero(false);

        domainAxis.setRange(dlower, dupper);
        domainAxis.setAutoRangeStickyZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setDomainZeroBaselineVisible(false);

        return chart;
    }

    private static NumberAxis getRangeAxis(String quote, double ilower, double iupper){
        final NumberAxis rangeAxis = new NumberAxis(quote);
        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRange(false);
        rangeAxis.setLabelFont(Constants.FONT_BOLD_28);
        rangeAxis.setTickLabelFont(Constants.FONT_BOLD_28);
        rangeAxis.setTickLabelsVisible(false);
        rangeAxis.setRange(ilower-ilower*0.03, iupper+0.1*iupper);
        rangeAxis.setAutoRangeStickyZero(false);
        rangeAxis.setAxisLineStroke(new BasicStroke(3.0f));
        rangeAxis.setTickLabelPaint(Color.BLACK);

        return rangeAxis;
    }

    private static NumberAxis getDomainAxis(String quote){
        final NumberAxis domainAxis = new NumberAxis(quote);
        domainAxis.setLabelFont(Constants.FONT_BOLD_20);
        domainAxis.setTickLabelFont(Constants.FONT_BOLD_20);
        domainAxis.setLabel(quote);
        domainAxis.setAutoRangeStickyZero(false);
        domainAxis.setAxisLineStroke(new BasicStroke(3.0f));
        domainAxis.setTickMarkStroke(new BasicStroke(3.0f));

        return domainAxis;
    }

    private ArrayList<String > getHeader(){
        return new ArrayList<>(Arrays.asList(
                "Rg-Guinier",
                "error",
                "Rg-Real Space",
                "error",
                "I(zero)-Guinier",
                "error",
                "I(zero)-Real Space",
                "error",
                "Porod Volume",
                "Vc",
                "qmin - P(r)",
                "qmax - P(r)",
                "Shannon Points",
                "bin width",
                "filename"
                ));
    }

    private String getHeaderString(){
       // String temp = " Rg-Reci error  Rg-Real error  I[zero]-Reci error  I[zero]-Real error    Vp      dmax     filename ";

        String formatted = String.format(" %s   %s %s   %s %s   %s %s   %s %s %s %s",
                centerText("Rg-G", 7, ' '),      // xxxx.xx
                centerText("error", 5, ' '), // 12.22
                centerText("Rg-Real", 7, ' '),         // xxxx.xx
                centerText("error", 5, ' '),    // 12.22
                centerText("I[zero]-G", 9, ' '),   // 1.XXXE-04
                centerText("error", 7, ' '), // 1.XE-04
                centerText("I[zero]-R", 9, ' '), // 12.22
                centerText("error", 7, ' '), // 12.22
                centerText("Vp", 9, ' '), // 12.22
                centerText("dmax", 6, ' '), // 12.22
                centerText("filename", 9, ' ') // 12.22
        );

        return formatted;
    }

    private String getAdditionalDetailsString(){
        String formatted = String.format(" %s   %s  %s %s  %s  %s  %s %s  %s %s %s",
                centerText("PE", 7, ' '),      // xxxx.xx
                centerText("error", 5, ' '), // 12.22
                centerText("Vc-Reci", 9, ' '),         // xxxx.xx
                centerText("Vc-Real", 9, ' '),    // 12.22
                centerText("dmax", 7, ' '),   // 1.XXXE-04
                centerText("r<ave>", 6, ' '), // 1.XE-04
                centerText("qmin-Real", 9, ' '), // 12.22
                centerText("qmax-Real", 9, ' '), // 12.22
                centerText("Chi2", 6, ' '), // 12.22
                centerText("Sk2", 5, ' '), // 12.22
                centerText("filename", 9, ' ') // 12.22
        );

        return formatted;
    }

    private Frame makeAdditionalDetailsRow(Dataset dataset, float width)throws IOException {
        String pe = "";
        String peerror = "";
        if (dataset.getPorodVolume() > 0) {
            pe = String.format("%1.2f", (double)dataset.getPorodExponent());
            peerror = String.format("%1.2f", (double)dataset.getPorodExponentError());
        } else {
            pe = "nd";
            peerror = "nd";
        }

        String vc = (dataset.getVC() > 0) ? String.format("%1.3E", dataset.getVC()) : "nd";
        String vcreal = (dataset.getVCReal() > 0) ? String.format("%1.3E", dataset.getVCReal()) : "nd";
        String dmax = (dataset.getRealIzero() > 0) ? String.format("%6.1f", dataset.getRealSpaceModel().getDmax()) : "nd";
        String raverage = (dataset.getRealIzero() > 0) ? String.format("%.1f", dataset.getRealSpaceModel().getRaverage()) : "nd";
        String qmax = (dataset.getRealIzero() > 0) ? String.format("%.6f", dataset.getRealSpaceModel().getQmax()) : "nd";

        String qmin = (dataset.getRealIzero() > 0) ? String.format("%.6f", dataset.getRealSpaceModel().getfittedqIq().getMinX()) : "nd";
        String chi = (dataset.getRealIzero() > 0) ? String.format("%5.2f", dataset.getRealSpaceModel().getChi2()) : "nd";
        String sk2 = (dataset.getRealIzero() > 0) ? String.format("%5.2f", dataset.getRealSpaceModel().getKurt_l1_sum()) : "nd";


        Color inUse = dataset.getColor();
        String hex = String.format("#%02x%02x%02x", inUse.getRed(), inUse.getGreen(), inUse.getBlue());

        Paragraph tempparagraph = new Paragraph();

        int lengthOfFilename = dataset.getFileName().length();
        String escaped = escape(dataset.getFileName());
        if (lengthOfFilename > 20){
            escaped = escape(dataset.getFileName().substring(0,19));
        }



        String formatted = String.format("{color:%s} %s +-%s  %s %s  %s  %s  %s %s  %s %s %s",
                hex,
                centerText(pe, 7, ' '),      // x.xx
                centerText(peerror, 5, ' '), // 1.22
                centerText(vc, 9, ' '),         // 1.XXXE-04
                centerText(vcreal, 9, ' '),    // 12.22
                centerText(dmax, 7, ' '),   // 1000.5
                centerText(raverage, 6, ' '), // 101.1
                centerText(qmin, 9, ' '), // 12.22
                centerText(qmax, 9, ' '), // 12.22
                centerText(chi, 6, ' '), // 12.22
                centerText(sk2, 5, ' '), // 12.22
                escaped
        );


        tempparagraph.addMarkup(formatted, 8, BaseFont.Courier);

        //StyledText styled = new StyledText(formatted, 8.0f, pdfont, dataset.getColor());

        Frame frame = new Frame(tempparagraph, width, 10f);
        frame.setShape(new Rect());
        //frame.setBorder(dataset.getColor(), new Stroke(0.5f));
        frame.setBorder(Color.WHITE, new Stroke(0.5f));
        frame.setPadding(0, 0, 0, 0);
        //frame.setMargin(40, 40, 5, 0);

        return frame;
    }

    private Frame makeRow(Dataset dataset, float width) throws IOException {

        // 100.00 => 7.2f
       if (dataset.getRealIzero() > 0 ){
           dataset.getRealSpaceModel().estimateErrors();
       }

        String guinierRg = (dataset.getGuinierRg() > 0) ? String.format("%.2f", dataset.getGuinierRg()) : "nd";
        String guinierRgerror = (dataset.getGuinierRg() > 0) ? String.format("%.2f", dataset.getGuinierRG_sigma()): "nd";

        String realRg = (dataset.getRealRg() > 0) ? String.format("%.2f", dataset.getRealRg()) : "nd";
        String realRgerror = (dataset.getRealRg() > 0) ? String.format("%.2f", dataset.getRealRgSigma()) : "nd";

        String guinierIzero = (dataset.getGuinierIzero() > 0) ? String.format("%1.3E", dataset.getGuinierIzero()) : "nd";
        String guinierIzeroSigma = (dataset.getGuinierIzero() > 0) ? String.format("%1.1E", dataset.getGuinierIzeroSigma()) : "nd";

        String realIzero = (dataset.getRealIzero() > 0) ? String.format("%1.3E",dataset.getRealIzero()) : "nd";
        String realIzeroSigma = (dataset.getRealIzero() > 0) ?  String.format("%1.1E", dataset.getRealIzeroSigma()) : "nd";

        String vp = "";
        if (dataset.getPorodVolumeReal() > 0 ){
            vp = String.format("%1.3E", (double)dataset.getPorodVolumeReal());
        } else if (dataset.getPorodVolume() > 0) {
            vp = String.format("%1.3E", (double)dataset.getPorodVolume());
        } else {
            vp = "nd";
        }

        //String vc = (dataset.getVC() > 0) ? String.format("%1.4E", dataset.getVC()) : " nd ";

        //String qmax = (dataset.getRealIzero() > 0) ? String.format("%.6f", dataset.getRealSpaceModel().getQmax()) : "  nd  ";
        String dmax = (dataset.getRealIzero() > 0) ? String.format("%6.1f", dataset.getRealSpaceModel().getDmax()) : "  nd  ";

        Color inUse = dataset.getColor();
        String hex = String.format("#%02x%02x%02x", inUse.getRed(), inUse.getGreen(), inUse.getBlue());
        int lengthOfFilename = dataset.getFileName().length();
        String escaped = escape(dataset.getFileName());
        if (lengthOfFilename > 20){
            escaped = escape(dataset.getFileName().substring(0,19));
        }
        Paragraph tempparagraph = new Paragraph();

        String formatted = String.format("{color:%s} %s +-%s %s +-%s %s +-%s %s +-%s %s %s %s",
                hex,
                centerText(guinierRg, 7, ' '),      // xxxx.xx
                centerText(guinierRgerror, 5, ' '), // 12.22
                centerText(realRg, 7, ' '),         // xxxx.xx
                centerText(realRgerror, 5, ' '),    // 12.22
                centerText(guinierIzero, 9, ' '),   // 1.XXXE-04
                centerText(guinierIzeroSigma, 7, ' '), // 1.XE-04
                centerText(realIzero, 9, ' '), // 12.22
                centerText(realIzeroSigma, 7, ' '), // 12.22
                centerText(vp, 9, ' '), // 12.22
                centerText(dmax, 6, ' '), // 12.22
                escaped
        );

        tempparagraph.addMarkup(formatted, 8, BaseFont.Courier);

        Frame frame = new Frame(tempparagraph, width, 10f);
        frame.setShape(new Rect());
        frame.setBorder(Color.white, new Stroke(0.5f));
        frame.setPadding(0, 0, 0, 0);
        //frame.setMargin(40, 40, 5, 0);

        return frame;
    }

    private String escape(String text){
        return text.replace("_", "\\_");
    }


    private String getDataSummaryTopHeader(){
        return String.format("%s %s %s", rightJustifyText(" ",16,' '), centerText("Reciprocal",20, ' '), centerText("Real Space",20, ' '));
    }
    /**
     * Create Arraylist of elements to add to table
     * each element of list is a row
     *
      * @param dataset
     * @return
     */
    private ArrayList<String> getSummaryTableRowsFromDataset(Dataset dataset){

        // 16 wide - mimic 4 column table
        // format = "%16s "
        int columnWidthLabels=16;
        int columnWidthData=20;
        // if no Guinier region
        // Izero and Rg = 0;
        ArrayList<String> rows = new ArrayList<>();

        String qminGuinier=centerText("-",columnWidthData,' ');
        String qmaxGuinier=centerText("-",columnWidthData,' ');
        String qminReal=centerText("-",columnWidthData,' ');
        String qmaxReal=centerText("-",columnWidthData,' ');
        String guinierPoints=centerText("-",columnWidthData,' ');
        String realPoints=centerText("-",columnWidthData,' ');
        String reciRg=centerText("-",columnWidthData,' ');
        String realRg=centerText("-",columnWidthData,' ');
        String reciIzero=centerText("-",columnWidthData,' ');
        String realIzero=centerText("-",columnWidthData,' ');
        String reciVolume=centerText("-",columnWidthData,' ');
        String realVolume=centerText("-",columnWidthData,' ');

        String reciVc=centerText("-",columnWidthData,' ');
        String realVc=centerText("-",columnWidthData,' ');
        String porodExponent=centerText("-",columnWidthData,' ');
        String dmax=centerText("-",columnWidthData,' ');
        String binwidth=centerText("-",columnWidthData,' ');
        String shannon=centerText("-",columnWidthData,' ');
        String redundancy=centerText("-", columnWidthData, ' ');
        String r2=centerText("-", columnWidthData, ' ');
        String chi2_free=centerText("-", columnWidthData, ' ');
        String method=centerText("-", columnWidthData, ' ');
        String background=centerText("-", columnWidthData, ' ');


        if (dataset.getGuinierRg() > 0){

            qminGuinier = String.format("%.8f", Math.sqrt(dataset.getGuinierData().getX(dataset.getIndexOfLowerGuinierFit()).doubleValue()));
            qmaxGuinier = String.format("%.8f", Math.sqrt(dataset.getGuinierData().getX(dataset.getIndexOfUpperGuinierFit()).doubleValue()));

            guinierPoints = Integer.toString(dataset.getIndexOfUpperGuinierFit() - dataset.getIndexOfLowerGuinierFit());

            reciRg = String.format("%.2f +- %.2f",dataset.getGuinierRg(), dataset.getGuinierRG_sigma());
            // (%1.1E) is 9 characters
            // %1.1E is 7 characters
            reciIzero = String.format("%1.2E +- %1.1E",dataset.getGuinierIzero(), dataset.getGuinierIzeroSigma());
            r2 = String.format("%.2f", dataset.getGuinierCorrelationCoefficient());
            if (dataset.getPorodVolume() > 0){
                reciVolume = String.format("%d",(int)dataset.getPorodVolume());
                porodExponent = String.format("%.2f +- %.2f", dataset.getPorodExponent(), dataset.getPorodExponentError());
            }

            if (dataset.getVC() > 0){
                reciVc = String.format("%.2f",(int)dataset.getVC());
            }
        }

        if (dataset.getRealIzero() > 0 ) {
            dataset.getRealSpaceModel().estimateErrors();
            qminReal = String.format("%.8f", dataset.getRealSpaceModel().getfittedqIq().getMinX());
            qmaxReal = String.format("%.8f", dataset.getRealSpaceModel().getQmax());
            realPoints = Integer.toString(dataset.getRealSpaceModel().getfittedqIq().getItemCount());

            realRg = String.format("%.2f +- %.2f",dataset.getRealRg(), dataset.getRealRgSigma());
            realIzero = String.format("%1.2E %1.1E",dataset.getRealIzero(), dataset.getRealIzeroSigma());

            chi2_free = String.format("%.2f", dataset.getRealSpaceModel().getChi2());
            if (dataset.getPorodVolumeReal() > 0){
                realVolume = String.format("%d",(int)dataset.getPorodVolumeReal());
            }

            if (dataset.getVCReal() > 0){
                realVc = String.format("%.2f",(int)dataset.getVCReal());
            }

            dmax = String.format("%.1f", dataset.getDmax());
            binwidth = String.format("%.2f", Math.PI/dataset.getRealSpaceModel().getQmax());
            double tempShannon = dataset.getRealSpaceModel().getQmax()*dataset.getDmax()/Math.PI;
            shannon = String.format("%d", (int)Math.ceil(tempShannon));
            redundancy = String.format("%d",(int)Math.ceil(dataset.getRealSpaceModel().getfittedqIq().getItemCount()/tempShannon));
            method = String.format("%s", dataset.getRealSpaceModel().getIndirectFTModel().getModelUsed());

            background=String.format("%s", (dataset.getRealSpaceModel().getIndirectFTModel().includeBackground) ? "Yes" : "No");
        }

        rows.add(String.format("%s %s %s", rightJustifyText("q-min", columnWidthLabels, ' '), centerText(qminGuinier,columnWidthData, ' '), centerText(qminReal,columnWidthData, ' '))); // qmin
        rows.add(String.format("%s %s %s", rightJustifyText("q-max", columnWidthLabels, ' '), centerText(qmaxGuinier,columnWidthData, ' '), centerText(qmaxReal,columnWidthData, ' '))); // qmax
        rows.add(String.format("%s %s %s", rightJustifyText("points(min:max)", columnWidthLabels, ' '), centerText(guinierPoints,columnWidthData, ' '), centerText(realPoints,columnWidthData, ' '))); // npoints

        // 1234567890123456 78910
        //          ( 1.11)
        rows.add(String.format("%s %s %s", rightJustifyText("Rg", columnWidthLabels, ' '), centerText(reciRg,columnWidthData, ' '), centerText(realRg,columnWidthData, ' '))); // rg
        rows.add(String.format("%s %s %s", rightJustifyText("I[zero]", columnWidthLabels, ' '), centerText(reciIzero,columnWidthData, ' '), centerText(realIzero,columnWidthData, ' '))); // izero

        rows.add(String.format("%s %s %s", rightJustifyText("Volume", columnWidthLabels, ' '), centerText(reciVolume,columnWidthData, ' '), centerText(realVolume,columnWidthData, ' '))); // volume
        rows.add(String.format("%s %s %s", rightJustifyText("Vc", columnWidthLabels, ' '), centerText(reciVc,columnWidthData, ' '), centerText(realVc,columnWidthData, ' '))); // Vc


        // score
        rows.add(String.format("%s %s %s", rightJustifyText(" ",16,' '), centerText("R^2",columnWidthData, ' '), centerText("Chi^2_free",columnWidthData, ' '))); // header
        rows.add(String.format("%s %s %s", rightJustifyText("Score",16,' '), centerText(r2,columnWidthData, ' '), centerText(chi2_free, columnWidthData, ' '))); // fitting scores
        rows.add(String.format("%s %s %s", rightJustifyText("Method",16,' '), centerText(" ",columnWidthData, ' '), centerText(method, columnWidthData, ' '))); // fitting scores
        rows.add(String.format("%s %s %s", rightJustifyText("Background",16,' '), centerText(" ",columnWidthData, ' '), centerText(background, columnWidthData, ' '))); // background fitted?

        rows.add(String.format("%s %s ", rightJustifyText("Porod Exponent", columnWidthLabels, ' '), centerText(porodExponent,columnWidthData, ' '))); // porod exponent
        rows.add(String.format("%s %s ", rightJustifyText("d-max", columnWidthLabels, ' '), centerText(dmax,columnWidthData, ' '))); // dmax
        rows.add(String.format("%s %s ", rightJustifyText("bin-width", columnWidthLabels, ' '), centerText(binwidth,columnWidthData, ' '))); // binwidth
        rows.add(String.format("%s %s ", rightJustifyText("Ns", columnWidthLabels, ' '), centerText(shannon,columnWidthData, ' '))); // shannon
        rows.add(String.format("%s %s ", rightJustifyText("redundancy", columnWidthLabels, ' '), centerText(redundancy,columnWidthData, ' '))); // redundancy

        return rows;
    }

    /**
     *
     * @param s string to center
     * @param size width of new string with centred text
     * @param pad
     * @return
     */
    public static String centerText(String s, int size, char pad) {
        if (s == null || size <= s.length())
            return s;

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < (size - s.length()) / 2; i++) {
            sb.append(pad);
        }
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }

    /**
     *
     * @param s string to center
     * @param size width of new string with centred text
     * @param pad
     * @return
     */
    public static String rightJustifyText(String s, int size, char pad) {
        if (s == null)
            System.out.println("NULL detected " + s.length());
        if (s == null || size <= s.length())
            return s;

        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < (size - s.length()); i++) {
            sb.append(pad);
        }

        sb.append(s);
        return sb.toString();
    }
}
