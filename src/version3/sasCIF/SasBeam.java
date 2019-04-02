package version3.sasCIF;

public class SasBeam {

    private String instrument_name;
    private String type_of_source = "synchrotron";
    private double radiation_wavelength = 12000;
    private double flux;
    private double sample_to_detector_distance;

    public SasBeam(){
    }

    public void setInstrument_name(String instrument_name) {
        this.instrument_name = instrument_name;
    }

    public void setType_of_source(String type_of_source) {
        this.type_of_source = type_of_source;
    }

    /**
     * units of eV
     * @param radiation_wavelength
     */
    public void setRadiation_wavelength(double radiation_wavelength) {
        this.radiation_wavelength = radiation_wavelength;
    }

    /**
     * units of photons per second
     * @param flux
     */
    public void setFlux(double flux) {
        this.flux = flux;
    }

    public void setSample_to_detector_distance(double dis) {
        this.sample_to_detector_distance = dis;
    }
}
