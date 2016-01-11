package version3;

import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Collection {
    //collection of datasets
    private ArrayList<Dataset> datasets;
    private XYSeriesCollection miniCollection;
    private String note;
    private double maxI;
    private double minI;
    private double minq;
    private double maxq;

    public Random rand;

    // constructor
    public Collection(){
        datasets = new ArrayList<Dataset>();
        miniCollection = new XYSeriesCollection();
        rand = new Random();
        maxI = -100000000.0;
        minI = 10000;
        maxq = 0.0;
        minq = 1.0;
    }
    /**
     * Returns XYSeriesCollection
     *
     */
    public XYSeriesCollection getMiniCollection(){
        return miniCollection;
    }
    /**
     * Clear XYSeriesCollection
     *
     */
    public void clearMiniCollection(){
        miniCollection.removeAllSeries();
    }

    /**
     * Adds dataset to collection
     * @param dat Dataset to be added
     */
    public void addDataset(Dataset dat){
        // do color assignment when adding to collection
        float r,g,b;
        r = rand.nextFloat();
        g = rand.nextFloat();
        b = rand.nextFloat();
        dat.setColor(new Color(r,g,b));
        datasets.add(dat);

        miniCollection.addSeries(dat.getData()); // log10 data
        // reset max and min values for collection
        if (dat.getMaxI() > this.maxI ) {
            this.maxI = dat.getMaxI();
        }
        if (dat.getMinI() < this.minI ) {
            this.minI = dat.getMinI();
        }
        if (dat.getMaxq() > this.maxq ) {
            this.maxq = dat.getMaxq();
        }
        if (dat.getMinq() < this.minq ) {
            this.minq = dat.getMinq();
        }
    }

    public void recalculateMinMaxQ(){
        // do color assignment when adding to collection
        int total = this.getDatasets().size();
        double newMinQ = this.getDataset(0).getMinq();
        double newMaxQ = this.getDataset(0).getMaxq();
        double newMinI = this.getDataset(0).getMinI();
        double newMaxI = this.getDataset(0).getMaxI();

        double temp;

        for(int i = 1; i< total; i++){
            temp = this.getDataset(i).getMaxq();
            if (temp > newMaxQ ) {
                newMaxQ = temp;
            }

            temp = this.getDataset(i).getMinq();
            if (temp < newMinQ ) {
                newMinQ = temp;
            }

            temp = this.getDataset(i).getMinI();
            if (temp < newMinI ) {
                newMinI = temp;
            }

            temp = this.getDataset(i).getMaxI();
            if (temp < newMaxI ) {
                newMaxI = temp;
            }
        }
        this.minq = newMinQ;
        this.maxq = newMaxQ;
        this.minI = newMinI;
        this.maxI = newMaxI;

    }

    /**
     * Removes dataset at specific index
     * @param index Index of a Dataset to be removed
     */
    public void removeDataset(int index) {
        datasets.remove(index);
        miniCollection.removeSeries(index);
    }
    /**
     * Returns dataset at specific index
     * @return Dataset object
     */
    public Dataset getDataset(int index){
        return datasets.get(index);
    }
    /**
     * Returns datasets
     * @return ArrayList of Dataset objects
     */
    public ArrayList<Dataset> getDatasets(){
        return datasets;
    }
    /**
     * Clear all datasets from the collection
     *
     */
    public void removeAllDatasets(){
        datasets.clear();
        miniCollection.removeAllSeries();
        maxI = -100000000.0;
        minI = 10000;
        maxq = 0.0;
        minq = 1.0;
        this.setNote("");

        datasets = new ArrayList<>();
        miniCollection = new XYSeriesCollection();
    }

    public void setNote(String text){
        note = text;
    }

    public String getNote(){
        return note;
    }

    public Dataset getLast(){
        int last = this.datasets.size() - 1;
        return this.getDataset(last);
    }

    public int getDatasetCount(){
        return datasets.size();
    }

    public double getMaxI(){
        return maxI;
    }

    public double getMinI(){
        return minI;
    }

    public double getMaxq(){
        return maxq;
    }

    public double getMinq(){
        return minq;
    }


}
