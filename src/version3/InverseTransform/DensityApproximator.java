package version3.InverseTransform;

import net.jafama.FastMath;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public class DensityApproximator extends IndirectFT {
    private double del_r;
    double[] r_vector;
    double[] target; // data
    double[] invVariance; // data
    double[] qvalues; // data
    final double inv6 = 1.0d/6.0d;
    int r_vector_size;
    boolean positiveOnly;
    private RealMatrix designMatrix; // assume this is the design matrix
    private double beta;

    public DensityApproximator(XYSeries nonStandardizedData, XYSeries errors, double dmax, double qmax, double alpha, double beta, boolean useL1, int cBoxValue, boolean includeBackground) {
        super(nonStandardizedData, errors, dmax, qmax, alpha, useL1, cBoxValue, includeBackground);


        this.beta = beta;

    }

    @Override
    void calculateIzeroRg() {

    }

    @Override
    void setPrDistribution() {

    }

    @Override
    public double calculateQIQ(double qvalue) {
        return 0;
    }

    @Override
    public double calculateIQ(double qvalue) {
        return 0;
    }

    @Override
    public double calculatePofRAtR(double r_value, double scale) {
        return 0;
    }

    @Override
    public void estimateErrors(XYSeries fittedData) {

    }

    @Override
    void createDesignMatrix(XYSeries datasetInuse) {

        /*
          alpha sets value in the gammaPDF, must be an integer > -1
          PDF needs to be evaluated at specific r-values
          number of r-values is determined by ns, this sets the series expansion for the polynomial
         */
        ns = (int) Math.ceil(qmax*dmax*INV_PI);  // set expansion order of the Laguerre Series
        coeffs_size = this.includeBackground ? ns + 1 : ns;   //+1 for constant background, +1 to include dmax in r_vector list

        rows = datasetInuse.getItemCount();    // rows
        target = new double[rows];
        invVariance = new double[rows];
        qvalues= new double[rows];

        r_vector_size = ns; // no background implies coeffs_size == ns

        /*
         * effective bin width of the Pr-distribution
         */
        del_r = dmax/(double)(r_vector_size);
        // if I think I can squeeze out one more Shannon Number, then I need to define del_r by dmax/ns+1
        r_vector = new double[r_vector_size];

        for(int i=0; i < r_vector_size; i++){ // calculate at midpoints
            r_vector[i] = (0.5 + i)*del_r; // dmax is not represented in this set
        }


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
                    a_matrix.set(row, col, FastMath.sin(r_value*tempData.getXValue()) / r_value);
                    designMatrix.setEntry(row, col, FastMath.sin(r_value*tempData.getXValue()) / r_value);
                }

                y_vector.set(row,0,tempData.getYValue()); //set data vector
                target[row] = tempData.getYValue();
                qvalues[row] = tempData.getXValue();
                invVariance[row] = 1.0/standardVariance.getY(row).doubleValue();
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
                target[row] = tempData.getYValue();
                qvalues[row] = tempData.getXValue();
                invVariance[row] = 1.0/standardVariance.getY(row).doubleValue();
            }
        }
    }


    private double laguerre(double beta_rvalue, int index){

        switch(index){
            case 0:
                return 1;
            case 1:
                return -beta_rvalue + alpha + 1;
            case 2:
                return beta_rvalue*beta_rvalue*0.5 - (alpha +2)*beta_rvalue + (alpha+2)*(alpha+1)*0.5;
            case 3:
                double squared = beta_rvalue*beta_rvalue;
                return -squared*beta_rvalue*inv6 + (alpha+3)*squared - (alpha+2)*(alpha+3)*beta_rvalue*0.5 + (alpha+1)*(alpha+2)*(alpha+3)*inv6;
            case 4:
                return 1;
            case 5:
                return 1;
            case 6:
                return 1;
            case 7:
                return 1;
            case 8:
                return 1;

            default:
                //leave the restaurant

        }

        return 0.0d;
    }
}
