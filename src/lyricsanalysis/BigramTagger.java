/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricsanalysis;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses TagHelper Tools to decompose text into a .arff format for building classifier
 * 
 * @author Jeffrey
 */
public class BigramTagger {
    String cpath = null; //path to csv file
    String tpath = null;
    
    /**
     * Creates a BigramTagger
     * 
     * @param tpath Path of the TagHelperTools2 directory
     * @param cpath Path to the CSV file
     */
    public BigramTagger(String tpath, String cpath){
        this.cpath = cpath;
        this.tpath = tpath + "\\";
    }
    
    /**
     * Runs TagHelperTools to create arff for training
     * .arff files are stored in the TagHelperTools2/ARFF directory
     */
    public void makeArffTrain(){
        try {
            Process p = Runtime.getRuntime().exec(tpath + "runtht.bat -ng -lang eng -f uni -f bi -f posbi -noev -cl [weka.classifiers.trees.RandomForest] " + cpath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Runs TagHelperTools to create arff for classification
     */
    public void makeArffSong(){
        
    }
}
