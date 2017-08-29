package version3.InverseTransform;

import net.jafama.FastMath;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import version3.Constants;
import version3.Functions;
import version3.Interpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SineIntegralTransform extends IndirectFT {

    private double del_r;
    double[] r_vector;
    int r_vector_size;

    // Dataset should be standardized and in form of [q, q*I(q)]
    public SineIntegralTransform(XYSeries dataset, XYSeries errors, double dmax, double qmax, double lambda, boolean useL1, int cBoxValue, boolean includeBackground) {
        super(dataset, errors, dmax, qmax, lambda, useL1, cBoxValue, includeBackground);

        this.createDesignMatrix(this.data);
        this.rambo_coeffs_L1();

        this.setModelUsed("DIRECT L1-NORM BINS");
       // System.out.println("L1 NORM RAMBO " + includeBackground);
    }

    /**
     *
     * @param dataset standardized data
     * @param errors
     * @param dmax
     * @param qmax
     * @param lambda
     * @param useL1
     * @param cBoxValue
     * @param includeBackground
     * @param stdmin
     * @param stdscale
     */
    public SineIntegralTransform(
            XYSeries dataset,
            XYSeries errors,
            double dmax,
            double qmax,
            double lambda,
            boolean useL1,
            int cBoxValue,
            boolean includeBackground,
            double stdmin,
            double stdscale){

        super(dataset, errors, dmax, qmax, lambda, cBoxValue, useL1, includeBackground, stdmin, stdscale);

        this.createDesignMatrix(dataset);
        this.rambo_coeffs_L1();
        this.setModelUsed("DIRECT IFT L1-NORM BINS");
    }

    /**
     * Copy constructor.
     */
    public SineIntegralTransform(SineIntegralTransform toCopy) {
        this(toCopy.data, toCopy.errors, toCopy.dmax, toCopy.qmax, toCopy.lambda, toCopy.useL1, toCopy.multiplesOfShannonNumber, toCopy.includeBackground, toCopy.standardizedMin, toCopy.standardizedScale);
        nonData = new XYSeries("nonstandard");
        for (int i=0; i<this.data.getItemCount(); i++){
            XYDataItem item = this.data.getDataItem(i);
            nonData.add(item.getX(), item.getYValue()*standardizedScale+standardizedMin);
        }
        //any no defensive copies to be created here?
        //what are the mutable object fields?
    }


    public void createDesignMatrix(XYSeries datasetInuse){
        ns = (int) Math.ceil(qmax*dmax*INV_PI)  ;  //
        coeffs_size = this.includeBackground ? ns + 1 : ns;   //+1 for constant background, +1 to include dmax in r_vector list
        rows = datasetInuse.getItemCount();    // rows

        r_vector_size = ns; // no background implies coeffs_size == ns

        //del_r = Math.PI/qmax; // dmax is based del_r*ns
        del_r = dmax/(double)ns;

        // if I think I can squeeze out one more Shannon Number, then I need to define del_r by dmax/ns+1
        //double del_r = dmax/(double)ns;
        r_vector = new double[r_vector_size];

        for(int i=0; i < r_vector_size; i++){ // last bin should be dmax
            //r_vector[i] = (i+1)*del_r;
            r_vector[i] = (0.5 + i)*del_r; // dmax is not represented in this set
        }

        /*
         * create A matrix (design Matrix)
         */
        a_matrix = new SimpleMatrix(rows, coeffs_size);
        /*
         * y_vector is q*I(q) data
         */
        y_vector = new SimpleMatrix(rows,1);

        if (!includeBackground) { // no constant background

            for(int row=0; row < rows; row++){ //rows, length is size of data
                XYDataItem tempData = datasetInuse.getDataItem(row);

                for(int col=0; col < coeffs_size; col++){
                        double r_value = r_vector[col];
                        a_matrix.set(row, col, FastMath.sin(r_value*tempData.getXValue()) / r_value);
                }
                y_vector.set(row,0,tempData.getYValue()); //set data vector
            }
        } else {
            for(int row=0; row < rows; row++){ //rows, length is size of data
                XYDataItem tempData = datasetInuse.getDataItem(row);

                for(int col=0; col < coeffs_size; col++){
                    if (col == 0){ // constant background term
                        //a_matrix.set(row, 0, tempData.getXValue());
                        a_matrix.set(row, 0, 1);
                    } else { // for col >= 1
                        double r_value = r_vector[col-1];
                        a_matrix.set(row, col, FastMath.sin(r_value*tempData.getXValue()) / r_value);
                    }
                }
                y_vector.set(row,0,tempData.getYValue()); //set data vector
            }
        }
    }


    /**
     * initialize Coefficient vector am for A*am_vector = y_vector
     *
     */
    private void initializeCoefficientVector(){
        am_vector = new SimpleMatrix(coeffs_size,1);  // am is 0 column
        //Gaussian guess = new Gaussian(dmax*0.5, 0.2*dmax);

        if (!includeBackground) { // no constant background
            for (int i=0; i < coeffs_size; i++){
                //am_vector.set(i, 0, guess.value(r_vector[i]));
                am_vector.set(i, 0, 0.00001); // initialize coefficient vector a_m to zero
            }
        } else {
            //am_vector.set(0,0,0.000000001); // set background constant, initial guess could be Gaussian
            am_vector.set(0,0,1); // set background constant, initial guess could be Gaussian
            for (int i=1; i < coeffs_size; i++){
                //am_vector.set(i, 0, guess.value(r_vector[i-1]));
                am_vector.set(i, 0, 1);
            }
        }
    }

    /**
     *
     * @return ArrayList<double[]> [coeffs] [r-values]
     */
    private void rambo_coeffs_L1(){

        initializeCoefficientVector();

        double t0 = Math.min(Math.max(1, 1.0/lambda), coeffs_size/0.001);
        double pitr = 0, pflg = 0, gap;
        double inv_t, t = t0;
        double s = Double.POSITIVE_INFINITY;
        //Hessian and preconditioner
        SimpleMatrix d1 = new SimpleMatrix(coeffs_size, coeffs_size);
        SimpleMatrix hessian;
        ArrayList<SimpleMatrix> answers;
        SimpleMatrix p_am_r2;

        /*
         * initialize u vector with 1's
         * size must include a_o and r_limit
         */
        DenseMatrix64F utemp = new DenseMatrix64F(coeffs_size,1);
        CommonOps.fill(utemp,1);

        SimpleMatrix a_transpose = a_matrix.transpose();
        SimpleMatrix gradphi0;
        SimpleMatrix gradx = new SimpleMatrix(coeffs_size,1);

        // for Preconditioner
        SimpleMatrix laplacian = (a_transpose.mult(a_matrix)).scale(2.0);

        SimpleMatrix dx = new SimpleMatrix(coeffs_size,1);

        /*
         * Backtracking Line Search
         */
        SimpleMatrix z;
        SimpleMatrix nu;
        SimpleMatrix new_z;
        SimpleMatrix new_x = new SimpleMatrix(coeffs_size,1);

        SimpleMatrix midf;
        midf = new SimpleMatrix(coeffs_size,1);
        for (int i=0; i < coeffs_size; i++){
            midf.set(i,0,-1*am_vector.get(i));
        }
        SimpleMatrix new_f = new SimpleMatrix(coeffs_size,1);

        int lsiter;
        double minAnu;
        double normL1, coeffsMu = (double)coeffs_size*MU;

        double phi, new_phi, logfSum, gdx, normg, pcgtol;
        double invdiff;

        calculationLoop:
        for (int ntiter=0; ntiter < max_nt_iter; ntiter++){

            z = (a_matrix.mult(am_vector)).minus(y_vector);

            //------------------------------------------------------------
            // Calculate Duality Gap
            //------------------------------------------------------------
            nu = z.scale(2.0);

            // get max of At*nu
            minAnu = min(a_transpose.mult(nu));
            if (minAnu < -lambda){
                nu = nu.scale(lambda/(-minAnu));
            }

            /*
             * calculate second derivative P(r) at specified r-values
             * length is the size of r_limit
             * ignore first element, a_0, of am vector (constant background)
             */
            //normL1 = normL1(am_vector);
            normL1 = am_vector.elementSum();
            pobj = (z.transpose().mult(z)).get(0,0) + (lambda * normL1);

            /*
             *  dobj  =  max(-0.25*nu'*nu-nu'*y,dobj);
             */
            dobj = Math.max(( (nu.transpose().mult(nu)).get(0,0)*(-0.25) - ((nu.transpose().mult(y_vector))).get(0,0) ), dobj);

            // dobj = Math.max(( (nu.transpose().mult(nu)).get(0,0)*(-0.25) - ((nu.transpose().mult(y))).get(0,0) ), dobj);
            gap   = pobj - dobj;

            //------------------------------------------------------------
            //       Shall we Stop?
            //------------------------------------------------------------
            //System.out.println(ntiter + " " + (gap/dobj) +  " GAP: " + gap + " : pobj " + pobj + " | dobj " + dobj + " step " + s + " PITR " + pitr);
            if (gap/Math.abs(dobj) < reltol) {
                status = ("Solved => GAP: " + gap + " : " + " | ratio " + gap/dobj + " reltol " + reltol + " " + pitr);
               // System.out.println(status);
                break calculationLoop;
            }

            //------------------------------------------------------------
            //       UPDATE t
            //------------------------------------------------------------
            if (s >= 0.5){
                t = Math.max(Math.min(coeffsMu/gap, MU*t), t);
            }
            inv_t = 1.0/t;

            //------------------------------------------------------------
            //      CALCULATE NEWTON STEP
            //------------------------------------------------------------
            //
            p_am_r2 = am_vector.elementMult(am_vector);

            /*
             * Partitioned Matrix terms for Hessian
             * D1: n x n
             */
            for(int row=0; row < coeffs_size; row++){
                invdiff = 1.0/p_am_r2.get(row,0)*inv_t;
                d1.set(row,row, invdiff);
            }

            /*
             * Gradient
             * gradphi = [At*(z*2) + lambda  - 1/t*1/x; lambda*ones(n,1)-(q1+q2)/t];
             */
            gradphi0 = a_transpose.mult(z.scale(2.0));

            for (int row=0; row < coeffs_size; row++){
                invdiff = inv_t/am_vector.get(row,0);
                gradx.set(row, 0, gradphi0.get(row,0) + lambda - invdiff);
            }

            normg = gradx.normF();
            pcgtol = Math.min(0.1, eta*gap/Math.min(1,normg));

            if (ntiter != 0 && pitr == 0){
                pcgtol = 0.1*pcgtol;
            }

            //diagxtx.plus(d1)
            hessian = hessphi_coeffs_positivity_constrained(laplacian, d1, coeffs_size);
            /*
             *
             */
            answers = linearPCGPositiveOnly(hessian, gradx.scale(-1.0), dx, d1, pcgtol, pcgmaxi);

            dx = answers.get(0);
            pitr = answers.get(2).get(0,0);

            /*
             *----------------------------------------------
             * Backtrack Line search
             *----------------------------------------------
             */
            logfSum = 0.0;
            for(int fi=0; fi < midf.numRows(); fi++){
                logfSum += FastMath.log(-midf.get(fi));
            }

            phi = (z.transpose().mult(z)).get(0,0) + lambda * am_vector.elementSum() - logfSum*inv_t;

            s=1.0;
            gdx = (gradx.transpose()).mult(dx).get(0,0);

            backtrackLoop:
            for (lsiter=0; lsiter < max_ls_iter; lsiter++){

                new_x = am_vector.plus(dx.scale(s));

                for(int ff=0; ff < coeffs_size; ff++){
                    new_f.set(ff, 0, -new_x.get(ff,0));
                }

                if (max(new_f) < 0){

                    new_z = (a_matrix.mult(new_x)).minus(y_vector);
                    logfSum = 0.0;

                    for(int fi=0; fi<new_f.getNumElements(); fi++){
                        logfSum += FastMath.log(-new_f.get(fi));
                    }

                    new_phi = (new_z.transpose().mult(new_z)).get(0,0)+lambda*new_x.elementSum()-logfSum*inv_t;

                    if (new_phi-phi <= alpha*s*gdx){
                        //System.out.println("Breaking BackTrackLoop");
                        break backtrackLoop;
                    }
                }

                s = beta*s;
            } // end backtrack loop

            if (lsiter == max_ls_iter){
                System.out.println("Max LS iteration: Failed");
                break calculationLoop;
            }

            midf = new_f.copy();
            am_vector = new_x.copy();
        }

        totalCoefficients = includeBackground ? coeffs_size: coeffs_size + 1;

        coefficients = new double[totalCoefficients];
        if (!includeBackground){
            coefficients[0] = 0; // set background to 0
            for (int j=1; j < totalCoefficients; j++){
                coefficients[j] = am_vector.get(j-1,0);
                //System.out.println(j + " COEFFS " + coefficients[j]);
            }
        } else {
            for (int j=0; j < coeffs_size; j++){
                coefficients[j] = am_vector.get(j,0);
            }
        }

        r_values = new double[r_vector_size+2];

        // populate r-values
        // first value is 0, last value is dmax
        r_values[0] = 0;
        for(int j=0; j< r_vector_size; j++){
            r_values[j+1] = r_vector[j]; //
        }

        double secondToLastRvalue = r_vector[r_vector_size-2]; // last value of r-vector
        double redo = 0.5*(dmax - secondToLastRvalue);
       // r_values[ r_values.length - 3] = secondToLastRvalue + 0.5*redo;
        r_values[ r_values.length - 2] = secondToLastRvalue + redo;
        r_values[ r_values.length - 1 ] = dmax;


        // P(dmax) should be zero.  So, we introduce a mid point between second to Last value and dmax to hold value of the last bin

        this.setPrDistribution();
        this.calculateIzeroRg();
        // I_calc based standardized data
        SimpleMatrix tempResiduals = a_matrix.mult(am_vector).minus(y_vector);
        coeffs_size = coefficients.length; // this resets the coefficients to possibly include background term as first element

        // calculate residuals
        residuals = new XYSeries("residuals");
        for(int i=0; i < rows; i++){
            XYDataItem item = data.getDataItem(i); // isn't quite correct, using data to get q-values, should be based on input for making design matrix
            residuals.add(item.getX(), tempResiduals.get(i,0));
        }
    }


    @Override
    public double calculatePofRAtR(double r_value, double scale){
        return (inv2PI2*standardizedScale)*splineFunction.value(r_value)*scale;
    }

    @Override
    void calculateIzeroRg() {
        double tempRgSum = 0, tempRg2Sum=0, xaverage=0;
        //del_r = prDistribution.getX(2).doubleValue() - prDistribution.getX(1).doubleValue() ;

        XYDataItem item;
        for(int i=0; i<totalInDistribution-2; i++){ // exclude last two points?
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

        //izero = (sum + standardizedMin)*standardizedScale;
        izero = sum*standardizedScale+standardizedMin;
        rAverage = xaverage/tempRgSum;
        area = tempRgSum;
    }



    @Override
    void setPrDistribution(){
        prDistribution = new XYSeries("PRDistribution");

        totalInDistribution = r_vector_size+2;

        for(int i=0; i<totalInDistribution; i++){

            if ( i == 0 ) { // interleaved r-value (even)
                prDistribution.add(0, 0);
            } else if (i == (totalInDistribution-1)) {
                prDistribution.add(dmax, 0);
            } else { // odd
                int index = i-1;
                prDistribution.add(r_vector[index], coefficients[index+1]);
            }
            //System.out.println(i + " " + prDistribution.getX(i) + " => " + prDistribution.getY(i));
        }
        // add an extra point for the spline interpolation at end, purely for rendering reasons
        XYDataItem secondToLastItem = prDistribution.getDataItem(totalInDistribution-2);
        double delta = dmax - secondToLastItem.getXValue();

        prDistribution.add(secondToLastItem.getXValue() + 0.5*delta, secondToLastItem.getYValue()*0.5);


//        prDistribution.add(0,0);
//        //System.out.println("Distribution set :");
//        // coefficients start at r_1 > 0 and ends at midpoint of last bin which is less dmax
//        for(int i=1; i < totalCoefficients; i++){
//            //prDistribution.add(del_r*(0.5+i-1), coefficients[i]);
//            prDistribution.add(r_values[i], coefficients[i]); // first value is background for coeffs
//            System.out.println(i + " " + r_values[i] + " " + coefficients[i]);
//        }
//        //double lastvalue = prDistribution.getY((prDistribution.getItemCount()-1)).doubleValue();
//        //prDistribution.add(r_values[totalInDistribution-3], 0.75*lastvalue); // should be dmax
//        //prDistribution.add(r_values[totalInDistribution-2], 0.5*lastvalue); // should be dmax
//        prDistribution.add(r_values[totalInDistribution-1], 0); // should be dmax

//        for(int i=0; i < totalInDistribution; i++){
//            System.out.println(prDistribution.getX(i) + " " +  prDistribution.getY(i));
//        }


        // create a list of r-values with interleaved values

//        totalInDistribution = 2*r_vector_size + 1 ;
//
//        for(int i=0; i<totalInDistribution; i++){
//
//            if ( (i & 1) == 0 ) { // interleaved r-value (even)
//
//                if (i==0) {
//                    prDistribution.add(0, 0);
//                } else if (i == (totalInDistribution-1)) {
//                    prDistribution.add(dmax, 0);
//                } else {
//                    int priorIndex = i/2; // divide by two to map to r_vector
//                    // need the coefficients that neighbor the boundary point
//                    double interpolatedValue = 0.5*(coefficients[priorIndex] + coefficients[priorIndex+1]);
//                    prDistribution.add(priorIndex*del_r, interpolatedValue);
//                }
//
//            } else { // odd
//                int index = (i-1)/2;
//                prDistribution.add(r_vector[index], coefficients[index+1]);
//            }
//            System.out.println(i + " " + prDistribution.getX(i) + " => " + prDistribution.getY(i));
//        }

        totalInDistribution = prDistribution.getItemCount();

        this.description  = String.format("REMARK 265  P(r) DISTRIBUTION OBTAINED AS DIRECT INVERSE FOURIER TRANSFORM OF I(q) %n");
        this.description += String.format("REMARK 265  COEFFICIENTS ARE THE HISTOGRAM HEIGHTS WITH EQUAL BIN WIDTHS %n");
        this.description += String.format("REMARK 265           BIN WIDTH (delta r) : %.5E %n", coefficients[0]);
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

        double sum = coefficients[0];
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
    public void estimateErrors(XYSeries fittedqIq){

        double[] oldCoefficients = new double[totalCoefficients];
        for(int i=0; i<totalCoefficients; i++){ // copy old coefficients temporarily
            oldCoefficients[i] = coefficients[i]; // will be over written in the estimate
        }

        XYSeries tempPr = new XYSeries("tempPr");
        for(int i=0; i<prDistribution.getItemCount(); i++){
            tempPr.add(prDistribution.getDataItem(i));
        }

        int size = fittedqIq.getItemCount();
        double upperq = fittedqIq.getMaxX();
        double bins = totalCoefficients-1;
        double delta_q = upperq/bins;
        double samplingLimit;

        //del_r = prDistribution.getX(2).doubleValue()  - prDistribution.getX(1).doubleValue() ;

        XYDataItem tempData;
        int totalRuns = 31;
        double[] rgValues = new double[totalRuns];
        double[] izeroValues = new double[totalRuns];

        ArrayList<Integer> countsPerBin = new ArrayList<>();
        int startbb = 0;

        // determine counts per bin to draw from
        // standardize data using specified mean and stdev
        double invStdev = 1.0/standardizedScale;

        for (int b=1; b <= bins; b++) {
            int sumCount = 0;
            double upperBound = delta_q*b;

            binloop:
            for (int bb=startbb; bb < size; bb++){
                double tempCurrent = fittedqIq.getX(bb).doubleValue();

                if ( (tempCurrent >= (delta_q*(b-1)) ) && (tempCurrent < upperBound) ){
                    sumCount += 1.0;
                } else if (tempCurrent >= upperBound ) {
                    startbb = bb;
                    break binloop;
                }
            }
            // you want a better algorithm, by a bad computer
            // greens theorem surface integral
            countsPerBin.add((int)sumCount);
        }

        XYSeries randomSeries = new XYSeries("Random-");

        int upperbb, locale;
        Random randomGenerator = new Random();
        int[] randomNumbers;

        for (int i=0; i < totalRuns; i++){

            randomSeries.clear();
            // randomly grab from each bin
            startbb = 0;
            upperbb = 0;

            for (int b=1; b <= bins; b++){
                // find upper q in bin
                // if s-to-n of bin < 1.5, sample more points in bin
                // if countPerBin < 5, randomly pick 1
                // what if countPerBin = 0?
                if (countsPerBin.get(b-1) > 0){

                    samplingLimit = (1.0 + randomGenerator.nextInt(17))/100.0;  // return a random percent up to ...
                    binloop:
                    for (int bb=startbb; bb < size; bb++){
                        if (fittedqIq.getX(bb).doubleValue() >= (delta_q*b) ){ // what happens on the last bin?
                            upperbb = bb;
                            break binloop;
                        }
                    }

                    // grab indices inbetween startbb and upperbb
                    randomNumbers = Functions.randomIntegersBounded(startbb, upperbb, samplingLimit);
                    startbb = upperbb;

                    // standardize the data
                    for(int h=0; h<randomNumbers.length; h++){
                        locale = randomNumbers[h];
                        tempData = fittedqIq.getDataItem(locale);
                        randomSeries.add(tempData.getXValue(), (tempData.getYValue()-standardizedMin)*invStdev);
                        //randomSeries.add(tempData.getXValue(), (tempData.getYValue()/tempData.getXValue()-standardizedMin)*invStdev*tempData.getXValue());
                    }
                } // end of checking if bin is empty
            }

            // calculate PofR
            this.createDesignMatrix(randomSeries);

//            if (useL1){
                this.rambo_coeffs_L1();
//            } else { // must always use background when doing second derivative L1
//                this.excludeBackground = false;
//                this.rambo_coeffs_smoothed();
//            }

            // calculate Rg
            double tempRgSum = 0, tempRg2Sum=0, xaverage=0;
            XYDataItem item;
            for(int pr=0; pr < totalInDistribution; pr++){
                item = prDistribution.getDataItem(pr);
                double rvalue = item.getXValue();
                tempRg2Sum += rvalue*rvalue*item.getYValue()*del_r;
                tempRgSum += item.getYValue()*del_r; // width x height => area
                xaverage += rvalue*item.getYValue()*del_r;
            }
            // IZERO estimate
            double sum = coefficients[0];
            for(int j=0; j< r_vector_size; j++){
                sum +=  coefficients[j+1];
            }

            rgValues[i] = Math.sqrt(0.5*tempRg2Sum/tempRgSum);
            izeroValues[i] = (sum + standardizedMin)*standardizedScale;
        }

        DescriptiveStatistics rgStat = new DescriptiveStatistics(rgValues);
        DescriptiveStatistics izeroStat = new DescriptiveStatistics(izeroValues);

        rgError = rgStat.getStandardDeviation()/rgStat.getMean();
        iZeroError = izeroStat.getStandardDeviation()/izeroStat.getMean();

        //RESTORE COEFFICIENTS AND PR DISTRIBUTION
        for(int i=0; i<totalCoefficients; i++){ // copy back the coefficients
            coefficients[i] = oldCoefficients[i];
        }

        prDistribution.clear();
        for(int i=0; i<tempPr.getItemCount(); i++){
            prDistribution.add(tempPr.getDataItem(i));
        }
        totalInDistribution = prDistribution.getItemCount();
        setSplineFunction();
    }
}
