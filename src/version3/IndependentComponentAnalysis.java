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
        if (numberOfEigenValuesToPlot < totalSets){
            useReduced = true;
        } else {
            this.numberOfComponents = numberOfEigenValuesToPlot;
        }

        // if total number of frames > numberOfComponents (k), must perform dimension reduction
        // divide set into k-bins
        // sample 1 from each bin
        // perform ICA, and average the results

        createMeanSubtractedDatasets();
        createAMatrix();
        performSVDAMatrix();

        whitenDataSet();
        //whitenAndReduceDataset();
        createCumulantMatrix();

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

        cols_number_of_q_values = meanCenteredSets.getSeries(0).getItemCount();
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
        DenseMatrix64F DB_matrix = new DenseMatrix64F(numberOfComponents, num_rows);

        CommonOps.transpose(B_matrix); // B has to be transposed before multiplication
        CommonOps.mult(B_matrix, D_matrix, DB_matrix);
        CommonOps.scale(invTotal, DB_matrix);

        System.out.println("Whitened X: " + matrixX.getNumRows() + " " + matrixX.getNumCols() + " | " + numberOfComponents);
        whitened_matrix = new DenseMatrix64F(numberOfComponents, matrixX.getNumCols());

        CommonOps.mult(DB_matrix, matrixX, whitened_matrix);

//        for (int i=0; i<matrixX.getNumCols(); i++){
//            System.out.println(meanCenteredSets.getSeries(0).getX(i) + " " + meanCenteredSets.getSeries(0).getY(i) + " " + whitened_matrix.get(0,i)  + " " + whitened_matrix.get(1,i));
//        }
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

        CommonOps.transpose(B_matrix); // in place transpose
        CommonOps.mult(B_matrix, D_matrix, DB_matrix); //
        CommonOps.scale(invTotal, DB_matrix);

        System.out.println("Whitened X: " + matrixX.getNumRows() + " " + matrixX.getNumCols());
        whitened_matrix = new DenseMatrix64F(matrixX.getNumRows(), matrixX.getNumCols());
        CommonOps.mult(DB_matrix, matrixX, whitened_matrix);
        numberOfComponents = matrixX.getNumRows();
    }


    /**
     *
     */
    private void jointDiagonalizationCumulantMatrix(){

        DenseMatrix64F matrix_V = CommonOps.identity(numberOfComponents);

        /*
         *
         */
        double on = 0;
        int range = numberOfComponents;

        int endAt = numberOfComponents;
        int colAt = 0;
        double value;

        // make a sum of the squared diagonals of the cumulants matrix
        while(endAt < nbcm){
            for (int i=0; i<numberOfComponents; i++){
                value = cumulants_matrix.get(i,i+colAt);
                on += value*value;
            }
            colAt = endAt;
            endAt += numberOfComponents;
        }

        DenseMatrix64F cumulants_matrix_element_product = new DenseMatrix64F(numberOfComponents, numberOfComponents*nbcm);

        CommonOps.elementMult(cumulants_matrix, cumulants_matrix, cumulants_matrix_element_product);

        double off = CommonOps.elementSum(cumulants_matrix_element_product) + on;


        double seiul = 0.000001/Math.sqrt((double)cols_number_of_q_values);
        boolean encore = true;
        int sweep = 0;
        int updates = 0;
        int upds = 0;

        double cos, sin, ton, toff, theta=0, gain;

        DenseMatrix64F g_matrix = new DenseMatrix64F(2,nbcm);
        DenseMatrix64F gg_matrix = new DenseMatrix64F(2,2);
        DenseMatrix64F bigG_matrix = new DenseMatrix64F(2,2);

        while (encore){

            sweep+=1;
            upds = 0;
            DenseMatrix64F VKeep = new DenseMatrix64F(matrix_V);

            for (int p=0; p<(numberOfComponents-1); p++){

                for (int q=(p+1); q<numberOfComponents; q++){

                    ton = gg_matrix.get(1,1) - gg_matrix.get(2,2);
                    toff = gg_matrix.get(1,2) + gg_matrix.get(2,1);
                    theta = 0.5*Math.atan2(toff, ton+Math.sqrt(ton*ton + toff*toff));
                    gain = (Math.sqrt(ton*ton + toff*toff) - ton)*0.25;

                    if (Math.abs(theta) > seiul){
                        encore = false;
                        upds+=1;
                        cos = Math.cos(theta);
                        sin = Math.sin(theta);

                        bigG_matrix.set(0,0,cos);
                        bigG_matrix.set(0,1,-sin);
                        bigG_matrix.set(1,0,sin);
                        bigG_matrix.set(1,1,cos);
                    }
                }
            }





        }


    }


    /**
     *
     */
    private void createCumulantMatrix(){

        double sqrt2 = Math.sqrt(2.0);

        dimsymm = numberOfComponents*(numberOfComponents+1)/2;
        nbcm = dimsymm;

        cumulants_matrix = new DenseMatrix64F(numberOfComponents, numberOfComponents*nbcm);

        DenseMatrix64F matrix_R = CommonOps.identity(numberOfComponents);

        DenseMatrix64F vector_Xim = new DenseMatrix64F(cols_number_of_q_values, 1);
        DenseMatrix64F vector_Xijm = new DenseMatrix64F(cols_number_of_q_values, 1);
        DenseMatrix64F vector_Xijm_temp = new DenseMatrix64F(cols_number_of_q_values, 1);

        DenseMatrix64F matrix_Qij = new DenseMatrix64F(numberOfComponents, numberOfComponents);
        //DenseMatrix64F matrix_Qij_1 = new DenseMatrix64F(numberOfComponents, numberOfComponents);

        DenseMatrix64F Xijm_matrixX_mult = new DenseMatrix64F(cols_number_of_q_values, numberOfComponents);

        // extract columns vector
        CommonOps.transpose(whitened_matrix);
        System.out.println("transposed Whitened X : " + whitened_matrix.getNumRows() + " " + whitened_matrix.getNumCols());
        System.out.println("transposed Whitened X : " + Xijm_matrixX_mult.getNumRows() + " " + Xijm_matrixX_mult.getNumCols());

        double invT = 1.0/cols_number_of_q_values, value;
        int range = numberOfComponents;

        for (int im=0; im<numberOfComponents; im++){
            CommonOps.extractColumn(whitened_matrix, im, vector_Xim);
            CommonOps.elementMult(vector_Xim, vector_Xim, vector_Xijm);

            // form Q_ij
            Xijm_matrixX_mult = elementwiseMultiplyVectorMatrix(vector_Xijm, whitened_matrix);
            CommonOps.transpose(Xijm_matrixX_mult);
            CommonOps.mult(Xijm_matrixX_mult, whitened_matrix, matrix_Qij); // return square matrix
            CommonOps.scale(invT, matrix_Qij);
            CommonOps.subtractEquals(matrix_Qij, matrix_R);

            value = matrix_Qij.get(im,im)-2;
            matrix_Qij.set(im, im, value);
            // add to cumulant matrix
            update_cumulant_matrix(cumulants_matrix, matrix_Qij, range, numberOfComponents);

            range += numberOfComponents;

            for (int jm=0; jm<im; jm++){

                CommonOps.extractColumn(whitened_matrix, jm, vector_Xijm_temp);
                CommonOps.elementMult(vector_Xim, vector_Xijm_temp, vector_Xijm);

                Xijm_matrixX_mult = elementwiseMultiplyVectorMatrix(vector_Xijm, whitened_matrix);
                CommonOps.transpose(Xijm_matrixX_mult);
                CommonOps.mult(Xijm_matrixX_mult, whitened_matrix, matrix_Qij); // return square matrix
                CommonOps.scale(invT, matrix_Qij);
                // subtract
                value = matrix_Qij.get(im,jm)-1;
                matrix_Qij.set(im, jm, value);
                value = matrix_Qij.get(jm,im)-1;
                matrix_Qij.set(jm, im, value);
                //scale by square root 2
                CommonOps.scale(sqrt2, matrix_Qij);

                // add to cumulant matrix
                update_cumulant_matrix(cumulants_matrix, matrix_Qij, range, numberOfComponents);
                range += numberOfComponents;

            }
        }
    }


    /**
     * Add a square matrix qij (mxm) to rectangular matrix cumulants_matrix that is mxn where n is a multiple of m
     *
     * @param cumulants_matrix destination matrix
     * @param matrix_qij source matrix
     * @param im starting column position in cumulants
     * @param lengthOf
     */
    private void update_cumulant_matrix(DenseMatrix64F cumulants_matrix, DenseMatrix64F matrix_qij, int im, int lengthOf) {
        CommonOps.extract(matrix_qij, 0, lengthOf, 0, lengthOf, cumulants_matrix, 0, im);
    }

    /**
     * multiply each column of the matrix by the column vector, elementwise*
     * @param columnVector must be same length as the number rows of matrix
     * @param matrix
     * @return
     */
    private DenseMatrix64F elementwiseMultiplyVectorMatrix(DenseMatrix64F columnVector, DenseMatrix64F matrix){


        int matrixRows = matrix.getNumRows();
        int cols = matrix.getNumCols();

        DenseMatrix64F returnMe = new DenseMatrix64F(matrixRows,cols);


        for (int row=0; row< matrixRows; row++){
            double value = columnVector.get(row,0);
            for(int col=0; col<cols; col++){
                returnMe.set(row, col, value*matrix.get(row,col));
            }
        }
        return returnMe;
    }


}
