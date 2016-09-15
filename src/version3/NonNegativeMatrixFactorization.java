package version3;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;

/**
 * Created by robertrambo on 14/09/2016.
 */
public class NonNegativeMatrixFactorization extends SwingWorker {

    private ArrayList<Number> qvalues;
    private DenseMatrix64F gradH;
    private DenseMatrix64F gradW;
    private DenseMatrix64F A_data_matrix;
    private int method=0;

    private double alpha;
    private double beta;
    private int st_rule = 1;

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






    }

    @Override
    protected Object doInBackground() throws Exception {
        return null;
    }

    private double getInitCriterion(int stopRule, DenseMatrix64F W, DenseMatrix64F H){

        int m = W.getNumRows();
        int k = W.getNumCols();
            k = H.getNumRows();
        int n = H.getNumCols();
        int numAll = m*k + k*n;

        // [gradW,gradH] = getGradient(A,W,H,type,alpha,beta);

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

}
