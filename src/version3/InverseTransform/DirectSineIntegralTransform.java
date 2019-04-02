package version3.InverseTransform;

import net.jafama.FastMath;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
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
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public class DirectSineIntegralTransform extends IndirectFT {

    private double del_r;
    double[] r_vector;
    int r_vector_size;
    double[] qvalues; // data
    double[] target; // data
    double[] invVariance; // data
    private RealMatrix designMatrix; // assume this is the design matrix

    public DirectSineIntegralTransform(XYSeries dataset, XYSeries errors, double dmax, double qmax, double lambda, boolean includeBackground) {
        super(dataset, errors, dmax, qmax, lambda, false, includeBackground);

        this.createDesignMatrix(this.data);
        this.setModelUsed("DirectSineIntegralTransform L2-NORM");
        this.invertStandardVariance();
        this.solve();
    }


    public DirectSineIntegralTransform(
            XYSeries dataset,
            XYSeries scaledqIqErrors,
            double dmax,
            double qmax,
            double lambda,
            double stdmin,
            double stdscale){

        super(dataset, scaledqIqErrors, dmax, qmax, lambda, false, false, stdmin, stdscale);

        XYDataItem tempData;
        standardVariance = new XYSeries("standardized error");
        int totalItems = data.getItemCount();

        double temperrorvalue;
        double invstdev = 1.0/standardizedScale;

        for(int r=0; r<totalItems; r++){
            tempData = data.getDataItem(r);
            temperrorvalue = scaledqIqErrors.getY(r).doubleValue(); // already in form of q*I(q)/scale
            standardVariance.add(tempData.getX(), temperrorvalue*temperrorvalue); // q_times_Iq_scaled
        }

        this.createDesignMatrix(dataset);

        this.setModelUsed("DirectSineIntegralTransform L2-NORM");
        this.invertStandardVariance();
        this.solve();
    }


    void solve(){

        LinearProblem problem = new LinearProblem(
                designMatrix,
                qvalues,
                target,
                invVariance,
                r_vector,
                dmax,
                lambda,
                includeBackground);

        try{
            NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
                    NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
                    new SimpleValueChecker(1e-9, -1)
            );

            double[] guess = new double[coeffs_size]; // approximate using Gaussian
            double sigma = dmax/6.0; // 0 to dmax should be 6 sigma
            double twoVar = 2.0*sigma*sigma;
            double invTwoVar = 1.0/twoVar;
            double prefactor = 1.0/Math.sqrt(Math.PI*twoVar);
            double midpoint = dmax/2.0; // midpoint is mu (average)


            if (includeBackground){
                guess[0] = 0.0000000001;
                for(int i=1; i < coeffs_size; i++){
                    double diff = (r_vector[i-1] - midpoint);
                    guess[i] = prefactor*Math.exp( -(diff*diff)*invTwoVar);
                    //guess[i] = 1;
                }
            } else {
                for(int i=0; i < coeffs_size; i++){
                    double diff = (r_vector[i] - midpoint);
                    guess[i] = prefactor*Math.exp( -(diff*diff)*invTwoVar);
//                    guess[i] = 0.01;
                }
            }


            PointValuePair optimum = optimizer.optimize(new MaxEval(30000),
                    problem.getObjectiveFunction(),
                    problem.getObjectiveFunctionGradient(),
                    GoalType.MINIMIZE,
                    new InitialGuess(guess));

            // initialize coefficient vector
            int totalCoeffs = includeBackground ? coeffs_size: coeffs_size+1;
            coefficients = new double[totalCoeffs];
            am_vector = new SimpleMatrix(coeffs_size,1);  // am is 0 column

            if (!includeBackground){
                coefficients[0] = 0; // set background to 0
                for (int j=0; j < coeffs_size; j++){
                    coefficients[j+1] = optimum.getPoint()[j];
                    am_vector.set(j, 0, optimum.getPoint()[j]);
                }
            } else { // has background
                for (int j=0; j < coeffs_size; j++){
                    coefficients[j] = optimum.getPoint()[j];
                    am_vector.set(j, 0, optimum.getPoint()[j]);
                }
            }

            totalCoefficients = coefficients.length;
            this.setPrDistribution();
            this.calculateIzeroRg();
            this.calibratePrDistribution();
        } catch (TooManyEvaluationsException ex) {
            System.out.println("TOO Few Evaluations");
        } catch (Exception e) {
            System.out.println("Exception occurred " + e.getMessage());
        }
    }

    @Override
    void calculateIzeroRg() {
        double tempRgSum = 0, tempRg2Sum=0, xaverage=0;
        //del_r = prDistribution.getX(2).doubleValue() - prDistribution.getX(1).doubleValue() ;

        XYDataItem item;
        for(int i=1; i<totalInDistribution-1; i++){ // exclude last two points?
            item = prDistribution.getDataItem(i);
            double rvalue = item.getXValue();
            tempRg2Sum += rvalue*rvalue*item.getYValue()*del_r;
            tempRgSum += item.getYValue()*del_r; // width x height => area
            xaverage += rvalue*item.getYValue()*del_r;
        }

        rg = Math.sqrt(0.5*tempRg2Sum/tempRgSum);

        double sum = coefficients[0];
        for(int j=0; j< r_vector_size; j++){
            sum +=  coefficients[j+1];
        }
        //System.out.println("SUM " + sum + " tempRgSum " + tempRgSum);
        //izero = tempRgSum*standardizedScale+standardizedMin;
        izero = sum*standardizedScale+standardizedMin;

        rAverage = xaverage/tempRgSum;
        area = tempRgSum;
    }

    @Override
    void setPrDistribution(){
        prDistribution = new XYSeries("PRDistribution");
        prDistributionForFitting = new XYSeries("OutputPrDistribution");

        totalInDistribution = r_vector_size+2;

        for(int i=0; i<totalInDistribution; i++){ // values in r_vector represent the midpoint or increments of (i+0.5)*del_r

            if ( i == 0 ) { // interleaved r-value (even)
                prDistribution.add(0, 0);
                //prDistributionForFitting.add(0,0);
            } else if (i == (totalInDistribution-1)) {
                prDistribution.add(dmax, 0);
                //prDistributionForFitting.add(dmax,0);
            } else { // odd
                int index = i-1;
                double value = coefficients[index+1];
                prDistribution.add(r_vector[index], value);
                prDistributionForFitting.add(r_vector[index], value);

//                if (value<0){
//                    System.out.println( " WARNING NEGATIVE BIN r: " + r_vector[index] + " => " + value);
//                }
                // if using background, add to each term?
                if (includeBackground){

                }
            }
        }



        totalInDistribution = prDistribution.getItemCount();

        this.description  = String.format("REMARK 265  P(r) DISTRIBUTION OBTAINED AS DIRECT INVERSE FOURIER TRANSFORM OF I(q) %n");
        this.description += String.format("REMARK 265  REGULARIZATION CONSTRAINT USES TOTAL DIFFERENCES METHOD %n");
        this.description += String.format("REMARK 265  COEFFICIENTS ARE THE HISTOGRAM HEIGHTS WITH EQUAL BIN WIDTHS %n");
        this.description += String.format("REMARK 265           BIN WIDTH (delta r) : %.4f %n", del_r);
        setSplineFunction();
    }

    /**
     * call this function before outputing pr distribution
     */
    private void setSplineFunction(){

        double[] totalrvalues = new double[totalInDistribution];
        double[] totalPrvalues = new double[totalInDistribution];

        // coefficients start at r_1 > 0 and ends at midpoint of last bin which is less dmax
        for(int i=0; i < totalInDistribution; i++){
            XYDataItem item = prDistribution.getDataItem(i);
            totalPrvalues[i] = item.getYValue();
            totalrvalues[i] = item.getXValue();
        }

        SplineInterpolator spline = new SplineInterpolator();
        splineFunction = spline.interpolate(totalrvalues, totalPrvalues);
    }

    @Override
    public double calculateQIQ(double qvalue) {
        double sum = coefficients[0]*qvalue;
        double rvalue;
        for(int j=0; j< r_vector_size; j++){
            rvalue = r_vector[j];
            sum +=  coefficients[j+1]*FastMath.sin(rvalue*qvalue) / rvalue;
        }

        return sum*standardizedScale + standardizedMin;
    }

    @Override
    public double calculateIQ(double qvalue) {
        return (this.calculateQIQ(qvalue))/qvalue;
    }

    @Override
    public double calculatePofRAtR(double r_value, double scale) {
        return (standardizedScale)*splineFunction.value(r_value)*scale;
    }

    @Override
    public void estimateErrors(XYSeries fittedData) {

    }

    @Override
    void createDesignMatrix(XYSeries datasetInuse) {
        int divisor = 5;

        ns = (int) Math.round(qmax*dmax*INV_PI)  ;  //
        r_vector_size = ns*divisor-1; // no background implies coeffs_size == ns

        coeffs_size = this.includeBackground ? r_vector_size + 1 : r_vector_size;   //+1 for constant background, +1 to include dmax in r_vector list

        rows = datasetInuse.getItemCount();    // rows
        //del_r = Math.PI/qmax; // dmax is based del_r*ns
        del_r = dmax/(double)(ns*divisor);
        bin_width = dmax/(double)ns; // bin_width is only defined by shannon number estimate

        // if I think I can squeeze out one more Shannon Number, then I need to define del_r by dmax/ns+1
        //double del_r = dmax/(double)ns;
        r_vector = new double[r_vector_size];
        qvalues= new double[rows];
        target = new double[rows];

        for(int i=0; i < r_vector_size; i++){ // last bin should be dmax
            r_vector[i] = (i+1)*del_r; // dmax is not represented in this set
        }

        /*
         * create A matrix (design Matrix)
         */
        a_matrix = new SimpleMatrix(rows, coeffs_size);
        designMatrix = new BlockRealMatrix(rows, coeffs_size);
        /*
         * y_vector is q*I(q) data
         */
        y_vector = new SimpleMatrix(rows,1);

        if (!includeBackground) { // no constant background

            for(int row=0; row < rows; row++){ //rows, length is size of data
                XYDataItem tempData = datasetInuse.getDataItem(row);

                for(int col=0; col < coeffs_size; col++){
                    double r_value = r_vector[col];
                    a_matrix.set(row, col, FastMath.sin(r_value*tempData.getXValue()) / (r_value));
                    designMatrix.setEntry(row, col, FastMath.sin(r_value*tempData.getXValue()) / (r_value));
                }
                y_vector.set(row,0,tempData.getYValue()); //set data vector
                qvalues[row] = tempData.getXValue();
                target[row] = tempData.getYValue();
            }

        } else {
            for(int row=0; row < rows; row++){ //rows, length is size of data
                XYDataItem tempData = datasetInuse.getDataItem(row);

                for(int col=0; col < coeffs_size; col++){
                    if (col == 0){ // constant background term
                        a_matrix.set(row, 0, tempData.getXValue());
                        designMatrix.setEntry(row, 0, tempData.getXValue());
                        //a_matrix.set(row, 0, 1);
                    } else { // for col >= 1
                        double r_value = r_vector[col-1];
                        a_matrix.set(row, col, FastMath.sin(r_value*tempData.getXValue()) / r_value);
                        designMatrix.setEntry(row, col, FastMath.sin(r_value*tempData.getXValue()) / r_value);
                    }
                }
                y_vector.set(row,0,tempData.getYValue()); //set data vector
                qvalues[row] = tempData.getXValue();
                target[row] = tempData.getYValue();
            }
        }
    }


    private static class LinearProblem {

        final RealMatrix factors; // assume this is the design matrix
        final double[] obs; // data
        final double[] invVariance;

        final double dmax, invDmax;
        final int totalqvalues;
        final double[] qvalues;
        final double[] rvalues;
        final double weight;
        final boolean useBackground;
        final double invP;
        final int totalP, totalRvalues, lastP;
        final int lastRvalue;

        public LinearProblem(RealMatrix designMatrix, double[] qvalues, double[] target, double[] invVar, double[] regularrvector, double dmax, double weight, boolean useBackGround) {

            this.factors = designMatrix; // first term will be background if included
            this.qvalues = qvalues;
            this.obs  = target;
            this.invVariance = invVar;
            this.weight = weight;
            this.rvalues = regularrvector;
            this.totalRvalues = rvalues.length;
            lastRvalue = totalRvalues-1;

            this.totalP = designMatrix.getColumnDimension();
            this.invP = 1.0/(double)totalP;

            this.lastP = totalP-1;
            this.totalqvalues = qvalues.length;

            this.dmax = dmax;
            this.invDmax = 1.0/dmax;

            this.useBackground = useBackGround;
        }


        /*
         *
         * Perform a residual weighted minimization with a total difference regularization term
         * variance weight is applied to each residual
         */
        public ObjectiveFunction getObjectiveFunction() {

            return new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    // point comes in as a vector and is multiplied by blockmatrix
                    double seconddiff=0;
                    double objective=0;
                    double residual;
                    double[] calc = factors.operate(point);

                    for (int i = 0; i < calc.length; ++i) {
                        residual = obs[i] - calc[i];
                        objective += residual*residual*invVariance[i];
                    }

                    double sum=0;

                    // calculate second derivative at each point
                    // background is constant, makes no contribution to smoothness of P(r) distribution
                    if (useBackground){
                        // add first term

                    } else {

                        double baseP, nextP, diff;
                        /*
                         * minimize lambda*|total_diff [P_(i) - P(j_next)| for all j != i
                         */
                        for(int i=0; i<totalP; i++){
                            diff = point[i];
                            sum += diff*diff;
                        }

                        sum *= 2;

                        for(int i=0; i<totalP; i++){
                            baseP=point[i];

                            for(int j=(i+1); j<totalP; j++){
                                nextP=point[j];
                                diff = baseP - nextP;
                                sum += diff*diff;
                            }
                        }

                        /*
                         * minimize gradient of Sum (qI_obs - qI_calc)^2 + |P_(i+1) - P_(i)|^2
                         */
//                        int stopAt = totalP-1;
//                        sum = point[0]*point[0] + point[stopAt]*point[stopAt]; // represents the difference between first point and 0 and last point and dmax
//
//                        for(int i=0; i<stopAt; i++){
//                            diff = point[i] - point[i+1];
//                            sum += diff*diff;
//                        }

                    }
                    // add last term to sum
                    return objective + weight*sum;
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
                    int total_params = factors.getColumnDimension();
                    double[] del_p = new double[total_params];

                    double[] calc = factors.operate(point);
                    int calcLength = calc.length;
                    double[] residualsInvVar = new double[calcLength];

                    /*
                     * Calculate Residuals as (qI_obs - qI_calc)/invVariance[q]
                     */
                    for (int q = 0; q < calcLength; ++q) {
                        residualsInvVar[q] = (obs[q]-calc[q])*invVariance[q];
                    }


                    if (useBackground){
                        // add first term (background)


                    } else {
                        /*
                         * minimize gradient of Sum (qI_obs - qI_calc)^2 + |P[r_(i+1)] - P_r(i)]|^2
                         */
                        for(int p=0; p < totalP; p++){
                            double sum = 0;
                            for (int q = 0; q < calcLength; ++q) { // over all intensities, constant column position, sum down the row
                                sum += residualsInvVar[q]*factors.getEntry(q,p);
                            }
                            del_p[p] = -2*sum;
                        }

                        /*
                         * gradient for the total differential
                         */
                        double innersum;

                        for(int index=0; index < totalP; index++){ // for a given coefficient, must sum over each difference

                            innersum=0;
                            for(int i=0; i<index; i++){ // for a given coefficient, must sum over each difference
                                innersum += point[index];
                            }

                            for(int i=index+1; i<totalP; i++){ // for a given coefficient, must sum over each difference
                                innersum += point[index];
                            }
                            del_p[index] += weight*(4*point[index] + 2*(totalP-1)*point[index] - 2*innersum);
                        }

                        /*
                         * minimize on neighboring differences SUM (p_i - p_[i+1])^2
                         */
//                        int stopAt = totalP-1;
//                        del_p[0] += weight*(4*point[0] - 2*point[1]);
//                        del_p[stopAt] += weight*(4*point[stopAt] - 2*point[stopAt-1]);
//                        for(int i=1; i<stopAt; i++){
//                            del_p[i] += weight*(4*point[i] - 2*point[i-1] - 2*point[i+1]);
//                        }
                    }

                    return del_p;
                }
            });
        }
    }

    @Override
    public void normalizeDistribution(){
        double sum=0;
        for(int i=0; i < totalInDistribution; i++){
            XYDataItem item = prDistribution.getDataItem(i);
            sum += item.getYValue();
        }

        sum *= del_r; //area
        double invSum = 1.0/sum;

        for(int i=0; i < totalInDistribution; i++){
            XYDataItem item = prDistribution.getDataItem(i);
            prDistribution.updateByIndex(i, item.getYValue()*invSum);
        }
    }

    /*
     * precalculate standardVariance
     * used in the Apache Solver
     *
     */
    private void invertStandardVariance(){
        int totalItems = standardVariance.getItemCount();
        invVariance = new double[totalItems];

        for(int r=0; r<totalItems; r++){
            invVariance[r] = 1.0/standardVariance.getY(r).doubleValue();
        }
    }


    private void standardizeErrors(){
        XYDataItem tempData;
        standardVariance = new XYSeries("standardized error");
        int totalItems = data.getItemCount();

        double temperrorvalue;
        double invstdev = 1.0/standardizedScale;

        for(int r=0; r<totalItems; r++){
            tempData = data.getDataItem(r);
            temperrorvalue = errors.getY(r).doubleValue(); // already in form of q*I(q)/scale
            standardVariance.add(tempData.getX(), temperrorvalue*temperrorvalue); // q_times_Iq_scaled
        }
    }
}
