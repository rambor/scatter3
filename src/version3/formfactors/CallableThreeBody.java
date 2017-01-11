package version3.formfactors;

import java.util.List;
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
    public CallableThreeBody(int index, double solventContrast, double[] particleContrasts, double[] radii, double rdis, List<Double> qvalues){
        this.index = index;
        this.solventContrast = solventContrast;

        this.qvalues = new Double[qvalues.size()];
        this.qvalues = qvalues.toArray(this.qvalues);
        this.rdis = rdis;

        synchronized (this){
            this.particleContrasts[0] = particleContrasts[0];
            this.params[0] = radii[0];
            this.params[1] = radii[1];
            this.params[2] = radii[2];
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
