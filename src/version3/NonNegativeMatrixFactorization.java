package version3;

import net.jafama.FastMath;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;

/**
 * Created by robertrambo on 14/09/2016.
 * Takes in a collection of XYSeries where each series has common X-values
 * performs NMF
 */
public class NonNegativeMatrixFactorization extends SwingWorker {

    private ArrayList<Number> qvalues;
    private DenseMatrix64F gradH;
    private DenseMatrix64F gradW;
    private SimpleMatrix matrixH;
    private SimpleMatrix matrixW;
    private SimpleMatrix matrixA;
    private SimpleMatrix matrixObservations;
    private int method=0;

    private double alpha;
    private double beta;
    private int st_rule = 1;
    private int samplingRounds;
    private int max_iter=100;
    private int min_iter=10;


    public NonNegativeMatrixFactorization(){

    }

    /**
     * Constructor
     * Create matrix of values based on common-q values in collection
     * First dataset in collection is Reference set
     * DOES NOT INTERPOLATE MISSING VALUES
     * @param collections
     * @param alpha
     * @param beta
     */
    public NonNegativeMatrixFactorization(Collection collections, double alpha, double beta){
        // need to make a matrix of nonnegative values
        qvalues = new ArrayList<>();

        this.alpha = alpha;
        this.beta = beta;

        int totalsets = collections.getDatasetCount();

        int startIndexCollection=0;
        for(int i=0; i<totalsets; i++){
            if (collections.getDataset(i).getInUse()){
                Dataset dataset = collections.getDataset(i);
                int start = dataset.getStart();
                int endAt = dataset.getAllData().indexOf(dataset.getData().getX(dataset.getEnd()-1));

                for(int m=start; m<endAt; m++) {
                    if (dataset.getAllData().getX(m).doubleValue() > 0){
                        qvalues.add(dataset.getAllData().getX(m));
                    }
                }
                startIndexCollection=i;
                break;
            }
        }

        int totalSetsSelected=1;
        // remove any q-values with negative intensities
        startIndexCollection++;
        XYDataItem temp;
        for(int i=startIndexCollection; i<totalsets; i++){
            if (collections.getDataset(i).getInUse()){
                Dataset dataset = collections.getDataset(i);
                totalSetsSelected++;
                int total = dataset.getAllData().getItemCount();
                for(int m=0; m<total; m++) {
                    temp = dataset.getAllData().getDataItem(m);
                    if (temp.getYValue() < 0 && qvalues.indexOf(temp.getXValue()) > -1){
                        qvalues.remove(temp.getXValue());
                    }
                }
            }
        }

        // build matrix with given qvalues;
        // rows = qvalues
        // cols totalSetsSelected
        int column=0;
        matrixObservations = new SimpleMatrix(totalSetsSelected, qvalues.size());

        for(int i=0; i<totalsets; i++){
            if (collections.getDataset(i).getInUse()){
                Dataset dataset = collections.getDataset(i);
                XYSeries tempData = dataset.getAllData();

                int rows=0;
                int indexToUse;
                for(int m=0; m<qvalues.size(); m++) {
                    indexToUse = tempData.indexOf(qvalues.get(m));
                    matrixObservations.set(rows, column, tempData.getY(indexToUse).doubleValue());
                    rows++;
                }
                column++;
            }
        }

        // build a standardized dataset



    }


    @Override
    protected Object doInBackground() throws Exception {

        // perform sampling from matrix
        // return average and median?

        // perform NMF on entire dataset


        double stopValue;
        //
        for(int iter=0; iter<max_iter; iter++){

            // calculate H-matrix (use multi-threaded)

            // calculate W-matrix

            // calculate gradient

//        [H,gradHX,subIterH] = nnlsm([W;sbetaI],[A;zerokn],H,par.nnls_solver);
//        [W,gradW,subIterW] = nnlsm([H';salphaI],[A';zerokm],W',par.nnls_solver);, W=W';, gradW=gradW';
//        gradH = (W'*W)*H - W'*A + par.beta*H;


            if (iter>min_iter){
                //stopValue = getStopCriterion()
//        SC = getStopCriterion(ST_RULE,A,W,H,par.type,par.alpha,par.beta,gradW,gradH);
//        if (par.verbose && (tTotal > par.max_time)) || (~par.verbose && ((cputime-tStart)>par.max_time))
//          break;
//        elseif (SC/initSC <= par.tol)
//          SCconv = SCconv + 1;
//          if (SCconv >= SC_COUNT), break;, end
//        else
//          SCconv = 0;
//        end
            }
        }



        return null;
    }


    /**
     * set number of sampling rounds, should be large, maybe based on coupon collector problem
     * @param rounds
     */
    public void setSamplingRounds(int rounds){
        this.samplingRounds = rounds;
    }


    private double getInitCriterion(int stopRule, DenseMatrix64F W, DenseMatrix64F H){

        int m = W.getNumRows();
        int k = W.getNumCols();
            k = H.getNumRows();
        int n = H.getNumCols();
        int numAll = m*k + k*n;

        // [gradW,gradH] = getGradient(A,W,H,type,alpha,beta);
        // setGradient()

        double retVal=0;

        switch (stopRule){
            case 1:
                retVal = this.getFrobeniusNormWH()/(double)numAll;
                break;
            case 2:
                retVal = this.getFrobeniusNormWH();
                break;
            case 3:
                retVal = getStopCriterion(3, W, H);
                break;
            default:
                retVal = 1;
                break;
        }

        return retVal;
    }



    private double getStopCriterion(int stopRule, DenseMatrix64F W, DenseMatrix64F H){
        double retVal = 0;

        //if nargin~=9
        //        [gradW,gradH] = getGradient(A,W,H,type,alpha,beta);
        //end

        switch (stopRule){
            case 1:

                break;
            case 2:

                break;
            case 3:

                break;
            default:
                retVal = 1e100;
                break;
        }

        return retVal;
    }

//    function retVal = getStopCriterion(stopRule,A,W,H,type,alpha,beta,gradW,gradH)
//            % STOPPING_RULE : 1 - Normalized proj. gradient
//    %                 2 - Proj. gradient
//    %                 3 - Delta by H. Kim
//    %                 0 - None (want to stop by MAX_ITER or MAX_TIME)
//    if nargin~=9
//            [gradW,gradH] = getGradient(A,W,H,type,alpha,beta);
//    end
//
//    switch stopRule
//    case 1
//    pGradW = gradW(gradW<0|W>0); // extract all elements of gradW
//    pGradH = gradH(gradH<0|H>0);
//    pGrad = [gradW(gradW<0|W>0); gradH(gradH<0|H>0)];
//    pGradNorm = norm(pGrad);
//    retVal = pGradNorm/length(pGrad);
//    case 2
//    pGradW = gradW(gradW<0|W>0);
//    pGradH = gradH(gradH<0|H>0);
//    pGrad = [gradW(gradW<0|W>0); gradH(gradH<0|H>0)];
//    retVal = norm(pGrad);
//    case 3
//    resmat=min(H,gradH); resvec=resmat(:);
//    resmat=min(W,gradW); resvec=[resvec; resmat(:)];
//    deltao=norm(resvec,1); %L1-norm
//            num_notconv=length(find(abs(resvec)>0));
//    retVal=deltao/num_notconv;
//    case 0
//    retVal = 1e100;
//    end
//  end          end

    private void updateGradient(DenseMatrix64F W, DenseMatrix64F H){

        DenseMatrix64F HH_inner_product;
        DenseMatrix64F WW_inner_product;
        DenseMatrix64F H_transpose;
        DenseMatrix64F W_transpose;
        DenseMatrix64F AH_transpose;
        DenseMatrix64F WHH;
        DenseMatrix64F WWH;
        DenseMatrix64F WA;

//        CommonOps.multInner(H, HH_inner_product);
//        CommonOps.transpose(H, H_transpose);
//        CommonOps.mult(W, HH_inner_product, WHH);
//        CommonOps.mult(A_data_matrix, H_transpose, AH_transpose);
//
//        CommonOps.transpose(W, W_transpose);
//        CommonOps.mult(W_transpose, W, WW_inner_product);
//        CommonOps.mult(WW_inner_product, H, WWH);
//        CommonOps.mult(W_transpose, A_data_matrix, WA);


        switch (method) {
            case 1: // plain
                //gradW = W*(H*H') - A*H';
                //gradH = (W'*W)*H - W'*A;
//                CommonOps.subtract(WHH, AH_transpose, gradW);
//                CommonOps.subtract(WWH, WA, gradH);

                break;
            case 2:  // regularized
                DenseMatrix64F alphaW;
                DenseMatrix64F betaH;
                //gradW = W*(H*H') - A*H' + alpha*W;
                //gradH = (W'*W)*H - W'*A + beta*H;
//                CommonOps.scale(alpha, W, alphaW);
//                CommonOps.subtractEquals(WHH, AH_transpose);
//                CommonOps.subtract(WHH, alphaW, gradW);
//
//                CommonOps.scale(beta, H, betaH);
//                CommonOps.subtractEquals(WWH, WA);
//                CommonOps.subtract(WWH, betaH, gradH);

                break;
            default: // sparse
//                k=size(W,2);
//                betaI = beta*ones(k,k);
//                gradW = W*(H*H') - A*H' + alpha*W;
//                gradH = (W'*W)*H - W'*A + betaI*H;
                break;
        }
    }

    private double getFrobeniusNormWH(){
        double gh = NormOps.normF(gradH);
        double gw = NormOps.normF(gradW);
        return Math.sqrt(gh*gh + gw*gw);
    }


    //
    // Non-negative least squares via interior point method
    // make new instance of each solver
    // use values in free as starting values for search
    //
    public class Solver implements Runnable {

        private final DenseMatrix64F unknowns;
        private final SimpleMatrix y_matrix;
        private final DenseMatrix64F designMatrix;
        private SimpleMatrix x_vector;
        private float lambda;
        private int rows_obs;
        private int cols_obs;
        private int total_rows_unknown;
        private String status;

        /**
         *
         * @param observations m x 1 matrix
         * @param design m x n matrix
         * @param free m x 1 matrix
         * @param alphaBeta
         */
        public Solver(DenseMatrix64F observations, DenseMatrix64F design, DenseMatrix64F free, float alphaBeta){ //
            this.y_matrix = new SimpleMatrix(observations);
            this.designMatrix = design;
            this.unknowns = free;
            lambda = alphaBeta;
            this.rows_obs = observations.getNumRows();
            this.cols_obs = 1;
            this.total_rows_unknown = unknowns.getNumRows();

            // total_rows_unkown == rows_obs
        }



        @Override
        public void run() {
           /*
            * Interior Point Parameters
            */
            int MU = 2;                    // updating parameter of t
            int max_nt_iter = 400;         // maximum IPM (Newton) iteration

           /*
            * LINE SEARCH PARAMETERS
            */
            double alpha = 0.01;            // minimum fraction of decrease in the objective
            double beta  = 0.5;             // stepsize decrease factor
            int max_ls_iter = 100;          // maximum backtracking line search iteration

            double t0 = Math.min(Math.max(1, 1.0/lambda), 2*total_rows_unknown/0.001);
            double reltol = 0.001;
            double eta = 0.001;
            int pcgmaxi = 5000;

            double dobj = Double.NEGATIVE_INFINITY;
            double pobj = Double.POSITIVE_INFINITY;
            double s = Double.POSITIVE_INFINITY;
            double pitr = 0, pflg = 0, gap;

            double t = t0;

            // create matrices
            //Hessian and preconditioner
            SimpleMatrix d1;// = new SimpleMatrix(total_rows_unknown,total_cols_unknown);
            SimpleMatrix dx = new SimpleMatrix(total_rows_unknown, 1);

            ArrayList<SimpleMatrix> answers;

            // min: |(A*X-Y)|
            // W is the unknown parameter matrix
            // Initialize and parameter matrix W
            //
            double inv_t;

            //SimpleMatrix x_matrix = new SimpleMatrix(total_rows_unknown,total_cols_unknown);
            x_vector = new SimpleMatrix(unknowns);

           /*
            * create A matrix (design Matrix)
            */
            SimpleMatrix a_matrix = new SimpleMatrix(designMatrix);

            SimpleMatrix a_transpose = a_matrix.transpose();
            final SimpleMatrix laplacian = (a_transpose.mult(a_matrix)).scale(2.0);

            SimpleMatrix gradphi0, gradphi;
            SimpleMatrix g_matrix = new SimpleMatrix(total_rows_unknown, 1);

           /*
            * Backtracking Line Search
            */
            SimpleMatrix z_matrix;
            double zTz;
            SimpleMatrix nu;

            SimpleMatrix f_matrix = x_vector; // has same dimensions as x, all values must be positive
            SimpleMatrix new_z;
            SimpleMatrix new_x = new SimpleMatrix(total_rows_unknown,1);
            SimpleMatrix new_f = new SimpleMatrix(total_rows_unknown, 1);

            int lsiter;
            double minAnu;

            double phi, new_phi, logfSum, gdx, pcgtol;

            calculationLoop:
            for (int ntiter=0; ntiter < max_nt_iter; ntiter++){

                z_matrix = (a_matrix.mult(x_vector)).minus(y_matrix);

                //------------------------------------------------------------
                // Calculate Duality Gap
                //------------------------------------------------------------
                nu = z_matrix.scale(2.0);

                // get max of At*nu
                minAnu = min(a_transpose.mult(nu));
                if (minAnu < -lambda){
                    nu = nu.scale(lambda/(-minAnu));
                }

               /*
                * PRIMAL OBJECTIVE
                */
                zTz = z_matrix.transpose().mult(z_matrix).get(0,0);
                pobj = zTz + lambda*NormOps.normP1(unknowns);

               /*
                * DUAL OBJECTIVE
                * dobj  =  max(-0.25*nu'*nu-nu'*y,dobj);
                */
                //dobj = Math.max( rootValue*rootValue*(-0.25) - (nu.transpose().mult(y_matrix)).elementSum(), dobj);
                dobj = Math.max((-0.25)*nu.transpose().mult(nu).get(0,0) - nu.transpose().mult(y_matrix).get(0,0), dobj);
                gap   = pobj - dobj;

                //System.out.println("GAP: " + gap + " : " + " | ratio " + gap/dobj + " reltol " + reltol);
                //------------------------------------------------------------
                //       Shall we Stop?
                //------------------------------------------------------------
                if (gap/Math.abs(dobj) < reltol) {
                    status = String.format("SOLVED : gap/dobj => %.5f < %.5f ( %i )\n", (gap/dobj), reltol, ntiter);
                    break calculationLoop;
                }

                //------------------------------------------------------------
                //       UPDATE t
                //------------------------------------------------------------
                if (s >= 0.5){
                    t = Math.max(Math.min(2*total_rows_unknown*MU/gap, MU*t), t);
                }
                inv_t = 1.0/t;

                //------------------------------------------------------------
                //      CALCULATE NEWTON STEP
                //------------------------------------------------------------
                //

               /*
                * Partitioned Matrix terms for Hessian
                * D1: n x n
                */
                d1 = x_vector.elementPower(-2).scale(inv_t);
                // hessian = laplacian + diag(d1)
               /*
                * Gradient
                * gradphi = [At*(z*2)-1/t*1/X; lambda*ones(n,1)-(q1+q2)/t];
                */
                gradphi0 = a_transpose.mult(z_matrix.scale(2.0));

                for (int ii=0; ii<total_rows_unknown; ii++){
                   g_matrix.set(ii, 0, (lambda - 1.0/x_vector.get(ii,0)*inv_t));
                }

                gradphi = gradphi0.plus(g_matrix); //
                pcgtol = Math.min(0.1, eta*gap/Math.min(1.0, gradphi.normF()));

                if (ntiter != 0 && pitr == 0){
                    pcgtol = 0.1*pcgtol;
                    //System.out.println("Updating PCG tolerance: " + pcgtol);
                }

               /*
                * laplacian = 2ATA
                */
                answers = linearPCGPositiveOnly(laplacian, gradphi.scale(-1.0), dx, d1, pcgtol, pcgmaxi);

                dx = answers.get(0);
                pitr = answers.get(1).get(0,0);

               /*
                *----------------------------------------------
                * Backtrack Line search
                *----------------------------------------------
                */
//                logfSum = 0.0;
                logfSum = logSum(f_matrix);

                phi = zTz + lambda*NormOps.normP1(x_vector.getMatrix()) - logfSum*inv_t;

                s=1.0;
                gdx = (gradphi.transpose()).mult(dx).elementSum(); // single number

                backtrackLoop:
                for (lsiter=0; lsiter < max_ls_iter; lsiter++){

                    new_x = x_vector.plus(dx.scale(s)); // adjust size of dx and add to x_matrix

                    new_f = new_x.copy();

                    if (max(new_f.scale(-1.0)) < 0){
                        new_z = (a_matrix.mult(new_x)).minus(y_matrix); // calculate residual
                        logfSum = logSum(new_f);
                        new_phi = new_z.transpose().mult(new_z).get(0,0) + lambda*NormOps.normP1(new_x.getMatrix()) - logfSum*inv_t; // calculate new cost function value
                        if (new_phi-phi <= alpha*s*gdx){
                            System.out.println("Breaking BackTrackLoop");
                            break backtrackLoop;
                        }
                    }
                    s = beta*s;
                } // end backtrack loop

                if (lsiter == max_ls_iter){
                    System.out.println("Max LS iteration: Failed");
                    break calculationLoop;
                }

                f_matrix = new_f.copy();
                x_vector = new_x.copy();

                status = String.format("STATUS : gap/dobj => %.5f <=> %.5f ( %d )\n", (gap/dobj), reltol, ntiter);
            }

            System.out.println("X_VEC : " + status);
            x_vector.print();
        }

        private SimpleMatrix getXMatrix(){
            return x_vector;
        }

        private void printXMatrix(){

        }

        private double logSum(SimpleMatrix matrix){
            int rows = matrix.numRows();
            int cols = matrix.numCols();
            double sum=0;
            for(int fi=0; fi < rows; fi++){
                for(int fj=0; fj < cols; fj++){
                    sum+=FastMath.log(matrix.get(fi,fj));
                }
            }
            return sum;
        }


        /**
         * get max value of matrix
         * @param new_f
         * @return
         */
        private double max(SimpleMatrix new_f) {
            double max = new_f.get(0,0);
            double temp;
            int rows = new_f.numRows();
            int cols = new_f.numCols();
            for(int i=0; i<rows; i++){
                for(int j=0; j<cols; j++){
                    temp = new_f.get(i,j);
                    if(temp > max){
                        max = temp;
                    }
                }
            }
            return max;
        }

        /**
         * Determine min of the column vector
         * @return
         */
        private double min(SimpleMatrix vec){
            int length = vec.numRows();
            double minVal = vec.get(0,0);
            double test;

            for (int i=1; i<length; i++){
                test = vec.get(i,0);
                if (test < minVal){
                    minVal = test;
                }
            }
            return minVal;
        }


        /**
         * Preconditioned conjugate gradient
         * hessian*initial = bMatrix
         * solve for initial
         *
         * @param laplacian 2*ATA
         * @param bMatrix   target (observations) column-vector
         * @param initial   parameter column-vector
         * @param d1        1/t*(1/x^2) column-vector
         * @param pcgtol
         * @param pcgmaxi
         * @return
         */
        private ArrayList<SimpleMatrix> linearPCGPositiveOnly(SimpleMatrix laplacian, SimpleMatrix bMatrix, SimpleMatrix initial, SimpleMatrix d1, double pcgtol, int pcgmaxi){

            ArrayList<SimpleMatrix> returnElements = new ArrayList<SimpleMatrix>();

            int pcg_index=0;

            SimpleMatrix a_matrix = laplacian.copy();
            SimpleMatrix preConditioner = laplacian.copy();

            //int pre_cols = preConditioner.numCols();
            int d1_rows = d1.numRows();

           /*
            * P = M  D1
            * Preconditioner, P is Hessian with first block set to:
            * M = diagonal entries of laplacian (= 2*ATA)
            */
            for(int row=0; row < d1_rows; row++){
                for(int col=0; col < d1_rows; col++){
                    if (row == col){
                        //preConditioner.set(row, row, 2+d1.get(row,0));
                        preConditioner.set(row, row, preConditioner.get(row,row)+d1.get(row,0));
                        a_matrix.set(row, row, a_matrix.get(row,row)+d1.get(row,0));
                    } else { // preserve diagonal entries and just add D1
                        preConditioner.set(row, col, 0);
                    }
                }
            }

            /**
             * calculate initial conjugate gradient parameters
             */
            SimpleMatrix invertM = preConditioner.invert();
            SimpleMatrix r = bMatrix.minus(a_matrix.mult(initial)); // residual
            SimpleMatrix r_plus_1;
            SimpleMatrix z = invertM.mult(r); // search direction
            SimpleMatrix p = z.copy();

            SimpleMatrix q;
            SimpleMatrix xu_vector = initial.copy();
            SimpleMatrix z_plus_1 ;

            double alpha;
            // stopping criteria
            double rkTzk = (r.transpose().mult(z)).elementSum();

            double beta;
            //double stop = rkTzk*pcgtol*pcgtol;
            double stop = pcgtol;

            while( (pcg_index < pcgmaxi) && (rkTzk > stop) ){

                q = a_matrix.mult(p);
                alpha = rkTzk/(p.transpose().mult(q)).get(0,0);

                // invert each element of dTq;
                xu_vector = xu_vector.plus(p.scale(alpha));

                if (pcg_index < 50){
                    r_plus_1 = bMatrix.minus(a_matrix.mult(xu_vector));
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
                rkTzk = (r.transpose().mult(z_plus_1)).get(0,0);
                z = z_plus_1.copy();
                pcg_index++;
            }

            //System.out.println("PCG STOP: " + stop + " > " + rkTzk + " at " + pcg_index);
            SimpleMatrix x_vector = xu_vector.copy();

            // set final iterate number
            SimpleMatrix pitr = new SimpleMatrix(1,1);
            pitr.set(0,0,pcg_index);

            returnElements.add(x_vector);  // 0
            returnElements.add(pitr);      // 1
            return returnElements;
        }

    }


    public void test(){
        /**
         * Design Matrix
         *
         */

        int rows=5;
        int cols=2;
        SimpleMatrix design = new SimpleMatrix(5,2);
        // random numbers
        for(int i=0; i<rows; i++){
            for(int j=0; j<cols; j++){
                design.set(i,j, Math.random());
            }
        }

        design.set(0,0,0.997);
        design.set(0,1,0.214);

        design.set(1,0,0.427);
        design.set(1,1,0.144);

        design.set(2,0,0.082);
        design.set(2,1,0.176);

        design.set(3,0,0.443);
        design.set(3,1,0.567);

        design.set(4,0,0.697);
        design.set(4,1,0.897);

        System.out.println("DESIGN MATRIX: ");
        design.print();
        /**
         * Free Matrix
         * [ 0.1 0.7 ]
         * [ 0.9 0.3 ]
         *
         */
        SimpleMatrix free = new SimpleMatrix(cols,1);
        free.set(0,0,0.13);
        free.set(1,0,1.07);

        SimpleMatrix guess = new SimpleMatrix(cols,1);
        guess.set(0,0,1);
        guess.set(1,0,1);

        SimpleMatrix obs = design.mult(free);
        System.out.println("PRINT OBS ");
        obs.print();

        Solver temp = new Solver(obs.getMatrix(), design.getMatrix(), guess.getMatrix(), (float) 0.001);
        temp.run();

    }

}
