package version3.formfactors;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by robertrambo on 27/11/2016.
 */
public class CallableCoreShellEllipsoid implements Callable<CoreShell> {

    private int index;
    private double solventContrast;
    private double[] particleContrasts = new double[2];
    private double[] params = new double[2];
    private Double[] qvalues;
    private boolean completeness = false;
    private double thickness;

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
    public CallableCoreShellEllipsoid(int index, double solventContrast, double[] particleContrasts, double thickness, double[] radii, List<Double> qvalues, boolean completeness){
        this.index = index;
        this.solventContrast = solventContrast;
        this.thickness = thickness;

        this.qvalues = new Double[qvalues.size()];
        this.qvalues = qvalues.toArray(this.qvalues);

        synchronized (this){
            this.particleContrasts[0] = particleContrasts[0]; // shell
            this.particleContrasts[1] = particleContrasts[1]; // core
            this.params[0] = radii[0];
            this.params[1] = radii[1];
        }
        this.completeness = completeness;
    }

    @Override
    public CoreShell call() throws Exception {
        CoreShell coreshell =  new CoreShell(
                index,
                solventContrast,
                particleContrasts,
                params,
                thickness,
                qvalues
        );

        return coreshell;
    }


}
