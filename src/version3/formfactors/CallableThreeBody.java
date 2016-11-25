package version3.formfactors;

import java.util.concurrent.Callable;

/**
 * Created by robertrambo on 24/11/2016.
 */
public class CallableThreeBody implements Callable<ThreeBody> {

    private int index;
    private double solventContrast;
    private double[] particleContrasts = new double[1];
    private double[] params = new double[3];
    private Double[] qvalues;
    private double rdis;

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
    public CallableThreeBody(int index, double solventContrast, double[] particleContrasts, double[] radii, double rdis, Double[] qvalues){
        this.index = index;
        this.solventContrast = solventContrast;
        this.qvalues = new Double[qvalues.length];
        this.rdis = rdis;

        synchronized (this){
            this.particleContrasts[0] = particleContrasts[0];
            this.params[0] = radii[0];
            this.params[1] = radii[1];
            this.params[2] = radii[2];
            System.arraycopy(qvalues, 0, this.qvalues, 0, qvalues.length);
        }
    }

    @Override
    public ThreeBody call() throws Exception {
        ThreeBody threeBody =  new ThreeBody(
                index,
                solventContrast,
                particleContrasts,
                params,
                rdis,
                qvalues
        );

        return threeBody;
    }


}
