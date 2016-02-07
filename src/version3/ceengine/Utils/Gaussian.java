package version3.ceengine.Utils;

/**
* Created by Nathan on 11/06/2015.
*/
public class Gaussian {

    private float mu, sigma;

    public Gaussian(float mean, float stanDev){
        mu = mean;
        sigma = stanDev;
    }

    public float evaluate(double x){
        return (float)((Math.exp(-1*((x - mu)*(x - mu))/(2*sigma*sigma)))/(Math.sqrt(2*Math.PI)*sigma));
    }

}

