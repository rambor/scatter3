package version3;

/**
 * Created by robertrambo on 18/01/2016.
 */
public class ReferenceItem {
    private String key;
    private int value;

    public ReferenceItem(String key, int value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "sample_" + Integer.toString(value);
    }

    public String getKey()
    {
        return key;
    }

    public int getValue()
    {
        return value;
    }
}