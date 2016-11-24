package version3.formfactors;

import java.util.concurrent.Callable;

/**
 * Created by robertrambo on 23/11/2016.
 */
public class CallableProlateEllipsoid implements Callable<ProlateEllipsoid> {

    private int index;
    private double solventContrast;
    private double[] particleContrasts = new double[1];
    private double[] params = new double[2];
    private Double[] qvalues;
    private double deltaqr;

    /**
     * Oblate ellipsoid is radii[1] > radii[0]
     * Prolate ellipsoid is radii[0] > radii[1]
     *
     * @param index
     * @param solventContrast
     * @param particleContrasts
     * @param radii
     * @param qvalues
     */
    public CallableProlateEllipsoid(int index, double solventContrast, double[] particleContrasts, double[] radii, Double[] qvalues){
        this.index = index;
        this.solventContrast = solventContrast;
        this.qvalues = new Double[qvalues.length];

        synchronized (this){
            this.particleContrasts[0] = particleContrasts[0];
            this.params[0] = radii[0];
            this.params[1] = radii[1];
            System.arraycopy(qvalues, 0, this.qvalues, 0, qvalues.length);
        }
    }

    @Override
    public ProlateEllipsoid call() throws Exception {
        ProlateEllipsoid ellipse =  new ProlateEllipsoid(
                index,
                solventContrast,
                particleContrasts,
                params,
                qvalues
        );
        //ellipse.printParams();
        return ellipse;
    }

}
