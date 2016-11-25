package version3.formfactors;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * Created by robertrambo on 28/10/2016.
 */
public class Ellipse extends Model {

    private double radius_a;
    private double radius_b;
    private double radius_c;
    private double integrationInterval;
    private int m_index, n_index;
    //private double[][] weights;
    private final double pi_half = Math.PI*0.5;
    private double contrast, c2, a2, b2;

    private double deltaqr;

    /**
     * Triaxial ellipsoid model
     * @param index
     * @param solventContrast
     * @param particleContrasts
     * @param radii
     * @param qvalues
     * @param deltaqr
     */
    public Ellipse(int index, double solventContrast, double[] particleContrasts, double[] radii, Double[] qvalues, double deltaqr) {


        super(index, ModelType.ELLIPSOID, (4.0/3.0*Math.PI*radii[0]*radii[1]*radii[2]), solventContrast, particleContrasts, qvalues.length);

        radius_a = radii[0];
        radius_b = radii[1];
        radius_c = radii[2];

        //df.setRoundingMode(RoundingMode.CEILING);

        this.contrast = particleContrasts[0] - solventContrast;
        this.integrationInterval = 1.0d/99.0d; // not sure what this should be
        // if interval is 0.01, then x,y is 100*100 = 10000 points
        // if interval is 0.001 then x,y is 1000*1000 = 1x10^6
        m_index = (int)(1.0/integrationInterval) + 1;
        n_index = m_index;

        //this.setConstant(9.0/this.getVolume()*this.contrast*this.contrast);
        this.setConstant(4*Math.PI*9.0*this.getVolume()*this.contrast*this.contrast);
        a2 = radius_a*radius_a;
        b2 = radius_b*radius_b;
        c2 = radius_c*radius_c;
        this.deltaqr = deltaqr;

        this.calculateModelIntensities(qvalues);
    }

    public void printParams(){
        System.out.println(this.getIndex() + " PARAMS " + radius_a + " " + radius_b + " " + radius_c);
    }

    public double getRadius_a(){ return radius_a;}
    public double getRadius_b(){ return radius_b;}
    public double getRadius_c(){ return radius_c;}

    @Override
    void calculateModelIntensities(Double[] qValues) {

        double maxR = Math.sqrt(c2 + a2);
        double qmaxRmax = qValues[qValues.length-1]*maxR;
        double qminRmin = qValues[0]*radius_a;

        int totalq = (int)Math.floor((qmaxRmax - qminRmin)/deltaqr) + 1;

        double[] cosxvalues=new double[totalq];
        double[] sinxvalues=new double[totalq];

        int ind = 0;
        for (double qr = qminRmin; qr <= qmaxRmax; qr += deltaqr){
            cosxvalues[ind] = FastMath.cos(qr);
            sinxvalues[ind] = FastMath.sin(qr);
            ind++;
        }
        int indexReset = (int)Math.floor(qminRmin/deltaqr) - 1;

        double[][]weights = new double[m_index][n_index];
        //double[] weights = new double[m_index*n_index];
        // create basis for rows for Simpson coefficients
        double[] rowBasis = new double[n_index];
        int lastCol = n_index -1;
        for(int col=0; col<n_index; col++){
            if (col==0 || col == lastCol){
                rowBasis[col] = 1;
            } else {
                if ( (col & 1) == 0 ) { // even
                    rowBasis[col] = 2;
                } else { // odd
                    rowBasis[col] = 4;
                }
            }
        }

        // fill weight matrix
        int lastRow = m_index-1;
        for(int row=1; row<lastRow; row++){

            boolean moddeven = false;
            if ( (row & 1) == 0 ) {
                moddeven = true;
            }

            for(int col=0; col<n_index; col++){
                if (moddeven){
                    weights[row][col] = 2*rowBasis[col];
                } else {
                    weights[row][col] = 4*rowBasis[col];
                }
            }
        }

        for(int col=0; col<n_index; col++){
            weights[0][col] = rowBasis[col];
            weights[lastRow][col] = rowBasis[col];
        }

        // calculate the
        double qValue, phi, cos, sin, acos2, asin2, y2, effective_qr;
        double sum, i2 = integrationInterval*integrationInterval, pihalfvalue;

        double invDeltaR = 1.0/deltaqr;
        int locale;
        //long startTime = System.currentTimeMillis();
        for(int i=0; i<getTotalIntensities(); i++){
            qValue = qValues[i];
            sum = 0;

            for(int x_value=0; x_value<m_index; x_value++){
                pihalfvalue = pi_half*x_value*integrationInterval;
                cos = FastMath.cos(pihalfvalue);
                acos2 = a2*cos*cos;
                //acos2 = radius_a*radius_a*cos2x.get(x_value);
                sin = FastMath.sin(pihalfvalue);
                asin2 = b2*sin*sin;
                //asin2 = radius_a*radius_a*sin2x.get(x_value);
                for(int y_value=0; y_value<n_index; y_value++){
                    y2=y_value*i2*y_value;
                    effective_qr = qValue*FastMath.sqrt(acos2 + asin2*(1.0d-y2) + c2*y2); // sqrt is r_effective
                    locale = (int)Math.floor(effective_qr*invDeltaR) - indexReset;
                    // slowest step by 10x
                    phi = (sinxvalues[locale] - effective_qr*cosxvalues[locale])/(effective_qr*effective_qr*effective_qr);
                    //phi = (FastMath.sin(effective_qr) - effective_qr*FastMath.cos(effective_qr))/(effective_qr*effective_qr*effective_qr);
                    //System.out.println(effective_qr + " PHI " + phi + " " + newphi + " " + locale + " size " + sinxvalues.size());
                    sum += weights[x_value][y_value]*phi*phi;
                }
            }

            this.addIntensity(i, qValue*this.getConstant()*sum); // q*I(q)
        }
//        System.out.println("TIME: " + (System.currentTimeMillis()-startTime));
    }



}
