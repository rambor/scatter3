package version3;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Created by robertrambo on 21/01/2016.
 */
public class SimilarityCollectionItem {

    private final Collection collection;
    private final String name;
    private boolean isSelected = true;

    public SimilarityCollectionItem(String name){
        this.name = name;
        this.collection = new Collection();
        isSelected = true;
    }

    public String getName(){
        return name;
    }

    public Collection getCollection(){
        return collection;
    }

    public void addDataset(Dataset dataset){
        //this.collection.addDataset(new Dataset(data, error, filename, id, false));
        this.collection.addDataset(new Dataset(dataset));
    }


    public Dataset getDataset(int index){
        return collection.getDataset(index);
    }

    public boolean isSelected(){
        return isSelected;
    }
    public void setSelected(boolean flag){
        this.isSelected = flag;
    }
}
