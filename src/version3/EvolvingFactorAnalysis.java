package version3;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

/**
 * Created by robertrambo on 28/08/2016.
 */
public class EvolvingFactorAnalysis extends SwingWorker {

    private double qmin;
    private double qmax;
    private XYSeriesCollection datasets;
    private int totalSets;
    private int eigenValuesToKeep;
    private int initialNumberOfColumns;
    private XYSeriesCollection meanCenteredSets;
    private XYSeriesCollection forwardEigenValueSet;
    private XYSeriesCollection reverseEigenValueSet;
    private int startIndexOfFrame;
    DenseMatrix64F matrixA;


    private JProgressBar bar;

    public EvolvingFactorAnalysis(double qmin, double qmax, XYSeriesCollection datasets, int startIndexOfFrame, int numberOfEigenValuesToPlot, JProgressBar bar) {

        this.qmin = qmin;
        this.qmax = qmax;
        this.datasets = datasets;
        totalSets = datasets.getSeriesCount();
        System.out.println("Total Sets SVDCovariance " + totalSets);
        this.startIndexOfFrame = startIndexOfFrame;

        // initialize eigenvalue series to plot
        this.eigenValuesToKeep = numberOfEigenValuesToPlot;
        if (numberOfEigenValuesToPlot < 3) {
            this.initialNumberOfColumns = 3;
        } else {
            this.initialNumberOfColumns = numberOfEigenValuesToPlot;
        }


        forwardEigenValueSet= new XYSeriesCollection();
        reverseEigenValueSet= new XYSeriesCollection();

        for(int r=0; r<eigenValuesToKeep; r++){
            forwardEigenValueSet.addSeries(new XYSeries(Integer.toString(r+1)));
            reverseEigenValueSet.addSeries(new XYSeries(Integer.toString(eigenValuesToKeep - r)));
        }

    }


    @Override
    protected Object doInBackground() throws Exception {

        // mean center the data
        createMeanSubtractedDatasets();

        // create initial matrix
        int rows = meanCenteredSets.getSeries(0).getItemCount();
        matrixA = new DenseMatrix64F(rows, initialNumberOfColumns);
        updateMatrixAForward(rows, initialNumberOfColumns);

        // perform SVD
        double[] s_values = SVDofMatrixA(rows, initialNumberOfColumns);

        // populate forward eigenvalues
        // if initial number of eigenvalues is less than 6
        // then calculate initialnumber of columsn at 6
        for (int frame=0; frame<initialNumberOfColumns; frame++) {
            for (int j = 0; j < eigenValuesToKeep; j++) {
                forwardEigenValueSet.getSeries(j).add(startIndexOfFrame + frame, s_values[j]);
            }
        }


        // perform SVD on subsequent frames
        for (int frame=initialNumberOfColumns; frame<totalSets; frame++){
            // add new column
            int totalColumns = frame+1;
            matrixA.reshape(rows, totalColumns, false);
            updateMatrixAForward(rows, totalColumns);
            // perform svd
            s_values = SVDofMatrixA(rows, totalColumns);
            // update forward eigenValues.
            for (int j = 0; j < eigenValuesToKeep; j++) {
                forwardEigenValueSet.getSeries(j).add(startIndexOfFrame + frame, s_values[j]);
            }
        }

        System.out.println("Finished Forward");
        // do reverse eigenvalues
        // create initial matrix
//        matrixA.reshape(rows, initialNumberOfColumns, false);
//        updateMatrixAReverse(rows, totalSets - 1 - initialNumberOfColumns);
//        s_values = SVDofMatrixA(rows, initialNumberOfColumns);
//        int indexOfLastFrame = startIndexOfFrame + totalSets - 1;
//
//        for (int frame=0; frame<initialNumberOfColumns; frame++) {
//            for (int j = 0; j < eigenValuesToKeep; j++) {
//                reverseEigenValueSet.getSeries(j).add(indexOfLastFrame - frame, s_values[j]);
//            }
//        }
//
//        // perform SVD on subsequent frames
//        for (int frame=initialNumberOfColumns; frame<totalSets; frame++){
//            // add new column
//            int totalColumns = frame+1;
//            matrixA.reshape(rows, totalColumns, false);
//
//            updateMatrixAReverse(rows, (totalSets - totalColumns - 1));
//            // perform svd
//            s_values = SVDofMatrixA(rows, totalColumns);
//            // update forward eigenValues.
//            for (int j = 0; j < eigenValuesToKeep; j++) {
//                reverseEigenValueSet.getSeries(j).add(indexOfLastFrame - frame, s_values[j]);
//            }
//        }

        System.out.println("Finished Reverse");

        return null;
    }


    public XYSeriesCollection getForwardEigenValueSet(){ return forwardEigenValueSet; }
    public XYSeriesCollection getReverseEigenValueSet(){ return reverseEigenValueSet; }


    private double[] SVDofMatrixA(int rows, int cols){

        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(rows, cols, true, true, false);
        // A = U*W*V_t
        try {
            svd.decompose(matrixA);
        } catch (Exception e){
            System.out.println("Matrix inversion exception in svdReduce ");
        }

        //DenseMatrix64F U = svd.getU(null,false);
        return svd.getSingularValues();
        //DenseMatrix64F V = svd.getV(null,false);
    }


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
            //System.out.println("Mean Centering " + i + " => "+ mean + " qmin " + qmin + " < " + qmax );

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
     * start from last of meanCenteredSets
     */
    private void updateMatrixAReverse(int rows, int stopAt){
        int col=0;
        // fill first column and use q-values as standard for subsequent rows
        int last = meanCenteredSets.getSeriesCount()-1;
        XYSeries refSeries = meanCenteredSets.getSeries(last);
        XYDataItem tempItem;
        for(int j=0; j<rows; j++){
            tempItem = refSeries.getDataItem(j);
            matrixA.set(j, col, tempItem.getYValue()); // could try q*I(q) also
        }

        // add remaining sets but check their q-values are in reference
        col = 1;
        for(int s=(last-1); s > stopAt; s--){

            XYSeries tempSeries = meanCenteredSets.getSeries(s);
            for(int j=0; j<rows; j++){
                int indexOf = tempSeries.indexOf(refSeries.getX(j));
                if (indexOf > -1){
                    matrixA.set(j, col, tempSeries.getDataItem(indexOf).getYValue());
                } else { // interpolate
                    System.out.println("Not found! ");
                }
            }
            col++;
        }
    }


    /**
     * Fill Matrix to be SVD decomposed
     * @param rows
     * @param stopAt
     */
    private void updateMatrixAForward(int rows, int stopAt){

        // fill first column and use q-values as standard for subsequent rows
        XYSeries refSeries = meanCenteredSets.getSeries(0);
        XYDataItem tempItem;

        for(int j=0; j<rows; j++){
            tempItem = refSeries.getDataItem(j);
            matrixA.set(j, 0, tempItem.getYValue()); // could try q*I(q) also
        }

        // add remaining sets but check their q-values are in reference
        for(int s=1; s<stopAt; s++){

            XYSeries tempSeries = meanCenteredSets.getSeries(s);
            for(int j=0; j<rows; j++){
                int indexOf = tempSeries.indexOf(refSeries.getX(j));
                if (indexOf > -1){
                    matrixA.set(j, s, tempSeries.getY(indexOf).doubleValue());
                } else { // interpolate
                    System.out.println("Not found! ");
                }
            }
        }

    }

    @Override
    protected void done() {
        try {
            super.get();

            System.out.println("done");
            //can call other gui update code here
        } catch (Throwable t) {
            //do something with the exception
        }
    }



}
