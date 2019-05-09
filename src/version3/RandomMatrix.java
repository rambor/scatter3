package version3;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import version3.RealSpace;

import java.util.ArrayList;
import java.util.Random;

public class RandomMatrix {

    private final int totalPts;
    public SimpleMatrix a_matrix;
    private XYSeries allData, scaledData;
    private double standardizedMin, standardizedScale;
    private ArrayList<Double> stats = new ArrayList<>();
    private XYSeries stationary = new XYSeries("ns");

    public RandomMatrix(RealSpace dataset, WorkingDirectory dir){
        this.allData = dataset.getAllData();
        this.totalPts = dataset.getAllData().getItemCount();

        this.fillMatrix();
    }

    private void fillMatrix(){
        a_matrix = new SimpleMatrix(totalPts, totalPts);
        double value1, value2, sqrt;

        /*
         * scale the data so that is it
         */
        double sum = 0, minavg=10000000, tempminavg;
        int windowSize = 17;
        double invwindow = 1.0/(double)windowSize;
        for(int r=0; r<(totalPts-windowSize); r++){
            tempminavg = 0;
            for (int w=0; w<windowSize; w++){
                tempminavg += allData.getDataItem(r).getYValue();
            }

            if (tempminavg < minavg){
                minavg = tempminavg;
            }
        }

        // rescale the data
        standardizedMin = minavg*invwindow;//nonData.getMinY();
        standardizedScale = Math.abs(allData.getMaxY() - standardizedMin);
        double invstdev = 1.0/standardizedScale;

        scaledData = new XYSeries("scaled");
        for(int i=0; i<totalPts; i++){
            XYDataItem tempData = allData.getDataItem(i);
            //scaledData.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
            scaledData.add(tempData);
        }

        Random rand = new Random();

        for(int i=0; i<totalPts; i++){
            value1 = scaledData.getY(i).doubleValue();
            for(int j=(i+1); j<totalPts; j++){
                value2 = value1 - scaledData.getY(j).doubleValue(); // calculate difference
//                value2 = rand.nextGaussian();
//                sqrt = Math.sqrt(value2*value2);
//                a_matrix.set(i,j, sqrt);
//                a_matrix.set(j,i, sqrt);
                a_matrix.set(i,j, value2);
                a_matrix.set(j,i, value2);
            }
            a_matrix.set(i,i,0);
        }






        // check boxed regions ,if eigenvalues are completely within distribuition then random

        // if not, suggests a signal


        /*
         * Calculate distance matrix
         *
         * Need to define a minimium window size, mw
         * Grab all values indices (n - mw)
         *
         *   1 2 3 4 5 6 7 . . . n
         * 1 0
         * 2   0
         * 3     0
         * 4       0
         * 5         0
         * 6           0
         * 7             0
         * .
         * .
         * .
         * n                     0
         *
         * for each window, calculate
         */


        int minWindow = 51;
        int stopAt = totalPts-minWindow;
        // center each row
        for(int row=0; row<stopAt; row++){

            double sumit = 0;
            double sumit2 = 0;
            double countit = 0.0d;
            for(int col=(row+1); col<totalPts; col++){
                double value = a_matrix.get(row, col);
                sumit += value;
                sumit2 += value*value;
                countit += 1.0d;
            }

            double tavg = sumit/countit;
            double tavg2 = sumit2/countit;
            double invvar = 1.0/Math.sqrt(tavg2 - tavg*tavg);
            for(int col=(row+1); col<totalPts; col++){
                double value = a_matrix.get(row, col);
                a_matrix.set(row, col, (value-tavg)*invvar);
            }
        }



        for(int startRow=minWindow; startRow<stopAt; startRow += 10){

            SimpleMatrix test_matrix = a_matrix.extractMatrix(0, startRow, startRow, totalPts);

            // calculate mean and variance
//            double testsum = test_matrix.elementSum();
//            double testavg = testsum/(double)test_matrix.getNumElements();
//
//            SimpleMatrix squared = test_matrix.elementPower(2);
//            double testsum2 = squared.elementSum();
//            double testavg2 = testsum2/(double)squared.getNumElements();
//            double testvariance = testavg2 - testavg*testavg;
//
//            // center the matrix and set to unit variance
//            SimpleMatrix scaledMatrix = test_matrix.minus(testavg).scale(1.0/Math.sqrt(testvariance));

            SimpleMatrix aTa = test_matrix.mult(test_matrix.transpose());
//            SimpleMatrix aTa = scaledMatrix.mult(scaledMatrix.transpose());
            //aTa = aTa.scale(1.0/(double)aTa.numRows());

            SimpleSVD svd = aTa.svd();
            double[] values = svd.getSVD().getSingularValues();

            aTa.printDimensions();
            // make histogram
            System.out.println("SVD round " + startRow +  " => " + (values[0]/values[1]));
            for(int m=0;m<5;m++){
                System.out.println("round " + startRow + " " + m + " " + values[m]);
            }
        }




        // divide into bins start with 2 bins and keep increasing
//        for (int i=1; i < stopAt; i++){
//
//            stats.add(0.0d);
//
//            for(int row=0; row< stopAt; row++){
//
//                value = a_matrix.get(row, i);
//                denominator = value*value; // sum of (x_t)^2
//                numerator=0;
//
//                for(int col=(i+1); col<totalPts; col++) {
//                        value = a_matrix.get(row, col);
//                        diff = value - a_matrix.get(row, col-1); // x_(t) - x_(t-1)
//                        numerator += diff*diff;
//                        denominator += value*value; // sum of (x_t)^2
//                }
//
//                stats.set(i-1, stats.get(i-1) + 2-numerator/denominator);
//            }
//        }

//        for (int i=0; i< stats.size(); i++){
//            System.out.println(i + " " + stats.get(i));
//        }


        //org.ejml.interfaces.decomposition.EigenDecomposition eigen = a_matrix.eig().getEVD();
//        SimpleMatrix aTa = a_matrix.mult(a_matrix.transpose());
//        //SimpleEVD evd = aTa.eig();
//        SimpleEVD evd = a_matrix.eig();
//        int totaltogo = evd.getNumberOfEigenvalues();
//
//
//        ArrayList<Double> cond = new ArrayList<>();
//        for(int i=0; i<totaltogo; i++){
//           cond.add(evd.getEigenvalue(i).getReal());
//            System.out.println(i + " " + evd.getEigenvalue(i).getReal());
//        }

    }


    private void makeStationary(){

        double denominator, numerator, value, diff;
        double sumVar=0, sumVarsq=0;
        int counter = 0;
        for(int i=2; i<totalPts; i++) {
            XYDataItem tempData = allData.getDataItem(i);
            //stats.add(tempData.getYValue() - 2*allData.getY(i-1).doubleValue() + allData.getY(i).doubleValue());
            stationary.add(tempData.getX(), tempData.getYValue() - 2*allData.getY(i-1).doubleValue() + allData.getY(i).doubleValue());
//            stationary.add(tempData.getX(), tempData.getYValue() - allData.getY(i-1).doubleValue());
            double val = stationary.getY(counter).doubleValue();
            sumVar += val;
            sumVarsq += val*val;
            counter += 1;
        }

        System.out.println("Stationary ");
        for(int i=0; i<stationary.getItemCount(); i++) {
            XYDataItem tempData = stationary.getDataItem(i);
            String out=String.format("%.6f %.4E",tempData.getXValue(),tempData.getYValue());
            System.out.println(out);
        }

        // calculate ACF
        double avg = sumVar/(double)counter;
        double baseVarianec = (sumVarsq/(double)counter - avg*avg);
        XYSeries acfSet = new XYSeries("ACF");

        int maxLag = stationary.getItemCount()/2 - 1;
        double val1, val2;
        for(int i=0; i<maxLag; i++){
            int lag = i;
            double acf = 0;
            denominator=0;
            for(int j=0; j<(stationary.getItemCount()-i); j++) {
                val1 = stationary.getDataItem(j).getYValue() - avg;
                acf += val1*(stationary.getDataItem(j+lag).getYValue() - avg);
                denominator += val1*val1;
            }
            acfSet.add(lag, (acf/(double)counter)/baseVarianec);
        }

        System.out.println("ACF");
        for(int i=0; i<acfSet.getItemCount(); i++){
            System.out.println((i+1) + " " + acfSet.getY(i).doubleValue());
        }
    }
}
