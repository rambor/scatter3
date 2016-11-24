package version3.formfactors;

import javax.xml.bind.SchemaOutputResolver;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by robertrambo on 04/11/2016.
 */
// make threadsafe object to update topList
public class TopList{

    private ConcurrentSkipListMap<Double, Integer> scoreList;  // strictly positive
    private ConcurrentSkipListMap<Integer, ArrayList<Integer>> modelList;
    private AtomicInteger counter = new AtomicInteger(0);
    private final int sizeOfList;

    public TopList(int sizeOfList){
        this.sizeOfList = sizeOfList;
        scoreList = new ConcurrentSkipListMap();   // sorted by keys
        modelList = new ConcurrentSkipListMap<>(); // sorted by keys
    }

    /**
     * Update Method maintains the top ten list and must be synchronized since it will
     * be accessed from multiple cores
     *
     * @param score
     * @param index
     * @param model
     * @param numberOfModelsPerTrial
     */
    public synchronized void update(double score, int index, TestModel model, int numberOfModelsPerTrial){
        // iterate over the list
        // start in reverse
        // and if score is better than
        // insert and remove last

        //System.out.println(index + " SCORE : " + score + " scorelist key : " + scoreList.ceilingKey(score)  +  " cnt => " + counter.get());

        if (counter.get() < sizeOfList){

            scoreList.put(score, index);
            modelList.put(index, new ArrayList<Integer>(numberOfModelsPerTrial));

            // Collections.copy(destination, source)
            for (Integer item : model.getSelectedIndices()) modelList.get(index).add(item);
            counter.incrementAndGet();

            // if list if full, check if score is less than some value in Map
            // if so, add and popoff last key-value in map
        } else if (scoreList.ceilingKey(score) != null){
            // popoff last
            //System.out.println(index + " REMOVING " + scoreList.size() + " " + scoreList.lastKey());
            // insert new entry
            scoreList.put(score, index); // score is
            modelList.put(index, new ArrayList<Integer>(numberOfModelsPerTrial));
            for (Integer item : model.getSelectedIndices()) modelList.get(index).add(item);

            if (scoreList.size() > sizeOfList){
                modelList.remove(scoreList.get(scoreList.lastKey())); // Map<Integer, ArrayList<Integer>>
                scoreList.remove(scoreList.lastKey());                // Map<Double, Integer>
            }

            //System.out.println(index + "   ADDING " + scoreList.size() + " " + score + " < " + scoreList.lastKey());

        }
    }

    /**
     *
     * ConcurrentSkipListMap<Double, Integer> scoreList;  // strictly positive
     * Integer is the index of the random trial set
     * @return
     */
    public Integer[] getModelIndicesFromScore(){
        //System.out.println("SCORELIST: " + scoreList.size());
        //System.out.println(scoreList.values().toString());
        int size = scoreList.size();
        Integer[] returnthis = new Integer[size];

        int index=0;
        for (Double key : scoreList.keySet()) {
            returnthis[index] = scoreList.get(key);
            index++;
        }
        return returnthis;
    }


    /**
     * Calculate average score in list
     * @return
     */
    public double getAverageScore(){

        double value = 0.0d;
        int index=0;
        for (Double key : scoreList.keySet()) {
            value += key;
            index++;
        }

        double first = scoreList.firstEntry().getKey();
        double last = scoreList.lastEntry().getKey();
        double average = (value/(double)index);
        double gap = last-first;


        System.out.println("FIRST: " + first + " LAST: " + last +  "  AVG: " + average + " GAP: "+ gap + " SIZE: " + scoreList.size());
        //return scoreList.firstEntry().getKey();
        return Math.log10(average);
    }



    public ArrayList<Integer> getModelIndicesByKey(int key){
        return modelList.get(key);
    }


    public void print(){
        int count=0;
        System.out.println("SIZE OF SCORELIST => " + scoreList.size() + " CNT " + counter.get());

        for (ConcurrentSkipListMap.Entry<Double, Integer> entry : scoreList.entrySet()) {
            System.out.println(count + " SCORE LIST KEY : " + entry.getKey() + " => " + entry.getValue());
            count++;
        }
    }


    /**
     * copies top list
     * key is score
     *
     * @param keptList
     * @param numberOfModelsPerTrial
     */
    public void copyToKeptList(ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList, int numberOfModelsPerTrial){

        int count=1;
        for (ConcurrentSkipListMap.Entry<Double, Integer> entry : scoreList.entrySet()) {
            double newKey = entry.getKey(); // use the score as the key
            //System.out.println(count + " score " + newKey);
            count++;
            keptList.put(newKey, new ArrayList<Integer>(numberOfModelsPerTrial));
            // populate list
            for (Integer item : modelList.get(entry.getValue())) keptList.get(newKey).add(item);
        }
    }

}