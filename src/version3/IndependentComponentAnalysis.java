package version3;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;
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

    private DenseMatrix64F matrixX; // input data matrix holding mean-centered data
    private DenseMatrix64F U_matrix;
    private DenseMatrix64F S_matrix;
    private double[] s_values;
    private DenseMatrix64F V_matrix;
    private DenseMatrix64F whitened_matrix;
    private DenseMatrix64F cumulants_matrix;
    private boolean useReduced = false;

    private int rows;
    private int cols_number_of_q_values;
    private int dimsymm;
    private int nbcm;



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

        createMeanSubtractedDatasets();
        createAMatrix();
        performSVDAMatrix();
        //whitenDataSet();
        whitenAndReduceDataset();

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

            double invCount = 1.0/count;
            double mean = sum*invCount;
            // System.out.println("Mean Centering " + i + " => "+ mean + " qmin " + qmin + " < " + qmax );
            // center the data
            meanCenteredSets.addSeries(new XYSeries("mean centered " + Integer.toString(i)));

            for(int j=0; j<totalInSeries; j++){
                tempItem = tempSeries.getDataItem(j);
                if (tempItem.getXValue() >= qmin && tempItem.getXValue() <= qmax){
                    meanCenteredSets.getSeries(i).add(tempItem.getXValue(), (tempItem.getYValue() - mean));
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
        // rows = number of frames
        // total q-values =
        cols_number_of_q_values = meanCenteredSets.getSeries(0).getItemCount();
        matrixX = new DenseMatrix64F(totalSets, cols_number_of_q_values);
        // matrixX = new DenseMatrix64F(rows, totalSets);
        // DenseMatrix64F matrixATranspose = new DenseMatrix64F(rows, totalSets);
        int row=0;
        // fill first column and use q-values as standard for subsequent rows
        XYSeries refSeries = meanCenteredSets.getSeries(0);
        for(int j=0; j<cols_number_of_q_values; j++){
            tempItem = refSeries.getDataItem(j);
            matrixX.set(0, j, tempItem.getYValue()); // could try q*I(q) also
        }

        // add remaining sets but check their q-values are in reference
        row = 1;
        for(int s=1; s<totalSets; s++){

            XYSeries tempSeries = meanCenteredSets.getSeries(s);
            for(int j=0; j<cols_number_of_q_values; j++){

                int indexOf = tempSeries.indexOf(refSeries.getX(j));
                if (indexOf > -1){
                    matrixX.set(row, j, tempSeries.getDataItem(indexOf).getYValue());
                } else { // interpolate
                    Double[] results = Functions.interpolate(tempSeries, refSeries.getX(j).doubleValue(), 1);
                    matrixX.set(row, j, results[1]);
                    System.out.println("Not found! Interpolating q : " + refSeries.getX(j));
                }
            }
            row++;
        }
    }


    /**
     * A = U*S*V_t
     */
    private void performSVDAMatrix(){

        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(rows, totalSets, true, true, false);

        try {
            svd.decompose(matrixX);
        } catch (Exception e){
            System.out.println("Matrix inversion exception in svdReduce ");
        }

        U_matrix = svd.getU(null,false);
        S_matrix = svd.getW(null);
        V_matrix = svd.getV(null,false);
        s_values = svd.getSingularValues();
        System.out.println(" U rows: " +  U_matrix.getNumRows() + " cols: " + U_matrix.getNumCols());
        System.out.println(" S rows: " +  S_matrix.getNumRows() + " cols: " + S_matrix.getNumCols());
        System.out.println(" V rows: " +  V_matrix.getNumRows() + " cols: " + V_matrix.getNumCols());
    }


    /**
     * ZCA whitening
     */
    private void whitenAndReduceDataset(){

        int num_rows = matrixX.getNumRows();
        DenseMatrix64F D_matrix = new DenseMatrix64F(num_rows, num_rows);
        double invTotal = 1.0/(double)totalSets;

        // create variance matrix (inverse singular values)
        for(int i=0; i < num_rows; i++){
            D_matrix.set(i,i, 1.0/S_matrix.get(i,i));
        }

        // create reduced rotation matrix by taking first k columns of U_matrix
        DenseMatrix64F B_matrix = new DenseMatrix64F(num_rows, numberOfComponents);
        CommonOps.extract(U_matrix, 0, num_rows, 0, numberOfComponents, B_matrix, 0, 0);

        // D_matrix is now square
        // U*D*U_t * X
        System.out.println(" BEFORE : " + B_matrix.getNumRows() + " " + B_matrix.getNumCols());

        DenseMatrix64F DB_matrix = new DenseMatrix64F(numberOfComponents, num_rows);

        CommonOps.transpose(B_matrix);
        System.out.println(" AFTER : " + B_matrix.getNumRows() + " " + B_matrix.getNumCols());

        CommonOps.mult(B_matrix, D_matrix, DB_matrix);
        CommonOps.scale(invTotal, DB_matrix);

        System.out.println("Whitened X: " + matrixX.getNumRows() + " " + matrixX.getNumCols() + " | " + numberOfComponents);
        whitened_matrix = new DenseMatrix64F(numberOfComponents, matrixX.getNumCols());

        CommonOps.mult(DB_matrix, matrixX, whitened_matrix);

        for (int i=0; i<matrixX.getNumCols(); i++){
            System.out.println(datasets.getSeries(0).getX(i) + " " + datasets.getSeries(0).getY(i) + " " + whitened_matrix.get(0,i));
        }
    }


    /**
     * ZCA whitening
     */
    private void whitenDataSet(){
        // svd whitendataset

        int numrows = S_matrix.getNumRows();
        DenseMatrix64F D_matrix = new DenseMatrix64F(numrows, numrows);
        double invTotal = 1.0/(double)totalSets;

        // create variance matrix (inverse singular values)
        for(int i=0; i < numrows; i++){
            D_matrix.set(i,i, 1.0/S_matrix.get(i,i));
        }

        // create rotation matrix
        DenseMatrix64F B_matrix = new DenseMatrix64F(numrows,numrows);
        CommonOps.extract(U_matrix, 0, numrows, 0, numrows, B_matrix, 0, 0);

        // D_matrix is now square
        // U*D*U_t * X
        DenseMatrix64F DB_matrix = new DenseMatrix64F(numrows, numrows);

        CommonOps.transpose(B_matrix);
        CommonOps.mult(B_matrix, D_matrix, DB_matrix);
        CommonOps.scale(invTotal, DB_matrix);

        //System.out.println("Whitened X: " + matrixX.getNumRows() + " " + matrixX.getNumCols());
        whitened_matrix = new DenseMatrix64F(matrixX.getNumRows(), matrixX.getNumCols());
        CommonOps.mult(DB_matrix, matrixX, whitened_matrix);

        //for (int i=0; i<matrixX.getNumCols(); i++){
        //    System.out.println(datasets.getSeries(0).getX(i) + " " + datasets.getSeries(0).getY(i) + " " + whitened_matrix.get(0,i));
        //}
    }



    private void createCumulantMatrix(){

        dimsymm = numberOfComponents*(numberOfComponents+1)/2;
        nbcm = dimsymm;

        cumulants_matrix = new DenseMatrix64F(numberOfComponents, numberOfComponents*nbcm);

        DenseMatrix64F r_matrix = CommonOps.identity(numberOfComponents);

    }


}
