package version3;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import org.jfree.data.xy.XYSeries;
import version3.ceengine.*;
import version3.ceengine.Utils.Gaussian;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by robertrambo on 03/02/2016.
 */
public class FormFactorCE extends SwingWorker<Void, Void> {
    /**
     * The Backgound level.
     */
    public static final float BKG = 0;

    /**
     * The Scale factor imposed.
     */
    public static final float SCALE = 1;

    /**
     * The scattering length density of the slovent.
     */
    public static float SLD_SOLVENT = 2e-6f;                              //MIGHT NEED SCALING IF CONTRAST TOO SMALL???
    private static float contrast;

    private float dmax;

    private XYSeries originalData;
    private XYSeries originalError;
    private XYSeries transformedOriginalData;  // based on the model, transform the data
    private XYSeries transformedOriginalError;
    private ModelType type;
    private ArrayList<ParameterSet> paramVector;
    private int transformPower;
    private JProgressBar bar;

    public FormFactorCE(XYSeries data, XYSeries error, JProgressBar bar){
        originalData = data;
        originalError = error;
        this.bar = bar;
    }

    /**
     * transform data by q^6 for fitting
     * set search range
     */
    public void setSphericalParameters(float lowerq, float upperq, float lowerRadius, float upperRadius, float sample, float solvent, JPanel dataFitPanel, JPanel sphereDistributionPanel){

        type = ModelType.SPHERICAL;
        transformPower = 6;
        transformedOriginalData = CrossEntropyEngine.transformData(originalData, transformPower, lowerq, upperq);
        transformedOriginalError = CrossEntropyEngine.transformData(originalError, transformPower, lowerq, upperq);
        dmax = upperRadius*2;

        float deltar = (float)((upperRadius-lowerRadius)/60.0);
        System.out.println("Delta r " + deltar);
        if (deltar < 0.5){
            deltar = 0.5f;
            upperRadius = lowerRadius + 60*deltar;
        }

        float[] lowParams, highParams,spacings;
        lowParams = new float[]{lowerRadius};
        highParams = new float[]{upperRadius};
        spacings = new float[]{deltar};
        contrast = sample - solvent;

        fillParamVector(lowParams, highParams, spacings, false, true);
    }


    /**
     * transform data by q^6 for fitting
     * set search range
     */
    public void setEllipsoidParameters(float lowerq, float upperq, float lowerLimit, float majorAxis, float sample, float solvent, JPanel dataFitPanel, JPanel sphereDistributionPanel){

        type = ModelType.ELLIPSOID;
        transformPower = 4;
        transformedOriginalData = CrossEntropyEngine.transformData(originalData, transformPower, lowerq, upperq);
        transformedOriginalError = CrossEntropyEngine.transformData(originalError, transformPower, lowerq, upperq);

        float deltar = (float)((majorAxis-lowerLimit)/60.0);

        if (deltar < 0.5){
            deltar = 0.5f;
            majorAxis = lowerLimit + 60*deltar;
        }

        dmax = majorAxis*2;

        float[] lowParams, highParams,spacings;
        lowParams = new float[]{lowerLimit,lowerLimit};
        highParams = new float[]{majorAxis,majorAxis};                  //Put 'major' first     'MAJOR' MUST BE BIGGER THAN 'MINOR'!!!!!!!!!
        spacings = new float[]{deltar,deltar};
        contrast = sample - solvent;

        fillParamVector(lowParams, highParams, spacings, false, true);
    }



    public void fillParamVector(float[] low, float[] high, float[] delta, boolean coreGreater, boolean coreIsSolvAswell) {
        paramVector = new ArrayList<>();

        switch (type){
            case SPHERICAL:
                float current = low[0];
                while(current < high[0]){
                    paramVector.add(new ParameterSet(new float[]{current}));
                    current += delta[0];
                }
                System.out.println("FINISHED SETTING PARAMETERS FOR SPHERE");
                break;
            case CORESHELL:
                float currentCore = low[0];
                while(currentCore < high[0]){
                    float currentThickness = low[1];
                    while(currentThickness < high[1]){
                        if(coreGreater){                               //If the core SLD is greater than the shell SLD
                            float currentContrastCore = low[2];
                            while(currentContrastCore < high[2]){
                                float currentContrastShell = low[3];
                                while(currentContrastShell < high[3] && currentContrastShell < currentContrastCore){
                                    if(!(currentContrastCore == currentContrastShell && currentContrastShell == SLD_SOLVENT)){
                                        paramVector.add(new ParameterSet(new float[]{currentCore, currentThickness, currentContrastCore, currentContrastShell}));
                                    }
                                    currentContrastShell += delta[3];
                                }
                                currentContrastCore += delta[2];
                            }
                        }else{                                          //IF the shell SLD is greater than the core SLD
                            float currentContrastCore = low[2];
                            while(currentContrastCore < high[2]){
                                float currentContrastShell = low[3];
                                while(currentContrastShell < high[3]){
                                    if(!(currentContrastCore == currentContrastShell && currentContrastShell == SLD_SOLVENT) && currentContrastShell > currentContrastCore){
                                        paramVector.add(new ParameterSet(new float[]{currentCore, currentThickness, currentContrastCore, currentContrastShell}));
                                    }
                                    currentContrastShell += delta[3];
                                }
                                currentContrastCore += delta[2];
                            }

                        }
                        currentThickness += delta[1];
                    }
                    currentCore += delta[0];
                }

                break;
            case CORESHELLBINARY:
                currentCore = low[0];
                while(currentCore < high[0]){
                    float currentThickness = low[1];
                    while(currentThickness < high[1]){
                        float currentContrastCore = low[2];
                        float currentContrastShell = low[3];
                        if(coreIsSolvAswell){
                            if(!(currentContrastCore == currentContrastShell && currentContrastShell == SLD_SOLVENT)){
                                paramVector.add(new ParameterSet(new float[]{currentCore, currentThickness, currentContrastCore, currentContrastShell}));
                            }
                            currentContrastCore = SLD_SOLVENT;
                            if(!(currentContrastCore == currentContrastShell && currentContrastShell == SLD_SOLVENT)){
                                paramVector.add(new ParameterSet(new float[]{currentCore, currentThickness, currentContrastCore, currentContrastShell}));
                            }
                        }else{
                            if(!(currentContrastCore == currentContrastShell && currentContrastShell == SLD_SOLVENT)){
                                paramVector.add(new ParameterSet(new float[]{currentCore, currentThickness, currentContrastCore, currentContrastShell}));
                            }
                        }
                        currentThickness += delta[1];
                    }
                    currentCore += delta[0];
                }
                break;
            case ELLIPSOID:
                float currentMajor = low[0];
                while(currentMajor < high[0]){
                    float currentMinor = low[1];
                    while(currentMinor < high[1] && currentMinor < currentMajor){
                        paramVector.add(new ParameterSet(new float[]{currentMajor, currentMinor}));
                        currentMinor += delta[1];
                    }
                    currentMajor += delta[0];
                }
                break;
            case CYLINDRICAL:
                float currentL = low[0];
                while(currentL < high[0]){
                    float currentr = low[1];
                    while(currentr < high[1]){
                        paramVector.add(new ParameterSet(new float[]{currentL, currentr}));
                        currentr += delta[1];
                    }
                    currentL += delta[0];
                }
                break;
        }

        if(paramVector.size() == 0){
            System.out.println("NO PARAMS!!!!!!!");
            System.exit(-1);
        }

        /*Iterator<ParameterSet> i = vector.iterator();
        while(i.hasNext()){
            System.out.println(Arrays.toString(i.next().getParams()));
        }*/
    }



    /**
     * Takes all the input data and runs the program.
     *
     * @param curves      The collection of predicted curves
     * @param graphics    Set true for graphical output
     * @param multi       Set true for a multithreaded implementation
     * @param smoothness  Set true for smoothness to be used (ONLY WORKS FOR ONE DIMENSION)
     * @param tType       The chosen target function
     * @param scale       The scale factor to be used in the engine
     */
    public void doStuff(PredictedIntensityCurves curves, boolean graphics, boolean multi, boolean smoothness, TargetType tType, float scale){
        CrossEntropyEngine e = new CrossEntropyEngine(multi, smoothness, scale);
        e.setDmax(dmax);
        e.setData(transformedOriginalData, transformedOriginalError);
        e.setParams(paramVector);
        e.setCurves(curves);

        float[] answer = e.solve(false, graphics, tType);

        //pickRandomAndOutput(paramVector, data.toArray()[0], answer, curves);
        float[] nDist = CrossEntropyEngine.normalise(answer);

        for (int i = 0; i < nDist.length; i++) {
            String s = "(" + paramVector.get(i).getParam(0);
            for(int j = 1; j < paramVector.get(i).getParamNumbers(); j++){
                s += ", " + paramVector.get(i).getParam(j);
            }
            s+= ")";
            System.out.print(s + ": ");
            System.out.println(nDist[i]);
        }


        if(curves.getType() == ModelType.CORESHELL){
            System.out.println();

            //Sums and prints the distribution for only the two radii
            for(int i = 0; i < nDist.length; i++){
                float temp = 0;
                float thing = paramVector.get(i).getParam(1);
                while(i < answer.length && paramVector.get(i).getParam(1) == thing){
                    temp += nDist[i++];
                }
                String s = "("+paramVector.get(i-1).getParam(0)+", "+paramVector.get(i-1).getParam(1)+")";
                System.out.print(s+": ");
                System.out.println(temp);
            }

            System.out.println();
            //Calculates weighted average of ratio (deltaRhoCS/deltaRhoSS)
            float sum = 0;
            for(int i = 0; i < nDist.length; i++){                         //NO NEED FOR NORMALISATION AS 'nDist[]' IS PDF!
                float bottom = (paramVector.get(i).getParam(3) - SLD_SOLVENT);
                if(bottom != 0 && nDist[i] != 0){
                    float ratio = (paramVector.get(i).getParam(2)-paramVector.get(i).getParam(3))/bottom;
                    sum += nDist[i]*ratio;
                }
            }
            System.out.println("deltaRhoCS/deltaRhoSS = " + sum);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        long start = System.currentTimeMillis();

        //Setting options
        boolean multiThreading = true, graphics = true, smoothness = true;             //SMOOTHNESS DOESN'T WORK WITH 2+ PARAMS!!!

        boolean coreIsSolvAswell = true;                                                    //only for the Core-shell Binary model
        float SLD_Core = 3e-6f;
        float SLD_Shell = 1e-6f;

        boolean coreSLDGreater = false;                                                      //only for Core-Shell model

        TargetType tType = TargetType.ABSVAL;

        float SLD_Sample = 1e-6f;                                      //Not used in Core-shell model

        float scaleFactor = 1e12f;

        //Perform validity checks
        if(smoothness && type !=ModelType.SPHERICAL){
            System.out.println("SMOOTHNESS DOESNT WORK FOR MULTIPARAMETERS!!! TURNING IT OFF...");
            smoothness = false;
        }
        if(transformPower != 4 && type == ModelType.CYLINDRICAL){
            System.out.println("Cylindrical curves are timesed by q^4.");
            //transformPower = 4;
        }
        if(transformPower == 4 && type != ModelType.CYLINDRICAL){
            System.out.println("Only Cylindrical curves are timesed by q^4.");
            //transformPower = 6;
        }

        float[] lowParams, highParams,spacings;
        switch(type){
            default:

            case CORESHELL:
                lowParams = new float[]{10,5,5e-7f,5e-7f};
                highParams = new float[]{100,20,5e-6f,5e-6f};         //Put 'core' first, then 'shell' then 'SLD_core' then 'SLD_shell'.
                spacings = new float[]{5,5,5e-7f,5e-7f};
                contrast = SLD_SOLVENT;
                break;
            case CORESHELLBINARY:
                lowParams = new float[]{10,5,SLD_Core,SLD_Shell};
                highParams = new float[]{200,20,SLD_Core+0.5f,SLD_Shell+0.5f};         //Put 'core' first, then 'shell' then 'SLD_core' then 'SLD_shell'.
                spacings = new float[]{5,5,1,1};
                contrast = SLD_SOLVENT;
                break;
            case CYLINDRICAL:
                lowParams = new float[]{50,5};
                highParams = new float[]{150,40};                  //Put 'length' first, TRANSFORMS DATA AS q^4!!!!!!!!!!!!!
                spacings = new float[]{5,5};
                contrast = SLD_Sample - SLD_SOLVENT;
                break;
        }

        //Imports or generates data
        //String fileName;
        //fileName = "ave_dsDNA_C_refined_sx.txt";             //DNA
        //fileName = "cylidner_rob.txt";                       //JAMES'S THING
        //fileName = "average_GI.txt";                         //SPHEREish THING
        //fileName = "Xy_A3_merged_refined_sx.txt";            //other thing

        //fillParamVector(lowParams, highParams, spacings, coreSLDGreater, coreIsSolvAswell);

        PredictedIntensityCurves curves = fillCurves();

        doStuff(curves, graphics, multiThreading, smoothness, tType, scaleFactor);

        //Prints the time taken for the program
        System.out.println(System.currentTimeMillis()-start + "ms");

        return null;
    }


    /**
     * Uses the parameter vector to create the curves.
     *
     * @return            The collection of curves
     */
    public PredictedIntensityCurves fillCurves() {
        PredictedIntensityCurves curves = CurveFactory.create(type, SCALE);
        curves.setContrast(contrast);                                //For Core-shell, this is just the SLD of the solvent
        curves.setThings(paramVector, transformedOriginalData.toArray()[0]);
        return curves;
    }


    //Data generation - XYSeries of q, I
    public static float[][] getValuesAndWeights(){
        return valuesAndWeights;
    }

    private static float[][] valuesAndWeights;
    static float sigma = 7;

    //Creates a spherical test data set with the specified number of peak spaced accordingly
    private static XYSeries generateSphericalDistributionTestData(int noPeaks, int spacing){
        float start = 100;
        int dataLength = 300;
        XYSeries data = new XYSeries("Generated Data");

        Gaussian[] g = new Gaussian[noPeaks];
        float[] heights = new float[noPeaks];
        float totalHeight = 0;
        for(int i=0; i<noPeaks; i++){
            g[i] = new Gaussian(start + i*spacing, sigma);
            heights[i] = 1;//(i+1)*0.5f;
            totalHeight += heights[i];
        }

        //Samples over 4 standard deviations for both peaks and divides to normalise the distribution!

        valuesAndWeights = new float[2][(int)(noPeaks*spacing + 8*sigma + 1)];
        int counter = 0;
        float sum = 0;
        for (int i = (int)(start - 4*sigma); i <= (int)(start + noPeaks*spacing + 4*sigma); i++ ) {
            valuesAndWeights[0][counter] = i;
            float thing = 0;
            for(int j=0; j<noPeaks; j++){
                thing += heights[j]*g[j].evaluate(i);
            }
            valuesAndWeights[1][counter++] = thing/totalHeight;
            sum+= thing/totalHeight;
        }

        System.out.println(Arrays.toString(valuesAndWeights[0]));
        System.out.println(Arrays.toString(valuesAndWeights[1]));

        double q = 0.01;
        double[] qValues = new double[dataLength];
        for(int i = 0; i < 100; i++) {
            qValues[i] = q;
            q += 0.00002915;
        }
        for(int i = 100; i < dataLength; i++) {
            qValues[i] = q;
            q += 0.0007;
        }

        PredictedIntensityCurves[] curves = new PredictedIntensityCurves[valuesAndWeights[0].length];
        for (int i = 0; i <curves.length; i++) {
            curves[i] = CurveFactory.create(ModelType.SPHERICAL, SCALE);

            ArrayList<ParameterSet> p = new ArrayList<>();
            p.add(new ParameterSet( new float[] {valuesAndWeights[0][i]}));
            curves[i].setContrast(contrast);
            curves[i].setThings(p ,qValues);
        }

        for (int i = 0; i < dataLength; i++) {
            double p = 0;
            for (int j = 0; j < curves.length; j++) {
                p += valuesAndWeights[1][j]*curves[j].getValue(0,i);
            }
            data.add(qValues[i],1e12f*p/sum);
        }
        return data;
    }

    //Creates a core-shell test dataset with the specified parameters
    private static XYSeries generateCoreShellDistributionTestData(float[] r, float[] t, float[] cCS, float[] cSS){
        int dataLength = 300;
        valuesAndWeights = new float[2][2];
        valuesAndWeights[0][0] = 1;
        XYSeries data = new XYSeries("Generated Data");
        double q = 0.01;
        double[] qValues = new double[dataLength];
/*        for(int i = 0; i < 100; i++) {
            qValues[i] = q;
            q += 0.00002915;
        }
        for(int i = 100; i < dataLength; i++) {*/
        for(int i = 0; i < dataLength; i++) {
            qValues[i] = q;
            q += 0.0007;
        }


        PredictedIntensityCurves[] curves = new PredictedIntensityCurves[r.length];
        for (int i = 0; i <curves.length; i++) {
            curves[i] = CurveFactory.create(ModelType.CORESHELL, SCALE);

            ArrayList<ParameterSet> p = new ArrayList<>();
            p.add(new ParameterSet(new float[]{r[i], t[i], cCS[i], cSS[i]}));
            curves[i].setContrast(contrast);
            curves[i].setThings(p ,qValues);
        }

        for (int i = 0; i < dataLength; i++) {
            double p = 0;
            for (int j = 0; j < curves.length; j++) {
                p += curves[j].getValue(0,i);
            }
            data.add(qValues[i],p/curves.length);
        }
        return data;
    }

    //Creates a ellipsoid test dataset with the specified parameters
    private static XYSeries generateEllipsoidDistributionTestData(float[] a, float[] b){
        int dataLength = 300;
        valuesAndWeights = new float[1][1];
        valuesAndWeights[0][0] = 1;
        XYSeries data = new XYSeries("Generated Data");
        PredictedIntensityCurves curve = new EllipsoidCurves(SCALE);
        double q = 0.01;
        double[] qValues = new double[dataLength];
/*        for(int i = 0; i < 100; i++) {
            qValues[i] = q;
            q += 0.00002915;
        }
        for(int i = 100; i < dataLength; i++) {*/
        for(int i = 0; i < dataLength; i++) {
            qValues[i] = q;
            q += 0.0007;
        }

        PredictedIntensityCurves[] curves = new PredictedIntensityCurves[a.length];
        for (int i = 0; i <curves.length; i++){
            curves[i] = CurveFactory.create(ModelType.ELLIPSOID, SCALE);
            ArrayList<ParameterSet> p = new ArrayList<>();
            p.add(new ParameterSet(new float[]{a[i], b[i]}));
            curves[i].setContrast(contrast);
            curves[i].setThings(p ,qValues);
        }

        for (int i = 0; i < dataLength; i++) {
            double p = 0;
            for (int j = 0; j < curves.length; j++) {
                p += curves[j].getValue(0,i);
            }
            data.add(qValues[i],p/curves.length);
        }
        return data;
    }

    //Creates a cylindrical test dataset with the specified parameters
    private static XYSeries generateCylindricalDistributionTestData(float[] L, float[] r){
        int dataLength = 300;
        valuesAndWeights = new float[1][1];
        valuesAndWeights[0][0] = 1;
        XYSeries data = new XYSeries("Generated Data");
        double q = 0.01;
        double[] qValues = new double[dataLength];
       /* for(int i = 0; i < 100; i++) {
            qValues[i] = q;
            q += 0.00002915;
        }
        for(int i = 100; i < dataLength; i++) {*/
        for(int i = 0; i < dataLength; i++) {
            qValues[i] = q;
            q += 0.0007;
        }

        PredictedIntensityCurves[] curves = new PredictedIntensityCurves[L.length];
        for (int i = 0; i <curves.length; i++){
            curves[i] = CurveFactory.create(ModelType.CYLINDRICAL, SCALE);
            ArrayList<ParameterSet> p = new ArrayList<>();
            p.add(new ParameterSet(new float[]{L[i], r[i]}));
            curves[i].setContrast(contrast);
            curves[i].setThings(p ,qValues);
        }

        for (int i = 0; i < dataLength; i++) {
            double p = 0;
            for (int j = 0; j < curves.length; j++) {
                p += curves[j].getValue(0,i);
            }
            data.add(qValues[i],1e12f*p/curves.length);
        }

        return data;
    }

}
