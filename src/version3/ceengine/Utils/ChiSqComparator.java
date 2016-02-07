package version3.ceengine.Utils;

import java.util.*;

/**
 * Created by Nathan on 25/06/2015.
 *
 * Class implements a comparator between two arrays of floats. The comparison is based on a comparison between a
 * mixture of the second and third elements. The amount the third element is taken into account is controlled by {@link #param}.
 *
 * @author Nathan
 * @version 1.0
 */

public class ChiSqComparator implements Comparator<Float[]>{

    /**
     * How much the smoothness is taken into consideration
     */
    private final float param = 1E-8f;

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(Float[] me, Float[] them){
        float total1 = me[1]+param*me[2];
        float total2 = them[1]+param*them[2];
        if(Math.abs(total1-total2) < 1e-10){
            return 0;
        }else if((total1-total2) > 1e-10){
            return 1;
        }else{
            return -1;
        }
    }
}