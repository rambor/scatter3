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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by robertrambo on 28/10/2016.
 */
public class Ellipse extends Model {

    private double integrationInterval;
    private int m_index, n_index;
    //private double[][] weights;
    private final double pi_half = Math.PI*0.5;
    private double contrast, c2, a2, b2;

    private double simpsonFactor;
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

        super(index, ModelType.ELLIPSOID, (4.0/3.0*Math.PI*radii[0]*radii[1]*radii[2]), solventContrast, particleContrasts, qvalues.length, 3);

        this.setFittedParamsByIndex(0, radii[0]);
        this.setFittedParamsByIndex(1, radii[1]);
        this.setFittedParamsByIndex(2, radii[2]);

        this.setString();

        this.contrast = particleContrasts[0] - solventContrast;

        this.integrationInterval = 1.0d/99.0d; // not sure what this should be
        // if interval is 0.01, then x,y is 100*100 = 10000 points
        // if interval is 0.001 then x,y is 1000*1000 = 1x10^6
        m_index = (int)(1.0/integrationInterval) + 1;
        n_index = m_index;
        simpsonFactor = 1.0/(9*m_index*n_index);
        this.setConstant(4*Math.PI*9.0*this.getVolume()*this.getVolume()*this.contrast*this.contrast);

        a2 = radii[0]*radii[0];
        b2 = radii[1]*radii[1];
        c2 = radii[2]*radii[2];

        this.deltaqr = deltaqr;
        this.calculateModelIntensities(qvalues);

    }


    public void printParams(){
        System.out.println(this.getIndex() + " PARAMS " + getRadius_a() + " " + getRadius_b() + " " + getRadius_c());
    }


    /**
     * Integrate using Simpson's rule
     * x and y are integrated from 0 to 1
     * @param qValues
     */
    @Override
    void calculateModelIntensities(Double[] qValues) {

        double maxR = Math.sqrt(c2 + a2);
        double qmaxRmax = qValues[qValues.length-1]*maxR;
        double qminRmin = qValues[0]*getRadius_a();

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
                sin = FastMath.sin(pihalfvalue);
                asin2 = b2*sin*sin;

                for(int y_value=0; y_value<n_index; y_value++){
                    y2=y_value*i2*y_value;
                    effective_qr = qValue*FastMath.sqrt(acos2 + asin2*(1.0d-y2) + c2*y2); // sqrt is r_effective
                    locale = (int)Math.floor(effective_qr*invDeltaR) - indexReset;
                    // slowest step by 10x
                    phi = (sinxvalues[locale] - effective_qr*cosxvalues[locale])/(effective_qr*effective_qr*effective_qr);
                    // phi = (FastMath.sin(effective_qr) - effective_qr*FastMath.cos(effective_qr))/(effective_qr*effective_qr*effective_qr);
                    sum += weights[x_value][y_value]*phi*phi;
                }
            }

            this.addIntensity(i, qValue*this.getConstant()*sum*simpsonFactor); // q*I(q)
        }
    }

    @Override
    String getConstrastString() {
        double c = contrast*contrast;
        return String.format("REMARK 265              SQUARED CONTRAST : %.6f %n", c);
    }

    public double getRadius_a(){return this.getFittedParamByIndex(0);}
    public double getRadius_b(){return this.getFittedParamByIndex(1);}
    public double getRadius_c(){return this.getFittedParamByIndex(2);}


    private void setString(){
        String newLines = String.format("REMARK 265  INDEX % 8d RADII %7.2f %7.2f %7.2f %n", getIndex(), getFittedParamByIndex(0), getFittedParamByIndex(1), getFittedParamByIndex(2));
        this.setStringToPrint(newLines);
    }

    public double[] calculatePr(int arraySize){
        //
        double[] values = new double[arraySize];
        double inva2, invc2, invb2;
        double diffx, diffy, diffz;
        Double[] axis = new Double[3];

        axis[0] = getRadius_a(); // a
        axis[1] = getRadius_b(); // b
        axis[2] = getRadius_c(); // c

        inva2 = 1.0/(axis[0]*axis[0]);
        invb2 = 1.0/(axis[1]*axis[1]);
        invc2 = 1.0/(axis[2]*axis[2]);

        double[] xvalues = new double[2];
        double[] yvalues = new double[2];
        double[] zvalues = new double[2];

        for(int i=0; i<arraySize; i++){
            // pick two random positions in ellipse
            // calculate distance
            int points = 0;
            while(points < 2){

                xvalues[points] = ThreadLocalRandom.current().nextDouble(-axis[0], axis[0]);
                yvalues[points] = ThreadLocalRandom.current().nextDouble(-axis[1], axis[1]);
                zvalues[points] = ThreadLocalRandom.current().nextDouble(-axis[2], axis[2]);

                if ((xvalues[points]*xvalues[points]*inva2 + yvalues[points]*yvalues[points]*invb2 + zvalues[points]*zvalues[points]*invc2) <= 1){
                    points++;
                }
            }

            diffx = xvalues[0] - xvalues[1];
            diffy = yvalues[0] - yvalues[1];
            diffz = zvalues[0] - zvalues[1];

            values[i] = Math.sqrt(diffx*diffx + diffy*diffy + diffz*diffz);
        }

        return values;
    }
}
