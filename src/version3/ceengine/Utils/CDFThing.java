package version3.ceengine.Utils;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.*;

/**
 * Created by Nathan on 04/06/2015.
 *
 * Represents a discrete cdf of 'buckets' using a LinkedHashMap. It is NOT normalised to 1!!!
 *
 * @author Nathan
 * @version 1.0
 */

public class CDFThing {

    private final RandomDataGenerator rand = new RandomDataGenerator();
    private LinkedHashMap<Integer, Float> lHMap;
    private float totalSum;

    /**
     * Creates an unormailised CDF from the given PDF.
     *
     * @param pdf The PDF used to make the CDF
     */
    public CDFThing(int[] pdf){
        lHMap = new LinkedHashMap(pdf.length);
        update(pdf);
    }

    /**
     * Creates a shallow copy of the CDF.
     *
     * @param copyMe - The CDF to be copied
     */
    public CDFThing(CDFThing copyMe){
        this.lHMap = copyMe.lHMap;
        this.totalSum = copyMe.totalSum;
    }

    /**
     * Fills the CDF with the values of the PDF.
     *
     * @param pdf The PDF used to make the CDF
     */
    public void update(int[] pdf){
        float current = 0;
        for(int i = 0; i < pdf.length; i++){
            current += pdf[i];
            lHMap.put(i, current);
        }
        totalSum = current;
    }

    /**
     * Returns the index of a random paramVector value according to the CDF.
     * <p>
     * To choose this method picks a random number and then iterates through the CDF to find the correct value. It is quite slow for large CDFs.
     *
     * @return the chosen index in the parameter vector
     */
    public int pickRandom(){
        double randNumber = rand.nextUniform(0,totalSum,true);
        Iterator i = lHMap.entrySet().iterator();
        int result = 0;
        Loop:
        while(i.hasNext()){
            Map.Entry e = (Map.Entry)i.next();
            if(randNumber < (float)e.getValue()){
                result = (int)e.getKey();
                break Loop;
            }
        }
        return result;
    }

    /**
     * Returns the index of a random paramVector value according to the CDF.
     * <p>
     * To choose this method picks a random number and then uses a binary search to navigate the CDF to find the correct value. It is faster than {@link #pickRandom}, especially for large CDFs.
     * <p>
     * This method only works if keys are the sames as the index in the map (which they are for this use).
     *
     * @return the chosen index in the parameter vector
     */
    public int pickRandomMyBinarySearch(){
        Float randNumber = (float)rand.nextUniform(0,totalSum,true);
        int max = lHMap.size()-1;
        int min = 0;
        while(max-min > 1){
            int mid = (int)((max-min)*0.5 + min);
            if(lHMap.get(mid) > randNumber){
                max = mid;
            }else{
                min = mid;
            }
        }
        return max;
    }

}
