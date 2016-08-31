package version3;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

/**
 * Created by robertrambo on 30/08/2016.
 */
public class IndependentComponentAnalysis extends SwingWorker {

    private double qmin;
    private double qmax;
    private XYSeriesCollection datasets;
    private int totalSets;
    private int numberOfComponents;
    private int initialNumberOfColumns;
    private XYSeriesCollection meanCenteredSets;
    private XYSeriesCollection SAXSICA;
    private XYSeriesCollection reverseEigenValueSet;
    private int startIndexOfFrame;

    private DenseMatrix64F matrixA;
    private DenseMatrix64F U_matrix;
    private DenseMatrix64F S_matrix;
    private double[] s_values;
    private DenseMatrix64F V_matrix;
    private boolean useReduced = false;

    private int rows;


    public IndependentComponentAnalysis(double qmin, double qmax, XYSeriesCollection datasets, int startIndexOfFrame, int numberOfEigenValuesToPlot, JProgressBar bar){
        this.qmin = qmin;
        this.qmax = qmax;
        this.datasets = datasets;
        totalSets = datasets.getSeriesCount();
        this.startIndexOfFrame = startIndexOfFrame;

        // initialize eigenvalue series to plot
        this.numberOfComponents = numberOfEigenValuesToPlot;

        if (numberOfComponents < totalSets){
            useReduced = true;
        }



        // if total number of frames > numberOfComponents (k), must perform dimension reduction

        // divide set into k-bins
        // sample 1 from each bin
        // perform ICA, and average the results

        SAXSICA= new XYSeriesCollection();
    }

    @Override
    protected Object doInBackground() throws Exception {
        return null;
    }


    /**
     *
     */
    private void createMeanSubtractedDatasets(){

        meanCenteredSets = new XYSeriesCollection();
        XYDataItem tempItem;

        for(int i=0; i<totalSets; i++){

            double sum=0;
            double count=0;

            XYSeries tempSeries = datasets.getSeries(i);
            int totalInSeries = tempSeries.getItemCount();

            for(int j=0; j<totalInSeries; j++){
                tempItem = tempSeries.getDataItem(j);
                if (tempItem.getXValue() >= qmin && tempItem.getXValue() <= qmax){
                    sum += tempItem.getYValue();
                    count += 1;
                }
            }

            double invCount = 1.0/(double)count;
            double mean = sum*invCount;
            // System.out.println("Mean Centering " + i + " => "+ mean + " qmin " + qmin + " < " + qmax );
            // center the data
            meanCenteredSets.addSeries(new XYSeries("mean centered " + Integer.toString(i)));

            for(int j=0; j<totalInSeries; j++){
                tempItem = tempSeries.getDataItem(j);
                if (tempItem.getXValue() >= qmin && tempItem.getXValue() <= qmax){
                    meanCenteredSets.getSeries(i).add(tempItem.getXValue(), (tempItem.getYValue() - mean)*invCount);
                }
            }
        }
        System.out.println("FINISHED CENTERING");
    }

    /**
     *
     */
    private void createAMatrix(){
        // create covariance matrix
        XYDataItem tempItem;
        rows = meanCenteredSets.getSeries(0).getItemCount();
        matrixA = new DenseMatrix64F(rows, totalSets);
        //DenseMatrix64F matrixATranspose = new DenseMatrix64F(rows, totalSets);

        int col=0;
        // fill first column and use q-values as standard for subsequent rows
        XYSeries refSeries = meanCenteredSets.getSeries(0);
        for(int j=0; j<rows; j++){
            tempItem = refSeries.getDataItem(j);
            matrixA.set(j, col, tempItem.getYValue()); // could try q*I(q) also
        }

        // add remaining sets but check their q-values are in reference
        col = 1;
        for(int s=1; s<totalSets; s++){

            XYSeries tempSeries = meanCenteredSets.getSeries(s);
            for(int j=0; j<rows; j++){
                int indexOf = tempSeries.indexOf(refSeries.getX(j));
                if (indexOf > -1){
                    matrixA.set(j, col, tempSeries.getDataItem(indexOf).getYValue());
                } else { // interpolate
                    Double[] results = Functions.interpolate(tempSeries, refSeries.getX(j).doubleValue(), 1);
                    matrixA.set(j, col, results[1]);
                    System.out.println("Not found! Interpolating q : " + refSeries.getX(j));
                }
            }
            col++;
        }
    }


    /**
     * A = U*S*V_t
     */
    private void performSVDAMatrix(){

        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(rows, totalSets, true, true, false);

        try {
            svd.decompose(matrixA);
        } catch (Exception e){
            System.out.println("Matrix inversion exception in svdReduce ");
        }

        U_matrix = svd.getU(null,false);
        S_matrix = svd.getW(null);
        V_matrix = svd.getV(null,false);
        s_values = svd.getSingularValues();
    }



    /**
     * ZCA whitening
      * @param k
     */
    private void whitenAndReduceDataset(int k){

        int numrows = S_matrix.getNumRows();
        DenseMatrix64F D_matrix = new DenseMatrix64F(S_matrix.getNumRows(), S_matrix.getNumCols());
        for(int i=0; i<k; i++){
            D_matrix.set(i,i, 1.0/S_matrix.get(i,i));
        }

    }


    /**
     * ZCA whitening
     */
    private void whitenDataSet(){
        // svd whitendataset

        int numrows = S_matrix.getNumRows();
        DenseMatrix64F D_matrix = new DenseMatrix64F(S_matrix.getNumRows(), S_matrix.getNumCols());
        for(int i=0; i<numrows; i++){
            D_matrix.set(i,i, 1.0/S_matrix.get(i,i));
        }
        // U*D*U_t * X


    }





}
