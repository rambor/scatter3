package version3;

import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Collection {
    //collection of datasets
    private ArrayList<Dataset> datasets;
    private XYSeriesCollection miniCollection;
    private int panelID;
    private int totalDatasets=0;
    private String note;
    private double maxI;
    private double minI;
    private double minq;
    private double maxq;

    public Random rand;
    private String WORKING_DIRECTORY_NAME;

    protected Vector propChangeListeners = new Vector();

    // constructor
    public Collection(){
        datasets = new ArrayList<>();
        miniCollection = new XYSeriesCollection();
        rand = new Random();
        maxI = -100000000.0;
        minI = 10000;
        maxq = 0.0;
        minq = 1.0;

        // the collection of objects listening for property changes

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
        //float r,g,b;
        //r = rand.nextFloat();
        //g = rand.nextFloat();
        //b = rand.nextFloat();
        //dat.setColor(new Color(r,g,b));
        datasets.add(dat);
        totalDatasets = datasets.size();

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

        this.notifyDataSetsChange();
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
        this.totalDatasets = datasets.size();
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
        datasets = new ArrayList<>();
        miniCollection = new XYSeriesCollection();

        maxI = -100000000.0;
        minI = 10000;
        maxq = 0.0;
        minq = 1.0;
        this.setNote("");

        totalDatasets=0;
        //datasets = new ArrayList<>();
        //miniCollection = new XYSeriesCollection();
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
        return totalDatasets;
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

    /**
     *
     * @return the number of selected datasets in use
     */
    public int getTotalSelected(){
        int selected=0;

        for(int i=0; i<this.getDatasets().size(); i++){
            if (this.getDataset(i).getInUse()){
                selected++;
            }
        }
        return selected;
    }

    /**
     *
     * @return returns datalist ID of singly selected dataset
     */
    public int getSelected(){
        int selected = -1;

        if (this.getTotalSelected() == 1){
            for(int i=0; i < totalDatasets; i++){
                if (this.getDataset(i).getInUse()){
                    selected = i;
                    break;
                }
            }
        }
        return selected;
    }

    public void setWORKING_DIRECTORY_NAME(String name){
        this.WORKING_DIRECTORY_NAME = name;
    }

    public void setPanelID(int id){
        panelID = id;
    }

    public int getPanelID(){return panelID;}

    public String getWORKING_DIRECTORY_NAME(){
        return this.WORKING_DIRECTORY_NAME;
    }


    public void reorderCollection(int currentIndex, int targetIndex){
        System.out.println("Current " + currentIndex + "  " + targetIndex);
        // reset IDs
        for(int i=0; i<totalDatasets; i++){
            System.out.println(i+ " Before " + datasets.get(i).getFileName());
        }


        if (targetIndex > currentIndex){
            Collections.rotate(datasets.subList(currentIndex, targetIndex+1), -1);
        } else if (targetIndex < currentIndex){
            Collections.rotate(datasets.subList(targetIndex, currentIndex+1), 1);
        }

        // reset IDs
        for(int i=0; i<totalDatasets; i++){
            datasets.get(i).setId(i);
        }

        for(int i=0; i<totalDatasets; i++){
            System.out.println(i+ " After " + datasets.get(i).getFileName());
        }
    }

    // add a property change listener
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        // add a listener if it is not already registered
        if (!propChangeListeners.contains(l)) {
            propChangeListeners.addElement(l);
        }
    }

    // remove a property change listener
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        // remove it if it is registered
        if (propChangeListeners.contains(l)) {
            propChangeListeners.removeElement(l);
        }
    }

    // notify listening objects of property changes
    protected void notifyDataSetsChange() {
        // create the event object
        PropertyChangeEvent evt = new PropertyChangeEvent(this, "DataSets", null, datasets);
        // make a copy of the listener object vector so that it cannot
        // be changed while we are firing events
        Vector v;
        synchronized(this) {
            v = (Vector) propChangeListeners.clone();
        }

        // fire the event to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            PropertyChangeListener client = (PropertyChangeListener)v.elementAt(i);
            client.propertyChange(evt);
        }
    }


}
