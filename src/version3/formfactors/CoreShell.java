package version3.formfactors;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.util.FastMath;

/**
 * Created by robertrambo on 26/11/2016.
 */
public class CoreShell extends Model{

    private double shellRadius_a; // minor axis
    private double shellRadius_c; // major axis
    private double corea2;
    private double corec2;
    private double shella2;
    private double shellc2;

    private double coreContrast;
    private double shellContrast;
    private double volumeCore;
    private double volumeShell;
    private boolean isEmpty=false;
    private double thickness;

    // fixed thickness shell?

    /**
     *
     * @param index
     * @param solventContrast
     * @param particleContrasts 1st is shell, 2nd is core
     * @param radii
     * @param thickness
     * @param qvalues
     */
    public CoreShell(int index, double solventContrast, double[] particleContrasts, double[] radii, double thickness, Double[] qvalues) {

        super(index, ModelType.CORESHELL, (4.0/3.0*Math.PI*radii[0]*radii[0]*radii[1]), solventContrast, particleContrasts, qvalues.length, 3);

        // prolate
        this.volumeCore = this.getVolume();
        this.volumeShell = 4.0/3.0*Math.PI*(radii[0]+thickness)*(radii[0]+thickness)*(radii[1]+thickness);

        if (radii[0] < radii[1]){ // oblate
            this.volumeCore = (4.0/3.0*Math.PI*radii[0]*radii[1]*radii[1]);
            this.volumeShell = 4.0/3.0*Math.PI*(radii[0]+thickness)*(radii[1]+thickness)*(radii[1]+thickness);
        }

        this.setVolume(this.volumeShell);

        this.setFittedParamsByIndex(0, radii[0]);  // r_a core
        this.setFittedParamsByIndex(1, radii[1]);  // r_c core
        this.setFittedParamsByIndex(2, thickness); // thickness
        this.setString();

        shellRadius_a = this.getRadius_a() + thickness;
        shellRadius_c = this.getRadius_c() + thickness;

        corea2 = this.getRadius_a()*this.getRadius_a();
        corec2 = this.getRadius_c()*this.getRadius_c();

        shella2 = shellRadius_a*shellRadius_a;
        shellc2 = shellRadius_c*shellRadius_c;

        this.shellContrast = 3.0d*(particleContrasts[0] - solventContrast)*volumeShell;
        this.coreContrast = 3.0d*(particleContrasts[1] - particleContrasts[0])*volumeCore;

        if (solventContrast==particleContrasts[1]){
            isEmpty=true;
        }

        this.setConstant(4.0*Math.PI*this.volumeShell);
        this.calculateModelIntensities(qvalues);
    }


    @Override
    void calculateModelIntensities(Double[] qValues) {

        // old method below using integrator
        SimpsonIntegrator t = new SimpsonIntegrator();

        double qValue;
        double constant = getConstant();

        // prolate
        for(int i=0; i<getTotalIntensities(); i++){
            qValue = qValues[i];
            EllipsoidUnivariateFunction func = new EllipsoidUnivariateFunction(qValue);
            float integral = (float)t.integrate(100000000, func, 0, 1);
            this.addIntensity(i, qValue*constant*integral);
        }
    }


    /**
     * Prolate ellipsoid CoreShellModel
     * alpha is integrated from 0 to 1
     */
    public class EllipsoidUnivariateFunction implements UnivariateFunction {

        private double q;

        public EllipsoidUnivariateFunction(double q){
            this.q = q;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double value(double alpha){

            double alpha2 = alpha*alpha;
            double  qcore = q*FastMath.sqrt( corec2*alpha2 +  corea2*(1.0d - alpha2));
            double qshell = q*FastMath.sqrt(shellc2*alpha2 + shella2*(1.0d - alpha2));

            double sinCosCore = coreContrast*((FastMath.sin(qcore) - qcore * FastMath.cos(qcore)));
            double sinCosShell = shellContrast*((FastMath.sin(qshell) - qshell * FastMath.cos(qshell)));

            double f = sinCosCore/(qcore*qcore*qcore) + sinCosShell/(qshell*qshell*qshell);
            return f*f;
        }
    }


    public double getShellRadius_a(){return shellRadius_a;}
    public double getRadius_a(){return getFittedParamByIndex(0);}
    public double getRadius_c(){return getFittedParamByIndex(1);}
    //public double getThickness(){return thickness;}
    public double getThickness(){return getFittedParamByIndex(2);}
    public double getShellRadius_c(){return shellRadius_c;}
    public double getVolumeShell(){return volumeShell;}
    public boolean isEmpty(){return isEmpty;}

    private void setString(){
        String newLines = String.format("REMARK 265 INDEX %d CORE RADII %.2f %.2f %n", getIndex(), getFittedParamByIndex(0), getFittedParamByIndex(1));
        newLines +=       String.format("REMARK 265 INDEX %d      SHELL %.2f %n", getIndex(), getFittedParamByIndex(2));
        this.setStringToPrint(newLines);
    }
}
