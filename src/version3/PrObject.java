package version3;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by robertrambo on 24/01/2016.
 */
public class PrObject implements Runnable {

    private double[] n_pi_squared = {
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

    private double qmax, dmax, lambda=0.01;
    private final double INV_PI = 1.0/Math.PI;
    private boolean useL1;
    private XYSeries data;
    private RealSpace dataset;

    public PrObject(RealSpace dataset, double lambda, boolean useL1){

        // create real space object for each dataset in use
        // XYSeries data, double dmax, double lambda, double qmax
        this.dataset = dataset;
        qmax = dataset.getLogData().getMaxX();
        dmax = dataset.getDmax();
        this.lambda = lambda;
        this.useL1 = useL1;
        data = dataset.getfittedqIq();
    }

    public PrObject(XYSeries fittedqIq, double qmax, double dmax, double lambda, boolean useL1Coefficients){
        data = fittedqIq;
        this.qmax = qmax;
        this.dmax = dmax;
        this.lambda = lambda;
        this.useL1 = useL1Coefficients;
    }


    @Override
    public void run() {

        ArrayList<double[]> tempResults;

        if (useL1){
            tempResults = moore_pr_L1();  // minimize on second derivative
        } else {
            tempResults = moore_coeffs_L1();  // minimize on coefficients
        }

        //update realspace dataset
        this.dataset.setMooreCoefficients(tempResults.get(0));
        this.dataset.calculateIntensityFromModel(useL1);
    }


    /**
     *
     * @return ArrayList<double[]> [coeffs] [r-values]
     */
    public ArrayList<double[]> moore_coeffs_L1(){

        ArrayList<double[]> results = new ArrayList<>(2);

        double inv_d = 1.0/dmax;

        int ns = (int) Math.round(qmax*dmax*INV_PI); //
        int coeffs_size = ns + 1;   //+1 for constant background
        //int coeffs_size = ns // no background correction

        double incr = 2.0;
        int r_limit = (int)incr*ns-1;
        double del_r = dmax/ns/incr;
        double[] r_vector = new double[r_limit];

        for(int i=0; i< r_limit; i++){
            r_vector[i] = (i+1)*del_r;
        }

        double pi_d = Math.PI*dmax;
        XYDataItem tempData;

        /*
         * Interior Point Parameters
         */
        int MU = 2;            // updating parameter of t
        int max_nt_iter = 500; // maximum IPM (Newton) iteration

        /*
         * LINE SEARCH PARAMETERS
         */
        double alpha = 0.01;          // minimum fraction of decrease in the objective
        double beta  = 0.5;           // stepsize decrease factor
        int max_ls_iter = 400;    // maximum backtracking line search iteration

        int m = data.getItemCount();  // rows
        int n = coeffs_size;          // columns
        int u_size = coeffs_size;
        int hessian_size = coeffs_size*2;

        double t0 = Math.min(Math.max(1, 1/lambda), 2*n/0.001);
        double reltol = 0.01;
        double eta = 0.001;
        int pcgmaxi = 5000;

        double dobj = Double.NEGATIVE_INFINITY;
        double pobj = Double.POSITIVE_INFINITY;
        double s = Double.POSITIVE_INFINITY;
        double pitr = 0, pflg = 0, gap;

        double t = t0;
        double tau = 0.01;

        //Hessian and preconditioner
        SimpleMatrix d1 = new SimpleMatrix(n,n);
        SimpleMatrix d2 = new SimpleMatrix(n,n);

        SimpleMatrix hessian;

        SimpleMatrix dxu = new SimpleMatrix(hessian_size,1);

        ArrayList<SimpleMatrix> answers;

        SimpleMatrix p_u_r2;
        SimpleMatrix p_am_r2;

        /*
         * initialize u vector with 1's
         * size must include a_o and r_limit
         */
        DenseMatrix64F utemp = new DenseMatrix64F(n,1);
        CommonOps.fill(utemp,1);
        SimpleMatrix u = SimpleMatrix.wrap(utemp);

        SimpleMatrix y = new SimpleMatrix(m,1);

        //
        // Initialize and guess am
        //
        double qd, inv_t;

        SimpleMatrix am = new SimpleMatrix(n,1);// am is 0 column
        am.set(0,0,0);

        for (int i=1; i < n; i++){
            am.set(i, 0, 0); // initialize coefficient vector a_m to zero
        }

        /*
         * create A matrix (design Matrix)
         */
        SimpleMatrix a_matrix = new SimpleMatrix(m,n);

        double qd2, pi_sq_n;

        for(int r=0; r<m; r++){ //rows, length is size of data
            tempData = data.getDataItem(r);

            qd = tempData.getXValue()*dmax;
            qd2 = qd*qd;
            for(int c=0; c < n; c++){
                if (c == 0){
                    // a_matrix.set(r, 0, tempData.getXValue());
                    a_matrix.set(r, 0, 1);
                } else {
                    pi_sq_n = n_pi_squared[c];
                    a_matrix.set(r, c, pi_d * c * Math.pow(-1.0, c + 1) * Math.sin(qd) / (pi_sq_n - qd2));
                }
            }
            y.set(r,0,tempData.getYValue());
        }

        SimpleMatrix a_transpose = a_matrix.transpose();
        SimpleMatrix gradphi0;

        SimpleMatrix gradux = new SimpleMatrix(hessian_size,1);

        SimpleMatrix laplacian = (a_transpose.mult(a_matrix)).scale(2.0);

        SimpleMatrix dx;// = new SimpleMatrix(n,1);
        SimpleMatrix du;// = new SimpleMatrix(u_size,1);

        /*
         * Backtracking Line Search
         */
        SimpleMatrix z;
        SimpleMatrix nu;

        SimpleMatrix new_z;
        SimpleMatrix new_u = new SimpleMatrix(n,1);
        SimpleMatrix new_x = new SimpleMatrix(n,1);

        SimpleMatrix f;
        f = new SimpleMatrix(u_size*2,1);
        for (int i=0; i < u_size*2; i++){
            f.set(i,0,-1);
        }
        SimpleMatrix new_f = new SimpleMatrix(u_size*2,1);

        int lsiter;
        double maxAtnu;
        double normL1;

        double phi, new_phi, logfSum, gdx, normg, pcgtol;
        double resultM, am_value, u_value, invdiff2, invdiff;

        String status;

        calculationLoop:
        for (int ntiter=0; ntiter < max_nt_iter; ntiter++){

            z = (a_matrix.mult(am)).minus(y);

            //------------------------------------------------------------
            // Calculate Duality Gap
            //------------------------------------------------------------
            nu = z.scale(2.0);

            // get max of At*nu
            // maxAtnu = max(vec);
            maxAtnu = inf_norm(a_transpose.mult(nu));

            if (maxAtnu > lambda){
                nu = nu.scale(lambda/(maxAtnu));
            }

            /*
             * calculate second derivative P(r) at specified r-values
             * length is the size of r_limit
             * ignore first element, a_0, of am vector (constant background)             *
             */

            normL1 = normL1(am);
            pobj = (z.transpose().mult(z)).get(0,0) + (lambda * normL1);

            /*
             *  dobj  =  max(-0.25*nu'*nu-nu'*y,dobj);
             */
            dobj = Math.max(( (nu.transpose().mult(nu)).get(0,0)*(-0.25) - ((nu.transpose().mult(y))).get(0,0) ), dobj);

            // dobj = Math.max(( (nu.transpose().mult(nu)).get(0,0)*(-0.25) - ((nu.transpose().mult(y))).get(0,0) ), dobj);
            gap   =  pobj - dobj;

            //System.out.println("GAP: " + gap + " : " + " | ratio " + gap/dobj + " reltol " + reltol);

            //------------------------------------------------------------
            //       Shall we Stop?
            //------------------------------------------------------------
            if (gap/dobj < reltol) {
                status = "Solved";
                break calculationLoop;
            }


            //------------------------------------------------------------
            //       UPDATE t
            //------------------------------------------------------------
            if (s >= 0.5){
                t = Math.max(Math.min(2*n*MU/gap, MU*t), t);
            }
            inv_t = 1.0/t;

            //------------------------------------------------------------
            //      CALCULATE NEWTON STEP
            //------------------------------------------------------------
            //

            p_am_r2 = am.elementMult(am);
            p_u_r2 = u.elementMult(u);

            /*
             * Partitioned Matrix terms for Hessian
             * D1: n x n
             * D2: n x u_size
             */
            for(int row=0; row < u_size; row++){
                am_value = am.get(row,0);
                u_value = u.get(row,0);

                invdiff = 1/(p_u_r2.get(row,0) - p_am_r2.get(row,0));
                invdiff2 = invdiff*invdiff*inv_t;

                d1.set(row,row, 2*(p_u_r2.get(row,0) + p_am_r2.get(row,0))*invdiff2);
                d2.set(row,row, -4*u_value*am_value*invdiff2);
            }

            /*
             * Gradient
             * gradphi = [At*(z*2)-(q1-q2)/t; lambda*ones(n,1)-(q1+q2)/t];
             */
            gradphi0 = a_transpose.mult(z.scale(2.0));

            for (int row=0; row<u_size; row++){
                am_value = am.get(row,0);
                u_value = u.get(row,0);
                invdiff = 2*inv_t/(p_u_r2.get(row,0) - p_am_r2.get(row,0));

                gradux.set(row, 0, gradphi0.get(row,0) + am_value*invdiff);
                gradux.set(row+u_size, 0, lambda - u_value*invdiff);
            }

            normg = gradux.normF();

            pcgtol = Math.min(0.1, eta*gap/Math.min(1,normg));

            if (ntiter != 0 && pitr == 0){
                pcgtol = 0.1*pcgtol;
                //System.out.println("Updating PCG tolerance: " + pcgtol);
            }

            hessian = hessphi_coeffs(laplacian, d1, d2, hessian_size, coeffs_size, u_size);

            /*
             *
             */
            answers = linearPCG(hessian, gradux.scale(-1.0), dxu, d1, d2, pcgtol, pcgmaxi, t*tau);

            dx = answers.get(0);
            du = answers.get(1);
            dxu = answers.get(2);
            pitr = answers.get(3).get(0,0);

            /*
             *----------------------------------------------
             * Backtrack Line search
             *----------------------------------------------
             */
            logfSum = 0.0;
            for(int fi=0; fi < f.numRows(); fi++){
                logfSum+=Math.log(-f.get(fi));
            }

            phi = (z.transpose().mult(z)).get(0,0) + lambda*u.elementSum() - logfSum*inv_t;

            s=1.0;
            gdx = (gradux.transpose()).mult(dxu).get(0,0);

            backtrackLoop:
            for (lsiter=0; lsiter < max_ls_iter; lsiter++){

                new_x = am.plus(dx.scale(s));
                new_u = u.plus(du.scale(s));

                for(int ff=0; ff < u_size; ff++){
                    am_value = new_x.get(ff,0);
                    u_value = new_u.get(ff,0);
                    new_f.set(ff, 0, am_value - u_value);
                    new_f.set(ff+u_size, 0, -am_value - u_value);
                }

                if (max(new_f) < 0){

                    new_z = (a_matrix.mult(new_x)).minus(y);
                    logfSum = 0.0;

                    for(int fi=0; fi<new_f.getNumElements(); fi++){
                        logfSum += Math.log(-new_f.get(fi));
                    }

                    new_phi = (new_z.transpose().mult(new_z)).get(0,0)+lambda*new_u.elementSum()-logfSum*inv_t;

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

            f = new_f.copy();
            am = new_x.copy();
            u = new_u.copy();

            // dxu = new SimpleMatrix(hessian_size,1);
        }

        results.add(new double[coeffs_size]);
        results.add(r_vector);

        for (int j=0; j < coeffs_size; j++){
            results.get(0)[j] = am.get(j,0);
        }

        return results;
    }

    private static SimpleMatrix hessphi_coeffs(SimpleMatrix ata, SimpleMatrix d1, SimpleMatrix d2, int hessian_size, int coeffs_size, int u_size) {

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

    /**
     *
     * @return ArrayList<double[]> [coeffs] [r-values]
     */
    public ArrayList<double[]> moore_pr_L1(){

        ArrayList<double[]> results = new ArrayList<double[]>(2);

        double inv_d = 1.0/dmax;
        //  double inv_2d = 0.5*inv_d;

        int ns = (int) Math.round(qmax*dmax*INV_PI); //

        int coeffs_size = ns + 1;   //+1 for constant background
        //int coeffs_size = ns;

        double incr = 2.0;
        int r_limit = (int)(incr*ns)-1;
        double del_r = dmax/(ns*incr);
        double[] r_vector = new double[r_limit];

        for(int i=0; i< r_limit; i++){
            r_vector[i] = (i+1)*del_r;
        }


        double pi_d = Math.PI*dmax;
        XYDataItem tempData;

        /*
         * Interior Point Parameters
         */
        int MU = 2;            // updating parameter of t
        int max_nt_iter = 400; // maximum IPM (Newton) iteration

        /*
         * LINE SEARCH PARAMETERS
         */
        double alpha = 0.01;          // minimum fraction of decrease in the objective
        double beta  = 0.5;           // stepsize decrease factor
        int max_ls_iter = 400;    // maximum backtracking line search iteration

        int m = data.getItemCount();  // rows

        int n = coeffs_size;          // columns
        int u_size = r_limit + 1;
        int hessian_size = coeffs_size + u_size;

        //double delq = (qmax-qmin)/ns;

        double t0 = Math.min(Math.max(1, 1/lambda), n/0.001);
        double reltol = 0.001;
        double eta = 0.001;
        int pcgmaxi = 5000;

        double dobj = Double.NEGATIVE_INFINITY;
        double pobj = Double.POSITIVE_INFINITY;
        double s = Double.POSITIVE_INFINITY;
        double pitr = 0, pflg = 0, gap;

        double t = t0;
        double tau = 0.01;

        //Hessian and Preconditioner
        SimpleMatrix d1 = new SimpleMatrix(n,n);
        SimpleMatrix d2 = new SimpleMatrix(n,u_size);
        SimpleMatrix d3;

        SimpleMatrix hessian;

        DenseMatrix64F dxutemp = new DenseMatrix64F(hessian_size,1);
        CommonOps.fill(dxutemp,1);
        SimpleMatrix dxu = SimpleMatrix.wrap(dxutemp.copy());


        double[][] n_onesArray = new double[n][1];

        for (double[] row : n_onesArray) {
            Arrays.fill(row, 1);
        }

        ArrayList<SimpleMatrix> answers;

        SimpleMatrix p_u_r2;
        SimpleMatrix p_am_r2;


        /*
         * initialize u vector with 1's
         * size must include a_o and r_limit
         */
        DenseMatrix64F utemp = new DenseMatrix64F(u_size,1);
        CommonOps.fill(utemp,1);
        SimpleMatrix u = SimpleMatrix.wrap(utemp);

        SimpleMatrix y = new SimpleMatrix(m,1);

        SimpleMatrix p_dd_r_of_am;
        SimpleMatrix p_dd_r_new;
        SimpleMatrix z;  // = new SimpleMatrix(m,1);
        SimpleMatrix new_z;  // = new SimpleMatrix(m,1);
        SimpleMatrix nu; // = new SimpleMatrix(m,1);

        //
        // Initialize and guess am
        //
        double qd, inv_t;

        SimpleMatrix am = new SimpleMatrix(n,1);// am is 0 column
        am.set(0,0,0.0000001);

        for (int i=1; i < n; i++){
            //q = delq*0.5 + delq*(i-1);
            //qd = q*dmax;
            //am.set(i, 0, 0); // initialize coefficient vector a_m to zero
            am.set(i,0,0);
            //am.set(i,0,(q*slope + izero)*q/pi_d/i/Math.pow(-1,i+1)*(Math.pow(Math.PI*i,2) - Math.pow(qd, 2))/Math.sin(qd));
        }

        /*
         * create A matrix (design Matrix)
         */
        SimpleMatrix a_matrix = new SimpleMatrix(m,n);

        double qd2, pi_sq_n, q;

        for(int r=0; r<m; r++){ //rows, length is size of data
            tempData = data.getDataItem(r);

            q = tempData.getXValue();
            qd = q*dmax;
            qd2 = qd*qd;

            for(int c=0; c < n; c++){
                if (c == 0){
                    a_matrix.set(r, 0, 1);
                    //a_matrix.set(r, 0, tempData.getXValue());
                } else {
                    pi_sq_n = n_pi_squared[c];
                    a_matrix.set(r, c, pi_d * c * Math.pow(-1.0, c + 1) * Math.sin(qd) / (pi_sq_n - qd2));
                }



            }
            y.set(r, 0, tempData.getYValue());
        }

        SimpleMatrix a_transpose = a_matrix.transpose();
        SimpleMatrix gradphi0;
        SimpleMatrix gradux = new SimpleMatrix(hessian_size,1);

        //SimpleMatrix diagAtA;
        SimpleMatrix laplacian = a_transpose.mult(a_matrix).scale(2.0);

        SimpleMatrix dx;// = new SimpleMatrix(n,1);
        SimpleMatrix du;// = new SimpleMatrix(u_size,1);

        /*
         * BackTrack Line Search
         */
        SimpleMatrix new_u = new SimpleMatrix(n,1);
        SimpleMatrix new_x = new SimpleMatrix(u_size,1);

        SimpleMatrix f;
        f = new SimpleMatrix(u_size*2,1);
        for (int i=0; i < u_size*2; i++){
            f.set(i,0,-1);
        }
        SimpleMatrix new_f = new SimpleMatrix(u_size*2,1);

        int lsiter, r_locale;
        double maxAtnu;
        double normL1;
        double[] d3Array = new double[u_size];
        double phi, new_phi, logfSum, gdx, normg, pcgtol;
        double resultM, am_value, u_value, invdiff2, cnir_row, cnir_col, value_at_g1, value_at_g2, sum, diff, value_at_d1, invdiff;
        //initialize f

        String status;

        calculationLoop:
        for (int ntiter=0; ntiter < max_nt_iter; ntiter++){


            z = (a_matrix.mult(am)).minus(y);

            //------------------------------------------------------------
            // Calculate Duality Gap
            //------------------------------------------------------------
            nu = z.scale(2.0);
            // get max of At*nu
            // maxAtnu = max(vec);
            maxAtnu = inf_norm(a_transpose.mult(nu));

            if (maxAtnu > lambda){
                nu = nu.scale(lambda/(maxAtnu));
            }

            /*
             * calculate second derivative P(r) at specified r-values
             * length is the size of r_limit
             * ignore first element, a_0, of am vector (constant background)             *
             */

            p_dd_r_of_am = p_dd_r(am, r_vector, inv_d);

            normL1 = normL1(p_dd_r_of_am) + Math.abs(am.get(0,0));

            pobj = (z.transpose().mult(z)).get(0,0) + (lambda * normL1);

            /*
             *  dobj  =  max(-0.25*nu'*nu-nu'*y,dobj);
             */
            dobj = Math.max(( (nu.transpose().mult(nu)).get(0,0)*(-0.25) - ((nu.transpose().mult(y))).get(0,0) ), dobj);
            gap   =  pobj - dobj;

            //------------------------------------------------------------
            //       Shall we Stop?
            //------------------------------------------------------------
            if (gap/dobj < reltol) {
                //status = "Solved";
                //System.out.println("Solved " + gap/dobj);
                break calculationLoop;
            }


            //------------------------------------------------------------
            //       UPDATE t
            //------------------------------------------------------------

            if (s >= 0.5){
                t = Math.max(Math.min(2*n*MU/gap, MU*t), t);
            }
            inv_t = 1.0/t;

            //------------------------------------------------------------
            //      CALCULATE NEWTON STEP
            //------------------------------------------------------------
            //
            // gradphi = [At*(z*2)-(q1-q2)/t; lambda*ones(n,1)-(q1+q2)/t];
            // p_am_r = p_dd_r_of_am.extractVector(false,0);

            p_am_r2 = p_dd_r_of_am.elementMult(p_dd_r_of_am);
            p_u_r2 = u.elementMult(u);

            /*
             * Partitioned Matrix terms for Hessian
             * D1: n x n
             * D2: n x u_size
             */
            for(int row=0; row < u_size; row++){

                for(int col=0; col < u_size; col++){

                    if(row==0 && col==0){

                        am_value = am.get(0,0);
                        u_value = u.get(0,0);
                        invdiff = 1/(p_u_r2.get(0,0) - am_value*am_value);
                        invdiff2 = invdiff*invdiff*inv_t;

                        d1.set(0,0, 2*(u_value*u_value + am_value*am_value)*invdiff2);
                        d2.set(0,0, -4*u_value*am_value*invdiff2);

                    } else if (row==0) { //assemble nxn d1 matrix

                        if (col < coeffs_size){
                            d1.set(0,col, 0);
                        }

                        d2.set(0,col, 0);

                    } else if (col==0){

                        if (row < coeffs_size) {
                            d1.set(row,0, 0);
                            d2.set(row,0, 0);
                        }

                    } else if (row > 0 && col > 0) {

                        if ((col < coeffs_size) && (row < coeffs_size)) { // d1 matrix
                            //assemble nxn d1 matrix of mixed partials
                            value_at_d1 = 0.0;
                            // first element is gradient of a_o which does not depend on

                            for(int r=0; r < r_limit; r++){
                                // first element of r_vector is nonzero
                                cnir_row = c_ni_r(row, r_vector[r], inv_d);
                                cnir_col = c_ni_r(col, r_vector[r], inv_d);

                                diff = p_u_r2.get(r+1,0) - p_am_r2.get(r,0);
                                invdiff2 = 1/(diff*diff);

                                sum = p_u_r2.get(r+1,0) + p_am_r2.get(r,0);
                                value_at_d1 += 2*cnir_row*cnir_col*sum*invdiff2;
                            }

                            r_locale = row-1; // u_vector and r_vector are not indexed the same.
                            diff = p_u_r2.get(row,0) - p_am_r2.get(r_locale,0);
                            invdiff2 = 1/(diff*diff);

                            d1.set(row,col, value_at_d1*inv_t); // d1 only indexes to row length (coeffs)
                            d2.set(row,col, -4*c_ni_r(row, r_vector[r_locale], inv_d)*p_dd_r_of_am.get(r_locale,0)*u.get(col,0)*invdiff2*inv_t);

                        } else {
                           /*
                            * u_vector is r_limit + 1
                            * r_vector is r_limit
                            * +1 is from a_0 term (constant background)
                            */
                            r_locale = row-1; // u_vector and r_vector are not indexed the same.
                            diff = p_u_r2.get(row,0) - p_am_r2.get(r_locale,0);

                            invdiff2 = 1/(diff*diff);
                            if (row < coeffs_size){
                                d2.set(row, col, -4*c_ni_r(row, r_vector[r_locale], inv_d)*p_dd_r_of_am.get(r_locale,0)*u.get(col,0)*invdiff2*inv_t);
                            }
                        }
                    }
                }

                /*
                 * D3 Matrix
                 * D3: u_size x u_size
                 */
                if(row==0) {
                    /*
                     * constant background term, first element of a_m vector and u vector
                     */
                    am_value = am.get(0,0);
                    u_value = u.get(0,0);
                    invdiff = 1/(u_value*u_value - am_value*am_value);
                    invdiff2 = invdiff*invdiff;
                    d3Array[0] = 2*(u_value*u_value+am_value*am_value)*invdiff2*inv_t;
                } else {
                   /*
                    * u_vector is r_limit + 1
                    * r_vector is r_limit
                    * +1 is from a_0 term (constant background)
                    */
                    r_locale = row-1; // u_vector and r_vector are not indexed the same.
                    diff = p_u_r2.get(row,0) - p_am_r2.get(r_locale,0);
                    sum = p_u_r2.get(row,0) + p_am_r2.get(r_locale,0);

                    invdiff2 = 1/(diff*diff)*inv_t;
                    d3Array[row] = 2*sum*invdiff2;
                }
            }

            d3 = SimpleMatrix.diag(d3Array);

            // gradient
            gradphi0 = a_transpose.mult(z.scale(2.0));

            for (int row=0; row<u_size; row++){

                value_at_g1 = 0.0;
                if (row < 1) {

                    am_value = am.get(0,0);
                    u_value = u.get(0,0);
                    invdiff = 1/(p_u_r2.get(0,0)-am_value*am_value)*inv_t;
                    value_at_g1 = 2*am_value*invdiff;

                    gradux.set(row, 0, gradphi0.get(row,0) + value_at_g1);
                    value_at_g2 = -2*u_value*invdiff;

                } else {

                    if (row < coeffs_size){

                        for(int r=0; r < r_limit; r++){
                            cnir_row = c_ni_r(row, r_vector[r], inv_d);
                            diff = p_u_r2.get(r+1,0) - p_am_r2.get(r,0);
                            invdiff2 = 1/(diff*diff);

                            sum = p_u_r2.get(r+1,0) + p_am_r2.get(r,0);
                            value_at_g1 += 2*cnir_row*sum*invdiff2;
                        }

                        gradux.set(row, 0, gradphi0.get(row,0) + value_at_g1*inv_t);
                    }

                    diff = p_u_r2.get(row,0) - p_am_r2.get(row-1,0);
                    u_value = u.get(row,0);

                    value_at_g2 = -2*u_value/diff;
                }

                gradux.set(row+coeffs_size,0, lambda + value_at_g2*inv_t);
            }

            normg = gradux.normF();
            pcgtol = Math.min(0.1, eta*gap/Math.min(1,normg));

            if (ntiter != 0 && pitr == 0){
                pcgtol = 0.1*pcgtol;
            }

            hessian = hessphi(laplacian, d1, d2, d3, hessian_size, coeffs_size, u_size);

            /*
             *
             *
             */
            answers = linearPCG(hessian, gradux.scale(-1.0), dxu, d1, d2, pcgtol, pcgmaxi, t*tau);

            dx = answers.get(0);
            du = answers.get(1);
            dxu = answers.get(2);
            pitr = answers.get(3).get(0,0);

            /*
             *----------------------------------------------
             * Backtrack Line search
             *----------------------------------------------
             */
            logfSum = 0.0;
            for(int fi=0; fi < f.numRows(); fi++){
                logfSum+=Math.log(-1*f.get(fi));
            }

            phi = (z.transpose().mult(z)).get(0,0)+lambda*u.elementSum() - logfSum*inv_t;

            s=1.0;
            gdx = gradux.transpose().mult(dxu).get(0,0);

            backtrackLoop:
            for (lsiter=0; lsiter < max_ls_iter; lsiter++){
                /*
                 * using new_x, calculate new p_dd_r
                 */
                new_x = am.plus(dx.scale(s));
                p_dd_r_new = p_dd_r(new_x, r_vector, inv_d);
                new_u = u.plus(du.scale(s));

                for(int ff=0; ff<u_size; ff++){
                    if (ff<1){
                        am_value = new_x.get(0,0);
                        u_value = new_u.get(0,0);
                        new_f.set(0,0, am_value - u_value);
                        new_f.set(u_size,0, -am_value - u_value);
                    } else {
                        am_value = p_dd_r_new.get(ff-1,0);
                        u_value = new_u.get(ff,0);
                        new_f.set(ff,0, am_value - u_value);
                        new_f.set(u_size+ff,0, -am_value - u_value);
                    }
                }


                if (max(new_f) < 0){

                    new_z = (a_matrix.mult(new_x)).minus(y);
                    logfSum = 0.0;

                    for(int fi=0; fi<new_f.numRows(); fi++){
                        logfSum+=Math.log(-1*new_f.get(fi));
                    }

                    new_phi = (new_z.transpose().mult(new_z)).get(0,0)+lambda*new_u.elementSum()-logfSum*inv_t;

                    if (new_phi-phi <= alpha*s*gdx){
                        break backtrackLoop;
                    }
                }
                s = beta*s;
            } // end backtrack loop


            if (lsiter == max_ls_iter){
                //System.out.println("Max LS iteration ");
                break calculationLoop;
            }

            f.set(new_f);
            am.set(new_x);
            u.set(new_u);
        }


        results.add(new double[coeffs_size]);
        results.add(r_vector);

        for (int j=0; j < coeffs_size; j++){
            results.get(0)[j] = am.get(j,0);
        }

        results.get(0)[0] = am.get(0,0);

        for (int j=0;j < r_limit;j++){
            double pi_dmax_r = Math.PI/dmax*r_vector[j];
            resultM = 0;
            for(int i=1; i < n; i++){
                //System.out.println(i + " " + am.get(i,0));
                resultM += am.get(i,0)*Math.sin(pi_dmax_r*i);
            }
            //System.out.println(r_vector[j] + " " + 1/Math.PI*0.5*r_vector[j]*resultM);
        }
        return results;

    }



    private ArrayList<SimpleMatrix> linearPCG(SimpleMatrix designMatrix, SimpleMatrix bMatrix, SimpleMatrix initial, SimpleMatrix d1, SimpleMatrix d2, double pcgtol, int pcgmaxi, double tauT){

        ArrayList<SimpleMatrix> returnElements = new ArrayList<SimpleMatrix>();

        int i=0;

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
                    //preConditioner.set(row,row,2*designMatrix.get(row,row)+d1.get(row,row));
                    preConditioner.set(row,row,2+d1.get(row,row));
                    // d3.set(row,row,2+d1.get(row,row));
                }
                if (row != col){ // preserve diagonal entries and just add D1
                    preConditioner.set(row, col, d1.get(row, col));
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
        /*
        // from Boyd Start
        SimpleMatrix r1 = new SimpleMatrix(xu_size/2,1);
        SimpleMatrix r2 = new SimpleMatrix(xu_size/2,1);

        for(int row=0; row<xu_size/2; row++){
            r1.set(row,0,r.get(row,0));
            r2.set(row,0,r.get(row+xu_size/2,0));
        }

        SimpleMatrix top = p1.mult(r1).minus(p2.mult(r2));
        SimpleMatrix bot = p3.mult(r2).minus(p2.mult(r1));

        for(int row=0; row<xu_size/2; row++){
            d.set(row,0,top.get(row,0));
            d.set(row+xu_size/2,0,bot.get(row,0));
        }
        // from Boyd END
        */
        SimpleMatrix z_plus_1 ;//= new SimpleMatrix(xu_size,1);

        SimpleMatrix x_vector = new SimpleMatrix(n, 1);
        SimpleMatrix u_vector = new SimpleMatrix(xu_size-n, 1);

        double alpha;
        // stopping criteria
        double rkTzk = r.transpose().mult(z).get(0);

        double beta;
        //double stop = rkTzk*pcgtol*pcgtol;
        double stop = pcgtol;

        while( (i < pcgmaxi) && (rkTzk > stop) ){

            q = designMatrix.mult(p);
            alpha = rkTzk/(p.transpose().mult(q)).get(0);

            // invert each element of dTq;
            xu_vector = xu_vector.plus(p.scale(alpha));

            if (i < 50){
                r_plus_1 = bMatrix.minus(designMatrix.mult(xu_vector));
            } else {
                r_plus_1 = r.minus(q.scale(alpha)); // r_k+1 = r_k - alpha_k * A*p_k
            }

            /*
            // from Boyd Start
            for(int row=0; row<xu_size/2; row++){
                r1.set(row,0,r.get(row,0));
                r2.set(row,0,r.get(row+xu_size/2,0));
            }

            top = p1.mult(r1).minus(p2.mult(r2));
            bot = p3.mult(r2).minus(p2.mult(r1));

            for(int row=0; row<xu_size/2; row++){
                s_.set(row,0,top.get(row,0));
                s_.set(row+xu_size/2,0,bot.get(row,0));
            }
            // from Boyd END
             */

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


    private SimpleMatrix hessphi(SimpleMatrix ata, SimpleMatrix d1, SimpleMatrix d2, SimpleMatrix d3, int totalSize, int coeffSize, int u_size){

        int h = d2.numCols();
        int n = ata.numCols();

        SimpleMatrix hessian = new SimpleMatrix(n+h,n+h);

        SimpleMatrix t_ata;
        t_ata =  ata.plus(d1);
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

    private double inf_norm(SimpleMatrix vec){
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

    private double c_ni_r(int ni, double r, double inv_d){
        double theta = ni*r*inv_d*Math.PI;
        double cir;
        cir = inv_d*ni*Math.cos(theta) - Math.PI*r*0.5*inv_d*inv_d*ni*ni*Math.sin(theta);

        return cir;
    }


    public static SimpleMatrix p_dd_r(SimpleMatrix am, double[] r_vector, double inv_d){

        double r_value, pi_r_n_inv_d, pi_r_inv_d, cos_pi_r_n_inv_d, sin_pi_r_n_inv_d;
        double pi_inv_d = Math.PI*inv_d;
        double inv_2d = inv_d*0.5;
        double a_i;

        int r_limit = r_vector.length;

        SimpleMatrix p_dd_r = new SimpleMatrix(r_limit,1);

        int coeffs_size = am.getNumElements();

        double a_i_sum, product;

        for (int r=0; r < r_limit; r++){
            r_value = r_vector[r];
            pi_r_inv_d = pi_inv_d*r_value;
            a_i_sum = 0;

            for(int n=1; n < coeffs_size; n++){
                a_i = am.get(n,0);
                pi_r_n_inv_d = pi_r_inv_d*n;
                cos_pi_r_n_inv_d = inv_d*n*Math.cos(pi_r_n_inv_d);
                sin_pi_r_n_inv_d = Math.sin(pi_r_n_inv_d);
                product = pi_r_inv_d*inv_2d*n*n*sin_pi_r_n_inv_d;
                a_i_sum += a_i*cos_pi_r_n_inv_d + a_i*product;
            }
            p_dd_r.set(r,0, a_i_sum);
        }

        return p_dd_r;
    }


    public static double normL1(SimpleMatrix vec){
        double sum = 0;
        int size = vec.getNumElements();

        for(int i=0; i<size; i++){
            sum += Math.abs(vec.get(i));
        }
        return sum;
    }

    public static double max(SimpleMatrix vec){
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

    public static double min(SimpleMatrix vec){
        double initial = vec.get(0);
        int length = vec.getNumElements();
        double current;

        for (int i=1; i< length; i++){
            current = vec.get(i);
            if (current < initial){
                initial = current;
            }
        }

        return initial;
    }



}
