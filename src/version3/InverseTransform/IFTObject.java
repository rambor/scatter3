package version3.InverseTransform;

import version3.LaguerreParamsSingleton;
import version3.RealSpace;


public class IFTObject implements Runnable {

    private double qmax, dmax, lambda=0.001;

    private boolean useL1;
    private boolean useDirectFT = false;
    private boolean useLegendre = false;
    private LaguerreParamsSingleton laguerreParamsSingleton;
    private boolean useLaguerre = false;
    private RealSpace dataset;
    private int cBoxValue=2;
    private boolean includeBackground = false;
    private boolean positiveOnly;


    /**
     * Make instance of an IndirectFT object and run within IFTObject class
     * @param dataset
     * @param lambda
     * @param useL1
     * @param cBoxValue
     * @param useDirectFt
     * @param includeBackground
     */
    public IFTObject(
            RealSpace dataset,
            double lambda,
            boolean useL1,
            int cBoxValue,
            boolean useDirectFt,
            boolean useLegendre,
            LaguerreParamsSingleton laguerreParamsSingleton,
            boolean includeBackground,
            boolean positiveOnly){

        // create real space object for each dataset in use
        // XYSeries data, double dmax, double lambda, double qmax
        this.dataset = dataset;
        this.qmax = dataset.getLogData().getMaxX();
        dataset.setQmax(this.qmax); // need to specify qmax used in IFT determination
        this.dmax = dataset.getDmax();
        this.lambda = lambda;
        this.useL1 = useL1;

        // after mouse click on spinner, this constructor is made => do we do standardization within constructor?

        this.cBoxValue = cBoxValue;
        this.useDirectFT = useDirectFt;      // default is true for Scatter => rambo method
        this.useLegendre = useLegendre;
        this.laguerreParamsSingleton = laguerreParamsSingleton;
        this.useLaguerre = laguerreParamsSingleton.getIsSelected();
        this.includeBackground = includeBackground; //
        this.positiveOnly = positiveOnly;
    }


    @Override
    public void run() {
        IndirectFT tempIFT;

        if (useDirectFT && !useLaguerre && !useLegendre){

            tempIFT = new SineIntegralTransform(dataset.getfittedqIq(), dataset.getfittedError(), dmax, qmax, lambda, useL1, cBoxValue, includeBackground, positiveOnly);

        } else if (useLegendre && !useLaguerre && !useDirectFT) {

            tempIFT = new LegendreTransform(dataset.getfittedqIq(), dataset.getfittedError(), dmax, qmax, lambda, 1, includeBackground);
            //tempIFT = new LaguerreTransform(37 , 29, dataset.getfittedqIq(), dataset.getfittedError(), dmax, qmax, lambda, 1, includeBackground);

        } else if (useLaguerre && !useLegendre && !useDirectFT) {  // use Laguerre
            double rg = laguerreParamsSingleton.getDefaultRg();
            double rave = laguerreParamsSingleton.getR_ave();

            if (dataset.getRg() > laguerreParamsSingleton.getRgLimit() && !laguerreParamsSingleton.getIsFixed()){
                rg = dataset.getRg();
                rave = dataset.getRaverage();
                System.out.println("not fixed ");
            }

            tempIFT = new LaguerreTransform(rave , rg, dataset.getfittedqIq(), dataset.getfittedError(), dmax, qmax, lambda, 1);

        } else  {  // use Moore Method

            tempIFT = new MooreTransform(dataset.getfittedqIq(), dataset.getfittedError(), dmax, qmax, lambda, useL1, cBoxValue, includeBackground);
        }

        this.dataset.setStandardizationMean(tempIFT.getStandardizedLocation(), tempIFT.getStandardizedScale());
        this.dataset.setPrDistribution(tempIFT.getPrDistribution());
        this.dataset.setIndirectFTModel(tempIFT);
    }

}
