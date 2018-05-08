package version3.sasCIF;

import version3.Dataset;

public class SasResult {

    private Dataset dataset;
    public SasResult(Dataset dataset){
        this.dataset = dataset;
    }

    public String getTextForOutput(int id){
        String tempHeader = String.format("# %n");
        tempHeader += String.format("_sas_result.id %d %n", id);
        tempHeader += String.format("_sas_result.experimental_MW ? %n");
        tempHeader += String.format("_sas_result.experimental_MW_error ? %n");

        if (dataset.getGuinierRg() > 0){
            tempHeader += String.format("_sas_result.reciprocal_space_I0 %.4E %n", dataset.getGuinierIzero());
            tempHeader += String.format("_sas_result.reciprocal_space_I0_error %.4E %n", dataset.getGuinierIzeroSigma());
            tempHeader += String.format("_sas_result.reciprocal_space_Rg %.2E %n", dataset.getGuinierRg());
            tempHeader += String.format("_sas_result.reciprocal_space_Rg_error %.2E %n", dataset.getGuinierRG_sigma());
        } else {
            tempHeader += String.format("_sas_result.reciprocal_space_I0 ? %n");
            tempHeader += String.format("_sas_result.reciprocal_space_I0_error ? %n");
            tempHeader += String.format("_sas_result.reciprocal_space_Rg ? %n");
            tempHeader += String.format("_sas_result.reciprocal_space_Rg_error ? %n");
        }


        if (dataset.getRealSpaceModel().getRg() > 0){
            tempHeader += String.format("_sas_result.real_space_I0 ? %n");
            tempHeader += String.format("_sas_result.real_space_I0_error ? %n");
            tempHeader += String.format("_sas_result.real_space_Rg ? %n");
            tempHeader += String.format("_sas_result.real_space_Rg_error ? %n");
            tempHeader += String.format("_sas_result.real_space_volume %n");
            tempHeader += String.format("_sas_result.real_space_volume_error ? %n");
        }


        if (dataset.getPorodVolume() > 0){
            tempHeader += String.format("_sas_result.reciprocal_space_volume %d %n", dataset.getPorodVolume());
            tempHeader += String.format("_sas_result.reciprocal_space_volume_error ? %n");
            tempHeader += String.format("_sas_result.porod_exponent %.2f %n", dataset.getPorodExponent());
            tempHeader += String.format("_sas_result.porod_exponent_error %.2f %n", dataset.getPorodExponentError());

            if (dataset.getGuinierRg() > 0){
                tempHeader += String.format("_sas_result.reciprocal_porod_volume_mass_1p11 %d %n", dataset.getPorodVolumeMass1p1() );
                tempHeader += String.format("_sas_result.reciprocal_porod_volume_mass_1p37 %d %n", dataset.getPorodVolumeMass1p37() );
            } else {
                tempHeader += String.format("_sas_result.reciprocal_porod_volume_mass_1p11 ? %n");
                tempHeader += String.format("_sas_result.reciprocal_porod_volume_mass_1p37 ? %n");
            }

            if (dataset.getRealRg() > 0){
                tempHeader += String.format("_sas_result.real_porod_volume_mass_1p11 %d %n", dataset.getPorodVolumeRealMass1p1() );
                tempHeader += String.format("_sas_result.real_porod_volume_mass_1p37 %d %n", dataset.getPorodVolumeRealMass1p37() );
            } else {
                tempHeader += String.format("_sas_result.real_porod_volume_mass_1p11 ? %n");
                tempHeader += String.format("_sas_result.real_porod_volume_mass_1p37 ? %n");
            }
        } else {
            tempHeader += String.format("_sas_result.reciprocal_space_volume ? %n");
            tempHeader += String.format("_sas_result.reciprocal_space_volume_error ? %n");
            tempHeader += String.format("_sas_result.porod_exponent ? %n");
            tempHeader += String.format("_sas_result.porod_exponent_error ? %n");
            tempHeader += String.format("_sas_result.reciprocal_porod_volume_mass_1p11 ? %n");
            tempHeader += String.format("_sas_result.reciprocal_porod_volume_mass_1p37 ? %n");
            tempHeader += String.format("_sas_result.real_porod_volume_mass_1p11 ? %n");
            tempHeader += String.format("_sas_result.real_porod_volume_mass_1p37 ? %n");
        }

        return tempHeader;
    }
}
