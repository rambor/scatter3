package version3;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

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

    private int startIndexOfFrame;

    private DenseMatrix64F matrixX; // input data matrix holding mean-centered data
    private DenseMatrix64F originalMatrixX; // input data matrix holding mean-centered data
    private DenseMatrix64F U_matrix;
    private DenseMatrix64F S_matrix;
    private double[] s_values;
    private DenseMatrix64F V_matrix;
    private DenseMatrix64F whitened_matrix;
    private DenseMatrix64F cumulants_matrix;
    private DenseMatrix64F DB_matrix;
    private DenseMatrix64F B_matrix;
    private int lengthOfCM;
    private boolean useReduced = false;

    private int rows;
    private int cols_number_of_q_values;
    private int dimsymm;
    private int nbcm;

    private JProgressBar bar;
    private XYSeriesCollection testCollection;

    public IndependentComponentAnalysis(double qmin, double qmax, XYSeriesCollection datasets, int startIndexOfFrame, int numberOfEigenValuesToPlot, JProgressBar bar){
        this.qmin = qmin;
        this.qmax = qmax;
        this.datasets = datasets;
        totalSets = datasets.getSeriesCount();
        this.startIndexOfFrame = startIndexOfFrame;

        // initialize eigenvalue series to plot
//        if (numberOfEigenValuesToPlot < totalSets){
//            useReduced = true;
//            this.numberOfComponents = numberOfEigenValuesToPlot;
//        }

        System.out.println("EIGEN " + numberOfEigenValuesToPlot + " " + totalSets);
        this.bar = bar;
        if (numberOfEigenValuesToPlot < totalSets){
            useReduced = true;
            this.numberOfComponents = numberOfEigenValuesToPlot;
        } else {
            this.numberOfComponents = totalSets;
        }

        // if total number of frames > numberOfComponents (k), must perform dimension reduction
        // divide set into k-bins
        // sample 1 from each bin
        // perform ICA, and average the results

//        createMeanSubtractedDatasets();
//        createAMatrix();
//        performSVDAMatrix();
//
//        //whitenDataSet();
//        whitenAndReduceDataset();
//        createCumulantMatrix();
//        jointDiagonalizationCumulantMatrix();
//
//        SAXSICA= new XYSeriesCollection();
    }


    public IndependentComponentAnalysis(double qmin, double qmax, XYSeriesCollection datasets, int numberOfEigenValuesToPlot, JProgressBar bar){
        this.qmin = qmin;
        this.qmax = qmax;
        this.datasets = datasets;
        totalSets = datasets.getSeriesCount();

        this.bar = bar;
        // initialize eigenvalue series to plot
        if (numberOfEigenValuesToPlot < totalSets){
            useReduced = true;
            this.numberOfComponents = numberOfEigenValuesToPlot;
        } else {
            this.numberOfComponents = totalSets;
        }

//        createMeanSubtractedDatasets();
//        createAMatrix();
//        performSVDAMatrix();
//
//        if (useReduced){
//            whitenAndReduceDataset();
//        } else {
//            whitenDataSet();
//        }
//
//        createCumulantMatrix();
//        jointDiagonalizationCumulantMatrix();
//
//        SAXSICA= new XYSeriesCollection();
    }


    @Override
    protected Object doInBackground() throws Exception {

        bar.setString("DEMIXING");
        bar.setIndeterminate(true);

        System.out.println("In Background ");

        //make a fake dataset for testing

        createMeanSubtractedDatasets();
        createAMatrix();
        performSVDAMatrix();

        if (useReduced){
            whitenAndReduceDataset();
            System.out.println("In Background REDUCED");
        } else {
            whitenDataSet();
        }

        createCumulantMatrix();
        jointDiagonalizationCumulantMatrix();
        bar.setIndeterminate(false);
        bar.setString("FINISHED");
        return null;
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


    /**
     *
     */
    private void createMeanSubtractedDatasets(){

        meanCenteredSets = new XYSeriesCollection();
        XYSeriesCollection ratioSets = new XYSeriesCollection();

        XYDataItem tempItem;

        // create ratio of datasets
        // create q-bins and randomly sample from each bin to make a dataset to invert
        // estimate dmax and select 2 to 3 points from each bin?

        // for each dataset
        // calculate average value
        // then iterate again and subtract the mean to center
        for(int i=0; i<totalSets; i++){

            double sum=0;
            double count=0;

            XYSeries tempSeries = datasets.getSeries(i);
            int totalInSeries = tempSeries.getItemCount();

            for(int j=0; j<totalInSeries; j++){
                tempItem = tempSeries.getDataItem(j);
                if (tempItem.getXValue() >= qmin && tempItem.getXValue() <= qmax){
                    //sum += tempItem.getYValue()*tempItem.getXValue(); // q*I(q)
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
                    //meanCenteredSets.getSeries(i).add(tempItem.getXValue(), (tempItem.getYValue()*tempItem.getXValue() - mean)); // q*I(q)
                    meanCenteredSets.getSeries(i).add(tempItem.getXValue(), (tempItem.getYValue() - mean)); //
                }
            }
        }

        // create matrix of original values bounded by qmin and qmax
        originalMatrixX = new DenseMatrix64F(totalSets, meanCenteredSets.getSeries(0).getItemCount());
        //
        XYSeries refSeries = datasets.getSeries(0);
        int count = 0;
        for(int j=0; j<refSeries.getItemCount(); j++){
            tempItem = refSeries.getDataItem(j);
            if (tempItem.getXValue() >= qmin && tempItem.getXValue() <= qmax){
                //originalMatrixX.set(0, count, tempItem.getYValue()*tempItem.getXValue()); //  q*I(q)
                originalMatrixX.set(0, count, tempItem.getYValue()); //
                count++;
            }
        }

        // add remaining sets but check their q-values are in reference

        for(int row=1; row<totalSets; row++){

            XYSeries tempSeries = datasets.getSeries(row);
            count=0;
            for(int j=0; j<refSeries.getItemCount(); j++){
                tempItem = refSeries.getDataItem(j);
                if (tempItem.getXValue() >= qmin && tempItem.getXValue() <= qmax){
                    int indexOf = tempSeries.indexOf(tempItem.getXValue());
                    if (indexOf > -1){
                        //originalMatrixX.set(row, count, tempSeries.getDataItem(indexOf).getYValue()*tempSeries.getDataItem(indexOf).getXValue()); // q*I(q)
                        originalMatrixX.set(row, count, tempSeries.getDataItem(indexOf).getYValue()); // q*I(q)
                    } else { // interpolate
                        Double[] results = Functions.interpolate(tempSeries, tempItem.getXValue(), 1);
                        //originalMatrixX.set(row, count, results[1]*tempItem.getXValue());
                        originalMatrixX.set(row, count, results[1]);
                        System.out.println("Not found! Interpolating for Original Matrix q : " + refSeries.getX(j));
                    }
                    count++;
                }
            }
        }

        cols_number_of_q_values = meanCenteredSets.getSeries(0).getItemCount(); // based on first dataset as reference
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

        // fill first column and use q-values as standard for subsequent rows
        XYSeries refSeries = meanCenteredSets.getSeries(0);
        for(int j=0; j<cols_number_of_q_values; j++){
            tempItem = refSeries.getDataItem(j);
            matrixX.set(0, j, tempItem.getYValue()); //
        }

        // add remaining sets but check their q-values are in reference

        for(int row=1; row<totalSets; row++){

            XYSeries tempSeries = meanCenteredSets.getSeries(row);
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
        DenseMatrix64F D_matrix = new DenseMatrix64F(numberOfComponents, numberOfComponents);
        double invTotal = 1.0/(double)totalSets;

        // create variance matrix (inverse singular values)
        for(int i=0; i < numberOfComponents; i++){ // square matrix limited to number of components
            D_matrix.set(i,i, 1.0/S_matrix.get(i,i));
        }

        // create reduced rotation matrix by taking first k columns of U_matrix
        B_matrix = new DenseMatrix64F(num_rows, numberOfComponents);
        CommonOps.extract(U_matrix, 0, num_rows, 0, numberOfComponents, B_matrix, 0, 0);

        // D_matrix is now square
        // U*D*U_t * X
        DB_matrix = new DenseMatrix64F(numberOfComponents, num_rows);

        CommonOps.transpose(B_matrix); // B is the U matrix and has to be transposed before multiplication
        CommonOps.mult(D_matrix, B_matrix, DB_matrix);
        CommonOps.scale(invTotal, DB_matrix); // check the data is whitened?

        System.out.println("Whitened X: Frames => " + matrixX.getNumRows() + " q-values => " + matrixX.getNumCols() + " | " + numberOfComponents);
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
        System.out.println("NO REDUCTION SOURCES = SIGNALS " + numberOfComponents + " = " + matrixX.getNumRows());
        int numrows = S_matrix.getNumRows();
        DenseMatrix64F D_matrix = new DenseMatrix64F(numrows, numrows);
        double invTotal = 1.0/(double)totalSets;

        // create variance matrix (inverse singular values)
        for(int i=0; i < numrows; i++){
            D_matrix.set(i,i, 1.0/S_matrix.get(i,i));
        }

        // create rotation matrix
        B_matrix = new DenseMatrix64F(numrows,numrows);
        CommonOps.extract(U_matrix, 0, numrows, 0, numrows, B_matrix, 0, 0);

        // D_matrix is now square
        // U*D*U_t * X
        DB_matrix = new DenseMatrix64F(numrows, numrows);

        CommonOps.transpose(B_matrix); // in place transpose
        CommonOps.mult(D_matrix, B_matrix, DB_matrix); //
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
        //int range = numberOfComponents;

        int endAt = numberOfComponents;
        int colAt = 0;
        double value;

        // make a sum of the squared diagonals of the cumulants matrix
        while(endAt < lengthOfCM){ // m*nbcm
            for (int i=0; i<numberOfComponents; i++){
                value = cumulants_matrix.get(i,i+colAt);
                System.out.println(i + " " + endAt + " => " + (colAt + i) + " ON " + value);
                on += value*value;
            }
            colAt = endAt;
            endAt += numberOfComponents;
        }

        DenseMatrix64F cumulants_matrix_element_product = new DenseMatrix64F(numberOfComponents, lengthOfCM);
        CommonOps.elementMult(cumulants_matrix, cumulants_matrix, cumulants_matrix_element_product);

        double off = CommonOps.elementSum(cumulants_matrix_element_product) + on;


        double seiul = 0.0000001/Math.sqrt((double)cols_number_of_q_values);
        boolean encore = true;
        int sweep = 0;
        int updates = 0;
        int upds = 0;

        double cos, sin, ton, toff, theta=0, gain;

        DenseMatrix64F g_matrix;
        DenseMatrix64F g_matrix_trans;
        DenseMatrix64F gg_matrix = new DenseMatrix64F(2,2);
        //DenseMatrix64F bigG_matrix = new DenseMatrix64F(2,2);
        //DenseMatrix64F matrixCMouput = new DenseMatrix64F(2, numberOfComponents);

        ArrayList<Integer> p_indices;
        ArrayList<Integer> q_indices;

        while (encore){

            encore=false;
            sweep+=1;
            upds = 0;
            //DenseMatrix64F VKeep = new DenseMatrix64F(matrix_V);

            for (int p=0; p<(numberOfComponents-1); p++){

                for (int q=(p+1); q<numberOfComponents; q++){

                    p_indices = new ArrayList<>();
                    q_indices = new ArrayList<>();

                    fill_indices(p, p_indices, nbcm);
                    fill_indices(q, q_indices, nbcm);

                    // p_indices and q_indices should be same length
                    // System.out.println("("+ p + ") P SIZE : " +p_indices.size() + " (" + q + ") Q SIZE : " + q_indices.size());

                    g_matrix = new DenseMatrix64F(2, p_indices.size());
                    g_matrix_trans = new DenseMatrix64F(p_indices.size(), 2);

                    fill_g_matrix(p, p_indices, q, q_indices, cumulants_matrix, g_matrix);

                    CommonOps.transpose(g_matrix, g_matrix_trans);
                    CommonOps.mult(g_matrix, g_matrix_trans, gg_matrix);

                    ton = gg_matrix.get(0,0) - gg_matrix.get(1,1);
                    toff = gg_matrix.get(0,1) + gg_matrix.get(1,0);
                    theta = 0.5*Math.atan2(toff, ton+Math.sqrt(ton*ton + toff*toff));
                    gain = (Math.sqrt(ton*ton + toff*toff) - ton)*0.25;

                    if (Math.abs(theta) > seiul){
                        encore = true;
                        upds+=1;
                        cos = Math.cos(theta);
                        sin = Math.sin(theta);

                        rotate_V_at_PQ(matrix_V, p, q, cos, sin);

                        rotate_CM_at_PQ(p, q, cos, sin);
                        //update CM at [p,q]
                        //update_CM_rotation(p, q, matrixCMouput);
                        update_CM(cos, sin, p_indices, q_indices);

                        on = on + gain;
                        off = off - gain;
                    }
                }
            }
            System.out.println("Updates : " + upds);
        }

        System.out.println("Finished => |theta| " + Math.abs(theta) + " < " + seiul);
        CommonOps.transpose(matrix_V);
        System.out.println("matrix_V : " + matrix_V.getNumRows() + " " + matrix_V.getNumCols());
        System.out.println("WHITENING MATRIX (DB) : " + DB_matrix.getNumRows() + " " + DB_matrix.getNumCols());

        DenseMatrix64F demixingMatrix = new DenseMatrix64F(DB_matrix.getNumRows(), DB_matrix.getNumCols());
        DenseMatrix64F demixingMatrixTemp = new DenseMatrix64F(DB_matrix.getNumRows(), DB_matrix.getNumCols());
        CommonOps.mult(matrix_V, DB_matrix, demixingMatrixTemp);

        // sort the matrix in terms of amplitudes
        DenseMatrix64F invMatrix = new DenseMatrix64F(DB_matrix.getNumCols(), DB_matrix.getNumRows());
        CommonOps.pinv(demixingMatrixTemp, invMatrix);
        ArrayList<Pair> indices = new ArrayList<>();
        column_sort_matrix(invMatrix, indices, demixingMatrixTemp);

        // get first column of demixing matrix and determine signs
        DenseMatrix64F signMatrix = new DenseMatrix64F(DB_matrix.getNumRows(), DB_matrix.getNumRows());
        for (int i=0; i<DB_matrix.getNumRows(); i++){
            signMatrix.set(i,i, (int)Math.signum( Math.signum(demixingMatrixTemp.get(i,0) + 0.1) ) );
        }

        CommonOps.mult(signMatrix, demixingMatrixTemp, demixingMatrix);
        DenseMatrix64F output = new DenseMatrix64F(B_matrix.getNumRows(), matrixX.getNumCols());

        SAXSICA= new XYSeriesCollection();

        if (useReduced){
            // reduce the data then demix
            System.out.println("DEMIXING MATRX : " + demixingMatrix.getNumRows() + " x (cols) " + demixingMatrix.getNumCols());
            CommonOps.mult(demixingMatrix, originalMatrixX, output);

            XYSeries tempSeries = meanCenteredSets.getSeries(0);
            double q_value;
            int totalrows = output.getNumRows();
            for (int row=0; row<totalrows; row++){
                SAXSICA.addSeries(new XYSeries("SERIES " + row));
                for(int j=0; j<cols_number_of_q_values; j++){
                    q_value = tempSeries.getX(j).doubleValue();
                    //System.out.println(q_value + " " + output.get(row, j)+ " " + output.get(row+1, j) );
                    SAXSICA.getSeries(row).add(q_value, output.get(row, j));
                }
            }


            for (int row=0; row<1; row++){
                for(int j=0; j<cols_number_of_q_values; j++){
                    q_value = tempSeries.getX(j).doubleValue();
                    if (output.getNumRows() > 2){
                        System.out.println(q_value + " " + output.get(row, j)+ " " + output.get(row+1, j) + " " + output.get(row+2, j));
                    } else {
                        System.out.println(q_value + " " + output.get(row, j)+ " " + output.get(row+1, j));
                    }

                }
            }

        } else { // not using reduced dataset
            //CommonOps.mult(demixingMatrix, originalMatrixX, output);
            CommonOps.mult(demixingMatrix, matrixX, output);

            XYSeries tempSeries = meanCenteredSets.getSeries(0);
            double q_value;
            int totalrows = output.getNumRows();
            for (int row=0; row<totalrows; row++){
                SAXSICA.addSeries(new XYSeries("SERIES " + row));
                for(int j=0; j<cols_number_of_q_values; j++){
                    q_value = tempSeries.getX(j).doubleValue();
                    //System.out.println(q_value + " " + output.get(row, j)+ " " + output.get(row+1, j));
                    SAXSICA.getSeries(row).add(q_value, output.get(row, j));
                }
            }
        }

        // column sum of output matrix norm
        
    }

    public XYSeriesCollection getSAXSICA(){return SAXSICA;}

    private void column_sort_matrix(DenseMatrix64F output, ArrayList<Pair> indices, DenseMatrix64F matrixToSort) {
        int rowsOf = output.getNumRows();
        int colsOf = output.getNumCols();
        DenseMatrix64F tempMatrix = new DenseMatrix64F(matrixToSort.getNumRows(), matrixToSort.getNumCols());
        double value, sum;
        // for each column, sum down row
        for (int c=0; c<colsOf; c++){
            sum = 0;
            for(int r=0; r<rowsOf; r++){
                value = output.get(r, c);
                sum += value*value;
            }
            indices.add(new Pair(c, sum));
        }
        // sort
        Collections.sort(indices, new Comparator<Pair>() {
            @Override public int compare(Pair p1, Pair p2) {
                return Double.compare(p2.value, p1.value);  // descending, note order of p2 and p1
            }
        });

        System.out.println("SORTED COLUMNS (descending) :");
        for(int i=0; i<indices.size(); i++){
            System.out.println(i + " " + indices.get(i).column + " => " + indices.get(i).value);
        }
        System.out.println(output.getNumRows() + " x " + output.getNumCols() + " | " + matrixToSort.getNumRows() + " x " + matrixToSort.getNumCols());
        System.out.println("Total sorted : " + indices.size());
        // sort the matrix, make copy and replace
        int totalToSort = matrixToSort.getNumRows();
        for (int i=0; i<totalToSort; i++){
            CommonOps.extract(matrixToSort, indices.get(i).column, indices.get(i).column+1, 0, matrixToSort.getNumCols(), tempMatrix, i, 0);
        }
        // replace
        for (int i=0; i<totalToSort; i++){
            CommonOps.extract(tempMatrix, i, i+1, 0, matrixToSort.getNumCols(), matrixToSort, i, 0);
        }
    }


    class Pair {
        public final int column;
        public final double value;
        // ...
        public Pair(int column, double value){
            this.column = column;
            this.value = value;
        }
    };

    /**
     * Rotate G matrix by columns p and q of V and update V (from matlab code)
     * G defined as [c s; -s c]
     * Performs calculation as: V(:, pair) * G
     *
     * @param matrix_v
     * @param p
     * @param q
     * @param cos
     * @param sin
     */
    private void rotate_V_at_PQ(DenseMatrix64F matrix_v, int p, int q, double cos, double sin) {

        double right, left, p_element, q_element;

        for(int row=0; row<numberOfComponents; row++){
            p_element = matrix_v.get(row, p);
            q_element = matrix_v.get(row, q);

            left = cos*p_element + sin*q_element;
            right = -sin*p_element + cos*q_element;

            matrix_v.set(row, p, left);
            matrix_v.set(row, q, right);
        }
    }


    /**
     * Rotate rows p and q by transpose of G matrix (from matlab code)
     * transpose ([c -s; s c]) => [c s; -s c]
     * Performs caculation as: G' * CM(pair, :)
     * @param p
     * @param q
     * @param cos
     * @param sin
     */
    private void rotate_CM_at_PQ(int p, int q, double cos, double sin) {
        double top, bottom, p_element, q_element;

        for(int col=0; col<lengthOfCM; col++){
            p_element = cumulants_matrix.get(p, col);
            q_element = cumulants_matrix.get(q, col);
            top = cos*p_element + sin*q_element;
            bottom = -sin*p_element + cos*q_element;
            cumulants_matrix.set(p, col, top);
            cumulants_matrix.set(q, col, bottom);
        }
    }


    /**
     *
     * @param cos
     * @param sin
     * @param p_indices columns of cumulant_matrix
     * @param q_indices columns of cumulant matrix
     */
    private void update_CM(double cos, double sin, ArrayList<Integer> p_indices, ArrayList<Integer> q_indices) {

        int cols=p_indices.size();
        int p_index, q_index;
        double cm_Ip, cm_Iq;
        double p_column, q_column;

        for (int row=0; row<numberOfComponents; row++){
            // iterate over each column
            for (int col=0; col<cols; col++){
                p_index = p_indices.get(col);
                q_index = q_indices.get(col);

                cm_Ip = cumulants_matrix.get(row,p_index);
                cm_Iq = cumulants_matrix.get(row,q_index);

                p_column = cos*cm_Ip+sin*cm_Iq;
                q_column = -sin*cm_Ip+cos*cm_Iq;

                cumulants_matrix.set(row, p_index, p_column);
                cumulants_matrix.set(row, q_index, q_column);
            }
        }
    }




    private void fill_g_matrix(int row_p, ArrayList<Integer> p_indices, int row_q, ArrayList<Integer> q_indices, DenseMatrix64F cm_matrix, DenseMatrix64F g_matrix) {
        int cols = p_indices.size();
        int p_index;
        int q_index;

        for (int i=0; i<cols; i++){
            p_index = p_indices.get(i);
            q_index = q_indices.get(i);
            g_matrix.set(0, i, cm_matrix.get(row_p, p_index) - cm_matrix.get(row_q, q_index));
            g_matrix.set(1, i, cm_matrix.get(row_p, q_index) + cm_matrix.get(row_q, p_index));
        }

    }


    private void fill_indices(int p, ArrayList<Integer> p_indices, int nbcm) {
        int endAt = numberOfComponents*nbcm;
        for(int i=p; i< endAt; i+=numberOfComponents){
            p_indices.add(i);
        }
    }


    /**
     *
     */
    private void createCumulantMatrix(){

        double sqrt2 = Math.sqrt(2.0);

        dimsymm = numberOfComponents*(numberOfComponents+1)/2;
        nbcm = dimsymm;

        lengthOfCM = numberOfComponents*nbcm;
        cumulants_matrix = new DenseMatrix64F(numberOfComponents, lengthOfCM);

        DenseMatrix64F matrix_R = CommonOps.identity(numberOfComponents);
        DenseMatrix64F vector_Xim = new DenseMatrix64F(cols_number_of_q_values, 1);
        DenseMatrix64F vector_Xijm = new DenseMatrix64F(cols_number_of_q_values, 1);
        DenseMatrix64F vector_Xijm_temp = new DenseMatrix64F(cols_number_of_q_values, 1);
        DenseMatrix64F matrix_Qij = new DenseMatrix64F(numberOfComponents, numberOfComponents);

        DenseMatrix64F Xijm_matrixX_mult = new DenseMatrix64F(cols_number_of_q_values, numberOfComponents);

        // extract columns vector
        CommonOps.transpose(whitened_matrix);
        System.out.println("transposed Whitened X : " + whitened_matrix.getNumRows() + " " + whitened_matrix.getNumCols());
        System.out.println("transposed Whitened X : " + Xijm_matrixX_mult.getNumRows() + " " + Xijm_matrixX_mult.getNumCols());

        double invT = 1.0/cols_number_of_q_values, value;
        int startColumn = 0;

        for (int im=0; im<numberOfComponents; im++){
            CommonOps.extractColumn(whitened_matrix, im, vector_Xim);
            //CommonOps.elementMult(vector_Xim, vector_Xim, vector_Xijm);
            CommonOps.elementPower(vector_Xim, 2, vector_Xijm);

            // form Q_ij
            Xijm_matrixX_mult = elementwiseMultiplyVectorMatrix(vector_Xijm, whitened_matrix);
            CommonOps.transpose(Xijm_matrixX_mult);
            CommonOps.mult(Xijm_matrixX_mult, whitened_matrix, matrix_Qij); // return square matrix

            // Qij = ((scale* (Xim.*Xim)) .* X ) * X' 	- R - 2 * R(:,im)*R(:,im)' ;
            CommonOps.scale(invT, matrix_Qij);
            CommonOps.subtractEquals(matrix_Qij, matrix_R);
            //matrix_R.set(im, im, 2); // 2 * R(:,im)*R(:,im)'
            //CommonOps.subtractEquals(matrix_Qij, matrix_R);
            value = matrix_Qij.get(im,im)-2;
            matrix_Qij.set(im, im, value);
            // add to cumulant matrix
            update_cumulant_matrix(cumulants_matrix, matrix_Qij, startColumn, numberOfComponents);

            startColumn += numberOfComponents;

            for (int jm=0; jm<= (im-1); jm++){ // skipped on first one and second?

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
                update_cumulant_matrix(cumulants_matrix, matrix_Qij, startColumn, numberOfComponents);
                startColumn += numberOfComponents;
            }
        }
    }


    /**
     * Add a square matrix qij (mxm) to rectangular matrix cumulants_matrix that is mxn where n is a multiple of m
     *
     * @param cumulants_matrix destination matrix
     * @param matrix_qij source matrix
     * @param startColumn starting column position in cumulants
     * @param lengthOf
     */
    private void update_cumulant_matrix(DenseMatrix64F cumulants_matrix, DenseMatrix64F matrix_qij, int startColumn, int lengthOf) {

      //  System.out.println(" NC : " + numberOfComponents +  " " + lengthOf + " mxn : " + cumulants_matrix.getNumRows() + " x " + cumulants_matrix.getNumCols());
      //  System.out.println(" matrix_qij : " + matrix_qij.getNumRows() + " " + matrix_qij.getNumCols());
        // cumulants is destination
        CommonOps.extract(matrix_qij, 0, lengthOf, 0, lengthOf, cumulants_matrix, 0, startColumn);
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
                //System.out.println("VALUE*MATRIX " + matrix.get(row,col));
            }
        }

        return returnMe;
    }

    private void createTestDataset(){
//        %=======================================================================
//        n	= 3 	;  % M = number of sources
//        m	= 4	;  % m = number of sensors (add the relevant lines in S= ...)
//        T	= 200	;  % sample size
//        NdB	= -30 	;  % kind of noise level in dB
//        %----------------------------------------------------------------------------
//
//                f1 = 0.013 ;
//        f2 = 0.02 ;
//
//        s1	=     1.2*cos(2*pi*f1*(1:T)) ;
//        s2	= sign(cos(2*pi*f2*(1:T))) ;
//        s3	=      randn(1,T) ;
//        s4	= sign(randn(1,T)) ;
//
//
//        S	= [ s1 ; s2 ; s3  ] ;
        Random rand = new Random();


        double f1 = 0.013;
        double f2 = 0.025;

        this.numberOfComponents = 3;
        this.useReduced = true; // numberOfComponents < signals
        int signals = 4;
        this.cols_number_of_q_values = 200;
        double noiseLevel = -30;

        qmin = 1;
        qmax = cols_number_of_q_values;


        DenseMatrix64F sources = new DenseMatrix64F(cols_number_of_q_values, numberOfComponents);

        System.out.println("SOURCES");

        for(int i=0; i<cols_number_of_q_values; i++){
            sources.set(i,0, 0.7*Math.cos(2*Math.PI*f1*(i+1)));
            //sources.set(i,1, 0.9*Math.signum(Math.cos(2*Math.PI*f2*(i+1))) + 1);
            sources.set(i,1, 0.9*(Math.cos(2*Math.PI*f2*(i+1))) + 1);
            sources.set(i,2, rand.nextGaussian());
            System.out.println((i+1) + " " + sources.get(i,0) + " " + sources.get(i,1) + " " + sources.get(i,2));
        }

        CommonOps.transpose(sources);
//        %%%%%%%%%%%  Mixing %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//        % mixing and noising
//
//
//        % random mixing matrix
        double noiseamp = Math.pow(10, (noiseLevel/20.0));
        DenseMatrix64F a_matrix = new DenseMatrix64F(signals, numberOfComponents);
        for(int i=0; i<signals; i++){
            for(int j=0; j<numberOfComponents; j++){
                a_matrix.set(i,j,rand.nextDouble() + noiseamp);
            }
        }

        //
        DenseMatrix64F testData = new DenseMatrix64F(signals, cols_number_of_q_values);
        CommonOps.mult(a_matrix, sources, testData);

        // make XYSeriesCollection
        totalSets = signals;


        testCollection = new XYSeriesCollection();

        for (int i=0; i<signals; i++){
            testCollection.addSeries(new XYSeries(i));
            for (int j=0; j<cols_number_of_q_values; j++){
                testCollection.getSeries(i).add((j+1),  testData.get(i,j));
            }
        }

        //ArrayList<String> datatext = new ArrayList<>();
        String tempString;
        System.out.println("MIXTURE MODEL ");
        for (int j=0; j<cols_number_of_q_values; j++){
            tempString=String.valueOf(j+1);
            for (int i=0; i<signals; i++){
                tempString += " " + testCollection.getSeries(i).getY(j);
            }
            System.out.println(tempString);
        }


    }

    public void runTest(){
        this.createTestDataset();
        datasets = testCollection;


        createMeanSubtractedDatasets();
        createAMatrix();
        performSVDAMatrix();

        if (useReduced){
            whitenAndReduceDataset();
            System.out.println("In Background REDUCED");
        } else {
            whitenDataSet();
        }

        createCumulantMatrix();
        jointDiagonalizationCumulantMatrix();

        // print SAXSICA
        double q_value;
        ArrayList<String> outputLines = new ArrayList<>(cols_number_of_q_values);
        String value ="";

        for(int j=0; j<cols_number_of_q_values; j++){
            value=String.valueOf(j+1);
            for(int i=0; i<SAXSICA.getSeriesCount(); i++ ){
                value += " " + SAXSICA.getSeries(i).getY(j);
            }
            // write to array
            outputLines.add(value);
        }


        System.out.println("PRINTING SOURCES");

        for(int i=0; i<cols_number_of_q_values; i++){
            System.out.println(outputLines.get(i));
        }

    }
}
