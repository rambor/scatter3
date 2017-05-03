package version3;

import org.apache.commons.io.FilenameUtils;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by robertrambo on 02/05/2017.
 */
public class FileListBuilder {
    String filename;
    String extension;
    String directory;
    String outputDirectory;
    File[] foundFiles;
    /** File type ZIP archive */
    public static final int ZIPFILE = 0x504b0304;
    /** File type GZIP compressed Data */
    public static final int GZFILE = 0x1f8b0000;

    public FileListBuilder(File file, String dir) throws Exception {
            // get directory
            // split
        extension = FilenameUtils.getExtension(file.getName());
        outputDirectory = dir;

        if (isZipFile(file)) {

            if (readZipFile(file)){
                //sort files

            } else {
                throw new Exception("No DAT Files in ZIP Archive");
            }

        } else if (isGZipFile(file)){

            throw new Exception("CANNOT PROCESS GZIP FILES, maybe later...");

        } else {

            filename = FilenameUtils.removeExtension(file.getName());
            String[] parts = filename.split("[-_]+");

            for(int i=0; i<parts.length; i++){
                System.out.println(i + " " + parts[i]);
            }

            directory = file.getParent();
            // build a list of files that match
            // b21-########_00001.dat
            // splits into 3 parts
            // match beginning gives me everything in the directory
            // if I match all three parts, then I get one file, so I want to start from either the front or back

            // 00001_filename.dat  <= that would be stupd
            // filename_00001.dat <= makes more sense

            // make sure last part matches a number
            String onlyDigits = "\\d+";

            if ( parts[parts.length-1].matches(onlyDigits) ) {

                this.collectFilesWithDigitsLast(parts[parts.length - 1]);

            } else if (parts[0].matches(onlyDigits)) { // 00001_filename.dat

                this.collectFilesWithDigitsFirst(parts[0]);

            } else {
                throw new Exception();
            }
        }

    }

    public File[] getFoundFiles(){ return foundFiles; }

    private void collectFilesWithDigitsLast(String digits){

        String prefix = filename.split(digits)[0];

        // grab all files with the prefix
        File lookInThisDir = new File(directory);

        foundFiles = lookInThisDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix) && name.endsWith(".dat");
            }
        });


        // sort on custom Comparator
        Arrays.sort(foundFiles, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;

                try {
                    String tempName = FilenameUtils.removeExtension(name);
                    //int s = name.indexOf('_')+1;
                    //int e = name.lastIndexOf('.');
                    //String number = name.substring(s, e);
                    String number = tempName.split(prefix)[0];
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0; // if filename does not match the format
                    // then default to 0
                }
                return i;
            }
        });
    }



    private void collectFilesWithDigitsFirst(String digits){

        String prefix = filename.split(digits)[1];

        // grab all files with the prefix
        File lookInThisDir = new File(directory);

        foundFiles = lookInThisDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(prefix) && name.endsWith(".dat");
                //return name.startsWith(prefix);
            }
        });

        // sort on custom Comparator
        Arrays.sort(foundFiles, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;

                try {
                    String tempName = FilenameUtils.removeExtension(name);
                    String number = tempName.split(prefix)[0];
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0; // if filename does not match the format
                    // then default to 0
                }
                return i;
            }
        });
    }


    /**
     * Determine whether a file is a ZIP File.
     */
    private boolean isZipFile(File file) throws IOException {
        if(file.isDirectory()) {
            return false;
        }
        if(!file.canRead()) {
            throw new IOException("Cannot read file "+file.getAbsolutePath());
        }
        if(file.length() < 4) {
            return false;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int test = in.readInt();
        in.close();
        return test == ZIPFILE;
    }

    /**
     * Determine whether a file is a GZIP File.
     */
    private boolean isGZipFile(File file) throws IOException {
        if(file.isDirectory()) {
            return false;
        }
        if(!file.canRead()) {
            throw new IOException("Cannot read file "+file.getAbsolutePath());
        }
        if(file.length() < 4) {
            return false;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int test = in.readInt();
        in.close();
        return test == GZFILE;
    }

    private boolean readZipFile(File file){

        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
        ArrayList<File> tempFiles = new ArrayList<>();
        int fileCount=0;

        try {
            zipinputstream = new ZipInputStream(new FileInputStream(file));
            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                String entryName = zipentry.getName();
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                String directory = newFile.getParent();

                if (directory == null) {
                    if (newFile.isDirectory())
                        break;
                }

                if (entryName.endsWith(".dat")){
                    tempFiles.add(new File(outputDirectory+"\\" + entryName) );
                    fileoutputstream = new FileOutputStream(tempFiles.get(fileCount));

                    int n;
                    while ((n = zipinputstream.read(buf, 0, 1024)) > -1){
                        fileoutputstream.write(buf, 0, n);
                    }
                    fileoutputstream.close();
                }

                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }
            zipinputstream.close();

            if (tempFiles.size() > 1){
                foundFiles = (File[])tempFiles.toArray();
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
