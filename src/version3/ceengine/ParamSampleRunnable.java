package version3.ceengine;

import version3.ceengine.Utils.CDFThing;

/**
 * Created by Nathan on 30/06/2015.
 *
 * Enables the parameter sampling to be multithreaded. The Thread is passed an array and it is filled.
 */
public class ParamSampleRunnable implements Runnable{

    private int start, end;
    private CDFThing cdf;
    private int[] chosenParams;

    /**
     * Creates a new ParamSampleRunnable object
     *
     * @param start        The index of the array to start filling
     * @param end          The index of the array to stop filling
     * @param cdf          The CDF to sample the parameters from
     * @param chosenParams The empty array to be filled
     */
    public ParamSampleRunnable(int start, int end, CDFThing cdf, int[] chosenParams){
        this.start = start;
        this.end = end;
        this.cdf=cdf;
        this.chosenParams = chosenParams;
    }

    /**
     * {@inheritDoc}
     */
    public void run(){
        long time = 0;
        int counter = 0;

        for(int i=start; i<end; i++){

            long s = System.nanoTime();

            chosenParams[i]=cdf.pickRandomMyBinarySearch();

            time += (System.nanoTime()-s);
            counter++;
        }

        System.out.println("Average time for sample: " + time/(float)counter + "ns");
    }

}
