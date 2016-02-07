package version3.ceengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
/**
 * Created by Nathan on 23/06/2015.
 *
 * Calculates q^6 times the scattering intensity as a function of q for spheres of various radii.
 *
 * Holds curve data in an array split into 'blocks' of q values which have the curve at that value of q for a r:
 *          q1          q2          q3       ...        qN-2      qN-1         qN
 *      [r1......rN||r1......rN||r1......rN||   ||r1......rN||r1......rN||r1......rN]
 *
 * @author Nathan
 * @version 1.0
 */

public class SphericalCurves extends PredictedIntensityCurves{

    private int noThreads = 4;
    private static int qPerThread;
    private float scale;

    public SphericalCurves(float scale){
        type = ModelType.SPHERICAL;
        this.scale = scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setThings(ArrayList<ParameterSet> paramValues, double[] qValues) {//USES double[] FOR THE Q VALUES AS THIS IS HOW IT COMES FROM XYSERIES
        paramLength = paramValues.size();
        qLength = qValues.length;
        constant = (scale * 12 * (float) Math.PI * contrast*contrast);

        int counter = 0;
        float[] rValues = new float[paramLength*paramValues.get(0).getParamNumbers()];
        for (int i = 0; i < paramLength; i++) {
            for (int j = 0; j < paramValues.get(i).getParamNumbers(); j++) {
                rValues[counter++] = paramValues.get(i).getParam(j);
            }
        }

        fillValues(rValues, qValues);
    }

    //Precalculates all the curves and puts in one array
    //Splits the q range and uses multiple threads to fill
    private void fillValues(float[] rValues, double[] qValues) {
        qPerThread = qLength/noThreads;
        values = new float[paramLength *qLength];
        //ExecutorService pool = Executors.newFixedThreadPool(noThreads);

        Set<Future<float[]>> set = new HashSet<>();
        for (int i = 0; i < noThreads-1; i++){
            Callable<float[]> callable=new SphericalQCallable(i, rValues, i*qPerThread, (i+1)*qPerThread, qValues);
            Future<float[]> future= CrossEntropyEngine.pool.submit(callable);
            set.add(future);
        }
        Callable<float[]> callable=new SphericalQCallable(noThreads-1, rValues, (noThreads-1)*qPerThread, (qValues.length), qValues);
        Future<float[]> future=CrossEntropyEngine.pool.submit(callable);
        set.add(future);

        for(Future<float[]> f: set){
            try{
                float[] got = f.get();
                int counter = (int)(got[0]*qPerThread*rValues.length);
                for (int i = 1; i < got.length; i++) {
                    values[counter++] = got[i];
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
