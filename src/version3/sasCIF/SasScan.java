package version3.sasCIF;

import version3.Dataset;

public class SasScan {

    Dataset dataset;
    String tempHeader;

    public SasScan(Dataset dataset, int id){
        this.dataset = dataset;
        //this.setHeader( );
    }


    private String getHeader(){

        SasDetails details = dataset.getSasDetails();
        tempHeader = String.format("# %n");
        tempHeader += String.format("# REMARK 265 sas_scan %n");
        tempHeader += String.format("loop_ %n");
        tempHeader += String.format("_sas_scan.id %n");
        tempHeader += String.format("_sas_scan.calibration_factor ? %n");
        tempHeader += String.format("_sas_scan.exposure_time %.3f %n");
        tempHeader += String.format("_sas_scan.filename %s %n");
        tempHeader += String.format("_sas_scan.intensity_units %n");
        tempHeader += String.format("_sas_scan.measurement_date %n");
        tempHeader += String.format("_sas_scan.momentum_transfer_units %s %n");
        tempHeader += String.format("_sas_scan.number_of_frames %d %n");
        tempHeader += String.format("_sas_scan.result_id %d %n");
        tempHeader += String.format("_sas_scan.sample_id %n");
        tempHeader += String.format("_sas_scan.title %s %n");
        tempHeader += String.format("_sas_scan.type %n");
        return tempHeader;
    }



//    public String getRow(int row_index){
//        tempHeader = String.format("# %n");
//        tempHeader += String.format("%d %d %s", row_index, id, );
//    }
}
