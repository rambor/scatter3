package version3;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import net.jafama.FastMath;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.UnivariateVectorFunction;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertrambo on 11/09/2016.
 */
public class KurtosisSolver extends SwingWorker {

    private double dmax;
    private double qmax;

    private int ns;
    private int coeffs_size;

    private XYSeries standardizedData;
    private XYSeries originalData;
    private XYSeries qIQDataset;
    private double standardizedMin;
    private double standardizedRange;
    private double observations;
    private final double INV_PI = 1.0/Math.PI;
    private RealMatrix designMatrix; // assume this is the design matrix
    private boolean background = false;


    public KurtosisSolver(XYSeries data, double dmax){

        // create Moore design matrix
        this.dmax = dmax;
        this.qmax = data.getMaxX();
        this.originalData = data;
    }

    @Override
    protected Object doInBackground() throws Exception {

        this.createQIQDataset();
        this.standardizeData();
        this.createDesignMatrix();


        int total = standardizedData.getItemCount();
        double[] obs = new double[total];
        for(int i=0; i<total; i++){
            obs[i] = standardizedData.getY(i).doubleValue();
        }

        double[] guess = new double[coeffs_size];
        for(int i=0; i < coeffs_size; i++){
            guess[i] = 0;
        }

        guess[0]=0.034057049334135454;
        guess[1]=0.024347229178351488;
        guess[2]=0.008164279994363909;
        guess[3]=0.004086752481750727;
        guess[4]=0.003081462356547316;
        guess[5]=0.0021306454542842843;
        guess[6]=0.0015810430668608197;
        guess[7]=0.0014640857928108074;
        guess[8]=6.298145124561982E-4;

        LinearProblem problem = new LinearProblem(designMatrix, obs, dmax, standardizedData);
        System.out.println("Setting optimizer ");
        NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                new SimpleValueChecker(1e-6, 1e-6)
        );

        System.out.println(" optimizer ");

        PointValuePair optimum = optimizer.optimize(new MaxEval(100),
                problem.getObjectiveFunction(),
                problem.getObjectiveFunctionGradient(),
                GoalType.MINIMIZE,
                new InitialGuess(guess));


        System.out.println("Printing results : " + optimizer.getIterations());
        for(int i=0; i< coeffs_size; i++){
            System.out.println(i + " => " + optimum.getPoint()[i]);
        }

        // calculate pr
        double totalPrPoints = (double)ns*3.0;
        int r_limit = (int)totalPrPoints;
        double deltar = dmax/totalPrPoints;
        double inv_d = 1.0/dmax;
        double pi_dmax = Math.PI*inv_d;
        double r_value, pi_dmax_r, resultM;
        int totalMooreCoefficients = ns;
        double[] mooreCoefficients = optimum.getPoint();
        double inv_2d = 0.5*inv_d;

        System.out.println(ns + " SIZE " + mooreCoefficients.length + " " + totalPrPoints);
        System.out.println(0 + " " + 0);

        for (int j=1; j < r_limit; j++){

            r_value = j*deltar;
            pi_dmax_r = pi_dmax*r_value;
            resultM = 0;

            for(int i=0; i < totalMooreCoefficients; i++){
                resultM += mooreCoefficients[i]*FastMath.sin(pi_dmax_r*(i+1));
            }

            System.out.println(r_value + " " + (inv_2d * r_value * resultM));
        }

        // print residuals
        double[] calcl = designMatrix.operate(optimum.getPoint());

        double diffvalue;
        System.out.println("RESIDUALS");
        for (int i = 0; i < calcl.length; ++i) {
            diffvalue = obs[i] - calcl[i];
            System.out.println(qIQDataset.getX(i) + "   " + diffvalue);
        }


        return null;
    }

    private void createQIQDataset(){
        int rows = originalData.getItemCount();
        qIQDataset = new XYSeries("QIQ");
        XYDataItem temp;
        for (int row=0; row<rows; row++){
            temp = originalData.getDataItem(row);
            qIQDataset.add(temp.getXValue(), temp.getXValue()*temp.getYValue());
        }
    }

    private void createDesignMatrix(){

        int rows = standardizedData.getItemCount();
        System.out.println("rows " + rows);
        ns = (int) Math.round(qmax*dmax*INV_PI) + 1;  //
        System.out.println("ns " + ns);
        double dmaxPi = Math.PI*dmax;

        // create block matrix and fill
        System.out.println("matrix made " + dmax);

        if (background){
            coeffs_size = ns+1;
            designMatrix = new BlockRealMatrix(rows, coeffs_size);
            for (int row=0; row<rows; row++){
                double dmaxq = standardizedData.getX(row).doubleValue()*dmax;

                designMatrix.setEntry(row, 0, standardizedData.getX(row).doubleValue());

                for(int col=1; col<coeffs_size; col++){
                    // if using background
                    int index = col;
                    designMatrix.setEntry(row, col, Constants.TWO_DIV_PI*dmaxPi*index*FastMath.pow(-1,index+1)*FastMath.sin(dmaxq)/(Constants.PI_2*index*index - dmaxq*dmaxq));
                }
            }

        } else {

            coeffs_size = ns;
            designMatrix = new BlockRealMatrix(rows, coeffs_size);
            System.out.println("no bakcground");
            for (int row=0; row<rows; row++){
                double dmaxq = standardizedData.getX(row).doubleValue()*dmax;
                for(int col=0; col<coeffs_size; col++){
                    // if using background
                    int index = col+1;
                    designMatrix.setEntry(row, col, Constants.TWO_DIV_PI*dmaxPi*index*FastMath.pow(-1,index+1)*FastMath.sin(dmaxq)/(Constants.PI_2*index*index - dmaxq*dmaxq));
                }
            }
        }
    }

    /**
     *
     * standardize data
     */
    private void standardizeData(){

        XYDataItem tempData;
        standardizedData = new XYSeries("Standardized data");
        int totalItems = qIQDataset.getItemCount();

        standardizedMin = qIQDataset.getMinY();
        standardizedRange = qIQDataset.getMaxY() - standardizedMin;
        double invstdev = 1.0/standardizedRange;

        for(int r=0; r<totalItems; r++){
            tempData = qIQDataset.getDataItem(r);
            standardizedData.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
        }
    }


    private static class LinearProblem {

        final RealMatrix factors; // assume this is the design matrix
        final double[] target; // data
        final double invN;
        final double invN2;
        final double invN4;
        final double dmax;
        final int totalqvalues;
        final double[] qvalues;

        public LinearProblem(RealMatrix designMatrix, double[] target, double dmax, XYSeries data) {
            this.factors = designMatrix;
            this.target  = target;

            this.totalqvalues = data.getItemCount();
            this.invN =1.0/(double)totalqvalues;
            invN2 = this.invN*this.invN;
            invN4 = this.invN2*this.invN2;
            this.dmax = dmax;

            this.qvalues = new double[totalqvalues];
            for(int i=0; i<totalqvalues; i++){
                qvalues[i] = data.getX(i).doubleValue();
            }
        }


        public ObjectiveFunction getObjectiveFunction() {

            return new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    // point comes in as a vector and is multiplied by blockmatrix

                    double diffsum4=0;
                    double diffsum2=0;
                    double tanh=0;
                    double diffvalue;
                    double[] diff = factors.operate(point);

                    for (int i = 0; i < diff.length; ++i) {
                        diffvalue = target[i] - diff[i];
                        diffsum4 += diffvalue*diffvalue*diffvalue*diffvalue;
                        diffsum2 += diffvalue*diffvalue;
                        tanh += Math.tanh(diffvalue);
                    }

                    for(int p=0;p<point.length; p++){
                        System.out.println("Params " + p + " => " + point[p]);
                    }
                    System.out.println("diff length: " + diff.length + " " + (invN*diffsum4 -3.0*invN2*diffsum2*diffsum2));
                    //return (invN*diffsum4 -3.0*invN2*diffsum2*diffsum2);
                    //return diffsum2;
                    return tanh;
                }
            });
        }

        /**
         * return gradient of the parameters
         * return vector is equal to the number of parameters
         * @return
         */
        public ObjectiveFunctionGradient getObjectiveFunctionGradient() {
            return new ObjectiveFunctionGradient(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    // difference betweens
                    double[] diff = factors.operate(point);
//                    double[] r = factors.operate(point);
//                    for (int i = 0; i < r.length; ++i) {
//                        r[i] -= target[i];
//                    }
//                    double[] p = factors.transpose().operate(r);
//                    for (int i = 0; i < p.length; ++i) {
//                        p[i] *= 2;
//                    }
//
//                    return p;


                    for (int i = 0; i < diff.length; ++i) {
                        diff[i] = target[i] - diff[i];
                    }

                    int total_params = factors.getColumnDimension();
                    double[] del_p = new double[total_params];

                    double diff_value;
                    int total = diff.length;
                    double qd, sinterm;
                    double firstterm, secondterm, thirdsum;
                    double derivative;

                    for(int i=0; i<total_params;i++) {
                        //for each param, calculate gradient
                        int index = i+1;
                        double power = Math.pow(-1, index+1);
                        double pin = index*index*Constants.PI_2;

                        firstterm = 0;
                        secondterm = 0;
                        thirdsum = 0;

                        derivative=0;
                        for(int q=0; q<total; q++){
                            diff_value = diff[q];
                            qd = qvalues[q]*dmax;
                            sinterm = 2*(index)*power*dmax*Math.sin(qd)/(pin - qd*qd);

                            firstterm += diff_value*diff_value*diff_value*(-sinterm);
                            secondterm += diff_value*diff_value;
                            thirdsum += diff_value*(-sinterm);

                            derivative += 1.0 - Math.tanh(diff[q]);
                        }



                        //del_p[i] = 4*invN4*firstterm - 12*invN2*secondterm*thirdsum;
                        del_p[i] = 4*invN*firstterm - 12*invN2*secondterm*thirdsum;
                        del_p[i] = derivative;
                        System.out.println(i + " gradient " + del_p[i]);
                    }

                    return del_p;
                }
            });
        }
    }

}
