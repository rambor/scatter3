package version3;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Dataset {
    private PropertyChangeSupport propChange = new PropertyChangeSupport(this);
    private ArrayList<Fit> fitList;
    private XYSeries plottedData;   // plotted log10 data
    private XYSeries plottedError;  // plotted log10 data
    private XYSeries plottedKratkyData;   // plotted log10 data

    private final XYSeries originalLog10Data;         // not to be modified
    private final XYSeries originalPositiveOnlyData;  // not to be modified
    private final XYSeries originalPositiveOnlyError; // not to be modified

    private final XYSeries allData;      // not to be modified
    private final XYSeries allDataError; // not to be modified

    private XYSeries normalizedKratkyData;  // derived from allData
    private XYSeries normalizedGuinierData; // derived from originalPositiveOnlyData
    private XYSeries guinierData; // derived from originalPositiveOnlyData
    private XYSeries kratkyData;            // derived from allData
    private XYSeries qIqData;               // derived from allData
    private XYSeries powerLawData;          // derived from originalPositiveOnlyData

    private XYSeries refinedData;
    private XYSeries refinedDataError;

    private XYSeries calcI;
    private String filename;

    private int totalCountInAllData;
    private int start;  // start of the nonNegativeData
    private int end;    // end of the nonNegativeData

    // below are properties of a subtracted dataset
    private double guinierIZero;
    private double guinierIZero_sigma;
    private double guinierRg;
    private double guinierRG_sigma;

    private double rC;
    private double rC_sigma;
    private double dMax;
    private double porodExponent;
    private double porodExponentError;
    private double scaleFactor;
    private double log10ScaleFactor;
    private double averageR;
    private double averageRSigma;
    private int porodDebyeVolume;
    private int porodVolume;
    private int porodDebyeVolumeReal;
    private int porodVolumeReal;
    private double vc;
    private double vcSigma;
    private int massProtein;
    private int massProteinSigma;
    private int massRna;
    private int massRnaSigma;
    private double vcReal;
    private double vcSigmaReal;
    private int massProteinReal;
    private int massProteinSigmaReal;
    private int massRnaReal;
    private int massRnaSigmaReal;
    private double chiPr;
    private double invariantQ;

    private double[] aM; //Moore Coefficients
    private double[] aMSigma; //Moore Coefficients errors
    private double realIZero;
    private double realIZero_sigma;
    private double realRg;
    private double realRg_sigma;

    private double pdqMax;
    private double pqMax;
    private double rqMax;
    private double pdA;
    private double pA;
    private double rA;
    private double maxI;
    private double minI;
    private double maxq;
    private double minq;
    private boolean inUse;
    private boolean fitFile;
    private Color color;
    private boolean baseShapeFilled;
    private int pointSize;
    private BasicStroke stroke;
    private int id;
    private double prScaleFactor;

    private RealSpace realSpace;

    /**
     *
     * @param dat current dataset
     * @param err sigma series
     * @param fileName name of the file
     */
    public Dataset(XYSeries dat, XYSeries err, String fileName, int id){

        totalCountInAllData = dat.getItemCount();

        String tempName = fileName + "-" + id;
        allData = new XYSeries(tempName);
        allDataError = new XYSeries(tempName);

        plottedData = new XYSeries(tempName);  // actual log10 data that is plotted
        plottedError = new XYSeries(tempName); // actual log10 data that is plotted
        plottedKratkyData = new XYSeries(tempName);

        originalPositiveOnlyData = new XYSeries(tempName);
        originalLog10Data = new XYSeries(tempName);
        originalPositiveOnlyError = new XYSeries(tempName);

        normalizedKratkyData = new XYSeries(tempName);  // derived from allData
        normalizedGuinierData = new XYSeries(tempName); // derived from originalPositiveOnlyData
        guinierData = new XYSeries(tempName); // derived from originalPositiveOnlyData
        kratkyData = new XYSeries(tempName);            // derived from allData
        qIqData = new XYSeries(tempName);               // derived from allData
        powerLawData = new XYSeries(tempName);          // derived from originalPositiveOnlyData

        /*
         * make working copies of the data into non-negative, log10 and all
         */
        int logCount=0;
        for(int i=0; i<totalCountInAllData; i++) {
            XYDataItem tempXY = dat.getDataItem(i);
            XYDataItem tempError = err.getDataItem(i);

            allData.add(tempXY);
            allDataError.add(tempError);

            double q = tempXY.getXValue();
            double q2 = q*q;
            //
            if (tempXY.getYValue() > 0) {
                // Positive only Data that is pared down based on sparse
                double logy = Math.log(tempXY.getYValue());
                originalPositiveOnlyData.add(tempXY);
                originalLog10Data.add(tempXY.getX(), Math.log10(tempXY.getYValue()));
                originalPositiveOnlyError.add(tempError);

                plottedData.add(originalLog10Data.getDataItem(logCount));
                plottedError.add(originalPositiveOnlyError.getDataItem(logCount));

                normalizedGuinierData.add(q2, logy);
                guinierData.add(q2, logy);
                powerLawData.add(Math.log(tempXY.getXValue()), logy);
                logCount++;
            }
            //kratky and q*I(q)
            /*
            private XYSeries normalizedKratky;  // derived from allData
            private XYSeries kratky;            // derived from allData
            private XYSeries qIq;               // derived from allData
            */
            normalizedKratkyData.add(q, q2*tempXY.getYValue());
            kratkyData.add(q, q2*tempXY.getYValue());
            qIqData.add(q, q*tempXY.getYValue());
        }


        fitList = new ArrayList<>();

        filename=fileName;
        this.start=0;
        this.end= originalPositiveOnlyData.getItemCount();

        // if possible do preliminary analysis here
        double[] izeroRg = Functions.calculateIzeroRg(this.originalPositiveOnlyData, this.originalPositiveOnlyError);

        if (izeroRg[0] > 0){
            guinierIZero=izeroRg[0];
            guinierIZero_sigma = izeroRg[2];
            guinierRg=izeroRg[1];
            guinierRG_sigma = izeroRg[3];
        }

        scaleFactor=1.000;
        log10ScaleFactor = 0;
        invariantQ=0.0;
        fitFile = false;
        inUse = true;
        maxI = this.plottedData.getMaxY();  //log10 data
        minI = this.plottedData.getMinY();  //log10 data
        maxq = this.plottedData.getMaxX();  //
        minq = this.plottedData.getMinX();  //
        dMax = 0.0d;

        baseShapeFilled = false;
        pointSize = 6;
        this.setStroke(1.0f);
        this.id = id;
    }

    /*
     * copy constructor
     */
    public Dataset (Dataset aDataset){
        fitList = aDataset.fitList;

        plottedData=aDataset.plottedData;
        plottedError=aDataset.plottedError;

        originalPositiveOnlyData = aDataset.originalPositiveOnlyData;
        originalLog10Data = aDataset.originalLog10Data;
        originalPositiveOnlyError = aDataset.originalPositiveOnlyError;

        allData = aDataset.allData;
        allDataError = aDataset.allDataError;
        refinedData = aDataset.refinedData;
        refinedDataError = aDataset.refinedDataError;

        filename=aDataset.filename;
        start=aDataset.start;
        end=aDataset.end;

        guinierIZero=aDataset.guinierIZero;
        guinierRg=aDataset.guinierRg;
        scaleFactor=aDataset.scaleFactor;
        log10ScaleFactor = aDataset.log10ScaleFactor;
        invariantQ=aDataset.invariantQ;

        realSpace = aDataset.realSpace;
    }


    /*
     * Add Fit Object to the list of Fit that belong to
     */
    public void addFitObject(Fit f){
        fitList.add(f);
    }
    /*
     * Retrieve Fit object at specified index
     */
    public Fit getFitObject(int index){
        return fitList.get(index);
    }
    /*
     * Remove all elements from fitList ArrayList
     */
    public void clearFits(){
        fitList.clear();
    }

    public int fitListSize(){
        return fitList.size();
    }

    public XYSeries getCalcI(){
        return calcI;
    }

    /**
     * Returns full path file name
     */
    public void setFileName(String text){
        this.filename = text;
    }

    /**
     * Returns full path file name
     * @return full path of the file
     */
    public String getFileName(){
        return filename;
    }

    /**
     * Returns XYSeries dataset used for plotting with y-axis on log10 scale
     * @return XYSeries of the dataset (plottedData)
     */
    public XYSeries getData(){
        return plottedData;
    }

    /**
     * Returns XYSeries error (q,sigma)
     * @return XYSeries sigma vs q
     */
    public XYSeries getError(){
        return plottedError;
    }


    /**
     * Returns unmodified XYSeries as q^2, ln[I(q)]
     * @return XYSeries
     */
    public XYSeries getGuinierData(){
        return guinierData;
    }


    /**
     * Returns dataItem of log10Data that is scaled
     * @return XYDataItem
     */
    public XYDataItem getScaledLog10DataItemAt(int index){
        XYDataItem temp = originalLog10Data.getDataItem(index);
        Number qvalue = temp.getX();
        return new XYDataItem(qvalue, temp.getYValue() + this.log10ScaleFactor);
    }

    /**
     * Returns unmodified XYSeries dataset (q,log10(I))
     * @return XYSeries parsed from file
     */
    public XYSeries getOriginalLog10Data(){
        return originalLog10Data;
    }

    /**
     * Returns original XYSeries error (q,sigma) of positive only intensity data
     * @return Unmodified XYSeries with sigma vs q
     */
    public XYSeries getOriginalPositiveOnlyError(){
        return originalPositiveOnlyError;
    }

    /**
     * Returns unmodified XYSeries dataset, includes negative intensities, (q, I(q))
     * @return XYSeries parsed from file
     */
    public XYSeries getAllData(){
        return allData;
    }

    /**
     * Returns original XYSeries error (q,sigma)
     * @return Unmodified XYSeries with sigma vs q
     */
    public XYSeries getAllDataError(){
        return allDataError;
    }

    public XYDataItem getKratkyItem(int index){
        return kratkyData.getDataItem(index);
    }

    /**
     * Returns refined XYSeries dataset (q,I(q))
     * @return XYSeries parsed from file
     */
    public XYSeries getRefinedData(){
        return refinedData;
    }

    /**
     * Returns refined XYSeries error (q,sigma)
     * @return Unmodified XYSeries with sigma vs q
     */
    public XYSeries getRefinedDataError(){
        return refinedDataError;
    }

    /**
     * Returns starting point of the series
     * @return Start point
     */
    public int getStart(){
        return start;
    }

    /**
     * Returns end point of the series
     * @return end point
     */
    public int getEnd(){
        return end;
    }

    /**
     *
     * @return guinier Izero of the series
     */
    public double getGuinierIzero(){
        return guinierIZero;
    }

    /**
     *
     * @return standard error of guinier Izero
     */
    public double getGuinierIzeroSigma(){
        return guinierIZero_sigma;
    }

    /**
     *
     * @return guinier Izero of the series calculated from P(r)
     */
    public double getRealIzero(){
        return realIZero;
    }

    /**
     *
     * @return standard error of guinier Izero calculated from P(r)
     */
    public double getRealIzeroSigma(){
        return realIZero_sigma;
    }

    /**
     * @return real Rg of the series calculated from P(r)
     */
    public double getRealRg(){
        return realRg;
    }

    /**
     *
     * @return standard error of real rg calculated from P(r)
     */
    public double getRealRgSigma(){
        return realRg_sigma;
    }

    /**
     *
     * @return Rc
     */
    public double getRc(){
        return rC;
    }

    /**
     *
     * @return Rc sigma
     */
    public double getRcSigma(){
        return rC_sigma;
    }

    /**
     *
     * @return  Dmax
     */
    public double getDmax(){
        return dMax;
    }

    /**
     *
     * @return Porod Exponent
     */
    public double getPorodExponent(){
        return porodExponent;
    }

    /**
     *
     * @return Porod Exponent
     */
    public double getPorodExponentError(){
        return porodExponentError;
    }

    /**
     *
     * @return Scale Factor
     */
    public double getScaleFactor(){
        return scaleFactor;
    }

    /**
     *
     * @return Moore Coefficients ArrayList
     */
    public double[] getAM(){
        return aM;
    }

    /**
     *
     * @return Moore Coefficients Errors as ArrayList
     */
    public double[] getAMSigma(){
        return aMSigma;
    }

    public double getRa(){
        return rA;
    }

    public double getPa(){
        return pA;
    }

    public double getPda(){
        return pdA;
    }

    public double getrqMax(){
        return rqMax;
    }

    public double getpqMax(){
        return pqMax;
    }

    public double getpdqMax(){
        return pdqMax;
    }

    public double getChiPr(){
        return chiPr;
    }

    public int getMassRnaSigmaReal() {
        return massRnaSigmaReal;
    }

    public int getMassProteinReal(){
        return massProteinReal;
    }

    public int getMassProteinSigmaReal(){
        return massProteinSigmaReal;
    }

    public int getMassRnaReal(){
        return massRnaReal;
    }

    public double getAverageR(){
        return averageR;
    }

    public double getAverageRSigma(){
        return averageRSigma;
    }

    public int getPorodDebyeVolume(){
        return porodDebyeVolume;
    }

    public int getPorodVolume(){
        return porodVolume;
    }

    public int getPorodDebyeVolumeReal(){
        return porodDebyeVolumeReal;
    }
    public int getPorodVolumeReal(){
        return porodVolumeReal;
    }
    public double getVC(){
        return vc;
    }
    public double getVCSigma(){
        return vcSigma;
    }
    public int getMassProtein(){
        return massProtein;
    }
    public int getMassProteinSigma(){
        return massProteinSigma;
    }
    public int getMassRna(){
        return massRna;
    }
    public int getMassRnaSigma(){
        return massRnaSigma;
    }
    public double getVCReal(){
        return vcReal;
    }
    public double getVCSigmaReal(){
        return vcSigmaReal;
    }
    public double getGuinierRg(){
        return guinierRg;
    }
    public double getGuinierRG_sigma(){
        return guinierRG_sigma;
    }
    public double getInvariantQ(){
        return invariantQ;
    }


    //***************************************

    /*
     * Sets XYSeries refined dataset (q, I(q))
     */
    public void setCalcI(XYSeries dat){
        calcI=dat;
    }

    /*
     * Sets XYSeries refined dataset (q, I(q))
     */
    public void setRefinedData(XYSeries dat){
        refinedData=dat;
    }

    /*
     * Sets XYSeries error (q,sigma)
     */
    public void setRefinedDataError(XYSeries err){
        refinedDataError=err;
    }

    /**
     * Sets starting point of the series
     *
     */
    public void setStart(int st){
        start=st;
    }
    /**
     * Sets end point of the series
     *
     */
    public void setEnd(int en){
        end=en;
    }
    /**
     * Sets guinier Izero of the series
     *
     */
    public void setGuinierIzero(double gIzero){
        guinierIZero=gIzero;
        this.updateMass();
    }
    /**
     * Sets standard error of guinier Izero
     *
     */
    public void setGuinierIzeroSigma(double gIzeroSigma){
        guinierIZero_sigma=gIzeroSigma;
    }
    /**
     * Sets guinier Rg of the series
     *
     */
    public void setGuinierRg(double rg){
        guinierRg=rg;
        this.updateMass();
    }
    /**
     * Sets guinier Izero of the series calculated from P(r)
     *
     */
    public void setRealIzero(double rIzero){
        realIZero=rIzero;
        this.updateMass();
    }
    /**
     * Sets standard error of guinier Izero calculated from P(r)
     *
     */
    public void setRealIzeroSigma(double rIzeroSigma){
        realIZero_sigma=rIzeroSigma;
    }
    /**
     * Sets real Rg of the series calculated from P(r)
     *
     */
    public void setRealRg(double rRg){
        realRg=rRg;
        this.updateMass();
    }
    /**
     * Sets standard error of real rg calculated from P(r)
     *
     */
    public void setRealRgSigma(double realRgS){
        realRg_sigma=realRgS;
    }
    /**
     * Sets average r calculated from P(r)
     *
     */
    public void setAverageR(double aveR){
        averageR=aveR;
    }
    /**
     * Set Rc
     *
     */
    public void setRc(double rc){
        rC=rc;
    }
    /**
     * Sets Rc sigma
     *
     */
    public void setRcSigma(double rcSigma){
        rC_sigma=rcSigma;
    }
    /**
     * Sets Dmax
     *
     */
    public void setDmax(double dmax){
        dMax=dmax;
    }
    /**
     * Sets Porod Exponent
     *
     */
    public void setPorodExponent(double porodExp){
        porodExponent=porodExp;
    }
    /**
     * Sets Porod Exponent
     *
     */
    public void setPorodExponentError(double porodExpError){
        porodExponentError=porodExpError;
    }


    /**
     * Sets ScaleFactor
     * @param factor
     */
    public void setScaleFactor(double factor){
        System.out.println("Factor " + factor + "  " + Math.log10(factor));
        log10ScaleFactor = Math.log10(factor);
        scaleFactor=factor;

        // rescale plottedData
        this.scalePlottedLog10IntensityData();

    }


    public void clearPlottedKratkyData(){
        plottedKratkyData.clear();
    }


    public XYSeries getPlottedKratkyDataSeries(){
        return plottedKratkyData;
    }

    /**
     * scales Kratky data if visible
     */
    public void scalePlottedKratkyData(){
        plottedKratkyData.clear();
        XYDataItem temp;

        if (scaleFactor != 1){
            for (int i = start; i<end; i++){
                temp = kratkyData.getDataItem(i);
                plottedKratkyData.add(temp.getX(), temp.getYValue()*scaleFactor);
            }
        } else {
            for (int i = start; i<end; i++){
                plottedKratkyData.add(kratkyData.getDataItem(i));
            }
        }
    }


    /**
     *
     */
    private void scalePlottedLog10IntensityData(){
        plottedData.clear();
        XYDataItem temp;

        if (scaleFactor != 1){
            for (int i = start; i<end; i++){
                temp = originalLog10Data.getDataItem(i);
                plottedData.add(temp.getX(), temp.getYValue() + log10ScaleFactor);
            }
        } else {
            for (int i = start; i<end; i++){
                plottedData.add(originalLog10Data.getDataItem(i));
            }
        }
    }


    /**
     * Sets more Coefficients
     * @param aM
     */
    public void setAM(double[] aM){
        this.aM = aM;
    }

    /**
     * Returns Moore Coefficients ArrayList
     *
     */
    public void setAMSigma(double[] aMSigma){
        this.aMSigma = aMSigma;
    }

    public void setRa(double ra){
        rA=ra;
    }

    public void setPa(double pa){
        pA=pa;
    }

    public void setPda(double pda){
        pdA=pda;
    }

    public void setRqMax(double rqmax){
        rqMax=rqmax;
    }

    public void setPqMax(double pqmax){
        pqMax= pqmax;
    }

    public void setPdqMax(double pdqmax){
        pdqMax=pdqmax;
    }

    public void setChiPr(double chipr){
        chiPr=chipr;
    }

    public void setMassRnaSigmaReal(int massRna){
        massRnaSigmaReal=massRna;
    }

    public void setMassProteinReal(int massProtReal){
        massProteinReal=massProtReal;
    }

    public void setMassProteinSigmaReal(int massPSR){
        massProteinSigmaReal=massPSR;
    }

    public void setMassRnaReal(int massRNAR){
        massRnaReal=massRNAR;
    }

    public void setAverageRSigma(double aveRSigma){
        averageRSigma=aveRSigma;
    }

    public void setPorodDebyeVolume(int porodDV){
        porodDebyeVolume=porodDV;
    }

    public void setPorodVolume(int porodV){
        porodVolume=porodV;
    }

    public void setPorodDebyeVolumeReal(int porodDVR){
        porodDebyeVolumeReal=porodDVR;
    }

    public void setPorodVolumeReal(int porodVR){
        porodVolumeReal=porodVR;
    }

    public void updateMass(){
        double qr, mass;

        if (this.guinierIZero > 0 && this.guinierRg > 0 && this.vc > 0){
            qr = Math.pow(this.vc,2)/this.getGuinierRg();
            mass = qr/0.1231;
            this.setMassProtein((int)mass);

            mass = qr/0.00934;
            this.setMassRna((int)Math.pow(mass,0.808));
        }

        if (this.realIZero > 0 && this.realRg > 0 && this.vcReal > 0){

            qr = Math.pow(this.vcReal,2)/this.realRg;
            mass = qr/0.1231;
            this.setMassProteinReal((int) mass);

            mass = qr/0.00934;
            this.setMassRnaReal((int) Math.pow(mass, 0.808));
        }
    }

    public void setVC(double vC){
        vc=vC;
        this.updateMass();
    }

    public void setVCSigma(double vcS){
        vcSigma=vcS;
    }

    public void setMassProtein(int massP){
        massProtein=massP;
    }

    public void setMassProteinSigma(int massProtSigma){
        massProteinSigma=massProtSigma;
    }

    public void setMassRna(int massrna){
        massRna=massrna;
    }

    public void setMassRnaSigma(int massRnaS){
        massRnaSigma=massRnaS;
    }

    public void setVCReal(double vcr){
        vcReal=vcr;
        this.updateMass();
    }

    public void setVCSigmaReal(double vcsReal){
        vcSigmaReal=vcsReal;
    }

    public void setGuinierRG_sigma(double guinierRGS){
        guinierRG_sigma=guinierRGS;
    }

    public void setInvariantQ(double invariant){
        invariantQ=invariant;
    }

    public void setInUse(boolean selected){
        inUse = selected;
    }

    public boolean getInUse(){
        return inUse;
    }

    public void setFitFile(boolean selected){
        fitFile = selected;
    }

    public boolean getFitFile(){
        return fitFile;
    }

    public void setColor(Color randColor){
        color = randColor;
    }

    public Color getColor(){
        return color;
    }

    public double getMaxI(){
        return maxI;
    }

    public double getMinI(){
        return minI;
    }

    public double getMaxq(){
        return maxq;
    }

    public double getMinq(){
        return minq;
    }

    public void setMaxq(double value){
        this.maxq = value;
    }

    public void setMinq(double value){
        this.minq = value;
    }

    public void setPointSize(int size){
        pointSize = size;
    }

    public int getPointSize(){
        return pointSize;
    }

    public void setStroke(float size){
        stroke = new BasicStroke(size);
    }

    public BasicStroke getStroke(){
        return stroke;
    }

    public void setId(int value){
        id = value;
    }

    public int getId(){
        return id;
    }

    public void setBaseShapeFilled(boolean what){
        baseShapeFilled = what;
    }

    public boolean getBaseShapeFilled(){
        return baseShapeFilled;
    }

    public void updateMainDataset(RealSpace dataset){
        this.setAverageR(dataset.getRaverage());
        this.setRealIzero(dataset.getIzero()/dataset.getAnalysisToPrScaleFactor());
        this.setRealRg(dataset.getRg());
        this.setAM(dataset.getMooreCoefficients());
        this.setDmax((double)dataset.getDmax());
        // update Vc
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(listener);
    }

    public void setPrScaleFactor(double value){
        prScaleFactor = value;
    }

    public double getPrScaleFactor(){
        return prScaleFactor;
    }

}