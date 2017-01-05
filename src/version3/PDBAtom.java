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

    // copy constructor
    public PDBAtom(PDBAtom atom){

        this.atomNumber =atom.atomNumber;
        this.xpos =atom.xpos;
        this.ypos =atom.ypos;
        this.zpos =atom.zpos;
        this.occ = atom.occ;
        this.temp = atom.temp;
        this.resid = atom.resid;
        this.chainID = atom.chainID;
        this.atom = atom.atom;
        this.alt = atom.alt;
        this.residue = atom.residue;
        this.atomType = atom.atomType;

        double[] temp = atom.getCoords();
        this.coords = new double[]{temp[0], temp[1], temp[2]};
        this.isWater = atom.isWater;
    }

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

    public void setCoords(double[] values){
        coords[0]=values[0];
        coords[1]=values[1];
        coords[2]=values[2];
        xpos = values[0];
        ypos = values[1];
        zpos = values[2];
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


    public String getPDBline(){
        if (this.isWater()){
            return String.format("%-6s%5d %4s%1s%3s %1s%4d%1s   %8.3f%8.3f%8.3f%6.2f%6.2f %n", "HETATM", atomNumber, atomType, " ", residue, chainID, resid, " ", xpos, ypos, zpos, occ, temp);
        } else {
            return String.format("%-6s%5d %4s%1s%3s %1s%4d%1s   %8.3f%8.3f%8.3f%6.2f%6.2f %n", "ATOM  ", atomNumber, atomType, " ", residue, chainID, resid, " ", xpos, ypos, zpos, occ, temp);
        }
    }


}

