package version3.InverseTransform;

import net.jafama.FastMath;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import version3.Functions;
import version3.Interpolator;
import version3.StatMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class IndirectFT implements RealSpacePrObjectInterface {

    final double[] n_pi_squared = {
            0.000,
            9.869604401089358,
            39.47841760435743,
            88.82643960980423,
            157.91367041742973,
            246.74011002723395,
            355.3057584392169,
            483.6106156533785,
            631.6546816697189,
            799.437956488238,
            986.9604401089358,
            1194.2221325318121,
            1421.2230337568676,
            1667.9631437841015,
            1934.442462613514,
            2220.6609902451055,
            2526.6187266788756,
            2852.3156719148246,
            3197.751825952952,
            3562.9271887932578,
            3947.8417604357433,
            4352.495540880407,
            4776.8885301272485,
            5221.0207281762705,
            5684.89213502747,
            6168.502750680849,
            6671.852575136406,
            7194.941608394141,
            7737.769850454056,
            8300.33730131615,
            8882.643960980422,
            9484.689829446874,
            10106.474906715503,
            10747.99919278631,
            11409.262687659299,
            12090.265391334464,
            12791.007303811808,
            13511.488425091331,
            14251.708755173031,
            15011.668294056912,
            15791.367041742973,
            16590.80499823121,
            17409.98216352163,
            18248.898537614223,
            19107.554120508994,
            19985.948912205953,
            20884.082912705082,
            21801.95612200639,
            22739.56854010988,
            23696.920167015545,
            24674.011002723397,
            25670.841047233418,
            26687.410300545624,
            27723.718762660006,
            28779.766433576566,
            29855.55331329531,
            30951.079401816223,
            32066.344699139325,
            33201.3492052646,
            34356.09292019206,
            35530.57584392169,
            36724.7979764535,
            37938.759317787495,
            39172.45986792366,
            40425.89962686201,
            41699.078594602535,
            42991.99677114524,
            44304.65415649013,
            45637.050750637194,
            46989.186553586434,
            48361.061565337855,
            49752.67578589146,
            51164.02921524723,
            52595.12185340518,
            54045.953700365324,
            55516.52475612764,
            57006.835020692124,
            58516.8844940588,
            60046.67317622765,
            61596.201067198686,
            63165.46816697189,
            64754.47447554727,
            66363.21999292485,
            67991.70471910459,
            69639.92865408651,
            71307.8917978706,
            72995.59415045689,
            74703.03571184535,
            76430.21648203598,
            78177.13646102881,
            79943.79564882381,
            81730.19404542097,
            83536.33165082033,
            85362.20846502185,
            87207.82448802557,
            89073.17971983146,
            90958.27416043953,
            92863.10780984977,
            94787.68066806218,
            96731.9927350768,
            98696.04401089359,
            100679.83449551254,
            102683.36418893367,
            104706.633091157,
            106749.6412021825,
            108812.38852201017,
            110894.87505064002,
            112997.10078807206,
            115119.06573430626,
            117260.76988934266,
            119422.21325318124,
            121603.39582582198,
            123804.3176072649,
            126024.97859751001,
            128265.3787965573,
            130525.51820440678,
            132805.3968210584,
            135105.01464651222,
            137424.37168076824,
            139763.4679238264,
            142122.30337568675,
            144500.87803634928,
            146899.191905814,
            149317.2449840809,
            151755.03727114998,
            154212.5687670212,
            156689.83947169463,
            159186.84938517027,
            161703.59850744804,
            164240.086838528,
            166796.31437841014,
            169372.2811270945,
            171967.98708458096,
            174583.43225086966,
            177218.6166259605,
            179873.54020985356,
            182548.20300254878,
            185242.60500404617,
            187956.74621434574,
            190690.62663344748,
            193444.24626135142,
            196217.6050980575,
            199010.70314356583,
            201823.54039787626,
            204656.11686098893,
            207508.43253290374,
            210380.48741362072,
            213272.28150313994,
            216183.8148014613,
            219115.08730858483,
            222066.09902451056,
            225036.8499492384,
            228027.3400827685,
            231037.56942510078,
            234067.5379762352,
            237117.24573617184,
            240186.6927049106,
            243275.87888245154,
            246384.80426879475,
            249513.46886394004,
            252661.87266788757,
            255830.01568063724,
            259017.89790218908,
            262225.51933254313,
            265452.8799716994,
            268699.97981965775,
            271966.81887641834,
            275253.3971419811,
            278559.71461634606,
            281885.7712995132,
            285231.5671914824,
            288597.10229225387,
            291982.37660182756,
            295387.3901202034,
            298812.1428473814,
            302256.6347833615,
            305720.8659281439,
            309204.8362817285,
            312708.54584411526,
            316231.9946153041,
            319775.18259529525,
            323338.10978408845,
            326920.7761816839,
            330523.1817880815,
            334145.3266032813,
            337787.21062728326,
            341448.8338600874,
            345130.1963016938,
            348831.29795210226,
            352552.13881131297,
            356292.71887932584,
            360053.0381561409,
            363833.0966417581,
            367632.89433617744,
            371452.43123939907,
            375291.7073514228,
            379150.7226722487,
            383029.4772018768,
            386927.9709403072,
            390846.2038875397,
            394784.17604357435,
            398741.88740841113,
            402719.33798205014,
            406716.5277644914,
            410733.4567557347,
            414770.1249557802,
            418826.532364628,
            422902.6789822778,
            426998.56480873,
            431114.1898439843,
            435249.55408804066,
            439404.6575408993,
            443579.5002025601,
            447774.08207302314,
            451988.4031522882,
            456222.46344035555,
            460476.26293722505,
            464749.8016428967,
            469043.0795573706,
            473356.09668064676,
            477688.85301272495,
            482041.3485536053,
            486413.5833032879,
            490805.5572617727,
            495217.2704290596,
            499648.7228051487,
            504099.91439004004,
            508570.84518373344,
            513061.5151862292,
            517571.924397527,
            522102.0728176271,
            526651.9604465293,
            531221.5872842337,
            535810.9533307402,
            540420.0585860489,
            545048.9030501598,
            549697.486723073,
            554365.8096047881,
            559053.8716953056,
            563761.6729946252,
            568489.213502747,
            573236.493219671,
            578003.5121453971,
            582790.2702799255,
            587596.767623256,
            592423.0041753887,
            597268.9799363236,
            602134.6949060606,
            607020.1490845999,
            611925.3424719413,
            616850.2750680848
    };

    public double[] coefficients, r_values;

    public final double qmax, dmax, lambda;
    public final double inv2PI2 = 1.0/(2*Math.PI*Math.PI);
    public final double INV_PI = 1.0/Math.PI;
    public final double TWO_INV_PI = 2.0/Math.PI;
    public boolean useL1;
    public XYSeries nonData, data, residuals, errors; // should be qI(q) dataset
    public final int multiplesOfShannonNumber;
    public double standardizedMin;
    public double standardizedScale;
    public boolean includeBackground = true;
    public double rg, izero, rAverage;
    public double rgError, iZeroError, rAverageError;
    public int totalInDistribution, totalCoefficients, rows;
    public XYSeries prDistribution;
    public boolean negativeValuesInModel;
    public PolynomialSplineFunction splineFunction;

    /*
     * LINE SEARCH PARAMETERS
     */
    public double alpha = 0.01;            // minimum fraction of decrease in the objective
    public double beta  = 0.5;             // stepsize decrease factor
    public int max_ls_iter = 400;          // maximum backtracking line search iteration
    /*
     * Interior Point Parameters
     */
    public int MU = 2;                    // updating parameter of t
    public int max_nt_iter = 500;         // maximum IPM (Newton) iteration
    public double reltol = 0.001;
    public double eta = 0.001;
    public double tau = 0.01;
    public int pcgmaxi = 5000;
    public double dobj = Double.NEGATIVE_INFINITY;
    public double pobj = Double.POSITIVE_INFINITY;
    public final double dmax_PI_TWO_INV_PI;
    public final double PI_INV_DMAX;
    public String status;
    public int ns, coeffs_size;
    public SimpleMatrix a_matrix;
    public SimpleMatrix y_vector;
    public SimpleMatrix am_vector;

    public IndirectFT(XYSeries nonStandardizedData, XYSeries errors, double dmax, double qmax, double lambda, boolean useL1, int cBoxValue, boolean includeBackground){
        this.nonData = nonStandardizedData;
        this.errors = errors;
        this.lambda = lambda;
        this.useL1 = useL1;
        this.qmax = qmax;
        this.dmax = dmax;
        this.multiplesOfShannonNumber = cBoxValue;
        this.includeBackground = includeBackground;
        this.dmax_PI_TWO_INV_PI = dmax * Math.PI * TWO_INV_PI;
        this.PI_INV_DMAX = Math.PI/dmax;
        this.standardizeData();
    }

    /**
     * used in refining Pr distribution, requires data that is already standardized
     * @param standardizedData
     * @param errors
     * @param dmax
     * @param qmax
     * @param lambda
     * @param cBoxValue
     * @param useL1
     * @param includeBackground
     * @param standardizationMean
     * @param standardizationStDev
     */
    public IndirectFT(XYSeries standardizedData, XYSeries errors, double dmax, double qmax, double lambda, int cBoxValue, boolean useL1, boolean includeBackground, double standardizationMean, double standardizationStDev){
        this.data = standardizedData;
        this.errors = errors;
        this.qmax = qmax;
        this.dmax = dmax;
        this.lambda = lambda;
        this.useL1 = useL1;
        this.includeBackground = includeBackground;
        this.multiplesOfShannonNumber = cBoxValue;
        this.dmax_PI_TWO_INV_PI = dmax * Math.PI * TWO_INV_PI;
        this.PI_INV_DMAX = Math.PI/dmax;
        standardizedMin = standardizationMean;
        standardizedScale = standardizationStDev;
        this.createNonStandardizedData();
    }


    @Override
    public double getStandardizedScale(){
        return standardizedScale;
    }

    @Override
    public double getStandardizedLocation(){
        return standardizedMin;
    }

    abstract void calculateIzeroRg();

    abstract void setPrDistribution();

    @Override
    public double[] getCoefficients(){
        return coefficients;
    }

    /**
     * use MonteCarlo esque method for estimating errors through sampling dataset
     * @param fittedData
     */
    public abstract void estimateErrors(XYSeries fittedData);

    abstract void createDesignMatrix(XYSeries series);

    @Override
    public int getTotalInDistribution(){
        return totalInDistribution;
    }

    @Override
    public XYSeries getPrDistribution() {
        return prDistribution;
    }

    @Override
    public double getRg() {
        return rg;
    }

    @Override
    public double getRgError() {
        return rgError;
    }

    @Override
    public double getIZero() {
        return izero;
    }

    @Override
    public double getIZeroError() {
        return iZeroError;
    }

    @Override
    public double getRAverage(){
        return rAverage;
    }

    @Override
    public XYSeries getIqcalc() {
        return null;
    }

    @Override
    public XYSeries getqIqCalc() {
        return null;
    }

    public double normL1(SimpleMatrix vec){
        double sum = 0;
        int size = vec.getNumElements();

        for(int i=0; i<size; i++){
            sum += Math.abs(vec.get(i));
        }
        return sum;
    }

    public SimpleMatrix hessphi_coeffs_positivity_constrained(SimpleMatrix ata, SimpleMatrix d1, int coeffs_size) {

        SimpleMatrix hessian = new SimpleMatrix(coeffs_size, coeffs_size);

        SimpleMatrix t_ata;
        t_ata =  ata.plus(d1);

        for (int r=0; r < coeffs_size; r++){

            for(int c=0; c < coeffs_size; c++){
                hessian.set(r,c, t_ata.get(r,c));
            }

        }
        return hessian;
    }


    public double inf_norm(SimpleMatrix vec){
        int sizeof = vec.getNumElements();
        double maxi = Math.abs(vec.get(0));
        double current;
        for(int i=1; i<sizeof; i++){
            current = Math.abs(vec.get(i));
            if (current > maxi){
                maxi = current;
            }
        }
        return maxi;
    }


    public double min(SimpleMatrix vec){

        double initial = vec.get(0);
        int length = vec.getNumElements();
        double current;

        for (int i=1; i<length; i++){
            current = vec.get(i);
            if (current < initial){
                initial = current;
            }
        }

        return initial;
    }

    public double max(SimpleMatrix vec){
        double initial = vec.get(0);
        int length = vec.getNumElements();
        double current;

        for (int i=1; i<length; i++){
            current = vec.get(i);
            if (current > initial){
                initial = current;
            }
        }

        return initial;
    }

    public ArrayList<SimpleMatrix> linearPCGPositiveOnly(SimpleMatrix designMatrix, SimpleMatrix bMatrix, SimpleMatrix initial, SimpleMatrix d1, double pcgtol, int pcgmaxi){

        ArrayList<SimpleMatrix> returnElements = new ArrayList<SimpleMatrix>();

        int i=0;

        SimpleMatrix r_matrix = bMatrix.minus(designMatrix.mult(initial)); // residual
        SimpleMatrix r_plus_1;
        SimpleMatrix preConditioner = designMatrix.copy();

        int cols = d1.numCols();
        /*
         * P = M
         * Preconditioner, P is Hessian with first block set to:
         * M = 2*diagATA + D1
         *
         */
        for(int row=0; row < cols; row++){

            for(int col=0; col< cols; col++){

                if (row == col){
                    //preConditioner.set(row,row,2*designMatrix.get(row,row)+d1.get(row,row));
                    preConditioner.set(row, row, (2+d1.get(row,row)));
                }

                if (row != col){ // preserve diagonal entries and just add D1
                    //preConditioner.set(row, col, d1.get(row, col));
                    preConditioner.set(row, col, 0);
                }
            }
        }

        SimpleMatrix invertM = preConditioner.invert();
        SimpleMatrix z = invertM.mult(r_matrix); // search direction
        //SimpleMatrix z = invertM.mult(initial); // search direction
        SimpleMatrix p = z.copy();

        SimpleMatrix q;

        SimpleMatrix delx_vector = initial.copy();
        int xu_size = delx_vector.getNumElements();

        SimpleMatrix z_plus_1 ;//= new SimpleMatrix(xu_size,1);
        SimpleMatrix x_vector = new SimpleMatrix(cols, 1);

        double alpha;
        // stopping criteria
        double rkTzk = r_matrix.transpose().mult(z).get(0);

        double beta;
        //double stop = rkTzk*pcgtol*pcgtol;
       //double stop = pcgtol;

        //System.out.println("PCG rkTzk " + rkTzk + " " + stop);
double magB = 1.0/bMatrix.normF();
double topB = 1000;

        //while( (i < pcgmaxi) && (rkTzk > stop) ){
            while( (i < pcgmaxi) && topB > pcgtol ){

            q = designMatrix.mult(p);
            alpha = rkTzk/(p.transpose().mult(q)).get(0);

            // invert each element of dTq;
            delx_vector = delx_vector.plus(p.scale(alpha));

//            if (i < 50){
//                r_plus_1 = bMatrix.minus(designMatrix.mult(delx_vector));
//            } else {
//                r_plus_1 = r_matrix.minus(q.scale(alpha)); // r_k+1 = r_k - alpha_k * A*p_k
//            }
            r_plus_1 = r_matrix.minus(q.scale(alpha)); // r_k+1 = r_k - alpha_k * A*p_k

            z_plus_1 = invertM.mult(r_plus_1);

            //Polak-Ribiere Beta
            beta = z_plus_1.transpose().mult(r_plus_1.minus(r_matrix)).get(0,0)/(z.transpose().mult(r_matrix)).get(0,0);
            //Fletcher Reeves
            //beta = z_plus_1.transpose().mult(r_plus_1).get(0,0)*(z.transpose().mult(r)).get(0,0);
            /*
             * k => k + 1
             */
            p = z_plus_1.plus(p.scale(beta));
            r_matrix = r_plus_1.copy();
            rkTzk = r_matrix.transpose().mult(z_plus_1).get(0);
            z = z_plus_1.copy();

            topB = (bMatrix.minus(designMatrix.mult(delx_vector))).normF()*magB;

            i++;
        }

        //System.out.println("PCG STOP: " + stop + " > " + rkTzk + " at " + i);

        for(int ii=0; ii < xu_size; ii++){
            x_vector.set(ii, 0, delx_vector.get(ii));
        }

        // set final iterate number
        SimpleMatrix pitr = new SimpleMatrix(1,1);
        pitr.set(0,0,i);

        returnElements.add(x_vector);  // 0
        returnElements.add(delx_vector); // 1
        returnElements.add(pitr);      // 2
        return returnElements;
    }


    public SimpleMatrix hessphi_coeffs(SimpleMatrix ata, SimpleMatrix d1, SimpleMatrix d2, int coeffs_size) {

        int n = ata.numCols();

        SimpleMatrix hessian = new SimpleMatrix(2*coeffs_size,2*coeffs_size);
        SimpleMatrix t_ata;
        t_ata =  ata.plus(d1);

        double d2_value;

        for (int r=0; r < coeffs_size; r++){

            for(int c=0; c < coeffs_size; c++){
                hessian.set(r,c,t_ata.get(r,c));
            }

            d2_value = d2.get(r,r);
            hessian.set(r+n, r, d2_value);   //D2(transpose) lower-left
            hessian.set(r, r+n, d2_value);   //D2 upper-right
            hessian.set(r+n, r+n, d1.get(r,r)); // lower-right
        }
        return hessian;
    }


    public ArrayList<SimpleMatrix> linearPCG(SimpleMatrix designMatrix, SimpleMatrix bMatrix, SimpleMatrix initial, SimpleMatrix d1, double pcgtol, int pcgmaxi, double tauT){

        ArrayList<SimpleMatrix> returnElements = new ArrayList<SimpleMatrix>();
        SimpleMatrix r = bMatrix.minus(designMatrix.mult(initial)); // residual
        SimpleMatrix r_plus_1;
        SimpleMatrix preConditioner = designMatrix.copy();

        int n = d1.numCols();
        /*
         * P = M  D2
         *     D2 D3
         * Preconditioner, P is Hessian with first block set to:
         * M = 2*diagATA + D1
         *
         */
        //SimpleMatrix d3 = new SimpleMatrix(n,n);
        for(int row=0; row < n; row++){
            for(int col=0; col< n; col++){
                if (row == col){
                    //preConditioner.set(row,row, designMatrix.get(row,row));
                    preConditioner.set(row,row,2*tauT+d1.get(row,row));
                    // d3.set(row,row,2+d1.get(row,row));
                }
                if (row != col){ // preserve diagonal entries and just add D1
                    preConditioner.set(row, col, 0);
                    //System.out.println("d1 " + row + " " + d1.get(row, col));
                    //preConditioner.set(row, col, d1.get(row, col));
                }
            }
        }

        /*
        // Boyd Inversion Start
        SimpleMatrix d1d3minusd22 = d1.mult(d3).minus(d2.elementMult(d2));
        SimpleMatrix p1 = new SimpleMatrix(n,n);
        SimpleMatrix p2 = new SimpleMatrix(n,n);
        SimpleMatrix p3 = new SimpleMatrix(n,n);

        for(int row=0; row < n; row++){
             p1.set(row,row, d1.get(row,row)/d1d3minusd22.get(row,row));
             p2.set(row,row, d2.get(row,row)/d1d3minusd22.get(row,row));
             p3.set(row,row, d3.get(row,row)/d1d3minusd22.get(row,row));
        }
        // Boyd Inversion END
        */
        SimpleMatrix invertM = preConditioner.invert();
        SimpleMatrix z = invertM.mult(r); // search direction
        SimpleMatrix p = z.copy();

        SimpleMatrix q;

        SimpleMatrix xu_vector = initial.copy();
        int xu_size = xu_vector.getNumElements();

        SimpleMatrix z_plus_1 ;//= new SimpleMatrix(xu_size,1);

        SimpleMatrix x_vector = new SimpleMatrix(n, 1);
        SimpleMatrix u_vector = new SimpleMatrix(xu_size-n, 1);

        double alpha;
        // stopping criteria
        double rkTzk = r.transpose().mult(z).get(0);

        double beta;
        //double stop = rkTzk*pcgtol*pcgtol;
        //System.out.println("PCG rkTzk " + rkTzk + " " + stop);
        double magB = 1.0/bMatrix.normF();
        double topB = 1000;

        int i=0;
        while( (i < pcgmaxi) && (topB > pcgtol) ){

            q = designMatrix.mult(p);
            alpha = rkTzk/(p.transpose().mult(q)).get(0);

            // invert each element of dTq;
            xu_vector = xu_vector.plus(p.scale(alpha));

            if (i < 50){
                r_plus_1 = bMatrix.minus(designMatrix.mult(xu_vector));
            } else {
                r_plus_1 = r.minus(q.scale(alpha)); // r_k+1 = r_k - alpha_k * A*p_k
            }

            z_plus_1 = invertM.mult(r_plus_1);

            //Polak-Ribiere Beta
            beta = z_plus_1.transpose().mult(r_plus_1.minus(r)).get(0,0)/(z.transpose().mult(r)).get(0,0);
            //Fletcher Reeves
            //beta = z_plus_1.transpose().mult(r_plus_1).get(0,0)*(z.transpose().mult(r)).get(0,0);

            /*
             * k => k + 1
             */
            p = z_plus_1.plus(p.scale(beta));
            r = r_plus_1.copy();
            rkTzk = r.transpose().mult(z_plus_1).get(0);
            z = z_plus_1.copy();
            topB = (bMatrix.minus(designMatrix.mult(xu_vector))).normF()*magB;
            i++;
        }

        // System.out.println("PCG STOP: " + stop + " > " + rkTzk + " at " + i);

        for(int ii=0; ii<(xu_size-n); ii++){

            if (ii<n){
                x_vector.set(ii,0,xu_vector.get(ii));
            }

            u_vector.set(ii,0,xu_vector.get(ii+n));
        }

        // set final iterate number
        SimpleMatrix pitr = new SimpleMatrix(1,1);
        pitr.set(0,0,i);

        returnElements.add(x_vector);  // 0
        returnElements.add(u_vector);  // 1
        returnElements.add(xu_vector); // 2
        returnElements.add(pitr);      // 3
        return returnElements;
    }

    /**
     * value of second derivative calculated at points in r_vector
     * @param am Moore coefficients, assume am[0] is constant backgroud
     * @param r_vector set of equally distributions points along dmax
     * @param inv_d 1/dmax
     * @return
     */
    public SimpleMatrix p_dd_r(SimpleMatrix am, double[] r_vector, double inv_d){

        double r_value, pi_r_n_inv_d, pi_r_inv_d, cos_pi_r_n_inv_d, sin_pi_r_n_inv_d;
        double pi_inv_d = Math.PI*inv_d;
        double a_i;

        int r_limit = r_vector.length;

        SimpleMatrix p_dd_r = new SimpleMatrix(r_limit,1);

        int coeffs_size = am.getNumElements();

        double a_i_sum, pi_inv_d_n;

        for (int r=0; r < r_limit; r++){
            r_value = r_vector[r];
            pi_r_inv_d = pi_inv_d*r_value;
            a_i_sum = 0;

            for(int n=1; n < coeffs_size; n++){
                a_i = am.get(n,0);

                pi_inv_d_n = pi_inv_d*n;
                pi_r_n_inv_d = pi_r_inv_d*n;

                cos_pi_r_n_inv_d = FastMath.cos(pi_r_n_inv_d);
                sin_pi_r_n_inv_d = FastMath.sin(pi_r_n_inv_d); // if r_value is ZERO, sine is ZERO

                a_i_sum += 2*a_i*pi_inv_d_n*cos_pi_r_n_inv_d - r_value*a_i*pi_inv_d_n*pi_inv_d_n*sin_pi_r_n_inv_d;
            }

            p_dd_r.set(r,0, a_i_sum*0.5*inv_d);
        }

        return p_dd_r;
    }


    /**
     * 1st derivative of P"(r) with respect to the Moore coefficients
     * @param n_i
     * @param r
     * @param inv_d
     * @return
     */
    public double c_ni_r(int n_i, double r, double inv_d){
        double inv_d_pi_n = inv_d*Math.PI*n_i;
        double theta = r*inv_d_pi_n;
        double cir;
        //cir = inv_d*ni*FastMath.cos(theta) - Math.PI*r*0.5*inv_d*inv_d*ni*ni*FastMath.sin(theta);
        cir = 2.0*inv_d_pi_n*FastMath.cos(theta) - r*inv_d_pi_n*inv_d_pi_n*FastMath.sin(theta);
        return 0.5*inv_d*cir;
    }



    public SimpleMatrix hessphi(SimpleMatrix ata, SimpleMatrix d1, SimpleMatrix d2, SimpleMatrix d3){

        int h = d2.numCols();
        int n = ata.numCols();

        SimpleMatrix hessian = new SimpleMatrix(n+h,n+h);

        SimpleMatrix t_ata;
        t_ata =  ata.plus(d1); //ata and d1 are not modified

        double d2_value;

        for (int r=0; r < h; r++){

            for(int c=0; c < h; c++){

                if (r<n && c<n) {
                    d2_value = d2.get(r,c);
                    hessian.set(r,c,t_ata.get(r,c));    // ATA + D1
                    hessian.set(c+n, r, d2_value);      // D2(transpose) lower-left
                    hessian.set(r, c+n, d2_value);      // D2 upper-right
                } else if (r<n && c >=n) {
                    // d2 nxh
                    d2_value = d2.get(r,c);
                    hessian.set(c+n, r, d2_value);   //D2 (transpose) lower-left
                    hessian.set(r, c+n, d2_value);   //D2 upper-right
                }

                hessian.set(r+n, c+n, d3.get(r,c));
            }
        }

        //hessian.print();
        return hessian;
    }



    /**
     *
     * standardize data
     */
    private void standardizeData(){
        XYDataItem tempData;
        data = new XYSeries("Standardized data");
        int totalItems = nonData.getItemCount();

        standardizedMin = nonData.getMinY();
        standardizedScale = nonData.getMaxY() - standardizedMin;
        double invstdev = 1.0/ standardizedScale;

        for(int r=0; r<totalItems; r++){
            tempData = nonData.getDataItem(r);
            data.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
        }
    }

    /**
     * estimate chi-squared by evaluating the function at the points of the cardinal series
     * @return
     */
    @Override
    public double getChiEstimate(){
        // how well does the fit estimate the cardinal series (shannon points)
        // how many points determined by Shannon Information Theory => ns
        double chi=0;
        double diffTolerance = 0.0001;
        double inv_card;
        final double pi_inv_dmax = Math.PI/dmax;
        final double dmax_inv_pi = 1.0/pi_inv_dmax;
        double error_value;
        // Number of Shannon bins, excludes the a_o
        int bins = ns, count=0, total = data.getItemCount();

        am_vector.printDimensions();
        a_matrix.printDimensions();
        y_vector.printDimensions();
        SimpleMatrix tempResiduals = a_matrix.mult(am_vector).minus(y_vector);
        // calculate residuals
        residuals = new XYSeries("residuals");
        for(int i=0; i < rows; i++){
            XYDataItem item = data.getDataItem(i); // isn't quite correct, using data to get q-values, should be based on input for making design matrix
            residuals.add(item.getX(), tempResiduals.get(i,0));
        }


        double df = 0; //1.0/(double)(bins-1);
        double cardinal, test_q = 0, diff;
        XYDataItem priorValue, postValue;

        // see if q-value exists but to what significant figure?
        // if low-q is truncated and is missing must exclude from calculation

        for (int i=1; i <= bins; i++){

            cardinal = i*pi_inv_dmax; // <= q-value
            inv_card = 1.0/cardinal;
            // what happens if a bin is empty?
            searchLoop:
            while ( (test_q < cardinal) && (count < total) ){
                test_q = data.getX(count).doubleValue();
                // find first value >= shannon cardinal value
                if (test_q >= cardinal){
                    break searchLoop;
                }
                count++;
            }

            // if either differences is less than 0.1% use the measured value
            if (count > 0 && count < total){
                priorValue = data.getDataItem(count-1);
                postValue = data.getDataItem(count);
                if ((cardinal - priorValue.getXValue())*inv_card < diffTolerance) {// check if difference is smaller pre-cardinal
                    error_value = 1.0/errors.getY(count - 1).doubleValue()*inv_card*standardizedScale; // get residual for this q-value
                    diff = residuals.getY(count - 1).doubleValue();
                    chi += (diff*diff)*(error_value*error_value);
                    df += 1.0;

                } else if ( Math.abs(postValue.getXValue() - cardinal)*inv_card < diffTolerance) {// check if difference is smaller post-cardinal
                    error_value = 1.0/errors.getY(count).doubleValue()*inv_card*standardizedScale;
                    diff = residuals.getY(count).doubleValue();
                    chi += (diff*diff)*(error_value*error_value);
                    df += 1.0;

                } else { // if difference is greater than 0.1% interpolate and also the cardinal is bounded
                    Interpolator tempI = new Interpolator(nonData, errors, cardinal); // interpolate intensity
                    // diff = tempI.interpolatedValue - this.calculateQIQ(cardinal);
                    if (this.getClass().getName().contains("MooreTransform")){
                        diff = tempI.interpolatedValue - (standardizedScale*(coefficients[0] + coefficients[i]*dmax_inv_pi) + standardizedMin);
                    } else {
                        diff = tempI.interpolatedValue - this.calculateQIQ(cardinal);
                    }

                    chi += (diff*diff)/(tempI.stderror* tempI.stderror);
                    df += 1.0;
                }
            }
        }

        double delta = ns - df;  // if no missing bin, should equal zero

        // estimate decorellated uncertainties for the cardinal points
//        int totalerrors = errors.getItemCount();
//        SimpleMatrix stdev = new SimpleMatrix(errors.getItemCount(), 1); // n x 1
//        for(int i=0; i<totalerrors; i++){
//            stdev.set(i,0, errors.getY(i).doubleValue());
//        }

//        SimpleMatrix covariance = stdev.mult(stdev.transpose()); // n x n
//        // perform SVD
//        DenseMatrix64F tempForSVD = covariance.getMatrix();
//        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(totalerrors, totalerrors, true, true, false);
//        // A = U*W*V_t
//        try {
//            svd.decompose(tempForSVD);
//        } catch (Exception e){
//            System.out.println("Matrix inversion exception in svdReduce ");
//        }
//
//        double[] sing = svd.getSingularValues();
//        for(int i=0; i<coeffs_size+2; i++){
//            System.out.println(i + " SVD " + sing[i]);
//        }

        return chi*1.0/(totalCoefficients - 1 - delta);
    }


    @Override
    public double getKurtosisEstimate(int rounds){  // calculated from the residuals
         /*
         * Divide scattering curve into Shannon bins
         * Determine kurtosis from a random sample of the ratio of I_calc to I_obs based on the binning
         * Take max of the kurtosis set
         */
        //Random newRandom = new Random();
        int total = residuals.getItemCount(); // fittedqIq is scaled for fitting
        //double[] ratio = new double[total];
        ArrayList<Double> test_residuals = new ArrayList<Double>();


        /*
         * bin the ratio
         * qmax*dmax/PI
         *
         */
        double[] kurtosises = new double[rounds];
        // calculate kurtosis
        double qmin = residuals.getMinX();
        double bins = ns*3.0;
        double delta_q = (residuals.getMaxX()-qmin)/bins;

        double samplingLimit, lowerLimit;
        Random randomGenerator = new Random();
        int[] randomNumbers;


        for (int i=0; i<rounds; i++){
            // for each round, get a random set of values from ratio
            int startbb = 0, upperbb = 0;
            test_residuals.clear();

            // same percent out of each bin
            samplingLimit = (0.5 + randomGenerator.nextInt(12)) / 100.0;  // return a random percent up to 12%

            for (int b=1; b < bins; b++) {
                // find upper q in bin
                // SAMPLE randomly per bin
                lowerLimit = (delta_q * b + qmin);

                binloop:
                for (int j = startbb; j < total; j++) {
                    if (residuals.getX(j).doubleValue() >= lowerLimit) {
                        upperbb = j;
                        break binloop;
                    }
                }

                // grab indices inbetween startbb and upperbb
                //System.out.println("bin " + b + " " + (upperbb - startbb));
                randomNumbers = Functions.randomIntegersBounded(startbb, upperbb, samplingLimit);
                startbb = upperbb;

                for(int h=0; h < randomNumbers.length; h++){
                    //test_residuals.add(ratio[randomNumbers[h]]);
                    test_residuals.add(residuals.getY(randomNumbers[h]).doubleValue());
                }
            }
            // calculate kurtosis
            kurtosises[i] = StatMethods.kurtosis(test_residuals);
            //System.out.println(i + " KURT : " + kurtosises[i] + " SL : " + samplingLimit);
        }

        Arrays.sort(kurtosises);
        return kurtosises[(rounds-1)/2];
    }


    @Override
    public int getTotalFittedCoefficients(){
        return includeBackground ? totalCoefficients : totalCoefficients -1 ;
    }



    /**
     * Dataset must be standardized and transformed as q*Iq
     * @param dataset
     * @return
     */
    @Override
    public double calculateMedianResidual(XYSeries dataset){

        List<Double> residualsList = new ArrayList<Double>();
        int dataLimit = dataset.getItemCount();
        double resi;
//        XYDataItem tempitem;
//        System.out.println("CalculateMedian Before " + data.getItemCount() + " " + dataset.getItemCount());
        this.createDesignMatrix(dataset); // this creates a_matrix and y_vector

//        System.out.println("CalculateMedian AFTER " + data.getItemCount() + " " + dataset.getItemCount());
//        SimpleMatrix temp_y_vector = new SimpleMatrix(dataLimit,1);
//        for (int i = 0; i < dataLimit; i++){
//            temp_y_vector.set(i,0, dataset.getY(i).doubleValue());
//        }

        SimpleMatrix tempResiduals = a_matrix.mult(am_vector).minus(y_vector); // this would resize rows and residuals via a_matrix and y_vector

        for (int i = 0; i < dataLimit; i++){
//            tempitem = dataset.getDataItem(i); // in standardized form
//            resi = this.calculateQIQ(tempitem.getXValue()) - (tempitem.getYValue()*standardizedScale + standardizedMin);
            //System.out.println(i + " " + tempResiduals.get(i,0) + " resi " + resi);
            resi = tempResiduals.get(i,0);
            // calculate value
            residualsList.add(resi*resi);
        } //

        // need to resetDesignMatrix so that a_matrix and y_vector revert back to the original dataset

        return Statistics.calculateMedian(residualsList);
    }

    @Override
    public void setNonStandardizedData(XYSeries nonStandardizedData){
        this.nonData = nonStandardizedData;
    }

    @Override
    public void createNonStandardizedData(){
        nonData = new XYSeries("nonstandard");
        for (int i=0; i<this.data.getItemCount(); i++){
            XYDataItem item = this.data.getDataItem(i);
            nonData.add(item.getX(), item.getYValue()*standardizedScale+standardizedMin);
        }
    }

}
