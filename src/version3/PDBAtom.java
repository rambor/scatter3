package version3;

/**
 * Created by robertrambo on 08/02/2016.
 */
public class PDBAtom {

    private double xpos;
    private double ypos;
    private double zpos;
    private double occ;
    private double temp;

    private int mappedResID;

    private int atomNumber;
    private int resid;

    private String chainID;
    private String atom;
    private String alt;

    private String residue; // 3-letter code for protein
    private String atomType;
    private double[] coords;
    private boolean isWater=false;
    //private String residueType; //DNA, RNA, protein, CARB

    public PDBAtom(String line){
        this.atomNumber = Integer.parseInt(line.substring(6,11).trim());
        this.atomType = line.substring(12,16).trim();

        String tempResidue = line.substring(17,20).trim();
        // check residue, convert to 3 letter code

        this.alt = line.substring(16,17).trim();
        this.chainID = line.substring(21,22).trim();

        this.resid = Integer.parseInt(line.substring(22,26).trim());

        this.xpos = Double.parseDouble(line.substring(30,38).trim());
        this.ypos = Double.parseDouble(line.substring(38,46).trim());
        this.zpos = Double.parseDouble(line.substring(46,54).trim());

        coords = new double[]{xpos, ypos, zpos};

        int  stringLength = line.length();

        if (stringLength >= 60){
            this.occ = Double.parseDouble(line.substring(54,60).trim());
        }

        if (stringLength >= 66){
            this.temp = Double.parseDouble(line.substring(60,66).trim());
        }

        if (stringLength >= 78){
            this.atom = line.substring(76,78).trim();
        }

        this.residue = tempResidue;

        if (this.residue.equals("HOH")){
            isWater = true;
        }

    }

    public double[] getCoords(){
        return coords;
    }

    public double getOccupancy (){
        return occ;
    }

    public String getAtomType(){
        return atomType;
    }

    public boolean isWater(){
        return isWater;
    }
}

