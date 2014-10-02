/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.ACE.XMLParsers.XMLDocumentParser;
import jAudioFeatureExtractor.DataModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

/**
 * Runs jAudio (adapted from previous year's code)
 * 
 * @author Jeffrey
 */
public class jAudioRunner {
    String xmlDir = "";
    public jAudioRunner(String xmlDir){
        this.xmlDir = xmlDir;
    }
    
    /**
     * 
     * @param songDir Directory of the files with songs
     * @param batchFile Directory oof the batchfile
     * @param outputDir Full path to .arff output (including file and file extension)
     */
    public void run(String songDir, String batchFile, String outputFile){
        try{
            String mdir = "songDir"; //directory for songs
            File musicdir = new File(mdir);
            File allfiles[]= musicdir.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name){
                    if(name.toLowerCase().endsWith(".wav")){
                        return true;
                    }
                    return false;
                }
            }
            ); 
            int windowSize = 512; //size of the analysis window in samples
            double windowOverlap = 0; //percent overlap as a value between 0 and 1
            double samplingRate = 44100; //number of samples per second
            boolean normalize = false; //should the file be normalized before execution
            boolean perWindow = false; //should features be extracted on a window by window basis
            boolean overall = true; //should global features be extracted
            int outputType = 1; //what output format should extracted features be stored in
            String featureLocation = this.xmlDir; //location of the feature definition file

            Object[] o = new Object[] {};
            try {
                    o = (Object[]) XMLDocumentParser.parseXMLDocument(batchFile, //location of XML file with batch settings
                                    "batchFile");
            } catch (Exception e) {
                    System.out.println("Error parsing the batch file");
                    System.out.println(e.getMessage());
                    System.exit(3);
            }
            String featureDestination;
            Batch b;
            DataModel dm = new DataModel("features.xml",null);
            for (int i = 0; i < o.length; ++i) {
                featureDestination = outputFile; //location where extracted features should be stored
                b = (Batch) o[i];
                b.setDestination(featureLocation,featureDestination);
                dm.featureKey = new FileOutputStream(new File(b.getDestinationFK()));
                dm.featureValue = new FileOutputStream(new File(b.getDestinationFV()));
                b.setDataModel(dm);
                b.setSettings(windowSize,windowOverlap,samplingRate,normalize,perWindow,overall,outputType);
                b.setRecordings(allfiles);
                b.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
