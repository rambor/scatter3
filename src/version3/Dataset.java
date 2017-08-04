package version3;


import org.jfree.data.xy.*;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Dataset {
    private PropertyChangeSupport propChange = new PropertyChangeSupport(this);
    private ArrayList<Fit> fitList;
    private XYSeries plottedData;                // plotted log10 data
    private XYSeries plottedError;               // plotted log10 data error
    private XYSeries plottedKratkyData;          // plotted Kratky data
    private XYSeries plottedqIqData;             // plotted qIq data
    private YIntervalSeries plottedLogErrors;    // plotted log10 errors data
    private XYSeries plottedPowerLaw;            // plotted log10 errors data

    private final XYSeries originalLog10Data;         // not to be modified
    private final XYSeries originalPositiveOnlyData;  // not to be modified
    private final XYSeries originalPositiveOnlyError; // not to be modified
    private final YIntervalSeries positiveOnlyIntensityError; //
    private final XYSeries originalQVectorError;      // not to be modified (SANS)

    private final XYSeries allData;              // not to be modified
    private final XYSeries allDataError;         // not to be modified
    private final YIntervalSeries allDataYError; // not to be modified

    private XYSeries normalizedKratkyReciprocalSpaceRgData;
    private XYSeries normalizedKratkyRealSpaceRgData;
    private XYSeries normalizedKratkyReciprocalSpaceVcData;
    private XYSeries normalizedKratkyRealSpaceVcData;

    private XYSeries normalizedGuinierData; // derived from originalPositiveOnlyData
    private XYSeries guinierData; // derived from originalPositiveOnlyData
    private XYSeries kratkyData;            // derived from allData
    private XYSeries qIqData;               // derived from allData
    private XYSeries powerLawData;          // derived from originalPositiveOnlyData

    private XYSeries refinedData;
    private XYSeries refinedDataError;

    private XYSeries calcI;
    private String filename;
    private String originalFilename;

    private final int totalCountInAllData;
    private final int totalCountInPositiveData;
    private int startAt;  // start of the nonNegativeData
    private int endAt;    // end of the nonNegativeData
    private int indexOfUpperGuinierFit; //belongs to positiveOnlyData

    // below are properties of a subtracted dataset
    private double guinierIZero = 0;
    private double guinierIZero_sigma = 0;
    private double guinierRg = 0;
    private double guinierRG_sigma = 0;

    private double rC = 0;
    private double rC_sigma = 0;
    private double dMax;
    private double porodExponent = 0;
    private double porodExponentError = 0;

    private double scaleFactor;
    private double log10ScaleFactor;

    private double averageR;
    private double averageRSigma;
    private int porodVolume = 0;
    private int porodVolumeReal = 0;

    private int porodMass1p07 = 0;
    private int porodRealMass1p1 = 0;
    private int porodMass1p11 = 0;
    private int porodRealMass1p37 = 0;


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
    private double invariantQ;
    private double porodVolumeQmax;

    private double[] aM; //Moore Coefficients
    private double[] aMSigma; //Moore Coefficients errors
    private double realIZero;
    private double realIZero_sigma;
    private double realRg;
    private double realRg_sigma;

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

    private String experimentalNotes;
    private String bufferComposition;

    private boolean isPDB = false;


    /**
     * use this constructor for samples and buffer in subtraction
     * @param dat
     * @param err
     * @param fileName
     * @param id
     */
    public Dataset(XYSeries dat, XYSeries err, String fileName, int id){

        totalCountInAllData = dat.getItemCount();

        filename=fileName;
        String tempName = fileName + "-" + id;
        scaleFactor=1.000;

        baseShapeFilled = false;
        pointSize = 6;
        this.setStroke(1.0f);
        this.id = id;

        Random r=new Random();
        color = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256));
        inUse = true;

        experimentalNotes ="";
        bufferComposition ="";

        allData = new XYSeries(tempName);
        allDataError = new XYSeries(tempName);
        allDataYError = new YIntervalSeries(tempName);
        plottedData = new XYSeries(tempName);  // actual log10 data that is plotted

        double tempy;
        for(int i=0; i<totalCountInAllData; i++) {
            XYDataItem tempXY = dat.getDataItem(i);
            XYDataItem tempError = err.getDataItem(i);

            allData.add(tempXY);
            allDataError.add(tempError);
            if (tempXY.getYValue() > 0){
             tempy = tempXY.getYValue();
                plottedData.add(tempXY.getX(), Math.log10(tempy));
                //guinierData.add(tempXY.getX().doubleValue()*tempXY.getX().doubleValue(), Math.log(tempy));
            }
        }


        plottedError = new XYSeries(tempName); // actual log10 data that is plotted
        plottedKratkyData = new XYSeries(tempName);
        plottedqIqData = new XYSeries(tempName);
        plottedLogErrors = new YIntervalSeries(tempName);
        plottedPowerLaw = new XYSeries(tempName);

        originalPositiveOnlyData = new XYSeries(tempName);
        originalLog10Data = new XYSeries(tempName);
        originalPositiveOnlyError = new XYSeries(tempName);
        positiveOnlyIntensityError = new YIntervalSeries(tempName);
        originalQVectorError = new XYSeries(tempName);

        normalizedKratkyReciprocalSpaceRgData = new XYSeries(tempName);  // derived from allData
        normalizedKratkyRealSpaceRgData = new XYSeries(tempName);  // derived from allData
        normalizedKratkyReciprocalSpaceVcData = new XYSeries(tempName);  // derived from allData
        normalizedKratkyRealSpaceVcData = new XYSeries(tempName);  // derived from allData

        normalizedGuinierData = new XYSeries(tempName); // derived from originalPositiveOnlyData
        guinierData = new XYSeries(tempName); // derived from originalPositiveOnlyData
        kratkyData = new XYSeries(tempName);            // derived from allData
        qIqData = new XYSeries(tempName);               // derived from allData
        powerLawData = new XYSeries(tempName);          // derived from originalPositiveOnlyData
        this.totalCountInPositiveData = plottedData.getItemCount();
        guinierIZero=0;
        guinierIZero_sigma = 0;
        guinierRg=0;
        guinierRG_sigma = 0;

        maxI = this.plottedData.getMaxY();  //log10 data
        minI = this.plottedData.getMinY();  //log10 data

        maxq = this.plottedData.getMaxX();  //
        minq = this.plottedData.getMinX();  //
    }

    /**
     *
     * @param dat current dataset
     * @param err sigma series
     * @param fileName name of the file
     */
    public Dataset(XYSeries dat, XYSeries err, String fileName, int id, boolean doGuinier){

        totalCountInAllData = dat.getItemCount();

        String tempName = fileName + "-" + id;
        experimentalNotes ="";
        bufferComposition ="";

        allData = new XYSeries(tempName);
        allDataError = new XYSeries(tempName);
        allDataYError = new YIntervalSeries(tempName);

        plottedData = new XYSeries(tempName);  // actual log10 data that is plotted
        plottedError = new XYSeries(tempName); // actual log10 data that is plotted
        plottedKratkyData = new XYSeries(tempName);
        plottedqIqData = new XYSeries(tempName);
        plottedLogErrors = new YIntervalSeries(tempName);
        plottedPowerLaw = new XYSeries(tempName);

        originalPositiveOnlyData = new XYSeries(tempName);
        originalLog10Data = new XYSeries(tempName);
        originalPositiveOnlyError = new XYSeries(tempName);
        positiveOnlyIntensityError = new YIntervalSeries(tempName);
        originalQVectorError = new XYSeries(tempName);

        normalizedKratkyReciprocalSpaceRgData = new XYSeries(tempName);  // derived from allData
        normalizedKratkyRealSpaceRgData = new XYSeries(tempName);  // derived from allData
        normalizedKratkyReciprocalSpaceVcData = new XYSeries(tempName);  // derived from allData
        normalizedKratkyRealSpaceVcData = new XYSeries(tempName);  // derived from allData

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

            double yvalue = tempXY.getYValue();
            allDataYError.add(tempXY.getXValue(), yvalue, yvalue-tempError.getYValue(), yvalue+tempError.getYValue());

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

                double delta = yvalue-tempError.getYValue();
                if (delta > 0){
                    delta = Math.log10(delta);
                } else {
                    delta = 0;
                }
                positiveOnlyIntensityError.add(q, Math.log10(tempXY.getYValue()), delta, Math.log10(yvalue+tempError.getYValue()));

                normalizedGuinierData.add(q2, logy);
                guinierData.add(q2, logy);
                powerLawData.add(Math.log10(q), Math.log10(tempXY.getYValue()));
                logCount++;
            }
            //kratky and q*I(q)
            kratkyData.add(q, q2*tempXY.getYValue());  // should not be modified
            qIqData.add(q, q*tempXY.getYValue());      // should not be modified
        }

        fitList = new ArrayList<>();

        filename=fileName;
        this.startAt=1;  // with respect to positivelyOnlyData
        this.endAt = originalPositiveOnlyData.getItemCount();
        this.totalCountInPositiveData = originalPositiveOnlyData.getItemCount();

        // if possible do preliminary analysis here
        if (doGuinier){
            //double[] izeroRg = Functions.calculateIzeroRg(this.originalPositiveOnlyData, this.originalPositiveOnlyError);
            //double[] izeroRg = Functions.autoRgTransformIt(this.originalPositiveOnlyData, this.originalPositiveOnlyError, this.startAt);
            AutoRg tempRg = new AutoRg(this.originalPositiveOnlyData, this.originalPositiveOnlyError, startAt);
            if (tempRg.getRg() > 0){
                guinierIZero = tempRg.getI_zero();
                guinierIZero_sigma = tempRg.getI_zero_error();
                guinierRg = tempRg.getRg();
                guinierRG_sigma = tempRg.getRg_error();
            }

//            if (izeroRg[0] > 0){
//                guinierIZero=izeroRg[0];
//                guinierIZero_sigma = izeroRg[2];
//                guinierRg=izeroRg[1];
//                guinierRG_sigma = izeroRg[3];
//            }

        } else {
            guinierIZero=0;
            guinierIZero_sigma = 0;
            guinierRg=0;
            guinierRG_sigma = 0;
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

        Random r=new Random();
        color = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256));

        this.realSpace = new RealSpace(this);
    }

    /*
     * copy constructor
     */
    public Dataset (Dataset aDataset){

        totalCountInAllData = aDataset.getAllData().getItemCount();

        String tempName = aDataset.getFileName() + "-" + id;
        allData = aDataset.allData;
        allDataError = aDataset.allDataError;
        allDataYError = aDataset.allDataYError;

        plottedData = aDataset.plottedData;
        plottedError = aDataset.plottedError;
        plottedKratkyData = aDataset.plottedKratkyData;
        plottedqIqData = aDataset.plottedqIqData;
        plottedLogErrors = aDataset.plottedLogErrors;
        plottedPowerLaw = aDataset.plottedPowerLaw;

        originalPositiveOnlyData = aDataset.originalPositiveOnlyData;
        originalLog10Data = aDataset.originalLog10Data;
        originalPositiveOnlyError = aDataset.originalPositiveOnlyError;
        positiveOnlyIntensityError = aDataset.positiveOnlyIntensityError;
        originalQVectorError = aDataset.originalQVectorError;

        normalizedKratkyReciprocalSpaceRgData = aDataset.normalizedKratkyReciprocalSpaceRgData;
        normalizedKratkyRealSpaceRgData = aDataset.normalizedKratkyReciprocalSpaceRgData;
        normalizedKratkyReciprocalSpaceVcData = aDataset.normalizedKratkyReciprocalSpaceVcData;
        normalizedKratkyRealSpaceVcData = aDataset.normalizedKratkyRealSpaceVcData;

        normalizedGuinierData = aDataset.normalizedGuinierData;
        guinierData = aDataset.guinierData;
        kratkyData = aDataset.kratkyData;
        qIqData = aDataset.qIqData;
        powerLawData = aDataset.powerLawData;

        // old
        fitList = aDataset.fitList;


        refinedData = aDataset.refinedData;
        refinedDataError = aDataset.refinedDataError;

        filename=aDataset.filename;
        startAt=aDataset.startAt;
        endAt=aDataset.endAt;

        guinierIZero=aDataset.guinierIZero;
        guinierRg=aDataset.guinierRg;
        scaleFactor=aDataset.scaleFactor;
        log10ScaleFactor = aDataset.log10ScaleFactor;
        invariantQ=aDataset.invariantQ;

        fitFile = aDataset.fitFile;
        inUse = true;
        maxI = aDataset.maxI;  //log10 data
        minI = aDataset.minI;
        maxq = aDataset.maxq;
        minq = aDataset.minq;
        dMax = aDataset.dMax;

        experimentalNotes = aDataset.experimentalNotes;
        bufferComposition = aDataset.bufferComposition;

        baseShapeFilled = false;
        pointSize = 6;
        stroke = aDataset.stroke;
        id = aDataset.id;

        realSpace = aDataset.realSpace;
        totalCountInPositiveData = aDataset.totalCountInPositiveData;
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
     * Returns starting point of the series with respect to Positive Only Data
     * @return Start point
     */
    public int getStart(){
        return startAt;
    }

    /**
     * Returns end point of the series with respect to Positive Only Data
     * @return end point
     */
    public int getEnd(){
        return endAt;
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

    //public int getPorodDebyeVolume(){
    //    return porodDebyeVolume;
    //}

    public int getPorodVolume(){
        return porodVolume;
    }

    //public int getPorodDebyeVolumeReal(){
    //    return porodDebyeVolumeReal;
    //}

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
        this.startAt=st;
    }
    /**
     * Sets end point of the series
     *
     */
    public void setEnd(int en){

        if (en > this.totalCountInPositiveData){
            System.out.println("WARNING EN " + en + " > " + this.totalCountInPositiveData);
        }
        this.endAt=en;
    }
    /**
     * Sets guinier Izero of the series
     *
     */
    public void setGuinierIzero(double gIzero){
        guinierIZero=gIzero;
        this.updateMass();
    }

    public void setGuinierParameters(double izero, double izeroError, double rg, double rgError){
        this.guinierIZero = izero;
        this.guinierIZero_sigma = izeroError;
        this.guinierRg = rg;
        this.guinierRG_sigma = rgError;

        // update normalized Kratky and Guinier plot

        this.updateMass();

        if (invariantQ > 0){
            porodVolume = (int)(Constants.TWO_PI_2*guinierIZero/this.invariantQ);
            this.calculatePorodMass();
        }
    }


    public void setRealIzeroRgParameters(double izero, double izeroError, double rg, double rgError, double rave){
        this.realIZero = izero;
        this.realIZero_sigma = izeroError;
        this.realRg = rg;
        this.realRg_sigma = rgError;
        this.averageR = rave;

        if (invariantQ > 0){
            porodVolumeReal = (int)(Constants.TWO_PI_2*realIZero/this.invariantQ);
            this.calculatePorodMass();
        }
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

        log10ScaleFactor = Math.log10(factor);
        scaleFactor=factor;

        // rescale plottedData
        // this.scalePlottedLog10IntensityData();
    }


    public void clearPlottedKratkyData(){
        plottedKratkyData.clear();
    }


    public XYSeries getPlottedKratkyDataSeries(){
        return plottedKratkyData;
    }


    public XYSeries getPlottedQIQDataSeries(){
        return plottedqIqData;
    }

    // Normalized Kratky Plots

    public void clearNormalizedKratkyReciRgData(){
        normalizedKratkyReciprocalSpaceRgData.clear();
    }

    /**
     * Reciprocal Space Dimensionless Kratky Plot
     * @return XYSeries for normalized Kratky plot
     */
    public XYSeries getNormalizedKratkyReciRgData(){
        return normalizedKratkyReciprocalSpaceRgData;
    }

    public void createNormalizedKratkyReciRgData(){
        XYDataItem temp;
        double rg2 = guinierRg*guinierRg/guinierIZero;
        int startHere = startAt - 1;

        for (int i = startHere; i < endAt; i++){
            temp = kratkyData.getDataItem(i);
            normalizedKratkyReciprocalSpaceRgData.add(temp.getXValue()*guinierRg, temp.getYValue()*rg2);
        }
    }

    /**
     * clears dataset for normalized kratky plot
     */
    public void clearNormalizedKratkyRealRgData(){
        normalizedKratkyRealSpaceRgData.clear();
    }

    /**
     * Real Space Dimensionless Kratky Plot
     * @return XYSeries for normalized Kratky plot
     */
    public XYSeries getNormalizedKratkyRealRgData(){
        return normalizedKratkyRealSpaceRgData;
    }

    public void createNormalizedKratkyRealRgData(){
        XYDataItem temp;
        double rg2 = realRg*realRg/realIZero;
        int startHere = startAt - 1;
        for (int i = startHere; i < endAt; i++){
            temp = kratkyData.getDataItem(i);
            normalizedKratkyRealSpaceRgData.add(temp.getXValue()*realRg, temp.getYValue()*rg2);
        }
    }



    /**
     * scales Kratky data if visible
     */
    public void scalePlottedKratkyData(){
        plottedKratkyData.clear();
        XYDataItem temp;

        int endHere = qIqData.indexOf(plottedData.getX(plottedData.getItemCount()-1));
        int startHere = qIqData.indexOf(plottedData.getX(0));


        if (scaleFactor == 1){
            for (int i = startHere; i<endHere; i++){
                plottedKratkyData.add(kratkyData.getDataItem(i));
            }
        } else {
            for (int i = startHere; i<endHere; i++){
                temp = kratkyData.getDataItem(i);
                plottedKratkyData.add(temp.getX(), temp.getYValue()*scaleFactor);
            }
        }
    }


    public XYSeries getQIQData(){ return qIqData;}
    public XYDataItem getQIQDataItem(int index){ return qIqData.getDataItem(index);}

    public void clearPlottedQIQData(){
        plottedqIqData.clear();
    }


    /**
     * scales data if visible
     */
    public void scalePlottedQIQData(){
        plottedqIqData.clear();
        XYDataItem temp;

        int endHere = qIqData.indexOf(plottedData.getX(plottedData.getItemCount()-1));
        int startHere = qIqData.indexOf(plottedData.getX(0));

        if (scaleFactor != 1){
            for (int i = startHere; i<endHere; i++){
                temp = qIqData.getDataItem(i);
                plottedqIqData.add(temp.getX(), temp.getYValue()*scaleFactor);
            }
        } else {
            for (int i = startHere; i<endHere; i++){
                plottedqIqData.add(qIqData.getDataItem(i));
            }
        }
    }

    public XYSeries getPlottedPowerLaw(){
        return plottedPowerLaw;
    }

    public void clearPlottedPowerLaw(){
        plottedPowerLaw.clear();
    }

    /**
     * rescales the data using natural logarithm
     */
    public void scalePlottedPowerLaw(){
        this.clearPlottedPowerLaw();
        XYDataItem temp;

        int endHere = powerLawData.indexOf(Math.log10(plottedData.getX(plottedData.getItemCount()-1).doubleValue()));
        int startHere = powerLawData.indexOf(Math.log10(plottedData.getX(0).doubleValue()));

        if (scaleFactor != 1){
            for (int i = startHere; i < endHere; i++){
                temp = powerLawData.getDataItem(i);
                plottedPowerLaw.add(temp.getX(), temp.getYValue() + log10ScaleFactor);
            }
        } else {
            for (int i = startHere; i < endHere; i++){
                plottedPowerLaw.add(powerLawData.getDataItem(i));
            }
        }

    }


    public XYSeries getOriginalPositiveOnlyData(){
        return originalPositiveOnlyData;
    }

    public XYDataItem getOriginalPositiveOnlyDataItem(int index){
        return originalPositiveOnlyData.getDataItem(index);
    }

    public YIntervalSeries getPlottedLog10ErrorData(){
        return plottedLogErrors;
    }

    public void clearPlottedLog10ErrorData(){
        plottedLogErrors.clear();
    }

    /**
     * scales Kratky data if visible
     */
    public void scalePlottedLogErrorData(){
        this.clearPlottedLog10ErrorData();
        YIntervalDataItem temp;

        int startHere = startAt - 1;

        if (scaleFactor != 1){
            for (int i = startHere; i < endAt; i++){
                temp = (YIntervalDataItem) positiveOnlyIntensityError.getDataItem(i);
                plottedLogErrors.add(temp.getX(), temp.getYValue()+log10ScaleFactor, temp.getYLowValue()+log10ScaleFactor, temp.getYHighValue()+log10ScaleFactor);
            }
        } else {
            for (int i = startHere; i < endAt; i++){
                plottedLogErrors.add((YIntervalDataItem) positiveOnlyIntensityError.getDataItem(i), false );
            }
        }
    }

    /**
     *
     */
    public synchronized void scalePlottedLog10IntensityData(){

        //plottedData.clear();
        //XYDataItem temp;
        int endOf = plottedData.getItemCount();
        int startHere = this.startAt - 1;

        if (scaleFactor != 1){
            for (int i = 0; i< endOf; i++){
                //temp = originalLog10Data.getDataItem(i);
                //plottedData.add(temp.getX(), temp.getYValue() + log10ScaleFactor);
                plottedData.updateByIndex(i, originalLog10Data.getY(startHere).doubleValue() + log10ScaleFactor);
                startHere++;
            }
        } else {

            for (int i = 0; i< endOf; i++){
                //temp = originalLog10Data.getDataItem(i);
                //plottedData.add(temp.getX(), temp.getYValue() + log10ScaleFactor);
                plottedData.updateByIndex(i, originalLog10Data.getY(startHere));
                startHere++;
            }
        }
    }


    public void setPlottedDataNotify(boolean value){
        plottedData.setNotify(value);
    }

public void lowBoundPlottedLog10IntensityData(int newStart){

            plottedData.setNotify(false);

            if (newStart < startAt){ // addValues
                int startHere = this.startAt - 1; // current location in originalLog10Data
                int limit = startAt-newStart;


                for(int i=0; i<limit && startHere > -1; i++){
                    startHere--;
                    XYDataItem temp = originalLog10Data.getDataItem(startHere);
                    plottedData.addOrUpdate(temp.getX(), temp.getYValue() + log10ScaleFactor);
                }

            } else if (newStart > startAt){ //remove values

                int limit = newStart - this.startAt;
                    for(int i=0; i < limit; i++){
                        plottedData.remove(0);
                    }
                    //plottedData.delete(0, (limit-1));
            }
//            plottedData.setNotify(true);
            this.startAt = newStart;
}

    public void upperBoundPlottedLog10IntensityData(int newEnd){

        plottedData.setNotify(false);
        if (newEnd > endAt){ // addValues
            int startHere = this.endAt; // upper bound in originalLog10 exclusive
            int limit = newEnd - this.endAt;
            int upper = originalLog10Data.getItemCount();

            for(int i=0; i<limit && startHere < upper; i++){
                XYDataItem temp = originalLog10Data.getDataItem(startHere);
                plottedData.addOrUpdate(temp.getX(), temp.getYValue() + log10ScaleFactor);
                startHere++;
            }

        } else if (newEnd < endAt){ //remove values

            int limit = this.endAt - newEnd;
            int lastValue = plottedData.getItemCount();

            for(int i=0; i<limit; i++){ // remove the last point
                lastValue--;
                plottedData.remove(lastValue);
            }
        }
        this.endAt = newEnd;
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

    public void setPorodVolume(int porodV){
        porodVolume=porodV;
        this.calculatePorodMass();
    }


    public void calculatePorodMass(){
        porodMass1p07 = (int)(porodVolume*1.07/1.66);
        porodMass1p11 = (int)(porodVolume*1.1/1.66);

        if (porodVolumeReal > 0){
            porodRealMass1p1 = (int)(porodVolumeReal*1.07/1.66);
            porodRealMass1p37 = (int)(porodVolumeReal*1.1/1.66);
        }
    }


    public void setPorodVolumeReal(int porodVR){
        porodVolumeReal=porodVR;
        this.calculatePorodMass();
    }

    public int getPorodVolumeRealMass1p1(){return porodRealMass1p1;}
    public int getPorodVolumeRealMass1p37(){return porodRealMass1p37;}

    public int getPorodVolumeMass1p1(){return porodMass1p07;}
    public int getPorodVolumeMass1p37(){return porodMass1p11;}

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

    public void setExperimentalNotes(String text){
        this.experimentalNotes = text;
    }

    public void appendExperimentalNotes(String text) {
        this.experimentalNotes = this.experimentalNotes + "\n"  + text;
    }

    public String getExperimentalNotes(){
        return this.experimentalNotes;
    }

    public void setBufferComposition(String text){
        this.bufferComposition = text;
    }

    public String getBufferComposition(){
        return this.bufferComposition;
    }

    public RealSpace getRealSpaceModel(){
        return realSpace;
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

    public void setIndexOfUpperGuinierFit(int index){
        this.indexOfUpperGuinierFit = index;
    }

    public int getIndexOfUpperGuinierFit(){
        return this.indexOfUpperGuinierFit;
    }

    public void updateRealSpaceErrors(double percentRgError, double percentIzeroError){
        this.realRg_sigma = percentRgError*this.realRg;
        this.realIZero_sigma = percentIzeroError*this.realIZero;
    }

    public void setAverageInfo(Collection collection){

        int totalCollection = collection.getDatasetCount();
        this.experimentalNotes += "AVERAGED FROM\n";
        int count=0;
        for (int i=0; i < totalCollection; i++){
            if (collection.getDataset(i).getInUse()){
                count++;
                this.experimentalNotes += String.format("\tFILE %4d : %s%n", count, collection.getDataset(i).getFileName());
            }
        }
    }

    public void setMedianInfo(Collection collection){

        int totalCollection = collection.getDatasetCount();
        this.experimentalNotes += "MEDIAN DERIVED FROM\n";
        int count=0;
        for (int i=0; i < totalCollection; i++){
            if (collection.getDataset(i).getInUse()){
                count++;
                this.experimentalNotes += String.format("\tFILE %4d : %s%n", count, collection.getDataset(i).getFileName());
            }
        }
    }

    public void copyAndRenameDataset(String newName, String cwd){
        String base = newName.replaceAll("\\W","_");
        this.appendExperimentalNotes("ORIGINAL FILE : " + this.filename);
        this.originalFilename = this.filename;
        this.setFileName(base);
        FileObject dataToWrite = new FileObject(new File(cwd));
        dataToWrite.writeSAXSFile(base, this);
    }

    public void setIsPDB(XYSeries pofrDistribution, float dmax, double rg, double izero){
        this.realSpace.setPrDistribution(pofrDistribution);
        this.realSpace.setDmax(dmax);
        this.isPDB = true;
        this.realSpace.setRg(rg);
        this.realSpace.setIzero(izero);
    }

    public String getOriginalFilename(){return this.originalFilename;}

    public boolean getIsPDB(){
        return this.isPDB;
    }

    public double getPorodVolumeQmax() {
        return porodVolumeQmax;
    }

    public void setPorodVolumeQmax(double porodVolumeQmax) {
        this.porodVolumeQmax = porodVolumeQmax;
    }
}